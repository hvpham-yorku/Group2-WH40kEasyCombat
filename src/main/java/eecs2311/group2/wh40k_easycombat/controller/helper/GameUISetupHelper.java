package eecs2311.group2.wh40k_easycombat.controller.helper;

import eecs2311.group2.wh40k_easycombat.controller.GameUIController;
import eecs2311.group2.wh40k_easycombat.model.instance.GameSetupConfig;
import eecs2311.group2.wh40k_easycombat.model.instance.Player;
import eecs2311.group2.wh40k_easycombat.model.mission.MissionCard;
import eecs2311.group2.wh40k_easycombat.service.BattleLogService;
import eecs2311.group2.wh40k_easycombat.service.game.GameEngine;
import eecs2311.group2.wh40k_easycombat.service.game.GameSetupService;
import eecs2311.group2.wh40k_easycombat.service.mission.MissionService;
import eecs2311.group2.wh40k_easycombat.service.mission.MissionSessionService;
import eecs2311.group2.wh40k_easycombat.viewmodel.GameArmyImportVM;
import eecs2311.group2.wh40k_easycombat.viewmodel.MissionEntryVM;

import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;

public final class GameUISetupHelper {

    private GameUISetupHelper() {
    }

    public static MissionEntryVM applySetupConfig(
            GameSetupService gameSetupService,
            MissionService missionService,
            MissionSessionService missionSessionService,
            GameEngine gameEngine,
            BattleLogService battleLogService,
            SetupCallbacks callbacks
    ) {
        GameSetupConfig config = gameSetupService.getCurrentConfig();
        MissionEntryVM primaryMissionEntry = null;

        if (config == null) {
            List<MissionCard> primaryMissions = missionService.getPrimaryMissions();
            if (!primaryMissions.isEmpty()) {
                primaryMissionEntry = new MissionEntryVM(primaryMissions.get(0));
                callbacks.setMissionName().accept(primaryMissionEntry.getName());
                callbacks.setPrimaryMissionState().accept("State: Active");
                gameEngine.selectMainMission(primaryMissionEntry.getName(), 0);
            }

            missionSessionService.initialize(null);
            missionSessionService.startTurn(gameEngine.getActivePlayer());
            callbacks.refreshMissionTables().run();
            battleLogService.logTurnEvent(
                    gameEngine.getCurrentRound(),
                    gameEngine.getCurrentPhase(),
                    gameEngine.getActivePlayer(),
                    "Battle started with the default setup. Attacker acts first."
            );
            return primaryMissionEntry;
        }

        gameEngine.configureBattle(config.primaryMission().title(), config.maxRounds());
        callbacks.ruleApplyState().accept(config.customRulesEnabled());

        callbacks.acceptImportedArmy().accept(GameUIController.ArmySide.BLUE, config.blueArmy());
        callbacks.acceptImportedArmy().accept(GameUIController.ArmySide.RED, config.redArmy());

        primaryMissionEntry = new MissionEntryVM(config.primaryMission());
        primaryMissionEntry.setState("Active");
        callbacks.setMissionName().accept(primaryMissionEntry.getName());
        callbacks.setPrimaryMissionState().accept("State: Active");

        missionSessionService.initialize(config);
        missionSessionService.startTurn(gameEngine.getActivePlayer());
        callbacks.refreshMissionTables().run();

        battleLogService.log(
                "Battle started. Attacker: "
                        + config.blueArmy().factionName()
                        + ". Defender: "
                        + config.redArmy().factionName()
                        + ". Battle size: "
                        + config.battleSizeLabel()
                        + " ("
                        + config.battleSizePoints()
                        + " points). Primary mission: "
                        + config.primaryMission().title()
                        + ". Max rounds: "
                        + config.maxRounds()
                        + ". Custom rules: "
                        + (config.customRulesEnabled() ? "enabled" : "disabled")
                        + "."
        );
        battleLogService.logTurnEvent(
                gameEngine.getCurrentRound(),
                gameEngine.getCurrentPhase(),
                gameEngine.getActivePlayer(),
                "Starting CP - Attacker: "
                        + callbacks.currentCp().apply(Player.ATTACKER)
                        + ", Defender: "
                        + callbacks.currentCp().apply(Player.DEFENDER)
                        + ". Attacker takes the first turn."
        );

        if (!config.blueFixedSecondaryMissions().isEmpty()) {
            battleLogService.log(
                    "Attacker fixed secondary missions: "
                            + config.blueFixedSecondaryMissions().stream()
                            .map(MissionCard::title)
                            .collect(Collectors.joining(", "))
                            + "."
            );
        }
        if (!config.redFixedSecondaryMissions().isEmpty()) {
            battleLogService.log(
                    "Defender fixed secondary missions: "
                            + config.redFixedSecondaryMissions().stream()
                            .map(MissionCard::title)
                            .collect(Collectors.joining(", "))
                            + "."
            );
        }

        return primaryMissionEntry;
    }

    public record SetupCallbacks(
            BiConsumer<GameUIController.ArmySide, GameArmyImportVM> acceptImportedArmy,
            Consumer<String> setMissionName,
            Consumer<String> setPrimaryMissionState,
            Runnable refreshMissionTables,
            Consumer<Boolean> ruleApplyState,
            Function<Player, Integer> currentCp
    ) {
    }
}
