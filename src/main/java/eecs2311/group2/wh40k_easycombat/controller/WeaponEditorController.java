package eecs2311.group2.wh40k_easycombat.controller;

// NOTE:
// Old RangeWeapons/MeleeWeapons/WeaponKeywords/Units/StaticDataService may not exist after schema change.
// Temporarily disable DB-dependent logic and keep UI navigable.

import eecs2311.group2.wh40k_easycombat.controller.helper.DialogHelper;
import eecs2311.group2.wh40k_easycombat.util.FixedAspectView;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;

import java.io.IOException;

public class WeaponEditorController {

    // ======================= ComboBox ==============================
    @FXML private ComboBox<String> categoryCBbox; // "MELEE" / "RANGED"

    // ======================= TextFields ============================
    @FXML private TextField nametxt;
    @FXML private TextField rangetxt;
    @FXML private TextField atxt;
    @FXML private TextField bstxt;
    @FXML private TextField stxt;
    @FXML private TextField aptxt;
    @FXML private TextField dtxt;

    // ======================= Tables – Weapons ======================
    // was: TableView<RangeWeapons>, TableView<MeleeWeapons>
    @FXML private TableView<?> rangedWepponTable;
    @FXML private TableColumn<?, ?> rangeWeaponName;

    @FXML private TableView<?> meleeWeaponTable;
    @FXML private TableColumn<?, ?> meleeWeaponName;

    // ======================= Tables – Keywords =====================
    // was: TableView<WeaponKeywords>
    @FXML private TableView<?> selectionKeywordTable;
    @FXML private TableColumn<?, ?> selectionKeyword;

    @FXML private TableView<?> keywordTable;
    @FXML private TableColumn<?, ?> words;

    @FXML
    private void initialize() {
        // TEMP DISABLED:
        // data.loadAll();
        // workingUnit = SelectedUnitContext.getSelectedUnit();
        // setupTables();
        // loadEquippedWeapons();

        if (categoryCBbox != null) {
            categoryCBbox.setItems(FXCollections.observableArrayList("MELEE", "RANGED"));
            categoryCBbox.getSelectionModel().select("MELEE");
            syncRangeField();
            categoryCBbox.valueProperty().addListener((obs, ov, nv) -> syncRangeField());
        }
        clearFieldsOnly();
    }

    private void syncRangeField() {
        boolean melee = categoryCBbox == null || "MELEE".equalsIgnoreCase(categoryCBbox.getValue());
        if (rangetxt != null) {
            rangetxt.setDisable(melee);
            if (melee) rangetxt.clear();
        }
    }

    // ======================= FXML handlers =======================

    @FXML
    void backToUnitEdtior(MouseEvent event) throws IOException {
        FixedAspectView.switchTo((Node) event.getSource(),
                "/eecs2311/group2/wh40k_easycombat/RuleEditor.fxml",
                1000.0, 600.0);
    }

    @FXML
    void ClearText(MouseEvent event) {
        clearFieldsOnly();
        if (keywordTable != null) keywordTable.getItems().clear();
        if (meleeWeaponTable != null) meleeWeaponTable.getItems().clear();
        if (rangedWepponTable != null) rangedWepponTable.getItems().clear();
    }

    @FXML
    void addKeyword(MouseEvent event) {
        showWarning("Temporarily Disabled",
                "Keyword editing is disabled until migrated to new schema.");
    }

    @FXML
    void deleteKeyword(MouseEvent event) {
        showWarning("Temporarily Disabled",
                "Keyword editing is disabled until migrated to new schema.");
    }

    @FXML
    void editWeapon(MouseEvent event) {
        showWarning("Temporarily Disabled",
                "Weapon editing is disabled until migrated to new schema.");
    }

    @FXML
    void deleteWeapon(MouseEvent event) {
        showWarning("Temporarily Disabled",
                "Weapon removal is disabled until migrated to new schema.");
    }

    @FXML
    void save(MouseEvent event) throws IOException {
        showWarning("Temporarily Disabled",
                "Save is disabled until WeaponEditor is migrated to new schema.");

        // Still allow navigation back if needed:
        FixedAspectView.switchTo((Node) event.getSource(),
                "/eecs2311/group2/wh40k_easycombat/RuleEditor.fxml",
                1000.0, 600.0);
    }

    // ======================= small helpers =======================

    private void clearFieldsOnly() {
        if (nametxt != null) nametxt.clear();
        if (rangetxt != null) rangetxt.clear();
        if (atxt != null) atxt.clear();
        if (bstxt != null) bstxt.clear();
        if (stxt != null) stxt.clear();
        if (aptxt != null) aptxt.clear();
        if (dtxt != null) dtxt.clear();
        syncRangeField();
    }

    private void showWarning(String title, String msg) {
        Alert a = new Alert(Alert.AlertType.WARNING);
        a.setTitle(title);
        a.setHeaderText(null);
        a.setContentText(msg);
        DialogHelper.styleAlert(a);
        a.showAndWait();
    }
}