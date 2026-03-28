package eecs2311.group2.wh40k_easycombat.model.mission;

public enum MissionDecision {
    COMPLETED,
    NOT_COMPLETED,
    CLOSED;

    public boolean isCompleted() {
        return this == COMPLETED;
    }

    public boolean isClosed() {
        return this == CLOSED;
    }
}
