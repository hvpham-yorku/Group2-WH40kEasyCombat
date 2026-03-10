package eecs2311.group2.wh40k_easycombat.controller;

import assembler.SavedArmyGameAssembler;
import assembler.SavedArmyGameAssembler.ImportedArmyData;
import assembler.SavedArmyGameAssembler.SavedArmyOption;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import javafx.stage.Stage;

public class ArmyImportController {

    @FXML private TableView<SavedArmyOption> armyTable;
    @FXML private TableColumn<SavedArmyOption, String> armyName;
    @FXML private TableColumn<SavedArmyOption, Number> armyPoints;
    @FXML private Button importButton;

    private GameUIController parentController;
    private GameUIController.ArmySide targetSide;

    @FXML
    private void initialize() {
        setupTable();
        loadSavedArmies();
    }

    public void setImportContext(GameUIController parentController, GameUIController.ArmySide targetSide) {
        this.parentController = parentController;
        this.targetSide = targetSide;
    }

    private void setupTable() {
        armyName.setCellValueFactory(data ->
                new SimpleStringProperty(
                        data.getValue().marked()
                                ? "★ " + data.getValue().name()
                                : data.getValue().name()
                )
        );

        armyPoints.setCellValueFactory(data ->
                new SimpleIntegerProperty(data.getValue().points())
        );
    }

    private void loadSavedArmies() {
        try {
            armyTable.setItems(FXCollections.observableArrayList(SavedArmyGameAssembler.loadSavedArmies()));
        } catch (Exception e) {
            showError("Load Saved Armies Error", e.getMessage());
        }
    }

    @FXML
    void importArmy(MouseEvent event) {
        SavedArmyOption selected = armyTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showWarning("No Army Selected", "Please select one army to import.");
            return;
        }

        if (parentController == null || targetSide == null) {
            showWarning("Import Error", "Import target is not set.");
            return;
        }

        try {
            ImportedArmyData data = SavedArmyGameAssembler.importArmy(selected.armyId());
            if (data == null) {
                showWarning("Import Failed", "The selected army could not be imported.");
                return;
            }

            parentController.acceptImportedArmy(targetSide, data);

            Stage stage = (Stage) importButton.getScene().getWindow();
            stage.close();
        } catch (Exception e) {
            showError("Import Error", e.getMessage());
        }
    }

    private void showWarning(String title, String text) {
        Alert a = new Alert(Alert.AlertType.WARNING, text, ButtonType.OK);
        a.setHeaderText(title);
        a.showAndWait();
    }

    private void showError(String title, String text) {
        Alert a = new Alert(Alert.AlertType.ERROR, text == null ? "Unknown error." : text, ButtonType.OK);
        a.setHeaderText(title);
        a.showAndWait();
    }
}