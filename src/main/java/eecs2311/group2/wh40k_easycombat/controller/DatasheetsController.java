package eecs2311.group2.wh40k_easycombat.controller;

import eecs2311.group2.wh40k_easycombat.controller.helper.DatasheetsPageControllerHelper;
import eecs2311.group2.wh40k_easycombat.controller.helper.DatasheetsRenderHelper;
import eecs2311.group2.wh40k_easycombat.controller.helper.DialogHelper;
import eecs2311.group2.wh40k_easycombat.model.instance.WeaponProfile;
import eecs2311.group2.wh40k_easycombat.util.FixedAspectView;
import eecs2311.group2.wh40k_easycombat.viewmodel.DatasheetListItemVM;
import eecs2311.group2.wh40k_easycombat.viewmodel.DatasheetsPageState;
import javafx.beans.property.SimpleStringProperty;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.input.InputMethodEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.VBox;
import javafx.scene.text.TextFlow;

import java.io.IOException;
import java.net.URL;
import java.sql.SQLException;
import java.util.ResourceBundle;

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
    @FXML private ListView<DatasheetListItemVM> datasheetsList;

    // ======================= Labels - Datasheet / Unit Names ===
    @FXML private Label datasheetName;
    @FXML private Label unitName1;
    @FXML private Label unitName2;

    // ======================= Unit 1 - Properties ==============
    @FXML private javafx.scene.layout.HBox unit1PropertyHBox;
    @FXML private Label unit1MLabel;
    @FXML private Label unit1TLabel;
    @FXML private Label unit1SvLabel;
    @FXML private Label unit1WLabel;
    @FXML private Label unit1OcLabel;
    @FXML private Label unit1LdLabel;
    @FXML private Label InvsvLabel;
    @FXML private Label insvTxtLabel;

    // ======================= Unit 2 - Properties ==============
    @FXML private javafx.scene.layout.HBox unit2PropertyHBox;
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
    @FXML private TableView<WeaponProfile> meleeWeaponTable;
    @FXML private TableColumn<WeaponProfile, String> meleeWeaponName;
    @FXML private TableColumn<WeaponProfile, String> meleeWeaponRange;
    @FXML private TableColumn<WeaponProfile, String> meleeWeaponA;
    @FXML private TableColumn<WeaponProfile, String> meleeWeaponWS;
    @FXML private TableColumn<WeaponProfile, String> meleeWeaponS;
    @FXML private TableColumn<WeaponProfile, String> meleeWeaponAP;
    @FXML private TableColumn<WeaponProfile, String> meleeWeaponD;

    // ======================= Tables - Ranged Weapons ==========
    @FXML private TableView<WeaponProfile> rangedWeaponTable;
    @FXML private TableColumn<WeaponProfile, String> rangedWeaponName;
    @FXML private TableColumn<WeaponProfile, String> rangedWeaponRange;
    @FXML private TableColumn<WeaponProfile, String> rangedWeaponA;
    @FXML private TableColumn<WeaponProfile, String> rangedWeaponBS;
    @FXML private TableColumn<WeaponProfile, String> rangedWeaponS;
    @FXML private TableColumn<WeaponProfile, String> rangedWeaponAP;
    @FXML private TableColumn<WeaponProfile, String> rangedWeaponD;

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
            rangedWeaponName.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().name()));
            rangedWeaponRange.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().range()));
            rangedWeaponA.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().a()));
            rangedWeaponBS.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().skill()));
            rangedWeaponS.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().s()));
            rangedWeaponAP.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().ap()));
            rangedWeaponD.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().d()));
            applyWeaponNameCellFactory(rangedWeaponName);
        }

        if (meleeWeaponName != null) {
            meleeWeaponName.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().name()));
            meleeWeaponRange.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().range()));
            meleeWeaponA.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().a()));
            meleeWeaponWS.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().skill()));
            meleeWeaponS.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().s()));
            meleeWeaponAP.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().ap()));
            meleeWeaponD.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().d()));
            applyWeaponNameCellFactory(meleeWeaponName);
        }

        if (rangedWeaponTable != null) {
            rangedWeaponTable.setFixedCellSize(40);
        }
        if (meleeWeaponTable != null) {
            meleeWeaponTable.setFixedCellSize(40);
        }
    }

    private void applyWeaponNameCellFactory(TableColumn<WeaponProfile, String> col) {
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

                WeaponProfile row = (WeaponProfile) getTableRow().getItem();

                nameLabel.setText(row.name());

                String desc = row.description();
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
        style += "-fx-background-color: #1e1e1e; -fx-text-fill: #e6e6e6; -fx-border-color: #c9a227; -fx-border-width: 1px; -fx-border-radius: 4px; -fx-background-radius: 4px;";

        label.setStyle(style);
    }

    // -------------------- Data loading --------------------

    private void loadPageData() {
        try {
            DatasheetsPageControllerHelper.loadPageData(state, datasheetsList, factionComboBox);

            if (!state.getFilteredDatasheets().isEmpty()) {
                datasheetsList.getSelectionModel().select(0);
                showSelectedDatasheet();
            } else {
                clearRightPanel();
            }
        } catch (Exception e) {
            DialogHelper.showError("Load data failed", e);
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
        try {
            FixedAspectView.switchResponsiveTo(
                    (Node) event.getSource(),
                    "/eecs2311/group2/wh40k_easycombat/RuleEditor.fxml",
                    1100.0,
                    760.0,
                    1480.0,
                    900.0
            );
        } catch (IOException e) {
            DialogHelper.showError("Open Rule Editor Failed", e);
        }
    }

    @FXML
    void clickEditButton(MouseEvent event) {
        clickAddButton(event);
    }

    @FXML
    void clickBackButton(MouseEvent event) throws IOException {
    	FixedAspectView.switchResponsiveTo(
    	        (Node) event.getSource(),
    	        "/eecs2311/group2/wh40k_easycombat/MainUI.fxml",
    	        800.0,
    	        600.0,
    	        1200.0,
    	        800.0
    	);
    }

    // -------------------- Filtering --------------------

    private void applyFilters() {
        boolean hasResult = DatasheetsPageControllerHelper.applyFilters(
                state,
                datasheetsList,
                searchTextField == null ? "" : searchTextField.getText(),
                factionComboBox == null ? "ALL" : factionComboBox.getValue()
        );

        if (hasResult) {
            showSelectedDatasheet();
        } else {
            clearRightPanel();
        }
    }

    // -------------------- Selection rendering --------------------

    private void showSelectedDatasheet() {
        DatasheetListItemVM selected = datasheetsList == null
                ? null
                : datasheetsList.getSelectionModel().getSelectedItem();

        if (selected == null) {
            clearRightPanel();
            return;
        }

        try {
            boolean rendered = DatasheetsRenderHelper.renderSelectedDatasheet(
                    selected,
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

            if (!rendered) {
                clearRightPanel();
            }
        } catch (SQLException e) {
            DialogHelper.showError("Read datasheet failed", e);
        }
    }

    private void clearRightPanel() {
        DatasheetsRenderHelper.clearRightPanel(
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
}
