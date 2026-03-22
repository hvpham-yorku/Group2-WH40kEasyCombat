package eecs2311.group2.wh40k_easycombat.controller;

import eecs2311.group2.wh40k_easycombat.cell.GameArmyUnitCell;
import eecs2311.group2.wh40k_easycombat.cell.GameStrategyCell;
import eecs2311.group2.wh40k_easycombat.controller.helper.DialogHelper;
import eecs2311.group2.wh40k_easycombat.manager.RoundManager;
import eecs2311.group2.wh40k_easycombat.manager.StratagemUseManager;
import eecs2311.group2.wh40k_easycombat.model.combat.PhaseAdvanceResult;
import eecs2311.group2.wh40k_easycombat.model.instance.Phase;
import eecs2311.group2.wh40k_easycombat.model.instance.Player;
import eecs2311.group2.wh40k_easycombat.model.instance.UnitInstance;
import eecs2311.group2.wh40k_easycombat.service.autobattle.AutoBattleMode;
import eecs2311.group2.wh40k_easycombat.service.calculations.DiceService;
import eecs2311.group2.wh40k_easycombat.service.game.ArmyListStateService;
import eecs2311.group2.wh40k_easycombat.service.game.BattleShockService;
import eecs2311.group2.wh40k_easycombat.service.game.GameTurnService;
import eecs2311.group2.wh40k_easycombat.util.FixedAspectView;
import eecs2311.group2.wh40k_easycombat.viewmodel.GameArmyImportVM;
import eecs2311.group2.wh40k_easycombat.viewmodel.GameArmyUnitVM;
import eecs2311.group2.wh40k_easycombat.viewmodel.GameStrategyVM;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.input.MouseEvent;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

public class GameUIController {

    public enum ArmySide {
        BLUE, RED
    }

    @FXML private CheckBox autoBattleCheckBox;

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

    @FXML private Button battleLogButton;
    @FXML private Button exitGameButton;
    @FXML private Button nextPhaseButton;
    @FXML private Button nextRoundButton;
    @FXML private Button rollButton;

    @FXML private Label missionNameLabel;
    @FXML private Label roundLabel;

    @FXML private TextArea virtuaDiceBox;
    @FXML private Spinner<Integer> virtuaDiceSpinner;

    private final ObservableList<GameArmyUnitVM> blueArmyUnits = FXCollections.observableArrayList();
    private final ObservableList<GameArmyUnitVM> redArmyUnits = FXCollections.observableArrayList();
    private final ObservableList<GameStrategyVM> blueStrategies = FXCollections.observableArrayList();
    private final ObservableList<GameStrategyVM> redStrategies = FXCollections.observableArrayList();

    private final GameTurnService turnService = new GameTurnService();
    private final DiceService manualDiceService = new DiceService();
    private final BattleShockService battleShockService = new BattleShockService();

    @FXML
    private void initialize() {
        setupArmyLists();
        setupStrategyLists();
        setupManualDice();
        initializePhaseState();

        if (nextRoundButton != null) {
            nextRoundButton.setManaged(false);
            nextRoundButton.setVisible(false);
        }
    }

    public void acceptImportedArmy(ArmySide side, GameArmyImportVM data) {
        if (data == null) {
            return;
        }

        ArmyListStateService.initializeDisplayOrder(data.units());

        if (side == ArmySide.BLUE) {
            blueArmyUnits.setAll(data.units());
            ArmyListStateService.refreshArmyOrdering(blueArmyUnits);
            blueStrategies.setAll(data.strategies());
            blueFactionLabel.setText(data.factionName());
            blueArmyList.refresh();
            return;
        }

        redArmyUnits.setAll(data.units());
        ArmyListStateService.refreshArmyOrdering(redArmyUnits);
        redStrategies.setAll(data.strategies());
        redFactionLabel.setText(data.factionName());
        redArmyList.refresh();
    }

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
        nextPhase(new ActionEvent(nextPhaseButton, nextPhaseButton));
    }

    @FXML
    void openLog(MouseEvent event) {
    }

    @FXML
    void rollDice(MouseEvent event) {
        int diceCount = virtuaDiceSpinner == null || virtuaDiceSpinner.getValue() == null
                ? 1
                : Math.max(1, virtuaDiceSpinner.getValue());

        manualDiceService.rollDice(diceCount);

        List<Integer> results = manualDiceService.getResults();
        int total = results.stream().mapToInt(Integer::intValue).sum();

        StringBuilder sb = new StringBuilder();
        sb.append("Rolled ").append(diceCount).append("D6");
        sb.append(" -> ");
        sb.append(results);
        sb.append(" | Total: ").append(total);
        sb.append("\n");

        if (virtuaDiceBox != null) {
            virtuaDiceBox.appendText(sb.toString());
        }
    }

    @FXML
    private void nextPhase(ActionEvent event) {
        if (!DialogHelper.confirmYesNo("Next Phase", "Advance to the next phase?")) {
            return;
        }

        PhaseAdvanceResult result = turnService.advancePhase(blueUnitInstances(), redUnitInstances());
        if (result.awardedCommandPoint()) {
            addCommandPoint(result.commandPointRecipient());
        }

        syncTurnUi();
        blueArmyList.refresh();
        redArmyList.refresh();
        maybeOpenBattleShockWindow();
    }

    @FXML
    private void openAutoBattle(ActionEvent event) {
        autoBattleCheckBox.setSelected(false);

        if (blueArmyUnits.isEmpty() || redArmyUnits.isEmpty()) {
            DialogHelper.showWarning(
                    "Auto Battle Unavailable",
                    "Please import both Blue and Red armies before opening Auto Battle."
            );
            return;
        }

        AutoBattleMode mode = turnService.currentAutoBattleMode();
        if (mode == null) {
            DialogHelper.showWarning(
                    "Wrong Phase",
                    "Auto Battle is available during Movement, Shooting, Charge and Fight phases."
            );
            return;
        }

        openAutoBattleWindow(mode);
    }

    private void setupArmyLists() {
        blueArmyList.setItems(blueArmyUnits);
        redArmyList.setItems(redArmyUnits);

        blueArmyList.setCellFactory(v -> new GameArmyUnitCell(() -> handleArmyStateChanged(ArmySide.BLUE)));
        redArmyList.setCellFactory(v -> new GameArmyUnitCell(() -> handleArmyStateChanged(ArmySide.RED)));
    }

    private void setupStrategyLists() {
        blueStrategyList.setItems(blueStrategies);
        redStrategyList.setItems(redStrategies);

        blueStrategyList.setCellFactory(v -> new GameStrategyCell());
        redStrategyList.setCellFactory(v -> new GameStrategyCell());
    }

    private void setupManualDice() {
        if (virtuaDiceSpinner != null) {
            virtuaDiceSpinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 40, 1));
        }

        if (virtuaDiceBox != null) {
            virtuaDiceBox.setEditable(false);
            virtuaDiceBox.setText("Manual dice log ready.\n");
        }
    }

    private void initializePhaseState() {
        turnService.reset();

        if (blueCPLabel != null) {
            blueCPLabel.setText("1");
        }
        if (redCPLabel != null) {
            redCPLabel.setText("1");
        }

        addCommandPoint(turnService.getActivePlayer());
        syncTurnUi();
    }

    private void syncTurnUi() {
        if (roundLabel != null) {
            roundLabel.setText(String.valueOf(turnService.getCurrentRound()));
        }

        if (bluePhaseLabel != null) {
            bluePhaseLabel.setText(turnService.phaseLabelFor(Player.ATTACKER));
        }

        if (redPhaseLabel != null) {
            redPhaseLabel.setText(turnService.phaseLabelFor(Player.DEFENDER));
        }
    }

    private void handleArmyStateChanged(ArmySide side) {
        ObservableList<GameArmyUnitVM> units = side == ArmySide.BLUE ? blueArmyUnits : redArmyUnits;
        ListView<GameArmyUnitVM> listView = side == ArmySide.BLUE ? blueArmyList : redArmyList;
        GameArmyUnitVM selected = listView.getSelectionModel().getSelectedItem();

        ArmyListStateService.refreshArmyOrdering(units);
        if (selected != null) {
            listView.getSelectionModel().select(selected);
        }
        listView.refresh();
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
            stage.setScene(new Scene(root));
            stage.showAndWait();
        } catch (Exception e) {
            DialogHelper.showError("Open Import Page Error", e);
        }
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
        return side == ArmySide.BLUE
                ? blueStrategyList.getSelectionModel().getSelectedItem()
                : redStrategyList.getSelectionModel().getSelectedItem();
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

        if (blueCPLabel != null) {
            blueCPLabel.setText(String.valueOf(state.blueCp()));
        }
        if (redCPLabel != null) {
            redCPLabel.setText(String.valueOf(state.redCp()));
        }
    }

    private void addCommandPoint(Player player) {
        if (player == Player.ATTACKER) {
            if (blueCPLabel != null) {
                blueCPLabel.setText(String.valueOf(parseInt(blueCPLabel.getText()) + 1));
            }
            return;
        }

        if (redCPLabel != null) {
            redCPLabel.setText(String.valueOf(parseInt(redCPLabel.getText()) + 1));
        }
    }

    private int parseInt(String text) {
        try {
            return Integer.parseInt(text == null ? "" : text.trim());
        } catch (Exception ignored) {
            return 0;
        }
    }

    private List<UnitInstance> blueUnitInstances() {
        return blueArmyUnits.stream()
                .map(GameArmyUnitVM::getUnit)
                .collect(Collectors.toList());
    }

    private List<UnitInstance> redUnitInstances() {
        return redArmyUnits.stream()
                .map(GameArmyUnitVM::getUnit)
                .collect(Collectors.toList());
    }

    private void maybeOpenBattleShockWindow() {
        if (turnService.getCurrentPhase() != Phase.COMMAND) {
            return;
        }

        List<UnitInstance> activeUnits = turnService.getActivePlayer() == Player.ATTACKER
                ? blueUnitInstances()
                : redUnitInstances();

        List<UnitInstance> candidates = battleShockService.battleShockCandidates(
                activeUnits,
                turnService.getCurrentRound()
        );

        if (candidates.isEmpty()) {
            return;
        }

        openBattleShockWindow(candidates);
    }

    private void openBattleShockWindow(List<UnitInstance> candidates) {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/eecs2311/group2/wh40k_easycombat/BattleShock.fxml")
            );
            Parent root = loader.load();

            BattleShockController controller = loader.getController();
            String factionName = turnService.getActivePlayer() == Player.ATTACKER
                    ? blueFactionLabel.getText()
                    : redFactionLabel.getText();
            controller.setContext(
                    factionName,
                    turnService.getCurrentRound(),
                    candidates,
                    this::refreshArmyViews
            );

            Stage stage = new Stage();
            stage.initOwner(nextPhaseButton.getScene().getWindow());
            stage.initModality(Modality.WINDOW_MODAL);
            stage.setTitle("Battle-shock Step");
            stage.setScene(new Scene(root));
            stage.showAndWait();

            refreshArmyViews();
        } catch (Exception e) {
            DialogHelper.showError("Open Battle-shock Step Error", e);
        }
    }

    private void refreshArmyViews() {
        ArmyListStateService.refreshArmyOrdering(blueArmyUnits);
        ArmyListStateService.refreshArmyOrdering(redArmyUnits);
        blueArmyList.refresh();
        redArmyList.refresh();
    }

    private void openAutoBattleWindow(AutoBattleMode mode) {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/eecs2311/group2/wh40k_easycombat/AutoBattle.fxml")
            );
            Parent root = loader.load();

            AutoBattleController controller = loader.getController();
            controller.setBattleContext(
                    mode,
                    turnService.getCurrentPhase(),
                    turnService.getActivePlayer(),
                    blueFactionLabel == null ? "Blue Army" : blueFactionLabel.getText(),
                    blueArmyUnits,
                    redFactionLabel == null ? "Red Army" : redFactionLabel.getText(),
                    redArmyUnits
            );

            Stage stage = new Stage();
            stage.initOwner(autoBattleCheckBox.getScene().getWindow());
            stage.initModality(Modality.WINDOW_MODAL);
            stage.setTitle("Auto Battle");
            stage.setScene(new Scene(root));
            stage.showAndWait();

            refreshArmyViews();
            syncTurnUi();
        } catch (Exception e) {
            DialogHelper.showError("Open Auto Battle Error", e);
        }
    }
}
