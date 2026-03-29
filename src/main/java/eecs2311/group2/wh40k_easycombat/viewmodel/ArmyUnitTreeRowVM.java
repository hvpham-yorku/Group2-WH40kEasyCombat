package eecs2311.group2.wh40k_easycombat.viewmodel;

public record ArmyUnitTreeRowVM(
        String displayName,
        String datasheetId,
        String role,
        int points,
        boolean group
) {
    public static ArmyUnitTreeRowVM group(String name) {
        return new ArmyUnitTreeRowVM(name, null, null, 0, true);
    }

    public boolean isGroup() {
        return group;
    }
}