package eecs2311.group2.wh40k_easycombat.service;

import eecs2311.group2.wh40k_easycombat.model.instance.UnitInstance;

import java.util.ArrayList;
import java.util.List;

public class CombatService {
    private final List<UnitInstance> attackerArmy = new ArrayList<>();
    private final List<UnitInstance> defenderArmy = new ArrayList<>();

    private int attackerCP = 0;
    private int defenderCP = 0;
    private int currentRound = 1;
}
