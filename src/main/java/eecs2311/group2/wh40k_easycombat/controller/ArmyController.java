package eecs2311.group2.wh40k_easycombat.controller;

import eecs2311.group2.wh40k_easycombat.cell.ArmyUnitCell;
import eecs2311.group2.wh40k_easycombat.model.Army;
import eecs2311.group2.wh40k_easycombat.model.Army_detachment;
import eecs2311.group2.wh40k_easycombat.service.ArmyControllerDataLoader;
import eecs2311.group2.wh40k_easycombat.service.ArmyControllerPersistence;
import eecs2311.group2.wh40k_easycombat.service.ArmyCrudService;
import eecs2311.group2.wh40k_easycombat.service.StaticDataService;
import eecs2311.group2.wh40k_easycombat.service.UnitFactory;
import eecs2311.group2.wh40k_easycombat.viewmodel.ArmyUnitVM;

import java.io.IOException;
import java.util.Comparator;
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
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
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
import javafx.stage.Stage;

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
            currentArmy.clear();
            armyNametxt.clear();

            refreshDetachmentOptions();
            rebuildUnitTree();
            refreshPoints();
        });
    }

    // ======================= Navigation =======================

    @FXML
    void cancelTheChange(MouseEvent event) throws IOException {
        FXMLLoader loader = new FXMLLoader(
                getClass().getResource("/eecs2311/group2/wh40k_easycombat/MainUI.fxml")
        );
        Parent root = loader.load();

        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        stage.setScene(new Scene(root));
        stage.show();
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
        currentArmy.clear();
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
            currentArmy.add(vm);
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

            int totalPoints = getCurrentTotalPoints();
            int limit = sizeCBbox.getValue() == null ? 2000 : sizeCBbox.getValue();

            if (totalPoints > limit) {
                showWarning("Point Limit Exceeded",
                        "Current army is " + totalPoints + " pts, but the selected limit is " + limit + " pts.");
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

            editingArmyId = loaded.army().auto_id();
            editingArmyMarked = loaded.army().isMarked();

            armyNametxt.setText(loaded.army().name());
            setSelectedFactionById(loaded.army().faction_id());

            refreshDetachmentOptions();
            setSelectedDetachmentById(loaded.detachmentId());

            sizeCBbox.setValue(loaded.sizeLimit());

            currentArmy.clear();
            currentArmy.addAll(loaded.units());

            rebuildUnitTree();
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
            StaticDataService.ArmyBundle bundle = StaticDataService.getArmyBundle(row.armyId());
            if (bundle == null || bundle.army == null) return;

            List<Army_detachment> detachments = StaticDataService.getArmyDetachments(row.armyId());

            Army updated = new Army(
                    bundle.army.auto_id(),
                    bundle.army.name(),
                    bundle.army.faction_id(),
                    bundle.army.warlord_id(),
                    bundle.army.total_points(),
                    !bundle.army.isMarked()
            );

            ArmyCrudService.updateArmyBundle(
                    new ArmyCrudService.ArmyWriteBundle(updated, detachments, bundle.units, bundle.wargear)
            );

            if (editingArmyId != null && editingArmyId.equals(row.armyId())) {
                editingArmyMarked = !editingArmyMarked;
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
        FXCollections.sort(currentArmy,
                Comparator.comparing(ArmyUnitVM::getRole, String.CASE_INSENSITIVE_ORDER)
                        .thenComparing(ArmyUnitVM::getUnitName, String.CASE_INSENSITIVE_ORDER));

        pointNumber.setText(String.valueOf(getCurrentTotalPoints()));
        armyList.refresh();
    }

    private void removeArmyUnit(ArmyUnitVM vm) {
        currentArmy.remove(vm);
        refreshPoints();
    }

    private void setWarlordFromCell(ArmyUnitVM vm) {
        if (vm == null || !vm.isCharacter()) {
            showWarning("Invalid Warlord", "Only CHARACTER units can be set as warlord.");
            return;
        }

        boolean wasWarlord = vm.warlordProperty().get();

        for (ArmyUnitVM unit : currentArmy) {
            unit.warlordProperty().set(false);
        }

        if (!wasWarlord) {
            vm.warlordProperty().set(true);
        }

        armyList.refresh();
    }

    private List<ArmyUnitVM.EnhancementEntry> buildAvailableEnhancements(ArmyUnitVM vm) {
        ObservableList<ArmyUnitVM.EnhancementEntry> list = FXCollections.observableArrayList();
        list.add(new ArmyUnitVM.EnhancementEntry("", "No Enhancement", 0));

        String selectedFactionId = getSelectedFactionId();
        String selectedDetachmentId = getSelectedDetachmentId();

        for (ArmyUnitVM.EnhancementEntry e : vm.getEnhancements()) {
            boolean sameAsCurrent = e.getId().equals(vm.getEnhancementId());

            boolean uniqueOk = sameAsCurrent || currentArmy.stream()
                    .noneMatch(x -> x != vm && e.getId().equals(x.getEnhancementId()));

            boolean factionOk = e.getFactionId().isBlank()
                    || selectedFactionId == null
                    || selectedFactionId.isBlank()
                    || e.getFactionId().equalsIgnoreCase(selectedFactionId);

            boolean detachmentOk = e.getDetachmentId().isBlank()
                    || selectedDetachmentId == null
                    || selectedDetachmentId.isBlank()
                    || e.getDetachmentId().equalsIgnoreCase(selectedDetachmentId);

            if (uniqueOk && factionOk && detachmentOk) {
                list.add(e);
            }
        }

        return list;
    }

    // ======================= Editor State =======================

    private void resetEditor() {
        editingArmyId = null;
        editingArmyMarked = false;
        currentArmy.clear();
        pointNumber.setText("0");
        armyNametxt.clear();
        sizeCBbox.setValue(2000);

        if (factionCBbox.getValue() == null && !factionCBbox.getItems().isEmpty()) {
            factionCBbox.setValue(defaultFactionDisplay(factionCBbox.getItems()));
        }

        refreshDetachmentOptions();
        rebuildUnitTree();
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
        if (getSelectedFactionId() == null || getSelectedFactionId().isBlank()
                || "all".equalsIgnoreCase(getSelectedFactionId())) {
            return "Please choose one specific faction. \"All\" is not allowed.";
        }

        if (getSelectedDetachmentId() == null || getSelectedDetachmentId().isBlank()) {
            return "Please choose one detachment.";
        }

        if (armyNametxt.getText() == null || armyNametxt.getText().trim().isEmpty()) {
            return "Please enter an army name.";
        }

        if (currentArmy.isEmpty()) {
            return "Your army is empty.";
        }

        long warlordCount = currentArmy.stream().filter(x -> x.warlordProperty().get()).count();
        if (warlordCount > 1) {
            return "Only one CHARACTER unit can be set as warlord.";
        }

        LinkedHashSet<String> usedEnhancements = new LinkedHashSet<>();
        for (ArmyUnitVM unit : currentArmy) {
            String id = unit.getEnhancementId();
            if (id == null || id.isBlank()) continue;
            if (!usedEnhancements.add(id)) {
                return "Each enhancement can only be taken once.";
            }
        }

        return null;
    }

    // ======================= Helpers =======================

    private int getCurrentTotalPoints() {
        return currentArmy.stream().mapToInt(x -> x.pointsProperty().get()).sum();
    }

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