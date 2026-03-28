package eecs2311.group2.wh40k_easycombat.controller;

import eecs2311.group2.wh40k_easycombat.controller.helper.DialogHelper;
import eecs2311.group2.wh40k_easycombat.model.mission.MissionCard;
import eecs2311.group2.wh40k_easycombat.model.mission.MissionDecision;
import eecs2311.group2.wh40k_easycombat.model.mission.MissionResolution;
import eecs2311.group2.wh40k_easycombat.model.mission.MissionType;
import eecs2311.group2.wh40k_easycombat.model.instance.Player;
import javafx.fxml.FXML;
import javafx.geometry.HPos;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import javafx.stage.Stage;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class MissionCardController {
    @FXML private VBox headerBox;
    @FXML private Label missionTypeLabel;
    @FXML private Label missionTitleLabel;
    @FXML private Label missionIntroLabel;
    @FXML private Label missionContextLabel;
    @FXML private VBox missionBodyBox;
    @FXML private ComboBox<Player> awardedPlayerComboBox;
    @FXML private ComboBox<Integer> vpPresetComboBox;
    @FXML private TextField customVpField;
    @FXML private Button completeButton;
    @FXML private Button activeButton;
    @FXML private Button closeButton;

    private MissionResolution resolution = MissionResolution.closed();

    @FXML
    private void initialize() {
        awardedPlayerComboBox.getItems().setAll(Player.ATTACKER, Player.DEFENDER);
        awardedPlayerComboBox.setCellFactory(list -> new javafx.scene.control.ListCell<>() {
            @Override
            protected void updateItem(Player item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : playerLabel(item));
            }
        });
        awardedPlayerComboBox.setButtonCell(new javafx.scene.control.ListCell<>() {
            @Override
            protected void updateItem(Player item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : playerLabel(item));
            }
        });
    }

    public void setContext(
            String contextLabel,
            MissionCard missionCard,
            Player defaultAwardedPlayer,
            boolean allowPlayerSelection,
            String keepButtonText
    ) {
        MissionCard card = missionCard == null
                ? new MissionCard(MissionType.SECONDARY, "", "", List.of())
                : missionCard;

        missionTypeLabel.setText(card.type().label().toUpperCase(Locale.ROOT));
        missionTitleLabel.setText(card.title());
        missionIntroLabel.setText(card.intro());
        missionContextLabel.setText(contextLabel == null ? "" : contextLabel.trim());

        headerBox.getStyleClass().removeAll("mission-primary-header", "mission-secondary-header");
        headerBox.getStyleClass().add(card.type() == MissionType.PRIMARY
                ? "mission-primary-header"
                : "mission-secondary-header");

        if (card.type() == MissionType.PRIMARY) {
            vpPresetComboBox.getItems().setAll(5, 10, 15);
            vpPresetComboBox.getSelectionModel().select(Integer.valueOf(5));
        } else {
            vpPresetComboBox.getItems().setAll(2, 3, 4, 5, 8, 10, 12, 15);
            vpPresetComboBox.getSelectionModel().select(Integer.valueOf(2));
        }

        awardedPlayerComboBox.getSelectionModel().select(defaultAwardedPlayer == null ? Player.ATTACKER : defaultAwardedPlayer);
        awardedPlayerComboBox.setDisable(!allowPlayerSelection);
        activeButton.setText(keepButtonText == null || keepButtonText.isBlank() ? "Keep Active" : keepButtonText);

        renderBody(card);
    }

    public MissionResolution getResolution() {
        return resolution;
    }

    @FXML
    private void markCompleted() {
        Integer preset = vpPresetComboBox.getValue();
        int awardedVp = preset == null ? 0 : preset;

        String custom = customVpField.getText() == null ? "" : customVpField.getText().trim();
        if (!custom.isBlank()) {
            try {
                awardedVp = Math.max(0, Integer.parseInt(custom));
            } catch (NumberFormatException ex) {
                DialogHelper.showWarning("Invalid VP", "Please enter a whole number in the custom VP field.");
                return;
            }
        }

        resolution = new MissionResolution(
                MissionDecision.COMPLETED,
                awardedPlayerComboBox.getValue(),
                awardedVp
        );
        closeWindow();
    }

    @FXML
    private void markNotCompleted() {
        resolution = new MissionResolution(
                MissionDecision.NOT_COMPLETED,
                awardedPlayerComboBox.getValue(),
                0
        );
        closeWindow();
    }

    @FXML
    private void closeOnly() {
        resolution = MissionResolution.closed();
        closeWindow();
    }

    private void closeWindow() {
        Stage stage = (Stage) closeButton.getScene().getWindow();
        stage.close();
    }

    private void renderBody(MissionCard card) {
        missionBodyBox.getChildren().clear();
        if (card.type() == MissionType.PRIMARY) {
            renderPrimaryBody(card.bodyLines());
            return;
        }
        renderSecondaryBody(card.bodyLines());
    }

    private void renderPrimaryBody(List<String> lines) {
        for (String line : lines) {
            if (line == null || line.isBlank()) {
                continue;
            }

            if (isPrimaryBanner(line)) {
                missionBodyBox.getChildren().add(buildBanner(line, "mission-primary-banner"));
                continue;
            }

            if (isOrLine(line)) {
                missionBodyBox.getChildren().add(buildCenteredLabel("OR", "mission-or-banner"));
                continue;
            }

            if (looksLikeVictoryLine(line)) {
                missionBodyBox.getChildren().add(buildBulletLabel(line));
                continue;
            }

            missionBodyBox.getChildren().add(buildTaggedParagraph(line));
        }
    }

    private void renderSecondaryBody(List<String> lines) {
        List<String> preamble = new ArrayList<>();
        List<String> structuredLines = new ArrayList<>();
        boolean encounteredHeader = false;

        for (String line : lines) {
            if (isSecondaryHeader(line)) {
                encounteredHeader = true;
            }

            if (encounteredHeader) {
                structuredLines.add(line);
            } else {
                preamble.add(line);
            }
        }

        for (String line : preamble) {
            missionBodyBox.getChildren().add(buildTaggedParagraph(line));
        }

        for (SecondarySection section : parseSecondarySections(structuredLines)) {
            missionBodyBox.getChildren().add(buildSecondarySectionNode(section));
        }
    }

    private Node buildSecondarySectionNode(SecondarySection section) {
        VBox root = new VBox(10);
        root.getStyleClass().add("mission-secondary-section");

        HBox headerRow = new HBox(12);
        Label left = new Label(section.headerLeft());
        left.getStyleClass().add("mission-secondary-header-left");
        left.setMaxWidth(Double.MAX_VALUE);
        HBox.setHgrow(left, Priority.ALWAYS);

        Label right = new Label("VICTORY POINTS");
        right.getStyleClass().add("mission-secondary-header-right");

        headerRow.getChildren().addAll(left, right);
        root.getChildren().add(headerRow);

        if (!section.whenLine().isBlank()) {
            root.getChildren().add(buildTaggedParagraph(section.whenLine()));
        }

        for (SecondaryRow row : section.rows()) {
            if (row.orSeparator()) {
                root.getChildren().add(buildCenteredLabel("OR", "mission-or-banner"));
                continue;
            }

            GridPane grid = new GridPane();
            grid.setHgap(18);
            grid.getStyleClass().add("mission-secondary-row");

            ColumnConstraints leftColumn = new ColumnConstraints();
            leftColumn.setPercentWidth(72);
            leftColumn.setHgrow(Priority.ALWAYS);
            ColumnConstraints rightColumn = new ColumnConstraints();
            rightColumn.setPercentWidth(28);
            rightColumn.setHalignment(HPos.RIGHT);
            grid.getColumnConstraints().addAll(leftColumn, rightColumn);

            Label description = new Label(row.description());
            description.getStyleClass().add("mission-body-text");
            description.setWrapText(true);

            grid.add(description, 0, 0);
            grid.add(buildScoreNode(row.scoreText()), 1, 0);
            root.getChildren().add(grid);
        }

        return root;
    }

    private List<SecondarySection> parseSecondarySections(List<String> lines) {
        List<SecondarySection> result = new ArrayList<>();
        String currentHeader = "";
        List<String> currentLines = new ArrayList<>();

        for (String line : lines) {
            if (isSecondaryHeader(line)) {
                if (!currentHeader.isBlank() || !currentLines.isEmpty()) {
                    result.add(buildSecondarySection(currentHeader, currentLines));
                    currentLines = new ArrayList<>();
                }
                currentHeader = extractSecondaryHeader(line);
                continue;
            }
            currentLines.add(line);
        }

        if (!currentHeader.isBlank() || !currentLines.isEmpty()) {
            result.add(buildSecondarySection(currentHeader, currentLines));
        }

        return result;
    }

    private SecondarySection buildSecondarySection(String header, List<String> lines) {
        String whenLine = "";
        List<SecondaryRow> rows = new ArrayList<>();
        StringBuilder description = new StringBuilder();
        StringBuilder score = new StringBuilder();

        for (String line : lines) {
            if (line == null || line.isBlank()) {
                continue;
            }

            if (line.startsWith("WHEN:")) {
                whenLine = line;
                continue;
            }

            if (isOrLine(line)) {
                flushSecondaryRow(rows, description, score);
                rows.add(new SecondaryRow("", "", true));
                continue;
            }

            if (isScoreFragment(line)) {
                if (!score.isEmpty()) {
                    score.append("\n");
                }
                score.append(line);
                continue;
            }

            if (!score.isEmpty() && !description.isEmpty()) {
                flushSecondaryRow(rows, description, score);
            }

            if (!description.isEmpty()) {
                description.append(" ");
            }
            description.append(line);
        }

        flushSecondaryRow(rows, description, score);
        return new SecondarySection(header.isBlank() ? "MISSION WINDOW" : header, whenLine, rows);
    }

    private void flushSecondaryRow(List<SecondaryRow> rows, StringBuilder description, StringBuilder score) {
        if (description.isEmpty() && score.isEmpty()) {
            return;
        }

        rows.add(new SecondaryRow(description.toString().trim(), score.toString().trim(), false));
        description.setLength(0);
        score.setLength(0);
    }

    private Label buildBanner(String text, String styleClass) {
        Label banner = new Label(text);
        banner.getStyleClass().add(styleClass);
        banner.setMaxWidth(Double.MAX_VALUE);
        return banner;
    }

    private Label buildCenteredLabel(String text, String styleClass) {
        Label label = new Label(text);
        label.getStyleClass().add(styleClass);
        label.setMaxWidth(Double.MAX_VALUE);
        label.setWrapText(true);
        return label;
    }

    private Label buildBulletLabel(String text) {
        Label label = new Label("\u2022 " + text);
        label.getStyleClass().add("mission-body-text");
        label.setWrapText(true);
        return label;
    }

    private Node buildScoreNode(String scoreText) {
        VBox box = new VBox(2);
        box.setAlignment(Pos.CENTER_RIGHT);
        box.getStyleClass().add("mission-score-box");

        String raw = scoreText == null ? "" : scoreText.trim();
        if (raw.isBlank()) {
            Label empty = new Label("");
            empty.getStyleClass().add("mission-score-text");
            box.getChildren().add(empty);
            return box;
        }

        String[] lines = raw.split("\\R+");
        for (String line : lines) {
            String normalized = line == null ? "" : line.trim();
            if (normalized.isBlank()) {
                continue;
            }

            Label label = new Label(normalized);
            label.setWrapText(true);

            String upper = normalized.toUpperCase(Locale.ROOT);
            if (upper.equals("FIXED") || upper.equals("TACTICAL")) {
                label.getStyleClass().add("mission-score-mode");
            } else if (upper.contains("VP") || upper.startsWith("+")) {
                label.getStyleClass().add("mission-score-value");
            } else {
                label.getStyleClass().add("mission-score-note");
            }

            box.getChildren().add(label);
        }

        return box;
    }

    private TextFlow buildTaggedParagraph(String line) {
        TextFlow flow = new TextFlow();
        flow.getStyleClass().add("mission-text-flow");

        int colonIndex = line.indexOf(':');
        if (colonIndex > 0 && colonIndex < line.length() - 1) {
            Text prefix = new Text(line.substring(0, colonIndex + 1) + " ");
            prefix.getStyleClass().add(
                    line.regionMatches(true, 0, "WHEN:", 0, "WHEN:".length())
                            ? "mission-when-tag-text"
                            : "mission-tag-text"
            );
            Text suffix = new Text(line.substring(colonIndex + 1).trim());
            suffix.getStyleClass().add("mission-body-text-node");
            flow.getChildren().addAll(prefix, suffix);
            return flow;
        }

        Text text = new Text(line);
        text.getStyleClass().add("mission-body-text-node");
        flow.getChildren().add(text);
        return flow;
    }

    private boolean isPrimaryBanner(String line) {
        String normalized = line == null ? "" : line.trim();
        return !normalized.isBlank()
                && !normalized.contains(":")
                && !normalized.equalsIgnoreCase("OR")
                && normalized.equals(normalized.toUpperCase(Locale.ROOT));
    }

    private boolean isSecondaryHeader(String line) {
        return line != null && line.toUpperCase(Locale.ROOT).contains("VICTORY POINTS");
    }

    private String extractSecondaryHeader(String line) {
        return line.toUpperCase(Locale.ROOT)
                .replace("VICTORY POINTS", "")
                .trim();
    }

    private boolean looksLikeVictoryLine(String line) {
        return line != null && line.matches("^[+]?\\d+VP.*");
    }

    private boolean isScoreFragment(String line) {
        if (line == null || line.isBlank()) {
            return false;
        }

        String normalized = line.trim().toUpperCase(Locale.ROOT);
        return normalized.equals("FIXED")
                || normalized.equals("TACTICAL")
                || normalized.startsWith("+")
                || normalized.matches("^\\d+VP.*")
                || normalized.startsWith("(");
    }

    private boolean isOrLine(String line) {
        return line != null && line.trim().equalsIgnoreCase("OR");
    }

    private String playerLabel(Player player) {
        return player == Player.DEFENDER ? "Red" : "Blue";
    }

    private record SecondarySection(String headerLeft, String whenLine, List<SecondaryRow> rows) {
    }

    private record SecondaryRow(String description, String scoreText, boolean orSeparator) {
    }
}
