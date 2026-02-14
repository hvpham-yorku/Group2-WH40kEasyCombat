package eecs2311.group2.wh40k_easycombat.controller;

import eecs2311.group2.wh40k_easycombat.model.CoreAbilities;
import eecs2311.group2.wh40k_easycombat.model.Factions;
import eecs2311.group2.wh40k_easycombat.model.OtherAbilities;
import eecs2311.group2.wh40k_easycombat.model.Units;
import eecs2311.group2.wh40k_easycombat.service.StaticDataService;
import eecs2311.group2.wh40k_easycombat.util.FixedAspectView;
import eecs2311.group2.wh40k_easycombat.util.SelectedUnitContext;
import javafx.beans.property.SimpleStringProperty;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import java.io.IOException;
import java.sql.SQLException;
import java.util.*;

public class UnitAbilityController {

    // ======================= Buttons ==============================
    @FXML private Button addCoreButton;
    @FXML private Button saveButton;
    @FXML private Button cancelButton;
    @FXML private Button deleteButton;
    @FXML private Button editCompositionButton;

    // ======================= Text Inputs ==========================
    @FXML private TextField compositionTxt;
    @FXML private TextField factiontxt;
    @FXML private TextArea maintxt; // other ability text

    // ======================= Tables – Selected Core Abilities (WORD) ======
    @FXML private TableView<CoreAbilities> coreTable;
    @FXML private TableColumn<CoreAbilities, String> core;

    // ======================= Tables – All Core Abilities (SELECT) =========
    @FXML private TableView<CoreAbilities> selectCoreTable;
    @FXML private TableColumn<CoreAbilities, String> selectCore;

    private final StaticDataService data = StaticDataService.getInstance();

    private Units workingUnit;

    // store selected core ability ids
    private final LinkedHashSet<Integer> selectedCoreIds = new LinkedHashSet<>();

    // store other ability ids of unit (usually 0 or 1 in your design)
    private final ArrayList<Integer> otherAbilityIds = new ArrayList<>();

    @FXML
    private void initialize() {

        data.loadAll();

        workingUnit = SelectedUnitContext.getSelectedUnit();
        if (workingUnit == null) {
            showError("No Unit", "Please open UnitAbility from UnitEditor.");
            return;
        }

        // --- setup columns ---
        if (core != null) {
            core.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().ability()));
        }
        if (selectCore != null) {
            selectCore.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().ability()));
        }

        // --- load faction text (read-only display) ---
        Factions f = data.getFactionById(workingUnit.factionId()).orElse(null);
        factiontxt.setText(f == null ? ("Faction#" + workingUnit.factionId()) : f.name());
        factiontxt.setEditable(false);

        // --- composition ---
        compositionTxt.setText(safe(workingUnit.composition()));

        // --- core abilities ---
        selectedCoreIds.clear();
        if (workingUnit.coreAbilityIdList() != null) selectedCoreIds.addAll(workingUnit.coreAbilityIdList());

        // select table: ALL core abilities from DB
        List<CoreAbilities> allCore = data.getAllCoreAbilities().stream()
                .sorted(Comparator.comparing(CoreAbilities::ability, String.CASE_INSENSITIVE_ORDER))
                .toList();
        selectCoreTable.getItems().setAll(allCore);

        // word table: unit selected core
        refreshSelectedCoreTable();

        // --- other ability text ---
        otherAbilityIds.clear();
        if (workingUnit.otherAbilityIdList() != null) otherAbilityIds.addAll(workingUnit.otherAbilityIdList());

        // if unit has existing other ability id, load its text; otherwise blank
        if (!otherAbilityIds.isEmpty()) {
            int id = otherAbilityIds.get(0);
            String txt = data.getOtherAbilityById(id).map(OtherAbilities::ability).orElse("");
            maintxt.setText(txt);
        } else {
            maintxt.clear();
        }

        // optional: composition edit button toggles disable state
        if (editCompositionButton != null) {
            compositionTxt.setDisable(false); // allow editing by default
        }
    }

    private void refreshSelectedCoreTable() {
        List<CoreAbilities> selected = selectedCoreIds.stream()
                .map(id -> data.getCoreAbilityById(id).orElse(null))
                .filter(Objects::nonNull)
                .sorted(Comparator.comparing(CoreAbilities::ability, String.CASE_INSENSITIVE_ORDER))
                .toList();
        coreTable.getItems().setAll(selected);
    }

    // When click "ADD" button, add the core ability to unit
    @FXML
    void addCore(MouseEvent event) {
        CoreAbilities picked = selectCoreTable.getSelectionModel().getSelectedItem();
        if (picked == null) {
            showWarning("No Selection", "Please select a core ability to add.");
            return;
        }
        selectedCoreIds.add(picked.id());
        refreshSelectedCoreTable();
    }

    // When click "Delete" button, delete the core ability of unit
    @FXML
    void delete(MouseEvent event) {
        CoreAbilities picked = coreTable.getSelectionModel().getSelectedItem();
        if (picked == null) {
            showWarning("No Selection", "Please select a core ability to delete.");
            return;
        }
        selectedCoreIds.remove(picked.id());
        refreshSelectedCoreTable();
    }

    // When click "Edit" button, edit and save the composition of unit
    @FXML
    void editComposition(MouseEvent event) {
        // simple toggle enable/disable
        compositionTxt.setDisable(!compositionTxt.isDisable());
    }

    // When click "Save" button, save all abilities to unit
    @FXML
    void save(MouseEvent event) {

        try {
            // --- validate composition required ---
            String composition = safe(compositionTxt.getText()).trim();
            if (composition.isEmpty()) {
                throw new IllegalArgumentException("Composition is required.");
            }

            // --- handle OTHER ability (text stored in DB table other_abilities) ---
            String otherText = safe(maintxt.getText()).trim();

            ArrayList<Integer> newOtherIds = new ArrayList<>(otherAbilityIds);

            // Edit unit: if has existing other ability id -> overwrite text (update)
            // New unit: create a new other ability when text not empty
            if (!newOtherIds.isEmpty()) {
                int otherId = newOtherIds.get(0);
                OtherAbilities updatedOther = new OtherAbilities(otherId, otherText);
                data.updateOtherAbility(updatedOther);
            } else {
                // no other id yet
                if (!otherText.isEmpty()) {
                    int newId = data.addOtherAbility(new OtherAbilities(0, otherText));
                    newOtherIds.clear();
                    newOtherIds.add(newId);
                } else {
                    // keep empty list if user didn't enter other ability
                    newOtherIds.clear();
                }
            }

            // --- build updated unit for context (DO NOT write unit to DB here) ---
            Units updatedUnit = new Units(
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
                    composition,
                    new ArrayList<>(selectedCoreIds),
                    newOtherIds,
                    workingUnit.keywordIdList(),
                    workingUnit.rangedWeaponIdList(),
                    workingUnit.meleeWeaponIdList()
            );

            SelectedUnitContext.setSelectedUnit(updatedUnit);

            // back to editor
            FixedAspectView.switchTo((Node) event.getSource(),
                    "/eecs2311/group2/wh40k_easycombat/RuleEditor.fxml",
                    1000.0, 600.0);

        } catch (SQLException e) {
            e.printStackTrace();
            showError("Database Error", e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            showError("Save Error", e.getMessage());
        }
    }

    // When click "Cancel" button, will back to RuleEditor page
    @FXML
    void cancelChange(MouseEvent event) throws IOException {
        FixedAspectView.switchTo((Node) event.getSource(),
                "/eecs2311/group2/wh40k_easycombat/RuleEditor.fxml",
                1000.0, 600.0);
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

    private void showError(String title, String msg) {
        Alert a = new Alert(Alert.AlertType.ERROR);
        a.setTitle(title);
        a.setHeaderText(null);
        a.setContentText(msg);
        a.showAndWait();
    }
}
