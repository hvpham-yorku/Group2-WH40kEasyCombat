package eecs2311.group2.wh40k_easycombat.service.game;

import eecs2311.group2.wh40k_easycombat.model.combat.AttackResult;
import eecs2311.group2.wh40k_easycombat.model.combat.PhaseAdvanceResult;
import eecs2311.group2.wh40k_easycombat.model.editor.EditorRerollType;
import eecs2311.group2.wh40k_easycombat.model.instance.ArmyInstance;
import eecs2311.group2.wh40k_easycombat.model.instance.Phase;
import eecs2311.group2.wh40k_easycombat.model.instance.Player;
import eecs2311.group2.wh40k_easycombat.model.instance.StratagemInstance;
import eecs2311.group2.wh40k_easycombat.model.instance.UnitInstance;
import eecs2311.group2.wh40k_easycombat.model.instance.UnitModelInstance;
import eecs2311.group2.wh40k_easycombat.model.instance.WeaponProfile;
import eecs2311.group2.wh40k_easycombat.service.autobattle.AttackKeywordContext;
import eecs2311.group2.wh40k_easycombat.service.autobattle.AutoBattleMode;
import eecs2311.group2.wh40k_easycombat.viewmodel.GameArmyImportVM;
import eecs2311.group2.wh40k_easycombat.viewmodel.GameArmyUnitVM;
import eecs2311.group2.wh40k_easycombat.viewmodel.GameStrategyVM;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class GameEngineTest {

    private GameEngine engine;

    @BeforeEach
    void setUp() {
        engine = new GameEngine();
        engine.start();
    }

    @Test
    @DisplayName("start seeds default battle state with attacker acting first")
    void startSeedsDefaultBattleState() {
        assertNotNull(engine.stateManager);
        assertEquals(1, engine.currentTurn);
        assertEquals(Phase.COMMAND, engine.getCurrentPhase());
        assertEquals(Player.ATTACKER, engine.getActivePlayer());
        assertEquals(1, engine.getCurrentRound());
        assertEquals(5, engine.getMaxRounds());
        assertEquals(2, engine.currentCp(Player.ATTACKER));
        assertEquals(1, engine.currentCp(Player.DEFENDER));
        assertEquals("Attacker Army", engine.getArmy(Player.ATTACKER).getArmyName());
        assertEquals("Defender Army", engine.getArmy(Player.DEFENDER).getArmyName());
    }

    @Test
    @DisplayName("configureBattle resets mission, max rounds and score state")
    void configureBattleResetsBattleState() {
        engine.addVictoryPoints(Player.ATTACKER, 8);
        engine.adjustCommandPoints(Player.DEFENDER, 3);

        engine.configureBattle("Take and Hold", 3);

        assertEquals("Take and Hold", engine.stateManager.getCurrentState().getMissionName());
        assertEquals(3, engine.getMaxRounds());
        assertEquals(1, engine.getCurrentRound());
        assertEquals(Phase.COMMAND, engine.getCurrentPhase());
        assertEquals(Player.ATTACKER, engine.getActivePlayer());
        assertEquals(2, engine.currentCp(Player.ATTACKER));
        assertEquals(1, engine.currentCp(Player.DEFENDER));
        assertEquals(0, engine.currentVp(Player.ATTACKER));
        assertEquals(0, engine.currentVp(Player.DEFENDER));
        assertFalse(engine.isBattleOver());
    }

    @Test
    @DisplayName("replaceArmy copies imported units and strategies into runtime battle state")
    void replaceArmyUsesImportedArmyData() {
        UnitInstance unit = makeUnit("Intercessor Squad");
        unit.addRangedWeapon(rangedWeapon("Bolt Rifle"));
        StratagemInstance strategy = new StratagemInstance("Command Re-roll", "1", "Any", "Any", "Re-roll one die");
        GameArmyImportVM vm = new GameArmyImportVM(
                10,
                "Gladius Task Force",
                "faction-id",
                "Space Marines",
                1000,
                List.of(new GameArmyUnitVM(unit)),
                List.of(new GameStrategyVM(strategy))
        );

        engine.replaceArmy(Player.ATTACKER, vm);

        ArmyInstance army = engine.getArmy(Player.ATTACKER);
        assertEquals("Gladius Task Force", army.getArmyName());
        assertEquals("Space Marines", army.getFactionName());
        assertEquals(1, army.getUnits().size());
        assertSame(unit, army.getUnits().getFirst());
        assertEquals(1, army.getStrategies().size());
        assertEquals("Command Re-roll", army.getStrategies().getFirst().name());
        assertEquals(2, army.getCurrentCp(), "Replacing the army should preserve current CP.");
    }

    @Test
    @DisplayName("advancePhase follows attacker then defender flow and awards CP on command handoff")
    void advancePhaseFollowsCurrentBattleFlow() {
        assertNull(engine.currentAutoBattleMode());

        assertEquals(Phase.MOVEMENT, engine.advancePhase().currentPhase());
        assertEquals(AutoBattleMode.REACTION_SHOOTING, engine.currentAutoBattleMode());

        engine.advancePhase();
        assertEquals(Phase.SHOOTING, engine.getCurrentPhase());
        assertEquals(AutoBattleMode.SHOOTING, engine.currentAutoBattleMode());

        engine.advancePhase();
        assertEquals(Phase.CHARGE, engine.getCurrentPhase());

        engine.advancePhase();
        assertEquals(Phase.FIGHT, engine.getCurrentPhase());
        assertEquals(AutoBattleMode.FIGHT, engine.currentAutoBattleMode());

        int defenderCpBefore = engine.currentCp(Player.DEFENDER);
        PhaseAdvanceResult handoff = engine.advancePhase();
        assertEquals(Phase.COMMAND, handoff.currentPhase());
        assertEquals(Player.DEFENDER, handoff.activePlayer());
        assertEquals(Player.DEFENDER, handoff.commandPointRecipient());
        assertEquals(defenderCpBefore + 1, engine.currentCp(Player.DEFENDER));
        assertEquals(1, engine.getCurrentRound());
    }

    @Test
    @DisplayName("battle is marked over once advancing past the configured max rounds")
    void battleEndsWhenAdvancingPastMaxRounds() {
        engine.configureBattle("Quick Battle", 1);

        for (int i = 0; i < 10; i++) {
            engine.advancePhase();
        }

        assertTrue(engine.isBattleOver());
        assertTrue(engine.getCurrentRound() > engine.getMaxRounds());
    }

    @Test
    @DisplayName("winner text follows the higher VP army and finishBattle locks the state")
    void winnerTextUsesRuntimeVp() {
        engine.addVictoryPoints(Player.ATTACKER, 15);
        engine.addVictoryPoints(Player.DEFENDER, 8);

        assertEquals("Battle Over. Attacker wins 15 to 8 VP.", engine.winnerText());

        engine.finishBattle();
        assertTrue(engine.isBattleOver());
    }

    @Test
    @DisplayName("performBattle resolves ranged attacks in shooting and melee in fight")
    void performBattleUsesCurrentPhaseRestrictions() {
        setUpArmiesWithUnits();

        engine.stateManager.getCurrentState().setCurrentPhase(Phase.SHOOTING);
        AttackResult ranged = engine.performBattle(getDefenderUnitId(), getAttackerUnitId(), "Bolt Rifle");
        assertTrue(ranged.resolved());

        engine.stateManager.getCurrentState().setCurrentPhase(Phase.FIGHT);
        AttackKeywordContext fightContext = new AttackKeywordContext(
                1,
                false,
                false,
                false,
                false,
                false,
                true,
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
        AttackResult melee = engine.performBattle(getDefenderUnitId(), getAttackerUnitId(), "Combat Knife", fightContext);
        assertTrue(melee.resolved());
    }

    @Test
    @DisplayName("mission and stratagem helpers still update runtime army state")
    void helperMethodsStillUpdateState() {
        setUpArmiesWithUnits();
        engine.getArmy(Player.ATTACKER).setCurrentCp(5);

        engine.selectMainMission("Take and Hold", 5);
        engine.selectAttackerMission("Assassinate", 3);
        engine.completedMission();
        engine.completedSecondaryMission();
        engine.useStrategem("Command Re-roll");

        assertEquals(8, engine.currentVp(Player.ATTACKER));
        assertEquals("", engine.getArmy(Player.ATTACKER).getSecondaryMissionName());
        assertEquals(4, engine.currentCp(Player.ATTACKER));
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
        return engine.stateManager.getArmy(Player.ATTACKER).getUnits().getFirst().getInstanceId();
    }

    private String getDefenderUnitId() {
        return engine.stateManager.getArmy(Player.DEFENDER).getUnits().getFirst().getInstanceId();
    }

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
}
