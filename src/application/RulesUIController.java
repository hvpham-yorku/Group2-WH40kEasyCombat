package application;

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
import javafx.scene.input.MouseEvent;

public class RulesUIController {
    @FXML
    private Button addButton;

    @FXML
    private Button backButton;

    @FXML
    private TextArea coreBox;

    @FXML
    private TreeTableColumn<?, ?> dataTreeColumn;

    @FXML
    private TreeTableView<?> dataTreeTable;

    @FXML
    private Button editButton;

    @FXML
    private TextArea factionBox;

    @FXML
    private TextArea keyBox;

    @FXML
    private Label ldLabel;

    @FXML
    private TableColumn<?, ?> mA;

    @FXML
    private TableColumn<?, ?> mAP;

    @FXML
    private TableColumn<?, ?> mD;

    @FXML
    private TableColumn<?, ?> mK;

    @FXML
    private Label mLabel;

    @FXML
    private TableColumn<?, ?> mName;

    @FXML
    private TableColumn<?, ?> mS;

    @FXML
    private TableColumn<?, ?> mWS;

    @FXML
    private TextArea mainBox;

    @FXML
    private TableView<?> meleeWeaponTable;

    @FXML
    private Label ocLabel;

    @FXML
    private TableColumn<?, ?> rA;

    @FXML
    private TableColumn<?, ?> rAP;

    @FXML
    private TableColumn<?, ?> rBS;

    @FXML
    private TableColumn<?, ?> rD;

    @FXML
    private TableColumn<?, ?> rK;

    @FXML
    private TableColumn<?, ?> rName;

    @FXML
    private TableColumn<?, ?> rRange;

    @FXML
    private TableColumn<?, ?> rS;

    @FXML
    private TableView<?> rangedWeaponTable;

    @FXML
    private Button searchButton;

    @FXML
    private TextField searchbox;

    @FXML
    private Label svLabel;

    @FXML
    private Label tLabel;

    @FXML
    private TextArea unitBox;

    @FXML
    private Label unitNameLabel;

    @FXML
    private Label wLabel;

    @FXML
    void backMainpage(MouseEvent event) throws IOException {
        Parent root = FXMLLoader.load(
                getClass().getResource("MainUI.fxml")
        );

        Stage stage = (Stage) ((Node) event.getSource())
                .getScene()
                .getWindow();

        stage.setScene(new Scene(root));
        stage.show();
    }
}
