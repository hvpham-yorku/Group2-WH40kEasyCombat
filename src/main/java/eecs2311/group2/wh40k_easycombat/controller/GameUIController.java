package eecs2311.group2.wh40k_easycombat.controller;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import eecs2311.group2.wh40k_easycombat.util.FixedAspectView;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import javafx.stage.Stage;

public class GameUIController {
	// ======================= Global Controls ======================
	@FXML private Button exitGameButton;
	@FXML private Button battleLogButton;
	@FXML private Button nextRoundButton;
	@FXML private Button rollButton;
	@FXML private CheckBox autoBattleCheckBox;

	// ======================= Round / Mission Info =================
	@FXML private Label roundLabel;
	@FXML private Label missionNameLabel;

	// ======================= Virtual Dice =========================
	@FXML private Spinner<?> virtuaDiceSpinner;
	@FXML private TextArea virtuaDiceBox;

	// ======================= Blue Player – Status =================
	@FXML private Label blueCPLabel;
	@FXML private Label blueVPLabel;

	// ======================= Blue Player – CP / Stratagem ==========
	@FXML private TreeTableView<?> blueCPTable;
	@FXML private TreeTableColumn<?, ?> blueStratagem;
	@FXML private TreeTableColumn<?, ?> blueCPcost;

	// ======================= Blue Player – Missions ================
	@FXML private TableView<?> blueMissionTable;
	@FXML private TableColumn<?, ?> blueMission;
	@FXML private TableColumn<?, ?> blueState;

	// ======================= Blue Player – Units ===================
	@FXML private TableView<?> blueUnitTable;
	@FXML private TableColumn<?, ?> blueUnits;

	// ======================= Blue Player – Actions =================
	@FXML private Button blueImportButton;
	@FXML private Button blueSelectButton;
	@FXML private Button bluePlusButton;
	@FXML private Button blueSubButton;

	// ======================= Red Player – Status ==================
	@FXML private Label redCPLabel;
	@FXML private Label redVPLabel;

	// ======================= Red Player – CP / Stratagem ===========
	@FXML private TreeTableView<?> redCPTable;
	@FXML private TreeTableColumn<?, ?> redStratagem;
	@FXML private TreeTableColumn<?, ?> redCPcost;

	// ======================= Red Player – Missions =================
	@FXML private TableView<?> redMissionTable;
	@FXML private TableColumn<?, ?> redMission;
	@FXML private TableColumn<?, ?> redState;

	// ======================= Red Player – Units ====================
	@FXML private TableView<?> redUnitTable;
	@FXML private TableColumn<?, ?> redUnits;

	// ======================= Red Player – Actions ==================
	@FXML private Button redImportButton;
	@FXML private Button redSelectButton;
	@FXML private Button redPlusButton;
	@FXML private Button redSubButton;

	// ======================= Local State ==========================
	private final Random rng = new Random();
	private int round = 1;
	private int blueCP = 0;
	private int redCP = 0;

	private final ObservableList<UnitNameRow> blueUnitRows = FXCollections.observableArrayList();
	private final ObservableList<UnitNameRow> redUnitRows  = FXCollections.observableArrayList();

	@FXML
	private void initialize() {
		// Round label
		updateRoundLabel();

		// Mission name (optional demo)
		if (missionNameLabel != null && (missionNameLabel.getText() == null || missionNameLabel.getText().isBlank())) {
			missionNameLabel.setText("Demo Mission");
		}

		// CP init
		updateCPLabels();

		// VP init (optional)
		if (blueVPLabel != null && (blueVPLabel.getText() == null || blueVPLabel.getText().isBlank())) blueVPLabel.setText("0");
		if (redVPLabel != null && (redVPLabel.getText() == null || redVPLabel.getText().isBlank())) redVPLabel.setText("0");

		// Dice spinner: default 1, range 1..50
		initDiceSpinner();

		// Unit tables
		initUnitTables();
	}

	// ======================= Blue Player ============================
	// When click "+" button, blue CP + 1
	@FXML
	void blueClickPlus(MouseEvent event) {
		blueCP++;
		updateCPLabels();
	}

	// When click "-" button, blue CP - 1
	@FXML
	void blueClickSub(MouseEvent event) {
		blueCP = Math.max(0, blueCP - 1);
		updateCPLabels();
	}

	// When click "Import Army" button, import the Army that selected to blue player
	@FXML
	void blueImport(MouseEvent event) {
		blueUnitRows.setAll(buildDemoArmyUnits());
		unsafeSetItems(blueUnitTable, blueUnitRows);
	}

	// When click "Select" button, blue use the stratagem and subtract the corresponding CP
	@FXML
	void blueSelect(MouseEvent event) {
		// Demo only – no stratagem logic required
		showInfo("Demo", "Stratagem system not required for demo.");
	}

	// ======================= Red Player ===============================
	// When click "+" button, red CP + 1
	@FXML
	void redClickPlus(MouseEvent event) {
		redCP++;
		updateCPLabels();
	}

	// When click "-" button, red CP - 1
	@FXML
	void redClickSub(MouseEvent event) {
		redCP = Math.max(0, redCP - 1);
		updateCPLabels();
	}

	// When click "Import Army" button, import the Army that selected to red player
	@FXML
	void redImport(MouseEvent event) {
		redUnitRows.setAll(buildDemoArmyUnits());
		unsafeSetItems(redUnitTable, redUnitRows);
	}

	// When click "Select" button, red use the stratagem and subtract the corresponding CP
	@FXML
	void redSelect(MouseEvent event) {
		// Demo only – no stratagem logic required
		showInfo("Demo", "Stratagem system not required for demo.");
	}

	// ======================= Global =====================================
	// When click "Roll" button, roll the numbers of dice that player set
	@FXML
	void rollDice(MouseEvent event) {
		int n = readDiceCount();
		if (n <= 0) {
			showWarn("Dice count must be a positive integer.");
			return;
		}

		List<Integer> results = new ArrayList<>(n);
		for (int i = 0; i < n; i++) {
			results.add(rng.nextInt(6) + 1);
		}

		String text = results.stream()
				.map(String::valueOf)
				.reduce((a, b) -> a + ", " + b)
				.orElse("");

		if (virtuaDiceBox != null) {
			virtuaDiceBox.setText(text);
		}
	}

	// When click "Next Round" button, start next round
	@FXML
	void nextRound(MouseEvent event) {
		round++;
		blueCP++;
		redCP++;
		updateRoundLabel();
		updateCPLabels();
	}

	// When click "Battle Log" button, open the battle log widow
	@FXML
	void openLog(MouseEvent event) {
		// Minimal demo window: show current dice box content
		String logText = (virtuaDiceBox == null) ? "" : virtuaDiceBox.getText();

		TextArea area = new TextArea();
		area.setEditable(false);
		area.setWrapText(true);
		area.setText("Battle Log (Demo)\n\nLast Dice Result:\n" + logText);

		Stage s = new Stage();
		s.setTitle("Battle Log");
		s.setScene(new Scene(area, 420, 320));
		s.show();
	}

	// When click "Exit Game" button, the alert will show, click ok will exit the game and back to Main page
	@FXML
	void clickExit(MouseEvent event) throws IOException {
		Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
		alert.setTitle("Exit");
		alert.setHeaderText("Are you sure you want to exit the current Game?");
		alert.setContentText("Unsaved changes will be lost.");

		Optional<ButtonType> result = alert.showAndWait();
		if (result.isPresent() && result.get() == ButtonType.OK) {
			FixedAspectView.switchTo((Node) event.getSource(),
					"/eecs2311/group2/wh40k_easycombat/MainUI.fxml",
					1200.0, 800.0);
		}
	}

	// ======================= Helpers =======================

	private void updateRoundLabel() {
		if (roundLabel != null) roundLabel.setText(String.valueOf(round));
	}

	private void updateCPLabels() {
		if (blueCPLabel != null) blueCPLabel.setText(String.valueOf(blueCP));
		if (redCPLabel != null) redCPLabel.setText(String.valueOf(redCP));
	}

	private void initDiceSpinner() {
		if (virtuaDiceSpinner == null) return;

		@SuppressWarnings("unchecked")
		Spinner<Integer> sp = (Spinner<Integer>) virtuaDiceSpinner;

		SpinnerValueFactory<Integer> vf = new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 50, 1);
		sp.setValueFactory(vf);
		sp.setEditable(true);
	}

	private int readDiceCount() {
		if (virtuaDiceSpinner == null) return 1;

		try {
			@SuppressWarnings("unchecked")
			Spinner<Integer> sp = (Spinner<Integer>) virtuaDiceSpinner;

			Integer v = sp.getValue();
			if (v != null) return v;

			// fallback: user typed in editor
			String txt = sp.getEditor() == null ? "" : sp.getEditor().getText();
			return Integer.parseInt(txt.trim());

		} catch (Exception e) {
			// try parse from editor anyway
			try {
				@SuppressWarnings("unchecked")
				Spinner<Integer> sp = (Spinner<Integer>) virtuaDiceSpinner;
				String txt = sp.getEditor() == null ? "" : sp.getEditor().getText();
				return Integer.parseInt(txt.trim());
			} catch (Exception ignored) {
				return -1;
			}
		}
	}

	private void initUnitTables() {
		// Blue
		if (blueUnits != null) {
			@SuppressWarnings("unchecked")
			TableColumn<UnitNameRow, String> c = (TableColumn<UnitNameRow, String>) blueUnits;
			c.setCellValueFactory(cd -> cd.getValue().nameProperty());
		}
		unsafeSetItems(blueUnitTable, blueUnitRows);

		// Red
		if (redUnits != null) {
			@SuppressWarnings("unchecked")
			TableColumn<UnitNameRow, String> c = (TableColumn<UnitNameRow, String>) redUnits;
			c.setCellValueFactory(cd -> cd.getValue().nameProperty());
		}
		unsafeSetItems(redUnitTable, redUnitRows);
	}

	private List<UnitNameRow> buildDemoArmyUnits() {
		// “500分十几个人”——这里做 15 个 unit（名字 test unit1..15）
		List<UnitNameRow> rows = new ArrayList<>();
		for (int i = 1; i <= 15; i++) {
			rows.add(new UnitNameRow("test unit" + i));
		}
		return rows;
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	private static void unsafeSetItems(TableView table, ObservableList items) {
		if (table != null) table.setItems(items);
	}

	private void showWarn(String msg) {
		Alert a = new Alert(Alert.AlertType.WARNING);
		a.setTitle("Warning");
		a.setHeaderText(null);
		a.setContentText(msg);
		a.showAndWait();
	}

	private void showInfo(String title, String msg) {
		Alert a = new Alert(Alert.AlertType.INFORMATION);
		a.setTitle(title);
		a.setHeaderText(null);
		a.setContentText(msg);
		a.showAndWait();
	}

	// ======================= Row Model =======================
	public static class UnitNameRow {
		private final StringProperty name = new SimpleStringProperty();

		public UnitNameRow(String name) {
			this.name.set(name);
		}

		public StringProperty nameProperty() { return name; }
	}
}
