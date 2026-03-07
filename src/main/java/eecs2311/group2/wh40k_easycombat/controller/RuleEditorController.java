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

    // ======================= ComboBox ===================================
    @FXML private ComboBox<Factions> factionCBbox;
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
    @FXML private TableColumn<UnitKeywords, String> keywords;

    // ======================= Service ====================================
    private final StaticDataService data = StaticDataService.getInstance();

    // ======================= Draft state =================================
    private Units workingUnit;               // null => add mode
    private Integer editingUnitId = null;    // null => add mode

    // category only has 3: 1 CHARACTER, 2 INFANTRY, 3 VEHICLE
    private int currentCategory = 2;

    // Unit composition is edited in UnitAbility page; keep it here and carry around.
    private String draftComposition = "";

    // selections
    private final LinkedHashSet<Integer> selectedUnitKeywordIds = new LinkedHashSet<>();
    private final LinkedHashSet<Integer> selectedRangedWeaponIds = new LinkedHashSet<>();
    private final LinkedHashSet<Integer> selectedMeleeWeaponIds = new LinkedHashSet<>();
    private final LinkedHashSet<Integer> selectedCoreAbilityIds = new LinkedHashSet<>();
    private final LinkedHashSet<Integer> selectedOtherAbilityIds = new LinkedHashSet<>();

    // caches for displaying names
    private final Map<Integer, String> unitKeywordById = new HashMap<>();
    private final Map<Integer, String> weaponKeywordById = new HashMap<>();

    private final ObservableList<Factions> factions = FXCollections.observableArrayList();
    private final ObservableList<UnitKeywords> allUnitKeywords = FXCollections.observableArrayList();

    @FXML
    private void initialize() {

        data.loadAll();

        // ---- load caches ----
        factions.setAll(
                data.getAllFactions().stream()
                        .sorted(Comparator.comparing(Factions::name, String.CASE_INSENSITIVE_ORDER))
                        .toList()
        );

        unitKeywordById.clear();
        for (UnitKeywords uk : data.getAllUnitKeywords()) unitKeywordById.put(uk.id(), uk.keyword());

        weaponKeywordById.clear();
        for (WeaponKeywords wk : data.getAllWeaponKeywords()) weaponKeywordById.put(wk.id(), wk.keyword());

        allUnitKeywords.setAll(
                data.getAllUnitKeywords().stream()
                        .sorted(Comparator.comparing(UnitKeywords::keyword, String.CASE_INSENSITIVE_ORDER))
                        .toList()
        );

        setupFactionCombo();
        setupCategoryComboBox();
        setupKeywordTable();
        setupWeaponTables();

        // ---- load from context ----
        workingUnit = SelectedUnitContext.getSelectedUnit();
        if (workingUnit == null) {
            setAddModeDefaults();
        } else {
            loadUnitToUI(workingUnit);
        }
    }

    // ======================= Setup UI =======================

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

    private void setupCategoryComboBox() {
        if (categoryCBbox == null) return;

        categoryCBbox.setItems(FXCollections.observableArrayList(
                "CHARACTER",
                "INFANTRY",
                "VEHICLE"
        ));

        // default INFANTRY
        categoryCBbox.getSelectionModel().select("INFANTRY");
        currentCategory = 2;

        categoryCBbox.valueProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal == null) return;
            switch (newVal) {
                case "CHARACTER" -> currentCategory = 1;
                case "INFANTRY" -> currentCategory = 2;
                case "VEHICLE" -> currentCategory = 3;
                default -> currentCategory = 2;
            }
        });
    }

    private void setupKeywordTable() {
        unitKeywordTable.setItems(allUnitKeywords);
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

    // ======================= Mode load =======================

    private void setAddModeDefaults() {
        editingUnitId = null;

        // default faction: Space Marines
        Factions defaultFaction = factions.stream()
                .filter(f -> f.name() != null && f.name().equalsIgnoreCase("Space Marines"))
                .findFirst()
                .orElse(factions.isEmpty() ? null : factions.get(0));
        factionCBbox.getSelectionModel().select(defaultFaction);

        unitNametxtBox.clear();
        pointtxtBox.clear();
        mBox.clear();
        tBox.clear();
        svBox.clear();
        wBox.clear();
        ldBox.clear();
        ocBOX.clear();
        isvBox.clear();

        currentCategory = 2;
        if (categoryCBbox != null) categoryCBbox.getSelectionModel().select("INFANTRY");

        draftComposition = "";

        selectedUnitKeywordIds.clear();
        selectedRangedWeaponIds.clear();
        selectedMeleeWeaponIds.clear();
        selectedCoreAbilityIds.clear();
        selectedOtherAbilityIds.clear();

        refreshKeywordTextbox();
        refreshWeaponsTables();
    }

    private void loadUnitToUI(Units u) {
        editingUnitId = u.id();
        currentCategory = u.category();
        draftComposition = safe(u.composition());

        if (categoryCBbox != null) {
            switch (currentCategory) {
                case 1 -> categoryCBbox.getSelectionModel().select("CHARACTER");
                case 2 -> categoryCBbox.getSelectionModel().select("INFANTRY");
                case 3 -> categoryCBbox.getSelectionModel().select("VEHICLE");
                default -> categoryCBbox.getSelectionModel().select("INFANTRY");
            }
        }

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
        refreshWeaponsTables();
    }

    private void refreshKeywordTextbox() {
        keywordTextbox.setText(
                selectedUnitKeywordIds.stream()
                        .map(id -> unitKeywordById.getOrDefault(id, "KW#" + id))
                        .collect(Collectors.joining(", "))
        );
    }

    private void refreshWeaponsTables() {
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
        return ids.stream()
                .map(id -> weaponKeywordById.getOrDefault(id, "WK#" + id))
                .collect(Collectors.joining(", "));
    }

    // ======================= Navigation =======================

    @FXML
    void cancel(MouseEvent event) throws IOException {
        SelectedUnitContext.clear();
        FixedAspectView.switchTo((Node) event.getSource(),
                "/eecs2311/group2/wh40k_easycombat/RulesUI.fxml",
                1200.0, 800.0);
    }

    @FXML
    void addWeapon(MouseEvent event) throws IOException {
        SelectedUnitContext.setSelectedUnit(buildDraftUnitFromUI());
        FixedAspectView.switchTo((Node) event.getSource(),
                "/eecs2311/group2/wh40k_easycombat/WeaponEditor.fxml",
                1200.0, 800.0);
    }

    @FXML
    void abilitySetting(MouseEvent event) throws IOException {
        SelectedUnitContext.setSelectedUnit(buildDraftUnitFromUI());
        FixedAspectView.switchTo((Node) event.getSource(),
                "/eecs2311/group2/wh40k_easycombat/UnitAbility.fxml",
                1000.0, 600.0);
    }

    // ======================= Keyword Add =======================

    @FXML
    void keywordAdd(MouseEvent event) {
        List<UnitKeywords> selected = unitKeywordTable.getSelectionModel().getSelectedItems();
        if (selected == null || selected.isEmpty()) {
            showWarning("No Selection", "Please select keyword(s) to add.");
            return;
        }

        for (UnitKeywords k : selected) selectedUnitKeywordIds.add(k.id());
        refreshKeywordTextbox();
    }

    // ======================= Save =======================

    @FXML
    void save(MouseEvent event) {
        try {
            validateRequiredFields();

            Units toSave = buildUnitForDBSave();

            if (toSave.id() == 0) {
                data.addUnit(toSave);
            } else {
                data.updateUnit(toSave);
            }

            data.loadAll();
            SelectedUnitContext.clear();

            FixedAspectView.switchTo((Node) event.getSource(),
                    "/eecs2311/group2/wh40k_easycombat/RulesUI.fxml",
                    1200.0, 800.0);

        } catch (SQLException e) {
            e.printStackTrace();
            showError("Database Error", e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            showError("Save Error", e.getMessage());
        }
    }

    private void validateRequiredFields() {

        if (factionCBbox.getValue() == null) {
            throw new IllegalArgumentException("Faction is required.");
        }

        if (categoryCBbox != null && categoryCBbox.getValue() == null) {
            throw new IllegalArgumentException("Category is required.");
        }

        if (safe(unitNametxtBox.getText()).trim().isEmpty()) {
            throw new IllegalArgumentException("Unit name is required.");
        }

        requireInt(pointtxtBox, "Points");
        requireInt(mBox, "M");
        requireInt(tBox, "T");
        requireInt(svBox, "SV");
        requireInt(wBox, "W");
        requireInt(ldBox, "LD");
        requireInt(ocBOX, "OC");

        String inv = safe(isvBox.getText()).trim();
        if (!inv.isEmpty()) {
            parseIntStrict(inv, "Invulnerable Save");
        }
    }

    private void requireInt(TextField field, String name) {
        String v = safe(field.getText()).trim();
        if (v.isEmpty()) throw new IllegalArgumentException(name + " is required.");
        parseIntStrict(v, name);
    }

    private int parseIntStrict(String raw, String fieldName) {
        try { return Integer.parseInt(raw.trim()); }
        catch (Exception e) { throw new IllegalArgumentException(fieldName + " must be an integer."); }
    }

    // Build a unit snapshot to pass to sub-editors (Weapon/Ability)
    private Units buildDraftUnitFromUI() {

        int id = (editingUnitId == null) ? 0 : editingUnitId;
        int factionId = factionCBbox.getValue() == null ? 0 : factionCBbox.getValue().id();

        // sync category from combobox in case listener didn't fire
        if (categoryCBbox != null && categoryCBbox.getValue() != null) {
            switch (categoryCBbox.getValue()) {
                case "CHARACTER" -> currentCategory = 1;
                case "INFANTRY" -> currentCategory = 2;
                case "VEHICLE" -> currentCategory = 3;
            }
        }

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

    // Build unit for DB save: must use validated fields but also must keep selections (keywords/weapons/abilities)
    private Units buildUnitForDBSave() {

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

        // IMPORTANT: if came back from Weapon/Ability editor, SelectedUnitContext might contain updated lists.
        Units ctx = SelectedUnitContext.getSelectedUnit();
        if (ctx != null) {
            selectedRangedWeaponIds.clear();
            if (ctx.rangedWeaponIdList() != null) selectedRangedWeaponIds.addAll(ctx.rangedWeaponIdList());

            selectedMeleeWeaponIds.clear();
            if (ctx.meleeWeaponIdList() != null) selectedMeleeWeaponIds.addAll(ctx.meleeWeaponIdList());

            selectedCoreAbilityIds.clear();
            if (ctx.coreAbilityIdList() != null) selectedCoreAbilityIds.addAll(ctx.coreAbilityIdList());

            selectedOtherAbilityIds.clear();
            if (ctx.otherAbilityIdList() != null) selectedOtherAbilityIds.addAll(ctx.otherAbilityIdList());

            draftComposition = safe(ctx.composition());
            currentCategory = ctx.category();
        } else {
            // if no ctx (normal save), still sync from combobox
            if (categoryCBbox != null && categoryCBbox.getValue() != null) {
                switch (categoryCBbox.getValue()) {
                    case "CHARACTER" -> currentCategory = 1;
                    case "INFANTRY" -> currentCategory = 2;
                    case "VEHICLE" -> currentCategory = 3;
                }
            }
        }

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

    private int safeInt(String raw) {
        try { return Integer.parseInt(raw == null ? "" : raw.trim()); }
        catch (Exception e) { return 0; }
    }

    private String safe(String s) { return s == null ? "" : s; }

    private void showWarning(String title, String msg) {
        Alert a = new Alert(Alert.AlertType.WARNING);
        a.setTitle(title);
        a.setHeaderText(null);
        a.setContentText(msg);
        a.showAndWait();
    }

    private void showError(String title, String msg) {
        Alert a = new Alert(Alert.AlertType.ERROR);
        a.setTitle(title);
        a.setHeaderText(null);
        a.setContentText(msg);
        a.showAndWait();
    }
}
