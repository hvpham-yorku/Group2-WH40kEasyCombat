package eecs2311.group2.wh40k_easycombat.model.editor;

public enum EditorRulePhase {
    ANY("Any Phase"),
    SHOOTING("Shooting"),
    REACTION_SHOOTING("Reaction Shooting"),
    FIGHT("Fight");

    private final String label;

    EditorRulePhase(String label) {
        this.label = label;
    }

    @Override
    public String toString() {
        return label;
    }
}
