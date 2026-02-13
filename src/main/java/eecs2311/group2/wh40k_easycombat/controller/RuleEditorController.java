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
import java.sql.SQLException;
import java.util.*;
import java.util.stream.Collectors;

public class RuleEditorController {

    // ======================= Buttons ====================================
    @FXML private Button abilityButton;
    @FXML private Button cancelButton;
    @FXML private Button saveButton;

    @FXML private Button unitKeywordAddButton;
    @FXML private Button weaponAddButton;
    @FXML private Button keywordEditButton_Unit;

    // ======================= ComboBox ===================================
    @FXML private ComboBox<Factions> factionCBbox;

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

    // ======================= Tables - Unit Keyword =======================
    @FXML private TableView<UnitKeywords> unitKeywordTable;
    @FXML private TableColumn<UnitKeywords, String> keywords; // you said you added this column

    // ======================= Service ====================================
    private final StaticDataService data = StaticDataService.getInstance();

    // ======================= Draft state =================================
    private Units baseUnit;                 // null => add mode
    private Integer editingUnitId = null;   // null => add mode
    private int currentCategory = 2;        // default INFANTRY (you can change)
    private String draftComposition = "";

    private final LinkedHashSet<Integer> selectedUnitKeywordIds = new LinkedHashSet<>();
    private final LinkedHashSet<Integer> selectedRangedWeaponIds = new LinkedHashSet<>();
    private final LinkedHashSet<Integer> selectedMeleeWeaponIds = new LinkedHashSet<>();
    private final LinkedHashSet<Integer> selectedCoreAbilityIds = new LinkedHashSet<>();
    private final LinkedHashSet<Integer> selectedOtherAbilityIds = new LinkedHashSet<>();

    private final Map<Integer, String> unitKeywordById = new HashMap<>();
    private final Map<Integer, String> weaponKeywordById = new HashMap<>();

    private final ObservableList<Factions> factions = FXCollections.observableArrayList();

    @FXML
    private void initialize() {

        data.loadAll();

        // cache
        factions.setAll(data.getAllFactions().stream()
                .sorted(Comparator.comparing(Factions::name, String.CASE_INSENSITIVE_ORDER))
                .toList());

        unitKeywordById.clear();
        for (UnitKeywords uk : data.getAllUnitKeywords()) unitKeywordById.put(uk.id(), uk.keyword());

        weaponKeywordById.clear();
        for (WeaponKeywords wk : data.getAllWeaponKeywords()) weaponKeywordById.put(wk.id(), wk.keyword());

        setupFactionCombo();
        setupKeywordTable();
        setupWeaponTables();

        // decide add/edit mode
        baseUnit = SelectedUnitContext.getSelectedUnit();
        if (baseUnit == null) {
            setAddModeDefaults();
        } else {
            loadUnitToUI(baseUnit);
        }
    }

    private void setupFactionCombo() {
        factionCBbox.setItems(factions);
        factionCBbox.setCellFactory(lv -> new ListCell<>() {
            @Override protected void updateItem(Factions item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? "" : item.name());
            }
        });
        factionCBbox.setButtonCell(new ListCell<>() {
            @Override protected void updateItem(Factions item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? "" : item.name());
            }
        });
    }

    private void setupKeywordTable() {
        unitKeywordTable.setItems(FXCollections.observableArrayList(
                data.getAllUnitKeywords().stream()
                        .sorted(Comparator.comparing(UnitKeywords::keyword, String.CASE_INSENSITIVE_ORDER))
                        .toList()
        ));
        unitKeywordTable.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        if (keywords != null) {
            keywords.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().keyword()));
        }
    }

    private void setupWeaponTables() {

        // melee
        if (mName != null) mName.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().name()));
        if (mA != null) mA.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().A()));
        if (mWS != null) mWS.setCellValueFactory(d -> new SimpleIntegerProperty(d.getValue().WS()));
        if (mS != null) mS.setCellValueFactory(d -> new SimpleIntegerProperty(d.getValue().S()));
        if (mAP != null) mAP.setCellValueFactory(d -> new SimpleIntegerProperty(d.getValue().AP()));
        if (mD != null) mD.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().D()));
        if (mK != null) mK.setCellValueFactory(d -> new SimpleStringProperty(toWeaponKeywordText(d.getValue().keywordIdList())));

        // ranged
        if (rName != null) rName.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().name()));
        if (rRange != null) rRange.setCellValueFactory(d -> new SimpleIntegerProperty(d.getValue().range()));
        if (rA != null) rA.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().A()));
        if (rBS != null) rBS.setCellValueFactory(d -> new SimpleIntegerProperty(d.getValue().BS()));
        if (rS != null) rS.setCellValueFactory(d -> new SimpleIntegerProperty(d.getValue().S()));
        if (rAP != null) rAP.setCellValueFactory(d -> new SimpleIntegerProperty(d.getValue().AP()));
        if (rD != null) rD.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().D()));
        if (rK != null) rK.setCellValueFactory(d -> new SimpleStringProperty(toWeaponKeywordText(d.getValue().keywordIdList())));
    }

    private void setAddModeDefaults() {
        editingUnitId = null;
        currentCategory = 2;
        draftComposition = "";

        clearInputs();

        // default faction: Space Marines
        Factions defaultFaction = factions.stream()
                .filter(f -> f.name() != null && f.name().equalsIgnoreCase("Space Marines"))
                .findFirst()
                .orElse(factions.isEmpty() ? null : factions.get(0));
        factionCBbox.getSelectionModel().select(defaultFaction);

        refreshKeywordTextbox();
        refreshWeapons();
    }

    private void loadUnitToUI(Units u) {
        editingUnitId = u.id();
        currentCategory = u.category();
        draftComposition = safe(u.composition());

        factionCBbox.getSelectionModel().select(
                factions.stream().filter(f -> f.id() == u.factionId()).findFirst().orElse(null)
        );

        unitNametxtBox.setText(safe(u.name()));
        pointtxtBox.setText(String.valueOf(u.points()));
        mBox.setText(String.valueOf(u.M()));
        tBox.setText(String.valueOf(u.T()));
        svBox.setText(String.valueOf(u.SV()));
        wBox.setText(String.valueOf(u.W()));
        ldBox.setText(String.valueOf(u.LD()));
        ocBOX.setText(String.valueOf(u.OC()));
        isvBox.setText(u.invulnerableSave() <= 0 ? "" : String.valueOf(u.invulnerableSave()));

        selectedUnitKeywordIds.clear();
        if (u.keywordIdList() != null) selectedUnitKeywordIds.addAll(u.keywordIdList());

        selectedRangedWeaponIds.clear();
        if (u.rangedWeaponIdList() != null) selectedRangedWeaponIds.addAll(u.rangedWeaponIdList());

        selectedMeleeWeaponIds.clear();
        if (u.meleeWeaponIdList() != null) selectedMeleeWeaponIds.addAll(u.meleeWeaponIdList());

        selectedCoreAbilityIds.clear();
        if (u.coreAbilityIdList() != null) selectedCoreAbilityIds.addAll(u.coreAbilityIdList());

        selectedOtherAbilityIds.clear();
        if (u.otherAbilityIdList() != null) selectedOtherAbilityIds.addAll(u.otherAbilityIdList());

        refreshKeywordTextbox();
        refreshWeapons();
    }

    private void refreshKeywordTextbox() {
        keywordTextbox.setText(
                selectedUnitKeywordIds.stream()
                        .map(id -> unitKeywordById.getOrDefault(id, "KW#" + id))
                        .collect(Collectors.joining(", "))
        );
    }

    private void refreshWeapons() {

        List<MeleeWeapons> melee = selectedMeleeWeaponIds.stream()
                .map(id -> data.getMeleeWeaponById(id).orElse(null))
                .filter(Objects::nonNull)
                .toList();

        List<RangeWeapons> ranged = selectedRangedWeaponIds.stream()
                .map(id -> data.getRangeWeaponById(id).orElse(null))
                .filter(Objects::nonNull)
                .toList();

        meleeWeaponTable.getItems().setAll(melee);
        rangedWeaponTable.getItems().setAll(ranged);
    }

    private String toWeaponKeywordText(List<Integer> ids) {
        if (ids == null || ids.isEmpty()) return "";
        return ids.stream().map(id -> weaponKeywordById.getOrDefault(id, "WK#" + id))
                .collect(Collectors.joining(", "));
    }

    private void clearInputs() {
        unitNametxtBox.clear();
        pointtxtBox.clear();
        mBox.clear();
        tBox.clear();
        svBox.clear();
        wBox.clear();
        ldBox.clear();
        ocBOX.clear();
        isvBox.clear();

        selectedUnitKeywordIds.clear();
        selectedRangedWeaponIds.clear();
        selectedMeleeWeaponIds.clear();
        selectedCoreAbilityIds.clear();
        selectedOtherAbilityIds.clear();
    }

    // ======================= Actions =======================

    @FXML
    void cancel(MouseEvent event) throws IOException {
        FixedAspectView.switchTo((Node) event.getSource(),
                "/eecs2311/group2/wh40k_easycombat/RulesUI.fxml",
                1200.0, 800.0);
    }

    @FXML
    void keywordAdd(MouseEvent event) {
        List<UnitKeywords> picked = unitKeywordTable.getSelectionModel().getSelectedItems();
        if (picked == null || picked.isEmpty()) {
            warn("No Selection", "Please select keyword(s) to add.");
            return;
        }
        for (UnitKeywords uk : picked) selectedUnitKeywordIds.add(uk.id());
        refreshKeywordTextbox();
    }

    @FXML
    void addWeapon(MouseEvent event) throws IOException {
        SelectedUnitContext.setSelectedUnit(buildDraftUnit());
        FixedAspectView.switchTo((Node) event.getSource(),
                "/eecs2311/group2/wh40k_easycombat/WeaponEditor.fxml",
                1200.0, 800.0);
    }

    @FXML
    void abilitySetting(MouseEvent event) throws IOException {
        SelectedUnitContext.setSelectedUnit(buildDraftUnit());
        FixedAspectView.switchTo((Node) event.getSource(),
                "/eecs2311/group2/wh40k_easycombat/UnitAbility.fxml",
                1000.0, 600.0);
    }

    @FXML
    void save(MouseEvent event) {

        List<String> errors = validateRequiredFields();
        if (!errors.isEmpty()) {
            warn("Missing Required Fields", String.join("\n", errors));
            return;
        }

        try {
            Units u = buildUnitFromUI();

            if (editingUnitId == null) data.addUnit(u);
            else data.updateUnit(u);

            data.loadAll();
            SelectedUnitContext.clear();

            FixedAspectView.switchTo((Node) event.getSource(),
                    "/eecs2311/group2/wh40k_easycombat/RulesUI.fxml",
                    1200.0, 800.0);

        } catch (SQLException e) {
            error("Database Error", e.getMessage());
        } catch (IOException e) {
            error("Navigation Error", e.getMessage());
        } catch (Exception e) {
            error("Error", e.getMessage());
        }
    }

    // ======================= Validation =======================

    private List<String> validateRequiredFields() {
        List<String> errors = new ArrayList<>();

        if (factionCBbox.getValue() == null) errors.add("Faction must be selected.");
        if (safe(unitNametxtBox.getText()).trim().isEmpty()) errors.add("Unit name is required.");

        requireInt(pointtxtBox, "Points", errors);
        requireInt(mBox, "M", errors);
        requireInt(tBox, "T", errors);
        requireInt(svBox, "SV", errors);
        requireInt(wBox, "W", errors);
        requireInt(ldBox, "LD", errors);
        requireInt(ocBOX, "OC", errors);

        String inv = safe(isvBox.getText()).trim();
        if (!inv.isEmpty() && !isInt(inv)) errors.add("Invulnerable Save must be an integer.");

        // You said user must provide all data; keep at least one keyword required
        if (selectedUnitKeywordIds.isEmpty()) errors.add("At least one Unit Keyword is required.");

        return errors;
    }

    private void requireInt(TextField field, String name, List<String> errors) {
        String v = safe(field.getText()).trim();
        if (v.isEmpty()) { errors.add(name + " is required."); return; }
        if (!isInt(v)) errors.add(name + " must be an integer.");
    }

    private boolean isInt(String s) {
        try { Integer.parseInt(s.trim()); return true; }
        catch (Exception e) { return false; }
    }

    // ======================= Build Units =======================

    private Units buildUnitFromUI() {

        int id = (editingUnitId == null) ? 0 : editingUnitId;

        int factionId = factionCBbox.getValue().id();
        String name = safe(unitNametxtBox.getText()).trim();

        int points = Integer.parseInt(pointtxtBox.getText().trim());
        int m = Integer.parseInt(mBox.getText().trim());
        int t = Integer.parseInt(tBox.getText().trim());
        int sv = Integer.parseInt(svBox.getText().trim());
        int w = Integer.parseInt(wBox.getText().trim());
        int ld = Integer.parseInt(ldBox.getText().trim());
        int oc = Integer.parseInt(ocBOX.getText().trim());

        int inv = 0;
        String invText = safe(isvBox.getText()).trim();
        if (!invText.isEmpty()) inv = Integer.parseInt(invText);

        return new Units(
                id,
                factionId,
                name,
                points,
                m,
                t,
                sv,
                w,
                ld,
                oc,
                inv,
                currentCategory,
                draftComposition,
                new ArrayList<>(selectedCoreAbilityIds),
                new ArrayList<>(selectedOtherAbilityIds),
                new ArrayList<>(selectedUnitKeywordIds),
                new ArrayList<>(selectedRangedWeaponIds),
                new ArrayList<>(selectedMeleeWeaponIds)
        );
    }

    private Units buildDraftUnit() {
        int id = (editingUnitId == null) ? 0 : editingUnitId;
        int factionId = (factionCBbox.getValue() == null) ? 0 : factionCBbox.getValue().id();

        return new Units(
                id,
                factionId,
                safe(unitNametxtBox.getText()).trim(),
                safeInt(pointtxtBox.getText()),
                safeInt(mBox.getText()),
                safeInt(tBox.getText()),
                safeInt(svBox.getText()),
                safeInt(wBox.getText()),
                safeInt(ldBox.getText()),
                safeInt(ocBOX.getText()),
                safeInt(isvBox.getText()),
                currentCategory,
                draftComposition,
                new ArrayList<>(selectedCoreAbilityIds),
                new ArrayList<>(selectedOtherAbilityIds),
                new ArrayList<>(selectedUnitKeywordIds),
                new ArrayList<>(selectedRangedWeaponIds),
                new ArrayList<>(selectedMeleeWeaponIds)
        );
    }

    private int safeInt(String raw) {
        try { return Integer.parseInt(raw == null ? "" : raw.trim()); }
        catch (Exception e) { return 0; }
    }

    private String safe(String s) { return s == null ? "" : s; }

    private void warn(String title, String msg) {
        Alert a = new Alert(Alert.AlertType.WARNING);
        a.setTitle(title);
        a.setHeaderText(null);
        a.setContentText(msg);
        a.showAndWait();
    }

    private void error(String title, String msg) {
        Alert a = new Alert(Alert.AlertType.ERROR);
        a.setTitle(title);
        a.setHeaderText(null);
        a.setContentText(msg);
        a.showAndWait();
    }
}
