package application;

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
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;
import javafx.stage.Stage;

public class RuleEditorController {
	// ======================= Buttons =======================
	@FXML private Button abilityButton;
	@FXML private Button cancelButton;
	@FXML private Button saveButton;

	@FXML private Button unitKeywordAddButton;
	@FXML private Button weaponKeywordAddButton;
	@FXML private Button keywordEditButton_Unit;
	@FXML private Button keywordEditButton_Weapon;

	// ======================= ComboBox =======================
	@FXML private ComboBox<?> factionCBbox;

	// ======================= Keyword Area =======================
	@FXML private TextArea keywordTextbox;

	// ======================= Unit Basic Inputs =======================
	@FXML private TextField unitNametxtBox;
	@FXML private TextField pointtxtBox;

	@FXML private TextField mBox;
	@FXML private TextField ocBOX;
	@FXML private TextField ldBox;
	@FXML private TextField svBox;
	@FXML private TextField tBox;
	@FXML private TextField wBox;

	// ======================= Weapon Inputs =======================
	@FXML private TextField wName;
	@FXML private TextField wRange;
	@FXML private TextField wA;
	@FXML private TextField wBS;
	@FXML private TextField wS;
	@FXML private TextField wAP;
	@FXML private TextField wD;

	// ======================= Tables - Melee Weapon =======================
	@FXML private TableView<?> meleeWeaponTable;
	@FXML private TableColumn<?, ?> mName;
	@FXML private TableColumn<?, ?> mA;
	@FXML private TableColumn<?, ?> mWS;
	@FXML private TableColumn<?, ?> mS;
	@FXML private TableColumn<?, ?> mAP;
	@FXML private TableColumn<?, ?> mD;
	@FXML private TableColumn<?, ?> mK;

	// ======================= Tables - Ranged Weapon =======================
	@FXML private TableView<?> rangedWeaponTable;
	@FXML private TableColumn<?, ?> rName;
	@FXML private TableColumn<?, ?> rRange;
	@FXML private TableColumn<?, ?> rA;
	@FXML private TableColumn<?, ?> rBS;
	@FXML private TableColumn<?, ?> rS;
	@FXML private TableColumn<?, ?> rAP;
	@FXML private TableColumn<?, ?> rD;
	@FXML private TableColumn<?, ?> rK;

	// ======================= Tables - Unit Keyword =======================
	@FXML private TableView<?> unitKeywordTable;

	
	// When click "Cancel" button
	@FXML
	void cancel(MouseEvent event) throws IOException {
		FXMLLoader loader = new FXMLLoader(
                getClass().getResource("RulesUI.fxml")
        );
        Parent root = loader.load();

        Stage stage = (Stage) ((Node) event.getSource())
                .getScene()
                .getWindow();
        
        Scene scene = new Scene(root);
        stage.setScene(scene);
        stage.show();
	}

	// When click "Save" button
	@FXML
	void save(MouseEvent event) {

	}

}
