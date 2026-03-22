package eecs2311.group2.wh40k_easycombat.service.game;

import eecs2311.group2.wh40k_easycombat.model.combat.PhaseAdvanceResult;
import eecs2311.group2.wh40k_easycombat.model.instance.Phase;
import eecs2311.group2.wh40k_easycombat.model.instance.Player;
import eecs2311.group2.wh40k_easycombat.model.instance.UnitInstance;
import eecs2311.group2.wh40k_easycombat.model.instance.UnitModelInstance;
import eecs2311.group2.wh40k_easycombat.model.instance.WeaponProfile;
import eecs2311.group2.wh40k_easycombat.service.autobattle.AutoBattleMode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class GameTurnServiceTest {

    private GameTurnService service;

    @BeforeEach
    void setUp() {
        service = new GameTurnService();
    }

    @Test
    @DisplayName("Auto battle mode follows the current phase")
    void currentAutoBattleModeMatchesPhase() {
        assertNull(service.currentAutoBattleMode());
        assertFalse(service.canOpenAutoBattle());

        service.advancePhase(List.of(), List.of());
        assertEquals(Phase.MOVEMENT, service.getCurrentPhase());
        assertEquals(AutoBattleMode.REACTION_SHOOTING, service.currentAutoBattleMode());

        service.advancePhase(List.of(), List.of());
        assertEquals(Phase.SHOOTING, service.getCurrentPhase());
        assertEquals(AutoBattleMode.SHOOTING, service.currentAutoBattleMode());

        service.advancePhase(List.of(), List.of());
        assertEquals(Phase.CHARGE, service.getCurrentPhase());
        assertEquals(AutoBattleMode.REACTION_SHOOTING, service.currentAutoBattleMode());

        service.advancePhase(List.of(), List.of());
        assertEquals(Phase.FIGHT, service.getCurrentPhase());
        assertEquals(AutoBattleMode.FIGHT, service.currentAutoBattleMode());
        assertTrue(service.canOpenAutoBattle());
    }

    @Test
    @DisplayName("Turn order runs through blue then red and round increments after red fight")
    void advancePhaseCyclesBetweenPlayersAndRounds() {
        PhaseAdvanceResult toRedCommand = null;
        for (int i = 0; i < 5; i++) {
            toRedCommand = service.advancePhase(List.of(), List.of());
        }

        assertNotNull(toRedCommand);
        assertEquals(1, toRedCommand.currentRound());
        assertEquals(Phase.COMMAND, toRedCommand.currentPhase());
        assertEquals(Player.DEFENDER, toRedCommand.activePlayer());
        assertEquals(Player.DEFENDER, toRedCommand.commandPointRecipient());
        assertTrue(toRedCommand.awardedCommandPoint());

        PhaseAdvanceResult toBlueCommand = null;
        for (int i = 0; i < 5; i++) {
            toBlueCommand = service.advancePhase(List.of(), List.of());
        }

        assertNotNull(toBlueCommand);
        assertEquals(2, toBlueCommand.currentRound());
        assertEquals(Phase.COMMAND, toBlueCommand.currentPhase());
        assertEquals(Player.ATTACKER, toBlueCommand.activePlayer());
        assertEquals(Player.ATTACKER, toBlueCommand.commandPointRecipient());
        assertTrue(toBlueCommand.awardedCommandPoint());
    }

    @Test
    @DisplayName("Entering shooting clears used ranged weapons for the active player only")
    void enteringShootingClearsUsedRangedWeaponsForActivePlayer() {
        UnitInstance blueUnit = makeUnitWithRangedWeapon("Blue Unit");
        UnitInstance redUnit = makeUnitWithRangedWeapon("Red Unit");
        WeaponProfile blueWeapon = blueUnit.getRangedWeapons().getFirst();
        WeaponProfile redWeapon = redUnit.getRangedWeapons().getFirst();

        blueUnit.markRangedWeaponUsedThisPhase(blueWeapon);
        redUnit.markRangedWeaponUsedThisPhase(redWeapon);

        service.advancePhase(List.of(blueUnit), List.of(redUnit));
        service.advancePhase(List.of(blueUnit), List.of(redUnit));

        assertEquals(Phase.SHOOTING, service.getCurrentPhase());
        assertFalse(blueUnit.hasUsedRangedWeaponThisPhase(blueWeapon));
        assertTrue(redUnit.hasUsedRangedWeaponThisPhase(redWeapon));
    }

    @Test
    @DisplayName("Advancing from fight clears battle-shock for the new active player and resets turn flags")
    void advancingFromFightResetsStatuses() {
        UnitInstance blueUnit = makeUnitWithRangedWeapon("Blue Unit");
        UnitInstance redUnit = makeUnitWithRangedWeapon("Red Unit");

        blueUnit.setBattleShocked(true);
        redUnit.setBattleShocked(true);
        blueUnit.setChargedThisTurn(true);
        redUnit.setChargedThisTurn(true);
        blueUnit.setEligibleToFightThisPhase(true);
        redUnit.setEligibleToFightThisPhase(true);
        blueUnit.setFoughtThisPhase(true);
        redUnit.setFoughtThisPhase(true);

        for (int i = 0; i < 4; i++) {
            service.advancePhase(List.of(blueUnit), List.of(redUnit));
        }

        PhaseAdvanceResult result = service.advancePhase(List.of(blueUnit), List.of(redUnit));

        assertEquals(Phase.COMMAND, result.currentPhase());
        assertEquals(Player.DEFENDER, result.activePlayer());
        assertFalse(redUnit.isBattleShocked());
        assertTrue(blueUnit.isBattleShocked());
        assertFalse(blueUnit.hasChargedThisTurn());
        assertFalse(redUnit.hasChargedThisTurn());
        assertFalse(blueUnit.isEligibleToFightThisPhase());
        assertFalse(redUnit.isEligibleToFightThisPhase());
        assertFalse(blueUnit.hasFoughtThisPhase());
        assertFalse(redUnit.hasFoughtThisPhase());
    }

    private static UnitInstance makeUnitWithRangedWeapon(String unitName) {
        UnitInstance unit = new UnitInstance("datasheet", unitName);
        unit.addModel(new UnitModelInstance("Model", "6", "4", "3+", "2", "6+", "1", ""));
        unit.addRangedWeapon(new WeaponProfile(
                1,
                "Bolt Rifle",
                "",
                1,
                "24",
                "2",
                "3+",
                "4",
                "-1",
                "1",
                false
        ));
        return unit;
    }
}
