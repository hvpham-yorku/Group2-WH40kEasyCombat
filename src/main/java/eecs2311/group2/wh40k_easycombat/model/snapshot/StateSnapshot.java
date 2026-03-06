package eecs2311.group2.wh40k_easycombat.model.snapshot;

import eecs2311.group2.wh40k_easycombat.model.instance.BattleState;

public record StateSnapshot(BattleState state, long timestamp) implements ISnapshot {
    @Override public SnapshotType getType() { return SnapshotType.STATE; }
    @Override public long getTimestamp() { return timestamp; }
}
