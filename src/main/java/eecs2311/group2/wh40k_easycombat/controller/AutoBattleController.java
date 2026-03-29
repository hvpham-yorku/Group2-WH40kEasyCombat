package eecs2311.group2.wh40k_easycombat.controller;

import eecs2311.group2.wh40k_easycombat.cell.GameArmyUnitCell;
import eecs2311.group2.wh40k_easycombat.controller.helper.AutoBattleLogFormatter;
import eecs2311.group2.wh40k_easycombat.controller.helper.AutoBattleOptionalRuleHelper;
import eecs2311.group2.wh40k_easycombat.controller.helper.AutoBattleViewHelper;
import eecs2311.group2.wh40k_easycombat.controller.helper.DialogHelper;
import eecs2311.group2.wh40k_easycombat.model.combat.AutoBattleResolution;
import eecs2311.group2.wh40k_easycombat.model.combat.FightPhaseState;
import eecs2311.group2.wh40k_easycombat.model.combat.PendingDamageStepResult;
import eecs2311.group2.wh40k_easycombat.model.editor.EditorActiveEffect;
import eecs2311.group2.wh40k_easycombat.model.editor.EditorRerollType;
import eecs2311.group2.wh40k_easycombat.model.editor.EditorRuleDefinition;
import eecs2311.group2.wh40k_easycombat.model.instance.Phase;
import eecs2311.group2.wh40k_easycombat.model.instance.Player;
import eecs2311.group2.wh40k_easycombat.model.instance.UnitInstance;
import eecs2311.group2.wh40k_easycombat.model.instance.UnitModelInstance;
import eecs2311.group2.wh40k_easycombat.model.instance.WeaponProfile;
import eecs2311.group2.wh40k_easycombat.service.BattleLogService;
import eecs2311.group2.wh40k_easycombat.service.autobattle.AttackKeywordContext;
import eecs2311.group2.wh40k_easycombat.service.autobattle.AutoBattleMode;
import eecs2311.group2.wh40k_easycombat.service.autobattle.AutoBattleService;
import eecs2311.group2.wh40k_easycombat.service.autobattle.FightPhaseOrderService;
import eecs2311.group2.wh40k_easycombat.service.autobattle.PendingDamageSession;
import eecs2311.group2.wh40k_easycombat.service.editor.EditorEffectRuntimeService;
import eecs2311.group2.wh40k_easycombat.service.editor.EditorRuleApplicationService;
import eecs2311.group2.wh40k_easycombat.service.game.*;
import eecs2311.group2.wh40k_easycombat.viewmodel.GameArmyUnitVM;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.util.EnumMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class AutoBattleController {
    // ======================= Labels =======================
    @FXML private Label blueFactionLabel, redFactionLabel, battleModeLabel, phaseStatusLabel, fightStepLabel;

    // ======================= Lists =======================
    @FXML private ListView<GameArmyUnitVM> blueUnitList, redUnitList;
    @FXML private ListView<UnitModelInstance> allocationModelList;

    // ======================= Weapon Selectors =======================
    @FXML private ComboBox<WeaponProfile> blueWeaponCombo, redWeaponCombo;
    @FXML private Spinner<Integer> blueBearerCountSpinner, redBearerCountSpinner;

    // ======================= Helper Labels =======================
    @FXML private Label blueWeaponHintLabel, redWeaponHintLabel, blueExtraAttackLabel, redExtraAttackLabel, pendingDamageStatusLabel;

    // ======================= Layout Boxes =======================
    @FXML private VBox shootingRulesBox, fightRulesBox, blueFightBox, redFightBox;
    @FXML private FlowPane blueOptionalRulesPane, redOptionalRulesPane;
    @FXML private Label blueOptionalRulesHintLabel, redOptionalRulesHintLabel;

    // ======================= CheckBoxes =======================
    @FXML private CheckBox withinHalfRangeCheckBox, remainedStationaryCheckBox, targetHasCoverCheckBox, blastLegalCheckBox;
    @FXML private CheckBox blueEligibleFightCheckBox, redEligibleFightCheckBox, blueChargedCheckBox, redChargedCheckBox;
    @FXML private CheckBox targetInfantryCheckBox, targetVehicleCheckBox, targetMonsterCheckBox, targetCharacterCheckBox, targetPsykerCheckBox;

    // ======================= Buttons and Logs =======================
    @FXML private Button blueAttackButton, redAttackButton, applyPendingDamageButton, closeButton;
    @FXML private TextArea battleResultBox, rollLogBox;

    private final ObservableList<GameArmyUnitVM> blueUnits = FXCollections.observableArrayList();
    private final ObservableList<GameArmyUnitVM> redUnits = FXCollections.observableArrayList();
    private final AutoBattleService autoBattleService = new AutoBattleService();
    private final EditorRuleApplicationService editorRuleApplicationService = new EditorRuleApplicationService();
    private final EditorEffectRuntimeService editorEffectRuntimeService = EditorEffectRuntimeService.getInstance();
    private final BattleLogService battleLogService = BattleLogService.getInstance();
    private final Map<Player, Set<String>> pendingOptionalSelections = new EnumMap<>(Player.class);
    private AutoBattleMode battleMode = AutoBattleMode.SHOOTING;
    private int currentRound = 1;
    private Phase currentPhase = Phase.COMMAND;
    private Player activeTurnPlayer = Player.ATTACKER;
    private FightPhaseState fightPhaseState = FightPhaseState.complete("No units are currently marked eligible to fight.");
    private Player lastFightPlayer;
    private PendingDamageSession currentPendingSession;

    // When this page loads, initialize all lists, controls and default log states for auto battle.
    @FXML
    private void initialize() {
        pendingOptionalSelections.put(Player.ATTACKER, new LinkedHashSet<>());
        pendingOptionalSelections.put(Player.DEFENDER, new LinkedHashSet<>());
        blueUnitList.setItems(blueUnits);
        redUnitList.setItems(redUnits);
        blueUnitList.setCellFactory(v -> new GameArmyUnitCell(() -> handleArmyStateChanged(Player.ATTACKER)));
        redUnitList.setCellFactory(v -> new GameArmyUnitCell(() -> handleArmyStateChanged(Player.DEFENDER)));
        AutoBattleViewHelper.configureWeaponCombo(blueWeaponCombo);
        AutoBattleViewHelper.configureWeaponCombo(redWeaponCombo);
        AutoBattleViewHelper.configureModelList(allocationModelList);
        blueBearerCountSpinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 99, 1));
        redBearerCountSpinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 99, 1));
        blueUnitList.getSelectionModel().selectedItemProperty().addListener((o, a, b) -> onUnitSelected(Player.ATTACKER, b));
        redUnitList.getSelectionModel().selectedItemProperty().addListener((o, a, b) -> onUnitSelected(Player.DEFENDER, b));
        blueWeaponCombo.valueProperty().addListener((o, a, b) -> {
            AutoBattleViewHelper.syncSpinnerToWeapon(blueBearerCountSpinner, b);
            refreshOptionalRulePane(Player.ATTACKER);
        });
        redWeaponCombo.valueProperty().addListener((o, a, b) -> {
            AutoBattleViewHelper.syncSpinnerToWeapon(redBearerCountSpinner, b);
            refreshOptionalRulePane(Player.DEFENDER);
        });
        blueEligibleFightCheckBox.selectedProperty().addListener((o, a, b) -> updateSelectedFightState(Player.ATTACKER));
        redEligibleFightCheckBox.selectedProperty().addListener((o, a, b) -> updateSelectedFightState(Player.DEFENDER));
        blueChargedCheckBox.selectedProperty().addListener((o, a, b) -> updateSelectedFightState(Player.ATTACKER));
        redChargedCheckBox.selectedProperty().addListener((o, a, b) -> updateSelectedFightState(Player.DEFENDER));
        battleResultBox.setEditable(false);
        rollLogBox.setEditable(false);
    }

    public void setBattleContext(AutoBattleMode mode, int round, Phase phase, Player activePlayer, String blueName, List<GameArmyUnitVM> blueArmyUnits, String redName, List<GameArmyUnitVM> redArmyUnits) {
        battleMode = mode == null ? AutoBattleMode.SHOOTING : mode;
        currentRound = round <= 0 ? 1 : round;
        currentPhase = phase == null ? Phase.COMMAND : phase;
        activeTurnPlayer = activePlayer == null ? Player.ATTACKER : activePlayer;
        lastFightPlayer = null;
        currentPendingSession = null;
        pendingOptionalSelections.get(Player.ATTACKER).clear();
        pendingOptionalSelections.get(Player.DEFENDER).clear();
        fightPhaseState = FightPhaseState.complete("No units are currently marked eligible to fight.");
        blueFactionLabel.setText(blank(blueName, "Attacker Army"));
        redFactionLabel.setText(blank(redName, "Defender Army"));
        blueUnits.setAll(blueArmyUnits == null ? List.of() : blueArmyUnits);
        redUnits.setAll(redArmyUnits == null ? List.of() : redArmyUnits);
        ArmyListStateService.refreshArmyOrdering(blueUnits);
        ArmyListStateService.refreshArmyOrdering(redUnits);
        battleModeLabel.setText(battleMode.title());
        AutoBattleViewHelper.setModeVisibility(battleMode, shootingRulesBox, fightRulesBox, blueFightBox, redFightBox);
        if (!blueUnits.isEmpty()) blueUnitList.getSelectionModel().selectFirst();
        if (!redUnits.isEmpty()) redUnitList.getSelectionModel().selectFirst();
        refreshUi();
    }

    // When click "Attacker Attacks Defender" button, resolve one automatic attack from the attacker side.
    @FXML private void blueAttackRed(ActionEvent event) { resolveAttack(Player.ATTACKER, blueUnitList.getSelectionModel().getSelectedItem(), blueWeaponCombo.getValue(), blueBearerCountSpinner.getValue(), redUnitList.getSelectionModel().getSelectedItem()); }

    // When click "Defender Attacks Attacker" button, resolve one automatic attack from the defender side.
    @FXML private void redAttackBlue(ActionEvent event) { resolveAttack(Player.DEFENDER, redUnitList.getSelectionModel().getSelectedItem(), redWeaponCombo.getValue(), redBearerCountSpinner.getValue(), blueUnitList.getSelectionModel().getSelectedItem()); }

    // When click "Apply Pending Damage" button, apply the next pending unsaved hit to the selected model.
    @FXML
    private void applyPendingDamage(ActionEvent event) {
        if (!hasPendingDamage()) {
            DialogHelper.showWarning("No Pending Damage", "There is no pending damage waiting for allocation.");
            return;
        }
        PendingDamageStepResult result = autoBattleService.applyNextPendingDamage(
                currentPendingSession,
                allocationModelList.getSelectionModel().getSelectedItem()
        );
        if (!result.applied()) {
            DialogHelper.showWarning("Allocation Error", result.message());
            return;
        }
        AutoBattleLogFormatter.logAllocation(
                battleLogService,
                battleResultBox,
                currentRound,
                currentPhase,
                currentPendingSession,
                result
        );
        if (result.sessionComplete()) currentPendingSession = null;
        refreshUi();
    }

    // When click "Close" button, confirm whether to leave the auto battle window with unresolved damage.
    @FXML
    private void closeWindow(ActionEvent event) {
        if (hasPendingDamage() && !DialogHelper.confirmYesNo(
                "Unresolved Attack",
                "This attack still has unresolved damage allocation. If you close now, this attack will not be recorded. Close anyway?"
        )) {
            return;
        }
        ((Stage) closeButton.getScene().getWindow()).close();
    }

    private void resolveAttack(Player attackerSide, GameArmyUnitVM attackerVm, WeaponProfile selectedWeapon, Integer bearerCount, GameArmyUnitVM defenderVm) {
        if (hasPendingDamage()) {
            DialogHelper.showWarning("Pending Damage", "Allocate all pending damage before starting another automatic attack.");
            return;
        }
        if (attackerVm == null) { DialogHelper.showWarning("No Attacker Selected", "Please select an attacking unit first."); return; }
        if (defenderVm == null) { DialogHelper.showWarning("No Target Selected", "Please select a target unit first."); return; }
        if (selectedWeapon == null) { DialogHelper.showWarning("No Weapon Selected", "Please select one weapon first."); return; }
        refreshFightState();

        List<EditorRuleDefinition> selectedOptionalRules = AutoBattleOptionalRuleHelper.selectedOptionalRulesForAttack(
                attackerSide,
                attackerVm.getUnit(),
                defenderVm.getUnit(),
                selectedWeapon,
                battleMode,
                editorRuleApplicationService,
                editorEffectRuntimeService,
                pendingOptionalSelections
        );
        List<EditorActiveEffect> activatedOptionalRules = editorEffectRuntimeService.activateOptionalRulesForAttack(
                selectedOptionalRules,
                attackerVm.getUnit(),
                defenderVm.getUnit(),
                attackerSide,
                activeTurnPlayer,
                currentPhase,
                currentRound
        );

        AutoBattleResolution resolution = autoBattleService.resolve(
                battleMode, activeTurnPlayer, attackerSide, attackerVm.getUnit(), defenderVm.getUnit(), selectedWeapon,
                buildContext(attackerSide, bearerCount == null ? 0 : bearerCount), fightPhaseState
        );
        if (!resolution.resolved()) {
            editorEffectRuntimeService.deactivateEffects(activatedOptionalRules);
            DialogHelper.showWarning("Attack Not Resolved", resolution.failureMessage());
            return;
        }
        if (battleMode == AutoBattleMode.FIGHT) lastFightPlayer = attackerSide;
        currentPendingSession = resolution.allocationSession() != null && resolution.allocationSession().hasPendingDamage() ? resolution.allocationSession() : null;
        AutoBattleOptionalRuleHelper.clearAppliedOptionalSelections(pendingOptionalSelections, attackerSide, activatedOptionalRules);
        AutoBattleOptionalRuleHelper.logOptionalRuleActivations(
                battleLogService,
                battleResultBox,
                currentRound,
                currentPhase,
                attackerSide,
                AutoBattleViewHelper.label(attackerSide),
                attackerVm.getUnitName(),
                activatedOptionalRules
        );
        AutoBattleLogFormatter.logResolution(
                battleLogService,
                battleResultBox,
                rollLogBox,
                currentRound,
                currentPhase,
                attackerSide,
                battleMode,
                attackerVm.getUnitName(),
                defenderVm.getUnitName(),
                resolution
        );
        refreshUi();
    }

    private void onUnitSelected(Player side, GameArmyUnitVM selectedVm) {
        if (side == Player.ATTACKER) {
            AutoBattleViewHelper.loadWeapons(autoBattleService, battleMode, selectedVm, blueWeaponCombo, blueBearerCountSpinner, blueWeaponHintLabel, blueExtraAttackLabel);
            AutoBattleViewHelper.loadFightFlags(battleMode, selectedVm, blueEligibleFightCheckBox, blueChargedCheckBox);
        } else {
            AutoBattleViewHelper.loadWeapons(autoBattleService, battleMode, selectedVm, redWeaponCombo, redBearerCountSpinner, redWeaponHintLabel, redExtraAttackLabel);
            AutoBattleViewHelper.loadFightFlags(battleMode, selectedVm, redEligibleFightCheckBox, redChargedCheckBox);
        }
        refreshOptionalRulePane(Player.ATTACKER);
        refreshOptionalRulePane(Player.DEFENDER);
    }

    private void refreshUi() {
        ArmyListStateService.refreshArmyOrdering(blueUnits);
        ArmyListStateService.refreshArmyOrdering(redUnits);
        refreshFightState();
        updatePhaseText();
        updateActionButtons();
        updatePendingDamageUi();
        AutoBattleViewHelper.loadWeapons(autoBattleService, battleMode, blueUnitList.getSelectionModel().getSelectedItem(), blueWeaponCombo, blueBearerCountSpinner, blueWeaponHintLabel, blueExtraAttackLabel);
        AutoBattleViewHelper.loadWeapons(autoBattleService, battleMode, redUnitList.getSelectionModel().getSelectedItem(), redWeaponCombo, redBearerCountSpinner, redWeaponHintLabel, redExtraAttackLabel);
        refreshOptionalRulePane(Player.ATTACKER);
        refreshOptionalRulePane(Player.DEFENDER);
        blueUnitList.refresh();
        redUnitList.refresh();
    }

    private void refreshFightState() {
        if (battleMode != AutoBattleMode.FIGHT) {
            fightStepLabel.setText(battleMode == AutoBattleMode.REACTION_SHOOTING ? "Reaction fire is handled by the non-active player." : "Use the selected ranged weapon to resolve attacks.");
            return;
        }
        fightPhaseState = FightPhaseOrderService.rebuildState(activeTurnPlayer, fightPhaseState.step(), lastFightPlayer, blueUnitInstances(), redUnitInstances());
        fightStepLabel.setText(fightPhaseState.message());
    }

    private void updatePhaseText() {
        AutoBattleViewHelper.updatePhaseText(phaseStatusLabel, battleMode, currentPhase, activeTurnPlayer);
    }

    private void updateActionButtons() {
        AutoBattleViewHelper.updateActionButtons(
                blueAttackButton,
                redAttackButton,
                battleMode,
                activeTurnPlayer,
                fightPhaseState,
                hasPendingDamage()
        );
    }

    private void updatePendingDamageUi() {
        AutoBattleViewHelper.updatePendingDamageUi(
                allocationModelList,
                pendingDamageStatusLabel,
                applyPendingDamageButton,
                currentPendingSession
        );
    }

    private void refreshOptionalRulePane(Player side) {
        FlowPane pane = side == Player.ATTACKER ? blueOptionalRulesPane : redOptionalRulesPane;
        Label hintLabel = side == Player.ATTACKER ? blueOptionalRulesHintLabel : redOptionalRulesHintLabel;
        GameArmyUnitVM attackerVm = side == Player.ATTACKER
                ? blueUnitList.getSelectionModel().getSelectedItem()
                : redUnitList.getSelectionModel().getSelectedItem();
        GameArmyUnitVM defenderVm = side == Player.ATTACKER
                ? redUnitList.getSelectionModel().getSelectedItem()
                : blueUnitList.getSelectionModel().getSelectedItem();
        WeaponProfile weapon = side == Player.ATTACKER ? blueWeaponCombo.getValue() : redWeaponCombo.getValue();
        AutoBattleOptionalRuleHelper.refreshOptionalRulePane(
                pane,
                hintLabel,
                side,
                attackerVm,
                defenderVm,
                weapon,
                battleMode,
                editorRuleApplicationService,
                editorEffectRuntimeService,
                pendingOptionalSelections,
                (ruleId, selected) -> {
                    boolean alreadyActive = AutoBattleOptionalRuleHelper.optionalRuleViewsForAttack(
                            editorRuleApplicationService,
                            editorEffectRuntimeService,
                            battleMode,
                            attackerVm == null ? null : attackerVm.getUnit(),
                            defenderVm == null ? null : defenderVm.getUnit(),
                            weapon
                    ).stream().anyMatch(view -> view.rule().getId().equals(ruleId) && view.active());
                    AutoBattleOptionalRuleHelper.updateOptionalSelection(
                            pendingOptionalSelections,
                            side,
                            ruleId,
                            selected,
                            alreadyActive
                    );
                }
        );
    }

    private void updateSelectedFightState(Player player) {
        if (battleMode != AutoBattleMode.FIGHT) return;
        GameArmyUnitVM vm = player == Player.ATTACKER ? blueUnitList.getSelectionModel().getSelectedItem() : redUnitList.getSelectionModel().getSelectedItem();
        if (vm == null) return;
        if (player == Player.ATTACKER) {
            vm.getUnit().setEligibleToFightThisPhase(blueEligibleFightCheckBox.isSelected());
            vm.getUnit().setChargedThisTurn(blueChargedCheckBox.isSelected());
        } else {
            vm.getUnit().setEligibleToFightThisPhase(redEligibleFightCheckBox.isSelected());
            vm.getUnit().setChargedThisTurn(redChargedCheckBox.isSelected());
        }
        refreshFightState();
        updateActionButtons();
    }

    private AttackKeywordContext buildContext(Player attackingPlayer, int bearerCount) {
        boolean charged = attackingPlayer == Player.ATTACKER ? blueChargedCheckBox.isSelected() : redChargedCheckBox.isSelected();
        boolean eligible = attackingPlayer == Player.ATTACKER ? blueEligibleFightCheckBox.isSelected() : redEligibleFightCheckBox.isSelected();
        int resolvedBearerCount = battleMode == AutoBattleMode.FIGHT ? 0 : bearerCount;
        return new AttackKeywordContext(
                resolvedBearerCount,
                withinHalfRangeCheckBox.isSelected(),
                remainedStationaryCheckBox.isSelected(),
                false,
                false,
                charged,
                eligible,
                targetHasCoverCheckBox.isSelected(),
                blastLegalCheckBox.isSelected(),
                false,
                "",
                targetInfantryCheckBox.isSelected(),
                targetVehicleCheckBox.isSelected(),
                targetMonsterCheckBox.isSelected(),
                targetCharacterCheckBox.isSelected(),
                targetPsykerCheckBox.isSelected(),
                0,
                0,
                EditorRerollType.NONE,
                EditorRerollType.NONE
        );
    }

    private boolean hasPendingDamage() { return currentPendingSession != null && currentPendingSession.hasPendingDamage(); }
    private void handleArmyStateChanged(Player side) { refreshUi(); }
    private List<UnitInstance> blueUnitInstances() { return blueUnits.stream().map(GameArmyUnitVM::getUnit).collect(Collectors.toList()); }
    private List<UnitInstance> redUnitInstances() { return redUnits.stream().map(GameArmyUnitVM::getUnit).collect(Collectors.toList()); }
    private String blank(String value, String fallback) { return value == null || value.isBlank() ? fallback : value; }
}
