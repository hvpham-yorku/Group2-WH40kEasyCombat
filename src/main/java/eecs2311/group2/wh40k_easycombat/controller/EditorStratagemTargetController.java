package eecs2311.group2.wh40k_easycombat.controller;

import eecs2311.group2.wh40k_easycombat.model.editor.EditorRuleDefinition;
import eecs2311.group2.wh40k_easycombat.viewmodel.GameArmyUnitVM;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.TextArea;
import javafx.stage.Stage;

import java.util.List;
import java.util.stream.Collectors;

public class EditorStratagemTargetController {
    @FXML private Label titleLabel;
    @FXML private Label subtitleLabel;
    @FXML private TextArea rulesTextArea;
    @FXML private ListView<GameArmyUnitVM> unitListView;
    @FXML private Button applyButton;
    @FXML private Button cancelButton;

    private GameArmyUnitVM selectedUnit;

    @FXML
    private void initialize() {
        unitListView.setCellFactory(list -> new ListCell<>() {
            @Override
            protected void updateItem(GameArmyUnitVM item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    return;
                }

                setText(item.getUnitName()
                        + " | Alive " + item.getAliveModelCount()
                        + "/" + item.getSubUnits().size());
            }
        });

        unitListView.getSelectionModel().selectedItemProperty().addListener((obs, oldValue, newValue) -> {
            selectedUnit = newValue;
            applyButton.setDisable(newValue == null);
        });

        rulesTextArea.setEditable(false);
        applyButton.setDisable(true);
    }

    public void setContext(
            String sideLabel,
            String stratagemName,
            List<EditorRuleDefinition> matchingRules,
            List<GameArmyUnitVM> candidateUnits
    ) {
        titleLabel.setText((sideLabel == null || sideLabel.isBlank() ? "Player" : sideLabel)
                + " Stratagem Target");
        subtitleLabel.setText("Choose one affected unit for \"" + safe(stratagemName, "Selected Stratagem") + "\".");
        rulesTextArea.setText(formatRules(matchingRules));
        unitListView.setItems(FXCollections.observableArrayList(candidateUnits == null ? List.of() : candidateUnits));

        if (!unitListView.getItems().isEmpty()) {
            unitListView.getSelectionModel().selectFirst();
        }
    }

    public GameArmyUnitVM getSelectedUnit() {
        return selectedUnit;
    }

    @FXML
    private void applySelection() {
        selectedUnit = unitListView.getSelectionModel().getSelectedItem();
        closeWindow();
    }

    @FXML
    private void cancel() {
        selectedUnit = null;
        closeWindow();
    }

    private void closeWindow() {
        Stage stage = (Stage) cancelButton.getScene().getWindow();
        stage.close();
    }

    private String formatRules(List<EditorRuleDefinition> matchingRules) {
        if (matchingRules == null || matchingRules.isEmpty()) {
            return "No custom rule effects matched this stratagem.";
        }

        return matchingRules.stream()
                .map(rule -> safe(rule.getName(), "Untitled Rule") + " | " + rule.getDuration())
                .collect(Collectors.joining("\n"));
    }

    private String safe(String value, String fallback) {
        if (value == null || value.isBlank()) {
            return fallback;
        }
        return value.trim();
    }
}
