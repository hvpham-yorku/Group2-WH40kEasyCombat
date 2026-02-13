package eecs2311.group2.wh40k_easycombat.controller;

import eecs2311.group2.wh40k_easycombat.model.*;
import eecs2311.group2.wh40k_easycombat.repository.*;
import eecs2311.group2.wh40k_easycombat.util.FixedAspectView;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import java.io.IOException;
import java.sql.SQLException;
import java.util.*;
import java.util.stream.Collectors;

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
    @FXML private ComboBox<Factions> factionCBbox;

    // ======================= Text Areas =======================
    @FXML private TextArea coreBox;     // core abilities of the selected unit
    @FXML private TextArea factionBox;  // faction ability (bound by factionId in controller)
    @FXML private TextArea keyBox;      // unit keywords
    @FXML private TextArea mainBox;     // other abilities of the selected unit
    @FXML private TextArea unitBox;     // composition

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
    @FXML private TableView<MeleeWeapons> meleeWeaponTable;
    @FXML private TableColumn<MeleeWeapons, String> mName;
    @FXML private TableColumn<MeleeWeapons, String> mA;
    @FXML private TableColumn<MeleeWeapons, Number> mWS;
    @FXML private TableColumn<MeleeWeapons, Number> mS;
    @FXML private TableColumn<MeleeWeapons, Number> mAP;
    @FXML private TableColumn<MeleeWeapons, String> mD;
    @FXML private TableColumn<MeleeWeapons, String> mK;

    // ======================= Tables - Ranged Weapon =======================
    @FXML private TableView<RangeWeapons> rangedWeaponTable;
    @FXML private TableColumn<RangeWeapons, String> rName;
    @FXML private TableColumn<RangeWeapons, Number> rRange;
    @FXML private TableColumn<RangeWeapons, String> rA;
    @FXML private TableColumn<RangeWeapons, Number> rBS;
    @FXML private TableColumn<RangeWeapons, Number> rS;
    @FXML private TableColumn<RangeWeapons, Number> rAP;
    @FXML private TableColumn<RangeWeapons, String> rD;
    @FXML private TableColumn<RangeWeapons, String> rK;

    // ======================= TreeTable ====================================
    @FXML private TreeTableView<UnitRow> dataTreeTable;
    @FXML private TreeTableColumn<UnitRow, String> dataTreeColumn;

    // ======================= In-memory caches =============================
    private List<Units> allUnits = new ArrayList<>();
    private final ObservableList<Factions> factionList = FXCollections.observableArrayList();

    private final Map<Integer, RangeWeapons> rangedById = new HashMap<>();
    private final Map<Integer, MeleeWeapons> meleeById = new HashMap<>();
    private final Map<Integer, String> unitKeywordById = new HashMap<>();
    private final Map<Integer, String> weaponKeywordById = new HashMap<>();

    // NEW: abilities cache
    private final Map<Integer, String> coreAbilityById = new HashMap<>();
    private final Map<Integer, String> otherAbilityById = new HashMap<>();

    private final ObservableList<RangeWeapons> rangedList = FXCollections.observableArrayList();
    private final ObservableList<MeleeWeapons> meleeList = FXCollections.observableArrayList();

    // Category name mapping (your confirmed mapping)
    private static final Map<Integer, String> CATEGORY_NAME = Map.of(
            1, "CHARACTER",
            2, "INFANTRY",
            3, "VEHICLE"
    );

    // NEW: faction ability binding (fill in your real texts)
    // Key = factionId from table factions.id
    private static final Map<Integer, String> FACTION_ABILITY = Map.of(
            1, "Oath of Moment"
            // 2, "....",
            // 3, "...."
    );

    @FXML
    private void initialize() {

        // 1) Setup columns
        setupTreeColumns();
        setupWeaponColumns();

        // 2) Bind weapon tables to observable lists
        rangedWeaponTable.setItems(rangedList);
        meleeWeaponTable.setItems(meleeList);

        // 3) Load DB data into memory (units + weapons + keywords + abilities)
        reloadCachesFromDatabase();

        // 4) Setup faction ComboBox
        setupFactionComboBox();

        // 5) Select default faction on startup
        selectDefaultFaction("Space Marines");

        // 6) Build the tree using the selected faction (default)
        Integer factionId = (factionCBbox.getValue() == null)
                ? null
                : factionCBbox.getValue().id();
        rebuildUnitTree(factionId, null);

        // 7) Listen selection changes: only leaf(unit) triggers details panel
        dataTreeTable.getSelectionModel().selectedItemProperty().addListener((obs, ov, nv) -> {
            if (nv == null) return;
            UnitRow row = nv.getValue();
            if (row == null) return;

            if (!row.isCategoryRow() && row.unit != null) {
                showUnitDetails(row.unit);
            }
        });

        // 8) Style category rows bold (optional)
        dataTreeTable.setRowFactory(tv -> new TreeTableRow<>() {
            @Override
            protected void updateItem(UnitRow item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setStyle("");
                } else if (item.isCategoryRow()) {
                    setStyle("-fx-font-weight: bold;");
                } else {
                    setStyle("");
                }
            }
        });
    }

    private void setupTreeColumns() {
        // Display name for both category and unit rows
        dataTreeColumn.setCellValueFactory(param ->
                new SimpleStringProperty(param.getValue().getValue().displayName)
        );
    }

    private void setupWeaponColumns() {
        // --- Melee ---
        mName.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().name()));
        mA.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().A()));
        mWS.setCellValueFactory(d -> new SimpleIntegerProperty(d.getValue().WS()));
        mS.setCellValueFactory(d -> new SimpleIntegerProperty(d.getValue().S()));
        mAP.setCellValueFactory(d -> new SimpleIntegerProperty(d.getValue().AP()));
        mD.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().D()));
        mK.setCellValueFactory(d -> new SimpleStringProperty(toWeaponKeywordText(d.getValue().keywordIdList())));

        // --- Ranged ---
        rName.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().name()));
        rRange.setCellValueFactory(d -> new SimpleIntegerProperty(d.getValue().range()));
        rA.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().A()));
        rBS.setCellValueFactory(d -> new SimpleIntegerProperty(d.getValue().BS()));
        rS.setCellValueFactory(d -> new SimpleIntegerProperty(d.getValue().S()));
        rAP.setCellValueFactory(d -> new SimpleIntegerProperty(d.getValue().AP()));
        rD.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().D()));
        rK.setCellValueFactory(d -> new SimpleStringProperty(toWeaponKeywordText(d.getValue().keywordIdList())));
    }

    private void setupFactionComboBox() {
        factionCBbox.setItems(factionList);

        // Show faction name in dropdown list
        factionCBbox.setCellFactory(lv -> new ListCell<Factions>() {
            @Override
            protected void updateItem(Factions item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? "" : item.name());
            }
        });

        // Show selected faction name on the ComboBox button
        factionCBbox.setButtonCell(new ListCell<Factions>() {
            @Override
            protected void updateItem(Factions item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? "" : item.name());
            }
        });
    }

    private void reloadCachesFromDatabase() {
        try {
            // Units
            allUnits = UnitRepository.getAllUnits();

            // Factions
            factionList.setAll(FactionRepository.getAllFactions());

            // Weapons
            rangedById.clear();
            for (RangeWeapons rw : RangeWeaponRepository.getAllRangeWeapons()) {
                rangedById.put(rw.id(), rw);
            }

            meleeById.clear();
            for (MeleeWeapons mw : MeleeWeaponRepository.getAllMeleeWeapons()) {
                meleeById.put(mw.id(), mw);
            }

            // Keywords
            unitKeywordById.clear();
            for (UnitKeywords uk : UnitKeywordRepository.getAllUnitKeywords()) {
                unitKeywordById.put(uk.id(), uk.keyword());
            }

            weaponKeywordById.clear();
            for (WeaponKeywords wk : WeaponKeywordRepository.getAllWeaponKeywords()) {
                weaponKeywordById.put(wk.id(), wk.keyword());
            }

            // NEW: Abilities
            coreAbilityById.clear();
            for (CoreAbilities ca : CoreAbilityRepository.getAllCoreAbilities()) {
                coreAbilityById.put(ca.id(), ca.ability());
            }

            otherAbilityById.clear();
            for (OtherAbilities oa : OtherAbilityRepository.getAllOtherAbilities()) {
                otherAbilityById.put(oa.id(), oa.ability());
            }

        } catch (SQLException e) {
            showError("Database Error", "Failed to load data from database.", e);
        }
    }

    // Select a faction by name (case-insensitive).
    // If not found, the first faction in the list will be selected.
    private void selectDefaultFaction(String factionName) {
        if (factionList == null || factionList.isEmpty()) return;

        Factions target = factionList.stream()
                .filter(f -> f.name() != null && f.name().equalsIgnoreCase(factionName))
                .findFirst()
                .orElse(factionList.get(0));

        factionCBbox.getSelectionModel().select(target);
    }

    // Build the unit tree grouped by category.
    // Filtering is done in-memory (no DB query needed).
    private void rebuildUnitTree(Integer factionId, String nameKeyword) {

        List<Units> filtered = allUnits.stream()
                .filter(u -> factionId == null || u.factionId() == factionId)
                .filter(u -> nameKeyword == null || nameKeyword.isBlank()
                        || u.name().toLowerCase().contains(nameKeyword.toLowerCase()))
                .collect(Collectors.toList());

        Map<Integer, List<Units>> byCategory = filtered.stream()
                .collect(Collectors.groupingBy(Units::category));

        TreeItem<UnitRow> root = new TreeItem<>(UnitRow.category(-1, "ROOT"));
        root.setExpanded(true);

        List<Integer> cats = new ArrayList<>(byCategory.keySet());
        Collections.sort(cats);

        for (Integer cat : cats) {
            String catName = CATEGORY_NAME.getOrDefault(cat, "CATEGORY " + cat);
            TreeItem<UnitRow> catNode = new TreeItem<>(UnitRow.category(cat, catName));
            catNode.setExpanded(false);

            List<Units> unitsInCat = byCategory.get(cat);
            unitsInCat.sort(Comparator.comparing(Units::name, String.CASE_INSENSITIVE_ORDER));

            for (Units u : unitsInCat) {
                catNode.getChildren().add(new TreeItem<>(UnitRow.unit(u)));
            }
            root.getChildren().add(catNode);
        }

        dataTreeTable.setRoot(root);
        dataTreeTable.setShowRoot(false);
    }

    private void showUnitDetails(Units u) {
        // Basic stats
        unitNameLabel.setText(u.name());
        pointLable.setText(String.valueOf(u.points()));
        mLabel.setText(String.valueOf(u.M()));
        tLabel.setText(String.valueOf(u.T()));
        wLabel.setText(String.valueOf(u.W()));
        svLabel.setText(String.valueOf(u.SV()));
        ldLabel.setText(String.valueOf(u.LD()));
        ocLabel.setText(String.valueOf(u.OC()));

        // Invulnerable save
        if (u.invulnerableSave() <= 0) isvLabel.setText("-");
        else isvLabel.setText(String.valueOf(u.invulnerableSave()));

        // Composition & keywords
        unitBox.setText(u.composition() == null ? "" : u.composition());
        keyBox.setText(toUnitKeywordText(u.keywordIdList()));

        // Weapons
        rangedList.setAll(toRangedWeapons(u.rangedWeaponIdList()));
        meleeList.setAll(toMeleeWeapons(u.meleeWeaponIdList()));

        // NEW: Abilities
        coreBox.setText(toCoreAbilityText(u.coreAbilityIdList()));
        mainBox.setText(toOtherAbilityText(u.otherAbilityIdList()));

        // NEW: Faction ability (bound by factionId)
        factionBox.setText(FACTION_ABILITY.getOrDefault(u.factionId(), ""));
    }

    // Clear the unit detail panel (recommended when switching faction)
    private void clearUnitDetails() {
        unitNameLabel.setText("");
        pointLable.setText("");
        mLabel.setText("");
        tLabel.setText("");
        wLabel.setText("");
        svLabel.setText("");
        ldLabel.setText("");
        ocLabel.setText("");
        isvLabel.setText("");

        unitBox.clear();
        keyBox.clear();
        coreBox.clear();
        mainBox.clear();
        factionBox.clear();

        rangedList.clear();
        meleeList.clear();
    }

    private List<RangeWeapons> toRangedWeapons(List<Integer> ids) {
        if (ids == null) return List.of();
        List<RangeWeapons> out = new ArrayList<>();
        for (Integer id : ids) {
            RangeWeapons rw = rangedById.get(id);
            if (rw != null) out.add(rw);
        }
        return out;
    }

    private List<MeleeWeapons> toMeleeWeapons(List<Integer> ids) {
        if (ids == null) return List.of();
        List<MeleeWeapons> out = new ArrayList<>();
        for (Integer id : ids) {
            MeleeWeapons mw = meleeById.get(id);
            if (mw != null) out.add(mw);
        }
        return out;
    }

    private String toUnitKeywordText(List<Integer> keywordIds) {
        if (keywordIds == null || keywordIds.isEmpty()) return "";
        return keywordIds.stream()
                .map(id -> unitKeywordById.getOrDefault(id, "KW#" + id))
                .collect(Collectors.joining(", "));
    }

    private String toWeaponKeywordText(List<Integer> keywordIds) {
        if (keywordIds == null || keywordIds.isEmpty()) return "";
        return keywordIds.stream()
                .map(id -> weaponKeywordById.getOrDefault(id, "WK#" + id))
                .collect(Collectors.joining(", "));
    }

    // NEW: core abilities text builder
    private String toCoreAbilityText(List<Integer> ids) {
        if (ids == null || ids.isEmpty()) return "";
        return ids.stream()
                .map(id -> coreAbilityById.getOrDefault(id, "CORE#" + id))
                .collect(Collectors.joining("\n\n"));
    }

    // NEW: other abilities text builder
    private String toOtherAbilityText(List<Integer> ids) {
        if (ids == null || ids.isEmpty()) return "";
        return ids.stream()
                .map(id -> otherAbilityById.getOrDefault(id, "ABILITY#" + id))
                .collect(Collectors.joining("\n\n"));
    }

    private void showError(String title, String header, Exception e) {
        e.printStackTrace();
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(e.getMessage());
        alert.showAndWait();
    }

    // ======================= UI actions =======================

    @FXML
    void backMainpage(MouseEvent event) throws IOException {
        FixedAspectView.switchTo(
                (Node) event.getSource(),
                "/eecs2311/group2/wh40k_easycombat/MainUI.fxml",
                1200.0,
                800.0
        );
    }

    @FXML
    void search(MouseEvent event) {
        Integer factionId = (factionCBbox.getValue() == null)
                ? null
                : factionCBbox.getValue().id();

        String kw = (searchbox.getText() == null) ? "" : searchbox.getText().trim();
        rebuildUnitTree(factionId, kw);
    }

    @FXML
    void confirm(MouseEvent event) {
        Integer factionId = (factionCBbox.getValue() == null)
                ? null
                : factionCBbox.getValue().id();

        rebuildUnitTree(factionId, null);
        clearUnitDetails();
    }

    @FXML
    void add(MouseEvent event) throws IOException {
        FixedAspectView.switchTo(
                (Node) event.getSource(),
                "/eecs2311/group2/wh40k_easycombat/RuleEditor.fxml",
                1000.0,
                600.0
        );
    }

    @FXML
    void edit(MouseEvent event) {
        // TODO:
        // 1) Get selected UnitRow from the tree.
        // 2) If it is a unit row, open editor and pass unit id.
    }

    // ======================= Tree row model ===================

    public static class UnitRow {
        final boolean categoryRow;
        final int category;
        final Units unit;
        final String displayName;

        private UnitRow(boolean categoryRow, int category, Units unit, String displayName) {
            this.categoryRow = categoryRow;
            this.category = category;
            this.unit = unit;
            this.displayName = displayName;
        }

        static UnitRow category(int category, String name) {
            return new UnitRow(true, category, null, name);
        }

        static UnitRow unit(Units u) {
            return new UnitRow(false, u.category(), u, u.name());
        }

        boolean isCategoryRow() { return categoryRow; }
    }
}
