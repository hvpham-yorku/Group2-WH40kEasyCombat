package eecs2311.group2.wh40k_easycombat.model.editor;

public enum EditorRerollType {
    NONE("None"),
    ONES("Re-roll 1s"),
    FAILS("Re-roll Fails");

    private final String label;

    EditorRerollType(String label) {
        this.label = label;
    }

    public static EditorRerollType stronger(EditorRerollType left, EditorRerollType right) {
        EditorRerollType safeLeft = left == null ? NONE : left;
        EditorRerollType safeRight = right == null ? NONE : right;
        return safeLeft.ordinal() >= safeRight.ordinal() ? safeLeft : safeRight;
    }

    @Override
    public String toString() {
        return label;
    }
}
