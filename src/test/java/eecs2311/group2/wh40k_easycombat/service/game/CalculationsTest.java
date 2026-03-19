package eecs2311.group2.wh40k_easycombat.service.game;

import static org.junit.jupiter.api.Assertions.*;

import eecs2311.group2.wh40k_easycombat.service.calculations.*;
import org.junit.jupiter.api.Test;

public class CalculationsTest {

    @Test
    void battleShockPassAboveLeadership() {
        BattleShockCalculations calc = new BattleShockCalculations();
        assertTrue(calc.checkBattleShock(7, 6));
    }

    @Test
    void battleShockPassEqualLeadership() {
        BattleShockCalculations calc = new BattleShockCalculations();
        assertTrue(calc.checkBattleShock(5, 5));
    }

    @Test
    void battleShockFailBelowLeadership() {
        BattleShockCalculations calc = new BattleShockCalculations();
        assertFalse(calc.checkBattleShock(3, 4));
    }

    @Test
    void VPTest1() {
        VictoryPoints vp = new VictoryPoints();
        assertEquals(3, vp.calculateVictoryPoints(1, 1, 1));
    }

    @Test
    void VPTest2() {
        VictoryPoints vp = new VictoryPoints();
        assertEquals(8, vp.addVictoryPoints(3, 5));
    }

    @Test
    void chargeSucceedsWhenRollEqualsDistance() {
        assertTrue(ChargePhaseCalculations.isChargeSuccessful(8, 8));
    }

    @Test
    void chargeSucceedsWhenRollAboveDistance() {
        assertTrue(ChargePhaseCalculations.isChargeSuccessful(8, 10));
    }

    @Test
    void chargeFailsWhenRollBelowDistance() {
        assertFalse(ChargePhaseCalculations.isChargeSuccessful(8, 7));
    }

    @Test
    void chargeMinimumPossibleRoll() {
        assertTrue(ChargePhaseCalculations.isChargeSuccessful(2, 2));
        assertFalse(ChargePhaseCalculations.isChargeSuccessful(3, 2));
    }

    @Test
    void chargeMaximumPossibleRoll() {
        assertTrue(ChargePhaseCalculations.isChargeSuccessful(12, 12));
        assertFalse(ChargePhaseCalculations.isChargeSuccessful(13, 12));
    }

    @Test
    void woundRollIs2WhenStrengthAtLeastDoubleToughness() {
        assertEquals(2, ShootingCalculations.requiredWoundRoll(8, 4));
    }

    @Test
    void woundRollIs3WhenStrengthGreaterThanToughness() {
        assertEquals(3, ShootingCalculations.requiredWoundRoll(5, 4));
    }

    @Test
    void woundRollIs4WhenStrengthEqualsToughness() {
        assertEquals(4, ShootingCalculations.requiredWoundRoll(4, 4));
    }

    @Test
    void woundRollIs5WhenStrengthLowerButMoreThanHalfToughness() {
        assertEquals(5, ShootingCalculations.requiredWoundRoll(4, 5));
    }

    @Test
    void woundRollIs6WhenStrengthAtMostHalfToughness() {
        assertEquals(6, ShootingCalculations.requiredWoundRoll(3, 6));
    }

    @Test
    void hitSucceedsOnEqualBallisticSkill() {
        assertTrue(ShootingCalculations.hitsTarget(4, 4));
    }

    @Test
    void hitFailsBelowBallisticSkill() {
        assertFalse(ShootingCalculations.hitsTarget(4, 3));
    }

    @Test
    void woundSucceedsWhenRollMeetsNeededValue() {
        assertTrue(ShootingCalculations.woundsTarget(4, 4, 4));
    }

    @Test
    void woundFailsWhenRollBelowNeededValue() {
        assertFalse(ShootingCalculations.woundsTarget(4, 4, 3));
    }

    @Test
    void saveSucceedsWhenRollMeetsEffectiveSave() {
        assertTrue(ShootingCalculations.savesAttack(3, -1, 4)); // effective save = 4+
    }

    @Test
    void saveFailsWhenRollBelowEffectiveSave() {
        assertFalse(ShootingCalculations.savesAttack(3, -1, 3)); // effective save = 4+
    }

    @Test
    void effectiveSaveGetsWorseWithNegativeAP() {
        assertEquals(5, ShootingCalculations.effectiveSave(3, -2));
    }

    @Test
    void effectiveSaveCannotImprovePast2Plus() {
        assertEquals(2, ShootingCalculations.effectiveSave(3, 2));
    }

    @Test
    void unitIsBelowHalfStrengthWhenLessThanHalf() {
        assertTrue(UnitStrengthCalculations.isBelowHalfStrength(10, 4));
    }

    @Test
    void unitAtExactHalfStrengthShouldBeConsideredBelowHalfStrength() {
        assertFalse(UnitStrengthCalculations.isBelowHalfStrength(10, 5));
    }

    @Test
    void unitIsNotBelowHalfStrengthWhenAboveHalf() {
        assertFalse(UnitStrengthCalculations.isBelowHalfStrength(10, 6));
    }

    @Test
    void unitDestroyedAtZeroModels() {
        assertTrue(UnitStrengthCalculations.isUnitDestroyed(0));
    }

    @Test
    void unitDestroyedBelowZeroModels() {
        assertTrue(UnitStrengthCalculations.isUnitDestroyed(-1));
    }

    @Test
    void unitNotDestroyedAboveZeroModels() {
        assertFalse(UnitStrengthCalculations.isUnitDestroyed(1));
    }

    @Test
    void modelDestroyedAtZeroWounds() {
        assertTrue(UnitStrengthCalculations.isModelDestroyed(0));
    }

    @Test
    void modelNotDestroyedAboveZeroWounds() {
        assertFalse(UnitStrengthCalculations.isModelDestroyed(1));
    }
}