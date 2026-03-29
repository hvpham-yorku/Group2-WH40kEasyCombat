package eecs2311.group2.wh40k_easycombat.controller.helper;

import eecs2311.group2.wh40k_easycombat.model.instance.Player;
import eecs2311.group2.wh40k_easycombat.service.BattleLogService;
import eecs2311.group2.wh40k_easycombat.service.game.GameEngine;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;

public final class GameUIScoreHelper {

    private GameUIScoreHelper() {
    }

    public static void syncScoreLabels(
            GameEngine gameEngine,
            Label attackerCpLabel,
            Label defenderCpLabel,
            Label attackerVpLabel,
            Label defenderVpLabel
    ) {
        if (attackerCpLabel != null) {
            attackerCpLabel.setText(String.valueOf(gameEngine.currentCp(Player.ATTACKER)));
        }
        if (defenderCpLabel != null) {
            defenderCpLabel.setText(String.valueOf(gameEngine.currentCp(Player.DEFENDER)));
        }
        if (attackerVpLabel != null) {
            attackerVpLabel.setText(String.valueOf(gameEngine.currentVp(Player.ATTACKER)));
        }
        if (defenderVpLabel != null) {
            defenderVpLabel.setText(String.valueOf(gameEngine.currentVp(Player.DEFENDER)));
        }
    }

    public static void updateWinnerLabel(GameEngine gameEngine, Label winnerLabel) {
        if (winnerLabel == null) {
            return;
        }

        if (gameEngine.isBattleOver()) {
            winnerLabel.setText(gameEngine.winnerText());
            return;
        }

        int attackerVp = gameEngine.currentVp(Player.ATTACKER);
        int defenderVp = gameEngine.currentVp(Player.DEFENDER);

        if (attackerVp == defenderVp) {
            winnerLabel.setText("Current Score: Tied at " + attackerVp + " VP");
            return;
        }

        boolean attackerLeading = attackerVp > defenderVp;
        winnerLabel.setText(
                "Current Leader: "
                        + (attackerLeading ? "Attacker" : "Defender")
                        + " ("
                        + (attackerLeading ? attackerVp : defenderVp)
                        + "-"
                        + (attackerLeading ? defenderVp : attackerVp)
                        + ")"
        );
    }

    public static void syncTurnUi(
            GameEngine gameEngine,
            Label roundLabel,
            Label attackerPhaseLabel,
            Label defenderPhaseLabel,
            Label attackerCpLabel,
            Label defenderCpLabel,
            Label attackerVpLabel,
            Label defenderVpLabel,
            Label winnerLabel,
            Label primaryMissionStateLabel,
            javafx.scene.control.Button nextPhaseButton,
            CheckBox autoBattleCheckBox,
            Runnable updateSecondaryMissionButtons
    ) {
        if (roundLabel != null) {
            roundLabel.setText(String.valueOf(gameEngine.getCurrentRound()));
        }

        if (attackerPhaseLabel != null) {
            attackerPhaseLabel.setText(gameEngine.phaseLabelFor(Player.ATTACKER));
        }

        if (defenderPhaseLabel != null) {
            defenderPhaseLabel.setText(gameEngine.phaseLabelFor(Player.DEFENDER));
        }

        syncScoreLabels(gameEngine, attackerCpLabel, defenderCpLabel, attackerVpLabel, defenderVpLabel);
        updateWinnerLabel(gameEngine, winnerLabel);
        updateSecondaryMissionButtons.run();

        if (nextPhaseButton != null) {
            nextPhaseButton.setDisable(gameEngine.isBattleOver());
        }
        if (autoBattleCheckBox != null) {
            autoBattleCheckBox.setDisable(gameEngine.isBattleOver());
        }
        if (primaryMissionStateLabel != null && primaryMissionStateLabel.getText() == null) {
            primaryMissionStateLabel.setText("");
        }
    }

    public static void finishBattle(
            GameEngine gameEngine,
            BattleLogService battleLogService,
            Runnable syncTurnUi
    ) {
        gameEngine.finishBattle();
        syncTurnUi.run();
        battleLogService.log(gameEngine.winnerText());
        DialogHelper.showInfo("Battle Over", gameEngine.winnerText());
    }
}
