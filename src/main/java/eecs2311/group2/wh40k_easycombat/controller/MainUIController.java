package eecs2311.group2.wh40k_easycombat.controller;

import javafx.fxml.FXML;
import java.io.IOException;
import java.util.Optional;

import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.input.MouseEvent;

public class MainUIController {
	@FXML private Button startButton;
    
	@FXML private Button ruleButton;
	
	@FXML private Button armyButton;

    @FXML private Button exitButton;

    // When click "Game Start" button
    @FXML
    void startBtn(MouseEvent event) {
    	
    }
    
    // When click "Rules and Datasheets" button
    @FXML
    void ruleBtn(MouseEvent event) throws IOException {
        FXMLLoader loader = new FXMLLoader(
                getClass().getResource("/eecs2311/group2/wh40k_easycombat/RulesUI.fxml")
        );
        Parent root = loader.load();

        Stage stage = (Stage) ((Node) event.getSource())
                .getScene()
                .getWindow();
        
        Scene scene = new Scene(root);
        stage.setScene(scene);
        stage.show();
    }

    // When click "Army" button, go to army page
    @FXML
    void armyBtn(MouseEvent event) throws IOException {
    	FXMLLoader loader = new FXMLLoader(
                getClass().getResource("/eecs2311/group2/wh40k_easycombat/Army.fxml")
        );
        Parent root = loader.load();

        Stage stage = (Stage) ((Node) event.getSource())
                .getScene()
                .getWindow();
        
        Scene scene = new Scene(root);
        stage.setScene(scene);
        stage.show();
    }

    // When click "Exit" button, exit the program
    @FXML
    void exitBtn(MouseEvent event) {
    	Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Exit");
        alert.setHeaderText("Are you sure you want to exit?");
        alert.setContentText("Unsaved changes will be lost.");

        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            Stage stage = (Stage) exitButton.getScene().getWindow();
            stage.close();
        }
    } 
}
