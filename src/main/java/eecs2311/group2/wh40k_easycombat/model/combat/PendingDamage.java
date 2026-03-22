package eecs2311.group2.wh40k_easycombat.model.combat;

public record PendingDamage(
        String sourceLabel,
        String weaponName,
        int attackSequence,
        int damage,
        boolean devastatingWounds
) {
    public PendingDamage {
        sourceLabel = sourceLabel == null ? "" : sourceLabel;
        weaponName = weaponName == null ? "" : weaponName;
        attackSequence = Math.max(1, attackSequence);
        damage = Math.max(0, damage);
    }

    public String displayLabel() {
        String suffix = devastatingWounds ? " [Devastating Wounds]" : "";
        return sourceLabel + " - " + weaponName + " - Attack " + attackSequence + " - Damage " + damage + suffix;
    }
}
