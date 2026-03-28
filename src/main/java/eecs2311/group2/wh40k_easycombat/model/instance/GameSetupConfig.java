package eecs2311.group2.wh40k_easycombat.model.instance;

import eecs2311.group2.wh40k_easycombat.model.mission.MissionCard;
import eecs2311.group2.wh40k_easycombat.model.mission.SecondaryMissionMode;
import eecs2311.group2.wh40k_easycombat.viewmodel.GameArmyImportVM;

import java.util.List;

public record GameSetupConfig(
        GameArmyImportVM blueArmy,
        GameArmyImportVM redArmy,
        int battleSizePoints,
        String battleSizeLabel,
        MissionCard primaryMission,
        SecondaryMissionMode blueSecondaryMode,
        SecondaryMissionMode redSecondaryMode,
        List<MissionCard> blueFixedSecondaryMissions,
        List<MissionCard> redFixedSecondaryMissions,
        int maxRounds,
        boolean customRulesEnabled
) {
    public GameSetupConfig {
        battleSizePoints = Math.max(0, battleSizePoints);
        battleSizeLabel = battleSizeLabel == null ? "" : battleSizeLabel.trim();
        blueSecondaryMode = blueSecondaryMode == null ? SecondaryMissionMode.TACTICAL : blueSecondaryMode;
        redSecondaryMode = redSecondaryMode == null ? SecondaryMissionMode.TACTICAL : redSecondaryMode;
        blueFixedSecondaryMissions = blueFixedSecondaryMissions == null ? List.of() : List.copyOf(blueFixedSecondaryMissions);
        redFixedSecondaryMissions = redFixedSecondaryMissions == null ? List.of() : List.copyOf(redFixedSecondaryMissions);
        maxRounds = maxRounds <= 0 ? 5 : maxRounds;
    }
}
