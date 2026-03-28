package eecs2311.group2.wh40k_easycombat.controller;

import eecs2311.group2.wh40k_easycombat.controller.helper.DialogHelper;
import eecs2311.group2.wh40k_easycombat.model.editor.EditorRerollType;
import eecs2311.group2.wh40k_easycombat.model.editor.EditorRuleAttackType;
import eecs2311.group2.wh40k_easycombat.model.editor.EditorRuleDefinition;
import eecs2311.group2.wh40k_easycombat.model.editor.EditorRuleDuration;
import eecs2311.group2.wh40k_easycombat.model.editor.EditorRulePhase;
import eecs2311.group2.wh40k_easycombat.model.editor.EditorRuleTargetRole;
import eecs2311.group2.wh40k_easycombat.model.editor.EditorRuleType;
import eecs2311.group2.wh40k_easycombat.model.editor.RuleEditorListItem;
import eecs2311.group2.wh40k_easycombat.service.editor.RuleEditorService;
import eecs2311.group2.wh40k_easycombat.service.editor.VisualVmScriptBuilder;
import eecs2311.group2.wh40k_easycombat.service.editor.VmRuleLibraryService;
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
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;

import java.io.IOException;

public class RuleEditorController {
    // ======================= Rule List =======================
    @FXML private ListView<RuleEditorListItem> rulesListView;
    @FXML private Label statusLabel;
    @FXML private Label helperLabel;
    @FXML private Label scriptHelpLabel;

    // ======================= Editor Cards =======================
    @FXML private VBox identityCard;
    @FXML private VBox matchCard;
    @FXML private VBox stratagemCard;
    @FXML private VBox visualLogicCard;

    // ======================= Identity Fields =======================
    @FXML private TextField nameField;
    @FXML private ComboBox<EditorRuleType> typeComboBox;
    @FXML private ComboBox<EditorRulePhase> phaseComboBox;
    @FXML private ComboBox<EditorRuleAttackType> attackTypeComboBox;
    @FXML private CheckBox enabledCheckBox;
    @FXML private CheckBox optionalActivationCheckBox;

    // ======================= Binding Fields =======================
    @FXML private TextField attackerUnitNameField;
    @FXML private TextField defenderUnitNameField;
    @FXML private TextField weaponNameField;
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

    // ======================= Stratagem Trigger Fields =======================
    @FXML private TextField triggeringStratagemField;
    @FXML private ComboBox<EditorRuleDuration> durationComboBox;
    @FXML private ComboBox<EditorRuleTargetRole> targetRoleComboBox;

    // ======================= Visual VM Builder Fields =======================
    @FXML private CheckBox visualWithinHalfRangeCheckBox;
    @FXML private CheckBox visualRemainedStationaryCheckBox;
    @FXML private CheckBox visualAdvancedThisTurnCheckBox;
    @FXML private CheckBox visualFellBackThisTurnCheckBox;
    @FXML private CheckBox visualChargedThisTurnCheckBox;
    @FXML private CheckBox visualAttackerCanFightCheckBox;
    @FXML private CheckBox visualTargetHasCoverCheckBox;
    @FXML private CheckBox visualBlastIsLegalCheckBox;
    @FXML private CheckBox visualTargetInfantryCheckBox;
    @FXML private CheckBox visualTargetVehicleCheckBox;
    @FXML private CheckBox visualTargetMonsterCheckBox;
    @FXML private CheckBox visualTargetCharacterCheckBox;
    @FXML private CheckBox visualTargetPsykerCheckBox;
    @FXML private Spinner<Integer> visualHitModifierSpinner;
    @FXML private Spinner<Integer> visualWoundModifierSpinner;
    @FXML private Spinner<Integer> visualAttacksModifierSpinner;
    @FXML private Spinner<Integer> visualDamageModifierSpinner;
    @FXML private Spinner<Integer> visualApModifierSpinner;
    @FXML private ComboBox<EditorRerollType> visualHitRerollComboBox;
    @FXML private ComboBox<EditorRerollType> visualWoundRerollComboBox;
    @FXML private TextField visualKeywordsField;

    // ======================= VM Script Fields =======================
    @FXML private TextArea scriptTextArea;

    // ======================= Buttons =======================
    @FXML private Button newRuleButton;
    @FXML private Button saveRuleButton;
    @FXML private Button deleteRuleButton;
    @FXML private Button resetLogicButton;
    @FXML private Button backButton;

    private final RuleEditorService ruleEditorService = RuleEditorService.getInstance();
    private final VmRuleLibraryService vmRuleLibraryService = new VmRuleLibraryService();
    private final VisualVmScriptBuilder visualVmScriptBuilder = new VisualVmScriptBuilder();
    private final ObservableList<RuleEditorListItem> ruleItems = FXCollections.observableArrayList();

    private String selectedRuleId;
    private boolean updatingForm;

    // When this page loads, initialize the VM rule editor and load all saved and built-in rules.
    @FXML
    private void initialize() {
        rulesListView.setItems(ruleItems);
        rulesListView.setCellFactory(list -> new ListCell<>() {
            @Override
            protected void updateItem(RuleEditorListItem item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    return;
                }

                setText(item.getDisplayTitle() + "\n" + item.getDisplaySubtitle());
            }
        });

        typeComboBox.getItems().setAll(EditorRuleType.values());
        phaseComboBox.getItems().setAll(EditorRulePhase.values());
        attackTypeComboBox.getItems().setAll(EditorRuleAttackType.values());
        durationComboBox.getItems().setAll(EditorRuleDuration.values());
        targetRoleComboBox.getItems().setAll(EditorRuleTargetRole.values());
        visualHitRerollComboBox.getItems().setAll(EditorRerollType.values());
        visualWoundRerollComboBox.getItems().setAll(EditorRerollType.values());
        configureSpinner(visualHitModifierSpinner, -6, 6, 0);
        configureSpinner(visualWoundModifierSpinner, -6, 6, 0);
        configureSpinner(visualAttacksModifierSpinner, -20, 20, 0);
        configureSpinner(visualDamageModifierSpinner, -10, 10, 0);
        configureSpinner(visualApModifierSpinner, -6, 6, 0);
        scriptTextArea.setEditable(false);

        rulesListView.getSelectionModel().selectedItemProperty()
                .addListener((obs, oldValue, newValue) -> showListEntry(newValue));

        wireVisualBuilderPreview();
        refreshRuleList(null);

        selectFirstEditableOrPrepareNew();
    }

    // When click "New Rule" button, clear the editor and prepare a fresh VM-backed custom rule.
    @FXML
    private void newRule(ActionEvent event) {
        prepareNewRule();
    }

    // When click "Reset Visual Logic" button, clear the visual builder and regenerate the VM script preview.
    @FXML
    private void resetVisualLogic(ActionEvent event) {
        if (saveRuleButton.isDisabled()) {
            return;
        }

        updatingForm = true;
        try {
            resetVisualControls();
        } finally {
            updatingForm = false;
        }
        refreshGeneratedScriptPreview();
        statusLabel.setText("Reset the visual VM logic to its default state.");
    }

    // When click "Save Rule" button, generate the VM script from the visual editor and save the custom rule.
    @FXML
    private void saveRule(ActionEvent event) {
        String name = safe(nameField.getText());
        if (name.isBlank()) {
            DialogHelper.showWarning("Missing Name", "Please give this custom rule a name.");
            return;
        }

        EditorRuleDefinition rule = new EditorRuleDefinition();
        rule.setId(selectedRuleId);
        rule.setName(name);
        rule.setType(typeComboBox.getValue());
        rule.setPhase(phaseComboBox.getValue());
        rule.setAttackType(attackTypeComboBox.getValue());
        rule.setEnabled(enabledCheckBox.isSelected());
        rule.setOptionalActivation(optionalActivationCheckBox.isSelected());
        rule.setAttackerUnitNameContains(attackerUnitNameField.getText());
        rule.setDefenderUnitNameContains(defenderUnitNameField.getText());
        rule.setWeaponNameContains(weaponNameField.getText());
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
        readVisualBuilder(rule);
        rule.setDslScript(visualVmScriptBuilder.build(rule));

        try {
            EditorRuleDefinition saved = ruleEditorService.saveRule(rule);
            refreshRuleList(saved.getId());
            statusLabel.setText("Saved VM-backed custom rule: " + saved.getName());
        } catch (IllegalArgumentException ex) {
            DialogHelper.showWarning("Invalid VM Script", ex.getMessage());
        }
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
        selectFirstEditableOrPrepareNew();
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
        ruleItems.clear();
        for (EditorRuleDefinition rule : ruleEditorService.getRules()) {
            ruleItems.add(RuleEditorListItem.fromSaved(rule));
        }
        ruleItems.addAll(vmRuleLibraryService.getBuiltInRules());

        if (selectedId == null || selectedId.isBlank()) {
            return;
        }

        for (RuleEditorListItem item : ruleItems) {
            if (item.isEditable()
                    && item.getSavedRule() != null
                    && selectedId.equals(item.getSavedRule().getId())) {
                rulesListView.getSelectionModel().select(item);
                return;
            }
        }
    }

    private void prepareNewRule() {
        updatingForm = true;
        rulesListView.getSelectionModel().clearSelection();
        updatingForm = false;
        showEditableRule(new EditorRuleDefinition(), false);
        statusLabel.setText("Creating a new VM-backed custom rule.");
    }

    private void showListEntry(RuleEditorListItem item) {
        if (updatingForm) {
            return;
        }
        if (item == null) {
            prepareNewRule();
            return;
        }
        if (item.isEditable()) {
            showEditableRule(item.getSavedRule(), true);
            return;
        }
        showBuiltInRule(item);
    }

    private void showEditableRule(EditorRuleDefinition rule, boolean existingRule) {
        EditorRuleDefinition current = rule == null ? new EditorRuleDefinition() : rule;
        updatingForm = true;
        try {
            selectedRuleId = existingRule ? current.getId() : null;
            deleteRuleButton.setDisable(!existingRule);
            saveRuleButton.setDisable(false);
            setEditorEditable(true);

            nameField.setText(current.getName());
            typeComboBox.setValue(current.getType());
            phaseComboBox.setValue(current.getPhase());
            attackTypeComboBox.setValue(current.getAttackType());
            enabledCheckBox.setSelected(current.isEnabled());
            optionalActivationCheckBox.setSelected(current.isOptionalActivation());

            attackerUnitNameField.setText(current.getAttackerUnitNameContains());
            defenderUnitNameField.setText(current.getDefenderUnitNameContains());
            weaponNameField.setText(current.getWeaponNameContains());
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

            loadVisualBuilder(current);
            updateEditableHelperText();
        } finally {
            updatingForm = false;
        }

        refreshGeneratedScriptPreview();
        if (existingRule) {
            statusLabel.setText("Editing VM-backed custom rule: " + current.getName());
        }
    }

    private void showBuiltInRule(RuleEditorListItem item) {
        updatingForm = true;
        try {
            selectedRuleId = null;
            deleteRuleButton.setDisable(true);
            saveRuleButton.setDisable(true);
            setEditorEditable(false);

            nameField.setText(item.getDisplayName());
            typeComboBox.setValue(EditorRuleType.KEYWORD);
            phaseComboBox.setValue(EditorRulePhase.ANY);
            attackTypeComboBox.setValue(EditorRuleAttackType.ANY);
            enabledCheckBox.setSelected(true);
            optionalActivationCheckBox.setSelected(false);
            clearMetadataFilters();
            resetVisualControls();
            scriptTextArea.setText(item.getScript());
            updateBuiltInHelperText();
            statusLabel.setText("Viewing built-in VM rule: " + item.getDisplayName());
        } finally {
            updatingForm = false;
        }
    }

    private void updateEditableHelperText() {
        helperLabel.setText("Use the friendly editor above to describe when the rule applies and what it changes. The VM script below is generated automatically and is the exact logic that auto battle will execute.");
        scriptHelpLabel.setText("""
                Generated VM outputs:
                hit_modifier, wound_modifier, attacks_modifier, damage_modifier, ap_modifier
                hit_reroll, wound_reroll and extra_weapon_keywords

                Selected conditions are joined with AND in the generated VM script.
                If Optional Activation is enabled, matching units and weapons can trigger this rule from the auto battle page.
                """);
    }

    private void updateBuiltInHelperText() {
        helperLabel.setText("This is a built-in VM rule loaded from resources/dsl. Built-in rules are read-only here so you can see how the VM script syntax works.");
        scriptHelpLabel.setText("Built-in VM script preview. Click New Rule to create an editable visual rule.");
    }

    private void clearMetadataFilters() {
        attackerUnitNameField.clear();
        defenderUnitNameField.clear();
        weaponNameField.clear();
        attackerKeywordField.clear();
        defenderKeywordField.clear();
        attackerAbilityField.clear();
        defenderAbilityField.clear();
        attackerFactionAbilityField.clear();
        defenderFactionAbilityField.clear();
        attackerDetachmentAbilityField.clear();
        defenderDetachmentAbilityField.clear();
        attackerFactionField.clear();
        defenderFactionField.clear();
        attackerDetachmentField.clear();
        defenderDetachmentField.clear();
        attackerEnhancementField.clear();
        defenderEnhancementField.clear();
        triggeringStratagemField.clear();
        durationComboBox.setValue(EditorRuleDuration.UNTIL_END_OF_PHASE);
        targetRoleComboBox.setValue(EditorRuleTargetRole.ATTACKER);
    }

    private void loadVisualBuilder(EditorRuleDefinition rule) {
        visualWithinHalfRangeCheckBox.setSelected(rule.isVisualWithinHalfRange());
        visualRemainedStationaryCheckBox.setSelected(rule.isVisualRemainedStationary());
        visualAdvancedThisTurnCheckBox.setSelected(rule.isVisualAdvancedThisTurn());
        visualFellBackThisTurnCheckBox.setSelected(rule.isVisualFellBackThisTurn());
        visualChargedThisTurnCheckBox.setSelected(rule.isVisualChargedThisTurn());
        visualAttackerCanFightCheckBox.setSelected(rule.isVisualAttackerCanFight());
        visualTargetHasCoverCheckBox.setSelected(rule.isVisualTargetHasCover());
        visualBlastIsLegalCheckBox.setSelected(rule.isVisualBlastIsLegal());
        visualTargetInfantryCheckBox.setSelected(rule.isVisualTargetIsInfantry());
        visualTargetVehicleCheckBox.setSelected(rule.isVisualTargetIsVehicle());
        visualTargetMonsterCheckBox.setSelected(rule.isVisualTargetIsMonster());
        visualTargetCharacterCheckBox.setSelected(rule.isVisualTargetIsCharacter());
        visualTargetPsykerCheckBox.setSelected(rule.isVisualTargetIsPsyker());
        visualHitModifierSpinner.getValueFactory().setValue(rule.getVisualHitModifier());
        visualWoundModifierSpinner.getValueFactory().setValue(rule.getVisualWoundModifier());
        visualAttacksModifierSpinner.getValueFactory().setValue(rule.getVisualAttacksModifier());
        visualDamageModifierSpinner.getValueFactory().setValue(rule.getVisualDamageModifier());
        visualApModifierSpinner.getValueFactory().setValue(rule.getVisualApModifier());
        visualHitRerollComboBox.setValue(rule.getVisualHitReroll());
        visualWoundRerollComboBox.setValue(rule.getVisualWoundReroll());
        visualKeywordsField.setText(rule.getVisualExtraWeaponKeywords());
    }

    private void readVisualBuilder(EditorRuleDefinition rule) {
        rule.setVisualWithinHalfRange(visualWithinHalfRangeCheckBox.isSelected());
        rule.setVisualRemainedStationary(visualRemainedStationaryCheckBox.isSelected());
        rule.setVisualAdvancedThisTurn(visualAdvancedThisTurnCheckBox.isSelected());
        rule.setVisualFellBackThisTurn(visualFellBackThisTurnCheckBox.isSelected());
        rule.setVisualChargedThisTurn(visualChargedThisTurnCheckBox.isSelected());
        rule.setVisualAttackerCanFight(visualAttackerCanFightCheckBox.isSelected());
        rule.setVisualTargetHasCover(visualTargetHasCoverCheckBox.isSelected());
        rule.setVisualBlastIsLegal(visualBlastIsLegalCheckBox.isSelected());
        rule.setVisualTargetIsInfantry(visualTargetInfantryCheckBox.isSelected());
        rule.setVisualTargetIsVehicle(visualTargetVehicleCheckBox.isSelected());
        rule.setVisualTargetIsMonster(visualTargetMonsterCheckBox.isSelected());
        rule.setVisualTargetIsCharacter(visualTargetCharacterCheckBox.isSelected());
        rule.setVisualTargetIsPsyker(visualTargetPsykerCheckBox.isSelected());
        rule.setVisualHitModifier(spinnerValue(visualHitModifierSpinner));
        rule.setVisualWoundModifier(spinnerValue(visualWoundModifierSpinner));
        rule.setVisualAttacksModifier(spinnerValue(visualAttacksModifierSpinner));
        rule.setVisualDamageModifier(spinnerValue(visualDamageModifierSpinner));
        rule.setVisualApModifier(spinnerValue(visualApModifierSpinner));
        rule.setVisualHitReroll(visualHitRerollComboBox.getValue());
        rule.setVisualWoundReroll(visualWoundRerollComboBox.getValue());
        rule.setVisualExtraWeaponKeywords(visualKeywordsField.getText());
    }

    private void refreshGeneratedScriptPreview() {
        if (updatingForm || saveRuleButton.isDisabled()) {
            return;
        }

        EditorRuleDefinition preview = new EditorRuleDefinition();
        readVisualBuilder(preview);
        scriptTextArea.setText(visualVmScriptBuilder.build(preview));
    }

    private void wireVisualBuilderPreview() {
        enabledCheckBox.selectedProperty().addListener((obs, oldValue, newValue) -> refreshGeneratedScriptPreview());
        visualWithinHalfRangeCheckBox.selectedProperty().addListener((obs, oldValue, newValue) -> refreshGeneratedScriptPreview());
        visualRemainedStationaryCheckBox.selectedProperty().addListener((obs, oldValue, newValue) -> refreshGeneratedScriptPreview());
        visualAdvancedThisTurnCheckBox.selectedProperty().addListener((obs, oldValue, newValue) -> refreshGeneratedScriptPreview());
        visualFellBackThisTurnCheckBox.selectedProperty().addListener((obs, oldValue, newValue) -> refreshGeneratedScriptPreview());
        visualChargedThisTurnCheckBox.selectedProperty().addListener((obs, oldValue, newValue) -> refreshGeneratedScriptPreview());
        visualAttackerCanFightCheckBox.selectedProperty().addListener((obs, oldValue, newValue) -> refreshGeneratedScriptPreview());
        visualTargetHasCoverCheckBox.selectedProperty().addListener((obs, oldValue, newValue) -> refreshGeneratedScriptPreview());
        visualBlastIsLegalCheckBox.selectedProperty().addListener((obs, oldValue, newValue) -> refreshGeneratedScriptPreview());
        visualTargetInfantryCheckBox.selectedProperty().addListener((obs, oldValue, newValue) -> refreshGeneratedScriptPreview());
        visualTargetVehicleCheckBox.selectedProperty().addListener((obs, oldValue, newValue) -> refreshGeneratedScriptPreview());
        visualTargetMonsterCheckBox.selectedProperty().addListener((obs, oldValue, newValue) -> refreshGeneratedScriptPreview());
        visualTargetCharacterCheckBox.selectedProperty().addListener((obs, oldValue, newValue) -> refreshGeneratedScriptPreview());
        visualTargetPsykerCheckBox.selectedProperty().addListener((obs, oldValue, newValue) -> refreshGeneratedScriptPreview());
        visualKeywordsField.textProperty().addListener((obs, oldValue, newValue) -> refreshGeneratedScriptPreview());
        visualHitRerollComboBox.valueProperty().addListener((obs, oldValue, newValue) -> refreshGeneratedScriptPreview());
        visualWoundRerollComboBox.valueProperty().addListener((obs, oldValue, newValue) -> refreshGeneratedScriptPreview());
        visualHitModifierSpinner.valueProperty().addListener((obs, oldValue, newValue) -> refreshGeneratedScriptPreview());
        visualWoundModifierSpinner.valueProperty().addListener((obs, oldValue, newValue) -> refreshGeneratedScriptPreview());
        visualAttacksModifierSpinner.valueProperty().addListener((obs, oldValue, newValue) -> refreshGeneratedScriptPreview());
        visualDamageModifierSpinner.valueProperty().addListener((obs, oldValue, newValue) -> refreshGeneratedScriptPreview());
        visualApModifierSpinner.valueProperty().addListener((obs, oldValue, newValue) -> refreshGeneratedScriptPreview());
    }

    private void configureSpinner(Spinner<Integer> spinner, int min, int max, int value) {
        spinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(min, max, value));
        spinner.getStyleClass().add("game-spinner");
    }

    private int spinnerValue(Spinner<Integer> spinner) {
        Integer value = spinner.getValue();
        return value == null ? 0 : value;
    }

    private void resetVisualControls() {
        visualWithinHalfRangeCheckBox.setSelected(false);
        visualRemainedStationaryCheckBox.setSelected(false);
        visualAdvancedThisTurnCheckBox.setSelected(false);
        visualFellBackThisTurnCheckBox.setSelected(false);
        visualChargedThisTurnCheckBox.setSelected(false);
        visualAttackerCanFightCheckBox.setSelected(false);
        visualTargetHasCoverCheckBox.setSelected(false);
        visualBlastIsLegalCheckBox.setSelected(false);
        visualTargetInfantryCheckBox.setSelected(false);
        visualTargetVehicleCheckBox.setSelected(false);
        visualTargetMonsterCheckBox.setSelected(false);
        visualTargetCharacterCheckBox.setSelected(false);
        visualTargetPsykerCheckBox.setSelected(false);
        visualHitModifierSpinner.getValueFactory().setValue(0);
        visualWoundModifierSpinner.getValueFactory().setValue(0);
        visualAttacksModifierSpinner.getValueFactory().setValue(0);
        visualDamageModifierSpinner.getValueFactory().setValue(0);
        visualApModifierSpinner.getValueFactory().setValue(0);
        visualHitRerollComboBox.setValue(EditorRerollType.NONE);
        visualWoundRerollComboBox.setValue(EditorRerollType.NONE);
        visualKeywordsField.clear();
    }

    private void setEditorEditable(boolean editable) {
        identityCard.setDisable(!editable);
        matchCard.setDisable(!editable);
        stratagemCard.setDisable(!editable);
        visualLogicCard.setDisable(!editable);
        resetLogicButton.setDisable(!editable);
    }

    private void selectFirstEditableOrPrepareNew() {
        for (RuleEditorListItem item : ruleItems) {
            if (item.isEditable()) {
                updatingForm = true;
                rulesListView.getSelectionModel().select(item);
                updatingForm = false;
                showListEntry(item);
                return;
            }
        }
        prepareNewRule();
    }

    private String safe(String value) {
        return value == null ? "" : value.trim();
    }
}
