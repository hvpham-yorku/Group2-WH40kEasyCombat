package eecs2311.group2.wh40k_easycombat.controller;

import java.io.IOException;

import eecs2311.group2.wh40k_easycombat.cell.GameArmyUnitCell;
import eecs2311.group2.wh40k_easycombat.cell.GameStrategyCell;
import eecs2311.group2.wh40k_easycombat.controller.helper.DialogHelper;
import eecs2311.group2.wh40k_easycombat.manager.RoundManager;
import eecs2311.group2.wh40k_easycombat.manager.StratagemUseManager;
import eecs2311.group2.wh40k_easycombat.util.FixedAspectView;
import eecs2311.group2.wh40k_easycombat.viewmodel.GameArmyImportVM;
import eecs2311.group2.wh40k_easycombat.viewmodel.GameArmyUnitVM;
import eecs2311.group2.wh40k_easycombat.viewmodel.GameStrategyVM;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.Spinner;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
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
        applyRoundState(RoundManager.initialize(
                roundLabel == null ? null : roundLabel.getText(),
                blueCPLabel == null ? null : blueCPLabel.getText(),
                redCPLabel == null ? null : redCPLabel.getText()
        ));
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

    public void acceptImportedArmy(ArmySide side, GameArmyImportVM data) {
        if (data == null) return;

        if (side == ArmySide.BLUE) {
            blueArmyUnits.setAll(data.units());
            blueStrategies.setAll(data.strategies());
            blueFactionLabel.setText(data.factionName());
        } else {
            redArmyUnits.setAll(data.units());
            redStrategies.setAll(data.strategies());
            redFactionLabel.setText(data.factionName());
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
            DialogHelper.showError("Open Import Page Error", e);
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
        applyRoundState(RoundManager.addBlueCp(readRoundState(), 1));
    }

    @FXML
    void blueClickSub(MouseEvent event) {
        applyRoundState(RoundManager.addBlueCp(readRoundState(), -1));
    }

    @FXML
    void blueImport(MouseEvent event) {
        openImportWindow(ArmySide.BLUE, blueImportButton);
    }

    @FXML
    void blueSelect(MouseEvent event) {
        useSelectedStrategy(ArmySide.BLUE);
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
        applyRoundState(RoundManager.addRedCp(readRoundState(), 1));
    }

    @FXML
    void redClickSub(MouseEvent event) {
        applyRoundState(RoundManager.addRedCp(readRoundState(), -1));
    }

    @FXML
    void redImport(MouseEvent event) {
        openImportWindow(ArmySide.RED, redImportButton);
    }

    @FXML
    void redSelect(MouseEvent event) {
        useSelectedStrategy(ArmySide.RED);
    }

    // ======================= General Actions ==================
    @FXML
    void clickExit(MouseEvent event) throws IOException {
        boolean shouldExit = DialogHelper.confirmOkCancel(
                "Exit",
                "Are you sure you want to exit this game?",
                "Unsaved changes will be lost."
        );

        if (shouldExit) {
        	FixedAspectView.switchResponsiveTo(
        	        (Node) event.getSource(),
        	        "/eecs2311/group2/wh40k_easycombat/MainUI.fxml",
        	        800.0,
        	        600.0,
        	        1200.0,
        	        800.0
        	);
        }
    }

    @FXML
    void nextRound(MouseEvent event) {
        if (!DialogHelper.confirmYesNo("Next Round", "Are you sure you want to enter the next round?")) {
            return;
        }

        applyRoundState(RoundManager.nextRound(readRoundState()));
    }

    @FXML
    void openLog(MouseEvent event) {

    }

    @FXML
    void rollDice(MouseEvent event) {

    }

    private void useSelectedStrategy(ArmySide side) {
        GameStrategyVM selected = getSelectedStrategy(side);

        StratagemUseManager.UseResult result = StratagemUseManager.useStrategy(
                toBattleSide(side),
                selected == null ? null : selected.getStrategy(),
                getCpLabel(side).getText()
        );

        if (!result.success()) {
            DialogHelper.showWarning(result.title(), result.message());
            return;
        }

        if (!DialogHelper.confirmYesNo("Confirm Stratagem", "Use stratagem \"" + result.title() + "\"?")) {
            return;
        }

        getCpLabel(side).setText(result.nextCpText());
        DialogHelper.showInfo(result.title(), result.message());
    }

    private GameStrategyVM getSelectedStrategy(ArmySide side) {
        if (side == ArmySide.BLUE) {
            return blueStrategyList.getSelectionModel().getSelectedItem();
        }
        return redStrategyList.getSelectionModel().getSelectedItem();
    }

    private Label getCpLabel(ArmySide side) {
        return side == ArmySide.BLUE ? blueCPLabel : redCPLabel;
    }

    private StratagemUseManager.BattleSide toBattleSide(ArmySide side) {
        return side == ArmySide.BLUE
                ? StratagemUseManager.BattleSide.BLUE
                : StratagemUseManager.BattleSide.RED;
    }

    private RoundManager.RoundState readRoundState() {
        return RoundManager.fromTexts(
                roundLabel == null ? null : roundLabel.getText(),
                blueCPLabel == null ? null : blueCPLabel.getText(),
                redCPLabel == null ? null : redCPLabel.getText()
        );
    }

    private void applyRoundState(RoundManager.RoundState state) {
        if (state == null) {
            return;
        }

        if (roundLabel != null) {
            roundLabel.setText(String.valueOf(state.round()));
        }
        if (blueCPLabel != null) {
            blueCPLabel.setText(String.valueOf(state.blueCp()));
        }
        if (redCPLabel != null) {
            redCPLabel.setText(String.valueOf(state.redCp()));
        }
    }
}
