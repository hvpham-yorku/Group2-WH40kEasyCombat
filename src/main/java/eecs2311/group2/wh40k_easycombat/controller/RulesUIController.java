package eecs2311.group2.wh40k_easycombat.controller;

import eecs2311.group2.wh40k_easycombat.util.FixedAspectView;
// import eecs2311.group2.wh40k_easycombat.model.*;        // TEMP disabled (schema changed)
// import eecs2311.group2.wh40k_easycombat.service.StaticDataService; // TEMP disabled
// import eecs2311.group2.wh40k_easycombat.util.SelectedUnitContext;  // TEMP disabled

import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;

import java.io.IOException;

public class RulesUIController {

    // ======================= Buttons ==========================
    @FXML private Button addButton;
    @FXML private Button backButton;
    @FXML private Button CoreRuleBackButton;
    @FXML private Button confirmButton;
    @FXML private Button editButton;
    @FXML private Button searchButton;

    // ======================= Inputs ===========================
    @FXML private TextField searchbox;

    // was: ComboBox<Factions>
    @FXML private ComboBox<?> factionCBbox;

    // ======================= Text Areas =======================
    @FXML private TextArea coreBox;
    @FXML private TextArea factionBox;
    @FXML private TextArea keyBox;
    @FXML private TextArea mainBox;
    @FXML private TextArea unitBox;

    // ======================= Labels ===========================
    @FXML private Label unitNameLabel;
    @FXML private Label pointLable;
    @FXML private Label isvLabel;
    @FXML private Label mLabel;
    @FXML private Label tLabel;
    @FXML private Label wLabel;
    @FXML private Label svLabel;
    @FXML private Label ocLabel;
    @FXML private Label ldLabel;

    // ======================= Tables - Melee Weapon ========================
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

    // ======================= TreeTable ====================================
    // was: TreeTableView<UnitRow>
    @FXML private TreeTableView<?> dataTreeTable;
    @FXML private TreeTableColumn<?, ?> dataTreeColumn;

    @FXML
    private void initialize() {
        // TEMP DISABLED:
        // setupTreeColumns();
        // setupWeaponColumns();
        // reloadCachesFromService();
        // setupFactionComboBox();
        // selectDefaultFaction("Space Marines");
        // rebuildUnitTree(...);
        // selection listeners

        clearUnitDetails();
        if (searchbox != null) searchbox.clear();
    }

    private void clearUnitDetails() {
        if (unitNameLabel != null) unitNameLabel.setText("");
        if (pointLable != null) pointLable.setText("");
        if (mLabel != null) mLabel.setText("");
        if (tLabel != null) tLabel.setText("");
        if (svLabel != null) svLabel.setText("");
        if (wLabel != null) wLabel.setText("");
        if (ldLabel != null) ldLabel.setText("");
        if (ocLabel != null) ocLabel.setText("");
        if (isvLabel != null) isvLabel.setText("");

        if (keyBox != null) keyBox.clear();
        if (coreBox != null) coreBox.clear();
        if (mainBox != null) mainBox.clear();
        if (unitBox != null) unitBox.clear();
        if (factionBox != null) factionBox.clear();

        if (meleeWeaponTable != null) meleeWeaponTable.getItems().clear();
        if (rangedWeaponTable != null) rangedWeaponTable.getItems().clear();
    }

    // ----------------------- Button handlers -----------------------

    @FXML
    void backMainpage(MouseEvent event) throws IOException {
        FixedAspectView.switchTo((Node) event.getSource(),
                "/eecs2311/group2/wh40k_easycombat/MainUI.fxml",
                1200.0, 800.0);
    }

    @FXML
    void confirm(MouseEvent event) {
        // TEMP DISABLED (depends on old Factions + Units + StaticDataService caches)
        clearUnitDetails();
        showWarning("Temporarily Disabled",
                "Confirm/filter is disabled until RulesUI is migrated to the new CSV/Wahapedia database schema.");
    }

    @FXML
    void search(MouseEvent event) {
        // TEMP DISABLED (depends on old Units list & tree rebuild)
        clearUnitDetails();
        showWarning("Temporarily Disabled",
                "Search is disabled until RulesUI is migrated to the new CSV/Wahapedia database schema.");
    }

    @FXML
    void add(MouseEvent event) throws IOException {
        // TEMP DISABLED: SelectedUnitContext.clear();
        FixedAspectView.switchTo((Node) event.getSource(),
                "/eecs2311/group2/wh40k_easycombat/RuleEditor.fxml",
                1000.0, 600.0);
    }

    @FXML
    void edit(MouseEvent event) {
        // TEMP DISABLED (depends on UnitRow + SelectedUnitContext + Units)
        showWarning("Temporarily Disabled",
                "Edit is disabled until RulesUI is migrated to the new CSV/Wahapedia database schema.");
    }

    // ----------------------- UI helpers -----------------------

    private void showWarning(String title, String msg) {
        Alert a = new Alert(Alert.AlertType.WARNING);
        a.setTitle(title);
        a.setHeaderText(null);
        a.setContentText(msg);
        a.showAndWait();
    }
}