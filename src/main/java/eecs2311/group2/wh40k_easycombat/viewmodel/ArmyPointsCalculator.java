package eecs2311.group2.wh40k_easycombat.viewmodel;

import java.util.List;

public final class ArmyPointsCalculator {

    private ArmyPointsCalculator() {
    }

    public static int calculateArmyPoints(List<ArmyUnitVM> units) {
        if (units == null || units.isEmpty()) {
            return 0;
        }

        return units.stream()
                .mapToInt(x -> x.pointsProperty().get())
                .sum();
    }

    public static boolean exceedsLimit(List<ArmyUnitVM> units, Integer limit) {
        int safeLimit = limit == null ? 2000 : limit;
        return calculateArmyPoints(units) > safeLimit;
    }
}