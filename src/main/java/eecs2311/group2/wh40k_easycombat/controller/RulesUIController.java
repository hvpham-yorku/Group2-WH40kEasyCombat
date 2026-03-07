package eecs2311.group2.wh40k_easycombat.controller;

import eecs2311.group2.wh40k_easycombat.model.*;
import eecs2311.group2.wh40k_easycombat.service.StaticDataService;
import eecs2311.group2.wh40k_easycombat.util.FixedAspectView;
import eecs2311.group2.wh40k_easycombat.util.SelectedUnitContext;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import java.io.IOException;
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

    // ======================= Service ======================================
    private final StaticDataService data = StaticDataService.getInstance();

    // ======================= In-memory caches (from service) ==============
    private List<Units> allUnits = new ArrayList<>();
    private final ObservableList<Factions> factionList = FXCollections.observableArrayList();

    private final Map<Integer, RangeWeapons> rangedById = new HashMap<>();
    private final Map<Integer, MeleeWeapons> meleeById = new HashMap<>();
    private final Map<Integer, String> unitKeywordById = new HashMap<>();
    private final Map<Integer, String> weaponKeywordById = new HashMap<>();
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

    // Faction ability binding (fill your real texts here)
    private static final Map<Integer, String> FACTION_ABILITY = Map.of(
            1, "Oath of Moment",
            2, "Code Chivalric, Super heavy Walker"
            // 3, "..."
    );

    @FXML
    private void initialize() {

        // 1) Setup columns
        setupTreeColumns();
        setupWeaponColumns();

        // 2) Bind weapon tables to observable lists
        rangedWeaponTable.setItems(rangedList);
        meleeWeaponTable.setItems(meleeList);

        // 3) Load data from service (cache)
        reloadCachesFromService();

        // 4) Setup faction ComboBox
        setupFactionComboBox();

        // 5) Default faction on startup (Space Marines)
        selectDefaultFaction("Space Marines");

        // 6) Build the tree using selected faction
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

        // 8) Style category rows bold
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

        factionCBbox.setCellFactory(lv -> new ListCell<>() {
            @Override
            protected void updateItem(Factions item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? "" : item.name());
            }
        });

        factionCBbox.setButtonCell(new ListCell<>() {
            @Override
            protected void updateItem(Factions item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? "" : item.name());
            }
        });
    }

    private void reloadCachesFromService() {
        // Ensure service is synced (same behavior as "reload from DB" before)
        data.loadAll();

        // Units
        allUnits = data.getAllUnits();

        // Factions
        factionList.setAll(
                data.getAllFactions().stream()
                        .sorted(Comparator.comparing(Factions::name, String.CASE_INSENSITIVE_ORDER))
                        .toList()
        );

        // Weapons maps
        rangedById.clear();
        for (RangeWeapons rw : data.getAllRangeWeapons()) rangedById.put(rw.id(), rw);

        meleeById.clear();
        for (MeleeWeapons mw : data.getAllMeleeWeapons()) meleeById.put(mw.id(), mw);

        // Unit keywords
        unitKeywordById.clear();
        for (UnitKeywords uk : data.getAllUnitKeywords()) unitKeywordById.put(uk.id(), uk.keyword());

        // Weapon keywords
        weaponKeywordById.clear();
        for (WeaponKeywords wk : data.getAllWeaponKeywords()) weaponKeywordById.put(wk.id(), wk.keyword());

        // Abilities
        coreAbilityById.clear();
        for (CoreAbilities ca : data.getAllCoreAbilities()) coreAbilityById.put(ca.id(), ca.ability());

        otherAbilityById.clear();
        for (OtherAbilities oa : data.getAllOtherAbilities()) otherAbilityById.put(oa.id(), oa.ability());
    }

    private void selectDefaultFaction(String defaultFactionName) {
        if (factionList.isEmpty()) return;

        Factions found = null;
        for (Factions f : factionList) {
            if (f != null && f.name() != null && f.name().equalsIgnoreCase(defaultFactionName)) {
                found = f;
                break;
            }
        }
        if (found == null) found = factionList.get(0);

        factionCBbox.getSelectionModel().select(found);
    }

    private void rebuildUnitTree(Integer factionId, String nameFilterLower) {

        List<Units> filtered = allUnits;

        // Filter by faction
        if (factionId != null) {
            filtered = filtered.stream()
                    .filter(u -> u.factionId() == factionId)
                    .collect(Collectors.toList());
        }

        // Filter by name
        if (nameFilterLower != null && !nameFilterLower.isBlank()) {
            String f = nameFilterLower.trim();
            filtered = filtered.stream()
                    .filter(u -> u.name() != null && u.name().toLowerCase().contains(f))
                    .collect(Collectors.toList());
        }

        // Group by category
        Map<Integer, List<Units>> byCat = filtered.stream()
                .collect(Collectors.groupingBy(Units::category));

        TreeItem<UnitRow> root = new TreeItem<>(UnitRow.categoryRoot("ROOT"));
        root.setExpanded(true);

        for (int catId : List.of(1, 2, 3)) {
            String catName = CATEGORY_NAME.getOrDefault(catId, "CATEGORY " + catId);
            TreeItem<UnitRow> catNode = new TreeItem<>(UnitRow.category(catId, catName));
            catNode.setExpanded(true);

            List<Units> units = new ArrayList<>(byCat.getOrDefault(catId, List.of()));
            units.sort(Comparator.comparing(Units::name, Comparator.nullsLast(String.CASE_INSENSITIVE_ORDER)));

            for (Units u : units) {
                catNode.getChildren().add(new TreeItem<>(UnitRow.unit(u)));
            }
            root.getChildren().add(catNode);
        }

        dataTreeTable.setRoot(root);
        dataTreeTable.setShowRoot(false);
    }

    private void showUnitDetails(Units u) {
        if (u == null) {
            clearUnitDetails();
            return;
        }

        unitNameLabel.setText(safe(u.name()));
        pointLable.setText(String.valueOf(u.points()));
        mLabel.setText(String.valueOf(u.M()));
        tLabel.setText(String.valueOf(u.T()));
        svLabel.setText(String.valueOf(u.SV()));
        wLabel.setText(String.valueOf(u.W()));
        ldLabel.setText(String.valueOf(u.LD()));
        ocLabel.setText(String.valueOf(u.OC()));
        isvLabel.setText(u.invulnerableSave() <= 0 ? "-" : String.valueOf(u.invulnerableSave()));

        // keywords
        keyBox.setText(toUnitKeywordText(u.keywordIdList()));

        // core abilities
        coreBox.setText(toCoreAbilityText(u.coreAbilityIdList()));

        // other abilities
        mainBox.setText(toOtherAbilityText(u.otherAbilityIdList()));

        // composition
        unitBox.setText(safe(u.composition()));

        // faction ability bound by factionId
        factionBox.setText(FACTION_ABILITY.getOrDefault(u.factionId(), ""));

        // weapons
        meleeList.setAll(resolveMeleeWeapons(u.meleeWeaponIdList()));
        rangedList.setAll(resolveRangedWeapons(u.rangedWeaponIdList()));
    }

    private void clearUnitDetails() {
        unitNameLabel.setText("");
        pointLable.setText("");
        mLabel.setText("");
        tLabel.setText("");
        svLabel.setText("");
        wLabel.setText("");
        ldLabel.setText("");
        ocLabel.setText("");
        isvLabel.setText("");

        keyBox.clear();
        coreBox.clear();
        mainBox.clear();
        unitBox.clear();
        factionBox.clear();

        meleeList.clear();
        rangedList.clear();
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
        Factions selected = factionCBbox.getValue();
        Integer factionId = (selected == null) ? null : selected.id();

        searchbox.clear();
        rebuildUnitTree(factionId, null);
        clearUnitDetails();
    }

    @FXML
    void search(MouseEvent event) {
        Factions selected = factionCBbox.getValue();
        Integer factionId = (selected == null) ? null : selected.id();

        String q = safe(searchbox.getText()).trim().toLowerCase();
        if (q.isBlank()) rebuildUnitTree(factionId, null);
        else rebuildUnitTree(factionId, q);

        clearUnitDetails();
    }

    @FXML
    void add(MouseEvent event) throws IOException {
        SelectedUnitContext.clear();
        FixedAspectView.switchTo((Node) event.getSource(),
                "/eecs2311/group2/wh40k_easycombat/RuleEditor.fxml",
                1000.0, 600.0);
    }

    @FXML
    void edit(MouseEvent event) throws IOException {
        TreeItem<UnitRow> selected = dataTreeTable.getSelectionModel().getSelectedItem();
        if (selected == null || selected.getValue() == null || selected.getValue().isCategoryRow()) {
            showWarning("No Unit Selected", "Please select a unit row (not a category).");
            return;
        }

        SelectedUnitContext.setSelectedUnit(selected.getValue().unit);
        FixedAspectView.switchTo((Node) event.getSource(),
                "/eecs2311/group2/wh40k_easycombat/RuleEditor.fxml",
                1000.0, 600.0);
    }

    // ----------------------- Text helpers -----------------------

    private String toUnitKeywordText(List<Integer> ids) {
        if (ids == null || ids.isEmpty()) return "";
        return ids.stream()
                .map(id -> unitKeywordById.getOrDefault(id, "KW#" + id))
                .collect(Collectors.joining(", "));
    }

    private String toWeaponKeywordText(List<Integer> ids) {
        if (ids == null || ids.isEmpty()) return "";
        return ids.stream()
                .map(id -> weaponKeywordById.getOrDefault(id, "WK#" + id))
                .collect(Collectors.joining(", "));
    }

    private String toCoreAbilityText(List<Integer> ids) {
        if (ids == null || ids.isEmpty()) return "";
        return ids.stream()
                .map(id -> coreAbilityById.getOrDefault(id, "CORE#" + id))
                .collect(Collectors.joining("\n"));
    }

    private String toOtherAbilityText(List<Integer> ids) {
        if (ids == null || ids.isEmpty()) return "";
        return ids.stream()
                .map(id -> otherAbilityById.getOrDefault(id, "OTHER#" + id))
                .collect(Collectors.joining("\n"));
    }

    private List<MeleeWeapons> resolveMeleeWeapons(List<Integer> ids) {
        if (ids == null || ids.isEmpty()) return List.of();
        return ids.stream()
                .map(meleeById::get)
                .filter(Objects::nonNull)
                .toList();
    }

    private List<RangeWeapons> resolveRangedWeapons(List<Integer> ids) {
        if (ids == null || ids.isEmpty()) return List.of();
        return ids.stream()
                .map(rangedById::get)
                .filter(Objects::nonNull)
                .toList();
    }

    private String safe(String s) {
        return s == null ? "" : s;
    }

    private void showWarning(String title, String msg) {
        Alert a = new Alert(Alert.AlertType.WARNING);
        a.setTitle(title);
        a.setHeaderText(null);
        a.setContentText(msg);
        a.showAndWait();
    }

    // ----------------------- Row model for TreeTable -----------------------

    public static class UnitRow {
        public final String displayName;
        public final Units unit;
        private final boolean categoryRow;
        public final int categoryId;

        private UnitRow(String displayName, Units unit, boolean categoryRow, int categoryId) {
            this.displayName = displayName;
            this.unit = unit;
            this.categoryRow = categoryRow;
            this.categoryId = categoryId;
        }

        public static UnitRow categoryRoot(String name) {
            return new UnitRow(name, null, true, -1);
        }

        public static UnitRow category(int categoryId, String name) {
            return new UnitRow(name, null, true, categoryId);
        }

        public static UnitRow unit(Units u) {
            String n = (u == null || u.name() == null) ? "" : u.name();
            return new UnitRow(n, u, false, (u == null ? -1 : u.category()));
        }

        public boolean isCategoryRow() {
            return categoryRow;
        }
    }
}
