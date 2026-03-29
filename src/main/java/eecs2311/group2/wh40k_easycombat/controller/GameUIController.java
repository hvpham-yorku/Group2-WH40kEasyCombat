package eecs2311.group2.wh40k_easycombat.controller;

import eecs2311.group2.wh40k_easycombat.cell.GameArmyUnitCell;
import eecs2311.group2.wh40k_easycombat.cell.GameStrategyCell;
import eecs2311.group2.wh40k_easycombat.controller.helper.DialogHelper;
import eecs2311.group2.wh40k_easycombat.controller.helper.GameUIDiceHelper;
import eecs2311.group2.wh40k_easycombat.controller.helper.GameUIMissionHelper;
import eecs2311.group2.wh40k_easycombat.controller.helper.GameUIPrimaryMissionHelper;
import eecs2311.group2.wh40k_easycombat.controller.helper.GameUIScoreHelper;
import eecs2311.group2.wh40k_easycombat.controller.helper.GameUISetupHelper;
import eecs2311.group2.wh40k_easycombat.controller.helper.GameUIStrategyHelper;
import eecs2311.group2.wh40k_easycombat.controller.helper.GameUITurnHelper;
import eecs2311.group2.wh40k_easycombat.controller.helper.GameUIWindowHelper;
import eecs2311.group2.wh40k_easycombat.manager.StratagemUseManager;
import eecs2311.group2.wh40k_easycombat.model.editor.EditorRuleDefinition;
import eecs2311.group2.wh40k_easycombat.model.instance.Phase;
import eecs2311.group2.wh40k_easycombat.model.instance.Player;
import eecs2311.group2.wh40k_easycombat.model.instance.UnitInstance;
import eecs2311.group2.wh40k_easycombat.model.mission.MissionCard;
import eecs2311.group2.wh40k_easycombat.model.mission.MissionResolution;
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
import javafx.scene.Node;
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

import java.io.IOException;
import java.util.List;
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
        GameUIWindowHelper.openBattleLogWindow(battleLogButton);
    }

    // When click "Roll" button, roll the selected number of D6 and count successes.
    @FXML
    void rollDice(MouseEvent event) {
        GameUIDiceHelper.rollDice(
                manualDiceService,
                virtuaDiceSpinner,
                virtualDiceSuccessComboBox,
                virtuaDiceBox,
                battleLogService,
                gameEngine
        );
    }

    // When click "Clear" button, clear the manual dice log text area.
    @FXML
    void clearDiceLog(MouseEvent event) {
        GameUIDiceHelper.clearDiceLog(virtuaDiceBox);
    }

    // When click "Next Phase" button, advance the battle to the next phase and update turn state.
    @FXML
    private void nextPhase(ActionEvent event) {
        GameUITurnHelper.nextPhase(
                gameEngine,
                editorEffectRuntimeService,
                missionSessionService,
                battleLogService,
                new GameUITurnHelper.PhaseLabelText(this::phaseName, this::playerLabel),
                new GameUITurnHelper.TurnCallbacks(
                        this::finishBattle,
                        this::syncTurnUi,
                        this::refreshArmyViews,
                        this::refreshMissionTablesFromSession,
                        this::maybeOpenBattleShockWindow,
                        this::currentCp
                )
        );
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
        GameUIScoreHelper.syncScoreLabels(gameEngine, blueCPLabel, redCPLabel, blueVPLabel, redVPLabel);
        syncTurnUi();
        GameUIScoreHelper.updateWinnerLabel(gameEngine, winnerLabel);
    }

    private void syncTurnUi() {
        GameUIScoreHelper.syncTurnUi(
                gameEngine,
                roundLabel,
                bluePhaseLabel,
                redPhaseLabel,
                blueCPLabel,
                redCPLabel,
                blueVPLabel,
                redVPLabel,
                winnerLabel,
                primaryMissionStateLabel,
                nextPhaseButton,
                autoBattleCheckBox,
                this::updateSecondaryMissionButtons
        );
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
        Player player = toPlayer(side);
        GameUIStrategyHelper.useSelectedStrategy(
                gameEngine,
                editorEffectRuntimeService,
                battleLogService,
                side,
                getSelectedStrategy(side),
                toBattleSide(side),
                getCpLabel(side).getText(),
                () -> unitsFor(side),
                (sideText, stratagemName, matchingRules, candidates) -> openStratagemTargetWindow(side, stratagemName, matchingRules, candidates),
                this::syncScoreLabels,
                this::refreshArmyViews,
                () -> currentCp(player),
                new GameUIStrategyHelper.SideTextResolver(this::sideLabel, this::playerLabel)
        );
    }

    private GameStrategyVM getSelectedStrategy(ArmySide side) {
        return side == ArmySide.BLUE
                ? blueStrategyList.getSelectionModel().getSelectedItem()
                : redStrategyList.getSelectionModel().getSelectedItem();
    }

    private void openSelectedSecondaryMission(ArmySide side) {
        Player owningPlayer = toPlayer(side);
        GameUIMissionHelper.openSelectedSecondaryMission(
                gameEngine,
                missionSessionService,
                battleLogService,
                owningPlayer,
                sideLabel(side),
                playerLabel(owningPlayer),
                getMissionTable(side),
                this::openMissionCardWindow,
                this::addVictoryPoints,
                this::refreshMissionTablesFromSession
        );
    }

    private void abandonSelectedMission(ArmySide side) {
        Player player = toPlayer(side);
        GameUIMissionHelper.abandonSelectedMission(
                gameEngine,
                missionSessionService,
                battleLogService,
                player,
                sideLabel(side),
                playerLabel(player),
                getMissionTable(side),
                this::syncScoreLabels,
                this::currentCp,
                () -> gameEngine.addCommandPoint(player),
                this::refreshMissionTablesFromSession
        );
    }

    @SuppressWarnings("unused")
	private MissionEntryVM getSelectedMission(ArmySide side) {
        return getMissionTable(side).getSelectionModel().getSelectedItem();
    }

    private TableView<MissionEntryVM> getMissionTable(ArmySide side) {
        return side == ArmySide.BLUE ? blueMissionTable : redMissionTable;
    }

    private void drawSecondaryMissions(ArmySide side) {
        Player player = toPlayer(side);
        GameUIMissionHelper.drawSecondaryMissions(
                gameEngine,
                missionSessionService,
                battleLogService,
                player,
                sideLabel(side),
                playerLabel(player),
                this::refreshMissionTablesFromSession
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
        GameUIScoreHelper.syncScoreLabels(gameEngine, blueCPLabel, redCPLabel, blueVPLabel, redVPLabel);
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
        String factionName = gameEngine.getActivePlayer() == Player.ATTACKER
                ? blueFactionLabel.getText()
                : redFactionLabel.getText();
        GameUIWindowHelper.openBattleShockWindow(
                nextPhaseButton,
                factionName,
                gameEngine.getCurrentRound(),
                candidates,
                this::refreshArmyViews
        );
    }

    private void refreshArmyViews() {
        ArmyListStateService.refreshArmyOrdering(blueArmyUnits);
        ArmyListStateService.refreshArmyOrdering(redArmyUnits);
        blueArmyList.refresh();
        redArmyList.refresh();
    }

    private void openAutoBattleWindow(AutoBattleMode mode) {
        GameUIWindowHelper.openAutoBattleWindow(
                autoBattleCheckBox,
                mode,
                gameEngine,
                blueFactionLabel == null ? "Attacker Army" : blueFactionLabel.getText(),
                blueArmyUnits,
                redFactionLabel == null ? "Defender Army" : redFactionLabel.getText(),
                redArmyUnits,
                () -> {
                    refreshArmyViews();
                    syncTurnUi();
                }
        );
    }

    private void applySetupConfig() {
        primaryMissionEntry = GameUISetupHelper.applySetupConfig(
                gameSetupService,
                missionService,
                missionSessionService,
                gameEngine,
                battleLogService,
                new GameUISetupHelper.SetupCallbacks(
                        this::acceptImportedArmy,
                        missionName -> missionNameLabel.setText(missionName),
                        stateText -> primaryMissionStateLabel.setText(stateText),
                        this::refreshMissionTablesFromSession,
                        this::ruleApplyState,
                        this::currentCp
                )
        );
    }

    private void ruleApplyState(boolean enabled) {
        eecs2311.group2.wh40k_easycombat.service.editor.RuleEditorService.getInstance().setAutoApplyEnabled(enabled);
    }

    private void refreshMissionTablesFromSession() {
        GameUIMissionHelper.refreshMissionTables(
                missionSessionService,
                blueMissionEntries,
                redMissionEntries,
                blueMissionTable,
                redMissionTable
        );
        updateSecondaryMissionButtons();
    }

    private void updateSecondaryMissionButtons() {
        GameUIMissionHelper.updateSecondaryMissionButtons(
                missionSessionService,
                gameEngine,
                blueDrawMissionButton,
                redDrawMissionButton,
                blueAbandonMissionButton,
                redAbandonMissionButton,
                blueCheckMissionButton,
                redCheckMissionButton,
                blueSelectButton,
                redSelectButton,
                blueMissionEntries,
                redMissionEntries
        );
    }

    // When click the primary mission button, open the shared primary mission card and award VP if completed.
    @FXML
    private void openPrimaryMission(ActionEvent event) {
        GameUIPrimaryMissionHelper.openPrimaryMission(
                gameEngine,
                battleLogService,
                primaryMissionEntry,
                primaryMissionStateLabel,
                this::playerLabel,
                this::openMissionCardWindow,
                this::addVictoryPoints
        );
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
        return GameUIWindowHelper.openStratagemTargetWindow(
                nextPhaseButton,
                sideLabel(side),
                stratagemName,
                matchingRules,
                candidates
        );
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
        return GameUIWindowHelper.openMissionCardWindow(
                nextPhaseButton,
                contextLabel,
                missionCard,
                defaultAwardedPlayer,
                allowPlayerSelection,
                keepButtonText
        );
    }

    private void addVictoryPoints(Player player, int vp) {
        if (vp <= 0) {
            GameUIScoreHelper.updateWinnerLabel(gameEngine, winnerLabel);
            return;
        }

        gameEngine.addVictoryPoints(player, vp);
        syncScoreLabels();
        GameUIScoreHelper.updateWinnerLabel(gameEngine, winnerLabel);
    }

    private void finishBattle() {
        GameUIScoreHelper.finishBattle(gameEngine, battleLogService, this::syncTurnUi);
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

}
