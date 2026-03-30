package eecs2311.group2.wh40k_easycombat.controller;

import eecs2311.group2.wh40k_easycombat.controller.helper.DialogHelper;
import eecs2311.group2.wh40k_easycombat.model.instance.GameSetupConfig;
import eecs2311.group2.wh40k_easycombat.model.mission.MissionCard;
import eecs2311.group2.wh40k_easycombat.model.mission.SecondaryMissionMode;
import eecs2311.group2.wh40k_easycombat.service.editor.RuleEditorService;
import eecs2311.group2.wh40k_easycombat.service.game.GameSetupService;
import eecs2311.group2.wh40k_easycombat.service.mission.MissionService;
import eecs2311.group2.wh40k_easycombat.util.FixedAspectView;
import eecs2311.group2.wh40k_easycombat.viewmodel.GameArmyImportVM;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory;
import javafx.scene.control.TextArea;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.List;

public class GameSetupController {
    // ======================= Attacker Army =======================
    @FXML private Label blueArmyLabel;
    @FXML private Label blueArmyPointsLabel;
    @FXML private Button blueImportButton;

    // ======================= Defender Army =======================
    @FXML private Label redArmyLabel;
    @FXML private Label redArmyPointsLabel;
    @FXML private Button redImportButton;

    // ======================= Battle Setup =======================
    @FXML private ComboBox<BattleSizeOption> battleSizeComboBox;
    @FXML private ComboBox<String> primaryMissionComboBox;
    @FXML private Label primaryPreviewTitleLabel;
    @FXML private Label primaryPreviewIntroLabel;
    @FXML private TextArea primaryPreviewBodyTextArea;
    @FXML private Spinner<Integer> roundSpinner;
    @FXML private CheckBox customRulesCheckBox;

    // ======================= Secondary Missions =======================
    @FXML private ComboBox<SecondaryMissionMode> blueMissionModeComboBox;
    @FXML private ComboBox<String> blueMissionOneComboBox;
    @FXML private ComboBox<String> blueMissionTwoComboBox;

    @FXML private ComboBox<SecondaryMissionMode> redMissionModeComboBox;
    @FXML private ComboBox<String> redMissionOneComboBox;
    @FXML private ComboBox<String> redMissionTwoComboBox;

    // ======================= Buttons =======================
    @FXML private Button swapRolesButton;
    @FXML private Button startBattleButton;
    @FXML private Button backButton;

    private final MissionService missionService = MissionService.getInstance();
    private final GameSetupService gameSetupService = GameSetupService.getInstance();
    private final RuleEditorService ruleEditorService = RuleEditorService.getInstance();

    private GameArmyImportVM blueArmy;
    private GameArmyImportVM redArmy;

    // When this page loads, initialize battle size, missions, round limit and current army labels.
    @FXML
    private void initialize() {
        battleSizeComboBox.setItems(FXCollections.observableArrayList(
                new BattleSizeOption("Combat Patrol - 500", 500),
                new BattleSizeOption("Incursion - 1000", 1000),
                new BattleSizeOption("Strike Force - 2000", 2000),
                new BattleSizeOption("Onslaught - 3000", 3000)
        ));
        battleSizeComboBox.getSelectionModel().select(2);

        List<String> secondaryTitles = missionService.getSecondaryMissions().stream()
                .map(MissionCard::title)
                .toList();
        List<String> primaryTitles = missionService.getPrimaryMissions().stream()
                .map(MissionCard::title)
                .toList();

        primaryMissionComboBox.setItems(FXCollections.observableArrayList(primaryTitles));
        if (!primaryTitles.isEmpty()) {
            primaryMissionComboBox.getSelectionModel().selectFirst();
        }
        primaryMissionComboBox.valueProperty().addListener((obs, oldValue, newValue) -> updatePrimaryMissionPreview());

        blueMissionModeComboBox.setItems(FXCollections.observableArrayList(SecondaryMissionMode.values()));
        redMissionModeComboBox.setItems(FXCollections.observableArrayList(SecondaryMissionMode.values()));
        blueMissionModeComboBox.getSelectionModel().select(SecondaryMissionMode.TACTICAL);
        redMissionModeComboBox.getSelectionModel().select(SecondaryMissionMode.TACTICAL);

        blueMissionOneComboBox.setItems(FXCollections.observableArrayList(secondaryTitles));
        blueMissionTwoComboBox.setItems(FXCollections.observableArrayList(secondaryTitles));
        redMissionOneComboBox.setItems(FXCollections.observableArrayList(secondaryTitles));
        redMissionTwoComboBox.setItems(FXCollections.observableArrayList(secondaryTitles));

        blueMissionModeComboBox.valueProperty().addListener((obs, oldValue, newValue) -> updateMissionModeUi());
        redMissionModeComboBox.valueProperty().addListener((obs, oldValue, newValue) -> updateMissionModeUi());

        roundSpinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 10, 5));
        customRulesCheckBox.setSelected(ruleEditorService.isAutoApplyEnabled());

        updateMissionModeUi();
        updatePrimaryMissionPreview();
        refreshArmyLabels();
        gameSetupService.clear();
    }

    // When click "Import Attacker Army" button, open the army import window for the attacker side.
    @FXML
    private void importBlueArmy(ActionEvent event) {
        openImportWindow(army -> {
            blueArmy = army;
            refreshArmyLabels();
        });
    }

    // When click "Import Defender Army" button, open the army import window for the defender side.
    @FXML
    private void importRedArmy(ActionEvent event) {
        openImportWindow(army -> {
            redArmy = army;
            refreshArmyLabels();
        });
    }

    // When click "Swap Roles" button, swap the current attacker and defender armies and mission settings.
    @FXML
    private void swapRoles(ActionEvent event) {
        GameArmyImportVM previousAttacker = blueArmy;
        blueArmy = redArmy;
        redArmy = previousAttacker;

        SecondaryMissionMode attackerMode = blueMissionModeComboBox.getValue();
        SecondaryMissionMode defenderMode = redMissionModeComboBox.getValue();
        blueMissionModeComboBox.setValue(defenderMode);
        redMissionModeComboBox.setValue(attackerMode);

        String attackerMissionOne = blueMissionOneComboBox.getValue();
        String attackerMissionTwo = blueMissionTwoComboBox.getValue();
        blueMissionOneComboBox.setValue(redMissionOneComboBox.getValue());
        blueMissionTwoComboBox.setValue(redMissionTwoComboBox.getValue());
        redMissionOneComboBox.setValue(attackerMissionOne);
        redMissionTwoComboBox.setValue(attackerMissionTwo);

        updateMissionModeUi();
        refreshArmyLabels();
    }

    // When click "Start Battle" button, validate the setup and enter the main game screen.
    @FXML
    private void startBattle(ActionEvent event) throws IOException {
        BattleSizeOption battleSize = battleSizeComboBox.getValue();
        if (battleSize == null) {
            DialogHelper.showWarning("Missing Battle Size", "Please choose a battle size first.");
            return;
        }
        if (blueArmy == null || redArmy == null) {
            DialogHelper.showWarning("Missing Army", "Please import both the Attacker and Defender armies before starting the battle.");
            return;
        }
        if (blueArmy.points() > battleSize.points() || redArmy.points() > battleSize.points()) {
            DialogHelper.showWarning(
                    "Army Too Large",
                    "One or more imported armies are above the selected battle size limit."
            );
            return;
        }

        MissionCard primaryMission = missionService.findPrimaryByTitle(primaryMissionComboBox.getValue());
        if (primaryMission == null) {
            DialogHelper.showWarning("Missing Primary Mission", "Please choose one primary mission.");
            return;
        }

        SecondaryMissionMode blueMode = defaultMode(blueMissionModeComboBox.getValue());
        SecondaryMissionMode redMode = defaultMode(redMissionModeComboBox.getValue());

        List<MissionCard> blueFixed = resolveFixedMissions(
                blueMode,
                blueMissionOneComboBox.getValue(),
                blueMissionTwoComboBox.getValue(),
                "Attacker"
        );
        if (blueFixed == null) {
            return;
        }

        List<MissionCard> redFixed = resolveFixedMissions(
                redMode,
                redMissionOneComboBox.getValue(),
                redMissionTwoComboBox.getValue(),
                "Defender"
        );
        if (redFixed == null) {
            return;
        }

        ruleEditorService.setAutoApplyEnabled(customRulesCheckBox.isSelected());
        gameSetupService.setCurrentConfig(new GameSetupConfig(
                blueArmy,
                redArmy,
                battleSize.points(),
                battleSize.label(),
                primaryMission,
                blueMode,
                redMode,
                blueFixed,
                redFixed,
                roundSpinner.getValue(),
                customRulesCheckBox.isSelected()
        ));

        FixedAspectView.switchResponsiveTo(
                (Node) event.getSource(),
                "/eecs2311/group2/wh40k_easycombat/GameUI.fxml",
                1024.0,
                680.0,
                1500.0,
                900.0
        );
    }

    // When click "Back" button, return to the main menu page.
    @FXML
    private void back(ActionEvent event) throws IOException {
        FixedAspectView.switchToMainMenu((Node) event.getSource());
    }

    private void openImportWindow(java.util.function.Consumer<GameArmyImportVM> consumer) {
        BattleSizeOption battleSize = battleSizeComboBox.getValue();
        if (battleSize == null) {
            DialogHelper.showWarning("Missing Battle Size", "Choose a battle size before importing an army.");
            return;
        }

        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/eecs2311/group2/wh40k_easycombat/ArmyImport.fxml")
            );
            Parent root = loader.load();

            ArmyImportController controller = loader.getController();
            controller.setImportContext(consumer, battleSize.points());

            Stage stage = new Stage();
            stage.initOwner(blueImportButton.getScene().getWindow());
            stage.initModality(Modality.WINDOW_MODAL);
            stage.setTitle("Import Army");
            stage.setScene(new Scene(root));
            stage.showAndWait();
        } catch (Exception e) {
            DialogHelper.showError("Open Import Page Error", e);
        }
    }

    private void updateMissionModeUi() {
        boolean blueFixed = defaultMode(blueMissionModeComboBox.getValue()) == SecondaryMissionMode.FIXED;
        boolean redFixed = defaultMode(redMissionModeComboBox.getValue()) == SecondaryMissionMode.FIXED;

        blueMissionOneComboBox.setDisable(!blueFixed);
        blueMissionTwoComboBox.setDisable(!blueFixed);
        redMissionOneComboBox.setDisable(!redFixed);
        redMissionTwoComboBox.setDisable(!redFixed);
    }

    private void refreshArmyLabels() {
        if (blueArmy == null) {
            blueArmyLabel.setText("No Attacker army imported");
            blueArmyPointsLabel.setText("0 pts");
        } else {
            blueArmyLabel.setText(blueArmy.armyName() + " (" + blueArmy.factionName() + ")");
            blueArmyPointsLabel.setText(blueArmy.points() + " pts");
        }

        if (redArmy == null) {
            redArmyLabel.setText("No Defender army imported");
            redArmyPointsLabel.setText("0 pts");
        } else {
            redArmyLabel.setText(redArmy.armyName() + " (" + redArmy.factionName() + ")");
            redArmyPointsLabel.setText(redArmy.points() + " pts");
        }
    }

    private void updatePrimaryMissionPreview() {
        MissionCard selected = missionService.findPrimaryByTitle(primaryMissionComboBox.getValue());
        if (selected == null) {
            primaryPreviewTitleLabel.setText("No Primary Mission Selected");
            primaryPreviewIntroLabel.setText("");
            primaryPreviewBodyTextArea.setText("");
            return;
        }

        primaryPreviewTitleLabel.setText(selected.title());
        primaryPreviewIntroLabel.setText(selected.intro());
        primaryPreviewBodyTextArea.setText(String.join("\n\n", selected.bodyLines()));
    }

    private List<MissionCard> resolveFixedMissions(
            SecondaryMissionMode mode,
            String firstTitle,
            String secondTitle,
            String sideLabel
    ) {
        if (mode != SecondaryMissionMode.FIXED) {
            return List.of();
        }

        if (firstTitle == null || firstTitle.isBlank() || secondTitle == null || secondTitle.isBlank()) {
            DialogHelper.showWarning(
                    "Missing Fixed Missions",
                    sideLabel + " must choose two fixed secondary missions."
            );
            return null;
        }

        if (firstTitle.equalsIgnoreCase(secondTitle)) {
            DialogHelper.showWarning(
                    "Duplicate Fixed Missions",
                    sideLabel + " must choose two different fixed secondary missions."
            );
            return null;
        }

        MissionCard first = missionService.findSecondaryByTitle(firstTitle);
        MissionCard second = missionService.findSecondaryByTitle(secondTitle);
        if (first == null || second == null) {
            DialogHelper.showWarning(
                    "Invalid Fixed Missions",
                    sideLabel + " selected one or more invalid secondary missions."
            );
            return null;
        }

        return List.of(first, second);
    }

    private SecondaryMissionMode defaultMode(SecondaryMissionMode mode) {
        return mode == null ? SecondaryMissionMode.TACTICAL : mode;
    }

    public record BattleSizeOption(String label, int points) {
        @Override
        public String toString() {
            return label;
        }
    }
}
