package eecs2311.group2.wh40k_easycombat;


import java.io.IOException;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;
import javafx.stage.Stage;

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

	// ======================= Tables – Core Abilities ===============
	@FXML private TableView<?> coreTable;
	@FXML private TableColumn<?, ?> core;

	// ======================= Tables – Selected Core Abilities ======
	@FXML private TableView<?> selectCoreTable;
	@FXML private TableColumn<?, ?> selectCore;
	
	
	@FXML
	void cancelChange(MouseEvent event) throws IOException {
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
