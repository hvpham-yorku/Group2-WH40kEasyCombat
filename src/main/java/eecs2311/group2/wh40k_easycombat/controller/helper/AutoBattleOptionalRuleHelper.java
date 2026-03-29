package eecs2311.group2.wh40k_easycombat.controller.helper;

import eecs2311.group2.wh40k_easycombat.model.editor.EditorActiveEffect;
import eecs2311.group2.wh40k_easycombat.model.editor.EditorRuleDefinition;
import eecs2311.group2.wh40k_easycombat.model.instance.Phase;
import eecs2311.group2.wh40k_easycombat.model.instance.Player;
import eecs2311.group2.wh40k_easycombat.model.instance.UnitInstance;
import eecs2311.group2.wh40k_easycombat.model.instance.WeaponProfile;
import eecs2311.group2.wh40k_easycombat.service.BattleLogService;
import eecs2311.group2.wh40k_easycombat.service.autobattle.AutoBattleMode;
import eecs2311.group2.wh40k_easycombat.service.editor.EditorEffectRuntimeService;
import eecs2311.group2.wh40k_easycombat.service.editor.EditorRuleApplicationService;
import eecs2311.group2.wh40k_easycombat.viewmodel.GameArmyUnitVM;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.layout.FlowPane;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;

public final class AutoBattleOptionalRuleHelper {

    private AutoBattleOptionalRuleHelper() {
    }

    public static void refreshOptionalRulePane(
            FlowPane pane,
            Label hintLabel,
            Player side,
            GameArmyUnitVM attackerVm,
            GameArmyUnitVM defenderVm,
            WeaponProfile weapon,
            AutoBattleMode battleMode,
            EditorRuleApplicationService editorRuleApplicationService,
            EditorEffectRuntimeService editorEffectRuntimeService,
            Map<Player, Set<String>> pendingOptionalSelections,
            BiConsumer<String, Boolean> selectionUpdater
    ) {
        pane.getChildren().clear();

        if (attackerVm == null || defenderVm == null || weapon == null) {
            hintLabel.setText("Select a unit, weapon and target to see optional VM rules for this attack.");
            pendingOptionalSelections.get(side).clear();
            return;
        }

        List<OptionalRuleView> optionalRules = optionalRuleViewsForAttack(
                editorRuleApplicationService,
                editorEffectRuntimeService,
                battleMode,
                attackerVm.getUnit(),
                defenderVm.getUnit(),
                weapon
        );
        if (optionalRules.isEmpty()) {
            hintLabel.setText("No optional VM rules match the currently selected attack.");
            pendingOptionalSelections.get(side).clear();
            return;
        }

        Set<String> selectedIds = pendingOptionalSelections.get(side);
        Set<String> allowedIds = optionalRules.stream()
                .map(view -> view.rule().getId())
                .collect(Collectors.toCollection(LinkedHashSet::new));
        selectedIds.retainAll(allowedIds);

        for (OptionalRuleView view : optionalRules) {
            CheckBox checkBox = new CheckBox(optionalRuleLabel(view));
            checkBox.setWrapText(true);
            checkBox.getStyleClass().add("game-checkbox");
            checkBox.setDisable(view.active());
            checkBox.setSelected(view.active() || selectedIds.contains(view.rule().getId()));
            checkBox.selectedProperty().addListener((obs, oldValue, newValue) ->
                    selectionUpdater.accept(view.rule().getId(), newValue));
            pane.getChildren().add(checkBox);
        }

        hintLabel.setText("Select any optional rules you want to activate before resolving this attack. Active rules stay on the unit until their duration expires.");
    }

    public static List<OptionalRuleView> optionalRuleViewsForAttack(
            EditorRuleApplicationService editorRuleApplicationService,
            EditorEffectRuntimeService editorEffectRuntimeService,
            AutoBattleMode battleMode,
            UnitInstance attacker,
            UnitInstance defender,
            WeaponProfile weapon
    ) {
        List<OptionalRuleView> views = new ArrayList<>();
        for (EditorRuleDefinition rule : editorRuleApplicationService.matchingOptionalRules(battleMode, attacker, defender, weapon)) {
            boolean active = editorEffectRuntimeService.isRuleActiveForAttack(rule, attacker, defender);
            views.add(new OptionalRuleView(rule, active));
        }
        return views;
    }

    public static List<EditorRuleDefinition> selectedOptionalRulesForAttack(
            Player side,
            UnitInstance attacker,
            UnitInstance defender,
            WeaponProfile weapon,
            AutoBattleMode battleMode,
            EditorRuleApplicationService editorRuleApplicationService,
            EditorEffectRuntimeService editorEffectRuntimeService,
            Map<Player, Set<String>> pendingOptionalSelections
    ) {
        Set<String> selectedIds = pendingOptionalSelections.get(side);
        if (selectedIds == null || selectedIds.isEmpty()) {
            return List.of();
        }

        List<EditorRuleDefinition> selectedRules = new ArrayList<>();
        for (OptionalRuleView view : optionalRuleViewsForAttack(
                editorRuleApplicationService,
                editorEffectRuntimeService,
                battleMode,
                attacker,
                defender,
                weapon
        )) {
            if (!view.active() && selectedIds.contains(view.rule().getId())) {
                selectedRules.add(view.rule());
            }
        }
        return List.copyOf(selectedRules);
    }

    public static void updateOptionalSelection(
            Map<Player, Set<String>> pendingOptionalSelections,
            Player side,
            String ruleId,
            boolean selected,
            boolean alreadyActive
    ) {
        if (alreadyActive || ruleId == null || ruleId.isBlank()) {
            return;
        }

        Set<String> selections = pendingOptionalSelections.get(side);
        if (selected) {
            selections.add(ruleId);
        } else {
            selections.remove(ruleId);
        }
    }

    public static void clearAppliedOptionalSelections(
            Map<Player, Set<String>> pendingOptionalSelections,
            Player side,
            List<EditorActiveEffect> activatedEffects
    ) {
        if (activatedEffects == null || activatedEffects.isEmpty()) {
            return;
        }

        Set<String> selections = pendingOptionalSelections.get(side);
        for (EditorActiveEffect effect : activatedEffects) {
            if (effect != null) {
                selections.remove(effect.ruleId());
            }
        }
    }

    public static void logOptionalRuleActivations(
            BattleLogService battleLogService,
            javafx.scene.control.TextArea battleResultBox,
            int currentRound,
            Phase currentPhase,
            Player side,
            String sideLabel,
            String attackerUnitName,
            List<EditorActiveEffect> activatedEffects
    ) {
        if (activatedEffects == null || activatedEffects.isEmpty()) {
            return;
        }

        String labels = activatedEffects.stream()
                .map(EditorActiveEffect::displayName)
                .collect(Collectors.joining(", "));
        battleResultBox.appendText("Optional VM rules activated: " + labels + "\n\n");
        battleLogService.logTurnEvent(
                currentRound,
                currentPhase,
                side,
                sideLabel + " activated optional VM rules for " + attackerUnitName + ": " + labels + "."
        );
    }

    private static String optionalRuleLabel(OptionalRuleView view) {
        StringBuilder label = new StringBuilder(blank(view.rule().getName(), "Custom Rule"));
        String weaponFilter = blank(view.rule().getWeaponNameContains(), "");
        if (!weaponFilter.isBlank()) {
            label.append(" | weapon: ").append(weaponFilter);
        }
        if (view.active()) {
            label.append(" (Active)");
        }
        return label.toString();
    }

    private static String blank(String value, String fallback) {
        return value == null || value.isBlank() ? fallback : value;
    }

    public record OptionalRuleView(EditorRuleDefinition rule, boolean active) {
    }
}
