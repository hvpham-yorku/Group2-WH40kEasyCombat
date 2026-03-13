package eecs2311.group2.wh40k_easycombat.viewmodel;

import java.util.List;

public record GameArmyImportVM(
        int armyId,
        String armyName,
        String factionId,
        String factionName,
        int points,
        List<GameArmyUnitVM> units,
        List<GameStrategyVM> strategies
) {
    public GameArmyImportVM {
        armyName = armyName == null ? "" : armyName;
        factionId = factionId == null ? "" : factionId;
        factionName = factionName == null ? "" : factionName;
        units = units == null ? List.of() : List.copyOf(units);
        strategies = strategies == null ? List.of() : List.copyOf(strategies);
    }
}