package eecs2311.group2.wh40k_easycombat.controller;

import eecs2311.group2.wh40k_easycombat.controller.helper.DialogHelper;
import eecs2311.group2.wh40k_easycombat.util.FixedAspectView;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;

import java.io.IOException;

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
    @FXML private TextArea maintxt;

    // ======================= Tables ===============================
    // was: TableView<CoreAbilities> etc
    @FXML private TableView<?> coreTable;
    @FXML private TableColumn<?, ?> core;

    @FXML private TableView<?> selectCoreTable;
    @FXML private TableColumn<?, ?> selectCore;

    @FXML
    private void initialize() {
        // TEMP DISABLED:
        // data.loadAll();
        // workingUnit = SelectedUnitContext.getSelectedUnit();
        // populate tables from DB

        if (factiontxt != null) {
            factiontxt.setText("N/A (schema changed)");
            factiontxt.setEditable(false);
        }
        if (compositionTxt != null) compositionTxt.clear();
        if (maintxt != null) maintxt.clear();
    }

    @FXML
    void addCore(MouseEvent event) {
        showWarning("Temporarily Disabled",
                "Core ability selection is disabled until migrated to new schema.");
    }

    @FXML
    void delete(MouseEvent event) {
        showWarning("Temporarily Disabled",
                "Delete is disabled until migrated to new schema.");
    }

    @FXML
    void editComposition(MouseEvent event) {
        if (compositionTxt != null) {
            compositionTxt.setDisable(!compositionTxt.isDisable());
        }
    }

    @FXML
    void save(MouseEvent event) {
        showWarning("Temporarily Disabled",
                "Save is disabled until UnitAbility is migrated to new schema.");
    }

    @FXML
    void cancelChange(MouseEvent event) throws IOException {
        FixedAspectView.switchTo((Node) event.getSource(),
                "/eecs2311/group2/wh40k_easycombat/RuleEditor.fxml",
                1000.0, 600.0);
    }

    private void showWarning(String title, String msg) {
        Alert a = new Alert(Alert.AlertType.WARNING);
        a.setTitle(title);
        a.setHeaderText(null);
        a.setContentText(msg);
        DialogHelper.styleAlert(a);
        a.showAndWait();
    }
}