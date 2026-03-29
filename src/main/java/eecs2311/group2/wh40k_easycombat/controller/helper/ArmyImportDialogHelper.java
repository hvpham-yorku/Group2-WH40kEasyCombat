package eecs2311.group2.wh40k_easycombat.controller.helper;

import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.DialogPane;
import javafx.scene.control.TextArea;

public final class ArmyImportDialogHelper {

    private ArmyImportDialogHelper() {
    }

    public static String openWh40kAppImportDialog() {
        Dialog<String> dialog = new Dialog<>();
        dialog.setTitle("Import From WH40K App");
        dialog.setHeaderText("Paste one official WH40K App army export below.");

        DialogPane pane = dialog.getDialogPane();
        pane.getStylesheets().add(
                ArmyImportDialogHelper.class
                        .getResource("/eecs2311/group2/wh40k_easycombat/application.css")
                        .toExternalForm()
        );
        pane.getStyleClass().add("custom-alert");

        ButtonType importButtonType = new ButtonType("Import", ButtonBar.ButtonData.OK_DONE);
        pane.getButtonTypes().addAll(importButtonType, ButtonType.CANCEL);

        TextArea inputArea = new TextArea();
        inputArea.setWrapText(true);
        inputArea.setPrefRowCount(24);
        inputArea.setPrefColumnCount(64);
        inputArea.setPromptText("Paste the full WH40K App export here...");
        inputArea.getStyleClass().add("game-textarea");

        pane.setPrefSize(880, 680);
        pane.setContent(inputArea);

        dialog.setResultConverter(buttonType ->
                buttonType == importButtonType ? inputArea.getText() : null
        );

        return dialog.showAndWait().orElse(null);
    }
}
