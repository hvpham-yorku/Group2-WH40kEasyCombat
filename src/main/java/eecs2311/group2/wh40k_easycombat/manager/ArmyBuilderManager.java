package eecs2311.group2.wh40k_easycombat.manager;

import eecs2311.group2.wh40k_easycombat.viewmodel.ArmyUnitVM;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.util.Comparator;
import java.util.List;

public final class ArmyBuilderManager {

    private ArmyBuilderManager() {
    }

    public static void addUnit(ObservableList<ArmyUnitVM> currentArmy, ArmyUnitVM unit) {
        if (currentArmy == null || unit == null) return;
        currentArmy.add(unit);
        sortArmy(currentArmy);
    }

    public static void removeUnit(ObservableList<ArmyUnitVM> currentArmy, ArmyUnitVM unit) {
        if (currentArmy == null || unit == null) return;
        currentArmy.remove(unit);
        sortArmy(currentArmy);
    }

    public static void clearArmy(ObservableList<ArmyUnitVM> currentArmy) {
        if (currentArmy == null) return;
        currentArmy.clear();
    }

    public static void sortArmy(ObservableList<ArmyUnitVM> currentArmy) {
        if (currentArmy == null) return;

        FXCollections.sort(currentArmy,
                Comparator.comparing(ArmyUnitVM::getRole, String.CASE_INSENSITIVE_ORDER)
                        .thenComparing(ArmyUnitVM::getUnitName, String.CASE_INSENSITIVE_ORDER));
    }

    public static void toggleWarlord(List<ArmyUnitVM> currentArmy, ArmyUnitVM target) {
        if (currentArmy == null || target == null || !target.isCharacter()) {
            return;
        }

        boolean wasWarlord = target.warlordProperty().get();

        for (ArmyUnitVM unit : currentArmy) {
            unit.warlordProperty().set(false);
        }

        if (!wasWarlord) {
            target.warlordProperty().set(true);
        }
    }

    public static List<ArmyUnitVM.EnhancementEntry> buildAvailableEnhancements(
            List<ArmyUnitVM> currentArmy,
            ArmyUnitVM vm,
            String selectedFactionId,
            String selectedDetachmentId
    ) {
        ObservableList<ArmyUnitVM.EnhancementEntry> list = FXCollections.observableArrayList();
        list.add(new ArmyUnitVM.EnhancementEntry("", "No Enhancement", 0));

        if (vm == null) {
            return list;
        }

        for (ArmyUnitVM.EnhancementEntry e : vm.getEnhancements()) {
            boolean sameAsCurrent = e.getId().equals(vm.getEnhancementId());

            boolean uniqueOk = sameAsCurrent || currentArmy.stream()
                    .noneMatch(x -> x != vm && e.getId().equals(x.getEnhancementId()));

            boolean factionOk = e.getFactionId().isBlank()
                    || selectedFactionId == null
                    || selectedFactionId.isBlank()
                    || e.getFactionId().equalsIgnoreCase(selectedFactionId);

            boolean detachmentOk = e.getDetachmentId().isBlank()
                    || selectedDetachmentId == null
                    || selectedDetachmentId.isBlank()
                    || e.getDetachmentId().equalsIgnoreCase(selectedDetachmentId);

            if (uniqueOk && factionOk && detachmentOk) {
                list.add(e);
            }
        }

        return list;
    }
}