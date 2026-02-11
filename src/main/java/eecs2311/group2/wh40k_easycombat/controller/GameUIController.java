package eecs2311.group2.wh40k_easycombat.controller;

import java.io.IOException;
import java.util.Optional;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.Spinner;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TreeTableColumn;
import javafx.scene.control.TreeTableView;
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

	
	// When click "Exit Game" button, the alert will show, click ok will exit the game and back to Main page
    @FXML
    void clickExit(MouseEvent event) throws IOException {
        FXMLLoader loader = new FXMLLoader(
                getClass().getResource("/eecs2311/group2/wh40k_easycombat/MainUI.fxml")
        );
        Parent root = loader.load();

    	Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Exit");
        alert.setHeaderText("Are you sure you want to exit the current Game?");
        alert.setContentText("Unsaved changes will be lost.");

        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            Stage stage = (Stage) ((Node) event.getSource())
                    .getScene()
                    .getWindow();
            
            Scene scene = new Scene(root);
            stage.setScene(scene);
            stage.show();
        }
    }

}
