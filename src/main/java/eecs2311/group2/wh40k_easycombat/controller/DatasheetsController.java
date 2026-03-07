package eecs2311.group2.wh40k_easycombat.controller;

import eecs2311.group2.wh40k_easycombat.model.instance.WeaponRow;
import eecs2311.group2.wh40k_easycombat.service.*;
import eecs2311.group2.wh40k_easycombat.service.StaticDataService.DatasheetBundle;
import eecs2311.group2.wh40k_easycombat.util.FixedAspectView;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.input.InputMethodEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import javafx.scene.text.TextFlow;

import java.io.IOException;
import java.net.URL;
import java.sql.SQLException;
import java.util.ResourceBundle;

import static eecs2311.group2.wh40k_easycombat.util.FxReflectionHelper.getAny;
import static eecs2311.group2.wh40k_easycombat.util.FxReflectionHelper.s;

public class DatasheetsController implements Initializable {

    // ======================= Buttons ==========================
    @FXML private Button addButton;
    @FXML private Button editButton;
    @FXML private Button searchButton;
    @FXML private Button backToMainButton;

    // ======================= Inputs ===========================
    @FXML private TextField searchTextField;
    @FXML private ComboBox<String> factionComboBox;

    // ======================= Lists ============================
    @FXML private ListView<Object> datasheetsList;

    // ======================= Labels - Datasheet / Unit Names ===
    @FXML private Label datasheetName;
    @FXML private Label unitName1;
    @FXML private Label unitName2;

    // ======================= Unit 1 - Properties ==============
    @FXML private HBox unit1PropertyHBox;
    @FXML private Label unit1MLabel;
    @FXML private Label unit1TLabel;
    @FXML private Label unit1SvLabel;
    @FXML private Label unit1WLabel;
    @FXML private Label unit1OcLabel;
    @FXML private Label unit1LdLabel;
    @FXML private Label InvsvLabel;
    @FXML private Label insvTxtLabel;

    // ======================= Unit 2 - Properties ==============
    @FXML private HBox unit2PropertyHBox;
    @FXML private Label unit2MLabel;
    @FXML private Label unit2TLabel;
    @FXML private Label unit2SvLabel;
    @FXML private Label unit2WLabel;
    @FXML private Label unit2OcLabel;
    @FXML private Label unit2LdLabel;

    // ======================= TextFlows - Descriptions =========
    @FXML private TextFlow costTextFlow;
    @FXML private TextFlow unitCompositionTextFlow;
    @FXML private TextFlow keywordsTextFlow;
    @FXML private TextFlow abilityTextFlow;
    @FXML private TextFlow factionAbilityTextFlow;
    @FXML private TextFlow otherTextFlow;

    // ======================= Tables - Melee Weapons ===========
    @FXML private TableView<WeaponRow> meleeWeaponTable;
    @FXML private TableColumn<WeaponRow, String> meleeWeaponName;
    @FXML private TableColumn<WeaponRow, String> meleeWeaponRange;
    @FXML private TableColumn<WeaponRow, String> meleeWeaponA;
    @FXML private TableColumn<WeaponRow, String> meleeWeaponWS;
    @FXML private TableColumn<WeaponRow, String> meleeWeaponAP;
    @FXML private TableColumn<WeaponRow, String> meleeWeaponD;

    // ======================= Tables - Ranged Weapons ==========
    @FXML private TableView<WeaponRow> rangedWeaponTable;
    @FXML private TableColumn<WeaponRow, String> rangedWeaponName;
    @FXML private TableColumn<WeaponRow, String> rangedWeaponRange;
    @FXML private TableColumn<WeaponRow, String> rangedWeaponA;
    @FXML private TableColumn<WeaponRow, String> rangedWeaponBS;
    @FXML private TableColumn<WeaponRow, String> rangedWeaponAP;
    @FXML private TableColumn<WeaponRow, String> rangedWeaponD;

    // ======================= In-memory ========================
    private final DatasheetsPageState state = new DatasheetsPageState();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        setupTables();
        ensurePropertyLabelsWhiteBackground();
        loadPageData();
        wireEvents();
    }

    // -------------------- Table setup --------------------

    private void setupTables() {
        if (rangedWeaponName != null) {
            rangedWeaponName.setCellValueFactory(c -> c.getValue().nameProperty());
            rangedWeaponRange.setCellValueFactory(c -> c.getValue().rangeProperty());
            rangedWeaponA.setCellValueFactory(c -> c.getValue().aProperty());
            rangedWeaponBS.setCellValueFactory(c -> c.getValue().skillProperty());
            rangedWeaponAP.setCellValueFactory(c -> c.getValue().apProperty());
            rangedWeaponD.setCellValueFactory(c -> c.getValue().dProperty());
            applyWeaponNameCellFactory(rangedWeaponName);
        }

        if (meleeWeaponName != null) {
            meleeWeaponName.setCellValueFactory(c -> c.getValue().nameProperty());
            meleeWeaponRange.setCellValueFactory(c -> c.getValue().rangeProperty());
            meleeWeaponA.setCellValueFactory(c -> c.getValue().aProperty());
            meleeWeaponWS.setCellValueFactory(c -> c.getValue().skillProperty());
            meleeWeaponAP.setCellValueFactory(c -> c.getValue().apProperty());
            meleeWeaponD.setCellValueFactory(c -> c.getValue().dProperty());
            applyWeaponNameCellFactory(meleeWeaponName);
        }

        if (rangedWeaponTable != null) {
            rangedWeaponTable.setFixedCellSize(-1);
        }
        if (meleeWeaponTable != null) {
            meleeWeaponTable.setFixedCellSize(-1);
        }
        
        rangedWeaponTable.setFixedCellSize(40);
        meleeWeaponTable.setFixedCellSize(40);
    }

    private void applyWeaponNameCellFactory(TableColumn<WeaponRow, String> col) {
        col.setCellFactory(tc -> new TableCell<>() {

            private final Label nameLabel = new Label();
            private final Label descLabel = new Label();
            private final VBox box = new VBox(1);

            {
                nameLabel.setStyle("-fx-font-weight: bold;");
                nameLabel.setWrapText(false);

                descLabel.setStyle("-fx-font-size: 11px;");
                descLabel.setWrapText(true);

                box.getChildren().addAll(nameLabel, descLabel);
            }

            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);

                if (empty || getTableRow() == null || getTableRow().getItem() == null) {
                    setGraphic(null);
                    return;
                }

                WeaponRow row = (WeaponRow) getTableRow().getItem();

                nameLabel.setText(row.nameProperty().get());

                String desc = row.descriptionProperty().get();
                boolean hasDesc = desc != null && !desc.isBlank();

                descLabel.setText(desc);
                descLabel.setVisible(hasDesc);
                descLabel.setManaged(hasDesc);

                setGraphic(box);
            }
        });
    }
    
    private void ensurePropertyLabelsWhiteBackground() {
        applyWhiteBackground(unit1MLabel);
        applyWhiteBackground(unit1TLabel);
        applyWhiteBackground(unit1SvLabel);
        applyWhiteBackground(unit1WLabel);
        applyWhiteBackground(unit1OcLabel);
        applyWhiteBackground(unit1LdLabel);
        applyWhiteBackground(InvsvLabel);

        applyWhiteBackground(unit2MLabel);
        applyWhiteBackground(unit2TLabel);
        applyWhiteBackground(unit2SvLabel);
        applyWhiteBackground(unit2WLabel);
        applyWhiteBackground(unit2OcLabel);
        applyWhiteBackground(unit2LdLabel);

        applyWhiteBackground(insvTxtLabel);
    }

    private void applyWhiteBackground(Label label) {
        if (label == null) return;

        String style = label.getStyle();
        if (style == null) style = "";

        style = style.replaceAll("-fx-background-color\\s*:[^;]*;?", "").trim();

        if (!style.isBlank() && !style.endsWith(";")) {
            style += ";";
        }
        style += "-fx-background-color: white;";

        label.setStyle(style);
    }

    // -------------------- Data loading --------------------

    private void loadPageData() {
        try {
            DatasheetsPageLoader.loadAbilitiesMaster(state);
            DatasheetsPageLoader.loadDatasheets(state, datasheetsList);
            DatasheetsPageLoader.loadFactionsIntoComboBox(state, factionComboBox);

            if (!state.getFilteredDatasheets().isEmpty()) {
                datasheetsList.getSelectionModel().select(0);
                showSelectedDatasheet();
            } else {
                clearRightPanel();
            }
        } catch (Exception e) {
            showError("Load data failed", e);
        }
    }

    // -------------------- Events wiring --------------------

    private void wireEvents() {
        if (datasheetsList != null) {
            datasheetsList.getSelectionModel().selectedItemProperty()
                    .addListener((obs, oldV, newV) -> showSelectedDatasheet());
        }

        if (factionComboBox != null) {
            factionComboBox.getSelectionModel().selectedItemProperty()
                    .addListener((obs, oldV, newV) -> applyFilters());
        }

        if (searchButton != null) {
            searchButton.setOnMouseClicked(e -> applyFilters());
        }
    }

    @FXML
    private void selectFaction(ActionEvent event) {
        applyFilters();
    }

    @FXML
    void changeFaction(InputMethodEvent event) {
        applyFilters();
    }

    @FXML
    void clickSearchButton(MouseEvent event) {
        applyFilters();
    }

    @FXML
    void clickAddButton(MouseEvent event) {
        // TODO
    }

    @FXML
    void clickEditButton(MouseEvent event) {
        // TODO
    }

    @FXML
    void clickBackButton(MouseEvent event) throws IOException {
        FixedAspectView.switchTo((Node) event.getSource(),
                "/eecs2311/group2/wh40k_easycombat/MainUI.fxml",
                1200.0, 800.0);
    }

    // -------------------- Filtering --------------------

    private void applyFilters() {
        DatasheetsFilterService.applyFilters(
                state,
                searchTextField == null ? "" : searchTextField.getText(),
                factionComboBox == null ? "ALL" : factionComboBox.getValue()
        );

        if (!state.getFilteredDatasheets().isEmpty()) {
            datasheetsList.getSelectionModel().select(0);
            showSelectedDatasheet();
        } else {
            clearRightPanel();
        }
    }

    // -------------------- Selection rendering --------------------

    private void showSelectedDatasheet() {
        Object selected = datasheetsList == null ? null : datasheetsList.getSelectionModel().getSelectedItem();
        if (selected == null) {
            clearRightPanel();
            return;
        }

        String datasheetId = s(getAny(selected, "id", "datasheet_id"));
        if (datasheetId.isBlank()) {
            clearRightPanel();
            return;
        }

        try {
            DatasheetBundle bundle = StaticDataService.getDatasheetBundle(datasheetId);
            if (bundle == null) {
                clearRightPanel();
                return;
            }

            DatasheetsRenderer.renderBundle(
                    bundle,
                    state,
                    datasheetName,
                    unitName1,
                    unitName2,
                    unit2PropertyHBox,
                    unit1MLabel,
                    unit1TLabel,
                    unit1SvLabel,
                    unit1WLabel,
                    unit1LdLabel,
                    unit1OcLabel,
                    InvsvLabel,
                    insvTxtLabel,
                    unit2MLabel,
                    unit2TLabel,
                    unit2SvLabel,
                    unit2WLabel,
                    unit2LdLabel,
                    unit2OcLabel,
                    costTextFlow,
                    unitCompositionTextFlow,
                    keywordsTextFlow,
                    abilityTextFlow,
                    factionAbilityTextFlow,
                    otherTextFlow,
                    rangedWeaponTable,
                    meleeWeaponTable
            );
        } catch (SQLException e) {
            showError("Read datasheet failed: " + datasheetId, e);
        }
    }

    private void clearRightPanel() {
        DatasheetsRenderer.clearRightPanel(
                datasheetName,
                unitName1,
                unitName2,
                unit1MLabel,
                unit1TLabel,
                unit1SvLabel,
                unit1WLabel,
                unit1LdLabel,
                unit1OcLabel,
                InvsvLabel,
                unit2MLabel,
                unit2TLabel,
                unit2SvLabel,
                unit2WLabel,
                unit2LdLabel,
                unit2OcLabel,
                costTextFlow,
                unitCompositionTextFlow,
                keywordsTextFlow,
                abilityTextFlow,
                factionAbilityTextFlow,
                otherTextFlow,
                rangedWeaponTable,
                meleeWeaponTable,
                unit2PropertyHBox,
                insvTxtLabel
        );
    }

    private void showError(String title, Exception e) {
        e.printStackTrace();
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText(title);
        alert.setContentText(e.getMessage());
        alert.showAndWait();
    }
}