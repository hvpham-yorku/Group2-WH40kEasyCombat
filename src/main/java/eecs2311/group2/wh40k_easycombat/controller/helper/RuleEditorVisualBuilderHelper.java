package eecs2311.group2.wh40k_easycombat.controller.helper;

import eecs2311.group2.wh40k_easycombat.model.editor.EditorRerollType;
import eecs2311.group2.wh40k_easycombat.model.editor.EditorRuleDefinition;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory;
import javafx.scene.control.TextField;

public final class RuleEditorVisualBuilderHelper {

    private RuleEditorVisualBuilderHelper() {
    }

    public static void loadVisualBuilder(EditorRuleDefinition rule, VisualControls controls) {
        controls.visualWithinHalfRangeCheckBox().setSelected(rule.isVisualWithinHalfRange());
        controls.visualRemainedStationaryCheckBox().setSelected(rule.isVisualRemainedStationary());
        controls.visualAdvancedThisTurnCheckBox().setSelected(rule.isVisualAdvancedThisTurn());
        controls.visualFellBackThisTurnCheckBox().setSelected(rule.isVisualFellBackThisTurn());
        controls.visualChargedThisTurnCheckBox().setSelected(rule.isVisualChargedThisTurn());
        controls.visualAttackerCanFightCheckBox().setSelected(rule.isVisualAttackerCanFight());
        controls.visualTargetHasCoverCheckBox().setSelected(rule.isVisualTargetHasCover());
        controls.visualBlastIsLegalCheckBox().setSelected(rule.isVisualBlastIsLegal());
        controls.visualTargetInfantryCheckBox().setSelected(rule.isVisualTargetIsInfantry());
        controls.visualTargetVehicleCheckBox().setSelected(rule.isVisualTargetIsVehicle());
        controls.visualTargetMonsterCheckBox().setSelected(rule.isVisualTargetIsMonster());
        controls.visualTargetCharacterCheckBox().setSelected(rule.isVisualTargetIsCharacter());
        controls.visualTargetPsykerCheckBox().setSelected(rule.isVisualTargetIsPsyker());
        controls.visualHitModifierSpinner().getValueFactory().setValue(rule.getVisualHitModifier());
        controls.visualWoundModifierSpinner().getValueFactory().setValue(rule.getVisualWoundModifier());
        controls.visualAttacksModifierSpinner().getValueFactory().setValue(rule.getVisualAttacksModifier());
        controls.visualDamageModifierSpinner().getValueFactory().setValue(rule.getVisualDamageModifier());
        controls.visualApModifierSpinner().getValueFactory().setValue(rule.getVisualApModifier());
        controls.visualHitRerollComboBox().setValue(rule.getVisualHitReroll());
        controls.visualWoundRerollComboBox().setValue(rule.getVisualWoundReroll());
        controls.visualKeywordsField().setText(rule.getVisualExtraWeaponKeywords());
    }

    public static void readVisualBuilder(EditorRuleDefinition rule, VisualControls controls) {
        rule.setVisualWithinHalfRange(controls.visualWithinHalfRangeCheckBox().isSelected());
        rule.setVisualRemainedStationary(controls.visualRemainedStationaryCheckBox().isSelected());
        rule.setVisualAdvancedThisTurn(controls.visualAdvancedThisTurnCheckBox().isSelected());
        rule.setVisualFellBackThisTurn(controls.visualFellBackThisTurnCheckBox().isSelected());
        rule.setVisualChargedThisTurn(controls.visualChargedThisTurnCheckBox().isSelected());
        rule.setVisualAttackerCanFight(controls.visualAttackerCanFightCheckBox().isSelected());
        rule.setVisualTargetHasCover(controls.visualTargetHasCoverCheckBox().isSelected());
        rule.setVisualBlastIsLegal(controls.visualBlastIsLegalCheckBox().isSelected());
        rule.setVisualTargetIsInfantry(controls.visualTargetInfantryCheckBox().isSelected());
        rule.setVisualTargetIsVehicle(controls.visualTargetVehicleCheckBox().isSelected());
        rule.setVisualTargetIsMonster(controls.visualTargetMonsterCheckBox().isSelected());
        rule.setVisualTargetIsCharacter(controls.visualTargetCharacterCheckBox().isSelected());
        rule.setVisualTargetIsPsyker(controls.visualTargetPsykerCheckBox().isSelected());
        rule.setVisualHitModifier(spinnerValue(controls.visualHitModifierSpinner()));
        rule.setVisualWoundModifier(spinnerValue(controls.visualWoundModifierSpinner()));
        rule.setVisualAttacksModifier(spinnerValue(controls.visualAttacksModifierSpinner()));
        rule.setVisualDamageModifier(spinnerValue(controls.visualDamageModifierSpinner()));
        rule.setVisualApModifier(spinnerValue(controls.visualApModifierSpinner()));
        rule.setVisualHitReroll(controls.visualHitRerollComboBox().getValue());
        rule.setVisualWoundReroll(controls.visualWoundRerollComboBox().getValue());
        rule.setVisualExtraWeaponKeywords(controls.visualKeywordsField().getText());
    }

    public static void wireVisualBuilderPreview(VisualControls controls, Runnable refreshGeneratedScriptPreview) {
        controls.visualWithinHalfRangeCheckBox().selectedProperty().addListener((obs, oldValue, newValue) -> refreshGeneratedScriptPreview.run());
        controls.visualRemainedStationaryCheckBox().selectedProperty().addListener((obs, oldValue, newValue) -> refreshGeneratedScriptPreview.run());
        controls.visualAdvancedThisTurnCheckBox().selectedProperty().addListener((obs, oldValue, newValue) -> refreshGeneratedScriptPreview.run());
        controls.visualFellBackThisTurnCheckBox().selectedProperty().addListener((obs, oldValue, newValue) -> refreshGeneratedScriptPreview.run());
        controls.visualChargedThisTurnCheckBox().selectedProperty().addListener((obs, oldValue, newValue) -> refreshGeneratedScriptPreview.run());
        controls.visualAttackerCanFightCheckBox().selectedProperty().addListener((obs, oldValue, newValue) -> refreshGeneratedScriptPreview.run());
        controls.visualTargetHasCoverCheckBox().selectedProperty().addListener((obs, oldValue, newValue) -> refreshGeneratedScriptPreview.run());
        controls.visualBlastIsLegalCheckBox().selectedProperty().addListener((obs, oldValue, newValue) -> refreshGeneratedScriptPreview.run());
        controls.visualTargetInfantryCheckBox().selectedProperty().addListener((obs, oldValue, newValue) -> refreshGeneratedScriptPreview.run());
        controls.visualTargetVehicleCheckBox().selectedProperty().addListener((obs, oldValue, newValue) -> refreshGeneratedScriptPreview.run());
        controls.visualTargetMonsterCheckBox().selectedProperty().addListener((obs, oldValue, newValue) -> refreshGeneratedScriptPreview.run());
        controls.visualTargetCharacterCheckBox().selectedProperty().addListener((obs, oldValue, newValue) -> refreshGeneratedScriptPreview.run());
        controls.visualTargetPsykerCheckBox().selectedProperty().addListener((obs, oldValue, newValue) -> refreshGeneratedScriptPreview.run());
        controls.visualKeywordsField().textProperty().addListener((obs, oldValue, newValue) -> refreshGeneratedScriptPreview.run());
        controls.visualHitRerollComboBox().valueProperty().addListener((obs, oldValue, newValue) -> refreshGeneratedScriptPreview.run());
        controls.visualWoundRerollComboBox().valueProperty().addListener((obs, oldValue, newValue) -> refreshGeneratedScriptPreview.run());
        controls.visualHitModifierSpinner().valueProperty().addListener((obs, oldValue, newValue) -> refreshGeneratedScriptPreview.run());
        controls.visualWoundModifierSpinner().valueProperty().addListener((obs, oldValue, newValue) -> refreshGeneratedScriptPreview.run());
        controls.visualAttacksModifierSpinner().valueProperty().addListener((obs, oldValue, newValue) -> refreshGeneratedScriptPreview.run());
        controls.visualDamageModifierSpinner().valueProperty().addListener((obs, oldValue, newValue) -> refreshGeneratedScriptPreview.run());
        controls.visualApModifierSpinner().valueProperty().addListener((obs, oldValue, newValue) -> refreshGeneratedScriptPreview.run());
    }

    public static void configureSpinner(Spinner<Integer> spinner, int min, int max, int value) {
        spinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(min, max, value));
        spinner.getStyleClass().add("game-spinner");
    }

    public static void resetVisualControls(VisualControls controls) {
        controls.visualWithinHalfRangeCheckBox().setSelected(false);
        controls.visualRemainedStationaryCheckBox().setSelected(false);
        controls.visualAdvancedThisTurnCheckBox().setSelected(false);
        controls.visualFellBackThisTurnCheckBox().setSelected(false);
        controls.visualChargedThisTurnCheckBox().setSelected(false);
        controls.visualAttackerCanFightCheckBox().setSelected(false);
        controls.visualTargetHasCoverCheckBox().setSelected(false);
        controls.visualBlastIsLegalCheckBox().setSelected(false);
        controls.visualTargetInfantryCheckBox().setSelected(false);
        controls.visualTargetVehicleCheckBox().setSelected(false);
        controls.visualTargetMonsterCheckBox().setSelected(false);
        controls.visualTargetCharacterCheckBox().setSelected(false);
        controls.visualTargetPsykerCheckBox().setSelected(false);
        controls.visualHitModifierSpinner().getValueFactory().setValue(0);
        controls.visualWoundModifierSpinner().getValueFactory().setValue(0);
        controls.visualAttacksModifierSpinner().getValueFactory().setValue(0);
        controls.visualDamageModifierSpinner().getValueFactory().setValue(0);
        controls.visualApModifierSpinner().getValueFactory().setValue(0);
        controls.visualHitRerollComboBox().setValue(EditorRerollType.NONE);
        controls.visualWoundRerollComboBox().setValue(EditorRerollType.NONE);
        controls.visualKeywordsField().clear();
    }

    private static int spinnerValue(Spinner<Integer> spinner) {
        Integer value = spinner.getValue();
        return value == null ? 0 : value;
    }

    public record VisualControls(
            CheckBox visualWithinHalfRangeCheckBox,
            CheckBox visualRemainedStationaryCheckBox,
            CheckBox visualAdvancedThisTurnCheckBox,
            CheckBox visualFellBackThisTurnCheckBox,
            CheckBox visualChargedThisTurnCheckBox,
            CheckBox visualAttackerCanFightCheckBox,
            CheckBox visualTargetHasCoverCheckBox,
            CheckBox visualBlastIsLegalCheckBox,
            CheckBox visualTargetInfantryCheckBox,
            CheckBox visualTargetVehicleCheckBox,
            CheckBox visualTargetMonsterCheckBox,
            CheckBox visualTargetCharacterCheckBox,
            CheckBox visualTargetPsykerCheckBox,
            Spinner<Integer> visualHitModifierSpinner,
            Spinner<Integer> visualWoundModifierSpinner,
            Spinner<Integer> visualAttacksModifierSpinner,
            Spinner<Integer> visualDamageModifierSpinner,
            Spinner<Integer> visualApModifierSpinner,
            ComboBox<EditorRerollType> visualHitRerollComboBox,
            ComboBox<EditorRerollType> visualWoundRerollComboBox,
            TextField visualKeywordsField
    ) {
    }
}
