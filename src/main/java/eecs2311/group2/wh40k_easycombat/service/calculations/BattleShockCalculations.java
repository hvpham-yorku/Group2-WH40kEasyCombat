package eecs2311.group2.wh40k_easycombat.service.calculations;

public class BattleShockCalculations {
    int roll;
    static int leadership;

    public void checkBattleShock(int roll, int leadership) {
        if (roll >= leadership) {
            System.out.println("PASS! Unit is not Battle-shocked.");
        } else {
            System.out.println("FAIL! Unit becomes Battle-shocked.");
        }
    }
}