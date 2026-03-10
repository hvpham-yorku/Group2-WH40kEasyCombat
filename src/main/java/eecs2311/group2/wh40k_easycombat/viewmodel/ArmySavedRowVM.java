package eecs2311.group2.wh40k_easycombat.viewmodel;

public record ArmySavedRowVM(int armyId, String armyName, int points, boolean marked) {

    public String displayName() {
        return marked ? "★ " + armyName : armyName;
    }
}