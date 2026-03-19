package eecs2311.group2.wh40k_easycombat.controller;

import eecs2311.group2.wh40k_easycombat.controller.helper.DialogHelper;
import eecs2311.group2.wh40k_easycombat.util.FixedAspectView;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import java.io.IOException;

public class RuleEditorController {

    // ======================= Buttons ====================================
    @FXML private Button abilityButton;
    @FXML private Button cancelButton;
    @FXML private Button saveButton;
    @FXML private Button unitKeywordAddButton;
    @FXML private Button unitKeywordDeleteButton;
    @FXML private Button weaponAddButton;

    // ======================= ComboBox ===================================
    // was: ComboBox<Factions>
    @FXML private ComboBox<?> factionCBbox;
    @FXML private ComboBox<String> categoryCBbox;

    // ======================= Keyword Area ===============================
    @FXML private TextArea keywordTextbox;

    // ======================= Unit Basic Inputs ==========================
    @FXML private TextField unitNametxtBox;
    @FXML private TextField pointtxtBox;
    @FXML private TextField isvBox;
    @FXML private TextField mBox;
    @FXML private TextField ocBOX;
    @FXML private TextField ldBox;
    @FXML private TextField svBox;
    @FXML private TextField tBox;
    @FXML private TextField wBox;

    // ======================= Tables - Melee Weapon =======================
    // was: TableView<MeleeWeapons>, TableColumn<MeleeWeapons,...>
    @FXML private TableView<?> meleeWeaponTable;
    @FXML private TableColumn<?, ?> mName;
    @FXML private TableColumn<?, ?> mA;
    @FXML private TableColumn<?, ?> mWS;
    @FXML private TableColumn<?, ?> mS;
    @FXML private TableColumn<?, ?> mAP;
    @FXML private TableColumn<?, ?> mD;
    @FXML private TableColumn<?, ?> mK;

    // ======================= Tables - Ranged Weapon =======================
    // was: TableView<RangeWeapons>, TableColumn<RangeWeapons,...>
    @FXML private TableView<?> rangedWeaponTable;
    @FXML private TableColumn<?, ?> rName;
    @FXML private TableColumn<?, ?> rRange;
    @FXML private TableColumn<?, ?> rA;
    @FXML private TableColumn<?, ?> rBS;
    @FXML private TableColumn<?, ?> rS;
    @FXML private TableColumn<?, ?> rAP;
    @FXML private TableColumn<?, ?> rD;
    @FXML private TableColumn<?, ?> rK;

    // ======================= Tables - Unit Keyword =======================
    // was: TableView<UnitKeywords>, TableColumn<UnitKeywords,String>
    @FXML private TableView<?> unitKeywordTable;
    @FXML private TableColumn<?, ?> keywords;

    @FXML
    private void initialize() {
        // ===== TEMP DISABLED: old DB loading & cache building =====
        // data.loadAll();
        // setupFactionCombo();
        // setupCategoryComboBox();
        // setupKeywordTable();
        // setupWeaponTables();
        //
        // workingUnit = SelectedUnitContext.getSelectedUnit();
        // if (workingUnit == null) setAddModeDefaults();
        // else loadUnitToUI(workingUnit);

        // Keep UI safe defaults so FXML doesn't explode
        if (categoryCBbox != null) {
            categoryCBbox.getItems().setAll("CHARACTER", "INFANTRY", "VEHICLE");
            categoryCBbox.getSelectionModel().select("INFANTRY");
        }
        if (keywordTextbox != null) keywordTextbox.clear();
    }

    // ======================= Navigation =======================

    @FXML
    void cancel(MouseEvent event) throws IOException {
        // SelectedUnitContext.clear(); // TEMP DISABLED (may depend on old Units model)
        FixedAspectView.switchTo((Node) event.getSource(),
                "/eecs2311/group2/wh40k_easycombat/RulesUI.fxml",
                1200.0, 800.0);
    }

    @FXML
    void addWeapon(MouseEvent event) throws IOException {
        // SelectedUnitContext.setSelectedUnit(buildDraftUnitFromUI()); // TEMP DISABLED
        FixedAspectView.switchTo((Node) event.getSource(),
                "/eecs2311/group2/wh40k_easycombat/WeaponEditor.fxml",
                1200.0, 800.0);
    }

    @FXML
    void abilitySetting(MouseEvent event) throws IOException {
        // SelectedUnitContext.setSelectedUnit(buildDraftUnitFromUI()); // TEMP DISABLED
        FixedAspectView.switchTo((Node) event.getSource(),
                "/eecs2311/group2/wh40k_easycombat/UnitAbility.fxml",
                1000.0, 600.0);
    }

    // ======================= Keyword Add =======================

    @FXML
    void keywordAdd(MouseEvent event) {
        // TEMP DISABLED (depends on old UnitKeywords model & DB cache)
        showWarning("Temporarily Disabled",
                "Keyword selection is disabled until controller is rewired to new CSV-based tables.");
    }
    

    @FXML
    void keywordDelete(MouseEvent event) {

    }

    // ======================= Save =======================

    @FXML
    void save(MouseEvent event) {
        // TEMP DISABLED (depends on old Units model + old service layer)
        showWarning("Temporarily Disabled",
                "Save is disabled until RuleEditor is migrated to new database schema.");
    }

    // ======================= UI Helpers =======================

    private void showWarning(String title, String msg) {
        Alert a = new Alert(Alert.AlertType.WARNING);
        a.setTitle(title);
        a.setHeaderText(null);
        a.setContentText(msg);
        DialogHelper.styleAlert(a);
        a.showAndWait();
    }

    @SuppressWarnings("unused")
    private void showError(String title, String msg) {
        Alert a = new Alert(Alert.AlertType.ERROR);
        a.setTitle(title);
        a.setHeaderText(null);
        a.setContentText(msg);
        DialogHelper.styleAlert(a);
        a.showAndWait();
    }
}