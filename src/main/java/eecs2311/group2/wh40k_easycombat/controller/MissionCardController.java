package eecs2311.group2.wh40k_easycombat.controller;

import eecs2311.group2.wh40k_easycombat.controller.helper.DialogHelper;
import eecs2311.group2.wh40k_easycombat.controller.helper.MissionCardRenderHelper;
import eecs2311.group2.wh40k_easycombat.model.mission.MissionCard;
import eecs2311.group2.wh40k_easycombat.model.mission.MissionDecision;
import eecs2311.group2.wh40k_easycombat.model.mission.MissionResolution;
import eecs2311.group2.wh40k_easycombat.model.mission.MissionType;
import eecs2311.group2.wh40k_easycombat.model.instance.Player;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.util.List;
import java.util.Locale;

public class MissionCardController {
    // ======================= Header and Body =======================
    @FXML private VBox headerBox;
    @FXML private Label missionTypeLabel;
    @FXML private Label missionTitleLabel;
    @FXML private Label missionIntroLabel;
    @FXML private Label missionContextLabel;
    @FXML private VBox missionBodyBox;

    // ======================= Mission Resolution Inputs =======================
    @FXML private ComboBox<Player> awardedPlayerComboBox;
    @FXML private ComboBox<Integer> vpPresetComboBox;
    @FXML private TextField customVpField;

    // ======================= Buttons =======================
    @FXML private Button completeButton;
    @FXML private Button activeButton;
    @FXML private Button closeButton;

    private MissionResolution resolution = MissionResolution.closed();

    // When this page loads, initialize the awarded player selector.
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

        MissionCardRenderHelper.applyHeaderStyle(headerBox, card.type());

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

        MissionCardRenderHelper.renderBody(missionBodyBox, card);
    }

    public MissionResolution getResolution() {
        return resolution;
    }

    // When click "Complete" button, record the selected VP award and mark the mission as completed.
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

    // When click "Keep Active" or "Not Completed" button, close the mission card without awarding VP.
    @FXML
    private void markNotCompleted() {
        resolution = new MissionResolution(
                MissionDecision.NOT_COMPLETED,
                awardedPlayerComboBox.getValue(),
                0
        );
        closeWindow();
    }

    // When click "Close" button, close the mission card without changing the mission state.
    @FXML
    private void closeOnly() {
        resolution = MissionResolution.closed();
        closeWindow();
    }

    private void closeWindow() {
        Stage stage = (Stage) closeButton.getScene().getWindow();
        stage.close();
    }

    private String playerLabel(Player player) {
        return player == Player.DEFENDER ? "Defender" : "Attacker";
    }
}
