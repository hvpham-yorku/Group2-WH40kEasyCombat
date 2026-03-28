package eecs2311.group2.wh40k_easycombat.service.game;

import eecs2311.group2.wh40k_easycombat.model.combat.AttackResult;
import eecs2311.group2.wh40k_easycombat.model.instance.*;
import eecs2311.group2.wh40k_easycombat.service.autobattle.AttackKeywordContext;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class GameEngineTest {

    private GameEngine engine;

    @BeforeEach
    void setUp() {
        engine = new GameEngine();
        engine.start();
    }

    // ── helpers ──────────────────────────────────────────────────────

    private static ArmyInstance makeArmy(String name) {
        return new ArmyInstance(1, name, "faction", "Faction", "detachment");
    }

    private static UnitInstance makeUnit(String name) {
        UnitInstance unit = new UnitInstance("ds-1", name);
        unit.addModel(new UnitModelInstance(name, "6\"", "4", "3+", "2", "6+", "1", ""));
        return unit;
    }

    private static WeaponProfile rangedWeapon(String name) {
        return new WeaponProfile(1, name, "", 1, "24\"", "2", "3+", "4", "-1", "1", false);
    }

    private static WeaponProfile meleeWeapon(String name) {
        return new WeaponProfile(2, name, "", 1, "Melee", "3", "3+", "5", "-2", "2", true);
    }

    private void setUpArmiesWithUnits() {
        ArmyInstance attacker = makeArmy("Attacker");
        UnitInstance attackerUnit = makeUnit("Space Marine");
        attackerUnit.addRangedWeapon(rangedWeapon("Bolt Rifle"));
        attackerUnit.addMeleeWeapon(meleeWeapon("Combat Knife"));
        attacker.addUnit(attackerUnit);
        attacker.addStrategy(new StratagemInstance("Command Re-roll", "1", "Any", "Any", "<b>Re-roll one hit</b>"));

        ArmyInstance defender = makeArmy("Defender");
        UnitInstance defenderUnit = makeUnit("Ork Boy");
        defender.addUnit(defenderUnit);

        engine.stateManager.setArmy(Player.ATTACKER, attacker);
        engine.stateManager.setArmy(Player.DEFENDER, defender);
    }

    private String getAttackerUnitId() {
        return engine.stateManager.getArmy(Player.ATTACKER).getUnits().get(0).getInstanceId();
    }

    private String getDefenderUnitId() {
        return engine.stateManager.getArmy(Player.DEFENDER).getUnits().get(0).getInstanceId();
    }

    // ── start() ─────────────────────────────────────────────────────

    @Nested
    @DisplayName("start()")
    class Start {

        @Test
        @DisplayName("initializes stateManager")
        void initializesStateManager() {
            assertNotNull(engine.stateManager);
        }

        @Test
        @DisplayName("sets currentTurn to 1")
        void currentTurnStartsAtOne() {
            assertEquals(1, engine.currentTurn);
        }

        @Test
        @DisplayName("state begins at COMMAND phase with ATTACKER active")
        void initialPhaseAndPlayer() {
            BattleState state = engine.stateManager.getCurrentState();
            assertEquals(Phase.COMMAND, state.getCurrentPhase());
            assertEquals(Player.ATTACKER, state.getActivePlayer());
        }

        @Test
        @DisplayName("calling start() again resets everything")
        void restartResetsState() {
            engine.currentTurn = 5;
            engine.start();
            assertEquals(1, engine.currentTurn);
            assertEquals(Phase.COMMAND, engine.stateManager.getCurrentState().getCurrentPhase());
        }
    }

    // ── selectMainMission() ─────────────────────────────────────────

    @Nested
    @DisplayName("selectMainMission()")
    class SelectMainMission {

        @Test
        @DisplayName("stores mission name on battle state")
        void setsMissionName() {
            engine.selectMainMission("Take and Hold", 10);
            assertEquals("Take and Hold", engine.stateManager.getCurrentState().getMissionName());
        }

        @Test
        @DisplayName("stores mission point value")
        void setsMissionValue() {
            engine.selectMainMission("Take and Hold", 10);
            assertEquals(10, engine.mainMissionValue);
        }
    }

    // ── selectAttackerMission() / selectDefenderMission() ───────────

    @Nested
    @DisplayName("selectAttackerMission() / selectDefenderMission()")
    class SelectSecondaryMissions {

        @BeforeEach
        void armiesNeeded() {
            setUpArmiesWithUnits();
        }

        @Test
        @DisplayName("attacker secondary mission sets name and value")
        void attackerMission() {
            engine.selectAttackerMission("Assassinate", 5);
            assertEquals("Assassinate",
                    engine.stateManager.getArmy(Player.ATTACKER).getSecondaryMissionName());
            assertEquals(5, engine.attackerSecondaryMissionValue);
        }

        @Test
        @DisplayName("defender secondary mission sets name and value")
        void defenderMission() {
            engine.selectDefenderMission("Defend Objective", 8);
            assertEquals("Defend Objective",
                    engine.stateManager.getArmy(Player.DEFENDER).getSecondaryMissionName());
            assertEquals(8, engine.defenderSecondaryMissionValue);
        }
    }

    // ── endPlayerTurn() ─────────────────────────────────────────────

    @Nested
    @DisplayName("endPlayerTurn()")
    class EndPlayerTurn {

        @BeforeEach
        void armiesNeeded() {
            setUpArmiesWithUnits();
        }

        @Test
        @DisplayName("first call switches active player without advancing phase")
        void firstCallSwitchesPlayer() {
            engine.endPlayerTurn(); // turn 1 -> 2 (odd->even check: 1%2!=0, no advance)
            assertEquals(Player.DEFENDER, engine.stateManager.getActivePlayer());
        }

        @Test
        @DisplayName("increments currentTurn each call")
        void incrementsTurn() {
            engine.endPlayerTurn();
            assertEquals(2, engine.currentTurn);
            engine.endPlayerTurn();
            assertEquals(3, engine.currentTurn);
        }

        @Test
        @DisplayName("on even turn, advances phase and awards CP to both players")
        void evenTurnAdvancesPhaseAndAwardsCP() {
            // Turn 1 (odd) -> no advance
            engine.endPlayerTurn();
            // currentTurn is now 2 (even) -> advance phase, give CP
            engine.endPlayerTurn();

            // Phase should have advanced from COMMAND
            assertEquals(Phase.MOVEMENT, engine.stateManager.getCurrentState().getCurrentPhase());

            // Both players should have received 1 CP
            assertTrue(engine.stateManager.getArmy(Player.ATTACKER).getCurrentCp() >= 1);
            assertTrue(engine.stateManager.getArmy(Player.DEFENDER).getCurrentCp() >= 1);
        }

        @Test
        @DisplayName("odd turn does NOT advance phase or award CP")
        void oddTurnNoAdvanceNoCP() {
            Phase before = engine.stateManager.getCurrentState().getCurrentPhase();
            int attackerCpBefore = engine.stateManager.getArmy(Player.ATTACKER).getCurrentCp();

            engine.endPlayerTurn(); // turn 1 is odd

            assertEquals(before, engine.stateManager.getCurrentState().getCurrentPhase());
            assertEquals(attackerCpBefore, engine.stateManager.getArmy(Player.ATTACKER).getCurrentCp());
        }

        @Test
        @DisplayName("multiple endPlayerTurn calls cycle through phases correctly")
        void multipleCallsCyclePhases() {
            // Turns 1,2 = one pair -> after turn 2, phase advances once
            engine.endPlayerTurn(); // turn 1 odd
            engine.endPlayerTurn(); // turn 2 even -> advance

            assertEquals(Phase.MOVEMENT, engine.stateManager.getCurrentState().getCurrentPhase());

            engine.endPlayerTurn(); // turn 3 odd
            engine.endPlayerTurn(); // turn 4 even -> advance

            assertEquals(Phase.SHOOTING, engine.stateManager.getCurrentState().getCurrentPhase());
        }
    }

    // ── performBattle() ─────────────────────────────────────────────

    @Nested
    @DisplayName("performBattle()")
    class PerformBattle {

        @BeforeEach
        void armiesNeeded() {
            setUpArmiesWithUnits();
        }

        @Test
        @DisplayName("returns not-resolved when stateManager is null")
        void nullStateManager() {
            engine.stateManager = null;
            AttackResult result = engine.performBattle("d", "a", "w");
            assertFalse(result.resolved());
            assertTrue(result.notes().get(0).contains("not been started"));
        }

        @Test
        @DisplayName("throws when attacker unit not found (weapon lookup NPE)")
        void attackerNotFound() {
            advanceToShootingPhase();

            // GameEngine looks up the weapon before null-checking the attacker,
            // so a bad attacker ID causes a NullPointerException in getWeaponProfile.
            assertThrows(NullPointerException.class, () ->
                    engine.performBattle(getDefenderUnitId(), "bad-id", "Bolt Rifle"));
        }

        @Test
        @DisplayName("returns not-resolved when defender unit not found")
        void defenderNotFound() {
            advanceToShootingPhase();

            AttackResult result = engine.performBattle("bad-id", getAttackerUnitId(), "Bolt Rifle");
            assertFalse(result.resolved());
            assertTrue(result.notes().get(0).contains("Defender unit not found"));
        }

        @Test
        @DisplayName("returns not-resolved when weapon not found")
        void weaponNotFound() {
            advanceToShootingPhase();

            AttackResult result = engine.performBattle(getDefenderUnitId(), getAttackerUnitId(), "Plasma Gun");
            assertFalse(result.resolved());
            assertTrue(result.notes().get(0).contains("Weapon not found"));
        }

        @Test
        @DisplayName("ranged weapon not allowed in FIGHT phase")
        void rangedInFightPhase() {
            advanceToPhase(Phase.FIGHT);

            AttackResult result = engine.performBattle(
                    getDefenderUnitId(), getAttackerUnitId(), "Bolt Rifle");
            assertFalse(result.resolved());
            assertTrue(result.notes().get(0).contains("cannot be resolved in the current phase"));
        }

        @Test
        @DisplayName("melee weapon not allowed in SHOOTING phase")
        void meleeInShootingPhase() {
            advanceToShootingPhase();

            AttackResult result = engine.performBattle(
                    getDefenderUnitId(), getAttackerUnitId(), "Combat Knife");
            assertFalse(result.resolved());
            assertTrue(result.notes().get(0).contains("cannot be resolved in the current phase"));
        }

        @Test
        @DisplayName("ranged weapon resolves successfully in SHOOTING phase")
        void rangedInShootingPhase() {
            advanceToShootingPhase();

            AttackResult result = engine.performBattle(
                    getDefenderUnitId(), getAttackerUnitId(), "Bolt Rifle");
            assertTrue(result.resolved());
        }

        @Test
        @DisplayName("melee weapon resolves successfully in FIGHT phase")
        void meleeInFightPhase() {
            advanceToPhase(Phase.FIGHT);

            // AutoBattler requires attackerIsEligibleToFight=true for melee
            AttackKeywordContext ctx = new AttackKeywordContext(
                    1, false, false, false, false, false,
                    true, // attackerIsEligibleToFight
                    false, false, false, "",
                    false, false, false, false, false, 0, 0, null, null);
            AttackResult result = engine.performBattle(
                    getDefenderUnitId(), getAttackerUnitId(), "Combat Knife", ctx);
            assertTrue(result.resolved());
        }

        @Test
        @DisplayName("ranged weapon not allowed in COMMAND phase")
        void rangedInCommandPhase() {
            // Engine starts in COMMAND phase
            AttackResult result = engine.performBattle(
                    getDefenderUnitId(), getAttackerUnitId(), "Bolt Rifle");
            assertFalse(result.resolved());
        }

        @Test
        @DisplayName("melee weapon not allowed in MOVEMENT phase")
        void meleeInMovementPhase() {
            advanceToPhase(Phase.MOVEMENT);

            AttackResult result = engine.performBattle(
                    getDefenderUnitId(), getAttackerUnitId(), "Combat Knife");
            assertFalse(result.resolved());
        }

        private void advanceToShootingPhase() {
            advanceToPhase(Phase.SHOOTING);
        }

        private void advanceToPhase(Phase target) {
            // Directly set the phase to avoid player switches from FIGHT->COMMAND wrap
            engine.stateManager.getCurrentState().setCurrentPhase(target);
        }
    }

    // ── useStrategem() ──────────────────────────────────────────────

    @Nested
    @DisplayName("useStrategem()")
    class UseStrategem {

        @BeforeEach
        void armiesNeeded() {
            setUpArmiesWithUnits();
            engine.stateManager.getArmy(Player.ATTACKER).setCurrentCp(5);
        }

        @Test
        @DisplayName("deducts CP cost of stratagem from active player")
        void deductsCp() {
            engine.useStrategem("Command Re-roll");
            assertEquals(4, engine.stateManager.getArmy(Player.ATTACKER).getCurrentCp());
        }

        @Test
        @DisplayName("throws when stratagem not found")
        void throwsForUnknownStratagem() {
            assertThrows(NullPointerException.class, () ->
                    engine.useStrategem("Nonexistent Stratagem"));
        }
    }

    // ── completedMission() ──────────────────────────────────────────

    @Nested
    @DisplayName("completedMission()")
    class CompletedMission {

        @BeforeEach
        void armiesNeeded() {
            setUpArmiesWithUnits();
        }

        @Test
        @DisplayName("awards main mission VP to active player")
        void awardsMainMissionVP() {
            engine.selectMainMission("Take and Hold", 10);
            engine.completedMission();
            assertEquals(10, engine.stateManager.getArmy(Player.ATTACKER).getCurrentVp());
        }

        @Test
        @DisplayName("awards 0 VP when no mission selected")
        void noMissionSelected() {
            engine.completedMission();
            assertEquals(0, engine.stateManager.getArmy(Player.ATTACKER).getCurrentVp());
        }

        @Test
        @DisplayName("VP accumulates across multiple completions")
        void vpAccumulates() {
            engine.selectMainMission("Mission", 5);
            engine.completedMission();
            engine.completedMission();
            assertEquals(10, engine.stateManager.getArmy(Player.ATTACKER).getCurrentVp());
        }
    }

    // ── completedSecondaryMission() ─────────────────────────────────

    @Nested
    @DisplayName("completedSecondaryMission()")
    class CompletedSecondaryMission {

        @BeforeEach
        void armiesNeeded() {
            setUpArmiesWithUnits();
        }

        @Test
        @DisplayName("awards attacker secondary VP when attacker is active")
        void attackerSecondary() {
            engine.selectAttackerMission("Assassinate", 5);
            engine.completedSecondaryMission();
            assertEquals(5, engine.stateManager.getArmy(Player.ATTACKER).getCurrentVp());
        }

        @Test
        @DisplayName("awards defender secondary VP when defender is active")
        void defenderSecondary() {
            engine.selectDefenderMission("Defend", 8);
            engine.stateManager.switchActivePlayer(); // switch to defender
            engine.completedSecondaryMission();
            assertEquals(8, engine.stateManager.getArmy(Player.DEFENDER).getCurrentVp());
        }

        @Test
        @DisplayName("clears secondary mission name after completion")
        void clearsMissionName() {
            engine.selectAttackerMission("Assassinate", 5);
            engine.completedSecondaryMission();
            assertEquals("", engine.stateManager.getArmy(Player.ATTACKER).getSecondaryMissionName());
        }
    }

    // ── integration / flow ──────────────────────────────────────────

    @Nested
    @DisplayName("Game flow integration")
    class GameFlow {

        @BeforeEach
        void armiesNeeded() {
            setUpArmiesWithUnits();
        }

        @Test
        @DisplayName("full two-player turn cycle works end to end")
        void fullTurnCycle() {
            // Both players start with 0 CP
            assertEquals(0, engine.stateManager.getArmy(Player.ATTACKER).getCurrentCp());
            assertEquals(0, engine.stateManager.getArmy(Player.DEFENDER).getCurrentCp());

            // Player 1 ends turn (turn 1, odd -> no phase advance, no CP)
            engine.endPlayerTurn();
            assertEquals(Player.DEFENDER, engine.stateManager.getActivePlayer());

            // Player 2 ends turn (turn 2, even -> phase advances, both get 1 CP)
            engine.endPlayerTurn();
            assertEquals(Player.ATTACKER, engine.stateManager.getActivePlayer());
            assertEquals(Phase.MOVEMENT, engine.stateManager.getCurrentState().getCurrentPhase());
            assertEquals(1, engine.stateManager.getArmy(Player.ATTACKER).getCurrentCp());
            assertEquals(1, engine.stateManager.getArmy(Player.DEFENDER).getCurrentCp());
        }

        @Test
        @DisplayName("mission selection followed by VP award works")
        void missionThenVP() {
            engine.selectMainMission("Hold the Line", 15);
            engine.selectAttackerMission("Slay the Warlord", 5);
            engine.selectDefenderMission("Behind Enemy Lines", 3);

            // Attacker completes main mission
            engine.completedMission();
            assertEquals(15, engine.stateManager.getArmy(Player.ATTACKER).getCurrentVp());

            // Attacker completes secondary
            engine.completedSecondaryMission();
            assertEquals(20, engine.stateManager.getArmy(Player.ATTACKER).getCurrentVp());

            // Switch to defender and complete secondary
            engine.stateManager.switchActivePlayer();
            engine.completedSecondaryMission();
            assertEquals(3, engine.stateManager.getArmy(Player.DEFENDER).getCurrentVp());
        }
    }
}
