package eecs2311.group2.wh40k_easycombat.controller;

import java.io.IOException;
import eecs2311.group2.wh40k_easycombat.util.FixedAspectView;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;

public class RuleEditorController {
	// ======================= Buttons ====================================
	@FXML private Button abilityButton;
	@FXML private Button cancelButton;
	@FXML private Button saveButton;

	@FXML private Button unitKeywordAddButton;
	@FXML private Button weaponAddButton;
	@FXML private Button keywordEditButton_Unit;

	// ======================= ComboBox ===================================
	@FXML private ComboBox<?> factionCBbox;

	// ======================= Keyword Area ===============================
	@FXML private TextArea keywordTextbox;

	// ======================= Unit Basic Inputs ==========================
	@FXML private TextField unitNametxtBox;
	@FXML private TextField pointtxtBox;
    @FXML private TextField isvBox;
	@FXML private TextField mBox;
	@FXML private TextField ocBOX;
	@FXML private TextField ldBox;
	@FXML private TextField svBox;
	@FXML private TextField tBox;
	@FXML private TextField wBox;

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

	
	// When click "Cancel" button, back to Rules page
	@FXML
	void cancel(MouseEvent event) throws IOException {
	    FixedAspectView.switchTo((Node) event.getSource(),
	            "/eecs2311/group2/wh40k_easycombat/RulesUI.fxml",
	            1200.0, 800.0);
	}

	// When click "Save" button, save all the data to database and return to RulesUIpage
	@FXML
	void save(MouseEvent event) {

	}
	
	// When click "ADD" button of add keywords, add the keyword that select in table to unit
    @FXML
    void keywordAdd(MouseEvent event) {

    }
	
	// When click "ADD" button of add weapon, go to WeaponEditor page
	@FXML
    void addWeapon(MouseEvent event) throws IOException {
	    FixedAspectView.switchTo((Node) event.getSource(),
	            "/eecs2311/group2/wh40k_easycombat/WeaponEditor.fxml",
	            1200.0, 800.0);
    }
	
	// When click "Ability Setting" button, go to UnitAbility page
    @FXML
    void abilitySetting(MouseEvent event) throws IOException {
        FixedAspectView.switchTo((Node) event.getSource(),
                "/eecs2311/group2/wh40k_easycombat/UnitAbility.fxml",
                1000.0, 600.0);
    }

}
