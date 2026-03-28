package eecs2311.group2.wh40k_easycombat.controller;

import eecs2311.group2.wh40k_easycombat.controller.helper.ArmyControllerDataHelper;
import eecs2311.group2.wh40k_easycombat.controller.helper.DialogHelper;
import eecs2311.group2.wh40k_easycombat.service.game.GameArmyImportService;
import eecs2311.group2.wh40k_easycombat.viewmodel.ArmySavedRowVM;
import eecs2311.group2.wh40k_easycombat.viewmodel.GameArmyImportVM;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.input.MouseEvent;
import javafx.stage.Stage;

import java.util.function.Consumer;

public class ArmyImportController {

    @FXML private TableView<ArmySavedRowVM> armyTable;
    @FXML private TableColumn<ArmySavedRowVM, String> armyName;
    @FXML private TableColumn<ArmySavedRowVM, Number> armyPoints;
    @FXML private Button importButton;

    private GameUIController parentController;
    private GameUIController.ArmySide targetSide;
    private Consumer<GameArmyImportVM> importConsumer;
    private int maxAllowedPoints;

    @FXML
    private void initialize() {
        setupTable();
        loadSavedArmies();
    }

    public void setImportContext(GameUIController parentController, GameUIController.ArmySide targetSide) {
        this.parentController = parentController;
        this.targetSide = targetSide;
        this.importConsumer = null;
        this.maxAllowedPoints = 0;
    }

    public void setImportContext(Consumer<GameArmyImportVM> importConsumer, int maxAllowedPoints) {
        this.parentController = null;
        this.targetSide = null;
        this.importConsumer = importConsumer;
        this.maxAllowedPoints = Math.max(0, maxAllowedPoints);
    }

    private void setupTable() {
        armyName.setCellValueFactory(data ->
                new SimpleStringProperty(data.getValue().displayName())
        );

        armyPoints.setCellValueFactory(data ->
                new SimpleIntegerProperty(data.getValue().points())
        );
    }

    private void loadSavedArmies() {
        try {
            armyTable.setItems(FXCollections.observableArrayList(
                    ArmyControllerDataHelper.loadSavedArmyRows()
            ));
        } catch (Exception e) {
            DialogHelper.showError("Load Saved Armies Error", e);
        }
    }

    @FXML
    void importArmy(MouseEvent event) {
        ArmySavedRowVM selected = armyTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            DialogHelper.showWarning("No Army Selected", "Please select one army to import.");
            return;
        }

        if (parentController == null || targetSide == null) {
            if (importConsumer == null) {
                DialogHelper.showWarning("Import Error", "Import target is not set.");
                return;
            }
        }

        if (maxAllowedPoints > 0 && selected.points() > maxAllowedPoints) {
            DialogHelper.showWarning(
                    "Army Too Large",
                    "This army has " + selected.points() + " points, which is above the selected battle size limit of "
                            + maxAllowedPoints + "."
            );
            return;
        }

        try {
            GameArmyImportVM data = GameArmyImportService.importArmy(selected.armyId());
            if (data == null) {
                DialogHelper.showWarning("Import Failed", "The selected army could not be imported.");
                return;
            }

            if (importConsumer != null) {
                importConsumer.accept(data);
            } else {
                parentController.acceptImportedArmy(targetSide, data);
            }

            Stage stage = (Stage) importButton.getScene().getWindow();
            stage.close();
        } catch (Exception e) {
            DialogHelper.showError("Import Error", e);
        }
    }
}
