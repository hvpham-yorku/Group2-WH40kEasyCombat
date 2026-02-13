package eecs2311.group2.wh40k_easycombat.controller;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
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
	@FXML private ComboBox<?> factionCBbox;
	@FXML private ComboBox<?> datachmentCBbox;
	@FXML private ComboBox<?> sizeCBbox;

	// ======================= Labels ========================
	@FXML private Label pointNumber;

	// ======================= Tables – Army Units ===========
	@FXML private TableView<?> armyTable;
	@FXML private TableColumn<?, ?> unit;
	@FXML private TableColumn<?, ?> point;
    @FXML private TableColumn<?, ?> composition;

	// ======================= Tables – Saved Armies =========
	@FXML private TableView<?> savedArmyTable;
	@FXML private TableColumn<?, ?> savedName;
	@FXML private TableColumn<?, ?> savedPoint;

	// ======================= TreeTable – Unit Selection ====
	@FXML private TreeTableView<?> unitSectionTreeTable;
	@FXML private TreeTableColumn<?, ?> selectUnit;
	@FXML private TreeTableColumn<?, ?> selectPoint;

	// -------------------- Demo Data --------------------
	private final ObservableList<SavedArmyRow> demoArmies = FXCollections.observableArrayList();
	private final ObservableList<UnitRow> demoArmyUnits = FXCollections.observableArrayList();

	@FXML
	private void initialize() {
		// 1) setup saved army columns
		if (savedName != null) {
			@SuppressWarnings("unchecked")
			TableColumn<SavedArmyRow, String> col = (TableColumn<SavedArmyRow, String>) savedName;
			col.setCellValueFactory(cd -> cd.getValue().nameProperty());
		}
		if (savedPoint != null) {
			@SuppressWarnings("unchecked")
			TableColumn<SavedArmyRow, Number> col = (TableColumn<SavedArmyRow, Number>) savedPoint;
			col.setCellValueFactory(cd -> cd.getValue().pointsProperty());
		}

		// 2) setup army table columns
		unsafeSetItems(armyTable, demoArmyUnits);

		if (unit != null) {
			@SuppressWarnings("unchecked")
			TableColumn<UnitRow, String> col = (TableColumn<UnitRow, String>) unit;
			col.setCellValueFactory(cd -> cd.getValue().nameProperty());
		}
		if (point != null) {
			@SuppressWarnings("unchecked")
			TableColumn<UnitRow, Number> col = (TableColumn<UnitRow, Number>) point;
			col.setCellValueFactory(cd -> cd.getValue().pointsProperty());
		}
		if (composition != null) {
			@SuppressWarnings("unchecked")
			TableColumn<UnitRow, String> col = (TableColumn<UnitRow, String>) composition;
			col.setCellValueFactory(cd -> cd.getValue().compositionProperty());
		}

		// 3) add ONE demo army in saved army table
		demoArmies.setAll(buildDemoArmies());
		unsafeSetItems(savedArmyTable, demoArmies);

		// auto-select the first army (optional)
		if (!demoArmies.isEmpty() && savedArmyTable != null) {
			savedArmyTable.getSelectionModel().select(0);
		}
	}

	private List<SavedArmyRow> buildDemoArmies() {
		List<SavedArmyRow> list = new ArrayList<>();
		list.add(new SavedArmyRow("test army", 500));
		return list;
	}

	private List<UnitRow> buildDemoUnitsForArmy(String armyName) {
		// 15 units total 500 points
		// 10 units * 30 = 300
		// 5 units  * 40 = 200
		// total = 500
		List<UnitRow> rows = new ArrayList<>();
		for (int i = 1; i <= 10; i++) {
			rows.add(new UnitRow("test unit" + i, 30, "1 model"));
		}
		for (int i = 11; i <= 15; i++) {
			rows.add(new UnitRow("test unit" + i, 40, "1 model"));
		}
		return rows;
	}

	private int sumPoints(List<UnitRow> rows) {
		int sum = 0;
		for (UnitRow r : rows) sum += r.points();
		return sum;
	}

	// When click "Cancel" button, back to main page
	@FXML
    void cancelTheChange(MouseEvent event) throws IOException {
		FXMLLoader loader = new FXMLLoader(
                getClass().getResource("/eecs2311/group2/wh40k_easycombat/MainUI.fxml")
        );
        Parent root = loader.load();

        Stage stage = (Stage) ((Node) event.getSource())
                .getScene()
                .getWindow();

        Scene scene = new Scene(root);
        stage.setScene(scene);
        stage.show();
    }

	// When click "ADD" button, add the unit to army
    @FXML
    void add(MouseEvent event) {
        // not required for demo
    }

    // When click "Confirm" button, change the faction to user selected
    @FXML
    void confirm(MouseEvent event) {
        // not required for demo
    }

    // When click "Load" button, load the army from army list that user selected
    @FXML
    void loadArmy(MouseEvent event) {
    	SavedArmyRow selected = unsafeGetSelected(savedArmyTable);
    	if (selected == null) {
    		showWarn("Please select an army in Saved Army table first.");
    		return;
    	}

    	String name = selected.name();
    	List<UnitRow> rows = buildDemoUnitsForArmy(name);

    	// load into army table
    	demoArmyUnits.setAll(rows);

    	// update name + actual total points
    	int total = sumPoints(rows);

    	if (armyNametxt != null) armyNametxt.setText(name);
    	if (pointNumber != null) pointNumber.setText(String.valueOf(total));
    }

    // When click "Favorite" button, mark the army as favorite
    @FXML
    void favorite(MouseEvent event) {
        // not required for demo
    }

    // When click "Delete" button, delete the army from army list that user selected
    @FXML
    void delete(MouseEvent event) {
        // not required for demo
    }

    // When click "Import" button, import the army table from user's Clipboard
    @FXML
    void importData(MouseEvent event) {
        // not required for demo
    }

    // When click "Remove" button, remove the unit from army
    @FXML
    void removeUnit(MouseEvent event) {
        // not required for demo
    }

    // When click "Save" button, save the army table to database
    @FXML
    void save(MouseEvent event) {
        // not required for demo
    }

    // When click "Set Warlord" button, set the unit to be warlord
    @FXML
    void setWarlord(MouseEvent event) {
        // not required for demo
    }

	// ---------------- Helper UI ----------------
	private void showWarn(String msg) {
		Alert a = new Alert(Alert.AlertType.WARNING);
		a.setTitle("Warning");
		a.setHeaderText(null);
		a.setContentText(msg);
		a.showAndWait();
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	private static void unsafeSetItems(TableView table, ObservableList items) {
		if (table != null) table.setItems(items);
	}

	@SuppressWarnings({ "rawtypes" })
	private static SavedArmyRow unsafeGetSelected(TableView table) {
		if (table == null) return null;
		Object o = table.getSelectionModel().getSelectedItem();
		return (o instanceof SavedArmyRow) ? (SavedArmyRow) o : null;
	}

	// ---------------- Row models ----------------
	public static class SavedArmyRow {
		private final StringProperty name = new SimpleStringProperty();
		private final IntegerProperty points = new SimpleIntegerProperty();

		public SavedArmyRow(String name, int points) {
			this.name.set(name);
			this.points.set(points);
		}

		public String name() { return name.get(); }
		public int points() { return points.get(); }

		public StringProperty nameProperty() { return name; }
		public IntegerProperty pointsProperty() { return points; }
	}

	public static class UnitRow {
		private final StringProperty name = new SimpleStringProperty();
		private final IntegerProperty points = new SimpleIntegerProperty();
		private final StringProperty composition = new SimpleStringProperty();

		public UnitRow(String name, int points, String composition) {
			this.name.set(name);
			this.points.set(points);
			this.composition.set(composition);
		}

		public String name() { return name.get(); }
		public int points() { return points.get(); }
		public String composition() { return composition.get(); }

		public StringProperty nameProperty() { return name; }
		public IntegerProperty pointsProperty() { return points; }
		public StringProperty compositionProperty() { return composition; }
	}
}
