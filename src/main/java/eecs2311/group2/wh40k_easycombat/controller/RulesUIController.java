package eecs2311.group2.wh40k_easycombat.controller;

import java.io.IOException;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.TreeTableColumn;
import javafx.scene.control.TreeTableView;
import javafx.scene.control.ComboBox;
import javafx.scene.input.MouseEvent;

public class RulesUIController {
	// ======================= Buttons ==========================
	@FXML private Button addButton;
	@FXML private Button backButton;
	@FXML private Button CoreRuleBackButton;
	@FXML private Button confirmButton;
	@FXML private Button editButton;
	@FXML private Button searchButton;

	// ======================= Inputs ===========================
	@FXML private TextField searchbox;
	@FXML private ComboBox<?> factionCBbox;

	// ======================= Text Areas =======================
	@FXML private TextArea coreBox;
	@FXML private TextArea factionBox;
	@FXML private TextArea keyBox;
	@FXML private TextArea mainBox;
	@FXML private TextArea unitBox;

	// ======================= Labels ===========================
	@FXML private Label unitNameLabel;
	@FXML private Label pointLable;
    @FXML private Label isvLabel;
	@FXML private Label mLabel;
	@FXML private Label tLabel;
	@FXML private Label wLabel;
	@FXML private Label svLabel;
	@FXML private Label ocLabel;
	@FXML private Label ldLabel;

	// ======================= Tables - Melee Weapon ========================
	@FXML private TableView<?> meleeWeaponTable;
	@FXML private TableColumn<?, ?> mName;
	@FXML private TableColumn<?, ?> mA;
	@FXML private TableColumn<?, ?> mAP;
	@FXML private TableColumn<?, ?> mD;
	@FXML private TableColumn<?, ?> mK;
	@FXML private TableColumn<?, ?> mS;
	@FXML private TableColumn<?, ?> mWS;

	// ======================= Tables - Ranged Weapon =======================
	@FXML private TableView<?> rangedWeaponTable;
	@FXML private TableColumn<?, ?> rName;
	@FXML private TableColumn<?, ?> rRange;
	@FXML private TableColumn<?, ?> rA;
	@FXML private TableColumn<?, ?> rAP;
	@FXML private TableColumn<?, ?> rBS;
	@FXML private TableColumn<?, ?> rD;
	@FXML private TableColumn<?, ?> rK;
	@FXML private TableColumn<?, ?> rS;

	// ======================= TreeTable =====================================
	@FXML private TreeTableView<?> dataTreeTable;
	@FXML private TreeTableColumn<?, ?> dataTreeColumn;


    // When click "Back to Main Page" button, back to Main page
    @FXML
    void backMainpage(MouseEvent event) throws IOException {
        Parent root = FXMLLoader.load(
                getClass().getResource("/eecs2311/group2/wh40k_easycombat/MainUI.fxml")
        );

        Stage stage = (Stage) ((Node) event.getSource())
                .getScene()
                .getWindow();

        stage.setScene(new Scene(root));
        stage.show();
    }
    
    // When click "Search" button, search the units
    @FXML
    void search(MouseEvent event) {

    }
    
    // When click "ADD" button, go to RuleEditor page
    @FXML
    void add(MouseEvent event) throws IOException {
    	Parent root = FXMLLoader.load(
                getClass().getResource("/eecs2311/group2/wh40k_easycombat/RuleEditor.fxml")
        );

        Stage stage = (Stage) ((Node) event.getSource())
                .getScene()
                .getWindow();

        stage.setScene(new Scene(root));
        stage.show();
    }
    
    // When click "Edit" button, get the data from unit and edit it
    @FXML
    void edit(MouseEvent event) {

    }
    
    // When click "Confirm" button, change the faction that user selected
    @FXML
    void confirm(MouseEvent event) {

    }

}
