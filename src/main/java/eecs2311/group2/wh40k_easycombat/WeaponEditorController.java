package eecs2311.group2.wh40k_easycombat;

import java.io.IOException;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;
import javafx.stage.Stage;


public class WeaponEditorController {
	// ======================= Buttons ===============================
	@FXML private Button backButton;
	@FXML private Button saveButton;
	@FXML private Button clearbutton;
	@FXML private Button editButton;
	@FXML private Button deleteButton;
    @FXML private Button deleteWeaponButton;
	@FXML private Button basicWeaponAddButton;
	@FXML private Button keywordAddButton;

	// ======================= ComboBox ==============================
	@FXML private ComboBox<?> categoryCBbox;

	// ======================= TextFields – Weapon Basic Info ========
	@FXML private TextField nametxt;     // weapon name
	@FXML private TextField rangetxt;    // range
	@FXML private TextField atxt;        // attacks
	@FXML private TextField bstxt;       // BS / WS
	@FXML private TextField stxt;        // strength
	@FXML private TextField aptxt;       // AP
	@FXML private TextField dtxt;        // damage

	// ======================= Tables – Melee Weapons ================
	@FXML private TableView<?> meleeWeaponTable;
	@FXML private TableColumn<?, ?> meleeWeaponName;

	// ======================= Tables – Ranged Weapons ===============
	@FXML private TableView<?> rangedWepponTable;
	@FXML private TableColumn<?, ?> rangeWeaponName;

	// ======================= Tables – Keywords =====================
	@FXML private TableView<?> selectionKeywordTable;
	@FXML private TableColumn<?, ?> selectionKeyword;
    @FXML private TableView<?> keywordTable;
	@FXML private TableColumn<?, ?> words;

	
	//When click "Back" button, back to RuleEditor page
	@FXML
    void backToUnitEdtior(MouseEvent event) throws IOException {
		FXMLLoader loader = new FXMLLoader(
                getClass().getResource("RuleEditor.fxml")
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
