package eecs2311.group2.wh40k_easycombat.service;

import eecs2311.group2.wh40k_easycombat.viewmodel.ArmyUnitVM;

import java.util.LinkedHashSet;
import java.util.List;

public final class ArmyValidationService {

    private ArmyValidationService() {
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
                .filter(x -> x.warlordProperty().get())
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

        if (ArmyPointService.exceedsLimit(currentArmy, sizeLimit)) {
            int total = ArmyPointService.calculateArmyPoints(currentArmy);
            int limit = sizeLimit == null ? 2000 : sizeLimit;
            return "Current army is " + total + " pts, but the selected limit is " + limit + " pts.";
        }

        return null;
    }
}