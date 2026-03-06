package eecs2311.group2.wh40k_easycombat.controller;

import eecs2311.group2.wh40k_easycombat.cell.GameArmyUnitCell;
import eecs2311.group2.wh40k_easycombat.cell.GameStrategyCell;
import eecs2311.group2.wh40k_easycombat.service.GameArmyImportService.ImportedArmyData;
import eecs2311.group2.wh40k_easycombat.service.GameStrategyImportService;
import eecs2311.group2.wh40k_easycombat.viewmodel.GameArmyUnitVM;
import eecs2311.group2.wh40k_easycombat.viewmodel.GameStrategyVM;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import javafx.stage.Modality;
import javafx.stage.Stage;

public class GameUIController {

    public enum ArmySide {
        BLUE, RED
    }

    // ======================= CheckBoxes =======================
    @FXML private CheckBox autoBattleCheckBox;

    // ======================= Blue Side ========================
    @FXML private Button blueAbandonMissionButton;
    @FXML private ListView<GameArmyUnitVM> blueArmyList;
    @FXML private Label blueCPLabel;
    @FXML private Button blueCheckMissionButton;
    @FXML private Label blueFactionLabel;
    @FXML private Button blueImportButton;
    @FXML private TableView<?> blueMissionTable;
    @FXML private Label bluePhaseLabel;
    @FXML private Button bluePlusButton;
    @FXML private Button blueSelectButton;
    @FXML private ListView<GameStrategyVM> blueStrategyList;
    @FXML private Button blueSubButton;
    @FXML private Label blueVPLabel;

    // ======================= Red Side =========================
    @FXML private Button redAbandonMissionButton;
    @FXML private ListView<GameArmyUnitVM> redArmyList;
    @FXML private Label redCPLabel;
    @FXML private Button redCheckMissionButton;
    @FXML private Label redFactionLabel;
    @FXML private Button redImportButton;
    @FXML private TableView<?> redMissionTable;
    @FXML private Label redPhaseLabel;
    @FXML private Button redPlusButton;
    @FXML private Button redSelectButton;
    @FXML private ListView<GameStrategyVM> redStrategyList;
    @FXML private Button redSubButton;
    @FXML private Label redVPLabel;

    // ======================= Game Control =====================
    @FXML private Button battleLogButton;
    @FXML private Button exitGameButton;
    @FXML private Button nextRoundButton;
    @FXML private Button rollButton;

    // ======================= Game Info ========================
    @FXML private Label missionNameLabel;
    @FXML private Label roundLabel;

    // ======================= Dice =============================
    @FXML private TextArea virtuaDiceBox;
    @FXML private Spinner<?> virtuaDiceSpinner;

    private final ObservableList<GameArmyUnitVM> blueArmyUnits = FXCollections.observableArrayList();
    private final ObservableList<GameArmyUnitVM> redArmyUnits = FXCollections.observableArrayList();

    private final ObservableList<GameStrategyVM> blueStrategies = FXCollections.observableArrayList();
    private final ObservableList<GameStrategyVM> redStrategies = FXCollections.observableArrayList();

    @FXML
    private void initialize() {
        setupArmyLists();
        setupStrategyLists();
    }

    private void setupArmyLists() {
        blueArmyList.setItems(blueArmyUnits);
        redArmyList.setItems(redArmyUnits);

        blueArmyList.setCellFactory(v -> new GameArmyUnitCell());
        redArmyList.setCellFactory(v -> new GameArmyUnitCell());
    }

    private void setupStrategyLists() {
        blueStrategyList.setItems(blueStrategies);
        redStrategyList.setItems(redStrategies);

        blueStrategyList.setCellFactory(v -> new GameStrategyCell());
        redStrategyList.setCellFactory(v -> new GameStrategyCell());
    }

    public void acceptImportedArmy(ArmySide side, ImportedArmyData data) {
        if (data == null) return;

        try {
            ObservableList<GameStrategyVM> importedStrategies =
                    FXCollections.observableArrayList(
                            GameStrategyImportService.importStrategiesForArmy(data.armyId())
                    );

            if (side == ArmySide.BLUE) {
                blueArmyUnits.setAll(data.units());
                blueFactionLabel.setText(data.factionName());
                blueStrategies.setAll(importedStrategies);
            } else {
                redArmyUnits.setAll(data.units());
                redFactionLabel.setText(data.factionName());
                redStrategies.setAll(importedStrategies);
            }
        } catch (Exception e) {
            Alert a = new Alert(Alert.AlertType.ERROR, e.getMessage(), ButtonType.OK);
            a.setHeaderText("Import Strategy Error");
            a.showAndWait();
        }
    }
    
    private void openImportWindow(ArmySide side, Button sourceButton) {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/eecs2311/group2/wh40k_easycombat/ArmyImport.fxml")
            );
            Parent root = loader.load();

            ArmyImportController controller = loader.getController();
            controller.setImportContext(this, side);

            Stage stage = new Stage();
            stage.initOwner(sourceButton.getScene().getWindow());
            stage.initModality(Modality.WINDOW_MODAL);
            stage.setTitle("Import Army");
            stage.setScene(new javafx.scene.Scene(root));
            stage.showAndWait();
        } catch (Exception e) {
            Alert a = new Alert(Alert.AlertType.ERROR, e.getMessage(), ButtonType.OK);
            a.setHeaderText("Open Import Page Error");
            a.showAndWait();
        }
    }

    // ======================= Blue Actions =====================
    @FXML void blueAbandonClicked(MouseEvent event) { }

    @FXML void blueCheckClicked(MouseEvent event) { }

    @FXML void blueClickPlus(MouseEvent event) { }

    @FXML void blueClickSub(MouseEvent event) { }

    @FXML
    void blueImport(MouseEvent event) {
        openImportWindow(ArmySide.BLUE, blueImportButton);
    }

    @FXML void blueSelect(MouseEvent event) { 
    	useSelectedStrategy(ArmySide.BLUE);
    }

    // ======================= Red Actions ======================
    @FXML void redAbandonClicked(MouseEvent event) { }

    @FXML void redCheckClicked(MouseEvent event) { }

    @FXML void redClickPlus(MouseEvent event) { }

    @FXML void redClickSub(MouseEvent event) { }

    @FXML
    void redImport(MouseEvent event) {
        openImportWindow(ArmySide.RED, redImportButton);
    }

    @FXML void redSelect(MouseEvent event) { 
    	useSelectedStrategy(ArmySide.RED);
    }

    // ======================= General Actions ==================
    @FXML void clickExit(MouseEvent event) { }

    @FXML void nextRound(MouseEvent event) { }

    @FXML void openLog(MouseEvent event) { }

    @FXML void rollDice(MouseEvent event) { }
    
 // ======================= Helper ==================
    private void useSelectedStrategy(ArmySide side) {
        GameStrategyVM selected;

        if (side == ArmySide.BLUE) {
            selected = blueStrategyList.getSelectionModel().getSelectedItem();
        } else {
            selected = redStrategyList.getSelectionModel().getSelectedItem();
        }

        if (selected == null) {
            showWarning("No Stratagem Selected", "Please select one stratagem first.");
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

        Alert result = new Alert(Alert.AlertType.INFORMATION, content, ButtonType.OK);
        result.setHeaderText(selected.getName());
        result.setTitle("Stratagem Used");
        result.showAndWait();

        appendToBattleBox(content);
    }

    private String buildStrategyUseText(ArmySide side, GameStrategyVM s) {
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

    private void appendToBattleBox(String text) {
        if (virtuaDiceBox == null) return;

        String old = virtuaDiceBox.getText();
        if (old == null || old.isBlank()) {
            virtuaDiceBox.setText(text);
        } else {
            virtuaDiceBox.appendText("\n\n" + text);
        }
    }

    private String htmlToPlainText(String html) {
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

    private void showWarning(String title, String text) {
        Alert a = new Alert(Alert.AlertType.WARNING, text, ButtonType.OK);
        a.setHeaderText(title);
        a.showAndWait();
    }
}