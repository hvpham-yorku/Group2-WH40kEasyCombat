package eecs2311.group2.wh40k_easycombat.service.game;

import eecs2311.group2.wh40k_easycombat.model.combat.CasualtyUpdate;
import eecs2311.group2.wh40k_easycombat.model.instance.UnitInstance;
import eecs2311.group2.wh40k_easycombat.model.instance.UnitModelInstance;
import eecs2311.group2.wh40k_easycombat.viewmodel.GameArmyUnitVM;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public final class ArmyListStateService {
    private static final Comparator<GameArmyUnitVM> UNIT_ORDER = Comparator
            .comparing(GameArmyUnitVM::isDestroyed)
            .thenComparingInt(GameArmyUnitVM::getDisplayOrder)
            .thenComparing(GameArmyUnitVM::getUnitName, String.CASE_INSENSITIVE_ORDER);

    private ArmyListStateService() {
    }

    public static void initializeDisplayOrder(List<GameArmyUnitVM> units) {
        if (units == null) {
            return;
        }

        for (int i = 0; i < units.size(); i++) {
            GameArmyUnitVM unit = units.get(i);
            if (unit != null) {
                unit.setDisplayOrder(i);
            }
        }
    }

    public static void refreshArmyOrdering(ObservableList<GameArmyUnitVM> units) {
        if (units == null) {
            return;
        }
        FXCollections.sort(units, UNIT_ORDER);
    }

    public static void reconcileUnitState(UnitInstance unit) {
        if (unit == null) {
            return;
        }

        int destroyedModels = 0;
        for (UnitModelInstance model : unit.getModels()) {
            if (model != null && model.isDestroyed()) {
                destroyedModels++;
            }
        }

        while (unit.getRemovedWeaponKeysForDestroyedModels().size() < destroyedModels) {
            String removedWeapon = unit.decrementRepeatedWeaponForCasualty();
            if (removedWeapon.isBlank()) {
                break;
            }
        }

        while (unit.getRemovedWeaponKeysForDestroyedModels().size() > destroyedModels) {
            String restoredWeapon = unit.restoreWeaponForRevivedModel();
            if (restoredWeapon.isBlank()) {
                break;
            }
        }
    }

    public static Set<String> destroyedModelIds(UnitInstance unit) {
        Set<String> result = new HashSet<>();
        if (unit == null) {
            return result;
        }

        for (UnitModelInstance model : unit.getModels()) {
            if (model != null && model.isDestroyed()) {
                result.add(model.getInstanceId());
            }
        }

        return result;
    }

    public static CasualtyUpdate applyCasualties(UnitInstance defender, Set<String> destroyedBefore) {
        if (defender == null) {
            return CasualtyUpdate.none();
        }

        Set<String> baseline = destroyedBefore == null ? Set.of() : destroyedBefore;
        List<String> destroyedModels = new ArrayList<>();
        List<String> removedWeapons = new ArrayList<>();

        for (UnitModelInstance model : defender.getModels()) {
            if (model == null || !model.isDestroyed()) {
                continue;
            }
            if (baseline.contains(model.getInstanceId())) {
                continue;
            }

            destroyedModels.add(model.getModelName());

            String removedWeapon = defender.decrementRepeatedWeaponForCasualty();
            if (!removedWeapon.isBlank()) {
                removedWeapons.add(removedWeapon);
            }
        }

        reconcileUnitState(defender);

        return new CasualtyUpdate(
                destroyedModels.size(),
                destroyedModels,
                removedWeapons,
                defender.isDestroyed()
        );
    }
}
