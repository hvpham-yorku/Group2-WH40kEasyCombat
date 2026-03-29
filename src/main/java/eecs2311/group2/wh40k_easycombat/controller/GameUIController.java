package eecs2311.group2.wh40k_easycombat.controller;

import eecs2311.group2.wh40k_easycombat.cell.GameArmyUnitCell;
import eecs2311.group2.wh40k_easycombat.cell.GameStrategyCell;
import eecs2311.group2.wh40k_easycombat.controller.helper.DialogHelper;
import eecs2311.group2.wh40k_easycombat.manager.StratagemUseManager;
import eecs2311.group2.wh40k_easycombat.model.combat.PhaseAdvanceResult;
import eecs2311.group2.wh40k_easycombat.model.editor.EditorRuleDefinition;
import eecs2311.group2.wh40k_easycombat.model.instance.GameSetupConfig;
import eecs2311.group2.wh40k_easycombat.model.instance.Phase;
import eecs2311.group2.wh40k_easycombat.model.instance.Player;
import eecs2311.group2.wh40k_easycombat.model.instance.UnitInstance;
import eecs2311.group2.wh40k_easycombat.model.mission.MissionCard;
import eecs2311.group2.wh40k_easycombat.model.mission.MissionResolution;
import eecs2311.group2.wh40k_easycombat.model.mission.SecondaryMissionMode;
import eecs2311.group2.wh40k_easycombat.service.BattleLogService;
import eecs2311.group2.wh40k_easycombat.service.autobattle.AutoBattleMode;
import eecs2311.group2.wh40k_easycombat.service.calculations.DiceService;
import eecs2311.group2.wh40k_easycombat.service.editor.EditorEffectRuntimeService;
import eecs2311.group2.wh40k_easycombat.service.game.ArmyListStateService;
import eecs2311.group2.wh40k_easycombat.service.game.BattleShockService;
import eecs2311.group2.wh40k_easycombat.service.game.GameEngine;
import eecs2311.group2.wh40k_easycombat.service.game.GameSetupService;
import eecs2311.group2.wh40k_easycombat.service.mission.MissionService;
import eecs2311.group2.wh40k_easycombat.service.mission.MissionSessionService;
import eecs2311.group2.wh40k_easycombat.util.FixedAspectView;
import eecs2311.group2.wh40k_easycombat.viewmodel.GameArmyImportVM;
import eecs2311.group2.wh40k_easycombat.viewmodel.GameArmyUnitVM;
import eecs2311.group2.wh40k_easycombat.viewmodel.GameStrategyVM;
import eecs2311.group2.wh40k_easycombat.viewmodel.MissionEntryVM;
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
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.input.MouseEvent;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

public class GameUIController {

    public enum ArmySide {
        BLUE, RED
    }

    // ======================= Shared Controls =======================
    @FXML private CheckBox autoBattleCheckBox;

    // ======================= Attacker Side =======================
    @FXML private Button blueAbandonMissionButton;
    @FXML private ListView<GameArmyUnitVM> blueArmyList;
    @FXML private Label blueCPLabel;
    @FXML private Button blueCheckMissionButton;
    @FXML private Button blueDrawMissionButton;
    @FXML private Label blueFactionLabel;
    @FXML private TableView<MissionEntryVM> blueMissionTable;
    @FXML private TableColumn<MissionEntryVM, String> blueMissionName;
    @FXML private TableColumn<MissionEntryVM, String> blueMissionState;
    @FXML private TableColumn<MissionEntryVM, String> blueMissionMode;
    @FXML private Label bluePhaseLabel;
    @FXML private Button bluePlusButton;
    @FXML private Button blueSelectButton;
    @FXML private ListView<GameStrategyVM> blueStrategyList;
    @FXML private Button blueSubButton;
    @FXML private Label blueVPLabel;

    // ======================= Defender Side =======================
    @FXML private Button redAbandonMissionButton;
    @FXML private ListView<GameArmyUnitVM> redArmyList;
    @FXML private Label redCPLabel;
    @FXML private Button redCheckMissionButton;
    @FXML private Button redDrawMissionButton;
    @FXML private Label redFactionLabel;
    @FXML private TableView<MissionEntryVM> redMissionTable;
    @FXML private TableColumn<MissionEntryVM, String> redMissionName;
    @FXML private TableColumn<MissionEntryVM, String> redState;
    @FXML private TableColumn<MissionEntryVM, String> redMissionMode;
    @FXML private Label redPhaseLabel;
    @FXML private Button redPlusButton;
    @FXML private Button redSelectButton;
    @FXML private ListView<GameStrategyVM> redStrategyList;
    @FXML private Button redSubButton;
    @FXML private Label redVPLabel;

    // ======================= Center Controls =======================
    @FXML private Button battleLogButton;
    @FXML private Button exitGameButton;
    @FXML private Button nextPhaseButton;
    @FXML private Button rollButton;

    @FXML private Label missionNameLabel;
    @FXML private Label primaryMissionStateLabel;
    @FXML private Label roundLabel;
    @FXML private Label winnerLabel;

    // ======================= Manual Dice =======================
    @FXML private TextArea virtuaDiceBox;
    @FXML private ComboBox<Integer> virtualDiceSuccessComboBox;
    @FXML private Spinner<Integer> virtuaDiceSpinner;

    private final ObservableList<GameArmyUnitVM> blueArmyUnits = FXCollections.observableArrayList();
    private final ObservableList<GameArmyUnitVM> redArmyUnits = FXCollections.observableArrayList();
    private final ObservableList<GameStrategyVM> blueStrategies = FXCollections.observableArrayList();
    private final ObservableList<GameStrategyVM> redStrategies = FXCollections.observableArrayList();
    private final ObservableList<MissionEntryVM> blueMissionEntries = FXCollections.observableArrayList();
    private final ObservableList<MissionEntryVM> redMissionEntries = FXCollections.observableArrayList();

    private final GameEngine gameEngine = new GameEngine();
    private final DiceService manualDiceService = new DiceService();
    private final BattleShockService battleShockService = new BattleShockService();
    private final EditorEffectRuntimeService editorEffectRuntimeService = EditorEffectRuntimeService.getInstance();
    private final MissionService missionService = MissionService.getInstance();
    private final MissionSessionService missionSessionService = MissionSessionService.getInstance();
    private final GameSetupService gameSetupService = GameSetupService.getInstance();
    private final BattleLogService battleLogService = BattleLogService.getInstance();

    private MissionEntryVM primaryMissionEntry;

    // When this page loads, initialize all lists, turn state, manual dice controls and setup data.
    @FXML
    private void initialize() {
        setupArmyLists();
        setupStrategyLists();
        setupMissionViews();
        setupManualDice();
        initializePhaseState();
        applySetupConfig();
    }

    public void acceptImportedArmy(ArmySide side, GameArmyImportVM data) {
        if (data == null) {
            return;
        }

        editorEffectRuntimeService.clearAll();
        ArmyListStateService.initializeDisplayOrder(data.units());
        gameEngine.replaceArmy(toPlayer(side), data);

        if (side == ArmySide.BLUE) {
            blueArmyUnits.setAll(data.units());
            ArmyListStateService.refreshArmyOrdering(blueArmyUnits);
            blueStrategies.setAll(data.strategies());
            blueFactionLabel.setText(data.factionName());
            syncScoreLabels();
            blueArmyList.refresh();
            return;
        }

        redArmyUnits.setAll(data.units());
        ArmyListStateService.refreshArmyOrdering(redArmyUnits);
        redStrategies.setAll(data.strategies());
        redFactionLabel.setText(data.factionName());
        syncScoreLabels();
        redArmyList.refresh();
    }

    // When click the attacker "Abandon Mission" button, abandon the selected attacker secondary mission.
    @FXML
    void blueAbandonClicked(MouseEvent event) {
        abandonSelectedMission(ArmySide.BLUE);
    }

    // When click the attacker "Check Mission" button, open the selected attacker mission card.
    @FXML
    void blueCheckClicked(MouseEvent event) {
        openSelectedSecondaryMission(ArmySide.BLUE);
    }

    // When click the attacker "Draw Mission" button, draw attacker tactical secondary missions.
    @FXML
    void blueDrawClicked(MouseEvent event) {
        drawSecondaryMissions(ArmySide.BLUE);
    }

    // When click the attacker "+" button, manually add one CP to the attacker.
    @FXML
    void blueClickPlus(MouseEvent event) {
        adjustManualCommandPoints(ArmySide.BLUE, 1);
    }

    // When click the attacker "-" button, manually subtract one CP from the attacker.
    @FXML
    void blueClickSub(MouseEvent event) {
        adjustManualCommandPoints(ArmySide.BLUE, -1);
    }

    // When click the attacker "Use Stratagem" button, use the selected attacker stratagem.
    @FXML
    void blueSelect(MouseEvent event) {
        useSelectedStrategy(ArmySide.BLUE);
    }

    // When click the defender "Abandon Mission" button, abandon the selected defender secondary mission.
    @FXML
    void redAbandonClicked(MouseEvent event) {
        abandonSelectedMission(ArmySide.RED);
    }

    // When click the defender "Check Mission" button, open the selected defender mission card.
    @FXML
    void redCheckClicked(MouseEvent event) {
        openSelectedSecondaryMission(ArmySide.RED);
    }

    // When click the defender "Draw Mission" button, draw defender tactical secondary missions.
    @FXML
    void redDrawClicked(MouseEvent event) {
        drawSecondaryMissions(ArmySide.RED);
    }

    // When click the defender "+" button, manually add one CP to the defender.
    @FXML
    void redClickPlus(MouseEvent event) {
        adjustManualCommandPoints(ArmySide.RED, 1);
    }

    // When click the defender "-" button, manually subtract one CP from the defender.
    @FXML
    void redClickSub(MouseEvent event) {
        adjustManualCommandPoints(ArmySide.RED, -1);
    }

    // When click the defender "Use Stratagem" button, use the selected defender stratagem.
    @FXML
    void redSelect(MouseEvent event) {
        useSelectedStrategy(ArmySide.RED);
    }

    // When click "Exit" button, confirm whether to leave the current battle.
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

    // When click "Battle Log" button, open the battle log window and show all recorded actions.
    @FXML
    void openLog(MouseEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/eecs2311/group2/wh40k_easycombat/BattleLog.fxml")
            );
            Parent root = loader.load();

            BattleLogController controller = loader.getController();
            controller.setContext("Battle Log");

            Stage stage = new Stage();
            stage.initOwner(battleLogButton.getScene().getWindow());
            stage.initModality(Modality.WINDOW_MODAL);
            stage.setTitle("Battle Log");
            stage.setScene(new Scene(root));
            stage.setMinWidth(900.0);
            stage.setMinHeight(620.0);
            stage.showAndWait();
        } catch (Exception e) {
            DialogHelper.showError("Open Battle Log Error", e);
        }
    }

    // When click "Roll" button, roll the selected number of D6 and count successes.
    @FXML
    void rollDice(MouseEvent event) {
        int diceCount = virtuaDiceSpinner == null || virtuaDiceSpinner.getValue() == null
                ? 1
                : Math.max(1, virtuaDiceSpinner.getValue());
        int successThreshold = virtualDiceSuccessComboBox == null || virtualDiceSuccessComboBox.getValue() == null
                ? 4
                : Math.max(1, virtualDiceSuccessComboBox.getValue());

        manualDiceService.rollDice(diceCount);

        List<Integer> results = manualDiceService.getResults();
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

        if (virtuaDiceBox != null) {
            virtuaDiceBox.appendText(sb.toString());
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

    // When click "Clear" button, clear the manual dice log text area.
    @FXML
    void clearDiceLog(MouseEvent event) {
        if (virtuaDiceBox != null) {
            virtuaDiceBox.clear();
            virtuaDiceBox.setText("Manual dice log ready.\n");
        }
    }

    // When click "Next Phase" button, advance the battle to the next phase and update turn state.
    @FXML
    private void nextPhase(ActionEvent event) {
        if (gameEngine.isBattleOver()) {
            DialogHelper.showInfo("Battle Over", winnerText());
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
                .append(phaseName(previousPhase))
                .append(" (")
                .append(playerLabel(previousActivePlayer))
                .append(" active)")
                .append(" to Round ")
                .append(gameEngine.getCurrentRound())
                .append(" ")
                .append(phaseName(gameEngine.getCurrentPhase()))
                .append(" (")
                .append(playerLabel(gameEngine.getActivePlayer()))
                .append(" active).");

        if (gameEngine.getCurrentRound() > gameEngine.getMaxRounds()) {
            battleLogService.logTurnEvent(previousRound, previousPhase, previousActivePlayer, phaseLog.toString());
            finishBattle();
            return;
        }

        if (result.awardedCommandPoint()) {
            int afterCp = currentCp(result.commandPointRecipient());
            int beforeCp = Math.max(0, afterCp - 1);
            missionSessionService.startTurn(result.commandPointRecipient());
            phaseLog.append(" ")
                    .append(playerLabel(result.commandPointRecipient()))
                    .append(" gained 1 CP (")
                    .append(beforeCp)
                    .append(" -> ")
                    .append(afterCp)
                    .append(").");
        }

        battleLogService.logTurnEvent(gameEngine.getCurrentRound(), gameEngine.getCurrentPhase(), gameEngine.getActivePlayer(), phaseLog.toString());
        syncTurnUi();
        blueArmyList.refresh();
        redArmyList.refresh();
        refreshMissionTablesFromSession();
        maybeOpenBattleShockWindow();
    }

    // When click "Auto Battle" check box, open the auto battle window for the current phase.
    @FXML
    private void openAutoBattle(ActionEvent event) {
        autoBattleCheckBox.setSelected(false);

        if (gameEngine.isBattleOver()) {
            DialogHelper.showInfo("Battle Over", winnerText());
            return;
        }

        if (blueArmyUnits.isEmpty() || redArmyUnits.isEmpty()) {
            DialogHelper.showWarning(
                    "Auto Battle Unavailable",
                    "Please import both the Attacker and Defender armies before opening Auto Battle."
            );
            return;
        }

        AutoBattleMode mode = gameEngine.currentAutoBattleMode();
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

    private void setupMissionViews() {
        blueMissionTable.setItems(blueMissionEntries);
        redMissionTable.setItems(redMissionEntries);

        blueMissionName.setCellValueFactory(cell -> cell.getValue().nameProperty());
        blueMissionState.setCellValueFactory(cell -> cell.getValue().stateProperty());
        blueMissionMode.setCellValueFactory(cell -> cell.getValue().modeProperty());
        redMissionName.setCellValueFactory(cell -> cell.getValue().nameProperty());
        redState.setCellValueFactory(cell -> cell.getValue().stateProperty());
        redMissionMode.setCellValueFactory(cell -> cell.getValue().modeProperty());
    }

    private void setupManualDice() {
        if (virtuaDiceSpinner != null) {
            virtuaDiceSpinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 40, 1));
        }
        if (virtualDiceSuccessComboBox != null) {
            virtualDiceSuccessComboBox.getItems().setAll(2, 3, 4, 5, 6);
            virtualDiceSuccessComboBox.getSelectionModel().select(Integer.valueOf(4));
        }

        if (virtuaDiceBox != null) {
            virtuaDiceBox.setEditable(false);
            virtuaDiceBox.setText("Manual dice log ready.\n");
        }
    }

    private void initializePhaseState() {
        gameEngine.start();
        editorEffectRuntimeService.clearAll();
        battleLogService.clear();
        syncScoreLabels();
        syncTurnUi();
        updateWinnerLabel();
    }

    private void syncTurnUi() {
        if (roundLabel != null) {
            roundLabel.setText(String.valueOf(gameEngine.getCurrentRound()));
        }

        if (bluePhaseLabel != null) {
            bluePhaseLabel.setText(gameEngine.phaseLabelFor(Player.ATTACKER));
        }

        if (redPhaseLabel != null) {
            redPhaseLabel.setText(gameEngine.phaseLabelFor(Player.DEFENDER));
        }

        syncScoreLabels();
        updateWinnerLabel();
        updateSecondaryMissionButtons();

        if (nextPhaseButton != null) {
            nextPhaseButton.setDisable(gameEngine.isBattleOver());
        }
        if (autoBattleCheckBox != null) {
            autoBattleCheckBox.setDisable(gameEngine.isBattleOver());
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

    private void useSelectedStrategy(ArmySide side) {
        if (gameEngine.isBattleOver()) {
            DialogHelper.showInfo("Battle Over", winnerText());
            return;
        }

        GameStrategyVM selected = getSelectedStrategy(side);
        int beforeCp = currentCp(toPlayer(side));
        StratagemUseManager.UseResult result = StratagemUseManager.useStrategy(
                toBattleSide(side),
                selected == null ? null : selected.getStrategy(),
                getCpLabel(side).getText()
        );

        if (!result.success()) {
            DialogHelper.showWarning(result.title(), result.message());
            return;
        }

        List<EditorRuleDefinition> matchingRules =
                editorEffectRuntimeService.matchingStratagemRules(selected == null ? null : selected.getStrategy());

        String confirmText = "Use stratagem \"" + result.title() + "\"?";
        if (!matchingRules.isEmpty()) {
            confirmText += "\n\nThis will also trigger "
                    + matchingRules.size()
                    + " custom rule"
                    + (matchingRules.size() == 1 ? "" : "s")
                    + " and prompt you to choose one affected unit.";
        }

        if (!DialogHelper.confirmYesNo("Confirm Stratagem", confirmText)) {
            return;
        }

        GameArmyUnitVM targetedUnit = null;
        if (!matchingRules.isEmpty()) {
            List<GameArmyUnitVM> candidates = unitsFor(side).stream()
                    .filter(vm -> vm != null && !vm.isDestroyed())
                    .collect(Collectors.toList());

            if (candidates.isEmpty()) {
                DialogHelper.showWarning(
                        "No Valid Unit",
                        "This stratagem has matching custom effects, but there is no living unit to receive them."
                );
                return;
            }

            targetedUnit = openStratagemTargetWindow(side, selected.getName(), matchingRules, candidates);
            if (targetedUnit == null) {
                return;
            }
        }

        gameEngine.setCommandPoints(toPlayer(side), parseInt(result.nextCpText()));
        syncScoreLabels();

        List<String> activatedLabels = List.of();
        if (targetedUnit != null && selected != null) {
            activatedLabels = editorEffectRuntimeService.activateStratagemRules(
                            selected.getStrategy(),
                            targetedUnit.getUnit(),
                            toPlayer(side),
                            gameEngine.getActivePlayer(),
                            gameEngine.getCurrentPhase(),
                            gameEngine.getCurrentRound()
                    ).stream()
                    .map(effect -> effect.displayName())
                    .collect(Collectors.toList());
        }

        StringBuilder info = new StringBuilder(result.message());
        if (!activatedLabels.isEmpty()) {
            info.append("\n\nAffected Unit: ").append(targetedUnit.getUnitName());
            info.append("\nActivated Effects:");
            for (String label : activatedLabels) {
                info.append("\n- ").append(label);
            }
        }

        DialogHelper.showInfo(result.title(), info.toString());
        int afterCp = parseInt(result.nextCpText());
        StringBuilder log = new StringBuilder();
        log.append(playerLabel(toPlayer(side)))
                .append(" used stratagem \"")
                .append(result.title())
                .append("\". CP ")
                .append(beforeCp)
                .append(" -> ")
                .append(afterCp)
                .append(".");
        if (targetedUnit != null) {
            log.append(" Affected unit: ").append(targetedUnit.getUnitName()).append(".");
        }
        if (!activatedLabels.isEmpty()) {
            log.append(" Activated effects: ").append(String.join(", ", activatedLabels)).append(".");
        }
        battleLogService.logTurnEvent(
                gameEngine.getCurrentRound(),
                gameEngine.getCurrentPhase(),
                toPlayer(side),
                log.toString()
        );
        refreshArmyViews();
    }

    private GameStrategyVM getSelectedStrategy(ArmySide side) {
        return side == ArmySide.BLUE
                ? blueStrategyList.getSelectionModel().getSelectedItem()
                : redStrategyList.getSelectionModel().getSelectedItem();
    }

    private void openSelectedSecondaryMission(ArmySide side) {
        if (gameEngine.isBattleOver()) {
            DialogHelper.showInfo("Battle Over", winnerText());
            return;
        }

        MissionEntryVM selected = getSelectedMission(side);
        if (selected == null || selected.getMissionCard() == null) {
            DialogHelper.showWarning("No Mission Selected", "Please select one secondary mission first.");
            return;
        }

        if ("Completed".equalsIgnoreCase(selected.getState())) {
            DialogHelper.showInfo("Mission Already Completed", "That tactical mission has already been completed.");
            return;
        }

        Player owningPlayer = toPlayer(side);
        MissionResolution resolution = openMissionCardWindow(
                sideLabel(side) + " Secondary Mission",
                selected.getMissionCard(),
                owningPlayer,
                false,
                missionSessionService.modeFor(owningPlayer) == SecondaryMissionMode.FIXED
                        ? "Keep Fixed Mission"
                        : "Keep Active"
        );

        if (resolution.decision().isClosed()) {
            return;
        }

        if (resolution.decision().isCompleted()) {
            addVictoryPoints(owningPlayer, resolution.vpAwarded());
            if (missionSessionService.modeFor(owningPlayer) == SecondaryMissionMode.TACTICAL) {
                missionSessionService.complete(owningPlayer, selected.getName());
            }
            battleLogService.logTurnEvent(
                    gameEngine.getCurrentRound(),
                    gameEngine.getCurrentPhase(),
                    owningPlayer,
                    playerLabel(owningPlayer)
                            + " completed "
                            + selected.getMode().toLowerCase(Locale.ROOT)
                            + " secondary mission \""
                            + selected.getName()
                            + "\" for "
                            + resolution.vpAwarded()
                            + " VP."
            );
        }

        refreshMissionTablesFromSession();
    }

    private void abandonSelectedMission(ArmySide side) {
        if (gameEngine.isBattleOver()) {
            DialogHelper.showInfo("Battle Over", winnerText());
            return;
        }

        if (toPlayer(side) != gameEngine.getActivePlayer()) {
            DialogHelper.showWarning(
                    "Wrong Turn",
                    "You can only abandon tactical missions during that army's current turn."
            );
            return;
        }

        MissionEntryVM selected = getSelectedMission(side);
        if (selected == null) {
            DialogHelper.showWarning("No Mission Selected", "Please select one secondary mission first.");
            return;
        }
        if ("Completed".equalsIgnoreCase(selected.getState())) {
            DialogHelper.showWarning("Cannot Abandon", "Completed missions are kept in the log and cannot be abandoned.");
            return;
        }
        if (!DialogHelper.confirmYesNo("Abandon Mission", "Mark \"" + selected.getName() + "\" as abandoned?")) {
            return;
        }

        if (!missionSessionService.abandon(toPlayer(side), selected.getName())) {
            DialogHelper.showWarning("Cannot Abandon", "That mission cannot be abandoned right now.");
            return;
        }

        boolean grantedCp = false;
        int beforeCp = currentCp(toPlayer(side));
        if (missionSessionService.grantAbandonCpIfAvailable(toPlayer(side))) {
            gameEngine.addCommandPoint(toPlayer(side));
            syncScoreLabels();
            grantedCp = true;
            DialogHelper.showInfo(
                    "Mission Abandoned",
                    sideLabel(side) + " gained 1 CP for the first mission abandoned this turn."
            );
        }

        int afterCp = currentCp(toPlayer(side));
        battleLogService.logTurnEvent(
                gameEngine.getCurrentRound(),
                gameEngine.getCurrentPhase(),
                toPlayer(side),
                playerLabel(toPlayer(side))
                        + " abandoned secondary mission \""
                        + selected.getName()
                        + "\"."
                        + (grantedCp
                        ? " First abandon this turn granted 1 CP (" + beforeCp + " -> " + afterCp + ")."
                        : "")
        );

        refreshMissionTablesFromSession();
    }

    private MissionEntryVM getSelectedMission(ArmySide side) {
        return getMissionTable(side).getSelectionModel().getSelectedItem();
    }

    private TableView<MissionEntryVM> getMissionTable(ArmySide side) {
        return side == ArmySide.BLUE ? blueMissionTable : redMissionTable;
    }

    private void drawSecondaryMissions(ArmySide side) {
        if (gameEngine.isBattleOver()) {
            DialogHelper.showInfo("Battle Over", winnerText());
            return;
        }

        if (toPlayer(side) != gameEngine.getActivePlayer()) {
            DialogHelper.showWarning(
                    "Wrong Turn",
                    "You can only draw tactical secondary missions during that army's current turn."
            );
            return;
        }

        Player player = toPlayer(side);
        int drawCount = missionSessionService.drawCountFor(player);
        if (drawCount <= 0) {
            DialogHelper.showInfo(
                    "No Draw Available",
                    sideLabel(side) + " has already drawn two tactical missions this turn."
            );
            return;
        }

        List<String> beforeMissionNames = missionNames(missionSessionService.activeEntriesFor(player));
        missionSessionService.drawFor(player);
        List<String> afterMissionNames = missionNames(missionSessionService.activeEntriesFor(player));
        List<String> drawnMissionNames = newlyAddedNames(beforeMissionNames, afterMissionNames);
        refreshMissionTablesFromSession();

        DialogHelper.showInfo(
                "Secondary Missions Drawn",
                sideLabel(side)
                        + " drew "
                        + drawCount
                        + " secondary mission"
                        + (drawCount == 1 ? "." : "s.")
        );

        battleLogService.logTurnEvent(
                gameEngine.getCurrentRound(),
                gameEngine.getCurrentPhase(),
                player,
                playerLabel(player)
                        + " drew tactical mission"
                        + (drawnMissionNames.size() == 1 ? "" : "s")
                        + ": "
                        + (drawnMissionNames.isEmpty()
                        ? drawCount + " mission" + (drawCount == 1 ? "" : "s")
                        : String.join(", ", drawnMissionNames))
                        + "."
        );
    }

    private Label getCpLabel(ArmySide side) {
        return side == ArmySide.BLUE ? blueCPLabel : redCPLabel;
    }

    private StratagemUseManager.BattleSide toBattleSide(ArmySide side) {
        return side == ArmySide.BLUE
                ? StratagemUseManager.BattleSide.BLUE
                : StratagemUseManager.BattleSide.RED;
    }

    private Player toPlayer(ArmySide side) {
        return side == ArmySide.BLUE ? Player.ATTACKER : Player.DEFENDER;
    }

    private void syncScoreLabels() {
        if (blueCPLabel != null) {
            blueCPLabel.setText(String.valueOf(gameEngine.currentCp(Player.ATTACKER)));
        }
        if (redCPLabel != null) {
            redCPLabel.setText(String.valueOf(gameEngine.currentCp(Player.DEFENDER)));
        }
        if (blueVPLabel != null) {
            blueVPLabel.setText(String.valueOf(gameEngine.currentVp(Player.ATTACKER)));
        }
        if (redVPLabel != null) {
            redVPLabel.setText(String.valueOf(gameEngine.currentVp(Player.DEFENDER)));
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
        if (gameEngine.getCurrentPhase() != Phase.COMMAND) {
            return;
        }

        List<UnitInstance> activeUnits = gameEngine.getActivePlayer() == Player.ATTACKER
                ? blueUnitInstances()
                : redUnitInstances();

        List<UnitInstance> candidates = battleShockService.battleShockCandidates(
                activeUnits,
                gameEngine.getCurrentRound()
        );

        if (candidates.isEmpty()) {
            return;
        }

        battleLogService.logTurnEvent(
                gameEngine.getCurrentRound(),
                gameEngine.getCurrentPhase(),
                gameEngine.getActivePlayer(),
                playerLabel(gameEngine.getActivePlayer())
                        + " begins the Battle-shock step for: "
                        + candidates.stream().map(UnitInstance::getUnitName).collect(Collectors.joining(", "))
                        + "."
        );

        openBattleShockWindow(candidates);
    }

    private void openBattleShockWindow(List<UnitInstance> candidates) {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/eecs2311/group2/wh40k_easycombat/BattleShock.fxml")
            );
            Parent root = loader.load();

            BattleShockController controller = loader.getController();
            String factionName = gameEngine.getActivePlayer() == Player.ATTACKER
                    ? blueFactionLabel.getText()
                    : redFactionLabel.getText();
            controller.setContext(
                    factionName,
                    gameEngine.getCurrentRound(),
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
                    gameEngine.getCurrentRound(),
                    gameEngine.getCurrentPhase(),
                    gameEngine.getActivePlayer(),
                    blueFactionLabel == null ? "Attacker Army" : blueFactionLabel.getText(),
                    blueArmyUnits,
                    redFactionLabel == null ? "Defender Army" : redFactionLabel.getText(),
                    redArmyUnits
            );

            Stage stage = new Stage();
            stage.initOwner(autoBattleCheckBox.getScene().getWindow());
            stage.initModality(Modality.WINDOW_MODAL);
            stage.setTitle("Auto Battle");
            stage.setScene(new Scene(root));
            stage.setMinWidth(1100.0);
            stage.setMinHeight(680.0);
            stage.showAndWait();

            refreshArmyViews();
            syncTurnUi();
        } catch (Exception e) {
            DialogHelper.showError("Open Auto Battle Error", e);
        }
    }

    private void applySetupConfig() {
        GameSetupConfig config = gameSetupService.getCurrentConfig();
        if (config == null) {
            List<MissionCard> primaryMissions = missionService.getPrimaryMissions();
            if (!primaryMissions.isEmpty()) {
                primaryMissionEntry = new MissionEntryVM(primaryMissions.get(0));
                missionNameLabel.setText(primaryMissionEntry.getName());
                primaryMissionStateLabel.setText("State: Active");
                gameEngine.selectMainMission(primaryMissionEntry.getName(), 0);
            }

            missionSessionService.initialize(null);
            missionSessionService.startTurn(gameEngine.getActivePlayer());
            refreshMissionTablesFromSession();
            battleLogService.logTurnEvent(
                    gameEngine.getCurrentRound(),
                    gameEngine.getCurrentPhase(),
                    gameEngine.getActivePlayer(),
                    "Battle started with the default setup. Attacker acts first."
            );
            return;
        }

        gameEngine.configureBattle(config.primaryMission().title(), config.maxRounds());
        ruleApplyState(config.customRulesEnabled());

        acceptImportedArmy(ArmySide.BLUE, config.blueArmy());
        acceptImportedArmy(ArmySide.RED, config.redArmy());

        primaryMissionEntry = new MissionEntryVM(config.primaryMission());
        primaryMissionEntry.setState("Active");
        missionNameLabel.setText(primaryMissionEntry.getName());
        primaryMissionStateLabel.setText("State: Active");

        missionSessionService.initialize(config);
        missionSessionService.startTurn(gameEngine.getActivePlayer());
        refreshMissionTablesFromSession();

        battleLogService.log(
                "Battle started. Attacker: "
                        + config.blueArmy().factionName()
                        + ". Defender: "
                        + config.redArmy().factionName()
                        + ". Battle size: "
                        + config.battleSizeLabel()
                        + " ("
                        + config.battleSizePoints()
                        + " points). Primary mission: "
                        + config.primaryMission().title()
                        + ". Max rounds: "
                        + config.maxRounds()
                        + ". Custom rules: "
                        + (config.customRulesEnabled() ? "enabled" : "disabled")
                        + "."
        );
        battleLogService.logTurnEvent(
                gameEngine.getCurrentRound(),
                gameEngine.getCurrentPhase(),
                gameEngine.getActivePlayer(),
                "Starting CP - Attacker: "
                        + currentCp(Player.ATTACKER)
                        + ", Defender: "
                        + currentCp(Player.DEFENDER)
                        + ". Attacker takes the first turn."
        );

        if (!config.blueFixedSecondaryMissions().isEmpty()) {
            battleLogService.log("Attacker fixed secondary missions: "
                    + config.blueFixedSecondaryMissions().stream().map(MissionCard::title).collect(Collectors.joining(", "))
                    + ".");
        }
        if (!config.redFixedSecondaryMissions().isEmpty()) {
            battleLogService.log("Defender fixed secondary missions: "
                    + config.redFixedSecondaryMissions().stream().map(MissionCard::title).collect(Collectors.joining(", "))
                    + ".");
        }
    }

    private void ruleApplyState(boolean enabled) {
        eecs2311.group2.wh40k_easycombat.service.editor.RuleEditorService.getInstance().setAutoApplyEnabled(enabled);
    }

    private void refreshMissionTablesFromSession() {
        blueMissionEntries.setAll(missionSessionService.activeEntriesFor(Player.ATTACKER));
        redMissionEntries.setAll(missionSessionService.activeEntriesFor(Player.DEFENDER));

        if (!blueMissionEntries.isEmpty()) {
            blueMissionTable.getSelectionModel().selectFirst();
        }
        if (!redMissionEntries.isEmpty()) {
            redMissionTable.getSelectionModel().selectFirst();
        }

        updateSecondaryMissionButtons();
    }

    private void updateSecondaryMissionButtons() {
        boolean blueFixed = missionSessionService.modeFor(Player.ATTACKER) == SecondaryMissionMode.FIXED;
        boolean redFixed = missionSessionService.modeFor(Player.DEFENDER) == SecondaryMissionMode.FIXED;
        boolean blueActiveTurn = gameEngine.getActivePlayer() == Player.ATTACKER;
        boolean redActiveTurn = gameEngine.getActivePlayer() == Player.DEFENDER;
        int blueDrawCount = missionSessionService.drawCountFor(Player.ATTACKER);
        int redDrawCount = missionSessionService.drawCountFor(Player.DEFENDER);

        if (blueDrawMissionButton != null) {
            blueDrawMissionButton.setText(
                    blueFixed
                            ? "Fixed Missions"
                            : (blueDrawCount == 1 ? "Draw 1 Mission" : "Draw " + blueDrawCount + " Missions")
            );
            blueDrawMissionButton.setDisable(
                    blueFixed || !blueActiveTurn || !missionSessionService.canDraw(Player.ATTACKER) || gameEngine.isBattleOver()
            );
        }
        if (redDrawMissionButton != null) {
            redDrawMissionButton.setText(
                    redFixed
                            ? "Fixed Missions"
                            : (redDrawCount == 1 ? "Draw 1 Mission" : "Draw " + redDrawCount + " Missions")
            );
            redDrawMissionButton.setDisable(
                    redFixed || !redActiveTurn || !missionSessionService.canDraw(Player.DEFENDER) || gameEngine.isBattleOver()
            );
        }
        if (blueAbandonMissionButton != null) {
            blueAbandonMissionButton.setDisable(blueFixed || !blueActiveTurn || blueMissionEntries.isEmpty() || gameEngine.isBattleOver());
        }
        if (redAbandonMissionButton != null) {
            redAbandonMissionButton.setDisable(redFixed || !redActiveTurn || redMissionEntries.isEmpty() || gameEngine.isBattleOver());
        }
        if (blueCheckMissionButton != null) {
            blueCheckMissionButton.setDisable(blueMissionEntries.isEmpty() || gameEngine.isBattleOver());
        }
        if (redCheckMissionButton != null) {
            redCheckMissionButton.setDisable(redMissionEntries.isEmpty() || gameEngine.isBattleOver());
        }
        if (blueSelectButton != null) {
            blueSelectButton.setDisable(gameEngine.isBattleOver());
        }
        if (redSelectButton != null) {
            redSelectButton.setDisable(gameEngine.isBattleOver());
        }
    }

    // When click the primary mission button, open the shared primary mission card and award VP if completed.
    @FXML
    private void openPrimaryMission(ActionEvent event) {
        if (gameEngine.isBattleOver()) {
            DialogHelper.showInfo("Battle Over", winnerText());
            return;
        }

        if (primaryMissionEntry == null || primaryMissionEntry.getMissionCard() == null) {
            DialogHelper.showWarning("No Primary Mission", "No primary mission card is currently loaded.");
            return;
        }

        MissionResolution resolution = openMissionCardWindow(
                "Shared Primary Mission",
                primaryMissionEntry.getMissionCard(),
                Player.ATTACKER,
                true,
                "Keep Active"
        );

        if (resolution.decision().isClosed()) {
            return;
        }

        if (resolution.decision().isCompleted()) {
            addVictoryPoints(resolution.awardedPlayer(), resolution.vpAwarded());
            if (primaryMissionStateLabel != null) {
                primaryMissionStateLabel.setText(
                        "Last Award: " + playerLabel(resolution.awardedPlayer()) + " +" + resolution.vpAwarded() + " VP"
                );
            }
            battleLogService.logTurnEvent(
                    gameEngine.getCurrentRound(),
                    gameEngine.getCurrentPhase(),
                    resolution.awardedPlayer(),
                    playerLabel(resolution.awardedPlayer())
                            + " scored "
                            + resolution.vpAwarded()
                            + " VP from the primary mission \""
                            + primaryMissionEntry.getName()
                            + "\"."
            );
            return;
        }

        if (primaryMissionStateLabel != null) {
            primaryMissionStateLabel.setText("State: Active");
        }
    }

    private List<GameArmyUnitVM> unitsFor(ArmySide side) {
        return side == ArmySide.BLUE ? List.copyOf(blueArmyUnits) : List.copyOf(redArmyUnits);
    }

    private GameArmyUnitVM openStratagemTargetWindow(
            ArmySide side,
            String stratagemName,
            List<EditorRuleDefinition> matchingRules,
            List<GameArmyUnitVM> candidates
    ) {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/eecs2311/group2/wh40k_easycombat/EditorStratagemTarget.fxml")
            );
            Parent root = loader.load();

            EditorStratagemTargetController controller = loader.getController();
            controller.setContext(
                    sideLabel(side),
                    stratagemName,
                    matchingRules,
                    candidates
            );
            
            Stage stage = new Stage();
            stage.initOwner(nextPhaseButton.getScene().getWindow());
            stage.initModality(Modality.WINDOW_MODAL);
            stage.setTitle("Choose Stratagem Target");
            stage.setScene(new Scene(root));
            stage.setMinWidth(760.0);
            stage.setMinHeight(520.0);
            stage.showAndWait();

            return controller.getSelectedUnit();
        } catch (Exception e) {
            DialogHelper.showError("Open Stratagem Target Error", e);
            return null;
        }
    }

    private String sideLabel(ArmySide side) {
        return side == ArmySide.BLUE ? "Attacker" : "Defender";
    }

    private MissionResolution openMissionCardWindow(
            String contextLabel,
            MissionCard missionCard,
            Player defaultAwardedPlayer,
            boolean allowPlayerSelection,
            String keepButtonText
    ) {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/eecs2311/group2/wh40k_easycombat/MissionCard.fxml")
            );
            Parent root = loader.load();

            MissionCardController controller = loader.getController();
            controller.setContext(
                    contextLabel,
                    missionCard,
                    defaultAwardedPlayer,
                    allowPlayerSelection,
                    keepButtonText
            );

            Stage stage = new Stage();
            stage.initOwner(nextPhaseButton.getScene().getWindow());
            stage.initModality(Modality.WINDOW_MODAL);
            stage.setTitle(missionCard == null ? "Mission Card" : missionCard.title());
            stage.setScene(new Scene(root, 760.0, 700.0));
            stage.setMinWidth(700.0);
            stage.setMinHeight(620.0);
            stage.showAndWait();

            return controller.getResolution();
        } catch (Exception e) {
            DialogHelper.showError("Open Mission Card Error", e);
            return MissionResolution.closed();
        }
    }

    private void addVictoryPoints(Player player, int vp) {
        if (vp <= 0) {
            updateWinnerLabel();
            return;
        }

        gameEngine.addVictoryPoints(player, vp);
        syncScoreLabels();
        updateWinnerLabel();
    }

    private void updateWinnerLabel() {
        if (winnerLabel == null) {
            return;
        }

        if (gameEngine.isBattleOver()) {
            winnerLabel.setText(winnerText());
            return;
        }

        int blueVp = gameEngine.currentVp(Player.ATTACKER);
        int redVp = gameEngine.currentVp(Player.DEFENDER);

        if (blueVp == redVp) {
            winnerLabel.setText("Current Score: Tied at " + blueVp + " VP");
            return;
        }

        boolean blueLeading = blueVp > redVp;
        winnerLabel.setText(
                "Current Leader: "
                        + (blueLeading ? "Attacker" : "Defender")
                        + " ("
                        + (blueLeading ? blueVp : redVp)
                        + "-"
                        + (blueLeading ? redVp : blueVp)
                        + ")"
        );
    }

    private void finishBattle() {
        gameEngine.finishBattle();
        syncTurnUi();
        battleLogService.log(winnerText());
        DialogHelper.showInfo("Battle Over", winnerText());
    }

    private String winnerText() {
        return gameEngine.winnerText();
    }

    private String playerLabel(Player player) {
        return player == Player.DEFENDER ? "Defender" : "Attacker";
    }

    private void adjustManualCommandPoints(ArmySide side, int delta) {
        Player player = toPlayer(side);
        int beforeCp = currentCp(player);
        gameEngine.adjustCommandPoints(player, delta);
        syncScoreLabels();

        int afterCp = currentCp(player);
        if (beforeCp == afterCp) {
            return;
        }

        battleLogService.logTurnEvent(
                gameEngine.getCurrentRound(),
                gameEngine.getCurrentPhase(),
                player,
                playerLabel(player)
                        + " manually adjusted CP: "
                        + beforeCp
                        + " -> "
                        + afterCp
                        + "."
        );
    }

    private int currentCp(Player player) {
        return gameEngine.currentCp(player);
    }

    private String phaseName(Phase phase) {
        if (phase == null) {
            return "Unknown";
        }
        return switch (phase) {
            case COMMAND -> "Command";
            case MOVEMENT -> "Movement";
            case SHOOTING -> "Shooting";
            case CHARGE -> "Charge";
            case FIGHT -> "Fight";
        };
    }

    private List<String> missionNames(List<MissionEntryVM> entries) {
        return entries.stream()
                .map(MissionEntryVM::getName)
                .collect(Collectors.toList());
    }

    private List<String> newlyAddedNames(List<String> before, List<String> after) {
        List<String> added = after.stream().collect(Collectors.toList());

        for (String previous : before) {
            added.remove(previous);
        }

        return added;
    }
}
