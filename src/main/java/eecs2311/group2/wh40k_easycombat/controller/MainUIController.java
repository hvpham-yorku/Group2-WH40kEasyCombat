package eecs2311.group2.wh40k_easycombat.controller;

import javafx.application.Platform;
import javafx.fxml.FXML;
import java.io.IOException;
import java.util.Optional;
import eecs2311.group2.wh40k_easycombat.util.FixedAspectView;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.geometry.Rectangle2D;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.StackPane;

public class MainUIController {
	@FXML private Button startButton;
    
	@FXML private Button ruleButton;
	
	@FXML private Button armyButton;

    @FXML private Button exitButton;

    // When click "Game Start" button
    @FXML
    void startBtn(MouseEvent event) throws IOException {

        FXMLLoader loader = new FXMLLoader(
                getClass().getResource("/eecs2311/group2/wh40k_easycombat/GameUI.fxml")
        );
        Parent root = loader.load();

        Group content = new Group(root);

        StackPane wrapper = new StackPane(content);
        wrapper.setAlignment(Pos.CENTER);
        wrapper.setStyle("-fx-background-color: transparent;");

        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        Scene scene = new Scene(wrapper);
        stage.setScene(scene);
        stage.setResizable(true);
        stage.show();

        final double baseW = 1920.0;
        final double baseH = 1080.0;
        final double ratio = baseW / baseH;

        Rectangle2D vb = Screen.getPrimary().getVisualBounds();

        double targetW = vb.getWidth();
        double targetH = targetW / ratio;
        if (targetH > vb.getHeight()) {
            targetH = vb.getHeight();
            targetW = targetH * ratio;
        }

        stage.setWidth(targetW);
        stage.setHeight(targetH);
        stage.setX(vb.getMinX() + (vb.getWidth() - targetW) / 2.0);
        stage.setY(vb.getMinY() + (vb.getHeight() - targetH) / 2.0);

        Runnable rescale = () -> {
            double w = stage.getWidth();
            double h = stage.getHeight();
            if (w <= 0 || h <= 0) return;

            double scale = Math.min(w / baseW, h / baseH);
            content.setScaleX(scale);
            content.setScaleY(scale);
        };

        final boolean[] adjusting = {false};

        stage.widthProperty().addListener((obs, oldW, newW) -> {
            if (adjusting[0]) return;
            adjusting[0] = true;

            stage.setHeight(newW.doubleValue() / ratio);

            adjusting[0] = false;
            rescale.run();
        });

        stage.heightProperty().addListener((obs, oldH, newH) -> rescale.run());

        Platform.runLater(rescale);
    }

    
    // When click "Rules and Datasheets" button
    @FXML
    void ruleBtn(MouseEvent event) throws IOException {
        FixedAspectView.switchTo((Node) event.getSource(),
                "/eecs2311/group2/wh40k_easycombat/RulesUI.fxml",
                1200.0, 800.0);
    }

    // When click "Army" button, go to army page
    @FXML
    void armyBtn(MouseEvent event) throws IOException {
        FixedAspectView.switchTo((Node) event.getSource(),
                "/eecs2311/group2/wh40k_easycombat/Army.fxml",
                1200.0, 800.0);
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
