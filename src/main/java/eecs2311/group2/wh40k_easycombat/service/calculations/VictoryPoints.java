package eecs2311.group2.wh40k_easycombat.service.calculations;

public class VictoryPoints {
    static int VP;

    public static int calculateVictoryPoints(int primaryVP, int secondaryVP, int bonusVP) {
        return (VP = primaryVP + secondaryVP + bonusVP);
    }

    public static int addVictoryPoints(int currentVP, int pointsToAdd) {
        return currentVP + pointsToAdd;
    }
}