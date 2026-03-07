package eecs2311.group2.wh40k_easycombat.controller;

import eecs2311.group2.wh40k_easycombat.model.*;
import eecs2311.group2.wh40k_easycombat.service.StaticDataService;
import eecs2311.group2.wh40k_easycombat.util.FixedAspectView;
import eecs2311.group2.wh40k_easycombat.util.SelectedUnitContext;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import java.io.IOException;
import java.sql.SQLException;
import java.util.*;

public class WeaponEditorController {

    // ======================= ComboBox ==============================
    @FXML private ComboBox<String> categoryCBbox; // "MELEE" / "RANGED"

    // ======================= TextFields ============================
    @FXML private TextField nametxt;
    @FXML private TextField rangetxt;
    @FXML private TextField atxt;
    @FXML private TextField bstxt;  // MELEE uses WS, RANGED uses BS
    @FXML private TextField stxt;
    @FXML private TextField aptxt;
    @FXML private TextField dtxt;

    // ======================= Tables – Weapons ======================
    @FXML private TableView<RangeWeapons> rangedWepponTable;
    @FXML private TableColumn<RangeWeapons, String> rangeWeaponName;

    @FXML private TableView<MeleeWeapons> meleeWeaponTable;
    @FXML private TableColumn<MeleeWeapons, String> meleeWeaponName;

    // ======================= Tables – Keywords =====================
    @FXML private TableView<WeaponKeywords> selectionKeywordTable; // DB keywords
    @FXML private TableColumn<WeaponKeywords, String> selectionKeyword;

    @FXML private TableView<WeaponKeywords> keywordTable; // current weapon keywords
    @FXML private TableColumn<WeaponKeywords, String> words;

    // ======================= Service / State =======================
    private final StaticDataService data = StaticDataService.getInstance();
    private Units workingUnit;

    private final LinkedHashSet<Integer> selectedRangedIds = new LinkedHashSet<>();
    private final LinkedHashSet<Integer> selectedMeleeIds = new LinkedHashSet<>();

    // caches
    private final Map<Integer, WeaponKeywords> weaponKeywordById = new HashMap<>();

    // currently selected weapon (edit mode)
    private RangeWeapons currentRanged = null;
    private MeleeWeapons currentMelee = null;

    // if true => editing ranged weapon, else melee (only meaningful when a weapon is selected)
    private boolean editingRanged = false;

    // if no weapon selected => creating new weapon using form fields
    private boolean creatingNewWeapon = true;

    @FXML
    private void initialize() {

        data.loadAll();

        workingUnit = SelectedUnitContext.getSelectedUnit();
        if (workingUnit == null) {
            showWarning("No Unit", "Please open Weapon Editor from Unit Editor.");
            return;
        }

        if (workingUnit.rangedWeaponIdList() != null) selectedRangedIds.addAll(workingUnit.rangedWeaponIdList());
        if (workingUnit.meleeWeaponIdList() != null) selectedMeleeIds.addAll(workingUnit.meleeWeaponIdList());

        weaponKeywordById.clear();
        for (WeaponKeywords wk : data.getAllWeaponKeywords()) weaponKeywordById.put(wk.id(), wk);

        setupCategory();
        setupTables();
        loadEquippedWeapons();

        // start in "new weapon" mode
        enterCreateMode();
    }

    // ======================= Setup =======================

    private void setupCategory() {
        categoryCBbox.setItems(FXCollections.observableArrayList("MELEE", "RANGED"));
        categoryCBbox.getSelectionModel().select("MELEE"); // default MELEE
        syncRangeField();

        categoryCBbox.valueProperty().addListener((obs, ov, nv) -> syncRangeField());
    }

    private void syncRangeField() {
        boolean melee = "MELEE".equalsIgnoreCase(categoryCBbox.getValue());
        rangetxt.setDisable(melee);
        if (melee) rangetxt.clear();
    }

    private void setupTables() {

        // weapon tables columns
        rangeWeaponName.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().name()));
        meleeWeaponName.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().name()));

        // keyword columns
        selectionKeyword.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().keyword()));
        words.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().keyword()));

        meleeWeaponTable.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
        rangedWepponTable.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);

        selectionKeywordTable.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        keywordTable.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);

        // load all weapon keywords into selection table
        selectionKeywordTable.getItems().setAll(
                data.getAllWeaponKeywords().stream()
                        .sorted(Comparator.comparing(WeaponKeywords::keyword, String.CASE_INSENSITIVE_ORDER))
                        .toList()
        );

        // selecting melee weapon
        meleeWeaponTable.getSelectionModel().selectedItemProperty().addListener((obs, ov, nv) -> {
            if (nv == null) return;
            rangedWepponTable.getSelectionModel().clearSelection();
            selectMelee(nv);
        });

        // selecting ranged weapon
        rangedWepponTable.getSelectionModel().selectedItemProperty().addListener((obs, ov, nv) -> {
            if (nv == null) return;
            meleeWeaponTable.getSelectionModel().clearSelection();
            selectRanged(nv);
        });
    }

    private void loadEquippedWeapons() {

        List<MeleeWeapons> melee = selectedMeleeIds.stream()
                .map(id -> data.getMeleeWeaponById(id).orElse(null))
                .filter(Objects::nonNull)
                .sorted(Comparator.comparing(MeleeWeapons::name, String.CASE_INSENSITIVE_ORDER))
                .toList();

        List<RangeWeapons> ranged = selectedRangedIds.stream()
                .map(id -> data.getRangeWeaponById(id).orElse(null))
                .filter(Objects::nonNull)
                .sorted(Comparator.comparing(RangeWeapons::name, String.CASE_INSENSITIVE_ORDER))
                .toList();

        meleeWeaponTable.getItems().setAll(melee);
        rangedWepponTable.getItems().setAll(ranged);
    }

    // ======================= Select weapon =======================

    private void selectMelee(MeleeWeapons w) {
        creatingNewWeapon = false;
        editingRanged = false;
        currentMelee = w;
        currentRanged = null;

        categoryCBbox.getSelectionModel().select("MELEE");
        syncRangeField();

        nametxt.setText(safe(w.name()));
        atxt.setText(safe(w.A()));
        bstxt.setText(String.valueOf(w.WS())); // WS in bstxt
        stxt.setText(String.valueOf(w.S()));
        aptxt.setText(String.valueOf(w.AP()));
        dtxt.setText(safe(w.D()));
        rangetxt.clear();

        loadKeywordsToKeywordTable(w.keywordIdList());
    }

    private void selectRanged(RangeWeapons w) {
        creatingNewWeapon = false;
        editingRanged = true;
        currentRanged = w;
        currentMelee = null;

        categoryCBbox.getSelectionModel().select("RANGED");
        syncRangeField();

        nametxt.setText(safe(w.name()));
        rangetxt.setText(String.valueOf(w.range()));
        atxt.setText(safe(w.A()));
        bstxt.setText(String.valueOf(w.BS())); // BS in bstxt
        stxt.setText(String.valueOf(w.S()));
        aptxt.setText(String.valueOf(w.AP()));
        dtxt.setText(safe(w.D()));

        loadKeywordsToKeywordTable(w.keywordIdList());
    }

    private void loadKeywordsToKeywordTable(List<Integer> keywordIds) {
        if (keywordIds == null || keywordIds.isEmpty()) {
            keywordTable.getItems().clear();
            return;
        }
        keywordTable.getItems().setAll(
                keywordIds.stream()
                        .map(weaponKeywordById::get)
                        .filter(Objects::nonNull)
                        .sorted(Comparator.comparing(WeaponKeywords::keyword, String.CASE_INSENSITIVE_ORDER))
                        .toList()
        );
    }

    private void enterCreateMode() {
        creatingNewWeapon = true;
        currentMelee = null;
        currentRanged = null;
        meleeWeaponTable.getSelectionModel().clearSelection();
        rangedWepponTable.getSelectionModel().clearSelection();

        // default new weapon type = MELEE
        categoryCBbox.getSelectionModel().select("MELEE");
        syncRangeField();

        clearFieldsOnly();
        keywordTable.getItems().clear();
    }

    // ======================= FXML handlers (must match FXML) =======================

    @FXML
    void backToUnitEdtior(MouseEvent event) throws IOException {
        FixedAspectView.switchTo((Node) event.getSource(),
                "/eecs2311/group2/wh40k_easycombat/RuleEditor.fxml",
                1000.0, 600.0);
    }

    @FXML
    void ClearText(MouseEvent event) {
        enterCreateMode();
    }

    // Add keyword(s) from DB selection to keywordTable
    @FXML
    void addKeyword(MouseEvent event) {

        List<WeaponKeywords> picked = selectionKeywordTable.getSelectionModel().getSelectedItems();
        if (picked == null || picked.isEmpty()) {
            showWarning("No Keyword Selected", "Please select keyword(s) to add.");
            return;
        }

        LinkedHashSet<Integer> ids = new LinkedHashSet<>(getKeywordIdsFromKeywordTable());
        for (WeaponKeywords wk : picked) ids.add(wk.id());

        keywordTable.getItems().setAll(
                ids.stream().map(weaponKeywordById::get).filter(Objects::nonNull).toList()
        );

        // if editing existing weapon, also update current object in memory
        if (!creatingNewWeapon) {
            setCurrentWeaponKeywordIds(new ArrayList<>(ids));
        }
    }

    // Delete keyword(s) from keywordTable
    @FXML
    void deleteKeyword(MouseEvent event) {

        List<WeaponKeywords> picked = keywordTable.getSelectionModel().getSelectedItems();
        if (picked == null || picked.isEmpty()) {
            showWarning("No Keyword Selected", "Please select keyword(s) to delete.");
            return;
        }

        LinkedHashSet<Integer> ids = new LinkedHashSet<>(getKeywordIdsFromKeywordTable());
        for (WeaponKeywords wk : picked) ids.remove(wk.id());

        keywordTable.getItems().setAll(
                ids.stream().map(weaponKeywordById::get).filter(Objects::nonNull).toList()
        );

        if (!creatingNewWeapon) {
            setCurrentWeaponKeywordIds(new ArrayList<>(ids));
        }
    }

    // Update selected weapon (no navigation)
    @FXML
    void editWeapon(MouseEvent event) {
        if (creatingNewWeapon || (currentMelee == null && currentRanged == null)) {
            showWarning("No Weapon Selected", "Please select a weapon to edit, or use Save to create a new weapon.");
            return;
        }

        try {
            applyFieldsToSelectedWeaponAndPersist();
            data.loadAll();
            loadEquippedWeapons();
        } catch (Exception e) {
            showError("Update Error", "Failed to update weapon.", e);
        }
    }

    // Remove weapon from unit loadout (NOT deleting DB record)
    @FXML
    void deleteWeapon(MouseEvent event) {

        if (currentMelee == null && currentRanged == null) {
            showWarning("No Weapon Selected", "Please select a weapon to remove from unit.");
            return;
        }

        if (editingRanged && currentRanged != null) {
            selectedRangedIds.remove(currentRanged.id());
        } else if (!editingRanged && currentMelee != null) {
            selectedMeleeIds.remove(currentMelee.id());
        }

        enterCreateMode();
        loadEquippedWeapons();
    }

    // Save:
    // - if no weapon selected => INSERT new weapon and bind to unit
    // - if weapon selected => UPDATE weapon and keep binding
    // Then write unit back to SelectedUnitContext and return
    @FXML
    void save(MouseEvent event) throws IOException {

        try {
            if (creatingNewWeapon) {
                int newId = createNewWeaponFromFieldsAndInsert();
                // bind to unit
                if ("RANGED".equalsIgnoreCase(categoryCBbox.getValue())) {
                    selectedRangedIds.add(newId);
                } else {
                    selectedMeleeIds.add(newId);
                }
                data.loadAll();
                loadEquippedWeapons();
                enterCreateMode(); // keep ready for next new weapon
            } else {
                // editing existing selected weapon => update DB
                applyFieldsToSelectedWeaponAndPersist();
                data.loadAll();
                loadEquippedWeapons();
            }

            Units updatedUnit = rebuildUnitWithWeapons(
                    workingUnit,
                    new ArrayList<>(selectedRangedIds),
                    new ArrayList<>(selectedMeleeIds)
            );
            SelectedUnitContext.setSelectedUnit(updatedUnit);

        } catch (Exception e) {
            showError("Save Error", "Failed to save weapon.", e);
            return; // stay on page
        }

        FixedAspectView.switchTo((Node) event.getSource(),
                "/eecs2311/group2/wh40k_easycombat/RuleEditor.fxml",
                1000.0, 600.0);
    }

    // ======================= Core logic =======================

    private int createNewWeaponFromFieldsAndInsert() throws SQLException {

        String type = categoryCBbox.getValue();
        if (type == null) type = "MELEE";

        String name = safe(nametxt.getText()).trim();
        if (name.isEmpty()) throw new IllegalArgumentException("Name is required.");

        String a = safe(atxt.getText()).trim();
        String d = safe(dtxt.getText()).trim();

        int s = parseIntStrict(stxt.getText(), "S");
        int ap = parseIntStrict(aptxt.getText(), "AP");

        List<Integer> keywordIds = getKeywordIdsFromKeywordTable();

        if ("RANGED".equalsIgnoreCase(type)) {
            int range = parseIntStrict(rangetxt.getText(), "Range");
            int bs = parseIntStrict(bstxt.getText(), "BS");
            RangeWeapons nw = new RangeWeapons(0, name, range, a, bs, s, ap, d, keywordIds);
            return data.addRangeWeapon(nw);
        } else {
            // MELEE: ignore range
            int ws = parseIntStrict(bstxt.getText(), "WS");
            MeleeWeapons nw = new MeleeWeapons(0, name, a, ws, s, ap, d, keywordIds);
            return data.addMeleeWeapon(nw);
        }
    }

    private void applyFieldsToSelectedWeaponAndPersist() throws SQLException {

        String name = safe(nametxt.getText()).trim();
        if (name.isEmpty()) throw new IllegalArgumentException("Name is required.");

        String a = safe(atxt.getText()).trim();
        String d = safe(dtxt.getText()).trim();

        int s = parseIntStrict(stxt.getText(), "S");
        int ap = parseIntStrict(aptxt.getText(), "AP");

        List<Integer> keywordIds = getKeywordIdsFromKeywordTable();

        if (editingRanged && currentRanged != null) {
            int range = parseIntStrict(rangetxt.getText(), "Range");
            int bs = parseIntStrict(bstxt.getText(), "BS");

            RangeWeapons updated = new RangeWeapons(
                    currentRanged.id(), name, range, a, bs, s, ap, d, keywordIds
            );
            data.updateRangeWeapon(updated);
            currentRanged = updated;

        } else if (!editingRanged && currentMelee != null) {
            int ws = parseIntStrict(bstxt.getText(), "WS");

            MeleeWeapons updated = new MeleeWeapons(
                    currentMelee.id(), name, a, ws, s, ap, d, keywordIds
            );
            data.updateMeleeWeapon(updated);
            currentMelee = updated;
        }
    }

    private List<Integer> getKeywordIdsFromKeywordTable() {
        if (keywordTable == null) return new ArrayList<>();
        return keywordTable.getItems().stream()
                .map(WeaponKeywords::id)
                .distinct()
                .toList();
    }

    private void setCurrentWeaponKeywordIds(List<Integer> ids) {
        if (editingRanged && currentRanged != null) {
            currentRanged = new RangeWeapons(
                    currentRanged.id(),
                    currentRanged.name(),
                    currentRanged.range(),
                    currentRanged.A(),
                    currentRanged.BS(),
                    currentRanged.S(),
                    currentRanged.AP(),
                    currentRanged.D(),
                    ids
            );
        } else if (!editingRanged && currentMelee != null) {
            currentMelee = new MeleeWeapons(
                    currentMelee.id(),
                    currentMelee.name(),
                    currentMelee.A(),
                    currentMelee.WS(),
                    currentMelee.S(),
                    currentMelee.AP(),
                    currentMelee.D(),
                    ids
            );
        }
    }

    private Units rebuildUnitWithWeapons(Units base, List<Integer> rangedIds, List<Integer> meleeIds) {
        return new Units(
                base.id(),
                base.factionId(),
                base.name(),
                base.points(),
                base.M(),
                base.T(),
                base.SV(),
                base.W(),
                base.LD(),
                base.OC(),
                base.invulnerableSave(),
                base.category(),
                base.composition(),
                base.coreAbilityIdList(),
                base.otherAbilityIdList(),
                base.keywordIdList(),
                rangedIds,
                meleeIds
        );
    }

    // ======================= small helpers =======================

    private void clearFieldsOnly() {
        nametxt.clear();
        rangetxt.clear();
        atxt.clear();
        bstxt.clear();
        stxt.clear();
        aptxt.clear();
        dtxt.clear();
        syncRangeField();
    }

    private int parseIntStrict(String raw, String fieldName) {
        try {
            return Integer.parseInt(raw == null ? "" : raw.trim());
        } catch (Exception e) {
            throw new IllegalArgumentException(fieldName + " must be an integer.");
        }
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

    private void showError(String title, String header, Exception e) {
        e.printStackTrace();
        Alert a = new Alert(Alert.AlertType.ERROR);
        a.setTitle(title);
        a.setHeaderText(header);
        a.setContentText(e.getMessage());
        a.showAndWait();
    }
}
