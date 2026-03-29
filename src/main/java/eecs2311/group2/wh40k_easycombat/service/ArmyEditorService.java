package eecs2311.group2.wh40k_easycombat.service;

import eecs2311.group2.wh40k_easycombat.model.aggregate.ArmyAggregate;
import eecs2311.group2.wh40k_easycombat.model.aggregate.ArmyWriteAggregate;
import eecs2311.group2.wh40k_easycombat.model.aggregate.DatasheetAggregate;
import eecs2311.group2.wh40k_easycombat.model.Army;
import eecs2311.group2.wh40k_easycombat.model.Army_detachment;
import eecs2311.group2.wh40k_easycombat.model.Army_units;
import eecs2311.group2.wh40k_easycombat.model.Army_wargear;
import eecs2311.group2.wh40k_easycombat.model.Datasheets;
import eecs2311.group2.wh40k_easycombat.model.Datasheets_enhancements;
import eecs2311.group2.wh40k_easycombat.model.Datasheets_keywords;
import eecs2311.group2.wh40k_easycombat.model.Datasheets_wargear;
import eecs2311.group2.wh40k_easycombat.util.CostParser;
import eecs2311.group2.wh40k_easycombat.util.CostTier;
import eecs2311.group2.wh40k_easycombat.util.StatFormatter;
import eecs2311.group2.wh40k_easycombat.viewmodel.ArmyEditorLoadVM;
import eecs2311.group2.wh40k_easycombat.viewmodel.ArmyUnitVM;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

public final class ArmyEditorService {

    private static final List<Integer> SIZE_OPTIONS = List.of(500, 1000, 1500, 2000, 3000);

    private ArmyEditorService() {
    }

    public static ArmyWriteAggregate buildWriteBundle(
            Integer editingArmyId,
            boolean editingArmyMarked,
            String armyName,
            String factionId,
            String detachmentId,
            List<ArmyUnitVM> currentArmy
    ) {
        List<ArmyUnitVM> safeArmy = currentArmy == null ? List.of() : currentArmy;

        ArmyUnitVM warlord = safeArmy.stream()
                .filter(x -> x.warlordProperty().get())
                .findFirst()
                .orElse(null);

        int totalPoints = safeArmy.stream()
                .mapToInt(x -> x.pointsProperty().get())
                .sum();

        Army army = new Army(
                editingArmyId == null ? 0 : editingArmyId,
                armyName,
                factionId,
                warlord == null ? "" : warlord.getDatasheetId(),
                totalPoints,
                editingArmyMarked
        );

        List<Army_detachment> detachments = List.of(
                new Army_detachment(0, 0, factionId, detachmentId)
        );

        List<Army_units> units = new ArrayList<>();
        List<Army_wargear> wargears = new ArrayList<>();

        int tempUnitId = -1;
        for (ArmyUnitVM unit : safeArmy) {
            units.add(new Army_units(
                    tempUnitId,
                    0,
                    unit.getDatasheetId(),
                    unit.getEnhancementId(),
                    unit.modelCountProperty().get(),
                    unit.pointsProperty().get()
            ));

            for (ArmyUnitVM.WargearEntry wg : unit.getWargears()) {
                if (wg.getCount() > 0) {
                    wargears.add(new Army_wargear(
                            0,
                            wg.getAutoId(),
                            tempUnitId,
                            wg.getCount()
                    ));
                }
            }

            tempUnitId--;
        }

        return new ArmyWriteAggregate(army, detachments, units, wargears);
    }

    public static ArmyEditorLoadVM loadArmyForEdit(
            int armyId,
            Map<String, ArmyUnitVM.EnhancementEntry> enhancementInfoById
    ) throws Exception {
        ArmyAggregate bundle = StaticDataService.getArmyBundle(armyId);
        if (bundle == null || bundle.army == null) {
            return null;
        }

        String detachmentId = loadPrimaryDetachmentId(bundle.army.auto_id());
        int suggestedLimit = suggestSizeLimit(bundle.army.total_points());

        List<ArmyUnitVM> units = new ArrayList<>();

        for (Army_units savedUnit : bundle.units) {
            DatasheetAggregate datasheetBundle = StaticDataService.getDatasheetBundle(savedUnit.datasheet_id());
            if (datasheetBundle == null) {
                continue;
            }

            ArmyUnitVM vm = createArmyUnitVM(datasheetBundle, enhancementInfoById);
            applySavedUnitState(vm, savedUnit, bundle.army.warlord_id());
            units.add(vm);
        }

        return new ArmyEditorLoadVM(bundle.army, detachmentId, suggestedLimit, units);
    }

    public static ArmyUnitVM createArmyUnitVM(
            DatasheetAggregate bundle,
            Map<String, ArmyUnitVM.EnhancementEntry> enhancementInfoById
    ) {
        if (bundle == null || bundle.datasheet == null) {
            throw new IllegalArgumentException("Datasheet bundle is null.");
        }

        Datasheets datasheet = bundle.datasheet;

        String datasheetId = datasheet.id();
        String unitName = safeText(datasheet.name(), datasheet.id());
        String statLine = StatFormatter.buildStatLine(bundle.models);
        String role = safeText(datasheet.role(), "Other");

        boolean character = isCharacterRole(role) || hasCharacterKeyword(bundle.keywords);
        List<CostTier> tiers = CostParser.parseTiers(bundle.costs);

        ArmyUnitVM vm = new ArmyUnitVM(
                datasheetId,
                unitName,
                statLine,
                role,
                character,
                tiers
        );

        if (bundle.wargear != null) {
            for (Datasheets_wargear wargear : bundle.wargear) {
                vm.addWargear(wargear.auto_id(), resolveWargearName(wargear));
            }
        }

        if (bundle.enhancements != null) {
            for (Datasheets_enhancements enhancement : bundle.enhancements) {
                String enhancementId = enhancement.enhancement_id();

                ArmyUnitVM.EnhancementEntry info =
                        enhancementInfoById == null ? null : enhancementInfoById.get(enhancementId);

                if (info != null) {
                    vm.addEnhancement(
                            info.getId(),
                            info.getName(),
                            info.getCost(),
                            info.getDetachmentId(),
                            info.getFactionId()
                    );
                } else {
                    vm.addEnhancement(enhancementId, enhancementId, 0);
                }
            }
        }

        return vm;
    }

    private static void applySavedUnitState(
            ArmyUnitVM vm,
            Army_units savedUnit,
            String warlordDatasheetId
    ) throws Exception {
        vm.setModelCount(savedUnit.model_count());
        applySavedEnhancement(vm, savedUnit.enhancements_id());

        for (Army_wargear wg : StaticDataService.getArmyWargearByUnitId(savedUnit.auto_id())) {
            vm.setWargearCount(wg.wargear_id(), wg.wargear_count());
        }

        if (Objects.equals(warlordDatasheetId, savedUnit.datasheet_id())) {
            vm.warlordProperty().set(true);
        }
    }

    private static void applySavedEnhancement(ArmyUnitVM vm, String enhancementId) {
        if (enhancementId == null || enhancementId.isBlank()) {
            return;
        }

        for (ArmyUnitVM.EnhancementEntry entry : vm.getEnhancements()) {
            if (Objects.equals(entry.getId(), enhancementId)) {
                vm.setEnhancement(entry);
                return;
            }
        }
    }

    private static String loadPrimaryDetachmentId(int armyId) throws Exception {
        List<Army_detachment> savedDetachments = StaticDataService.getArmyDetachments(armyId);
        if (savedDetachments.isEmpty()) {
            return null;
        }
        return savedDetachments.get(0).detachment_id();
    }

    private static int suggestSizeLimit(int totalPoints) {
        return SIZE_OPTIONS.stream()
                .filter(x -> x >= totalPoints)
                .findFirst()
                .orElse(3000);
    }

    private static String resolveWargearName(Datasheets_wargear wargear) {
        if (wargear == null) {
            return "Wargear";
        }

        if (wargear.name() != null && !wargear.name().isBlank()) {
            return wargear.name();
        }

        if (wargear.description() != null && !wargear.description().isBlank()) {
            return wargear.description();
        }

        return "Wargear";
    }

    private static boolean isCharacterRole(String role) {
        return role != null && role.toLowerCase(Locale.ROOT).contains("character");
    }

    private static boolean hasCharacterKeyword(List<Datasheets_keywords> keywords) {
        if (keywords == null) {
            return false;
        }

        for (Datasheets_keywords keyword : keywords) {
            if (keyword.keyword() != null && keyword.keyword().trim().equalsIgnoreCase("character")) {
                return true;
            }
        }

        return false;
    }

    private static String safeText(String primary, String fallback) {
        if (primary != null && !primary.isBlank()) {
            return primary;
        }
        return fallback == null ? "" : fallback;
    }
}