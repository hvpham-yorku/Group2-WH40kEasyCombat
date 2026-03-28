package eecs2311.group2.wh40k_easycombat.controller;

import eecs2311.group2.wh40k_easycombat.cell.GameArmyUnitCell;
import eecs2311.group2.wh40k_easycombat.controller.helper.DialogHelper;
import eecs2311.group2.wh40k_easycombat.model.combat.AttackResult;
import eecs2311.group2.wh40k_easycombat.model.combat.AutoBattleResolution;
import eecs2311.group2.wh40k_easycombat.model.combat.CasualtyUpdate;
import eecs2311.group2.wh40k_easycombat.model.combat.FightPhaseState;
import eecs2311.group2.wh40k_easycombat.model.combat.PendingDamage;
import eecs2311.group2.wh40k_easycombat.model.combat.PendingDamageStepResult;
import eecs2311.group2.wh40k_easycombat.model.combat.ResolvedAttack;
import eecs2311.group2.wh40k_easycombat.model.editor.EditorRerollType;
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
import eecs2311.group2.wh40k_easycombat.service.autobattle.FightStep;
import eecs2311.group2.wh40k_easycombat.service.autobattle.PendingDamageSession;
import eecs2311.group2.wh40k_easycombat.service.game.*;
import eecs2311.group2.wh40k_easycombat.viewmodel.GameArmyUnitVM;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.util.List;
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
    private final BattleLogService battleLogService = BattleLogService.getInstance();
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
        blueUnitList.setItems(blueUnits);
        redUnitList.setItems(redUnits);
        blueUnitList.setCellFactory(v -> new GameArmyUnitCell(() -> handleArmyStateChanged(Player.ATTACKER)));
        redUnitList.setCellFactory(v -> new GameArmyUnitCell(() -> handleArmyStateChanged(Player.DEFENDER)));
        configureWeaponCombo(blueWeaponCombo);
        configureWeaponCombo(redWeaponCombo);
        configureModelList();
        blueBearerCountSpinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 99, 1));
        redBearerCountSpinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 99, 1));
        blueUnitList.getSelectionModel().selectedItemProperty().addListener((o, a, b) -> onUnitSelected(Player.ATTACKER, b));
        redUnitList.getSelectionModel().selectedItemProperty().addListener((o, a, b) -> onUnitSelected(Player.DEFENDER, b));
        blueWeaponCombo.valueProperty().addListener((o, a, b) -> syncSpinnerToWeapon(blueBearerCountSpinner, b));
        redWeaponCombo.valueProperty().addListener((o, a, b) -> syncSpinnerToWeapon(redBearerCountSpinner, b));
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
        fightPhaseState = FightPhaseState.complete("No units are currently marked eligible to fight.");
        blueFactionLabel.setText(blank(blueName, "Attacker Army"));
        redFactionLabel.setText(blank(redName, "Defender Army"));
        blueUnits.setAll(blueArmyUnits == null ? List.of() : blueArmyUnits);
        redUnits.setAll(redArmyUnits == null ? List.of() : redArmyUnits);
        ArmyListStateService.refreshArmyOrdering(blueUnits);
        ArmyListStateService.refreshArmyOrdering(redUnits);
        battleModeLabel.setText(battleMode.title());
        setModeVisibility();
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
        logAllocation(result);
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
        AutoBattleResolution resolution = autoBattleService.resolve(
                battleMode, activeTurnPlayer, attackerSide, attackerVm.getUnit(), defenderVm.getUnit(), selectedWeapon,
                buildContext(attackerSide, bearerCount == null ? 0 : bearerCount), fightPhaseState
        );
        if (!resolution.resolved()) { DialogHelper.showWarning("Attack Not Resolved", resolution.failureMessage()); return; }
        if (battleMode == AutoBattleMode.FIGHT) lastFightPlayer = attackerSide;
        currentPendingSession = resolution.allocationSession() != null && resolution.allocationSession().hasPendingDamage() ? resolution.allocationSession() : null;
        logResolution(attackerSide, attackerVm.getUnitName(), defenderVm.getUnitName(), resolution);
        refreshUi();
    }

    private void onUnitSelected(Player side, GameArmyUnitVM selectedVm) {
        if (side == Player.ATTACKER) {
            loadWeapons(selectedVm, blueWeaponCombo, blueBearerCountSpinner, blueWeaponHintLabel, blueExtraAttackLabel);
            loadFightFlags(selectedVm, blueEligibleFightCheckBox, blueChargedCheckBox);
        } else {
            loadWeapons(selectedVm, redWeaponCombo, redBearerCountSpinner, redWeaponHintLabel, redExtraAttackLabel);
            loadFightFlags(selectedVm, redEligibleFightCheckBox, redChargedCheckBox);
        }
    }

    private void refreshUi() {
        ArmyListStateService.refreshArmyOrdering(blueUnits);
        ArmyListStateService.refreshArmyOrdering(redUnits);
        refreshFightState();
        updatePhaseText();
        updateActionButtons();
        updatePendingDamageUi();
        loadWeapons(blueUnitList.getSelectionModel().getSelectedItem(), blueWeaponCombo, blueBearerCountSpinner, blueWeaponHintLabel, blueExtraAttackLabel);
        loadWeapons(redUnitList.getSelectionModel().getSelectedItem(), redWeaponCombo, redBearerCountSpinner, redWeaponHintLabel, redExtraAttackLabel);
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
        String active = label(activeTurnPlayer);
        if (battleMode == AutoBattleMode.SHOOTING) {
            phaseStatusLabel.setText("Current Phase: " + currentPhase.name() + " | Active Turn: " + active + " | Only " + active + " may attack.");
        } else if (battleMode == AutoBattleMode.REACTION_SHOOTING) {
            phaseStatusLabel.setText("Current Phase: " + currentPhase.name() + " | Active Turn: " + active + " | " + label(opposite(activeTurnPlayer)) + " may resolve reaction shooting.");
        } else {
            phaseStatusLabel.setText("Current Phase: " + currentPhase.name() + " | Active Turn: " + active + " | Follow the fight order below.");
        }
    }

    private void updateActionButtons() {
        if (hasPendingDamage()) {
            blueAttackButton.setDisable(true);
            redAttackButton.setDisable(true);
            return;
        }
        if (battleMode == AutoBattleMode.SHOOTING) {
            blueAttackButton.setDisable(activeTurnPlayer != Player.ATTACKER);
            redAttackButton.setDisable(activeTurnPlayer != Player.DEFENDER);
            return;
        }
        if (battleMode == AutoBattleMode.REACTION_SHOOTING) {
            blueAttackButton.setDisable(activeTurnPlayer == Player.ATTACKER);
            redAttackButton.setDisable(activeTurnPlayer == Player.DEFENDER);
            return;
        }
        if (fightPhaseState.step() == FightStep.COMPLETE) {
            blueAttackButton.setDisable(true);
            redAttackButton.setDisable(true);
        } else if (fightPhaseState.nextPlayer() == null) {
            blueAttackButton.setDisable(false);
            redAttackButton.setDisable(false);
        } else {
            blueAttackButton.setDisable(fightPhaseState.nextPlayer() != Player.ATTACKER);
            redAttackButton.setDisable(fightPhaseState.nextPlayer() != Player.DEFENDER);
        }
    }

    private void updatePendingDamageUi() {
        allocationModelList.getItems().clear();
        if (!hasPendingDamage()) {
            pendingDamageStatusLabel.setText("No pending damage. Resolve a new attack to generate wounds.");
            applyPendingDamageButton.setDisable(true);
            return;
        }
        List<UnitModelInstance> aliveModels = currentPendingSession.defender().getModels().stream().filter(m -> m != null && !m.isDestroyed()).collect(Collectors.toList());
        allocationModelList.getItems().setAll(aliveModels);
        if (!aliveModels.isEmpty()) {
            allocationModelList.getSelectionModel().selectFirst();
        }
        PendingDamage currentDamage = currentPendingSession.currentDamage();
        pendingDamageStatusLabel.setText(
                "Defender: " + currentPendingSession.defenderUnitName()
                        + " | Pending unsaved attacks: " + currentPendingSession.pendingDamageCount()
                        + " | Current damage: " + (currentDamage == null ? 0 : currentDamage.damage())
                        + " | Total pending damage: " + currentPendingSession.totalPendingDamage()
                        + " | Select one living model below, then click Apply Pending Damage."
        );
        applyPendingDamageButton.setDisable(aliveModels.isEmpty());
    }

    private void loadWeapons(GameArmyUnitVM vm, ComboBox<WeaponProfile> comboBox, Spinner<Integer> spinner, Label hintLabel, Label extraAttackLabel) {
        comboBox.getItems().clear();
        extraAttackLabel.setText("");
        spinner.setDisable(battleMode == AutoBattleMode.FIGHT);
        if (vm == null) { hintLabel.setText("Select a unit first."); syncSpinnerToWeapon(spinner, null); return; }
        List<WeaponProfile> weapons = autoBattleService.availableWeapons(battleMode, vm.getUnit());
        comboBox.getItems().setAll(weapons);
        if (!weapons.isEmpty()) { comboBox.getSelectionModel().selectFirst(); syncSpinnerToWeapon(spinner, weapons.get(0)); } else { syncSpinnerToWeapon(spinner, null); }
        hintLabel.setText(hintText(weapons.isEmpty()));
        if (battleMode == AutoBattleMode.FIGHT) {
            List<WeaponProfile> extras = autoBattleService.extraAttackWeapons(vm.getUnit(), null);
            extraAttackLabel.setText(
                    extras.isEmpty()
                            ? "All melee weapon profiles on this unit will be resolved."
                            : "All melee weapon profiles on this unit will be resolved. Extra Attacks included: "
                            + extras.stream().map(w -> w.name() + " x" + w.count()).collect(Collectors.joining(", "))
            );
        }
    }

    private void loadFightFlags(GameArmyUnitVM vm, CheckBox eligibleBox, CheckBox chargedBox) {
        if (vm == null || battleMode != AutoBattleMode.FIGHT) {
            eligibleBox.setSelected(false);
            chargedBox.setSelected(false);
            return;
        }
        eligibleBox.setSelected(vm.getUnit().isEligibleToFightThisPhase());
        chargedBox.setSelected(vm.getUnit().hasChargedThisTurn());
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

    private void logResolution(Player attackerSide, String attackerUnitName, String defenderUnitName, AutoBattleResolution resolution) {
        StringBuilder battle = new StringBuilder();
        battle.append("==================================================\n").append(label(attackerSide)).append(" | ").append(battleMode.attackLabel()).append("\n").append("Attacker Unit: ").append(attackerUnitName).append("\n").append("Target Unit: ").append(defenderUnitName).append("\n\n");
        int totalPendingCount = 0;
        int totalPendingDamage = 0;
        for (ResolvedAttack attack : resolution.attacks()) {
            AttackResult result = attack.result();
            totalPendingCount += result.pendingDamages().size();
            totalPendingDamage += result.pendingDamages().stream().mapToInt(PendingDamage::damage).sum();
            battle.append(attack.label()).append(" - ").append(attack.weaponName()).append("\n").append("Attacks: ").append(result.attacks()).append("\n").append("Hits: ").append(result.hits()).append("\n").append("Wounds: ").append(result.wounds()).append("\n").append("Unsaved Attacks: ").append(result.unsaved()).append("\n").append("Pending Damage: ").append(result.totalDamage()).append("\n");
            if (!result.notes().isEmpty()) {
                battle.append("Notes:\n");
                for (String note : result.notes()) battle.append("- ").append(note).append("\n");
            }
            battle.append("\n");
            rollLogBox.appendText("==================================================\n" + attack.label() + " - " + attack.weaponName() + "\n");
            for (String line : result.rollLog()) rollLogBox.appendText(line + "\n");
            rollLogBox.appendText("\n");

            StringBuilder summary = new StringBuilder();
            summary.append(label(attackerSide))
                    .append(" ")
                    .append(attackerUnitName)
                    .append(" resolved ")
                    .append(attack.label())
                    .append(" with ")
                    .append(attack.weaponName())
                    .append(" against ")
                    .append(defenderUnitName)
                    .append(": attacks ")
                    .append(result.attacks())
                    .append(", hits ")
                    .append(result.hits())
                    .append(", wounds ")
                    .append(result.wounds())
                    .append(", unsaved ")
                    .append(result.unsaved())
                    .append(", pending damage ")
                    .append(result.totalDamage())
                    .append(".");
            if (!result.notes().isEmpty()) {
                summary.append(" Notes: ").append(String.join(" ", result.notes()));
            }
            battleLogService.logTurnEvent(currentRound, currentPhase, attackerSide, summary.toString());
        }
        if (totalPendingCount > 0) {
            battle.append("Pending Allocation:\n- Unsaved attacks waiting for defender choice: ").append(totalPendingCount).append("\n- Total pending damage across those attacks: ").append(totalPendingDamage).append("\n\n");
        } else {
            battle.append("No unsaved damage was generated.\n\n");
        }
        if (resolution.hazardousTriggered()) battle.append("Hazardous: resolve the Hazardous test manually after this attack sequence.\n\n");
        battleResultBox.appendText(battle.toString());
    }

    private void logAllocation(PendingDamageStepResult result) {
        StringBuilder battle = new StringBuilder();
        CasualtyUpdate casualty = result.casualtyUpdate();
        battle.append("Allocation Result\n");
        if (result.resolvedDamage() != null) battle.append("- Source: ").append(result.resolvedDamage().displayLabel()).append("\n");
        battle.append("- Target Model: ").append(result.targetModelName()).append("\n").append("- Applied Damage: ").append(result.appliedDamage()).append("\n");
        if (result.wastedDamage() > 0) battle.append("- Overflow Lost: ").append(result.wastedDamage()).append("\n");
        if (result.targetDestroyed()) battle.append("- Target Model Status: Destroyed\n");
        if (casualty.newlyDestroyedModels() > 0) {
            battle.append("- Destroyed Models Finalized: ").append(String.join(", ", casualty.destroyedModelNames())).append("\n");
        }
        if (!casualty.removedWeaponNames().isEmpty()) {
            battle.append("- Weapon Count Changes: ").append(String.join(", ", casualty.removedWeaponNames())).append("\n");
        }
        if (result.sessionComplete()) {
            if (casualty.defenderDestroyed()) battle.append("- Defender Unit Status: Destroyed and moved to the bottom of the army list.\n");
        } else {
            battle.append("- Remaining Pending Unsaved Attacks: ").append(result.remainingPendingCount()).append("\n").append("- Remaining Pending Damage: ").append(result.remainingPendingDamage()).append("\n");
        }
        battle.append("\n");
        battleResultBox.appendText(battle.toString());

        if (currentPendingSession != null) {
            StringBuilder summary = new StringBuilder();
            summary.append(label(opposite(currentPendingSession.attackingPlayer())))
                    .append(" allocated ")
                    .append(result.resolvedDamage() == null ? "pending damage" : result.resolvedDamage().displayLabel())
                    .append(" to ")
                    .append(result.targetModelName())
                    .append(": applied ")
                    .append(result.appliedDamage())
                    .append(" damage");
            if (result.wastedDamage() > 0) {
                summary.append(", ").append(result.wastedDamage()).append(" overflow lost");
            }
            summary.append(".");
            if (result.targetDestroyed()) {
                summary.append(" Target model destroyed.");
            }
            if (casualty.newlyDestroyedModels() > 0) {
                summary.append(" Destroyed models finalized: ")
                        .append(String.join(", ", casualty.destroyedModelNames()))
                        .append(".");
            }
            if (casualty.defenderDestroyed()) {
                summary.append(" Defender unit destroyed.");
            } else if (!result.sessionComplete()) {
                summary.append(" Remaining pending attacks: ")
                        .append(result.remainingPendingCount())
                        .append(", remaining pending damage: ")
                        .append(result.remainingPendingDamage())
                        .append(".");
            }
            battleLogService.logTurnEvent(currentRound, currentPhase, opposite(currentPendingSession.attackingPlayer()), summary.toString());
        }
    }

    private boolean hasPendingDamage() { return currentPendingSession != null && currentPendingSession.hasPendingDamage(); }
    private void handleArmyStateChanged(Player side) { refreshUi(); }
    private void syncSpinnerToWeapon(Spinner<Integer> spinner, WeaponProfile weapon) {
        int max = weapon == null ? 1 : Math.max(1, weapon.count());
        spinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(1, max, max));
    }
    private void setModeVisibility() { boolean ranged = battleMode.usesRangedWeapons(); shootingRulesBox.setManaged(ranged); shootingRulesBox.setVisible(ranged); fightRulesBox.setManaged(!ranged); fightRulesBox.setVisible(!ranged); blueFightBox.setManaged(!ranged); blueFightBox.setVisible(!ranged); redFightBox.setManaged(!ranged); redFightBox.setVisible(!ranged); }
    private void configureWeaponCombo(ComboBox<WeaponProfile> comboBox) { comboBox.setCellFactory(v -> new ListCell<>() { @Override protected void updateItem(WeaponProfile item, boolean empty) { super.updateItem(item, empty); setText(empty || item == null ? null : formatWeapon(item)); }}); comboBox.setButtonCell(new ListCell<>() { @Override protected void updateItem(WeaponProfile item, boolean empty) { super.updateItem(item, empty); setText(empty || item == null ? null : formatWeapon(item)); }}); }
    private void configureModelList() {
        allocationModelList.setCellFactory(v -> new ListCell<>() {
            @Override
            protected void updateItem(UnitModelInstance item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : formatModel(item));
            }
        });
        allocationModelList.setPlaceholder(new Label("No living model is available to receive damage."));
    }
    private List<UnitInstance> blueUnitInstances() { return blueUnits.stream().map(GameArmyUnitVM::getUnit).collect(Collectors.toList()); }
    private List<UnitInstance> redUnitInstances() { return redUnits.stream().map(GameArmyUnitVM::getUnit).collect(Collectors.toList()); }
    private Player opposite(Player player) { return player == Player.ATTACKER ? Player.DEFENDER : Player.ATTACKER; }
    private String label(Player player) { return player == Player.ATTACKER ? "Attacker" : "Defender"; }
    private String blank(String value, String fallback) { return value == null || value.isBlank() ? fallback : value; }
    private String safe(String value) { return value == null ? "" : value; }
    private String formatWeapon(WeaponProfile weapon) { return weapon.name() + " | Range " + safe(weapon.range()) + " | A " + safe(weapon.a()) + " | " + (weapon.melee() ? "WS " : "BS ") + safe(weapon.skill()) + " | S " + safe(weapon.s()) + " | AP " + safe(weapon.ap()) + " | D " + safe(weapon.d()) + " | available x" + weapon.count(); }
    private String formatModel(UnitModelInstance model) { return model.getModelName() + " | HP " + model.getCurrentHp() + "/" + model.getMaxHp(); }
    private String hintText(boolean noWeapons) {
        if (battleMode == AutoBattleMode.SHOOTING) return noWeapons ? "No ranged weapons remain available for this shooting phase." : "Select one ranged weapon profile. The spinner defaults to the maximum available equipped weapons, and any unused quantity can attack later.";
        if (battleMode == AutoBattleMode.REACTION_SHOOTING) return noWeapons ? "This unit has no ranged weapon available for reaction shooting." : "Select one ranged weapon profile for reaction fire. The spinner defaults to the maximum available equipped weapons, and any unused quantity can attack later.";
        return noWeapons ? "No melee weapon is available for this unit." : "All melee weapon profiles on this unit will be resolved in this fight, then the defender allocates each unsaved hit.";
    }
}
