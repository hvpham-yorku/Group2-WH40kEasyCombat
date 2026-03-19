package eecs2311.group2.wh40k_easycombat.service.calculations;

public class UnitStrengthCalculations {

    public static boolean isBelowHalfStrength(int startingModels, int currentModels) {
        if (startingModels <= 1) {
            throw new IllegalArgumentException("Apply the wound calculation when a unit contains only one model.");
        }
        return currentModels < (startingModels / 2.0);
    }

    public static boolean isBelowHalfStrengthSingleModel(int startingWounds, int currentWounds) {
        return currentWounds < (startingWounds / 2.0);
    }

    public static boolean isUnitDestroyed(int currentModels) {
        return currentModels <= 0;
    }

    public static boolean isModelDestroyed(int currentWounds) {
        return currentWounds <= 0;
    }
}