package eecs2311.group2.wh40k_easycombat.model.mission;

public enum SecondaryMissionMode {
    FIXED("Fixed Missions"),
    TACTICAL("Draw In Game");

    private final String label;

    SecondaryMissionMode(String label) {
        this.label = label;
    }

    @Override
    public String toString() {
        return label;
    }
}
