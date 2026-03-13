package eecs2311.group2.wh40k_easycombat.controller.helper;

import eecs2311.group2.wh40k_easycombat.model.aggregate.DatasheetAggregate;
import eecs2311.group2.wh40k_easycombat.model.Army;
import eecs2311.group2.wh40k_easycombat.model.Datasheets;
import eecs2311.group2.wh40k_easycombat.model.Datasheets_models_cost;
import eecs2311.group2.wh40k_easycombat.model.Detachments;
import eecs2311.group2.wh40k_easycombat.model.Enhancements;
import eecs2311.group2.wh40k_easycombat.model.Factions;
import eecs2311.group2.wh40k_easycombat.repository.DatasheetsRepository;
import eecs2311.group2.wh40k_easycombat.repository.DetachmentsRepository;
import eecs2311.group2.wh40k_easycombat.repository.EnhancementsRepository;
import eecs2311.group2.wh40k_easycombat.repository.FactionsRepository;
import eecs2311.group2.wh40k_easycombat.service.StaticDataService;
import eecs2311.group2.wh40k_easycombat.util.CostParser;
import eecs2311.group2.wh40k_easycombat.util.CostTier;
import eecs2311.group2.wh40k_easycombat.viewmodel.ArmySavedRowVM;
import eecs2311.group2.wh40k_easycombat.viewmodel.ArmyUnitTreeRowVM;
import eecs2311.group2.wh40k_easycombat.viewmodel.ArmyUnitVM;
import javafx.scene.control.TreeItem;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class ArmyControllerDataHelper {

    private ArmyControllerDataHelper() {
    }

    public static FactionDisplayData loadFactionDisplayData() {
        LinkedHashMap<String, String> displayToId = new LinkedHashMap<>();
        LinkedHashMap<String, String> idToDisplay = new LinkedHashMap<>();

        try {
            List<Factions> factions = FactionsRepository.getAllFactions();
            for (Factions faction : factions) {
                String id = safe(faction.id());
                if (id.isBlank()) continue;

                String display = safe(faction.name(), id);
                displayToId.put(display, id);
                idToDisplay.put(id, display);
            }
        } catch (Exception ignored) {
        }

        if (!displayToId.isEmpty()) {
            return new FactionDisplayData(displayToId, idToDisplay);
        }

        try {
            for (Datasheets datasheet : DatasheetsRepository.getAllDatasheets()) {
                String id = safe(datasheet.faction_id());
                if (id.isBlank() || "all".equalsIgnoreCase(id)) continue;

                displayToId.putIfAbsent(id, id);
                idToDisplay.putIfAbsent(id, id);
            }
        } catch (Exception ignored) {
        }

        return new FactionDisplayData(displayToId, idToDisplay);
    }

    public static Map<String, ArmyUnitVM.EnhancementEntry> loadEnhancementInfoById() {
        Map<String, ArmyUnitVM.EnhancementEntry> map = new LinkedHashMap<>();

        try {
            for (Enhancements enhancement : EnhancementsRepository.getAllEnhancements()) {
                String id = safe(enhancement.id());
                if (id.isBlank()) continue;

                map.put(id, new ArmyUnitVM.EnhancementEntry(
                        id,
                        safe(enhancement.name(), id),
                        parseIntSafe(enhancement.cost()),
                        safe(enhancement.detachment_id()),
                        safe(enhancement.faction_id())
                ));
            }
        } catch (Exception ignored) {
        }

        return map;
    }

    public static DetachmentDisplayData loadDetachmentDisplayData(String factionId) {
        LinkedHashMap<String, String> displayToId = new LinkedHashMap<>();
        LinkedHashMap<String, String> idToDisplay = new LinkedHashMap<>();

        try {
            for (Detachments detachment : DetachmentsRepository.getAllDetachments()) {
                String id = safe(detachment.id());
                if (id.isBlank()) continue;

                String rowFactionId = safe(detachment.faction_id());
                if (!safe(factionId).isBlank()
                        && !rowFactionId.isBlank()
                        && !safe(factionId).equalsIgnoreCase(rowFactionId)) {
                    continue;
                }

                String display = safe(detachment.name(), id);
                displayToId.put(display, id);
                idToDisplay.put(id, display);
            }
        } catch (Exception ignored) {
        }

        return new DetachmentDisplayData(displayToId, idToDisplay);
    }

    public static List<ArmySavedRowVM> loadSavedArmyRows() {
        List<ArmySavedRowVM> rows = new ArrayList<>();

        try {
            for (Army army : StaticDataService.getAllArmies()) {
                rows.add(new ArmySavedRowVM(
                        army.auto_id(),
                        army.name(),
                        army.total_points(),
                        army.isMarked()
                ));
            }
        } catch (Exception ignored) {
        }

        rows.sort(Comparator
                .comparing(ArmySavedRowVM::marked).reversed()
                .thenComparing(ArmySavedRowVM::armyName, String.CASE_INSENSITIVE_ORDER));

        return rows;
    }

    public static TreeItem<ArmyUnitTreeRowVM> buildUnitTree(String factionId) {
        TreeItem<ArmyUnitTreeRowVM> root = new TreeItem<>(ArmyUnitTreeRowVM.group("ROOT"));
        root.setExpanded(true);

        if (safe(factionId).isBlank()) {
            return root;
        }

        List<Datasheets> allDatasheets;
        try {
            allDatasheets = DatasheetsRepository.getAllDatasheets();
        } catch (Exception e) {
            return root;
        }

        LinkedHashMap<String, List<ArmyUnitTreeRowVM>> grouped = new LinkedHashMap<>();

        for (Datasheets datasheet : allDatasheets) {
            if (datasheet == null) continue;
            if (!safe(factionId).equalsIgnoreCase(safe(datasheet.faction_id()))) continue;

            try {
                DatasheetAggregate bundle = StaticDataService.getDatasheetBundle(datasheet.id());
                if (bundle == null) continue;

                String role = normalizeRole(datasheet.role());
                int minPoints = basePoints(bundle.costs);

                grouped.computeIfAbsent(role, ignored -> new ArrayList<>())
                        .add(new ArmyUnitTreeRowVM(
                                safe(datasheet.name(), datasheet.id()),
                                datasheet.id(),
                                role,
                                minPoints,
                                false
                        ));
            } catch (Exception ignored) {
            }
        }

        for (Map.Entry<String, List<ArmyUnitTreeRowVM>> entry : grouped.entrySet()) {
            entry.getValue().sort(Comparator.comparing(ArmyUnitTreeRowVM::displayName, String.CASE_INSENSITIVE_ORDER));

            TreeItem<ArmyUnitTreeRowVM> roleNode = new TreeItem<>(ArmyUnitTreeRowVM.group(entry.getKey()));
            roleNode.setExpanded(true);

            for (ArmyUnitTreeRowVM row : entry.getValue()) {
                roleNode.getChildren().add(new TreeItem<>(row));
            }

            root.getChildren().add(roleNode);
        }

        return root;
    }

    public record FactionDisplayData(
            LinkedHashMap<String, String> displayToId,
            LinkedHashMap<String, String> idToDisplay
    ) {
    }

    public record DetachmentDisplayData(
            LinkedHashMap<String, String> displayToId,
            LinkedHashMap<String, String> idToDisplay
    ) {
    }

    private static int basePoints(List<Datasheets_models_cost> costs) {
        List<CostTier> tiers = CostParser.parseTiers(costs);
        return tiers.isEmpty() ? 0 : tiers.get(0).points();
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

    private static String normalizeRole(String role) {
        return safe(role, "Other");
    }

    private static String safe(String value) {
        return value == null ? "" : value.trim();
    }

    private static String safe(String value, String fallback) {
        String normalized = safe(value);
        return normalized.isBlank() ? safe(fallback) : normalized;
    }
}
