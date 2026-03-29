package eecs2311.group2.wh40k_easycombat.model.instance;

import eecs2311.group2.wh40k_easycombat.model.combat.PhaseAdvanceResult;
import eecs2311.group2.wh40k_easycombat.service.autobattle.AutoBattleMode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class BattleStateTest {

    @Test
    @DisplayName("reset restores the default round, phase, player and clears battle over")
    void resetRestoresDefaults() {
        BattleState state = new BattleState("Mission", 3);
        state.setCurrentRound(2);
        state.setCurrentPhase(Phase.FIGHT);
        state.setActivePlayer(Player.DEFENDER);
        state.setBattleOver(true);

        state.reset("Reset Mission", 6);

        assertEquals("Reset Mission", state.getMissionName());
        assertEquals(6, state.getMaxRounds());
        assertEquals(1, state.getCurrentRound());
        assertEquals(Phase.COMMAND, state.getCurrentPhase());
        assertEquals(Player.ATTACKER, state.getActivePlayer());
        assertFalse(state.isBattleOver());
        assertNull(state.getAttackerArmy());
        assertNull(state.getDefenderArmy());
    }

    @Test
    @DisplayName("current auto battle mode follows the battle phase")
    void currentAutoBattleModeFollowsPhase() {
        BattleState state = new BattleState();

        assertNull(state.currentAutoBattleMode());

        state.setCurrentPhase(Phase.MOVEMENT);
        assertEquals(AutoBattleMode.REACTION_SHOOTING, state.currentAutoBattleMode());

        state.setCurrentPhase(Phase.SHOOTING);
        assertEquals(AutoBattleMode.SHOOTING, state.currentAutoBattleMode());

        state.setCurrentPhase(Phase.FIGHT);
        assertEquals(AutoBattleMode.FIGHT, state.currentAutoBattleMode());
        assertEquals("Fight (Active)", state.phaseLabelFor(Player.ATTACKER));
        assertEquals("Fight", state.phaseLabelFor(Player.DEFENDER));
    }

    @Test
    @DisplayName("advancing from fight switches player, clears battle shock and awards one CP")
    void advancePhaseStateWrapsFightToCommand() {
        BattleState state = new BattleState("Mission", 5);
        ArmyInstance attacker = makeArmy("Attacker");
        ArmyInstance defender = makeArmy("Defender");
        UnitInstance attackerUnit = makeUnit("Attacker Unit");
        UnitInstance defenderUnit = makeUnit("Defender Unit");

        attackerUnit.setBattleShocked(true);
        attackerUnit.setChargedThisTurn(true);
        attackerUnit.setEligibleToFightThisPhase(true);
        attackerUnit.setFoughtThisPhase(true);
        defenderUnit.setBattleShocked(true);
        defender.setCurrentCp(1);

        attacker.addUnit(attackerUnit);
        defender.addUnit(defenderUnit);

        state.setAttackerArmy(attacker);
        state.setDefenderArmy(defender);
        state.setCurrentPhase(Phase.FIGHT);

        PhaseAdvanceResult result = state.advancePhaseState();

        assertEquals(Phase.COMMAND, result.currentPhase());
        assertEquals(Player.DEFENDER, result.activePlayer());
        assertEquals(Player.DEFENDER, result.commandPointRecipient());
        assertFalse(defenderUnit.isBattleShocked());
        assertTrue(attackerUnit.isBattleShocked());
        assertFalse(attackerUnit.hasChargedThisTurn());
        assertFalse(attackerUnit.isEligibleToFightThisPhase());
        assertFalse(attackerUnit.hasFoughtThisPhase());
        assertEquals(2, defender.getCurrentCp());
    }

    @Test
    @DisplayName("deepCopy includes max rounds, battle over, CP, VP and army contents")
    void deepCopyIncludesRuntimeState() {
        BattleState state = new BattleState("Mission", 4);
        ArmyInstance attacker = makeArmy("Attacker");
        UnitInstance attackerUnit = makeUnit("Attacker Unit");
        attackerUnit.addRangedWeapon(new WeaponProfile(1, "Bolt Rifle", "", 1, "24", "2", "3+", "4", "-1", "1", false));
        attacker.addUnit(attackerUnit);
        attacker.addStrategy(new StratagemInstance("Command Re-roll", "1", "Any", "Any", ""));
        attacker.setCurrentCp(5);
        attacker.setCurrentVp(9);

        state.setAttackerArmy(attacker);
        state.setBattleOver(true);
        state.setCurrentRound(3);
        state.setCurrentPhase(Phase.CHARGE);

        BattleState copy = state.deepCopy();

        assertNotSame(state, copy);
        assertEquals(4, copy.getMaxRounds());
        assertTrue(copy.isBattleOver());
        assertEquals(3, copy.getCurrentRound());
        assertEquals(Phase.CHARGE, copy.getCurrentPhase());
        assertEquals(5, copy.getCurrentCp(Player.ATTACKER));
        assertEquals(9, copy.getCurrentVp(Player.ATTACKER));
        assertEquals(1, copy.getAttackerArmy().getUnits().size());
        assertEquals(1, copy.getAttackerArmy().getStrategies().size());

        copy.getAttackerArmy().setCurrentCp(0);
        copy.getAttackerArmy().getUnits().clear();

        assertEquals(5, state.getCurrentCp(Player.ATTACKER));
        assertEquals(1, state.getAttackerArmy().getUnits().size());
    }

    @Test
    @DisplayName("hasExceededMaxRounds reports once current round is above the limit")
    void hasExceededMaxRoundsReportsWhenRoundGoesPastLimit() {
        BattleState state = new BattleState("Mission", 2);
        assertFalse(state.hasExceededMaxRounds());

        state.setCurrentRound(3);
        assertTrue(state.hasExceededMaxRounds());
    }

    private static ArmyInstance makeArmy(String name) {
        return new ArmyInstance(1, name, "faction", "Faction", "detachment");
    }

    private static UnitInstance makeUnit(String unitName) {
        UnitInstance unit = new UnitInstance("datasheet", unitName);
        unit.addModel(new UnitModelInstance("Model", "6", "4", "3+", "2", "6+", "1", ""));
        return unit;
    }
}
