package eecs2311.group2.wh40k_easycombat.model.editor;

public enum EditorRuleAttackType {
    ANY("Any"),
    RANGED("Ranged"),
    MELEE("Melee");

    private final String label;

    EditorRuleAttackType(String label) {
        this.label = label;
    }

    public boolean matches(boolean meleeWeapon) {
        return switch (this) {
            case ANY -> true;
            case RANGED -> !meleeWeapon;
            case MELEE -> meleeWeapon;
        };
    }

    @Override
    public String toString() {
        return label;
    }
}
