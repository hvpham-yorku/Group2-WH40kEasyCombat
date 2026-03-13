package eecs2311.group2.wh40k_easycombat.service;

import eecs2311.group2.wh40k_easycombat.model.instance.ArmyInstance;
import eecs2311.group2.wh40k_easycombat.model.instance.BattleState;
import eecs2311.group2.wh40k_easycombat.model.instance.Phase;
import eecs2311.group2.wh40k_easycombat.model.instance.Player;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class CombatServiceTest {

    private CombatService service;

    @BeforeEach
    void setUp() {
        service = new CombatService("Test Mission");
    }

    // ---- Constructor / initial state ----

    @Test
    @DisplayName("Default constructor creates state with empty mission name")
    void defaultConstructor() {
        CombatService cs = new CombatService();
        BattleState state = cs.getCurrentState();
        assertNotNull(state);
        assertEquals("", state.getMissionName());
    }

    @Test
    @DisplayName("Parameterized constructor sets mission name")
    void parameterizedConstructor() {
        assertEquals("Test Mission", service.getCurrentState().getMissionName());
    }

    @Test
    @DisplayName("Initial state starts at round 1, COMMAND phase, ATTACKER active")
    void initialState() {
        BattleState state = service.getCurrentState();
        assertEquals(1, state.getCurrentRound());
        assertEquals(Phase.COMMAND, state.getCurrentPhase());
        assertEquals(Player.ATTACKER, state.getActivePlayer());
    }

    // ---- createNewBattle ----

    @Test
    @DisplayName("createNewBattle resets state with new mission name")
    void createNewBattle() {
        service.advancePhase();
        service.createNewBattle("New Mission");
        BattleState state = service.getCurrentState();
        assertEquals("New Mission", state.getMissionName());
        assertEquals(Phase.COMMAND, state.getCurrentPhase());
        assertEquals(1, state.getCurrentRound());
    }

    // ---- startBattle ----

    @Test
    @DisplayName("startBattle sets both armies")
    void startBattle() {
        ArmyInstance attacker = makeArmy("Attacker");
        ArmyInstance defender = makeArmy("Defender");
        service.startBattle("Battle", attacker, defender);

        assertEquals("Battle", service.getCurrentState().getMissionName());
        assertSame(attacker, service.getArmy(Player.ATTACKER));
        assertSame(defender, service.getArmy(Player.DEFENDER));
    }

    // ---- setCurrentBattle ----

    @Test
    @DisplayName("setCurrentBattle rejects null")
    void setCurrentBattleNull() {
        assertThrows(IllegalArgumentException.class, () -> service.setCurrentBattle(null));
    }

    @Test
    @DisplayName("setCurrentBattle replaces state")
    void setCurrentBattleValid() {
        BattleState newState = new BattleState("Replaced");
        service.setCurrentBattle(newState);
        assertEquals("Replaced", service.getCurrentState().getMissionName());
    }

    // ---- setArmy / getArmy ----

    @Test
    @DisplayName("setArmy and getArmy work for both players")
    void setAndGetArmy() {
        ArmyInstance a = makeArmy("A");
        ArmyInstance d = makeArmy("D");
        service.setArmy(Player.ATTACKER, a);
        service.setArmy(Player.DEFENDER, d);

        assertSame(a, service.getArmy(Player.ATTACKER));
        assertSame(d, service.getArmy(Player.DEFENDER));
    }

    // ---- getActiveArmy / getInactiveArmy ----

    @Test
    @DisplayName("getActiveArmy returns attacker army initially")
    void getActiveArmy() {
        ArmyInstance a = makeArmy("A");
        service.setArmy(Player.ATTACKER, a);
        assertSame(a, service.getActiveArmy());
    }

    @Test
    @DisplayName("getInactiveArmy returns defender army initially")
    void getInactiveArmy() {
        ArmyInstance d = makeArmy("D");
        service.setArmy(Player.DEFENDER, d);
        assertSame(d, service.getInactiveArmy());
    }

    // ---- switchActivePlayer ----

    @Test
    @DisplayName("switchActivePlayer toggles between ATTACKER and DEFENDER")
    void switchActivePlayer() {
        assertEquals(Player.ATTACKER, service.getCurrentState().getActivePlayer());
        service.switchActivePlayer();
        assertEquals(Player.DEFENDER, service.getCurrentState().getActivePlayer());
        service.switchActivePlayer();
        assertEquals(Player.ATTACKER, service.getCurrentState().getActivePlayer());
    }

    // ---- advancePhase ----

    @Test
    @DisplayName("advancePhase cycles through all phases in order")
    void advancePhaseOrder() {
        BattleState state = service.getCurrentState();
        assertEquals(Phase.COMMAND, state.getCurrentPhase());

        service.advancePhase();
        assertEquals(Phase.MOVEMENT, state.getCurrentPhase());

        service.advancePhase();
        assertEquals(Phase.SHOOTING, state.getCurrentPhase());

        service.advancePhase();
        assertEquals(Phase.CHARGE, state.getCurrentPhase());

        service.advancePhase();
        assertEquals(Phase.FIGHT, state.getCurrentPhase());
    }

    @Test
    @DisplayName("advancePhase from FIGHT resets to COMMAND and switches active player")
    void advancePhaseFightWraps() {
        // Advance attacker through all phases to FIGHT
        for (int i = 0; i < 4; i++) service.advancePhase(); // COMMAND -> ... -> FIGHT
        assertEquals(Phase.FIGHT, service.getCurrentState().getCurrentPhase());
        assertEquals(Player.ATTACKER, service.getCurrentState().getActivePlayer());

        // Advance from FIGHT should switch to DEFENDER, COMMAND
        service.advancePhase();
        assertEquals(Phase.COMMAND, service.getCurrentState().getCurrentPhase());
        assertEquals(Player.DEFENDER, service.getCurrentState().getActivePlayer());
    }

    @Test
    @DisplayName("Full round: after both players complete FIGHT, round increments")
    void fullRoundIncrementsRound() {
        // Attacker: COMMAND -> FIGHT (4 advances), then advance from FIGHT
        for (int i = 0; i < 5; i++) service.advancePhase();
        // Now defender COMMAND
        assertEquals(Player.DEFENDER, service.getCurrentState().getActivePlayer());
        assertEquals(1, service.getCurrentState().getCurrentRound());

        // Defender: COMMAND -> FIGHT (4 advances), then advance from FIGHT
        for (int i = 0; i < 5; i++) service.advancePhase();
        // Round should now be 2
        assertEquals(2, service.getCurrentState().getCurrentRound());
        assertEquals(Player.ATTACKER, service.getCurrentState().getActivePlayer());
    }

    // ---- nextRound ----

    @Test
    @DisplayName("nextRound increments round and resets phase/player")
    void nextRound() {
        service.advancePhase();
        service.switchActivePlayer();
        service.nextRound();

        BattleState state = service.getCurrentState();
        assertEquals(2, state.getCurrentRound());
        assertEquals(Phase.COMMAND, state.getCurrentPhase());
        assertEquals(Player.ATTACKER, state.getActivePlayer());
    }

    // ---- CP management ----

    @Test
    @DisplayName("addCp increases command points for a player")
    void addCp() {
        ArmyInstance a = makeArmy("A");
        service.setArmy(Player.ATTACKER, a);

        service.addCp(Player.ATTACKER, 3);
        assertEquals(3, a.getCurrentCp());
    }

    @Test
    @DisplayName("spendCp deducts CP when sufficient")
    void spendCpSuccess() {
        ArmyInstance a = makeArmy("A");
        a.setCurrentCp(5);
        service.setArmy(Player.ATTACKER, a);

        assertTrue(service.spendCp(Player.ATTACKER, 3));
        assertEquals(2, a.getCurrentCp());
    }

    @Test
    @DisplayName("spendCp fails when insufficient CP")
    void spendCpInsufficient() {
        ArmyInstance a = makeArmy("A");
        a.setCurrentCp(1);
        service.setArmy(Player.ATTACKER, a);

        assertFalse(service.spendCp(Player.ATTACKER, 5));
        assertEquals(1, a.getCurrentCp());
    }

    @Test
    @DisplayName("spendCp returns true for zero or negative amount")
    void spendCpZero() {
        ArmyInstance a = makeArmy("A");
        service.setArmy(Player.ATTACKER, a);
        assertTrue(service.spendCp(Player.ATTACKER, 0));
        assertTrue(service.spendCp(Player.ATTACKER, -1));
    }

    // ---- VP management ----

    @Test
    @DisplayName("addVp increases victory points")
    void addVp() {
        ArmyInstance a = makeArmy("A");
        service.setArmy(Player.ATTACKER, a);

        service.addVp(Player.ATTACKER, 5);
        assertEquals(5, a.getCurrentVp());
    }

    // ---- snapshot ----

    @Test
    @DisplayName("snapshot returns a deep copy that does not affect original state")
    void snapshot() {
        ArmyInstance a = makeArmy("A");
        a.setCurrentCp(10);
        service.setArmy(Player.ATTACKER, a);

        BattleState snap = service.snapshot();
        assertNotSame(service.getCurrentState(), snap);
        assertEquals("Test Mission", snap.getMissionName());

        // Modifying snapshot should not affect the original
        snap.setMissionName("Changed");
        assertEquals("Test Mission", service.getCurrentState().getMissionName());
    }

    // ---- helper ----

    private static ArmyInstance makeArmy(String name) {
        return new ArmyInstance(1, name, "faction", "Faction", "detachment");
    }
}
