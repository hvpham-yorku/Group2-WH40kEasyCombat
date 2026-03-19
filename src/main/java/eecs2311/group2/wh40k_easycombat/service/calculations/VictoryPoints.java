package eecs2311.group2.wh40k_easycombat.service.calculations;

public class VictoryPoints {
    static int VP;

    public int calculateVictoryPoints(int primaryVP, int secondaryVP, int bonusVP) {
        return (VP = primaryVP + secondaryVP + bonusVP);
    }

    public int addVictoryPoints(int currentVP, int pointsToAdd) {
        return currentVP + pointsToAdd;
    }
}