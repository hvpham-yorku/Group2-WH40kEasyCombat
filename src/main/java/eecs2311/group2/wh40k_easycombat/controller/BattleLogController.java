package eecs2311.group2.wh40k_easycombat.controller;

import eecs2311.group2.wh40k_easycombat.service.BattleLogService;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.stage.Stage;

public class BattleLogController {
    // ======================= Labels =======================
    @FXML private Label headerLabel;
    @FXML private Label summaryLabel;

    // ======================= Content =======================
    @FXML private TextArea battleLogArea;

    // ======================= Buttons =======================
    @FXML private Button refreshButton;
    @FXML private Button closeButton;

    private final BattleLogService battleLogService = BattleLogService.getInstance();

    // When this page loads, initialize the battle log display area.
    @FXML
    private void initialize() {
        battleLogArea.setEditable(false);
        battleLogArea.setWrapText(true);
        refreshDisplay();
    }

    public void setContext(String titleText) {
        headerLabel.setText(titleText == null || titleText.isBlank() ? "Battle Log" : titleText);
        refreshDisplay();
    }

    // When click "Refresh" button, reload the latest battle log records.
    @FXML
    private void refreshLog(ActionEvent event) {
        refreshDisplay();
    }

    // When click "Close" button, close the battle log window.
    @FXML
    private void closeWindow(ActionEvent event) {
        ((Stage) closeButton.getScene().getWindow()).close();
    }

    private void refreshDisplay() {
        battleLogArea.setText(battleLogService.displayText());
        battleLogArea.positionCaret(0);
        summaryLabel.setText("Total Entries: " + battleLogService.entryCount());
    }
}
