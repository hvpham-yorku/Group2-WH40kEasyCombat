package eecs2311.group2.wh40k_easycombat.manager;

import eecs2311.group2.wh40k_easycombat.viewmodel.ArmyUnitVM;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashSet;
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

        FXCollections.sort(
                currentArmy,
                Comparator.comparing(ArmyUnitVM::getRole, String.CASE_INSENSITIVE_ORDER)
                        .thenComparing(ArmyUnitVM::getUnitName, String.CASE_INSENSITIVE_ORDER)
        );
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

    public static int calculateArmyPoints(List<ArmyUnitVM> units) {
        if (units == null || units.isEmpty()) {
            return 0;
        }

        return units.stream()
                .mapToInt(vm -> vm.pointsProperty().get())
                .sum();
    }

    public static boolean exceedsLimit(List<ArmyUnitVM> units, Integer limit) {
        int safeLimit = limit == null ? 2000 : limit;
        return calculateArmyPoints(units) > safeLimit;
    }

    public static String validateBeforeSave(
            String factionId,
            String detachmentId,
            String armyName,
            List<ArmyUnitVM> currentArmy,
            Integer sizeLimit
    ) {
        if (factionId == null || factionId.isBlank() || "all".equalsIgnoreCase(factionId)) {
            return "Please choose one specific faction. \"All\" is not allowed.";
        }

        if (detachmentId == null || detachmentId.isBlank()) {
            return "Please choose one detachment.";
        }

        if (armyName == null || armyName.trim().isEmpty()) {
            return "Please enter an army name.";
        }

        if (currentArmy == null || currentArmy.isEmpty()) {
            return "Your army is empty.";
        }

        long warlordCount = currentArmy.stream()
                .filter(vm -> vm.warlordProperty().get())
                .count();

        if (warlordCount > 1) {
            return "Only one CHARACTER unit can be set as warlord.";
        }

        LinkedHashSet<String> usedEnhancements = new LinkedHashSet<>();
        for (ArmyUnitVM unit : currentArmy) {
            String id = unit.getEnhancementId();
            if (id == null || id.isBlank()) continue;

            if (!usedEnhancements.add(id)) {
                return "Each enhancement can only be taken once.";
            }
        }

        if (exceedsLimit(currentArmy, sizeLimit)) {
            int total = calculateArmyPoints(currentArmy);
            int limit = sizeLimit == null ? 2000 : sizeLimit;
            return "Current army is " + total + " pts, but the selected limit is " + limit + " pts.";
        }

        return null;
    }

    public static List<ArmyUnitVM.EnhancementEntry> buildAvailableEnhancements(
            List<ArmyUnitVM> currentArmy,
            ArmyUnitVM vm,
            String selectedFactionId,
            String selectedDetachmentId
    ) {
        List<ArmyUnitVM> safeArmy = currentArmy == null ? List.of() : currentArmy;
        List<ArmyUnitVM.EnhancementEntry> list = new ArrayList<>();
        list.add(new ArmyUnitVM.EnhancementEntry("", "No Enhancement", 0));

        if (vm == null) {
            return list;
        }

        for (ArmyUnitVM.EnhancementEntry e : vm.getEnhancements()) {
            boolean sameAsCurrent = e.getId().equals(vm.getEnhancementId());

            boolean uniqueOk = sameAsCurrent || safeArmy.stream()
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
