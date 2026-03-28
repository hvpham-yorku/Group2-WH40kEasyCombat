package eecs2311.group2.wh40k_easycombat.service.autobattle;

import eecs2311.group2.wh40k_easycombat.model.editor.EditorRerollType;
import eecs2311.group2.wh40k_easycombat.model.editor.EditorRuleModifiers;

public record AttackKeywordContext(
        int attackingWeaponBearerCount,
        boolean withinHalfRange,
        boolean remainedStationary,
        boolean advancedThisTurn,
        boolean fellBackThisTurn,
        boolean bearerChargedThisTurn,
        boolean attackerIsEligibleToFight,
        boolean targetHasBenefitOfCover,
        boolean blastIsLegal,
        boolean applyPrecisionToChosenModel,
        String preferredDefenderModelId,
        boolean targetIsInfantry,
        boolean targetIsVehicle,
        boolean targetIsMonster,
        boolean targetIsCharacter,
        boolean targetIsPsyker,
        int customHitModifier,
        int customWoundModifier,
        EditorRerollType hitRerollType,
        EditorRerollType woundRerollType
) {
    public static AttackKeywordContext none() {
        return new AttackKeywordContext(
                0,
                false,
                false,
                false,
                false,
                false,
                false,
                false,
                false,
                false,
                "",
                false,
                false,
                false,
                false,
                false,
                0,
                0,
                EditorRerollType.NONE,
                EditorRerollType.NONE
        );
    }

    public AttackKeywordContext withEditorModifiers(EditorRuleModifiers modifiers) {
        if (modifiers == null || !modifiers.hasAnyEffect()) {
            return this;
        }

        return new AttackKeywordContext(
                attackingWeaponBearerCount,
                withinHalfRange,
                remainedStationary,
                advancedThisTurn,
                fellBackThisTurn,
                bearerChargedThisTurn,
                attackerIsEligibleToFight,
                targetHasBenefitOfCover,
                blastIsLegal,
                applyPrecisionToChosenModel,
                preferredDefenderModelId,
                targetIsInfantry,
                targetIsVehicle,
                targetIsMonster,
                targetIsCharacter,
                targetIsPsyker,
                customHitModifier + modifiers.hitModifier(),
                customWoundModifier + modifiers.woundModifier(),
                EditorRerollType.stronger(hitRerollType, modifiers.hitReroll()),
                EditorRerollType.stronger(woundRerollType, modifiers.woundReroll())
        );
    }
}
