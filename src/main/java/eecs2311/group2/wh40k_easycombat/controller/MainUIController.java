package eecs2311.group2.wh40k_easycombat.controller;

import javafx.fxml.FXML;
import java.io.IOException;
import java.util.Optional;
import eecs2311.group2.wh40k_easycombat.controller.helper.DialogHelper;
import eecs2311.group2.wh40k_easycombat.util.FixedAspectView;
import javafx.scene.Node;
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
    void startBtn(MouseEvent event) throws IOException {
        FixedAspectView.switchResponsiveTo(
                (Node) event.getSource(),
                "/eecs2311/group2/wh40k_easycombat/GameUI.fxml",
                1024.0,
                680.0,
                1500.0,
                900.0
        );
    }

    
    // When click "Rules and Datasheets" button
    @FXML
    void ruleBtn(MouseEvent event) throws IOException {
    	FixedAspectView.switchResponsiveTo(
                (Node) event.getSource(),
                "/eecs2311/group2/wh40k_easycombat/Datasheets.fxml",
                1080.0,
                700.0,
                1320.0,
                820.0
        );
    }

    // When click "Army" button, go to army page
    @FXML
    void armyBtn(MouseEvent event) throws IOException {
        FixedAspectView.switchResponsiveTo(
                (Node) event.getSource(),
                "/eecs2311/group2/wh40k_easycombat/Army.fxml",
                1080.0,
                700.0,
                1320.0,
                820.0
        );
    }

    // When click "Exit" button, exit the program
    @FXML
    void exitBtn(MouseEvent event) {
    	Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Exit");
        alert.setHeaderText("Are you sure you want to exit?");
        alert.setContentText("Unsaved changes will be lost.");
        DialogHelper.styleAlert(alert);

        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            Stage stage = (Stage) exitButton.getScene().getWindow();
            stage.close();
        }
    } 
}
