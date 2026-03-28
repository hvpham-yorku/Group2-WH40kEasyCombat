package eecs2311.group2.wh40k_easycombat.model.mission;

public enum MissionType {
    PRIMARY("Primary Mission"),
    SECONDARY("Secondary Mission");

    private final String label;

    MissionType(String label) {
        this.label = label;
    }

    public String label() {
        return label;
    }
}
