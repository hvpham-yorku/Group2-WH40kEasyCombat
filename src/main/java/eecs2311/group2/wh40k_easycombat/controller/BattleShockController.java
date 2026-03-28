package eecs2311.group2.wh40k_easycombat.controller;

import eecs2311.group2.wh40k_easycombat.controller.helper.DialogHelper;
import eecs2311.group2.wh40k_easycombat.model.combat.BattleShockTestResult;
import eecs2311.group2.wh40k_easycombat.model.instance.UnitInstance;
import eecs2311.group2.wh40k_easycombat.service.game.BattleShockService;
import eecs2311.group2.wh40k_easycombat.viewmodel.BattleShockUnitVM;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.CheckBoxTableCell;
import javafx.stage.Stage;

import java.util.List;

public class BattleShockController {
    // ======================= Labels =======================
    @FXML private Label headerLabel;
    @FXML private Label summaryLabel;

    // ======================= Battle-shock Table =======================
    @FXML private TableView<BattleShockUnitVM> battleShockTable;
    @FXML private TableColumn<BattleShockUnitVM, String> unitColumn;
    @FXML private TableColumn<BattleShockUnitVM, String> strengthColumn;
    @FXML private TableColumn<BattleShockUnitVM, Number> leadershipColumn;
    @FXML private TableColumn<BattleShockUnitVM, String> rollColumn;
    @FXML private TableColumn<BattleShockUnitVM, Boolean> battleShockedColumn;

    // ======================= Buttons =======================
    @FXML private Button rollSelectedButton;
    @FXML private Button rollAllButton;
    @FXML private Button closeButton;

    private final ObservableList<BattleShockUnitVM> rows = FXCollections.observableArrayList();
    private final BattleShockService battleShockService = new BattleShockService();
    private Runnable onStateChanged;

    // When this page loads, initialize the Battle-shock table and button state.
    @FXML
    private void initialize() {
        battleShockTable.setItems(rows);
        battleShockTable.setEditable(true);
        battleShockTable.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);

        unitColumn.setCellValueFactory(data -> data.getValue().unitNameProperty());
        strengthColumn.setCellValueFactory(data -> data.getValue().strengthTextProperty());
        leadershipColumn.setCellValueFactory(data -> data.getValue().leadershipProperty());
        rollColumn.setCellValueFactory(data -> data.getValue().rollResultProperty());
        battleShockedColumn.setCellValueFactory(data -> data.getValue().battleShockedProperty());
        battleShockedColumn.setCellFactory(CheckBoxTableCell.forTableColumn(battleShockedColumn));

        battleShockTable.setPlaceholder(new Label("No units need to take a Battle-shock test right now."));
        battleShockTable.getSelectionModel().selectedItemProperty().addListener((obs, oldValue, newValue) -> updateButtons());
    }

    public void setContext(
            String factionName,
            int round,
            List<UnitInstance> units,
            Runnable onStateChanged
    ) {
        this.onStateChanged = onStateChanged;
        rows.clear();

        if (units != null) {
            for (UnitInstance unit : units) {
                BattleShockUnitVM row = new BattleShockUnitVM(unit);
                row.battleShockedProperty().addListener((obs, oldValue, newValue) -> notifyStateChanged());
                rows.add(row);
            }
        }

        headerLabel.setText((factionName == null || factionName.isBlank() ? "Current Player" : factionName)
                + " Battle-shock Step");
        summaryLabel.setText("Round " + round
                + " Command phase. Roll 2D6 for each unit below half-strength. "
                + "Pass on a result equal to or higher than the best Leadership in that unit.");
        updateButtons();
    }

    // When click "Roll Selected" button, roll one Battle-shock test for the selected unit.
    @FXML
    private void rollSelected(ActionEvent event) {
        BattleShockUnitVM selected = battleShockTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            DialogHelper.showWarning("No Unit Selected", "Please select one unit to roll its Battle-shock test.");
            return;
        }
        if (!selected.isPending()) {
            DialogHelper.showWarning("Already Tested", "That unit has already taken its Battle-shock test.");
            return;
        }

        applyTest(selected);
    }

    // When click "Roll All" button, roll Battle-shock tests for all pending units.
    @FXML
    private void rollAll(ActionEvent event) {
        for (BattleShockUnitVM row : rows) {
            if (row.isPending()) {
                applyTest(row);
            }
        }
    }

    // When click "Close" button, confirm whether to leave with pending Battle-shock tests.
    @FXML
    private void closeWindow(ActionEvent event) {
        if (hasPendingRows() && !DialogHelper.confirmYesNo(
                "Pending Battle-shock Tests",
                "Some eligible units have not taken their Battle-shock tests yet. Close anyway?"
        )) {
            return;
        }

        ((Stage) closeButton.getScene().getWindow()).close();
    }

    private void applyTest(BattleShockUnitVM row) {
        BattleShockTestResult result = battleShockService.rollBattleShockTest(row.getUnit());
        row.applyTestResult(result);
        battleShockTable.refresh();
        notifyStateChanged();
        updateButtons();
    }

    private boolean hasPendingRows() {
        for (BattleShockUnitVM row : rows) {
            if (row.isPending()) {
                return true;
            }
        }
        return false;
    }

    private void updateButtons() {
        boolean hasRows = !rows.isEmpty();
        boolean hasPending = hasPendingRows();
        BattleShockUnitVM selected = battleShockTable.getSelectionModel().getSelectedItem();

        rollAllButton.setDisable(!hasRows || !hasPending);
        rollSelectedButton.setDisable(selected == null || !selected.isPending());
    }

    private void notifyStateChanged() {
        if (onStateChanged != null) {
            onStateChanged.run();
        }
    }
}
