package eecs2311.group2.wh40k_easycombat.controller.helper;

import eecs2311.group2.wh40k_easycombat.model.instance.Player;
import eecs2311.group2.wh40k_easycombat.model.mission.MissionCard;
import eecs2311.group2.wh40k_easycombat.model.mission.MissionResolution;
import eecs2311.group2.wh40k_easycombat.model.mission.SecondaryMissionMode;
import eecs2311.group2.wh40k_easycombat.service.BattleLogService;
import eecs2311.group2.wh40k_easycombat.service.game.GameEngine;
import eecs2311.group2.wh40k_easycombat.service.mission.MissionSessionService;
import eecs2311.group2.wh40k_easycombat.viewmodel.MissionEntryVM;
import javafx.collections.ObservableList;
import javafx.scene.control.Button;
import javafx.scene.control.TableView;

import java.util.List;
import java.util.Locale;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.stream.Collectors;

public final class GameUIMissionHelper {

    private GameUIMissionHelper() {
    }

    public static void openSelectedSecondaryMission(
            GameEngine gameEngine,
            MissionSessionService missionSessionService,
            BattleLogService battleLogService,
            Player owningPlayer,
            String sideLabel,
            String playerLabel,
            TableView<MissionEntryVM> missionTable,
            MissionCardOpener missionCardOpener,
            BiConsumer<Player, Integer> addVictoryPoints,
            Runnable refreshMissionTables
    ) {
        if (gameEngine.isBattleOver()) {
            DialogHelper.showInfo("Battle Over", gameEngine.winnerText());
            return;
        }

        MissionEntryVM selected = missionTable.getSelectionModel().getSelectedItem();
        if (selected == null || selected.getMissionCard() == null) {
            DialogHelper.showWarning("No Mission Selected", "Please select one secondary mission first.");
            return;
        }

        if ("Completed".equalsIgnoreCase(selected.getState())) {
            DialogHelper.showInfo("Mission Already Completed", "That tactical mission has already been completed.");
            return;
        }

        MissionResolution resolution = missionCardOpener.open(
                sideLabel + " Secondary Mission",
                selected.getMissionCard(),
                owningPlayer,
                false,
                missionSessionService.modeFor(owningPlayer) == SecondaryMissionMode.FIXED
                        ? "Keep Fixed Mission"
                        : "Keep Active"
        );

        if (resolution.decision().isClosed()) {
            return;
        }

        if (resolution.decision().isCompleted()) {
            addVictoryPoints.accept(owningPlayer, resolution.vpAwarded());
            if (missionSessionService.modeFor(owningPlayer) == SecondaryMissionMode.TACTICAL) {
                missionSessionService.complete(owningPlayer, selected.getName());
            }
            battleLogService.logTurnEvent(
                    gameEngine.getCurrentRound(),
                    gameEngine.getCurrentPhase(),
                    owningPlayer,
                    playerLabel
                            + " completed "
                            + selected.getMode().toLowerCase(Locale.ROOT)
                            + " secondary mission \""
                            + selected.getName()
                            + "\" for "
                            + resolution.vpAwarded()
                            + " VP."
            );
        }

        refreshMissionTables.run();
    }

    public static void abandonSelectedMission(
            GameEngine gameEngine,
            MissionSessionService missionSessionService,
            BattleLogService battleLogService,
            Player player,
            String sideLabel,
            String playerLabel,
            TableView<MissionEntryVM> missionTable,
            Runnable syncScoreLabels,
            Function<Player, Integer> currentCp,
            Runnable grantCommandPoint,
            Runnable refreshMissionTables
    ) {
        if (gameEngine.isBattleOver()) {
            DialogHelper.showInfo("Battle Over", gameEngine.winnerText());
            return;
        }

        if (player != gameEngine.getActivePlayer()) {
            DialogHelper.showWarning(
                    "Wrong Turn",
                    "You can only abandon tactical missions during that army's current turn."
            );
            return;
        }

        MissionEntryVM selected = missionTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            DialogHelper.showWarning("No Mission Selected", "Please select one secondary mission first.");
            return;
        }
        if ("Completed".equalsIgnoreCase(selected.getState())) {
            DialogHelper.showWarning("Cannot Abandon", "Completed missions are kept in the log and cannot be abandoned.");
            return;
        }
        if (!DialogHelper.confirmYesNo("Abandon Mission", "Mark \"" + selected.getName() + "\" as abandoned?")) {
            return;
        }

        if (!missionSessionService.abandon(player, selected.getName())) {
            DialogHelper.showWarning("Cannot Abandon", "That mission cannot be abandoned right now.");
            return;
        }

        boolean grantedCp = false;
        int beforeCp = currentCp.apply(player);
        if (missionSessionService.grantAbandonCpIfAvailable(player)) {
            grantCommandPoint.run();
            syncScoreLabels.run();
            grantedCp = true;
            DialogHelper.showInfo(
                    "Mission Abandoned",
                    sideLabel + " gained 1 CP for the first mission abandoned this turn."
            );
        }

        int afterCp = currentCp.apply(player);
        battleLogService.logTurnEvent(
                gameEngine.getCurrentRound(),
                gameEngine.getCurrentPhase(),
                player,
                playerLabel
                        + " abandoned secondary mission \""
                        + selected.getName()
                        + "\"."
                        + (grantedCp
                        ? " First abandon this turn granted 1 CP (" + beforeCp + " -> " + afterCp + ")."
                        : "")
        );

        refreshMissionTables.run();
    }

    public static void drawSecondaryMissions(
            GameEngine gameEngine,
            MissionSessionService missionSessionService,
            BattleLogService battleLogService,
            Player player,
            String sideLabel,
            String playerLabel,
            Runnable refreshMissionTables
    ) {
        if (gameEngine.isBattleOver()) {
            DialogHelper.showInfo("Battle Over", gameEngine.winnerText());
            return;
        }

        if (player != gameEngine.getActivePlayer()) {
            DialogHelper.showWarning(
                    "Wrong Turn",
                    "You can only draw tactical secondary missions during that army's current turn."
            );
            return;
        }

        int drawCount = missionSessionService.drawCountFor(player);
        if (drawCount <= 0) {
            DialogHelper.showInfo(
                    "No Draw Available",
                    sideLabel + " has already drawn two tactical missions this turn."
            );
            return;
        }

        List<String> beforeMissionNames = missionNames(missionSessionService.activeEntriesFor(player));
        missionSessionService.drawFor(player);
        List<String> afterMissionNames = missionNames(missionSessionService.activeEntriesFor(player));
        List<String> drawnMissionNames = newlyAddedNames(beforeMissionNames, afterMissionNames);
        refreshMissionTables.run();

        DialogHelper.showInfo(
                "Secondary Missions Drawn",
                sideLabel
                        + " drew "
                        + drawCount
                        + " secondary mission"
                        + (drawCount == 1 ? "." : "s.")
        );

        battleLogService.logTurnEvent(
                gameEngine.getCurrentRound(),
                gameEngine.getCurrentPhase(),
                player,
                playerLabel
                        + " drew tactical mission"
                        + (drawnMissionNames.size() == 1 ? "" : "s")
                        + ": "
                        + (drawnMissionNames.isEmpty()
                        ? drawCount + " mission" + (drawCount == 1 ? "" : "s")
                        : String.join(", ", drawnMissionNames))
                        + "."
        );
    }

    public static void refreshMissionTables(
            MissionSessionService missionSessionService,
            ObservableList<MissionEntryVM> attackerEntries,
            ObservableList<MissionEntryVM> defenderEntries,
            TableView<MissionEntryVM> attackerTable,
            TableView<MissionEntryVM> defenderTable
    ) {
        attackerEntries.setAll(missionSessionService.activeEntriesFor(Player.ATTACKER));
        defenderEntries.setAll(missionSessionService.activeEntriesFor(Player.DEFENDER));

        if (!attackerEntries.isEmpty()) {
            attackerTable.getSelectionModel().selectFirst();
        }
        if (!defenderEntries.isEmpty()) {
            defenderTable.getSelectionModel().selectFirst();
        }
    }

    public static void updateSecondaryMissionButtons(
            MissionSessionService missionSessionService,
            GameEngine gameEngine,
            Button attackerDrawButton,
            Button defenderDrawButton,
            Button attackerAbandonButton,
            Button defenderAbandonButton,
            Button attackerCheckButton,
            Button defenderCheckButton,
            Button attackerStrategyButton,
            Button defenderStrategyButton,
            ObservableList<MissionEntryVM> attackerEntries,
            ObservableList<MissionEntryVM> defenderEntries
    ) {
        boolean attackerFixed = missionSessionService.modeFor(Player.ATTACKER) == SecondaryMissionMode.FIXED;
        boolean defenderFixed = missionSessionService.modeFor(Player.DEFENDER) == SecondaryMissionMode.FIXED;
        boolean attackerActiveTurn = gameEngine.getActivePlayer() == Player.ATTACKER;
        boolean defenderActiveTurn = gameEngine.getActivePlayer() == Player.DEFENDER;
        int attackerDrawCount = missionSessionService.drawCountFor(Player.ATTACKER);
        int defenderDrawCount = missionSessionService.drawCountFor(Player.DEFENDER);

        if (attackerDrawButton != null) {
            attackerDrawButton.setText(
                    attackerFixed
                            ? "Fixed Missions"
                            : (attackerDrawCount == 1 ? "Draw 1 Mission" : "Draw " + attackerDrawCount + " Missions")
            );
            attackerDrawButton.setDisable(
                    attackerFixed || !attackerActiveTurn || !missionSessionService.canDraw(Player.ATTACKER) || gameEngine.isBattleOver()
            );
        }
        if (defenderDrawButton != null) {
            defenderDrawButton.setText(
                    defenderFixed
                            ? "Fixed Missions"
                            : (defenderDrawCount == 1 ? "Draw 1 Mission" : "Draw " + defenderDrawCount + " Missions")
            );
            defenderDrawButton.setDisable(
                    defenderFixed || !defenderActiveTurn || !missionSessionService.canDraw(Player.DEFENDER) || gameEngine.isBattleOver()
            );
        }
        if (attackerAbandonButton != null) {
            attackerAbandonButton.setDisable(attackerFixed || !attackerActiveTurn || attackerEntries.isEmpty() || gameEngine.isBattleOver());
        }
        if (defenderAbandonButton != null) {
            defenderAbandonButton.setDisable(defenderFixed || !defenderActiveTurn || defenderEntries.isEmpty() || gameEngine.isBattleOver());
        }
        if (attackerCheckButton != null) {
            attackerCheckButton.setDisable(attackerEntries.isEmpty() || gameEngine.isBattleOver());
        }
        if (defenderCheckButton != null) {
            defenderCheckButton.setDisable(defenderEntries.isEmpty() || gameEngine.isBattleOver());
        }
        if (attackerStrategyButton != null) {
            attackerStrategyButton.setDisable(gameEngine.isBattleOver());
        }
        if (defenderStrategyButton != null) {
            defenderStrategyButton.setDisable(gameEngine.isBattleOver());
        }
    }

    private static List<String> missionNames(List<MissionEntryVM> entries) {
        return entries.stream()
                .map(MissionEntryVM::getName)
                .collect(Collectors.toList());
    }

    private static List<String> newlyAddedNames(List<String> before, List<String> after) {
        List<String> added = after.stream().collect(Collectors.toList());
        for (String previous : before) {
            added.remove(previous);
        }
        return added;
    }

    @FunctionalInterface
    public interface MissionCardOpener {
        MissionResolution open(
                String contextLabel,
                MissionCard missionCard,
                Player defaultAwardedPlayer,
                boolean allowPlayerSelection,
                String keepButtonText
        );
    }
}
