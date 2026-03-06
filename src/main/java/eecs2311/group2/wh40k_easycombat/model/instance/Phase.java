package eecs2311.group2.wh40k_easycombat.model.instance;

public enum Phase {
    COMMAND,
    MOVEMENT,
    SHOOTING,
    CHARGE,
    FIGHT;

    public Phase next() {
        Phase[] values = Phase.values();
        int nextOrdinal = (this.ordinal() + 1) % values.length;
        return values[nextOrdinal];
    }
}
