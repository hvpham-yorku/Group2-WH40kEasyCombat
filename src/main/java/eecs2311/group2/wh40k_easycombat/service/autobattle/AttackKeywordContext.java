package eecs2311.group2.wh40k_easycombat.service.autobattle;

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
        boolean targetIsPsyker
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
                false
        );
    }
}
