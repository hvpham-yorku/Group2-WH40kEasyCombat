package eecs2311.group2.wh40k_easycombat.service;

import eecs2311.group2.wh40k_easycombat.model.Army;
import eecs2311.group2.wh40k_easycombat.model.Army_detachment;
import eecs2311.group2.wh40k_easycombat.model.Army_units;
import eecs2311.group2.wh40k_easycombat.model.Army_wargear;
import eecs2311.group2.wh40k_easycombat.viewmodel.ArmyUnitVM;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public final class ArmyControllerPersistence {

    private static final List<Integer> SIZE_OPTIONS = List.of(500, 1000, 1500, 2000, 3000);

    private ArmyControllerPersistence() {
    }

    public static ArmyCrudService.ArmyWriteBundle buildWriteBundle(
            Integer editingArmyId,
            boolean editingArmyMarked,
            String armyName,
            String factionId,
            String detachmentId,
            List<ArmyUnitVM> currentArmy
    ) {
        ArmyUnitVM warlord = currentArmy.stream()
                .filter(x -> x.warlordProperty().get())
                .findFirst()
                .orElse(null);

        int totalPoints = currentArmy.stream().mapToInt(x -> x.pointsProperty().get()).sum();

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
        for (ArmyUnitVM unit : currentArmy) {
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

        return new ArmyCrudService.ArmyWriteBundle(army, detachments, units, wargears);
    }

    public static LoadedArmyData loadArmyForEdit(
            int armyId,
            Map<String, ArmyUnitVM.EnhancementEntry> enhancementInfoById
    ) throws Exception {
        StaticDataService.ArmyBundle bundle = StaticDataService.getArmyBundle(armyId);
        if (bundle == null || bundle.army == null) {
            return null;
        }

        String detachmentId = null;
        List<Army_detachment> savedDetachments = StaticDataService.getArmyDetachments(bundle.army.auto_id());
        if (!savedDetachments.isEmpty()) {
            detachmentId = savedDetachments.get(0).detachment_id();
        }

        int suggestedLimit = SIZE_OPTIONS.stream()
                .filter(x -> x >= bundle.army.total_points())
                .findFirst()
                .orElse(3000);

        List<ArmyUnitVM> units = new ArrayList<>();

        for (Army_units savedUnit : bundle.units) {
            StaticDataService.DatasheetBundle datasheetBundle =
                    StaticDataService.getDatasheetBundle(savedUnit.datasheet_id());

            if (datasheetBundle == null) continue;

            ArmyUnitVM vm = UnitFactory.create(datasheetBundle, enhancementInfoById);
            vm.setModelCount(savedUnit.model_count());

            if (savedUnit.enhancements_id() != null && !savedUnit.enhancements_id().isBlank()) {
                for (ArmyUnitVM.EnhancementEntry e : vm.getEnhancements()) {
                    if (Objects.equals(e.getId(), savedUnit.enhancements_id())) {
                        vm.setEnhancement(e);
                        break;
                    }
                }
            }

            for (Army_wargear wg : StaticDataService.getArmyWargearByUnitId(savedUnit.auto_id())) {
                vm.setWargearCount(wg.wargear_id(), wg.wargear_count());
            }

            if (Objects.equals(bundle.army.warlord_id(), savedUnit.datasheet_id())) {
                vm.warlordProperty().set(true);
            }

            units.add(vm);
        }

        return new LoadedArmyData(bundle.army, detachmentId, suggestedLimit, units);
    }

    public record LoadedArmyData(
            Army army,
            String detachmentId,
            int sizeLimit,
            List<ArmyUnitVM> units
    ) {
    }
}