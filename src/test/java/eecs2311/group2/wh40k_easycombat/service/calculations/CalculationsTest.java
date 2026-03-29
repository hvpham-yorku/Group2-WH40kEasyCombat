package eecs2311.group2.wh40k_easycombat.service.calculations;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class CalculationsTest {

    @Test
    @DisplayName("Battle-shock passes when roll is above leadership")
    void battleShockPassAboveLeadership() {
        assertTrue(BattleShockCalculations.checkBattleShock(7, 6));
    }

    @Test
    @DisplayName("Battle-shock passes when roll equals leadership")
    void battleShockPassEqualLeadership() {
        assertTrue(BattleShockCalculations.checkBattleShock(5, 5));
    }

    @Test
    @DisplayName("Battle-shock fails when roll is below leadership")
    void battleShockFailBelowLeadership() {
        assertFalse(BattleShockCalculations.checkBattleShock(3, 4));
    }

    @Test
    @DisplayName("Battle-shock total sums an existing 2D6 roll")
    void battleShockTotalFromExistingRoll() {
        assertEquals(9, BattleShockCalculations.rollBattleShockTotalFromExistingRoll(java.util.List.of(4, 5)));
    }

    @Test
    @DisplayName("Battle-shock total can use a provided DiceService")
    void battleShockTotalUsesProvidedDiceService() {
        DiceService dice = new FixedDiceService(List.of(2, 6));
        assertEquals(8, BattleShockCalculations.rollBattleShockTotal(dice));
    }

    @Test
    @DisplayName("Charge succeeds when roll equals distance")
    void chargeSucceedsWhenRollEqualsDistance() {
        assertTrue(ChargePhaseCalculations.isChargeSuccessful(8, 8));
    }

    @Test
    @DisplayName("Charge succeeds when roll is above distance")
    void chargeSucceedsWhenRollAboveDistance() {
        assertTrue(ChargePhaseCalculations.isChargeSuccessful(8, 10));
    }

    @Test
    @DisplayName("Charge fails when roll is below distance")
    void chargeFailsWhenRollBelowDistance() {
        assertFalse(ChargePhaseCalculations.isChargeSuccessful(8, 7));
    }

    @Test
    @DisplayName("Wound threshold follows the official strength vs toughness table")
    void woundThresholds() {
        assertEquals(2, ShootingCalculations.requiredWoundRoll(8, 4));
        assertEquals(3, ShootingCalculations.requiredWoundRoll(5, 4));
        assertEquals(4, ShootingCalculations.requiredWoundRoll(4, 4));
        assertEquals(5, ShootingCalculations.requiredWoundRoll(4, 5));
        assertEquals(6, ShootingCalculations.requiredWoundRoll(3, 6));
    }

    @Test
    @DisplayName("Hit and wound checks use the required threshold")
    void hitAndWoundChecks() {
        assertTrue(ShootingCalculations.hitsTarget(4, 4));
        assertFalse(ShootingCalculations.hitsTarget(4, 3));
        assertTrue(ShootingCalculations.woundsTarget(4, 4, 4));
        assertFalse(ShootingCalculations.woundsTarget(4, 4, 3));
    }

    @Test
    @DisplayName("Save checks worsen with AP and cannot improve beyond 2+")
    void saveChecks() {
        assertTrue(ShootingCalculations.savesAttack(3, -1, 4));
        assertFalse(ShootingCalculations.savesAttack(3, -1, 3));
        assertEquals(5, ShootingCalculations.effectiveSave(3, -2));
        assertEquals(2, ShootingCalculations.effectiveSave(3, 2));
    }

    @Test
    @DisplayName("Below half-strength for multi-model units is strictly less than half")
    void unitBelowHalfStrengthRules() {
        assertTrue(UnitStrengthCalculations.isBelowHalfStrength(10, 4));
        assertFalse(UnitStrengthCalculations.isBelowHalfStrength(10, 5));
        assertFalse(UnitStrengthCalculations.isBelowHalfStrength(10, 6));
    }

    @Test
    @DisplayName("Below half-strength for a single model uses current wounds")
    void singleModelBelowHalfStrengthRules() {
        assertTrue(UnitStrengthCalculations.isBelowHalfStrengthSingleModel(10, 4));
        assertFalse(UnitStrengthCalculations.isBelowHalfStrengthSingleModel(10, 5));
        assertFalse(UnitStrengthCalculations.isBelowHalfStrengthSingleModel(10, 6));
    }

    @Test
    @DisplayName("Destroyed checks treat zero or less as destroyed")
    void destroyedChecks() {
        assertTrue(UnitStrengthCalculations.isUnitDestroyed(0));
        assertTrue(UnitStrengthCalculations.isUnitDestroyed(-1));
        assertFalse(UnitStrengthCalculations.isUnitDestroyed(1));
        assertTrue(UnitStrengthCalculations.isModelDestroyed(0));
        assertFalse(UnitStrengthCalculations.isModelDestroyed(1));
    }

    private static final class FixedDiceService extends DiceService {
        private final ArrayList<Integer> results;

        private FixedDiceService(List<Integer> rolls) {
            this.results = new ArrayList<>(rolls);
        }

        @Override
        public void rollDice(int n) {
            // Keep the provided deterministic results for test assertions.
        }

        @Override
        public ArrayList<Integer> getResults() {
            return results;
        }
    }
}
