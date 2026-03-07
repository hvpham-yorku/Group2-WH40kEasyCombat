package eecs2311.group2.wh40k_easycombat.controller;

import java.io.IOException;
import java.util.Optional;

import eecs2311.group2.wh40k_easycombat.cell.GameArmyUnitCell;
import eecs2311.group2.wh40k_easycombat.cell.GameStrategyCell;
import eecs2311.group2.wh40k_easycombat.manager.RoundManager;
import eecs2311.group2.wh40k_easycombat.manager.StratagemUseManager;
import eecs2311.group2.wh40k_easycombat.service.GameArmyImportService.ImportedArmyData;
import eecs2311.group2.wh40k_easycombat.util.FixedAspectView;
import eecs2311.group2.wh40k_easycombat.manager.GameStateManager;
import eecs2311.group2.wh40k_easycombat.viewmodel.GameArmyUnitVM;
import eecs2311.group2.wh40k_easycombat.viewmodel.GameStrategyVM;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import javafx.stage.Modality;
import javafx.stage.Stage;

public class GameUIController {

    public enum ArmySide {
        BLUE, RED
    }

    // ======================= CheckBoxes =======================
    @FXML private CheckBox autoBattleCheckBox;

    // ======================= Blue Side ========================
    @FXML private Button blueAbandonMissionButton;
    @FXML private ListView<GameArmyUnitVM> blueArmyList;
    @FXML private Label blueCPLabel;
    @FXML private Button blueCheckMissionButton;
    @FXML private Label blueFactionLabel;
    @FXML private Button blueImportButton;
    @FXML private TableView<?> blueMissionTable;
    @FXML private Label bluePhaseLabel;
    @FXML private Button bluePlusButton;
    @FXML private Button blueSelectButton;
    @FXML private ListView<GameStrategyVM> blueStrategyList;
    @FXML private Button blueSubButton;
    @FXML private Label blueVPLabel;

    // ======================= Red Side =========================
    @FXML private Button redAbandonMissionButton;
    @FXML private ListView<GameArmyUnitVM> redArmyList;
    @FXML private Label redCPLabel;
    @FXML private Button redCheckMissionButton;
    @FXML private Label redFactionLabel;
    @FXML private Button redImportButton;
    @FXML private TableView<?> redMissionTable;
    @FXML private Label redPhaseLabel;
    @FXML private Button redPlusButton;
    @FXML private Button redSelectButton;
    @FXML private ListView<GameStrategyVM> redStrategyList;
    @FXML private Button redSubButton;
    @FXML private Label redVPLabel;

    // ======================= Game Control =====================
    @FXML private Button battleLogButton;
    @FXML private Button exitGameButton;
    @FXML private Button nextRoundButton;
    @FXML private Button rollButton;

    // ======================= Game Info ========================
    @FXML private Label missionNameLabel;
    @FXML private Label roundLabel;

    // ======================= Dice =============================
    @FXML private TextArea virtuaDiceBox;
    @FXML private Spinner<?> virtuaDiceSpinner;

    private final ObservableList<GameArmyUnitVM> blueArmyUnits = FXCollections.observableArrayList();
    private final ObservableList<GameArmyUnitVM> redArmyUnits = FXCollections.observableArrayList();

    private final ObservableList<GameStrategyVM> blueStrategies = FXCollections.observableArrayList();
    private final ObservableList<GameStrategyVM> redStrategies = FXCollections.observableArrayList();

    @FXML
    private void initialize() {
        setupArmyLists();
        setupStrategyLists();
        RoundManager.initialize(roundLabel, blueCPLabel, redCPLabel);
    }

    private void setupArmyLists() {
        blueArmyList.setItems(blueArmyUnits);
        redArmyList.setItems(redArmyUnits);

        blueArmyList.setCellFactory(v -> new GameArmyUnitCell());
        redArmyList.setCellFactory(v -> new GameArmyUnitCell());
    }

    private void setupStrategyLists() {
        blueStrategyList.setItems(blueStrategies);
        redStrategyList.setItems(redStrategies);

        blueStrategyList.setCellFactory(v -> new GameStrategyCell());
        redStrategyList.setCellFactory(v -> new GameStrategyCell());
    }

    public void acceptImportedArmy(ArmySide side, ImportedArmyData data) {
        if (data == null) return;

        try {
            GameStateManager.applyImportedArmy(
                    side,
                    data,
                    blueArmyUnits,
                    redArmyUnits,
                    blueStrategies,
                    redStrategies
            );

            if (side == ArmySide.BLUE) {
                blueFactionLabel.setText(data.factionName());
            } else {
                redFactionLabel.setText(data.factionName());
            }
        } catch (Exception e) {
            Alert a = new Alert(Alert.AlertType.ERROR, e.getMessage(), ButtonType.OK);
            a.setHeaderText("Import Strategy Error");
            a.showAndWait();
        }
    }

    private void openImportWindow(ArmySide side, Button sourceButton) {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/eecs2311/group2/wh40k_easycombat/ArmyImport.fxml")
            );
            Parent root = loader.load();

            ArmyImportController controller = loader.getController();
            controller.setImportContext(this, side);

            Stage stage = new Stage();
            stage.initOwner(sourceButton.getScene().getWindow());
            stage.initModality(Modality.WINDOW_MODAL);
            stage.setTitle("Import Army");
            stage.setScene(new javafx.scene.Scene(root));
            stage.showAndWait();
        } catch (Exception e) {
            Alert a = new Alert(Alert.AlertType.ERROR, e.getMessage(), ButtonType.OK);
            a.setHeaderText("Open Import Page Error");
            a.showAndWait();
        }
    }

    // ======================= Blue Actions =====================
    @FXML
    void blueAbandonClicked(MouseEvent event) {

    }

    @FXML
    void blueCheckClicked(MouseEvent event) {

    }

    @FXML
    void blueClickPlus(MouseEvent event) {
        RoundManager.addBlueCp(blueCPLabel, 1);
    }

    @FXML
    void blueClickSub(MouseEvent event) {
        RoundManager.addBlueCp(blueCPLabel, -1);
    }

    @FXML
    void blueImport(MouseEvent event) {
        openImportWindow(ArmySide.BLUE, blueImportButton);
    }

    @FXML
    void blueSelect(MouseEvent event) {
        StratagemUseManager.useSelectedStrategy(
                ArmySide.BLUE,
                blueStrategyList,
                redStrategyList,
                blueCPLabel,
                redCPLabel
        );
    }

    // ======================= Red Actions ======================
    @FXML
    void redAbandonClicked(MouseEvent event) {

    }

    @FXML
    void redCheckClicked(MouseEvent event) {

    }

    @FXML
    void redClickPlus(MouseEvent event) {
        RoundManager.addRedCp(redCPLabel, 1);
    }

    @FXML
    void redClickSub(MouseEvent event) {
        RoundManager.addRedCp(redCPLabel, -1);
    }

    @FXML
    void redImport(MouseEvent event) {
        openImportWindow(ArmySide.RED, redImportButton);
    }

    @FXML
    void redSelect(MouseEvent event) {
        StratagemUseManager.useSelectedStrategy(
                ArmySide.RED,
                blueStrategyList,
                redStrategyList,
                blueCPLabel,
                redCPLabel
        );
    }

    // ======================= General Actions ==================
    @FXML
    void clickExit(MouseEvent event) throws IOException {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Exit");
        alert.setHeaderText("Are you sure you want to exit this game?");
        alert.setContentText("Unsaved changes will be lost.");

        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            FixedAspectView.switchTo((Node) event.getSource(),
                    "/eecs2311/group2/wh40k_easycombat/MainUI.fxml",
                    1200.0, 800.0);
        }
    }

    @FXML
    void nextRound(MouseEvent event) {
        Alert alert = new Alert(
                Alert.AlertType.CONFIRMATION,
                "Are you sure you want to enter the next round?",
                ButtonType.YES,
                ButtonType.NO
        );
        alert.setHeaderText("Next Round");

        if (alert.showAndWait().orElse(ButtonType.NO) != ButtonType.YES) {
            return;
        }

        RoundManager.nextRound(roundLabel, blueCPLabel, redCPLabel);
    }

    @FXML
    void openLog(MouseEvent event) {

    }

    @FXML
    void rollDice(MouseEvent event) {

    }
}