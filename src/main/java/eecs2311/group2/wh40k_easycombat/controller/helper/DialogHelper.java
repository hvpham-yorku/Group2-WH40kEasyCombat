package eecs2311.group2.wh40k_easycombat.controller.helper;

import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;

public final class DialogHelper {

    private DialogHelper() {
    }

    public static void showWarning(String title, String text) {
        Alert alert = new Alert(Alert.AlertType.WARNING, text, ButtonType.OK);
        alert.setHeaderText(title);
        alert.showAndWait();
    }

    public static void showInfo(String title, String text) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION, text, ButtonType.OK);
        alert.setHeaderText(title);
        alert.showAndWait();
    }

    public static void showError(String title, String text) {
        Alert alert = new Alert(
                Alert.AlertType.ERROR,
                text == null || text.isBlank() ? "Unknown error." : text,
                ButtonType.OK
        );
        alert.setHeaderText(title);
        alert.showAndWait();
    }

    public static void showError(String title, Exception e) {
        if (e != null) {
            e.printStackTrace();
        }
        showError(title, e == null ? null : e.getMessage());
    }

    public static boolean confirmYesNo(String header, String text) {
        Alert alert = new Alert(
                Alert.AlertType.CONFIRMATION,
                text,
                ButtonType.YES,
                ButtonType.NO
        );
        alert.setTitle("Confirm");
        alert.setHeaderText(header);
        return alert.showAndWait().orElse(ButtonType.NO) == ButtonType.YES;
    }

    public static boolean confirmOkCancel(String title, String header, String text) {
        Alert alert = new Alert(
                Alert.AlertType.CONFIRMATION,
                text,
                ButtonType.OK,
                ButtonType.CANCEL
        );
        alert.setTitle(title);
        alert.setHeaderText(header);
        return alert.showAndWait().orElse(ButtonType.CANCEL) == ButtonType.OK;
    }
}