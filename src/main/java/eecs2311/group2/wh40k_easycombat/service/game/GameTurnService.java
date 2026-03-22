package eecs2311.group2.wh40k_easycombat.service.game;

import eecs2311.group2.wh40k_easycombat.model.combat.PhaseAdvanceResult;
import eecs2311.group2.wh40k_easycombat.model.instance.Phase;
import eecs2311.group2.wh40k_easycombat.model.instance.Player;
import eecs2311.group2.wh40k_easycombat.model.instance.UnitInstance;
import eecs2311.group2.wh40k_easycombat.service.autobattle.AutoBattleMode;

import java.util.ArrayList;
import java.util.List;

public class GameTurnService {
    private Phase currentPhase = Phase.COMMAND;
    private Player activePlayer = Player.ATTACKER;
    private int currentRound = 1;

    public void reset() {
        currentPhase = Phase.COMMAND;
        activePlayer = Player.ATTACKER;
        currentRound = 1;
    }

    public Phase getCurrentPhase() {
        return currentPhase;
    }

    public Player getActivePlayer() {
        return activePlayer;
    }

    public Player getInactivePlayer() {
        return activePlayer == Player.ATTACKER ? Player.DEFENDER : Player.ATTACKER;
    }

    public int getCurrentRound() {
        return currentRound;
    }

    public boolean canOpenAutoBattle() {
        return currentAutoBattleMode() != null;
    }

    public AutoBattleMode currentAutoBattleMode() {
        return switch (currentPhase) {
            case MOVEMENT, CHARGE -> AutoBattleMode.REACTION_SHOOTING;
            case SHOOTING -> AutoBattleMode.SHOOTING;
            case FIGHT -> AutoBattleMode.FIGHT;
            case COMMAND -> null;
        };
    }

    public String phaseText() {
        return switch (currentPhase) {
            case COMMAND -> "Command";
            case MOVEMENT -> "Movement";
            case SHOOTING -> "Shooting";
            case CHARGE -> "Charge";
            case FIGHT -> "Fight";
        };
    }

    public String phaseLabelFor(Player player) {
        return player == activePlayer ? phaseText() + " (Active)" : phaseText();
    }

    public PhaseAdvanceResult advancePhase(List<UnitInstance> blueUnits, List<UnitInstance> redUnits) {
        if (currentPhase == Phase.FIGHT) {
            activePlayer = getInactivePlayer();
            currentPhase = Phase.COMMAND;

            if (activePlayer == Player.ATTACKER) {
                currentRound++;
            }

            clearBattleShockForCommandPhase(unitsFor(activePlayer, blueUnits, redUnits));
            resetForNewTurn(allUnits(blueUnits, redUnits));
            return snapshot(activePlayer);
        }

        currentPhase = currentPhase.next();

        if (currentPhase == Phase.SHOOTING) {
            resetForNewShootingPhase(unitsFor(activePlayer, blueUnits, redUnits));
        }

        if (currentPhase == Phase.FIGHT) {
            resetForNewFightPhase(allUnits(blueUnits, redUnits));
        }

        return snapshot(null);
    }

    private PhaseAdvanceResult snapshot(Player commandPointRecipient) {
        return new PhaseAdvanceResult(currentRound, currentPhase, activePlayer, commandPointRecipient);
    }

    private List<UnitInstance> allUnits(List<UnitInstance> blueUnits, List<UnitInstance> redUnits) {
        List<UnitInstance> result = new ArrayList<>();
        if (blueUnits != null) {
            result.addAll(blueUnits);
        }
        if (redUnits != null) {
            result.addAll(redUnits);
        }
        return result;
    }

    private List<UnitInstance> unitsFor(Player player, List<UnitInstance> blueUnits, List<UnitInstance> redUnits) {
        if (player == Player.ATTACKER) {
            return blueUnits == null ? List.of() : blueUnits;
        }
        return redUnits == null ? List.of() : redUnits;
    }

    private void resetForNewTurn(List<UnitInstance> units) {
        for (UnitInstance unit : units) {
            if (unit != null) {
                unit.resetForNewTurn();
            }
        }
    }

    private void resetForNewShootingPhase(List<UnitInstance> units) {
        for (UnitInstance unit : units) {
            if (unit != null) {
                unit.resetForNewShootingPhase();
            }
        }
    }

    private void resetForNewFightPhase(List<UnitInstance> units) {
        for (UnitInstance unit : units) {
            if (unit != null) {
                unit.resetForNewFightPhase();
            }
        }
    }

    private void clearBattleShockForCommandPhase(List<UnitInstance> units) {
        for (UnitInstance unit : units) {
            if (unit != null) {
                unit.setBattleShocked(false);
            }
        }
    }
}
