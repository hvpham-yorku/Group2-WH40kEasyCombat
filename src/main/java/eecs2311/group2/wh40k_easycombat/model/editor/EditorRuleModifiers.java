package eecs2311.group2.wh40k_easycombat.model.editor;

import java.util.List;

public record EditorRuleModifiers(
        int hitModifier,
        int woundModifier,
        int attacksModifier,
        int damageModifier,
        int apModifier,
        String extraWeaponKeywords,
        EditorRerollType hitReroll,
        EditorRerollType woundReroll,
        List<String> appliedRuleNames,
        List<String> errorMessages
) {
    public EditorRuleModifiers {
        extraWeaponKeywords = extraWeaponKeywords == null ? "" : extraWeaponKeywords.trim();
        hitReroll = hitReroll == null ? EditorRerollType.NONE : hitReroll;
        woundReroll = woundReroll == null ? EditorRerollType.NONE : woundReroll;
        appliedRuleNames = appliedRuleNames == null ? List.of() : List.copyOf(appliedRuleNames);
        errorMessages = errorMessages == null ? List.of() : List.copyOf(errorMessages);
    }

    public static EditorRuleModifiers none() {
        return new EditorRuleModifiers(
                0,
                0,
                0,
                0,
                0,
                "",
                EditorRerollType.NONE,
                EditorRerollType.NONE,
                List.of(),
                List.of()
        );
    }

    public boolean hasAnyEffect() {
        return hitModifier != 0
                || woundModifier != 0
                || attacksModifier != 0
                || damageModifier != 0
                || apModifier != 0
                || !extraWeaponKeywords.isBlank()
                || hitReroll != EditorRerollType.NONE
                || woundReroll != EditorRerollType.NONE;
    }
}
