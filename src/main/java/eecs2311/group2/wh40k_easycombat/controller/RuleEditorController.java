package eecs2311.group2.wh40k_easycombat.controller;

import eecs2311.group2.wh40k_easycombat.controller.helper.DialogHelper;
import eecs2311.group2.wh40k_easycombat.controller.helper.RuleEditorFormMapper;
import eecs2311.group2.wh40k_easycombat.controller.helper.RuleEditorVisualBuilderHelper;
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
        RuleEditorVisualBuilderHelper.configureSpinner(visualHitModifierSpinner, -6, 6, 0);
        RuleEditorVisualBuilderHelper.configureSpinner(visualWoundModifierSpinner, -6, 6, 0);
        RuleEditorVisualBuilderHelper.configureSpinner(visualAttacksModifierSpinner, -20, 20, 0);
        RuleEditorVisualBuilderHelper.configureSpinner(visualDamageModifierSpinner, -10, 10, 0);
        RuleEditorVisualBuilderHelper.configureSpinner(visualApModifierSpinner, -6, 6, 0);
        scriptTextArea.setEditable(false);

        rulesListView.getSelectionModel().selectedItemProperty()
                .addListener((obs, oldValue, newValue) -> showListEntry(newValue));

        RuleEditorVisualBuilderHelper.wireVisualBuilderPreview(visualControls(), this::refreshGeneratedScriptPreview);
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
            RuleEditorVisualBuilderHelper.resetVisualControls(visualControls());
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

        EditorRuleDefinition rule = RuleEditorFormMapper.readRuleFromForm(
                selectedRuleId,
                identityFields(),
                matchFields(),
                stratagemFields()
        );
        rule.setName(name);
        RuleEditorVisualBuilderHelper.readVisualBuilder(rule, visualControls());
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
            RuleEditorFormMapper.writeRuleToForm(
                    current,
                    existingRule,
                    identityFields(),
                    matchFields(),
                    stratagemFields(),
                    editorCards()
            );
            RuleEditorVisualBuilderHelper.loadVisualBuilder(current, visualControls());
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
            RuleEditorFormMapper.showBuiltInRule(
                    item.getDisplayName(),
                    item.getScript(),
                    identityFields(),
                    matchFields(),
                    stratagemFields(),
                    editorCards()
            );
            RuleEditorVisualBuilderHelper.resetVisualControls(visualControls());
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

    private void refreshGeneratedScriptPreview() {
        if (updatingForm || saveRuleButton.isDisabled()) {
            return;
        }

        EditorRuleDefinition preview = new EditorRuleDefinition();
        RuleEditorVisualBuilderHelper.readVisualBuilder(preview, visualControls());
        scriptTextArea.setText(visualVmScriptBuilder.build(preview));
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

    private RuleEditorFormMapper.RuleIdentityFields identityFields() {
        return new RuleEditorFormMapper.RuleIdentityFields(
                nameField,
                typeComboBox,
                phaseComboBox,
                attackTypeComboBox,
                enabledCheckBox,
                optionalActivationCheckBox
        );
    }

    private RuleEditorFormMapper.RuleMatchFields matchFields() {
        return new RuleEditorFormMapper.RuleMatchFields(
                attackerUnitNameField,
                defenderUnitNameField,
                weaponNameField,
                attackerKeywordField,
                defenderKeywordField,
                attackerAbilityField,
                defenderAbilityField,
                attackerFactionAbilityField,
                defenderFactionAbilityField,
                attackerDetachmentAbilityField,
                defenderDetachmentAbilityField,
                attackerFactionField,
                defenderFactionField,
                attackerDetachmentField,
                defenderDetachmentField,
                attackerEnhancementField,
                defenderEnhancementField
        );
    }

    private RuleEditorFormMapper.RuleStratagemFields stratagemFields() {
        return new RuleEditorFormMapper.RuleStratagemFields(
                triggeringStratagemField,
                durationComboBox,
                targetRoleComboBox
        );
    }

    private RuleEditorFormMapper.RuleEditorCards editorCards() {
        return new RuleEditorFormMapper.RuleEditorCards(
                identityCard,
                matchCard,
                stratagemCard,
                visualLogicCard,
                deleteRuleButton,
                saveRuleButton,
                resetLogicButton,
                scriptTextArea
        );
    }

    private RuleEditorVisualBuilderHelper.VisualControls visualControls() {
        return new RuleEditorVisualBuilderHelper.VisualControls(
                visualWithinHalfRangeCheckBox,
                visualRemainedStationaryCheckBox,
                visualAdvancedThisTurnCheckBox,
                visualFellBackThisTurnCheckBox,
                visualChargedThisTurnCheckBox,
                visualAttackerCanFightCheckBox,
                visualTargetHasCoverCheckBox,
                visualBlastIsLegalCheckBox,
                visualTargetInfantryCheckBox,
                visualTargetVehicleCheckBox,
                visualTargetMonsterCheckBox,
                visualTargetCharacterCheckBox,
                visualTargetPsykerCheckBox,
                visualHitModifierSpinner,
                visualWoundModifierSpinner,
                visualAttacksModifierSpinner,
                visualDamageModifierSpinner,
                visualApModifierSpinner,
                visualHitRerollComboBox,
                visualWoundRerollComboBox,
                visualKeywordsField
        );
    }

    private String safe(String value) {
        return value == null ? "" : value.trim();
    }
}
