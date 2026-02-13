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

    // ======================= Buttons ===============================
    @FXML private Button backButton;
    @FXML private Button saveButton;
    @FXML private Button clearbutton;
    @FXML private Button editButton;
    @FXML private Button deleteButton;
    @FXML private Button deleteWeaponButton;
    @FXML private Button keywordAddButton;

    // ======================= ComboBox ==============================
    @FXML private ComboBox<String> categoryCBbox; // MELEE / RANGED

    // ======================= TextFields ============================
    @FXML private TextField nametxt;
    @FXML private TextField rangetxt;
    @FXML private TextField atxt;
    @FXML private TextField bstxt;
    @FXML private TextField stxt;
    @FXML private TextField aptxt;
    @FXML private TextField dtxt;

    // ======================= Tables – Weapons ======================
    @FXML private TableView<RangeWeapons> rangedWepponTable;
    @FXML private TableColumn<RangeWeapons, String> rangeWeaponName;

    @FXML private TableView<MeleeWeapons> meleeWeaponTable;
    @FXML private TableColumn<MeleeWeapons, String> meleeWeaponName;

    // ======================= Tables – Keywords =====================
    @FXML private TableView<WeaponKeywords> selectionKeywordTable; // all keywords in DB
    @FXML private TableColumn<WeaponKeywords, String> selectionKeyword;

    @FXML private TableView<WeaponKeywords> keywordTable; // current weapon keywords
    @FXML private TableColumn<WeaponKeywords, String> words;

    private final StaticDataService data = StaticDataService.getInstance();

    private Units workingUnit;

    private final LinkedHashSet<Integer> selectedRangedIds = new LinkedHashSet<>();
    private final LinkedHashSet<Integer> selectedMeleeIds = new LinkedHashSet<>();

    private final Map<Integer, WeaponKeywords> weaponKeywordById = new HashMap<>();

    private RangeWeapons currentRanged = null;
    private MeleeWeapons currentMelee = null;

    @FXML
    private void initialize() {

        data.loadAll();

        workingUnit = SelectedUnitContext.getSelectedUnit();
        if (workingUnit == null) {
            warn("No Unit", "Please open Weapon Editor from Unit Editor.");
            return;
        }

        if (workingUnit.rangedWeaponIdList() != null) selectedRangedIds.addAll(workingUnit.rangedWeaponIdList());
        if (workingUnit.meleeWeaponIdList() != null) selectedMeleeIds.addAll(workingUnit.meleeWeaponIdList());

        for (WeaponKeywords wk : data.getAllWeaponKeywords()) weaponKeywordById.put(wk.id(), wk);

        // category selector (DEFAULT MELEE)
        categoryCBbox.setItems(FXCollections.observableArrayList("MELEE", "RANGED"));
        categoryCBbox.getSelectionModel().select("MELEE");
        syncRangeField();

        categoryCBbox.valueProperty().addListener((obs, ov, nv) -> syncRangeField());

        setupTables();
        loadEquippedWeapons();
    }

    private void setupTables() {

        rangeWeaponName.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().name()));
        meleeWeaponName.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().name()));

        selectionKeyword.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().keyword()));
        words.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().keyword()));

        selectionKeywordTable.getItems().setAll(
                data.getAllWeaponKeywords().stream()
                        .sorted(Comparator.comparing(WeaponKeywords::keyword, String.CASE_INSENSITIVE_ORDER))
                        .toList()
        );
        selectionKeywordTable.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        keywordTable.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);

        // selecting a weapon loads editor fields
        rangedWepponTable.getSelectionModel().selectedItemProperty().addListener((obs, ov, nv) -> {
            if (nv == null) return;
            meleeWeaponTable.getSelectionModel().clearSelection();
            selectRanged(nv);
        });

        meleeWeaponTable.getSelectionModel().selectedItemProperty().addListener((obs, ov, nv) -> {
            if (nv == null) return;
            rangedWepponTable.getSelectionModel().clearSelection();
            selectMelee(nv);
        });
    }

    private void loadEquippedWeapons() {
        rangedWepponTable.getItems().setAll(
                selectedRangedIds.stream()
                        .map(id -> data.getRangeWeaponById(id).orElse(null))
                        .filter(Objects::nonNull)
                        .toList()
        );

        meleeWeaponTable.getItems().setAll(
                selectedMeleeIds.stream()
                        .map(id -> data.getMeleeWeaponById(id).orElse(null))
                        .filter(Objects::nonNull)
                        .toList()
        );
    }

    private void syncRangeField() {
        boolean melee = "MELEE".equalsIgnoreCase(categoryCBbox.getValue());
        rangetxt.setDisable(melee);
        if (melee) rangetxt.clear();
    }

    private void selectRanged(RangeWeapons w) {
        currentRanged = w;
        currentMelee = null;

        categoryCBbox.getSelectionModel().select("RANGED");
        syncRangeField();

        nametxt.setText(safe(w.name()));
        rangetxt.setText(String.valueOf(w.range()));
        atxt.setText(safe(w.A()));
        bstxt.setText(String.valueOf(w.BS()));
        stxt.setText(String.valueOf(w.S()));
        aptxt.setText(String.valueOf(w.AP()));
        dtxt.setText(safe(w.D()));

        loadCurrentWeaponKeywords(w.keywordIdList());
    }

    private void selectMelee(MeleeWeapons w) {
        currentMelee = w;
        currentRanged = null;

        categoryCBbox.getSelectionModel().select("MELEE");
        syncRangeField();

        nametxt.setText(safe(w.name()));
        atxt.setText(safe(w.A()));
        bstxt.setText(String.valueOf(w.WS())); // WS shown in BS field
        stxt.setText(String.valueOf(w.S()));
        aptxt.setText(String.valueOf(w.AP()));
        dtxt.setText(safe(w.D()));

        loadCurrentWeaponKeywords(w.keywordIdList());
    }

    private void loadCurrentWeaponKeywords(List<Integer> ids) {
        if (ids == null) {
            keywordTable.getItems().clear();
            return;
        }
        keywordTable.getItems().setAll(
                ids.stream().map(weaponKeywordById::get).filter(Objects::nonNull).toList()
        );
    }

    // ======================= Handlers (match FXML) =======================

    @FXML
    void backToUnitEdtior(MouseEvent event) throws IOException {
        FixedAspectView.switchTo((Node) event.getSource(),
                "/eecs2311/group2/wh40k_easycombat/RuleEditor.fxml",
                1000.0, 600.0);
    }

    @FXML
    void ClearText(MouseEvent event) {
        nametxt.clear();
        rangetxt.clear();
        atxt.clear();
        bstxt.clear();
        stxt.clear();
        aptxt.clear();
        dtxt.clear();
        keywordTable.getItems().clear();
        syncRangeField();
    }

    @FXML
    void addKeyword(MouseEvent event) {
        if (currentRanged == null && currentMelee == null) {
            warn("No Weapon Selected", "Please select a weapon first.");
            return;
        }

        List<WeaponKeywords> picked = selectionKeywordTable.getSelectionModel().getSelectedItems();
        if (picked == null || picked.isEmpty()) {
            warn("No Keyword Selected", "Please select keyword(s) to add.");
            return;
        }

        LinkedHashSet<Integer> ids = new LinkedHashSet<>(getCurrentKeywordIds());
        for (WeaponKeywords wk : picked) ids.add(wk.id());

        setCurrentKeywordIds(new ArrayList<>(ids));
        loadCurrentWeaponKeywords(new ArrayList<>(ids));
    }

    @FXML
    void deleteKeyword(MouseEvent event) {
        if (currentRanged == null && currentMelee == null) return;

        List<WeaponKeywords> picked = keywordTable.getSelectionModel().getSelectedItems();
        if (picked == null || picked.isEmpty()) return;

        LinkedHashSet<Integer> ids = new LinkedHashSet<>(getCurrentKeywordIds());
        for (WeaponKeywords wk : picked) ids.remove(wk.id());

        setCurrentKeywordIds(new ArrayList<>(ids));
        loadCurrentWeaponKeywords(new ArrayList<>(ids));
    }

    @FXML
    void deleteWeapon(MouseEvent event) {
        if (currentRanged != null) selectedRangedIds.remove(currentRanged.id());
        if (currentMelee != null) selectedMeleeIds.remove(currentMelee.id());

        currentRanged = null;
        currentMelee = null;

        ClearText(event);
        loadEquippedWeapons();
    }

    @FXML
    void editWeapon(MouseEvent event) {
        if (currentRanged == null && currentMelee == null) {
            warn("No Weapon Selected", "Please select a weapon first.");
            return;
        }

        try {
            applyChangesToCurrentWeapon();
            data.loadAll();
            loadEquippedWeapons();
        } catch (Exception e) {
            error("Update Error", e.getMessage());
        }
    }

    @FXML
    void save(MouseEvent event) throws IOException {
        // Save unit weapon id lists back to context, DB writes happen in editor buttons
        Units updated = new Units(
                workingUnit.id(),
                workingUnit.factionId(),
                workingUnit.name(),
                workingUnit.points(),
                workingUnit.M(),
                workingUnit.T(),
                workingUnit.SV(),
                workingUnit.W(),
                workingUnit.LD(),
                workingUnit.OC(),
                workingUnit.invulnerableSave(),
                workingUnit.category(),
                workingUnit.composition(),
                workingUnit.coreAbilityIdList(),
                workingUnit.otherAbilityIdList(),
                workingUnit.keywordIdList(),
                new ArrayList<>(selectedRangedIds),
                new ArrayList<>(selectedMeleeIds)
        );

        SelectedUnitContext.setSelectedUnit(updated);

        FixedAspectView.switchTo((Node) event.getSource(),
                "/eecs2311/group2/wh40k_easycombat/RuleEditor.fxml",
                1000.0, 600.0);
    }

    // ======================= Core logic =======================

    private void applyChangesToCurrentWeapon() throws SQLException {

        // If current weapon is MELEE, we NEVER read/write range
        if (currentMelee != null) {
            int ws = parseIntStrict(bstxt.getText(), "WS");
            MeleeWeapons updated = new MeleeWeapons(
                    currentMelee.id(),
                    safe(nametxt.getText()),
                    safe(atxt.getText()),
                    ws,
                    parseIntStrict(stxt.getText(), "S"),
                    parseIntStrict(aptxt.getText(), "AP"),
                    safe(dtxt.getText()),
                    getCurrentKeywordIds()
            );
            data.updateMeleeWeapon(updated);
            currentMelee = updated;

            // force UI state
            categoryCBbox.getSelectionModel().select("MELEE");
            syncRangeField();
            return;
        }

        // current ranged -> must have range
        if (currentRanged != null) {
            int range = parseIntStrict(rangetxt.getText(), "Range");
            int bs = parseIntStrict(bstxt.getText(), "BS");

            RangeWeapons updated = new RangeWeapons(
                    currentRanged.id(),
                    safe(nametxt.getText()),
                    range,
                    safe(atxt.getText()),
                    bs,
                    parseIntStrict(stxt.getText(), "S"),
                    parseIntStrict(aptxt.getText(), "AP"),
                    safe(dtxt.getText()),
                    getCurrentKeywordIds()
            );
            data.updateRangeWeapon(updated);
            currentRanged = updated;

            categoryCBbox.getSelectionModel().select("RANGED");
            syncRangeField();
        }
    }

    private List<Integer> getCurrentKeywordIds() {
        if (currentRanged != null) return new ArrayList<>(currentRanged.keywordIdList());
        if (currentMelee != null) return new ArrayList<>(currentMelee.keywordIdList());
        return new ArrayList<>();
    }

    private void setCurrentKeywordIds(List<Integer> ids) {
        if (currentRanged != null) {
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
        }
        if (currentMelee != null) {
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

    private int parseIntStrict(String raw, String fieldName) {
        try { return Integer.parseInt(raw == null ? "" : raw.trim()); }
        catch (Exception e) { throw new IllegalArgumentException(fieldName + " must be an integer."); }
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
