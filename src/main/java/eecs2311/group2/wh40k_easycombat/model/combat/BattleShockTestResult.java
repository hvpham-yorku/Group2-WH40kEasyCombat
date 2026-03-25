package eecs2311.group2.wh40k_easycombat.model.combat;

import java.util.List;

public record BattleShockTestResult(
        String unitName,
        List<Integer> rolls,
        int total,
        int leadership,
        boolean passed,
        boolean battleShocked
) {
    public BattleShockTestResult {
        unitName = unitName == null ? "" : unitName;
        rolls = rolls == null ? List.of() : List.copyOf(rolls);
    }
}
