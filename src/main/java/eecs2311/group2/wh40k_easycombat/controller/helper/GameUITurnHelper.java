package eecs2311.group2.wh40k_easycombat.controller.helper;

import eecs2311.group2.wh40k_easycombat.model.combat.PhaseAdvanceResult;
import eecs2311.group2.wh40k_easycombat.model.instance.Phase;
import eecs2311.group2.wh40k_easycombat.model.instance.Player;
import eecs2311.group2.wh40k_easycombat.service.BattleLogService;
import eecs2311.group2.wh40k_easycombat.service.editor.EditorEffectRuntimeService;
import eecs2311.group2.wh40k_easycombat.service.game.GameEngine;
import eecs2311.group2.wh40k_easycombat.service.mission.MissionSessionService;

import java.util.function.Function;

public final class GameUITurnHelper {

    private GameUITurnHelper() {
    }

    public static void nextPhase(
            GameEngine gameEngine,
            EditorEffectRuntimeService editorEffectRuntimeService,
            MissionSessionService missionSessionService,
            BattleLogService battleLogService,
            PhaseLabelText phaseLabelText,
            TurnCallbacks callbacks
    ) {
        if (gameEngine.isBattleOver()) {
            DialogHelper.showInfo("Battle Over", gameEngine.winnerText());
            return;
        }

        if (!DialogHelper.confirmYesNo("Next Phase", "Advance to the next phase?")) {
            return;
        }

        int previousRound = gameEngine.getCurrentRound();
        Phase previousPhase = gameEngine.getCurrentPhase();
        Player previousActivePlayer = gameEngine.getActivePlayer();
        PhaseAdvanceResult result = gameEngine.advancePhase();
        editorEffectRuntimeService.clearExpiredEffects(
                gameEngine.getCurrentRound(),
                gameEngine.getCurrentPhase(),
                gameEngine.getActivePlayer()
        );

        StringBuilder phaseLog = new StringBuilder();
        phaseLog.append("Phase advanced from Round ")
                .append(previousRound)
                .append(" ")
                .append(phaseLabelText.phaseName().apply(previousPhase))
                .append(" (")
                .append(phaseLabelText.playerLabel().apply(previousActivePlayer))
                .append(" active)")
                .append(" to Round ")
                .append(gameEngine.getCurrentRound())
                .append(" ")
                .append(phaseLabelText.phaseName().apply(gameEngine.getCurrentPhase()))
                .append(" (")
                .append(phaseLabelText.playerLabel().apply(gameEngine.getActivePlayer()))
                .append(" active).");

        if (gameEngine.getCurrentRound() > gameEngine.getMaxRounds()) {
            battleLogService.logTurnEvent(previousRound, previousPhase, previousActivePlayer, phaseLog.toString());
            callbacks.finishBattle().run();
            return;
        }

        if (result.awardedCommandPoint()) {
            int afterCp = callbacks.currentCp().apply(result.commandPointRecipient());
            int beforeCp = Math.max(0, afterCp - 1);
            missionSessionService.startTurn(result.commandPointRecipient());
            phaseLog.append(" ")
                    .append(phaseLabelText.playerLabel().apply(result.commandPointRecipient()))
                    .append(" gained 1 CP (")
                    .append(beforeCp)
                    .append(" -> ")
                    .append(afterCp)
                    .append(").");
        }

        battleLogService.logTurnEvent(
                gameEngine.getCurrentRound(),
                gameEngine.getCurrentPhase(),
                gameEngine.getActivePlayer(),
                phaseLog.toString()
        );
        callbacks.syncTurnUi().run();
        callbacks.refreshArmyViews().run();
        callbacks.refreshMissionTables().run();
        callbacks.maybeOpenBattleShockWindow().run();
    }

    public record PhaseLabelText(
            Function<Phase, String> phaseName,
            Function<Player, String> playerLabel
    ) {
    }

    public record TurnCallbacks(
            Runnable finishBattle,
            Runnable syncTurnUi,
            Runnable refreshArmyViews,
            Runnable refreshMissionTables,
            Runnable maybeOpenBattleShockWindow,
            Function<Player, Integer> currentCp
    ) {
    }
}
