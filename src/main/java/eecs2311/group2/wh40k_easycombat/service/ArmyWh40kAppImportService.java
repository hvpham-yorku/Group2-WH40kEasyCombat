package eecs2311.group2.wh40k_easycombat.service;

import eecs2311.group2.wh40k_easycombat.model.Datasheets;
import eecs2311.group2.wh40k_easycombat.model.Datasheets_models;
import eecs2311.group2.wh40k_easycombat.model.aggregate.DatasheetAggregate;
import eecs2311.group2.wh40k_easycombat.repository.DatasheetsRepository;
import eecs2311.group2.wh40k_easycombat.util.CostParser;
import eecs2311.group2.wh40k_easycombat.util.CostTier;
import eecs2311.group2.wh40k_easycombat.viewmodel.ArmyUnitVM;

import java.text.Normalizer;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class ArmyWh40kAppImportService {

    private static final Pattern UNIT_HEADER_PATTERN =
            Pattern.compile("^(.+?)\\s*\\((\\d+)\\s+points?\\)$", Pattern.CASE_INSENSITIVE);

    private static final Pattern COUNTED_LINE_PATTERN =
            Pattern.compile("^(\\d+)x\\s+(.+)$", Pattern.CASE_INSENSITIVE);

    private ArmyWh40kAppImportService() {
    }

    public static ImportResult importArmyText(
            String rawText,
            Map<String, ArmyUnitVM.EnhancementEntry> enhancementInfoById
    ) throws Exception {
        String safeText = rawText == null ? "" : rawText.replace("\r\n", "\n").replace('\r', '\n');
        List<String> rawLines = List.of(safeText.split("\n", -1));

        Metadata metadata = parseMetadata(rawLines);
        if (metadata.armyName().isBlank()) {
            throw new IllegalArgumentException("Please paste one full WH40K App export.");
        }

        List<UnitBlock> unitBlocks = parseUnitBlocks(rawLines);
        if (unitBlocks.isEmpty()) {
            throw new IllegalArgumentException("No unit entries were found in the pasted text.");
        }

        DatasheetLookup lookup = buildDatasheetLookup();

        List<ArmyUnitVM> importedUnits = new ArrayList<>();
        LinkedHashSet<String> skippedUnits = new LinkedHashSet<>();
        LinkedHashSet<String> skippedItems = new LinkedHashSet<>();
        LinkedHashSet<String> warnings = new LinkedHashSet<>();

        for (UnitBlock unitBlock : unitBlocks) {
            DatasheetAggregate bundle = lookup.find(unitBlock.unitName());
            if (bundle == null) {
                skippedUnits.add(unitBlock.unitName());
                continue;
            }

            ArmyUnitVM vm = ArmyEditorService.createArmyUnitVM(bundle, enhancementInfoById);
            applyParsedUnitBlock(vm, bundle, unitBlock, skippedItems, warnings);
            importedUnits.add(vm);
        }

        return new ImportResult(
                metadata.armyName(),
                metadata.factionName(),
                metadata.detachmentName(),
                List.copyOf(importedUnits),
                List.copyOf(skippedUnits),
                List.copyOf(skippedItems),
                List.copyOf(warnings)
        );
    }

    private static Metadata parseMetadata(List<String> rawLines) {
        List<String> headerLines = new ArrayList<>();

        for (String rawLine : rawLines) {
            String cleaned = cleanLine(rawLine);
            if (cleaned.isBlank()) {
                continue;
            }
            if (isSectionHeader(cleaned)) {
                break;
            }
            headerLines.add(cleaned);
        }

        String armyName = headerLines.size() > 0 ? headerLines.get(0) : "";
        String factionName = headerLines.size() > 1 ? headerLines.get(1) : "";
        String detachmentName = headerLines.size() > 4 ? headerLines.get(4) : "";

        return new Metadata(armyName, factionName, detachmentName);
    }

    private static List<UnitBlock> parseUnitBlocks(List<String> rawLines) {
        List<UnitBlock> blocks = new ArrayList<>();
        boolean insideUnitSections = false;
        UnitBlockBuilder current = null;

        for (String rawLine : rawLines) {
            String cleaned = cleanLine(rawLine);
            if (cleaned.isBlank()) {
                continue;
            }

            if (cleaned.toLowerCase(Locale.ROOT).startsWith("exported with app version")) {
                break;
            }

            if (isSectionHeader(cleaned)) {
                insideUnitSections = true;
                if (current != null) {
                    blocks.add(current.build());
                    current = null;
                }
                continue;
            }

            if (!insideUnitSections) {
                continue;
            }

            Matcher unitHeaderMatcher = UNIT_HEADER_PATTERN.matcher(cleaned);
            if (unitHeaderMatcher.matches()) {
                if (current != null) {
                    blocks.add(current.build());
                }

                current = new UnitBlockBuilder(
                        unitHeaderMatcher.group(1).trim(),
                        parseIntSafe(unitHeaderMatcher.group(2))
                );
                continue;
            }

            if (current != null) {
                current.lines.add(new ParsedLine(countLeadingSpaces(rawLine), cleaned));
            }
        }

        if (current != null) {
            blocks.add(current.build());
        }

        return blocks;
    }

    private static DatasheetLookup buildDatasheetLookup() throws Exception {
        StaticDataService.loadAllFromSqlite();

        Map<String, List<DatasheetAggregate>> exact = new LinkedHashMap<>();
        List<DatasheetAggregate> allBundles = new ArrayList<>();

        for (Datasheets datasheet : DatasheetsRepository.getAllDatasheets()) {
            if (datasheet == null || datasheet.id() == null || datasheet.id().isBlank()) {
                continue;
            }

            DatasheetAggregate bundle = StaticDataService.getDatasheetBundle(datasheet.id());
            if (bundle == null || bundle.datasheet == null) {
                continue;
            }

            allBundles.add(bundle);
            exact.computeIfAbsent(normalize(bundle.datasheet.name()), ignored -> new ArrayList<>()).add(bundle);
        }

        return new DatasheetLookup(exact, List.copyOf(allBundles));
    }

    private static void applyParsedUnitBlock(
            ArmyUnitVM vm,
            DatasheetAggregate bundle,
            UnitBlock unitBlock,
            Set<String> skippedItems,
            Set<String> warnings
    ) {
        ParsedUnitDetails details = parseUnitDetails(bundle, unitBlock.points(), unitBlock.lines());

        if (details.modelCount() > 0) {
            vm.setModelCount(details.modelCount());
        }

        Map<String, ArmyUnitVM.WargearEntry> wargearByName = new LinkedHashMap<>();
        for (ArmyUnitVM.WargearEntry wargear : vm.getWargears()) {
            wargearByName.putIfAbsent(normalize(wargear.getName()), wargear);
        }

        for (Map.Entry<String, Integer> entry : details.wargearCounts().entrySet()) {
            ArmyUnitVM.WargearEntry wargear = wargearByName.get(entry.getKey());
            if (wargear == null) {
                skippedItems.add(unitBlock.unitName() + " - " + entry.getValue() + "x " + details.wargearDisplayName(entry.getKey()));
                continue;
            }

            vm.setWargearCount(wargear.getAutoId(), entry.getValue());
        }

        if (details.warlord()) {
            vm.warlordProperty().set(true);
        }

        if (!details.enhancementName().isBlank()) {
            boolean applied = false;
            String expected = normalize(details.enhancementName());

            for (ArmyUnitVM.EnhancementEntry enhancement : vm.getEnhancements()) {
                if (normalize(enhancement.getName()).equals(expected)
                        || normalize(enhancement.getId()).equals(expected)) {
                    vm.setEnhancement(enhancement);
                    applied = true;
                    break;
                }
            }

            if (!applied) {
                warnings.add(unitBlock.unitName() + " - enhancement not found: " + details.enhancementName());
            }
        }
    }

    private static ParsedUnitDetails parseUnitDetails(
            DatasheetAggregate bundle,
            int unitPoints,
            List<ParsedLine> lines
    ) {
        Map<String, Integer> wargearCounts = new LinkedHashMap<>();
        Map<String, String> wargearDisplayNames = new LinkedHashMap<>();
        Set<String> modelNames = new LinkedHashSet<>();

        for (Datasheets_models model : bundle.models) {
            String name = safe(model.name());
            if (!name.isBlank()) {
                modelNames.add(normalize(name));
            }
        }

        int matchedModelCount = 0;
        int topLevelCountSum = 0;
        String enhancementName = "";
        boolean warlord = false;

        for (ParsedLine parsedLine : lines) {
            String text = parsedLine.text();
            if (text.equalsIgnoreCase("Warlord")) {
                warlord = true;
                continue;
            }

            if (text.regionMatches(true, 0, "Enhancement:", 0, "Enhancement:".length())) {
                enhancementName = safe(text.substring("Enhancement:".length()));
                continue;
            }

            Matcher countedMatcher = COUNTED_LINE_PATTERN.matcher(text);
            if (!countedMatcher.matches()) {
                continue;
            }

            int count = parseIntSafe(countedMatcher.group(1));
            if (count <= 0) {
                continue;
            }

            String itemName = safe(countedMatcher.group(2));
            String normalizedItemName = normalize(itemName);
            boolean topLevel = parsedLine.indent() <= 2;
            boolean matchesModel = modelNames.contains(normalizedItemName);

            if (topLevel) {
                topLevelCountSum += count;
            }

            if (matchesModel) {
                matchedModelCount += count;
                continue;
            }

            wargearCounts.merge(normalizedItemName, count, Integer::sum);
            wargearDisplayNames.putIfAbsent(normalizedItemName, itemName);
        }

        int textDerivedModelCount = matchedModelCount > 0 ? matchedModelCount : topLevelCountSum;
        int modelCount = inferModelCountFromCost(bundle, unitPoints, textDerivedModelCount);
        if (modelCount <= 0) {
            modelCount = textDerivedModelCount;
        }

        return new ParsedUnitDetails(modelCount, wargearCounts, wargearDisplayNames, enhancementName, warlord);
    }

    private static int inferModelCountFromCost(
            DatasheetAggregate bundle,
            int unitPoints,
            int textDerivedModelCount
    ) {
        if (bundle == null || unitPoints <= 0) {
            return 0;
        }

        List<CostTier> tiers = CostParser.parseTiers(bundle.costs);
        if (tiers.isEmpty()) {
            return 0;
        }

        List<CostTier> exactMatches = tiers.stream()
                .filter(tier -> tier.points() == unitPoints)
                .sorted((left, right) -> Integer.compare(left.models(), right.models()))
                .toList();

        if (exactMatches.isEmpty()) {
            return 0;
        }

        if (exactMatches.size() == 1 || textDerivedModelCount <= 0) {
            return exactMatches.getFirst().models();
        }

        CostTier closest = exactMatches.getFirst();
        int closestDistance = Math.abs(closest.models() - textDerivedModelCount);

        for (int i = 1; i < exactMatches.size(); i++) {
            CostTier candidate = exactMatches.get(i);
            int candidateDistance = Math.abs(candidate.models() - textDerivedModelCount);
            if (candidateDistance < closestDistance) {
                closest = candidate;
                closestDistance = candidateDistance;
            }
        }

        return closest.models();
    }

    private static int countLeadingSpaces(String rawLine) {
        if (rawLine == null || rawLine.isEmpty()) {
            return 0;
        }

        int spaces = 0;
        for (int i = 0; i < rawLine.length(); i++) {
            char current = rawLine.charAt(i);
            if (current == ' ') {
                spaces++;
            } else if (current == '\t') {
                spaces += 4;
            } else {
                break;
            }
        }

        return spaces;
    }

    private static boolean isSectionHeader(String line) {
        if (line == null || line.isBlank()) {
            return false;
        }
        if (line.contains("(") || line.contains(")")) {
            return false;
        }
        if (line.toLowerCase(Locale.ROOT).startsWith("exported with app version")) {
            return false;
        }
        return line.equals(line.toUpperCase(Locale.ROOT)) && line.chars().anyMatch(Character::isLetter);
    }

    private static int parseIntSafe(String text) {
        if (text == null || text.isBlank()) {
            return 0;
        }

        String cleaned = text.replaceAll("[^0-9-]", "").trim();
        if (cleaned.isBlank()) {
            return 0;
        }

        try {
            return Integer.parseInt(cleaned);
        } catch (Exception e) {
            return 0;
        }
    }

    private static String cleanLine(String rawLine) {
        String cleaned = rawLine == null ? "" : rawLine
                .replace('\u00a0', ' ')
                .replace("鈥檚", "'s")
                .replace("鈥檛", "n't")
                .replace("’", "'")
                .replace("“", "\"")
                .replace("”", "\"")
                .trim();

        while (!cleaned.isEmpty()) {
            char current = cleaned.charAt(0);
            if (Character.isLetterOrDigit(current) || current == '(') {
                break;
            }
            cleaned = cleaned.substring(1).trim();
        }

        return cleaned;
    }

    private static String normalize(String text) {
        String cleaned = cleanLine(text)
                .replace('&', ' ')
                .replace('-', ' ')
                .replace('_', ' ')
                .replace('/', ' ')
                .replaceAll("[^\\p{IsAlphabetic}\\p{IsDigit}' ]", " ");

        String normalized = Normalizer.normalize(cleaned, Normalizer.Form.NFKD)
                .replaceAll("\\p{M}+", "")
                .toLowerCase(Locale.ROOT)
                .replaceAll("\\s+", " ")
                .trim();

        return normalized;
    }

    private static String safe(String text) {
        return text == null ? "" : text.trim();
    }

    public record ImportResult(
            String armyName,
            String factionName,
            String detachmentName,
            List<ArmyUnitVM> units,
            List<String> skippedUnits,
            List<String> skippedItems,
            List<String> warnings
    ) {
    }

    private record Metadata(String armyName, String factionName, String detachmentName) {
    }

    private record ParsedLine(int indent, String text) {
    }

    private record UnitBlock(String unitName, int points, List<ParsedLine> lines) {
    }

    private record ParsedUnitDetails(
            int modelCount,
            Map<String, Integer> wargearCounts,
            Map<String, String> wargearDisplayNames,
            String enhancementName,
            boolean warlord
    ) {
        private String wargearDisplayName(String normalizedName) {
            return wargearDisplayNames.getOrDefault(normalizedName, normalizedName);
        }
    }

    private record DatasheetLookup(
            Map<String, List<DatasheetAggregate>> exactByNormalizedName,
            List<DatasheetAggregate> allBundles
    ) {
        private DatasheetAggregate find(String importedUnitName) {
            String normalized = normalize(importedUnitName);
            List<DatasheetAggregate> exactMatches = exactByNormalizedName.get(normalized);
            if (exactMatches != null && !exactMatches.isEmpty()) {
                return exactMatches.get(0);
            }

            DatasheetAggregate uniqueContainsMatch = null;
            for (DatasheetAggregate bundle : allBundles) {
                String candidate = normalize(bundle.datasheet.name());
                if (!candidate.contains(normalized) && !normalized.contains(candidate)) {
                    continue;
                }

                if (uniqueContainsMatch != null) {
                    return null;
                }
                uniqueContainsMatch = bundle;
            }

            return uniqueContainsMatch;
        }
    }

    private static final class UnitBlockBuilder {
        private final String unitName;
        private final int points;
        private final List<ParsedLine> lines = new ArrayList<>();

        private UnitBlockBuilder(String unitName, int points) {
            this.unitName = unitName;
            this.points = points;
        }

        private UnitBlock build() {
            return new UnitBlock(unitName, points, List.copyOf(lines));
        }
    }
}
