package eecs2311.group2.wh40k_easycombat.controller.helper;

import eecs2311.group2.wh40k_easycombat.model.editor.EditorRuleAttackType;
import eecs2311.group2.wh40k_easycombat.model.editor.EditorRuleDefinition;
import eecs2311.group2.wh40k_easycombat.model.editor.EditorRuleDuration;
import eecs2311.group2.wh40k_easycombat.model.editor.EditorRulePhase;
import eecs2311.group2.wh40k_easycombat.model.editor.EditorRuleTargetRole;
import eecs2311.group2.wh40k_easycombat.model.editor.EditorRuleType;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;

public final class RuleEditorFormMapper {

    private RuleEditorFormMapper() {
    }

    public static EditorRuleDefinition readRuleFromForm(
            String selectedRuleId,
            RuleIdentityFields identityFields,
            RuleMatchFields matchFields,
            RuleStratagemFields stratagemFields
    ) {
        EditorRuleDefinition rule = new EditorRuleDefinition();
        rule.setId(selectedRuleId);
        rule.setName(safe(identityFields.nameField().getText()));
        rule.setType(identityFields.typeComboBox().getValue());
        rule.setPhase(identityFields.phaseComboBox().getValue());
        rule.setAttackType(identityFields.attackTypeComboBox().getValue());
        rule.setEnabled(identityFields.enabledCheckBox().isSelected());
        rule.setOptionalActivation(identityFields.optionalActivationCheckBox().isSelected());

        rule.setAttackerUnitNameContains(matchFields.attackerUnitNameField().getText());
        rule.setDefenderUnitNameContains(matchFields.defenderUnitNameField().getText());
        rule.setWeaponNameContains(matchFields.weaponNameField().getText());
        rule.setAttackerKeyword(matchFields.attackerKeywordField().getText());
        rule.setDefenderKeyword(matchFields.defenderKeywordField().getText());
        rule.setAttackerAbilityNameContains(matchFields.attackerAbilityField().getText());
        rule.setDefenderAbilityNameContains(matchFields.defenderAbilityField().getText());
        rule.setAttackerFactionAbilityNameContains(matchFields.attackerFactionAbilityField().getText());
        rule.setDefenderFactionAbilityNameContains(matchFields.defenderFactionAbilityField().getText());
        rule.setAttackerDetachmentAbilityNameContains(matchFields.attackerDetachmentAbilityField().getText());
        rule.setDefenderDetachmentAbilityNameContains(matchFields.defenderDetachmentAbilityField().getText());
        rule.setAttackerFactionNameContains(matchFields.attackerFactionField().getText());
        rule.setDefenderFactionNameContains(matchFields.defenderFactionField().getText());
        rule.setAttackerDetachmentNameContains(matchFields.attackerDetachmentField().getText());
        rule.setDefenderDetachmentNameContains(matchFields.defenderDetachmentField().getText());
        rule.setAttackerEnhancementNameContains(matchFields.attackerEnhancementField().getText());
        rule.setDefenderEnhancementNameContains(matchFields.defenderEnhancementField().getText());

        rule.setTriggeringStratagemNameContains(stratagemFields.triggeringStratagemField().getText());
        rule.setDuration(stratagemFields.durationComboBox().getValue());
        rule.setTargetRole(stratagemFields.targetRoleComboBox().getValue());
        return rule;
    }

    public static void writeRuleToForm(
            EditorRuleDefinition rule,
            boolean existingRule,
            RuleIdentityFields identityFields,
            RuleMatchFields matchFields,
            RuleStratagemFields stratagemFields,
            RuleEditorCards editorCards
    ) {
        EditorRuleDefinition current = rule == null ? new EditorRuleDefinition() : rule;

        editorCards.deleteRuleButton().setDisable(!existingRule);
        editorCards.saveRuleButton().setDisable(false);
        setEditorEditable(editorCards, true);

        identityFields.nameField().setText(current.getName());
        identityFields.typeComboBox().setValue(current.getType());
        identityFields.phaseComboBox().setValue(current.getPhase());
        identityFields.attackTypeComboBox().setValue(current.getAttackType());
        identityFields.enabledCheckBox().setSelected(current.isEnabled());
        identityFields.optionalActivationCheckBox().setSelected(current.isOptionalActivation());

        matchFields.attackerUnitNameField().setText(current.getAttackerUnitNameContains());
        matchFields.defenderUnitNameField().setText(current.getDefenderUnitNameContains());
        matchFields.weaponNameField().setText(current.getWeaponNameContains());
        matchFields.attackerKeywordField().setText(current.getAttackerKeyword());
        matchFields.defenderKeywordField().setText(current.getDefenderKeyword());
        matchFields.attackerAbilityField().setText(current.getAttackerAbilityNameContains());
        matchFields.defenderAbilityField().setText(current.getDefenderAbilityNameContains());
        matchFields.attackerFactionAbilityField().setText(current.getAttackerFactionAbilityNameContains());
        matchFields.defenderFactionAbilityField().setText(current.getDefenderFactionAbilityNameContains());
        matchFields.attackerDetachmentAbilityField().setText(current.getAttackerDetachmentAbilityNameContains());
        matchFields.defenderDetachmentAbilityField().setText(current.getDefenderDetachmentAbilityNameContains());
        matchFields.attackerFactionField().setText(current.getAttackerFactionNameContains());
        matchFields.defenderFactionField().setText(current.getDefenderFactionNameContains());
        matchFields.attackerDetachmentField().setText(current.getAttackerDetachmentNameContains());
        matchFields.defenderDetachmentField().setText(current.getDefenderDetachmentNameContains());
        matchFields.attackerEnhancementField().setText(current.getAttackerEnhancementNameContains());
        matchFields.defenderEnhancementField().setText(current.getDefenderEnhancementNameContains());

        stratagemFields.triggeringStratagemField().setText(current.getTriggeringStratagemNameContains());
        stratagemFields.durationComboBox().setValue(current.getDuration());
        stratagemFields.targetRoleComboBox().setValue(current.getTargetRole());
    }

    public static void showBuiltInRule(
            String displayName,
            String script,
            RuleIdentityFields identityFields,
            RuleMatchFields matchFields,
            RuleStratagemFields stratagemFields,
            RuleEditorCards editorCards
    ) {
        editorCards.deleteRuleButton().setDisable(true);
        editorCards.saveRuleButton().setDisable(true);
        setEditorEditable(editorCards, false);

        identityFields.nameField().setText(displayName);
        identityFields.typeComboBox().setValue(EditorRuleType.KEYWORD);
        identityFields.phaseComboBox().setValue(EditorRulePhase.ANY);
        identityFields.attackTypeComboBox().setValue(EditorRuleAttackType.ANY);
        identityFields.enabledCheckBox().setSelected(true);
        identityFields.optionalActivationCheckBox().setSelected(false);
        clearMetadataFilters(matchFields, stratagemFields);
        editorCards.scriptTextArea().setText(script);
    }

    public static void clearMetadataFilters(
            RuleMatchFields matchFields,
            RuleStratagemFields stratagemFields
    ) {
        matchFields.attackerUnitNameField().clear();
        matchFields.defenderUnitNameField().clear();
        matchFields.weaponNameField().clear();
        matchFields.attackerKeywordField().clear();
        matchFields.defenderKeywordField().clear();
        matchFields.attackerAbilityField().clear();
        matchFields.defenderAbilityField().clear();
        matchFields.attackerFactionAbilityField().clear();
        matchFields.defenderFactionAbilityField().clear();
        matchFields.attackerDetachmentAbilityField().clear();
        matchFields.defenderDetachmentAbilityField().clear();
        matchFields.attackerFactionField().clear();
        matchFields.defenderFactionField().clear();
        matchFields.attackerDetachmentField().clear();
        matchFields.defenderDetachmentField().clear();
        matchFields.attackerEnhancementField().clear();
        matchFields.defenderEnhancementField().clear();

        stratagemFields.triggeringStratagemField().clear();
        stratagemFields.durationComboBox().setValue(EditorRuleDuration.UNTIL_END_OF_PHASE);
        stratagemFields.targetRoleComboBox().setValue(EditorRuleTargetRole.ATTACKER);
    }

    public static void setEditorEditable(RuleEditorCards editorCards, boolean editable) {
        editorCards.identityCard().setDisable(!editable);
        editorCards.matchCard().setDisable(!editable);
        editorCards.stratagemCard().setDisable(!editable);
        editorCards.visualLogicCard().setDisable(!editable);
        editorCards.resetLogicButton().setDisable(!editable);
    }

    private static String safe(String value) {
        return value == null ? "" : value.trim();
    }

    public record RuleIdentityFields(
            TextField nameField,
            ComboBox<EditorRuleType> typeComboBox,
            ComboBox<EditorRulePhase> phaseComboBox,
            ComboBox<EditorRuleAttackType> attackTypeComboBox,
            CheckBox enabledCheckBox,
            CheckBox optionalActivationCheckBox
    ) {
    }

    public record RuleMatchFields(
            TextField attackerUnitNameField,
            TextField defenderUnitNameField,
            TextField weaponNameField,
            TextField attackerKeywordField,
            TextField defenderKeywordField,
            TextField attackerAbilityField,
            TextField defenderAbilityField,
            TextField attackerFactionAbilityField,
            TextField defenderFactionAbilityField,
            TextField attackerDetachmentAbilityField,
            TextField defenderDetachmentAbilityField,
            TextField attackerFactionField,
            TextField defenderFactionField,
            TextField attackerDetachmentField,
            TextField defenderDetachmentField,
            TextField attackerEnhancementField,
            TextField defenderEnhancementField
    ) {
    }

    public record RuleStratagemFields(
            TextField triggeringStratagemField,
            ComboBox<EditorRuleDuration> durationComboBox,
            ComboBox<EditorRuleTargetRole> targetRoleComboBox
    ) {
    }

    public record RuleEditorCards(
            VBox identityCard,
            VBox matchCard,
            VBox stratagemCard,
            VBox visualLogicCard,
            Button deleteRuleButton,
            Button saveRuleButton,
            Button resetLogicButton,
            TextArea scriptTextArea
    ) {
    }
}
