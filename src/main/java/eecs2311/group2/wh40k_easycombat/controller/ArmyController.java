package eecs2311.group2.wh40k_easycombat.controller;

import eecs2311.group2.wh40k_easycombat.cell.ArmyUnitCell;
import eecs2311.group2.wh40k_easycombat.service.ArmyBuilderManager;
import eecs2311.group2.wh40k_easycombat.service.ArmyCrudService;
import eecs2311.group2.wh40k_easycombat.service.ArmyEditorStateService;
import eecs2311.group2.wh40k_easycombat.service.ArmyFavoriteService;
import eecs2311.group2.wh40k_easycombat.service.ArmyPointService;
import eecs2311.group2.wh40k_easycombat.service.ArmyValidationService;
import eecs2311.group2.wh40k_easycombat.service.StaticDataService;
import eecs2311.group2.wh40k_easycombat.util.FixedAspectView;
import eecs2311.group2.wh40k_easycombat.viewmodel.ArmyControllerDataLoader;
import eecs2311.group2.wh40k_easycombat.viewmodel.ArmyControllerPersistence;
import eecs2311.group2.wh40k_easycombat.viewmodel.ArmyUnitVM;
import eecs2311.group2.wh40k_easycombat.viewmodel.UnitFactory;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
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
    @FXML private TableView<SavedArmyRow> savedArmyTable;
    @FXML private TableColumn<SavedArmyRow, String> savedName;
    @FXML private TableColumn<SavedArmyRow, Number> savedPoint;

    // ======================= Unit Selection ================
    @FXML private TreeTableView<UnitTreeRow> unitSectionTreeTable;
    @FXML private TreeTableColumn<UnitTreeRow, String> selectUnit;
    @FXML private TreeTableColumn<UnitTreeRow, Number> selectPoint;

    private final ObservableList<ArmyUnitVM> currentArmy = FXCollections.observableArrayList();
    private final ObservableList<SavedArmyRow> savedArmyRows = FXCollections.observableArrayList();

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
            showError("Initialization Error", e.getMessage());
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
        enhancementInfoById = ArmyControllerDataLoader.loadEnhancementInfoById();

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
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Exit");
        alert.setHeaderText("Are you sure you want to exit this page?");
        alert.setContentText("Unsaved changes will be lost.");

        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            FixedAspectView.switchTo((Node) event.getSource(),
                    "/eecs2311/group2/wh40k_easycombat/MainUI.fxml",
                    1200.0, 800.0);
        }
    }

    // ======================= Main Actions =======================

    @FXML
    void confirm(MouseEvent event) {
        String factionId = getSelectedFactionId();
        if (factionId == null || factionId.isBlank() || "all".equalsIgnoreCase(factionId)) {
            showWarning("Faction Required", "Please choose one specific faction. \"All\" is not allowed.");
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
        TreeItem<UnitTreeRow> selected = unitSectionTreeTable.getSelectionModel().getSelectedItem();
        if (selected == null || selected.getValue() == null || selected.getValue().group()) {
            showWarning("No Unit Selected", "Please select one unit from the tree.");
            return;
        }

        try {
            StaticDataService.DatasheetBundle bundle =
                    StaticDataService.getDatasheetBundle(selected.getValue().datasheetId());

            if (bundle == null) {
                showWarning("Missing Datasheet", "Selected unit could not be loaded.");
                return;
            }

            ArmyUnitVM vm = UnitFactory.create(bundle, enhancementInfoById);
            ArmyBuilderManager.addUnit(currentArmy, vm);
            refreshPoints();
        } catch (Exception e) {
            showError("Add Unit Error", e.getMessage());
        }
    }

    @FXML
    void removeUnit(MouseEvent event) {
        ArmyUnitVM selected = armyList.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showWarning("No Unit Selected", "Please select one unit in the army list.");
            return;
        }

        removeArmyUnit(selected);
    }

    @FXML
    void setWarlord(MouseEvent event) {
        ArmyUnitVM selected = armyList.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showWarning("No Unit Selected", "Please select one unit in the army list.");
            return;
        }

        setWarlordFromCell(selected);
    }

    @FXML
    void save(MouseEvent event) {
        try {
            String validation = validateBeforeSave();
            if (validation != null) {
                showWarning("Cannot Save", validation);
                return;
            }

            ArmyCrudService.ArmyWriteBundle bundle = ArmyControllerPersistence.buildWriteBundle(
                    editingArmyId,
                    editingArmyMarked,
                    armyNametxt.getText().trim(),
                    getSelectedFactionId(),
                    getSelectedDetachmentId(),
                    currentArmy
            );

            if (editingArmyId == null) {
                ArmyCrudService.createArmyBundle(bundle);
            } else {
                ArmyCrudService.updateArmyBundle(bundle);
            }

            loadSavedArmies();
            resetEditor();
            showInfo("Saved", "Army saved successfully.");
        } catch (Exception e) {
            showError("Save Error", e.getMessage());
        }
    }

    @FXML
    void loadArmy(MouseEvent event) {
        SavedArmyRow row = savedArmyTable.getSelectionModel().getSelectedItem();
        if (row == null) {
            showWarning("No Army Selected", "Please select one saved army.");
            return;
        }

        try {
            ArmyControllerPersistence.LoadedArmyData loaded =
                    ArmyControllerPersistence.loadArmyForEdit(row.armyId(), enhancementInfoById);

            if (loaded == null || loaded.army() == null) {
                showWarning("Load Failed", "The selected army could not be loaded.");
                return;
            }

            ArmyEditorStateService.LoadedEditorState state =
                    ArmyEditorStateService.applyLoadedArmy(
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
            showError("Load Error", e.getMessage());
        }
    }

    @FXML
    void delete(MouseEvent event) {
        SavedArmyRow row = savedArmyTable.getSelectionModel().getSelectedItem();
        if (row == null) {
            showWarning("No Army Selected", "Please select one saved army.");
            return;
        }

        Alert alert = new Alert(
                Alert.AlertType.CONFIRMATION,
                "Delete army \"" + row.armyName() + "\" ?",
                ButtonType.YES, ButtonType.NO
        );
        alert.setHeaderText("Confirm Delete");

        if (alert.showAndWait().orElse(ButtonType.NO) != ButtonType.YES) {
            return;
        }

        try {
            ArmyCrudService.deleteArmyBundle(row.armyId());
            if (editingArmyId != null && editingArmyId.equals(row.armyId())) {
                resetEditor();
            }
            loadSavedArmies();
        } catch (Exception e) {
            showError("Delete Error", e.getMessage());
        }
    }

    @FXML
    void favorite(MouseEvent event) {
        SavedArmyRow row = savedArmyTable.getSelectionModel().getSelectedItem();
        if (row == null) {
            showWarning("No Army Selected", "Please select one saved army.");
            return;
        }

        try {
            boolean newMarked = ArmyFavoriteService.toggleFavorite(row.armyId());

            if (editingArmyId != null && editingArmyId.equals(row.armyId())) {
                editingArmyMarked = newMarked;
            }

            loadSavedArmies();
        } catch (Exception e) {
            showError("Favorite Error", e.getMessage());
        }
    }

    @FXML
    void importData(MouseEvent event) {
        showInfo("Reserved", "Import will be implemented later.");
    }

    // ======================= Cell Callbacks =======================
    private void refreshPoints() {
        ArmyBuilderManager.sortArmy(currentArmy);
        pointNumber.setText(String.valueOf(ArmyPointService.calculateArmyPoints(currentArmy)));
        armyList.refresh();
    }

    private void removeArmyUnit(ArmyUnitVM vm) {
        ArmyBuilderManager.removeUnit(currentArmy, vm);
        refreshPoints();
    }

    private void setWarlordFromCell(ArmyUnitVM vm) {
        if (vm == null || !vm.isCharacter()) {
            showWarning("Invalid Warlord", "Only CHARACTER units can be set as warlord.");
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
        ArmyEditorStateService.EditorResetResult state =
                ArmyEditorStateService.resetEditor(
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
        savedArmyRows.setAll(ArmyControllerDataLoader.loadSavedArmyRows());
    }

    private void loadFactionOptions() {
        ArmyControllerDataLoader.FactionDisplayData data = ArmyControllerDataLoader.loadFactionDisplayData();

        factionDisplayToId = data.displayToId();
        factionIdToDisplay = data.idToDisplay();

        LinkedHashSet<String> displays = new LinkedHashSet<>(factionDisplayToId.keySet());
        factionCBbox.setItems(FXCollections.observableArrayList(displays));

        if (!factionCBbox.getItems().isEmpty()) {
            factionCBbox.setValue(defaultFactionDisplay(factionCBbox.getItems()));
        }
    }

    private void refreshDetachmentOptions() {
        ArmyControllerDataLoader.DetachmentDisplayData data =
                ArmyControllerDataLoader.loadDetachmentDisplayData(getSelectedFactionId());

        detachmentDisplayToId = data.displayToId();
        detachmentIdToDisplay = data.idToDisplay();

        ObservableList<String> displays = FXCollections.observableArrayList(detachmentDisplayToId.keySet());
        datachmentCBbox.setItems(displays);
        datachmentCBbox.setValue(displays.isEmpty() ? null : displays.get(0));
    }

    private void rebuildUnitTree() {
        TreeItem<UnitTreeRow> root = ArmyControllerDataLoader.buildUnitTree(
                getSelectedFactionId(),
                unit -> new UnitTreeRow(unit.name(), unit.datasheetId(), unit.role(), unit.points(), false),
                UnitTreeRow::group
        );
        unitSectionTreeTable.setRoot(root);
    }

    // ======================= Validation =======================
    private String validateBeforeSave() {
        return ArmyValidationService.validateBeforeSave(
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
        for (String f : factions) {
            if ("Space Marines".equalsIgnoreCase(f)) return f;
        }
        for (String f : factions) {
            if ("Adeptus Astartes".equalsIgnoreCase(f)) return f;
        }
        for (String f : factions) {
            if (f != null && f.toLowerCase(Locale.ROOT).contains("marine")) return f;
        }
        return factions.isEmpty() ? null : factions.get(0);
    }

    private void showWarning(String title, String text) {
        Alert a = new Alert(Alert.AlertType.WARNING, text, ButtonType.OK);
        a.setHeaderText(title);
        a.showAndWait();
    }

    private void showError(String title, String text) {
        Alert a = new Alert(Alert.AlertType.ERROR, text == null ? "Unknown error." : text, ButtonType.OK);
        a.setHeaderText(title);
        a.showAndWait();
    }

    private void showInfo(String title, String text) {
        Alert a = new Alert(Alert.AlertType.INFORMATION, text, ButtonType.OK);
        a.setHeaderText(title);
        a.showAndWait();
    }

    // ======================= Rows =======================
    public record UnitTreeRow(String displayName, String datasheetId, String role, int points, boolean group) {
        static UnitTreeRow group(String name) {
            return new UnitTreeRow(name, null, null, 0, true);
        }
    }

    public record SavedArmyRow(int armyId, String armyName, int points, boolean marked) {
        String displayName() {
            return marked ? "★ " + armyName : armyName;
        }
    }
}