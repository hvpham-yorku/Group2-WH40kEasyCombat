package eecs2311.group2.wh40k_easycombat.controller;

import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;

public class LoadingController {
    @FXML private Label statusLabel;
    @FXML private ProgressBar progressBar;

    public void bind(Task<?> task) {
        statusLabel.textProperty().bind(task.messageProperty());
        progressBar.progressProperty().bind(task.progressProperty());
    }

    public void showFailure(String message) {
        statusLabel.textProperty().unbind();
        progressBar.progressProperty().unbind();
        statusLabel.setText(message);
        progressBar.setProgress(1.0);
    }
}
