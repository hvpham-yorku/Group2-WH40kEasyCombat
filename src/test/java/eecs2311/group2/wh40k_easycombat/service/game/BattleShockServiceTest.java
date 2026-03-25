package eecs2311.group2.wh40k_easycombat.service.game;

import eecs2311.group2.wh40k_easycombat.model.combat.BattleShockTestResult;
import eecs2311.group2.wh40k_easycombat.model.instance.UnitInstance;
import eecs2311.group2.wh40k_easycombat.model.instance.UnitModelInstance;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class BattleShockServiceTest {

    private BattleShockService service;

    @BeforeEach
    void setUp() {
        service = new BattleShockService();
    }

    @Test
    @DisplayName("Battle-shock candidates can appear from round 1 and only for below-half living units")
    void battleShockCandidatesFollowRoundAndStrengthRules() {
        UnitInstance healthy = makeMultiModelUnit("Healthy", 4, 3, "6+");
        UnitInstance exactHalf = makeMultiModelUnit("Exact Half", 4, 2, "6+");
        UnitInstance belowHalf = makeMultiModelUnit("Below Half", 5, 2, "6+");
        UnitInstance destroyed = makeMultiModelUnit("Destroyed", 3, 0, "6+");

        assertTrue(service.battleShockCandidates(List.of(healthy, exactHalf, belowHalf, destroyed), 0).isEmpty());

        List<UnitInstance> candidates = service.battleShockCandidates(
                List.of(healthy, exactHalf, belowHalf, destroyed),
                1
        );

        assertEquals(1, candidates.size());
        assertSame(belowHalf, candidates.getFirst());
    }

    @Test
    @DisplayName("Battle-shock test always passes when leadership is 2+")
    void battleShockTestPassesForTwoPlusLeadership() {
        UnitInstance unit = makeSingleModelUnit("Steady Unit", 4, "2+");

        BattleShockTestResult result = service.rollBattleShockTest(unit);

        assertTrue(result.passed());
        assertFalse(result.battleShocked());
        assertFalse(unit.isBattleShocked());
        assertEquals(2, result.rolls().size());
        assertEquals(result.rolls().stream().mapToInt(Integer::intValue).sum(), result.total());
    }

    @Test
    @DisplayName("Battle-shock test always fails when leadership is 13+")
    void battleShockTestFailsForImpossibleLeadership() {
        UnitInstance unit = makeSingleModelUnit("Shaken Unit", 4, "13+");

        BattleShockTestResult result = service.rollBattleShockTest(unit);

        assertFalse(result.passed());
        assertTrue(result.battleShocked());
        assertTrue(unit.isBattleShocked());
        assertEquals(13, result.leadership());
    }

    @Test
    @DisplayName("Command phase clear removes battle-shock from all provided units")
    void clearBattleShockForCommandPhaseClearsStatuses() {
        UnitInstance blue = makeSingleModelUnit("Blue Unit", 3, "6+");
        UnitInstance red = makeSingleModelUnit("Red Unit", 3, "6+");
        blue.setBattleShocked(true);
        red.setBattleShocked(true);

        service.clearBattleShockForCommandPhase(List.of(blue, red));

        assertFalse(blue.isBattleShocked());
        assertFalse(red.isBattleShocked());
    }

    private static UnitInstance makeSingleModelUnit(String unitName, int wounds, String leadership) {
        UnitInstance unit = new UnitInstance("datasheet", unitName);
        unit.addModel(new UnitModelInstance("Leader", "6", "4", "3+", String.valueOf(wounds), leadership, "1", ""));
        return unit;
    }

    private static UnitInstance makeMultiModelUnit(String unitName, int totalModels, int aliveModels, String leadership) {
        UnitInstance unit = new UnitInstance("datasheet", unitName);

        for (int i = 0; i < totalModels; i++) {
            UnitModelInstance model = new UnitModelInstance(
                    "Model " + i,
                    "6",
                    "4",
                    "3+",
                    "2",
                    leadership,
                    "1",
                    ""
            );

            if (i >= aliveModels) {
                model.setCurrentHp(0);
            }

            unit.addModel(model);
        }

        return unit;
    }
}
