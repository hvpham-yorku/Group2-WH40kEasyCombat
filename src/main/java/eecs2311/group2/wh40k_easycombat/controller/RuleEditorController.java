package eecs2311.group2.wh40k_easycombat.controller;

import eecs2311.group2.wh40k_easycombat.controller.helper.DialogHelper;
import eecs2311.group2.wh40k_easycombat.model.editor.EditorRerollType;
import eecs2311.group2.wh40k_easycombat.model.editor.EditorRuleActivationMode;
import eecs2311.group2.wh40k_easycombat.model.editor.EditorRuleAttackType;
import eecs2311.group2.wh40k_easycombat.model.editor.EditorRuleDefinition;
import eecs2311.group2.wh40k_easycombat.model.editor.EditorRuleDuration;
import eecs2311.group2.wh40k_easycombat.model.editor.EditorRulePhase;
import eecs2311.group2.wh40k_easycombat.model.editor.EditorRuleTargetRole;
import eecs2311.group2.wh40k_easycombat.model.editor.EditorRuleType;
import eecs2311.group2.wh40k_easycombat.service.editor.RuleEditorService;
import eecs2311.group2.wh40k_easycombat.util.FixedAspectView;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;

import java.io.IOException;
import java.util.List;

public class RuleEditorController {
    // ======================= Rule List =======================
    @FXML private ListView<EditorRuleDefinition> rulesListView;
    @FXML private Label statusLabel;
    @FXML private Label helperLabel;

    // ======================= Basic Rule Fields =======================
    @FXML private TextField nameField;
    @FXML private ComboBox<EditorRuleType> typeComboBox;
    @FXML private ComboBox<EditorRuleActivationMode> activationModeComboBox;
    @FXML private ComboBox<EditorRulePhase> phaseComboBox;
    @FXML private ComboBox<EditorRuleAttackType> attackTypeComboBox;
    @FXML private CheckBox enabledCheckBox;

    // ======================= Binding Fields =======================
    @FXML private TextField attackerUnitNameField;
    @FXML private TextField defenderUnitNameField;
    @FXML private TextField attackerKeywordField;
    @FXML private TextField defenderKeywordField;
    @FXML private TextField attackerAbilityField;
    @FXML private TextField defenderAbilityField;
    @FXML private TextField attackerFactionAbilityField;
    @FXML private TextField defenderFactionAbilityField;
    @FXML private TextField attackerDetachmentAbilityField;
    @FXML private TextField defenderDetachmentAbilityField;
    @FXML private TextField attackerFactionField;
    @FXML private TextField defenderFactionField;
    @FXML private TextField attackerDetachmentField;
    @FXML private TextField defenderDetachmentField;
    @FXML private TextField attackerEnhancementField;
    @FXML private TextField defenderEnhancementField;
    @FXML private TextField triggeringStratagemField;
    @FXML private ComboBox<EditorRuleDuration> durationComboBox;
    @FXML private ComboBox<EditorRuleTargetRole> targetRoleComboBox;

    // ======================= Condition CheckBoxes =======================
    @FXML private CheckBox requireHalfRangeCheckBox;
    @FXML private CheckBox requireStationaryCheckBox;
    @FXML private CheckBox requireChargedCheckBox;
    @FXML private CheckBox requireTargetCoverCheckBox;
    @FXML private CheckBox requireInfantryCheckBox;
    @FXML private CheckBox requireVehicleCheckBox;
    @FXML private CheckBox requireMonsterCheckBox;
    @FXML private CheckBox requireCharacterCheckBox;
    @FXML private CheckBox requirePsykerCheckBox;

    // ======================= Effect Fields =======================
    @FXML private TextField hitModifierField;
    @FXML private TextField woundModifierField;
    @FXML private TextField attacksModifierField;
    @FXML private TextField damageModifierField;
    @FXML private TextField apModifierField;
    @FXML private TextField extraKeywordsField;
    @FXML private ComboBox<EditorRerollType> hitRerollComboBox;
    @FXML private ComboBox<EditorRerollType> woundRerollComboBox;
    @FXML private TextArea notesTextArea;

    // ======================= Buttons =======================
    @FXML private Button newRuleButton;
    @FXML private Button saveRuleButton;
    @FXML private Button deleteRuleButton;
    @FXML private Button backButton;

    private final RuleEditorService ruleEditorService = RuleEditorService.getInstance();
    private final ObservableList<EditorRuleDefinition> ruleItems = FXCollections.observableArrayList();

    private String selectedRuleId;

    // When this page loads, initialize all editor controls and load saved custom rules.
    @FXML
    private void initialize() {
        rulesListView.setItems(ruleItems);
        rulesListView.setCellFactory(list -> new ListCell<>() {
            @Override
            protected void updateItem(EditorRuleDefinition item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    return;
                }

                setText(item.displayName()
                        + "\n"
                        + item.getType() + " | " + item.getPhase() + " | " + item.getAttackType());
            }
        });

        typeComboBox.getItems().setAll(EditorRuleType.values());
        activationModeComboBox.getItems().setAll(EditorRuleActivationMode.values());
        phaseComboBox.getItems().setAll(EditorRulePhase.values());
        attackTypeComboBox.getItems().setAll(EditorRuleAttackType.values());
        durationComboBox.getItems().setAll(EditorRuleDuration.values());
        targetRoleComboBox.getItems().setAll(EditorRuleTargetRole.values());
        hitRerollComboBox.getItems().setAll(EditorRerollType.values());
        woundRerollComboBox.getItems().setAll(EditorRerollType.values());
        activationModeComboBox.setDisable(true);
        activationModeComboBox.setValue(EditorRuleActivationMode.PASSIVE);

        rulesListView.getSelectionModel().selectedItemProperty().addListener((obs, oldValue, newValue) -> showRule(newValue));

        refreshRuleList(null);
        if (ruleItems.isEmpty()) {
            prepareNewRule();
        } else {
            rulesListView.getSelectionModel().selectFirst();
        }
    }

    // When click "New Rule" button, clear the editor and prepare a fresh custom rule.
    @FXML
    private void newRule(ActionEvent event) {
        prepareNewRule();
    }

    // When click "Save Rule" button, validate the input and save the current custom rule.
    @FXML
    private void saveRule(ActionEvent event) {
        String name = safe(nameField.getText());
        if (name.isBlank()) {
            DialogHelper.showWarning("Missing Name", "Please give this custom rule a name.");
            return;
        }

        EditorRuleDefinition rule = new EditorRuleDefinition();
        try {
            rule.setId(selectedRuleId);
            rule.setName(name);
            rule.setType(typeComboBox.getValue());
            rule.setActivationMode(EditorRuleActivationMode.PASSIVE);
            rule.setPhase(phaseComboBox.getValue());
            rule.setAttackType(attackTypeComboBox.getValue());
            rule.setEnabled(enabledCheckBox.isSelected());
            rule.setAttackerUnitNameContains(attackerUnitNameField.getText());
            rule.setDefenderUnitNameContains(defenderUnitNameField.getText());
            rule.setAttackerKeyword(attackerKeywordField.getText());
            rule.setDefenderKeyword(defenderKeywordField.getText());
            rule.setAttackerAbilityNameContains(attackerAbilityField.getText());
            rule.setDefenderAbilityNameContains(defenderAbilityField.getText());
            rule.setAttackerFactionAbilityNameContains(attackerFactionAbilityField.getText());
            rule.setDefenderFactionAbilityNameContains(defenderFactionAbilityField.getText());
            rule.setAttackerDetachmentAbilityNameContains(attackerDetachmentAbilityField.getText());
            rule.setDefenderDetachmentAbilityNameContains(defenderDetachmentAbilityField.getText());
            rule.setAttackerFactionNameContains(attackerFactionField.getText());
            rule.setDefenderFactionNameContains(defenderFactionField.getText());
            rule.setAttackerDetachmentNameContains(attackerDetachmentField.getText());
            rule.setDefenderDetachmentNameContains(defenderDetachmentField.getText());
            rule.setAttackerEnhancementNameContains(attackerEnhancementField.getText());
            rule.setDefenderEnhancementNameContains(defenderEnhancementField.getText());
            rule.setTriggeringStratagemNameContains(triggeringStratagemField.getText());
            rule.setDuration(durationComboBox.getValue());
            rule.setTargetRole(targetRoleComboBox.getValue());
            rule.setRequireWithinHalfRange(requireHalfRangeCheckBox.isSelected());
            rule.setRequireRemainedStationary(requireStationaryCheckBox.isSelected());
            rule.setRequireChargedThisTurn(requireChargedCheckBox.isSelected());
            rule.setRequireTargetHasCover(requireTargetCoverCheckBox.isSelected());
            rule.setRequireTargetInfantry(requireInfantryCheckBox.isSelected());
            rule.setRequireTargetVehicle(requireVehicleCheckBox.isSelected());
            rule.setRequireTargetMonster(requireMonsterCheckBox.isSelected());
            rule.setRequireTargetCharacter(requireCharacterCheckBox.isSelected());
            rule.setRequireTargetPsyker(requirePsykerCheckBox.isSelected());
            rule.setHitModifier(parseInt(hitModifierField));
            rule.setWoundModifier(parseInt(woundModifierField));
            rule.setAttacksModifier(parseInt(attacksModifierField));
            rule.setDamageModifier(parseInt(damageModifierField));
            rule.setApModifier(parseInt(apModifierField));
            rule.setExtraWeaponKeywords(extraKeywordsField.getText());
            rule.setHitReroll(hitRerollComboBox.getValue());
            rule.setWoundReroll(woundRerollComboBox.getValue());
            rule.setNotes(notesTextArea.getText());
        } catch (IllegalArgumentException ex) {
            DialogHelper.showWarning("Invalid Number", ex.getMessage());
            return;
        }

        EditorRuleDefinition saved = ruleEditorService.saveRule(rule);
        refreshRuleList(saved.getId());
        statusLabel.setText("Saved custom rule: " + saved.getName());
    }

    // When click "Delete Rule" button, delete the selected custom rule.
    @FXML
    private void deleteRule(ActionEvent event) {
        if (selectedRuleId == null || selectedRuleId.isBlank()) {
            DialogHelper.showWarning("No Rule Selected", "Select a saved rule before deleting it.");
            return;
        }
        if (!DialogHelper.confirmYesNo("Delete Rule", "Delete the selected custom rule?")) {
            return;
        }

        boolean removed = ruleEditorService.deleteRule(selectedRuleId);
        if (!removed) {
            DialogHelper.showWarning("Delete Failed", "The selected rule could not be deleted.");
            return;
        }

        refreshRuleList(null);
        if (ruleItems.isEmpty()) {
            prepareNewRule();
        } else {
            rulesListView.getSelectionModel().selectFirst();
        }
        statusLabel.setText("Deleted custom rule.");
    }

    // When click "Back" button, return to the datasheets page.
    @FXML
    private void back(ActionEvent event) throws IOException {
        FixedAspectView.switchResponsiveTo(
                (Node) event.getSource(),
                "/eecs2311/group2/wh40k_easycombat/Datasheets.fxml",
                1080.0,
                700.0,
                1320.0,
                820.0
        );
    }

    private void refreshRuleList(String selectedId) {
        List<EditorRuleDefinition> loaded = ruleEditorService.getRules();
        ruleItems.setAll(loaded);

        if (selectedId == null || selectedId.isBlank()) {
            return;
        }

        for (EditorRuleDefinition rule : ruleItems) {
            if (selectedId.equals(rule.getId())) {
                rulesListView.getSelectionModel().select(rule);
                return;
            }
        }
    }

    private void prepareNewRule() {
        selectedRuleId = null;
        showRule(new EditorRuleDefinition());
        rulesListView.getSelectionModel().clearSelection();
        deleteRuleButton.setDisable(true);
        statusLabel.setText("Creating a new custom rule.");
    }

    private void showRule(EditorRuleDefinition rule) {
        EditorRuleDefinition current = rule == null ? new EditorRuleDefinition() : rule;
        selectedRuleId = current.getId();
        deleteRuleButton.setDisable(rule == null);

        nameField.setText(current.getName());
        typeComboBox.setValue(current.getType());
        activationModeComboBox.setValue(EditorRuleActivationMode.PASSIVE);
        phaseComboBox.setValue(current.getPhase());
        attackTypeComboBox.setValue(current.getAttackType());
        enabledCheckBox.setSelected(current.isEnabled());
        attackerUnitNameField.setText(current.getAttackerUnitNameContains());
        defenderUnitNameField.setText(current.getDefenderUnitNameContains());
        attackerKeywordField.setText(current.getAttackerKeyword());
        defenderKeywordField.setText(current.getDefenderKeyword());
        attackerAbilityField.setText(current.getAttackerAbilityNameContains());
        defenderAbilityField.setText(current.getDefenderAbilityNameContains());
        attackerFactionAbilityField.setText(current.getAttackerFactionAbilityNameContains());
        defenderFactionAbilityField.setText(current.getDefenderFactionAbilityNameContains());
        attackerDetachmentAbilityField.setText(current.getAttackerDetachmentAbilityNameContains());
        defenderDetachmentAbilityField.setText(current.getDefenderDetachmentAbilityNameContains());
        attackerFactionField.setText(current.getAttackerFactionNameContains());
        defenderFactionField.setText(current.getDefenderFactionNameContains());
        attackerDetachmentField.setText(current.getAttackerDetachmentNameContains());
        defenderDetachmentField.setText(current.getDefenderDetachmentNameContains());
        attackerEnhancementField.setText(current.getAttackerEnhancementNameContains());
        defenderEnhancementField.setText(current.getDefenderEnhancementNameContains());
        triggeringStratagemField.setText(current.getTriggeringStratagemNameContains());
        durationComboBox.setValue(current.getDuration());
        targetRoleComboBox.setValue(current.getTargetRole());
        requireHalfRangeCheckBox.setSelected(current.isRequireWithinHalfRange());
        requireStationaryCheckBox.setSelected(current.isRequireRemainedStationary());
        requireChargedCheckBox.setSelected(current.isRequireChargedThisTurn());
        requireTargetCoverCheckBox.setSelected(current.isRequireTargetHasCover());
        requireInfantryCheckBox.setSelected(current.isRequireTargetInfantry());
        requireVehicleCheckBox.setSelected(current.isRequireTargetVehicle());
        requireMonsterCheckBox.setSelected(current.isRequireTargetMonster());
        requireCharacterCheckBox.setSelected(current.isRequireTargetCharacter());
        requirePsykerCheckBox.setSelected(current.isRequireTargetPsyker());
        hitModifierField.setText(String.valueOf(current.getHitModifier()));
        woundModifierField.setText(String.valueOf(current.getWoundModifier()));
        attacksModifierField.setText(String.valueOf(current.getAttacksModifier()));
        damageModifierField.setText(String.valueOf(current.getDamageModifier()));
        apModifierField.setText(String.valueOf(current.getApModifier()));
        extraKeywordsField.setText(current.getExtraWeaponKeywords());
        hitRerollComboBox.setValue(current.getHitReroll());
        woundRerollComboBox.setValue(current.getWoundReroll());
        notesTextArea.setText(current.getNotes());

        updateHelperText();
        if (rule != null) {
            statusLabel.setText("Editing custom rule: " + current.getName());
        }
    }

    private void updateHelperText() {
        helperLabel.setText("Enabled rules auto-apply whenever their bindings match. If a Triggering Stratagem is set, using that stratagem will prompt for one affected unit and apply the rule for the chosen duration.");
    }

    private int parseInt(TextField field) {
        String raw = safe(field == null ? "" : field.getText());
        if (raw.isBlank() || "-".equals(raw)) {
            return 0;
        }

        try {
            return Integer.parseInt(raw);
        } catch (NumberFormatException ex) {
            throw new IllegalArgumentException("Please enter a whole number in every numeric modifier field.", ex);
        }
    }

    private String safe(String value) {
        return value == null ? "" : value.trim();
    }
}
