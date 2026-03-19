package eecs2311.group2.wh40k_easycombat.controller;

import eecs2311.group2.wh40k_easycombat.model.aggregate.ArmyWriteAggregate;
import eecs2311.group2.wh40k_easycombat.model.aggregate.DatasheetAggregate;
import eecs2311.group2.wh40k_easycombat.cell.ArmyUnitCell;
import eecs2311.group2.wh40k_easycombat.controller.helper.ArmyControllerDataHelper;
import eecs2311.group2.wh40k_easycombat.controller.helper.ArmyEditorControllerHelper;
import eecs2311.group2.wh40k_easycombat.controller.helper.DialogHelper;
import eecs2311.group2.wh40k_easycombat.manager.ArmyBuilderManager;
import eecs2311.group2.wh40k_easycombat.service.ArmyBundleService;
import eecs2311.group2.wh40k_easycombat.service.ArmyEditorService;
import eecs2311.group2.wh40k_easycombat.service.StaticDataService;
import eecs2311.group2.wh40k_easycombat.util.FixedAspectView;
import eecs2311.group2.wh40k_easycombat.viewmodel.ArmyEditorLoadVM;
import eecs2311.group2.wh40k_easycombat.viewmodel.ArmySavedRowVM;
import eecs2311.group2.wh40k_easycombat.viewmodel.ArmyUnitTreeRowVM;
import eecs2311.group2.wh40k_easycombat.viewmodel.ArmyUnitVM;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeTableColumn;
import javafx.scene.control.TreeTableView;
import javafx.scene.input.MouseEvent;

public class ArmyController {

    // ======================= Buttons =======================
    @FXML private Button addButton;
    @FXML private Button confirmButton;
    @FXML private Button saveButton;
    @FXML private Button deleteButton;
    @FXML private Button removeButton;
    @FXML private Button importButton;
    @FXML private Button setWarlordButton;
    @FXML private Button CancelButton;
    @FXML private Button loadButton;
    @FXML private Button favoriteButton;

    // ======================= Text Inputs ===================
    @FXML private TextField armyNametxt;

    // ======================= ComboBoxes ====================
    @FXML private ComboBox<String> factionCBbox;
    @FXML private ComboBox<String> datachmentCBbox;
    @FXML private ComboBox<Integer> sizeCBbox;

    // ======================= Labels ========================
    @FXML private Label pointNumber;

    // ======================= Army Units ====================
    @FXML private ListView<ArmyUnitVM> armyList;

    // ======================= Saved Armies ==================
    @FXML private TableView<ArmySavedRowVM> savedArmyTable;
    @FXML private TableColumn<ArmySavedRowVM, String> savedName;
    @FXML private TableColumn<ArmySavedRowVM, Number> savedPoint;

    // ======================= Unit Selection ================
    @FXML private TreeTableView<ArmyUnitTreeRowVM> unitSectionTreeTable;
    @FXML private TreeTableColumn<ArmyUnitTreeRowVM, String> selectUnit;
    @FXML private TreeTableColumn<ArmyUnitTreeRowVM, Number> selectPoint;

    private final ObservableList<ArmyUnitVM> currentArmy = FXCollections.observableArrayList();
    private final ObservableList<ArmySavedRowVM> savedArmyRows = FXCollections.observableArrayList();

    private Map<String, ArmyUnitVM.EnhancementEntry> enhancementInfoById = new LinkedHashMap<>();

    private LinkedHashMap<String, String> factionDisplayToId = new LinkedHashMap<>();
    private LinkedHashMap<String, String> factionIdToDisplay = new LinkedHashMap<>();

    private LinkedHashMap<String, String> detachmentDisplayToId = new LinkedHashMap<>();
    private LinkedHashMap<String, String> detachmentIdToDisplay = new LinkedHashMap<>();

    private Integer editingArmyId = null;
    private boolean editingArmyMarked = false;

    private static final List<Integer> SIZE_OPTIONS = List.of(500, 1000, 1500, 2000, 3000);

    @FXML
    private void initialize() {
        try {
            StaticDataService.loadAllFromSqlite();
        } catch (Exception e) {
            DialogHelper.showError("Initialization Error", e);
        }

        setupSizeComboBox();
        setupArmyList();
        setupSavedArmyTable();
        setupUnitTreeTable();
        loadInitialData();
        setupFactionListener();
    }

    // ======================= Setup =======================

    private void setupSizeComboBox() {
        sizeCBbox.setItems(FXCollections.observableArrayList(SIZE_OPTIONS));
        sizeCBbox.setValue(2000);
    }

    private void setupArmyList() {
        armyList.setItems(currentArmy);
        armyList.setCellFactory(v -> new ArmyUnitCell(
                this::refreshPoints,
                this::removeArmyUnit,
                this::setWarlordFromCell,
                this::buildAvailableEnhancements
        ));
    }

    private void setupSavedArmyTable() {
        savedArmyTable.setItems(savedArmyRows);
        savedName.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().displayName()));
        savedPoint.setCellValueFactory(data -> new SimpleIntegerProperty(data.getValue().points()));
    }

    private void setupUnitTreeTable() {
        selectUnit.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getValue().displayName()));
        selectPoint.setCellValueFactory(data -> new SimpleIntegerProperty(data.getValue().getValue().points()));
        unitSectionTreeTable.setShowRoot(false);
    }

    private void loadInitialData() {
        enhancementInfoById = ArmyControllerDataHelper.loadEnhancementInfoById();

        loadFactionOptions();
        loadSavedArmies();
        resetEditor();
    }

    private void setupFactionListener() {
        factionCBbox.valueProperty().addListener((obs, oldValue, newValue) -> {
            if (Objects.equals(oldValue, newValue)) return;
            if (newValue == null || newValue.isBlank()) return;

            editingArmyId = null;
            editingArmyMarked = false;
            ArmyBuilderManager.clearArmy(currentArmy);
            armyNametxt.clear();

            refreshDetachmentOptions();
            rebuildUnitTree();
            refreshPoints();
        });
    }

    // ======================= Navigation =======================

    @FXML
    void cancelTheChange(MouseEvent event) throws IOException {
        boolean shouldExit = DialogHelper.confirmOkCancel(
                "Exit",
                "Are you sure you want to exit this page?",
                "Unsaved changes will be lost."
        );

        if (shouldExit) {
        	FixedAspectView.switchResponsiveTo(
        	        (Node) event.getSource(),
        	        "/eecs2311/group2/wh40k_easycombat/MainUI.fxml",
        	        800.0,
        	        600.0,
        	        1200.0,
        	        800.0
        	);
        }
    }

    // ======================= Main Actions =======================

    @FXML
    void confirm(MouseEvent event) {
        String factionId = getSelectedFactionId();
        if (factionId == null || factionId.isBlank() || "all".equalsIgnoreCase(factionId)) {
            DialogHelper.showWarning("Faction Required", "Please choose one specific faction. \"All\" is not allowed.");
            return;
        }

        editingArmyId = null;
        editingArmyMarked = false;
        ArmyBuilderManager.clearArmy(currentArmy);
        armyNametxt.clear();

        refreshDetachmentOptions();
        rebuildUnitTree();
        refreshPoints();
    }

    @FXML
    void add(MouseEvent event) {
        TreeItem<ArmyUnitTreeRowVM> selected = unitSectionTreeTable.getSelectionModel().getSelectedItem();
        if (selected == null || selected.getValue() == null || selected.getValue().isGroup()) {
            DialogHelper.showWarning("No Unit Selected", "Please select one unit from the tree.");
            return;
        }

        try {
            DatasheetAggregate bundle =
                    StaticDataService.getDatasheetBundle(selected.getValue().datasheetId());

            if (bundle == null) {
                DialogHelper.showWarning("Missing Datasheet", "Selected unit could not be loaded.");
                return;
            }

            ArmyUnitVM vm = ArmyEditorService.createArmyUnitVM(bundle, enhancementInfoById);
            ArmyBuilderManager.addUnit(currentArmy, vm);
            refreshPoints();
        } catch (Exception e) {
            DialogHelper.showError("Add Unit Error", e);
        }
    }

    @FXML
    void removeUnit(MouseEvent event) {
        ArmyUnitVM selected = armyList.getSelectionModel().getSelectedItem();
        if (selected == null) {
            DialogHelper.showWarning("No Unit Selected", "Please select one unit in the army list.");
            return;
        }

        removeArmyUnit(selected);
    }

    @FXML
    void setWarlord(MouseEvent event) {
        ArmyUnitVM selected = armyList.getSelectionModel().getSelectedItem();
        if (selected == null) {
            DialogHelper.showWarning("No Unit Selected", "Please select one unit in the army list.");
            return;
        }

        setWarlordFromCell(selected);
    }

    @FXML
    void save(MouseEvent event) {
        try {
            String validation = validateBeforeSave();
            if (validation != null) {
                DialogHelper.showWarning("Cannot Save", validation);
                return;
            }

            ArmyWriteAggregate bundle = ArmyEditorService.buildWriteBundle(
                    editingArmyId,
                    editingArmyMarked,
                    armyNametxt.getText().trim(),
                    getSelectedFactionId(),
                    getSelectedDetachmentId(),
                    currentArmy
            );

            if (editingArmyId == null) {
                ArmyBundleService.createArmyBundle(bundle);
            } else {
                ArmyBundleService.updateArmyBundle(bundle);
            }

            loadSavedArmies();
            resetEditor();
            DialogHelper.showInfo("Saved", "Army saved successfully.");
        } catch (Exception e) {
            DialogHelper.showError("Save Error", e);
        }
    }

    @FXML
    void loadArmy(MouseEvent event) {
        ArmySavedRowVM row = savedArmyTable.getSelectionModel().getSelectedItem();
        if (row == null) {
            DialogHelper.showWarning("No Army Selected", "Please select one saved army.");
            return;
        }

        try {
            ArmyEditorLoadVM loaded =
                    ArmyEditorService.loadArmyForEdit(row.armyId(), enhancementInfoById);

            if (loaded == null || loaded.army() == null) {
                DialogHelper.showWarning("Load Failed", "The selected army could not be loaded.");
                return;
            }

            ArmyEditorControllerHelper.LoadedEditorState state =
                    ArmyEditorControllerHelper.applyLoadedArmy(
                            loaded,
                            currentArmy,
                            this::setSelectedFactionById,
                            this::refreshDetachmentOptions,
                            this::setSelectedDetachmentById,
                            sizeCBbox,
                            armyNametxt,
                            this::rebuildUnitTree
                    );

            editingArmyId = state.editingArmyId();
            editingArmyMarked = state.editingArmyMarked();

            refreshPoints();
        } catch (Exception e) {
            DialogHelper.showError("Load Error", e);
        }
    }

    @FXML
    void delete(MouseEvent event) {
        ArmySavedRowVM row = savedArmyTable.getSelectionModel().getSelectedItem();
        if (row == null) {
            DialogHelper.showWarning("No Army Selected", "Please select one saved army.");
            return;
        }

        if (!DialogHelper.confirmYesNo("Confirm Delete", "Delete army \"" + row.armyName() + "\" ?")) {
            return;
        }

        try {
            ArmyBundleService.deleteArmyBundle(row.armyId());
            if (editingArmyId != null && editingArmyId.equals(row.armyId())) {
                resetEditor();
            }
            loadSavedArmies();
        } catch (Exception e) {
            DialogHelper.showError("Delete Error", e);
        }
    }

    @FXML
    void favorite(MouseEvent event) {
        ArmySavedRowVM row = savedArmyTable.getSelectionModel().getSelectedItem();
        if (row == null) {
            DialogHelper.showWarning("No Army Selected", "Please select one saved army.");
            return;
        }

        try {
        	boolean newMarked = ArmyBundleService.toggleFavorite(row.armyId());

            if (editingArmyId != null && editingArmyId.equals(row.armyId())) {
                editingArmyMarked = newMarked;
            }

            loadSavedArmies();
        } catch (Exception e) {
            DialogHelper.showError("Favorite Error", e);
        }
    }

    @FXML
    void importData(MouseEvent event) {
        DialogHelper.showInfo("Reserved", "Import will be implemented later.");
    }

    // ======================= Cell Callbacks =======================
    private void refreshPoints() {
        ArmyBuilderManager.sortArmy(currentArmy);
        pointNumber.setText(String.valueOf(ArmyBuilderManager.calculateArmyPoints(currentArmy)));
        armyList.refresh();
    }

    private void removeArmyUnit(ArmyUnitVM vm) {
        ArmyBuilderManager.removeUnit(currentArmy, vm);
        refreshPoints();
    }

    private void setWarlordFromCell(ArmyUnitVM vm) {
        if (vm == null || !vm.isCharacter()) {
            DialogHelper.showWarning("Invalid Warlord", "Only CHARACTER units can be set as warlord.");
            return;
        }

        ArmyBuilderManager.toggleWarlord(currentArmy, vm);
        armyList.refresh();
    }

    private List<ArmyUnitVM.EnhancementEntry> buildAvailableEnhancements(ArmyUnitVM vm) {
        return ArmyBuilderManager.buildAvailableEnhancements(
                currentArmy,
                vm,
                getSelectedFactionId(),
                getSelectedDetachmentId()
        );
    }

    // ======================= Editor State =======================
    private void resetEditor() {
        ArmyEditorControllerHelper.EditorResetResult state =
                ArmyEditorControllerHelper.resetEditor(
                        currentArmy,
                        armyNametxt,
                        factionCBbox,
                        sizeCBbox,
                        () -> defaultFactionDisplay(factionCBbox.getItems()),
                        this::refreshDetachmentOptions,
                        this::rebuildUnitTree
                );

        editingArmyId = state.editingArmyId();
        editingArmyMarked = state.editingArmyMarked();
        pointNumber.setText("0");
    }

    private void loadSavedArmies() {
        savedArmyRows.setAll(ArmyControllerDataHelper.loadSavedArmyRows());
    }

    private void loadFactionOptions() {
        ArmyControllerDataHelper.FactionDisplayData data = ArmyControllerDataHelper.loadFactionDisplayData();

        factionDisplayToId = data.displayToId();
        factionIdToDisplay = data.idToDisplay();

        LinkedHashSet<String> displays = new LinkedHashSet<>(factionDisplayToId.keySet());
        factionCBbox.setItems(FXCollections.observableArrayList(displays));

        if (!factionCBbox.getItems().isEmpty()) {
            factionCBbox.setValue(defaultFactionDisplay(factionCBbox.getItems()));
        }
    }

    private void refreshDetachmentOptions() {
        ArmyControllerDataHelper.DetachmentDisplayData data =
                ArmyControllerDataHelper.loadDetachmentDisplayData(getSelectedFactionId());

        detachmentDisplayToId = data.displayToId();
        detachmentIdToDisplay = data.idToDisplay();

        ObservableList<String> displays = FXCollections.observableArrayList(detachmentDisplayToId.keySet());
        datachmentCBbox.setItems(displays);
        datachmentCBbox.setValue(displays.isEmpty() ? null : displays.get(0));
    }

    private void rebuildUnitTree() {
        TreeItem<ArmyUnitTreeRowVM> root = ArmyControllerDataHelper.buildUnitTree(getSelectedFactionId());
        unitSectionTreeTable.setRoot(root);
    }

    // ======================= Validation =======================
    private String validateBeforeSave() {
        return ArmyBuilderManager.validateBeforeSave(
                getSelectedFactionId(),
                getSelectedDetachmentId(),
                armyNametxt.getText(),
                currentArmy,
                sizeCBbox.getValue()
        );
    }

    // ======================= Helpers =======================
    private String getSelectedFactionId() {
        String display = factionCBbox.getValue();
        if (display == null) return null;
        return factionDisplayToId.getOrDefault(display, display);
    }

    private void setSelectedFactionById(String factionId) {
        if (factionId == null) return;
        factionCBbox.setValue(factionIdToDisplay.getOrDefault(factionId, factionId));
    }

    private String getSelectedDetachmentId() {
        String display = datachmentCBbox.getValue();
        if (display == null) return null;
        return detachmentDisplayToId.getOrDefault(display, display);
    }

    private void setSelectedDetachmentById(String detachmentId) {
        if (detachmentId == null) return;
        datachmentCBbox.setValue(detachmentIdToDisplay.getOrDefault(detachmentId, detachmentId));
    }

    private static String defaultFactionDisplay(List<String> factions) {
        for (String faction : factions) {
            if ("Space Marines".equalsIgnoreCase(faction)) return faction;
        }
        for (String faction : factions) {
            if ("Adeptus Astartes".equalsIgnoreCase(faction)) return faction;
        }
        for (String faction : factions) {
            if (faction != null && faction.toLowerCase(Locale.ROOT).contains("marine")) return faction;
        }
        return factions.isEmpty() ? null : factions.get(0);
    }
}