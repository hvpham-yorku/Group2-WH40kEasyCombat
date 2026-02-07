package eecs2311.group2.wh40k_easycombat;

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

	
	@FXML	
	//When click "Cancel" button, back to main page
    void cancelTheChange(MouseEvent event) throws IOException {
		FXMLLoader loader = new FXMLLoader(
                getClass().getResource("MainUI.fxml")
        );
        Parent root = loader.load();

        Stage stage = (Stage) ((Node) event.getSource())
                .getScene()
                .getWindow();
        
        Scene scene = new Scene(root);
        stage.setScene(scene);
        stage.show();
    }

}
