package eecs2311.group2.wh40k_easycombat.model.combat;

import java.util.List;

public record AttackResult(
        boolean resolved,
        String weaponName,
        int attacks,
        int hits,
        int wounds,
        int unsaved,
        int totalDamage,
        int modelsDestroyed,
        List<String> notes,
        List<PendingDamage> pendingDamages,
        List<String> rollLog
) {
    public AttackResult {
        weaponName = weaponName == null ? "" : weaponName;
        notes = notes == null ? List.of() : List.copyOf(notes);
        pendingDamages = pendingDamages == null ? List.of() : List.copyOf(pendingDamages);
        rollLog = rollLog == null ? List.of() : List.copyOf(rollLog);
    }

    public static AttackResult notResolved(String reason) {
        return new AttackResult(
                false,
                "",
                0,
                0,
                0,
                0,
                0,
                0,
                List.of(reason == null ? "Attack was not resolved." : reason),
                List.of(),
                List.of()
        );
    }
}
