package eecs2311.group2.wh40k_easycombat.service.calculations;

import java.util.List;

public class BattleShockCalculations {
    private BattleShockCalculations() {
    }

    public static boolean checkBattleShock(int roll, int leadership) {
        return roll >= leadership;
    }

    public static int rollBattleShockTotal(DiceService diceService) {
        if (diceService == null) {
            diceService = new DiceService();
        }

        diceService.rollDice(2);
        return diceService.getResults().get(0) + diceService.getResults().get(1);
    }

    public static int rollBattleShockTotalFromExistingRoll(List<Integer> rolls) {
        if (rolls == null || rolls.isEmpty()) {
            return 0;
        }

        return rolls.stream().mapToInt(Integer::intValue).sum();
    }
}
