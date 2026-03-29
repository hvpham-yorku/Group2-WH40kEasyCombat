package eecs2311.group2.wh40k_easycombat.controller.helper;

import eecs2311.group2.wh40k_easycombat.model.instance.Player;
import eecs2311.group2.wh40k_easycombat.model.mission.MissionCard;
import eecs2311.group2.wh40k_easycombat.model.mission.MissionResolution;
import eecs2311.group2.wh40k_easycombat.service.BattleLogService;
import eecs2311.group2.wh40k_easycombat.service.game.GameEngine;
import eecs2311.group2.wh40k_easycombat.viewmodel.MissionEntryVM;
import javafx.scene.control.Label;

import java.util.function.BiConsumer;
import java.util.function.Function;

public final class GameUIPrimaryMissionHelper {

    private GameUIPrimaryMissionHelper() {
    }

    public static void openPrimaryMission(
            GameEngine gameEngine,
            BattleLogService battleLogService,
            MissionEntryVM primaryMissionEntry,
            Label primaryMissionStateLabel,
            Function<Player, String> playerLabel,
            PrimaryMissionOpener missionCardOpener,
            BiConsumer<Player, Integer> addVictoryPoints
    ) {
        if (gameEngine.isBattleOver()) {
            DialogHelper.showInfo("Battle Over", gameEngine.winnerText());
            return;
        }

        if (primaryMissionEntry == null || primaryMissionEntry.getMissionCard() == null) {
            DialogHelper.showWarning("No Primary Mission", "No primary mission card is currently loaded.");
            return;
        }

        MissionResolution resolution = missionCardOpener.open(
                "Shared Primary Mission",
                primaryMissionEntry.getMissionCard(),
                Player.ATTACKER,
                true,
                "Keep Active"
        );

        if (resolution.decision().isClosed()) {
            return;
        }

        if (resolution.decision().isCompleted()) {
            addVictoryPoints.accept(resolution.awardedPlayer(), resolution.vpAwarded());
            if (primaryMissionStateLabel != null) {
                primaryMissionStateLabel.setText(
                        "Last Award: "
                                + playerLabel.apply(resolution.awardedPlayer())
                                + " +"
                                + resolution.vpAwarded()
                                + " VP"
                );
            }
            battleLogService.logTurnEvent(
                    gameEngine.getCurrentRound(),
                    gameEngine.getCurrentPhase(),
                    resolution.awardedPlayer(),
                    playerLabel.apply(resolution.awardedPlayer())
                            + " scored "
                            + resolution.vpAwarded()
                            + " VP from the primary mission \""
                            + primaryMissionEntry.getName()
                            + "\"."
            );
            return;
        }

        if (primaryMissionStateLabel != null) {
            primaryMissionStateLabel.setText("State: Active");
        }
    }

    @FunctionalInterface
    public interface PrimaryMissionOpener {
        MissionResolution open(
                String contextLabel,
                MissionCard missionCard,
                Player defaultAwardedPlayer,
                boolean allowPlayerSelection,
                String keepButtonText
        );
    }
}
