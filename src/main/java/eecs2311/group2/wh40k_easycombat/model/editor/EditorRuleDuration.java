package eecs2311.group2.wh40k_easycombat.model.editor;

public enum EditorRuleDuration {
    UNTIL_END_OF_PHASE("Until End Of Phase"),
    UNTIL_END_OF_TURN("Until End Of Turn"),
    UNTIL_START_OF_YOUR_NEXT_COMMAND("Until Start Of Your Next Command"),
    UNTIL_END_OF_BATTLE("Until End Of Battle");

    private final String label;

    EditorRuleDuration(String label) {
        this.label = label;
    }

    @Override
    public String toString() {
        return label;
    }
}
