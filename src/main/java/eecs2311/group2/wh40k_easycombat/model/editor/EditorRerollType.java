package eecs2311.group2.wh40k_easycombat.model.editor;

public enum EditorRerollType {
    NONE("None", 0),
    ONES("Re-roll 1s", 1),
    FAILS("Re-roll Fails", 2);

    private final String label;
    private final int power;

    EditorRerollType(String label, int power) {
        this.label = label;
        this.power = power;
    }

    public static EditorRerollType stronger(EditorRerollType left, EditorRerollType right) {
        EditorRerollType safeLeft = left == null ? NONE : left;
        EditorRerollType safeRight = right == null ? NONE : right;
        return safeLeft.power >= safeRight.power ? safeLeft : safeRight;
    }

    @Override
    public String toString() {
        return label;
    }
}
