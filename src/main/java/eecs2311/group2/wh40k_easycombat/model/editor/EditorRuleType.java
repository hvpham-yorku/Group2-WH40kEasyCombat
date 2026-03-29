package eecs2311.group2.wh40k_easycombat.model.editor;

public enum EditorRuleType {
    ABILITY("Ability"),
    KEYWORD("Keyword"),
    STRATAGEM("Stratagem"),
    ENHANCEMENT("Enhancement");

    private final String label;

    EditorRuleType(String label) {
        this.label = label;
    }

    @Override
    public String toString() {
        return label;
    }
}
