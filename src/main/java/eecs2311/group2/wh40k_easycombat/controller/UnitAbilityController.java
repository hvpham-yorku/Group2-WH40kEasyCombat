package eecs2311.group2.wh40k_easycombat.controller;

import eecs2311.group2.wh40k_easycombat.model.*;
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
import java.util.stream.Collectors;

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
    @FXML private TextArea maintxt; // Other ability text

    // ======================= Tables – Core Abilities ===============
    // IMPORTANT: You said word/select were reversed.
    // We'll use:
    //   selectCoreTable = ALL core abilities (selection)
    //   coreTable       = unit selected core abilities (words/current)
    @FXML private TableView<CoreAbilities> coreTable;
    @FXML private TableColumn<CoreAbilities, String> core;

    @FXML private TableView<CoreAbilities> selectCoreTable;
    @FXML private TableColumn<CoreAbilities, String> selectCore;

    private final StaticDataService data = StaticDataService.getInstance();

    private Units workingUnit;

    private final List<CoreAbilities> allCore = new ArrayList<>();
    private final LinkedHashSet<Integer> selectedCoreIds = new LinkedHashSet<>();

    @FXML
    private void initialize() {

        data.loadAll();

        workingUnit = SelectedUnitContext.getSelectedUnit();
        if (workingUnit == null) {
            warn("No Unit", "Please open Unit Ability from Unit Editor.");
            return;
        }

        // faction display
        factiontxt.setEditable(false);
        factiontxt.setText(
                data.getFactionById(workingUnit.factionId())
                        .map(Factions::name)
                        .orElse("")
        );

        // composition
        compositionTxt.setText(workingUnit.composition() == null ? "" : workingUnit.composition());

        // other ability text
        maintxt.setText(loadOtherAbilityText(workingUnit.otherAbilityIdList()));

        setupTables();
        loadCoreData();
        loadSelectedCoreFromUnit();
    }

    private void setupTables() {

        // coreTable = selected/current
        core.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().ability()));

        // selectCoreTable = all selection
        selectCore.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().ability()));

        coreTable.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        selectCoreTable.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
    }

    private void loadCoreData() {
        allCore.clear();
        allCore.addAll(
                data.getAllCoreAbilities().stream()
                        .sorted(Comparator.comparing(CoreAbilities::ability, String.CASE_INSENSITIVE_ORDER))
                        .toList()
        );

        // all abilities go to SELECT table
        selectCoreTable.getItems().setAll(allCore);
    }

    private void loadSelectedCoreFromUnit() {
        selectedCoreIds.clear();
        if (workingUnit.coreAbilityIdList() != null) selectedCoreIds.addAll(workingUnit.coreAbilityIdList());
        refreshSelectedCoreTable();
    }

    private void refreshSelectedCoreTable() {
        // selected abilities go to WORD table (coreTable)
        List<CoreAbilities> selected = allCore.stream()
                .filter(a -> selectedCoreIds.contains(a.id()))
                .toList();
        coreTable.getItems().setAll(selected);
    }

    // ======================= Buttons =======================

    // Add: from selectCoreTable -> coreTable
    @FXML
    void addCore(MouseEvent event) {
        List<CoreAbilities> picked = selectCoreTable.getSelectionModel().getSelectedItems();
        if (picked == null || picked.isEmpty()) {
            warn("No Selection", "Please select core ability(s) to add.");
            return;
        }
        for (CoreAbilities a : picked) selectedCoreIds.add(a.id());
        refreshSelectedCoreTable();
    }

    // Delete: from coreTable
    @FXML
    void delete(MouseEvent event) {
        List<CoreAbilities> picked = coreTable.getSelectionModel().getSelectedItems();
        if (picked == null || picked.isEmpty()) {
            warn("No Selection", "Please select core ability(s) to delete.");
            return;
        }
        for (CoreAbilities a : picked) selectedCoreIds.remove(a.id());
        refreshSelectedCoreTable();
    }

    @FXML
    void editComposition(MouseEvent event) {
        compositionTxt.requestFocus();
        compositionTxt.selectAll();
    }

    @FXML
    void save(MouseEvent event) {
        try {
            String composition = safe(compositionTxt.getText()).trim();
            String otherText = safe(maintxt.getText()).trim();

            List<Integer> coreIds = new ArrayList<>(selectedCoreIds);
            List<Integer> otherIds = resolveOtherAbilityIds(otherText);

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
                    composition,
                    coreIds,
                    otherIds,
                    workingUnit.keywordIdList(),
                    workingUnit.rangedWeaponIdList(),
                    workingUnit.meleeWeaponIdList()
            );

            SelectedUnitContext.setSelectedUnit(updated);

            FixedAspectView.switchTo((Node) event.getSource(),
                    "/eecs2311/group2/wh40k_easycombat/RuleEditor.fxml",
                    1000.0, 600.0);

        } catch (Exception e) {
            error("Save Error", e.getMessage());
        }
    }

    @FXML
    void cancelChange(MouseEvent event) throws IOException {
        FixedAspectView.switchTo((Node) event.getSource(),
                "/eecs2311/group2/wh40k_easycombat/RuleEditor.fxml",
                1000.0, 600.0);
    }

    // ======================= Other ability rules =======================

    private List<Integer> resolveOtherAbilityIds(String otherText) throws SQLException {

        if (otherText.isBlank()) return new ArrayList<>();

        // Edit existing unit: overwrite existing other ability if exists
        if (workingUnit.id() != 0 && workingUnit.otherAbilityIdList() != null && !workingUnit.otherAbilityIdList().isEmpty()) {
            int existingId = workingUnit.otherAbilityIdList().get(0);
            data.updateOtherAbility(new OtherAbilities(existingId, otherText));
            return List.of(existingId);
        }

        // New unit: create other ability row
        int newId = data.addOtherAbility(new OtherAbilities(0, otherText));
        return List.of(newId);
    }

    private String loadOtherAbilityText(List<Integer> otherIds) {
        if (otherIds == null || otherIds.isEmpty()) return "";
        return otherIds.stream()
                .map(id -> data.getOtherAbilityById(id).map(OtherAbilities::ability).orElse(""))
                .filter(s -> s != null && !s.isBlank())
                .collect(Collectors.joining("\n"));
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
