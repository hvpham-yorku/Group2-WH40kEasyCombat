package eecs2311.group2.wh40k_easycombat.manager;

import eecs2311.group2.wh40k_easycombat.controller.GameUIController.ArmySide;
import eecs2311.group2.wh40k_easycombat.viewmodel.GameStrategyVM;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;

public final class StratagemUseManager {

    private StratagemUseManager() {
    }

    public static void useSelectedStrategy(
            ArmySide side,
            ListView<GameStrategyVM> blueStrategyList,
            ListView<GameStrategyVM> redStrategyList,
            Label blueCPLabel,
            Label redCPLabel
    ) {
        GameStrategyVM selected = getSelectedStrategy(side, blueStrategyList, redStrategyList);

        if (selected == null) {
            showWarning("No Stratagem Selected", "Please select one stratagem first.");
            return;
        }

        Label cpLabel = (side == ArmySide.BLUE) ? blueCPLabel : redCPLabel;

        if (!CommandPointManager.hasEnoughCp(cpLabel.getText(), selected.getCpCost())) {
            showWarning("Not Enough CP", "You do not have enough CP to use this stratagem.");
            return;
        }

        Alert confirm = new Alert(
                Alert.AlertType.CONFIRMATION,
                "Use stratagem \"" + selected.getName() + "\"?",
                ButtonType.YES,
                ButtonType.NO
        );
        confirm.setHeaderText("Confirm Stratagem");

        if (confirm.showAndWait().orElse(ButtonType.NO) != ButtonType.YES) {
            return;
        }

        String content = buildStrategyUseText(side, selected);

        cpLabel.setText(CommandPointManager.spendCp(cpLabel.getText(), selected.getCpCost()));

        Alert result = new Alert(Alert.AlertType.INFORMATION, content, ButtonType.OK);
        result.setHeaderText(selected.getName());
        result.setTitle("Stratagem Used");
        result.showAndWait();
    }

    private static GameStrategyVM getSelectedStrategy(
            ArmySide side,
            ListView<GameStrategyVM> blueStrategyList,
            ListView<GameStrategyVM> redStrategyList
    ) {
        if (side == ArmySide.BLUE) {
            return blueStrategyList.getSelectionModel().getSelectedItem();
        }
        return redStrategyList.getSelectionModel().getSelectedItem();
    }

    private static String buildStrategyUseText(ArmySide side, GameStrategyVM s) {
        String sideName = side == ArmySide.BLUE ? "Blue" : "Red";

        StringBuilder sb = new StringBuilder();

        sb.append(sideName)
          .append(" used ")
          .append(s.getName());

        if (s.getCpCost() != null && !s.getCpCost().isBlank()) {
            sb.append(" (").append(s.getCpCost()).append(" CP)");
        }

        sb.append("\n");

        if (s.getTurn() != null && !s.getTurn().isBlank()) {
            sb.append("Turn: ").append(s.getTurn()).append("\n");
        }

        if (s.getPhase() != null && !s.getPhase().isBlank()) {
            sb.append("Phase: ").append(s.getPhase()).append("\n");
        }

        String description = htmlToPlainText(s.getDescriptionHtml());
        if (!description.isBlank()) {
            sb.append("\n").append(description);
        }

        return sb.toString().trim();
    }

    private static String htmlToPlainText(String html) {
        if (html == null || html.isBlank()) return "";

        String s = html;
        s = s.replace("<br><br>", "\n\n");
        s = s.replace("<br/>", "\n");
        s = s.replace("<br />", "\n");
        s = s.replace("<br>", "\n");

        s = s.replaceAll("(?i)</b>", "");
        s = s.replaceAll("(?i)<b>", "");

        s = s.replace("&nbsp;", " ");
        s = s.replace("&lt;", "<");
        s = s.replace("&gt;", ">");
        s = s.replace("&amp;", "&");
        s = s.replace("&quot;", "\"");
        s = s.replace("&#39;", "'");

        s = s.replaceAll("(?is)<[^>]+>", "");
        return s.trim();
    }

    private static void showWarning(String title, String text) {
        Alert a = new Alert(Alert.AlertType.WARNING, text, ButtonType.OK);
        a.setHeaderText(title);
        a.showAndWait();
    }
}