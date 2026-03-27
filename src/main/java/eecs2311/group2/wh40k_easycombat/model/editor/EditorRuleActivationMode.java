package eecs2311.group2.wh40k_easycombat.model.editor;

public enum EditorRuleActivationMode {
    PASSIVE("Passive"),
    MANUAL("Manual");

    private final String label;

    EditorRuleActivationMode(String label) {
        this.label = label;
    }

    public String label() {
        return label;
    }

    @Override
    public String toString() {
        return label;
    }
}
