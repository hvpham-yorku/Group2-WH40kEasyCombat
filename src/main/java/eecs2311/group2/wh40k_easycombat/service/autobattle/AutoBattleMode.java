package eecs2311.group2.wh40k_easycombat.service.autobattle;

public enum AutoBattleMode {
    SHOOTING("Shooting Auto Battle", "Shooting"),
    REACTION_SHOOTING("Reaction Shooting", "Reaction Shooting"),
    FIGHT("Fight Auto Battle", "Fight");

    private final String title;
    private final String attackLabel;

    AutoBattleMode(String title, String attackLabel) {
        this.title = title;
        this.attackLabel = attackLabel;
    }

    public String title() {
        return title;
    }

    public String attackLabel() {
        return attackLabel;
    }

    public boolean usesRangedWeapons() {
        return this != FIGHT;
    }
}
