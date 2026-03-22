package eecs2311.group2.wh40k_easycombat.service.game;

import eecs2311.group2.wh40k_easycombat.model.combat.BattleShockTestResult;
import eecs2311.group2.wh40k_easycombat.model.instance.UnitInstance;
import eecs2311.group2.wh40k_easycombat.service.calculations.BattleShockCalculations;
import eecs2311.group2.wh40k_easycombat.service.calculations.DiceService;

import java.util.ArrayList;
import java.util.List;

public class BattleShockService {
    private final DiceService diceService = new DiceService();

    public void clearBattleShockForCommandPhase(List<UnitInstance> units) {
        if (units == null) {
            return;
        }

        for (UnitInstance unit : units) {
            if (unit != null) {
                unit.setBattleShocked(false);
            }
        }
    }

    public List<UnitInstance> battleShockCandidates(List<UnitInstance> units, int round) {
        if (units == null || round < 1) {
            return List.of();
        }

        List<UnitInstance> result = new ArrayList<>();
        for (UnitInstance unit : units) {
            if (unit == null || unit.isDestroyed()) {
                continue;
            }
            if (!unit.isBelowHalfStrength()) {
                continue;
            }

            result.add(unit);
        }

        return result;
    }

    public BattleShockTestResult rollBattleShockTest(UnitInstance unit) {
        if (unit == null) {
            return new BattleShockTestResult("", List.of(), 0, 7, true, false);
        }

        diceService.rollDice(2);
        List<Integer> rolls = List.copyOf(diceService.getResults());
        int total = BattleShockCalculations.rollBattleShockTotalFromExistingRoll(rolls);
        int leadership = unit.getBestLeadership();
        boolean passed = BattleShockCalculations.checkBattleShock(total, leadership);

        unit.setBattleShocked(!passed);

        return new BattleShockTestResult(
                unit.getUnitName(),
                rolls,
                total,
                leadership,
                passed,
                unit.isBattleShocked()
        );
    }
}
