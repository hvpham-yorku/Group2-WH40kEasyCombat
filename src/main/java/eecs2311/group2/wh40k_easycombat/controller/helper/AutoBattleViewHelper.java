package eecs2311.group2.wh40k_easycombat.controller.helper;

import eecs2311.group2.wh40k_easycombat.model.combat.FightPhaseState;
import eecs2311.group2.wh40k_easycombat.model.combat.PendingDamage;
import eecs2311.group2.wh40k_easycombat.model.instance.Phase;
import eecs2311.group2.wh40k_easycombat.model.instance.Player;
import eecs2311.group2.wh40k_easycombat.model.instance.UnitModelInstance;
import eecs2311.group2.wh40k_easycombat.model.instance.WeaponProfile;
import eecs2311.group2.wh40k_easycombat.service.autobattle.AutoBattleMode;
import eecs2311.group2.wh40k_easycombat.service.autobattle.FightStep;
import eecs2311.group2.wh40k_easycombat.service.autobattle.PendingDamageSession;
import eecs2311.group2.wh40k_easycombat.service.autobattle.AutoBattleService;
import eecs2311.group2.wh40k_easycombat.viewmodel.GameArmyUnitVM;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory;
import javafx.scene.layout.VBox;

import java.util.List;
import java.util.stream.Collectors;

public final class AutoBattleViewHelper {

    private AutoBattleViewHelper() {
    }

    public static void updatePhaseText(
            Label phaseStatusLabel,
            AutoBattleMode battleMode,
            Phase currentPhase,
            Player activeTurnPlayer
    ) {
        String active = label(activeTurnPlayer);
        if (battleMode == AutoBattleMode.SHOOTING) {
            phaseStatusLabel.setText("Current Phase: " + currentPhase.name() + " | Active Turn: " + active + " | Only " + active + " may attack.");
        } else if (battleMode == AutoBattleMode.REACTION_SHOOTING) {
            phaseStatusLabel.setText("Current Phase: " + currentPhase.name() + " | Active Turn: " + active + " | " + label(opposite(activeTurnPlayer)) + " may resolve reaction shooting.");
        } else {
            phaseStatusLabel.setText("Current Phase: " + currentPhase.name() + " | Active Turn: " + active + " | Follow the fight order below.");
        }
    }

    public static void updateActionButtons(
            javafx.scene.control.Button blueAttackButton,
            javafx.scene.control.Button redAttackButton,
            AutoBattleMode battleMode,
            Player activeTurnPlayer,
            FightPhaseState fightPhaseState,
            boolean hasPendingDamage
    ) {
        if (hasPendingDamage) {
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

    public static void updatePendingDamageUi(
            ListView<UnitModelInstance> allocationModelList,
            Label pendingDamageStatusLabel,
            javafx.scene.control.Button applyPendingDamageButton,
            PendingDamageSession currentPendingSession
    ) {
        allocationModelList.getItems().clear();
        if (currentPendingSession == null || !currentPendingSession.hasPendingDamage()) {
            pendingDamageStatusLabel.setText("No pending damage. Resolve a new attack to generate wounds.");
            applyPendingDamageButton.setDisable(true);
            return;
        }
        List<UnitModelInstance> aliveModels = currentPendingSession.defender().getModels().stream()
                .filter(m -> m != null && !m.isDestroyed())
                .collect(Collectors.toList());
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

    public static void loadWeapons(
            AutoBattleService autoBattleService,
            AutoBattleMode battleMode,
            GameArmyUnitVM vm,
            ComboBox<WeaponProfile> comboBox,
            Spinner<Integer> spinner,
            Label hintLabel,
            Label extraAttackLabel
    ) {
        comboBox.getItems().clear();
        extraAttackLabel.setText("");
        spinner.setDisable(battleMode == AutoBattleMode.FIGHT);
        if (vm == null) {
            hintLabel.setText("Select a unit first.");
            syncSpinnerToWeapon(spinner, null);
            return;
        }
        List<WeaponProfile> weapons = autoBattleService.availableWeapons(battleMode, vm.getUnit());
        comboBox.getItems().setAll(weapons);
        if (!weapons.isEmpty()) {
            comboBox.getSelectionModel().selectFirst();
            syncSpinnerToWeapon(spinner, weapons.get(0));
        } else {
            syncSpinnerToWeapon(spinner, null);
        }
        hintLabel.setText(hintText(battleMode, weapons.isEmpty()));
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

    public static void loadFightFlags(
            AutoBattleMode battleMode,
            GameArmyUnitVM vm,
            CheckBox eligibleBox,
            CheckBox chargedBox
    ) {
        if (vm == null || battleMode != AutoBattleMode.FIGHT) {
            eligibleBox.setSelected(false);
            chargedBox.setSelected(false);
            return;
        }
        eligibleBox.setSelected(vm.getUnit().isEligibleToFightThisPhase());
        chargedBox.setSelected(vm.getUnit().hasChargedThisTurn());
    }

    public static void syncSpinnerToWeapon(Spinner<Integer> spinner, WeaponProfile weapon) {
        int max = weapon == null ? 1 : Math.max(1, weapon.count());
        spinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(1, max, max));
    }

    public static void setModeVisibility(
            AutoBattleMode battleMode,
            VBox shootingRulesBox,
            VBox fightRulesBox,
            VBox blueFightBox,
            VBox redFightBox
    ) {
        boolean ranged = battleMode.usesRangedWeapons();
        shootingRulesBox.setManaged(ranged);
        shootingRulesBox.setVisible(ranged);
        fightRulesBox.setManaged(!ranged);
        fightRulesBox.setVisible(!ranged);
        blueFightBox.setManaged(!ranged);
        blueFightBox.setVisible(!ranged);
        redFightBox.setManaged(!ranged);
        redFightBox.setVisible(!ranged);
    }

    public static void configureWeaponCombo(ComboBox<WeaponProfile> comboBox) {
        comboBox.setCellFactory(v -> new ListCell<>() {
            @Override
            protected void updateItem(WeaponProfile item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : formatWeapon(item));
            }
        });
        comboBox.setButtonCell(new ListCell<>() {
            @Override
            protected void updateItem(WeaponProfile item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : formatWeapon(item));
            }
        });
    }

    public static void configureModelList(ListView<UnitModelInstance> allocationModelList) {
        allocationModelList.setCellFactory(v -> new ListCell<>() {
            @Override
            protected void updateItem(UnitModelInstance item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : formatModel(item));
            }
        });
        allocationModelList.setPlaceholder(new Label("No living model is available to receive damage."));
    }

    public static String label(Player player) {
        return player == Player.ATTACKER ? "Attacker" : "Defender";
    }

    public static Player opposite(Player player) {
        return player == Player.ATTACKER ? Player.DEFENDER : Player.ATTACKER;
    }

    private static String safe(String value) {
        return value == null ? "" : value;
    }

    private static String formatWeapon(WeaponProfile weapon) {
        return weapon.name() + " | Range " + safe(weapon.range()) + " | A " + safe(weapon.a())
                + " | " + (weapon.melee() ? "WS " : "BS ") + safe(weapon.skill())
                + " | S " + safe(weapon.s()) + " | AP " + safe(weapon.ap())
                + " | D " + safe(weapon.d()) + " | available x" + weapon.count();
    }

    private static String formatModel(UnitModelInstance model) {
        return model.getModelName() + " | HP " + model.getCurrentHp() + "/" + model.getMaxHp();
    }

    private static String hintText(AutoBattleMode battleMode, boolean noWeapons) {
        if (battleMode == AutoBattleMode.SHOOTING) {
            return noWeapons
                    ? "No ranged weapons remain available for this shooting phase."
                    : "Select one ranged weapon profile. The spinner defaults to the maximum available equipped weapons, and any unused quantity can attack later.";
        }
        if (battleMode == AutoBattleMode.REACTION_SHOOTING) {
            return noWeapons
                    ? "This unit has no ranged weapon available for reaction shooting."
                    : "Select one ranged weapon profile for reaction fire. The spinner defaults to the maximum available equipped weapons, and any unused quantity can attack later.";
        }
        return noWeapons
                ? "No melee weapon is available for this unit."
                : "All melee weapon profiles on this unit will be resolved in this fight, then the defender allocates each unsaved hit.";
    }
}
