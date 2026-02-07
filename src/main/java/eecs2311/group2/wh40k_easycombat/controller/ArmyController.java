package eecs2311.group2.wh40k_easycombat.controller;

import java.io.IOException;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
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

    }

    // When click "Confirm" button, change the faction to user selected
    @FXML
    void confirm(MouseEvent event) {

    }
    
    // When click "Load" button, load the army from army list that user selected
    @FXML
    void loadArmy(MouseEvent event) {

    }

    // When click "Delete" button, delete the army from army list that user selected
    @FXML
    void delete(MouseEvent event) {

    }

    // When click "Import" button, import the army table from user's Clipboard
    @FXML
    void importData(MouseEvent event) {

    }

    // When click "Remove" button, remove the unit from army
    @FXML
    void removeUnit(MouseEvent event) {

    }

    // When click "Save" button, save the army table to database
    @FXML
    void save(MouseEvent event) {

    }

    // When click "Set Warlord" button, set the unit to be warlord
    @FXML
    void setWarlord(MouseEvent event) {

    }

}
