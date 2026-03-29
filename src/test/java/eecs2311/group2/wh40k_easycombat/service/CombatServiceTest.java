package eecs2311.group2.wh40k_easycombat.service;

import eecs2311.group2.wh40k_easycombat.model.combat.PhaseAdvanceResult;
import eecs2311.group2.wh40k_easycombat.model.instance.ArmyInstance;
import eecs2311.group2.wh40k_easycombat.model.instance.BattleState;
import eecs2311.group2.wh40k_easycombat.model.instance.Phase;
import eecs2311.group2.wh40k_easycombat.model.instance.Player;
import eecs2311.group2.wh40k_easycombat.model.instance.UnitInstance;
import eecs2311.group2.wh40k_easycombat.model.instance.UnitModelInstance;
import eecs2311.group2.wh40k_easycombat.model.instance.WeaponProfile;
import eecs2311.group2.wh40k_easycombat.service.autobattle.AutoBattleMode;
import eecs2311.group2.wh40k_easycombat.service.game.CombatService;
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

    @Test
    @DisplayName("constructors create a default command phase battle")
    void constructorsCreateExpectedDefaults() {
        CombatService blank = new CombatService();
        assertEquals("", blank.getCurrentState().getMissionName());
        assertEquals("Test Mission", service.getCurrentState().getMissionName());
        assertEquals(1, service.getCurrentRound());
        assertEquals(Phase.COMMAND, service.getCurrentPhase());
        assertEquals(Player.ATTACKER, service.getActivePlayer());
    }

    @Test
    @DisplayName("resetBattle sets mission and max rounds")
    void resetBattleSetsMissionAndMaxRounds() {
        service.resetBattle("New Mission", 3);

        assertEquals("New Mission", service.getCurrentState().getMissionName());
        assertEquals(3, service.getMaxRounds());
        assertEquals(1, service.getCurrentRound());
        assertEquals(Phase.COMMAND, service.getCurrentPhase());
    }

    @Test
    @DisplayName("startBattle overload keeps attacker and defender armies")
    void startBattleStoresArmies() {
        ArmyInstance attacker = makeArmy("Attacker");
        ArmyInstance defender = makeArmy("Defender");

        service.startBattle("Battle", 4, attacker, defender);

        assertEquals("Battle", service.getCurrentState().getMissionName());
        assertEquals(4, service.getMaxRounds());
        assertSame(attacker, service.getArmy(Player.ATTACKER));
        assertSame(defender, service.getArmy(Player.DEFENDER));
    }

    @Test
    @DisplayName("advancePhaseState wraps fight to defender command and awards one CP")
    void advancePhaseStateWrapsAndAwardsCp() {
        ArmyInstance attacker = makeArmy("Attacker");
        ArmyInstance defender = makeArmy("Defender");
        defender.setCurrentCp(2);
        defender.addUnit(makeUnitWithWeapon("Defender Unit"));
        attacker.addUnit(makeUnitWithWeapon("Attacker Unit"));
        service.startBattle("Battle", 5, attacker, defender);

        for (int i = 0; i < 4; i++) {
            service.advancePhase();
        }

        PhaseAdvanceResult result = service.advancePhaseState();

        assertEquals(Phase.COMMAND, result.currentPhase());
        assertEquals(Player.DEFENDER, result.activePlayer());
        assertEquals(Player.DEFENDER, result.commandPointRecipient());
        assertEquals(3, service.getCurrentCp(Player.DEFENDER));
        assertNull(service.currentAutoBattleMode());
    }

    @Test
    @DisplayName("auto battle helpers reflect the current phase")
    void autoBattleHelpersReflectPhase() {
        assertNull(service.currentAutoBattleMode());
        assertFalse(service.canOpenAutoBattle());

        service.advancePhase();
        assertEquals(AutoBattleMode.REACTION_SHOOTING, service.currentAutoBattleMode());
        assertTrue(service.canOpenAutoBattle());

        service.advancePhase();
        assertEquals(AutoBattleMode.SHOOTING, service.currentAutoBattleMode());
        assertEquals("Shooting", service.phaseText());
        assertEquals("Shooting (Active)", service.phaseLabelFor(Player.ATTACKER));
        assertEquals("Shooting", service.phaseLabelFor(Player.DEFENDER));
    }

    @Test
    @DisplayName("command point and victory point helpers update the selected army")
    void pointHelpersUpdateSelectedArmy() {
        ArmyInstance attacker = makeArmy("Attacker");
        service.setArmy(Player.ATTACKER, attacker);

        service.addCp(Player.ATTACKER, 3);
        assertEquals(3, service.getCurrentCp(Player.ATTACKER));

        assertTrue(service.spendCp(Player.ATTACKER, 2));
        assertEquals(1, service.getCurrentCp(Player.ATTACKER));

        service.setCurrentCp(Player.ATTACKER, 7);
        service.addVp(Player.ATTACKER, 5);
        service.setCurrentVp(Player.ATTACKER, 11);

        assertEquals(7, service.getCurrentCp(Player.ATTACKER));
        assertEquals(11, service.getCurrentVp(Player.ATTACKER));
    }

    @Test
    @DisplayName("battle over flag and snapshot are delegated through the wrapped state")
    void battleOverAndSnapshotUseWrappedState() {
        ArmyInstance attacker = makeArmy("Attacker");
        attacker.setCurrentCp(4);
        attacker.setCurrentVp(9);
        service.setArmy(Player.ATTACKER, attacker);
        service.setBattleOver(true);

        BattleState snapshot = service.snapshot();

        assertTrue(service.isBattleOver());
        assertTrue(snapshot.isBattleOver());
        assertEquals(4, snapshot.getCurrentCp(Player.ATTACKER));
        assertEquals(9, snapshot.getCurrentVp(Player.ATTACKER));

        snapshot.setMissionName("Changed");
        snapshot.setCurrentCp(Player.ATTACKER, 0);
        assertEquals("Test Mission", service.getCurrentState().getMissionName());
        assertEquals(4, service.getCurrentCp(Player.ATTACKER));
    }

    private static ArmyInstance makeArmy(String name) {
        return new ArmyInstance(1, name, "faction", "Faction", "detachment");
    }

    private static UnitInstance makeUnitWithWeapon(String unitName) {
        UnitInstance unit = new UnitInstance("datasheet", unitName);
        unit.addModel(new UnitModelInstance("Model", "6", "4", "3+", "2", "6+", "1", ""));
        unit.addRangedWeapon(new WeaponProfile(1, "Bolt Rifle", "", 1, "24", "2", "3+", "4", "-1", "1", false));
        return unit;
    }
}
