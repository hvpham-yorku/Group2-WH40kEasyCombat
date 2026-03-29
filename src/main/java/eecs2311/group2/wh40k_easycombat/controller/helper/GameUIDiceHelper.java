package eecs2311.group2.wh40k_easycombat.controller.helper;

import eecs2311.group2.wh40k_easycombat.service.BattleLogService;
import eecs2311.group2.wh40k_easycombat.service.calculations.DiceService;
import eecs2311.group2.wh40k_easycombat.service.game.GameEngine;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Spinner;
import javafx.scene.control.TextArea;

import java.util.List;

public final class GameUIDiceHelper {

    private GameUIDiceHelper() {
    }

    public static void rollDice(
            DiceService diceService,
            Spinner<Integer> diceSpinner,
            ComboBox<Integer> successComboBox,
            TextArea diceLogBox,
            BattleLogService battleLogService,
            GameEngine gameEngine
    ) {
        int diceCount = diceSpinner == null || diceSpinner.getValue() == null
                ? 1
                : Math.max(1, diceSpinner.getValue());
        int successThreshold = successComboBox == null || successComboBox.getValue() == null
                ? 4
                : Math.max(1, successComboBox.getValue());

        diceService.rollDice(diceCount);

        List<Integer> results = diceService.getResults();
        int successCount = (int) results.stream()
                .filter(result -> result != null && result >= successThreshold)
                .count();

        StringBuilder sb = new StringBuilder();
        sb.append("Rolled ").append(diceCount).append("D6");
        sb.append(" | Success on ").append(successThreshold).append("+");
        sb.append(" -> ");
        sb.append(results);
        sb.append(" | Successes: ").append(successCount);
        sb.append("\n");

        if (diceLogBox != null) {
            diceLogBox.appendText(sb.toString());
        }

        battleLogService.logTurnEvent(
                gameEngine.getCurrentRound(),
                gameEngine.getCurrentPhase(),
                gameEngine.getActivePlayer(),
                "Manual dice roll: "
                        + diceCount
                        + "D6, success on "
                        + successThreshold
                        + "+ -> "
                        + results
                        + ", successes "
                        + successCount
                        + "."
        );
    }

    public static void clearDiceLog(TextArea diceLogBox) {
        if (diceLogBox == null) {
            return;
        }

        diceLogBox.clear();
        diceLogBox.setText("Manual dice log ready.\n");
    }
}
