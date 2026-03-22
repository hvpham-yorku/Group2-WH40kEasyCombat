package eecs2311.group2.wh40k_easycombat.service.autobattle;

import eecs2311.group2.wh40k_easycombat.model.combat.FightPhaseState;
import eecs2311.group2.wh40k_easycombat.model.instance.Player;
import eecs2311.group2.wh40k_easycombat.model.instance.UnitInstance;

import java.util.List;

public final class FightPhaseOrderService {

    private FightPhaseOrderService() {
    }

    public static FightPhaseState rebuildState(
            Player activeTurnPlayer,
            FightStep previousStep,
            Player lastFightPlayer,
            List<UnitInstance> blueUnits,
            List<UnitInstance> redUnits
    ) {
        FightStep currentStep = determineCurrentStep(blueUnits, redUnits);
        if (currentStep == FightStep.COMPLETE) {
            return FightPhaseState.complete("All marked eligible units have fought.");
        }

        Player preferredPlayer = (lastFightPlayer == null || previousStep == null || currentStep != previousStep)
                ? opposite(activeTurnPlayer)
                : opposite(lastFightPlayer);

        Player nextPlayer = pickNextPlayer(currentStep, preferredPlayer, blueUnits, redUnits);
        if (nextPlayer == null) {
            return FightPhaseState.complete("No eligible units remain.");
        }

        String stepText = currentStep == FightStep.FIGHTS_FIRST
                ? "Fights First step"
                : "Remaining combats step";

        return new FightPhaseState(
                currentStep,
                nextPlayer,
                stepText + " - " + label(nextPlayer) + " chooses next."
        );
    }

    private static FightStep determineCurrentStep(
            List<UnitInstance> blueUnits,
            List<UnitInstance> redUnits
    ) {
        if (hasUnitsForStep(FightStep.FIGHTS_FIRST, blueUnits)
                || hasUnitsForStep(FightStep.FIGHTS_FIRST, redUnits)) {
            return FightStep.FIGHTS_FIRST;
        }

        if (hasUnitsForStep(FightStep.REMAINING_COMBATANTS, blueUnits)
                || hasUnitsForStep(FightStep.REMAINING_COMBATANTS, redUnits)) {
            return FightStep.REMAINING_COMBATANTS;
        }

        return FightStep.COMPLETE;
    }

    private static boolean hasUnitsForStep(FightStep step, List<UnitInstance> units) {
        if (units == null || units.isEmpty()) {
            return false;
        }

        for (UnitInstance unit : units) {
            if (isEligibleForStep(step, unit)) {
                return true;
            }
        }

        return false;
    }

    private static boolean isEligibleForStep(FightStep step, UnitInstance unit) {
        if (unit == null || unit.isDestroyed()) {
            return false;
        }
        if (!unit.isEligibleToFightThisPhase() || unit.hasFoughtThisPhase()) {
            return false;
        }

        if (step == FightStep.FIGHTS_FIRST) {
            return unit.hasFightsFirst() || unit.hasChargedThisTurn();
        }

        if (step == FightStep.REMAINING_COMBATANTS) {
            return true;
        }

        return false;
    }

    private static Player pickNextPlayer(
            FightStep step,
            Player preferredPlayer,
            List<UnitInstance> blueUnits,
            List<UnitInstance> redUnits
    ) {
        if (preferredPlayer != null && hasUnitsForPlayer(step, preferredPlayer, blueUnits, redUnits)) {
            return preferredPlayer;
        }

        Player other = preferredPlayer == null ? null : opposite(preferredPlayer);
        if (other != null && hasUnitsForPlayer(step, other, blueUnits, redUnits)) {
            return other;
        }

        return null;
    }

    private static boolean hasUnitsForPlayer(
            FightStep step,
            Player player,
            List<UnitInstance> blueUnits,
            List<UnitInstance> redUnits
    ) {
        List<UnitInstance> units = player == Player.ATTACKER ? blueUnits : redUnits;
        return hasUnitsForStep(step, units);
    }

    private static Player opposite(Player player) {
        if (player == null) {
            return null;
        }
        return player == Player.ATTACKER ? Player.DEFENDER : Player.ATTACKER;
    }

    private static String label(Player player) {
        if (player == null) {
            return "None";
        }
        return player == Player.ATTACKER ? "Blue" : "Red";
    }
}
