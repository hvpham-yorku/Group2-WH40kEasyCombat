package eecs2311.group2.wh40k_easycombat.controller.helper;

import eecs2311.group2.wh40k_easycombat.controller.AutoBattleController;
import eecs2311.group2.wh40k_easycombat.controller.BattleLogController;
import eecs2311.group2.wh40k_easycombat.controller.BattleShockController;
import eecs2311.group2.wh40k_easycombat.controller.EditorStratagemTargetController;
import eecs2311.group2.wh40k_easycombat.controller.MissionCardController;
import eecs2311.group2.wh40k_easycombat.model.editor.EditorRuleDefinition;
import eecs2311.group2.wh40k_easycombat.model.instance.Player;
import eecs2311.group2.wh40k_easycombat.model.instance.UnitInstance;
import eecs2311.group2.wh40k_easycombat.model.mission.MissionCard;
import eecs2311.group2.wh40k_easycombat.model.mission.MissionResolution;
import eecs2311.group2.wh40k_easycombat.service.autobattle.AutoBattleMode;
import eecs2311.group2.wh40k_easycombat.service.game.GameEngine;
import eecs2311.group2.wh40k_easycombat.viewmodel.GameArmyUnitVM;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.util.List;

public final class GameUIWindowHelper {

    private GameUIWindowHelper() {
    }

    public static void openBattleLogWindow(Node owner) {
        try {
            FXMLLoader loader = new FXMLLoader(
                    GameUIWindowHelper.class.getResource("/eecs2311/group2/wh40k_easycombat/BattleLog.fxml")
            );
            Parent root = loader.load();
            BattleLogController controller = loader.getController();
            controller.setContext("Battle Log");

            Stage stage = new Stage();
            stage.initOwner(owner.getScene().getWindow());
            stage.initModality(Modality.WINDOW_MODAL);
            stage.setTitle("Battle Log");
            stage.setScene(new Scene(root));
            stage.setMinWidth(900.0);
            stage.setMinHeight(620.0);
            stage.showAndWait();
        } catch (Exception e) {
            DialogHelper.showError("Open Battle Log Error", e);
        }
    }

    public static void openBattleShockWindow(
            Node owner,
            String factionName,
            int round,
            List<UnitInstance> candidates,
            Runnable onCloseRefresh
    ) {
        try {
            FXMLLoader loader = new FXMLLoader(
                    GameUIWindowHelper.class.getResource("/eecs2311/group2/wh40k_easycombat/BattleShock.fxml")
            );
            Parent root = loader.load();

            BattleShockController controller = loader.getController();
            controller.setContext(factionName, round, candidates, onCloseRefresh);

            Stage stage = new Stage();
            stage.initOwner(owner.getScene().getWindow());
            stage.initModality(Modality.WINDOW_MODAL);
            stage.setTitle("Battle-shock Step");
            stage.setScene(new Scene(root));
            stage.showAndWait();

            onCloseRefresh.run();
        } catch (Exception e) {
            DialogHelper.showError("Open Battle-shock Step Error", e);
        }
    }

    public static void openAutoBattleWindow(
            Node owner,
            AutoBattleMode mode,
            GameEngine gameEngine,
            String attackerArmyName,
            List<GameArmyUnitVM> attackerUnits,
            String defenderArmyName,
            List<GameArmyUnitVM> defenderUnits,
            Runnable onCloseRefresh
    ) {
        try {
            FXMLLoader loader = new FXMLLoader(
                    GameUIWindowHelper.class.getResource("/eecs2311/group2/wh40k_easycombat/AutoBattle.fxml")
            );
            Parent root = loader.load();

            AutoBattleController controller = loader.getController();
            controller.setBattleContext(
                    mode,
                    gameEngine.getCurrentRound(),
                    gameEngine.getCurrentPhase(),
                    gameEngine.getActivePlayer(),
                    attackerArmyName,
                    attackerUnits,
                    defenderArmyName,
                    defenderUnits
            );

            Stage stage = new Stage();
            stage.initOwner(owner.getScene().getWindow());
            stage.initModality(Modality.WINDOW_MODAL);
            stage.setTitle("Auto Battle");
            stage.setScene(new Scene(root));
            stage.setMinWidth(1100.0);
            stage.setMinHeight(680.0);
            stage.showAndWait();

            onCloseRefresh.run();
        } catch (Exception e) {
            DialogHelper.showError("Open Auto Battle Error", e);
        }
    }

    public static GameArmyUnitVM openStratagemTargetWindow(
            Node owner,
            String sideLabel,
            String stratagemName,
            List<EditorRuleDefinition> matchingRules,
            List<GameArmyUnitVM> candidates
    ) {
        try {
            FXMLLoader loader = new FXMLLoader(
                    GameUIWindowHelper.class.getResource("/eecs2311/group2/wh40k_easycombat/EditorStratagemTarget.fxml")
            );
            Parent root = loader.load();

            EditorStratagemTargetController controller = loader.getController();
            controller.setContext(sideLabel, stratagemName, matchingRules, candidates);

            Stage stage = new Stage();
            stage.initOwner(owner.getScene().getWindow());
            stage.initModality(Modality.WINDOW_MODAL);
            stage.setTitle("Choose Stratagem Target");
            stage.setScene(new Scene(root));
            stage.setMinWidth(760.0);
            stage.setMinHeight(520.0);
            stage.showAndWait();

            return controller.getSelectedUnit();
        } catch (Exception e) {
            DialogHelper.showError("Open Stratagem Target Error", e);
            return null;
        }
    }

    public static MissionResolution openMissionCardWindow(
            Node owner,
            String contextLabel,
            MissionCard missionCard,
            Player defaultAwardedPlayer,
            boolean allowPlayerSelection,
            String keepButtonText
    ) {
        try {
            FXMLLoader loader = new FXMLLoader(
                    GameUIWindowHelper.class.getResource("/eecs2311/group2/wh40k_easycombat/MissionCard.fxml")
            );
            Parent root = loader.load();

            MissionCardController controller = loader.getController();
            controller.setContext(
                    contextLabel,
                    missionCard,
                    defaultAwardedPlayer,
                    allowPlayerSelection,
                    keepButtonText
            );

            Stage stage = new Stage();
            stage.initOwner(owner.getScene().getWindow());
            stage.initModality(Modality.WINDOW_MODAL);
            stage.setTitle(missionCard == null ? "Mission Card" : missionCard.title());
            stage.setScene(new Scene(root, 760.0, 700.0));
            stage.setMinWidth(700.0);
            stage.setMinHeight(620.0);
            stage.showAndWait();

            return controller.getResolution();
        } catch (Exception e) {
            DialogHelper.showError("Open Mission Card Error", e);
            return MissionResolution.closed();
        }
    }
}
