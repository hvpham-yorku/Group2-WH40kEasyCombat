package eecs2311.group2.wh40k_easycombat.model.editor;

public enum EditorRuleTargetRole {
    ATTACKER("Selected Unit Is Attacking"),
    DEFENDER("Selected Unit Is Defending"),
    EITHER("Selected Unit Is Participating");

    private final String label;

    EditorRuleTargetRole(String label) {
        this.label = label;
    }

    @Override
    public String toString() {
        return label;
    }
}
