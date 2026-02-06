package application;

import javafx.fxml.FXML;
import java.io.IOException;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.scene.control.Button;
import javafx.scene.input.MouseEvent;

public class MainUIController {
	@FXML
    private Button startButton;
    
	@FXML
    private Button ruleButton;
	
	@FXML
    private Button armyButton;

    @FXML
    private Button exitButton;

    // When click "Game Start" button
    @FXML
    void startBtn(MouseEvent event) {
    	
    }
    
    // When click "Rules and Datasheets" button
    @FXML
    void ruleBtn(MouseEvent event) throws IOException {
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

    // When click "Army" button
    @FXML
    void armyBtn(MouseEvent event) {
    	
    }

    // When click "Exit" button
    @FXML
    void exitBtn(MouseEvent event) {
    	
    } 
}
