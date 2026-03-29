package eecs2311.group2.wh40k_easycombat.model.snapshot;

public record LogSnapshot(String message, long timestamp) implements ISnapshot {
    @Override public SnapshotType getType() { return SnapshotType.LOG; }
    @Override public long getTimestamp() { return timestamp; }
}
