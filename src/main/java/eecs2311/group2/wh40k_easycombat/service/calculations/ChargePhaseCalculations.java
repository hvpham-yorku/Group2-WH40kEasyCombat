package eecs2311.group2.wh40k_easycombat.service.calculations;

public class ChargePhaseCalculations {

    private static DiceService diceService = new DiceService();

    public static int roll2D6(){
        diceService.rollDice(2);
        return diceService.getResults().get(0) + diceService.getResults().get(1);
    }

    public static int rollChargeDistance() {
        return roll2D6();
    }

    public static boolean isChargeSuccessful(int distanceToEnemy) {
        int chargeRoll = rollChargeDistance();
        return chargeRoll >= distanceToEnemy;
    }

    public static boolean isChargeSuccessful(int distanceToEnemy, int chargeRoll) {
        return chargeRoll >= distanceToEnemy;
    }
}
