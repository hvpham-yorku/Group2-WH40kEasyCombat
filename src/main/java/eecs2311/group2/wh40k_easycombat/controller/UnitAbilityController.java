package eecs2311.group2.wh40k_easycombat.controller;

import java.io.IOException;
import eecs2311.group2.wh40k_easycombat.util.FixedAspectView;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;

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
	
	// When click "ADD" button, add the core ability to unit
	@FXML
    void addCore(MouseEvent event) {

    }
	
	// When click "Delete" button, delete the core ability of unit
	@FXML
	void delete(MouseEvent event) {

	}

	// When click "Edit" button, edit and save the composition of unit
	@FXML
	void editComposition(MouseEvent event) {

	}

	// When click "Save" button, save all abilities to unit
	@FXML
	void save(MouseEvent event) {

	}
	 
	// When click "Cancel" button, will back to RuleEditor page
	@FXML
	void cancelChange(MouseEvent event) throws IOException {
	    FixedAspectView.switchTo((Node) event.getSource(),
	            "/eecs2311/group2/wh40k_easycombat/RuleEditor.fxml",
	            1000.0, 600.0);
	}

}
