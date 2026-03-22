package eecs2311.group2.wh40k_easycombat.model.combat;

public record ResolvedAttack(
        String label,
        String weaponName,
        AttackResult result
) {
    public ResolvedAttack {
        label = label == null ? "" : label;
        weaponName = weaponName == null ? "" : weaponName;
    }
}
