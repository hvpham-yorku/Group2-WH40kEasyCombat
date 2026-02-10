package eecs2311.group2.wh40k_easycombat.service;

import eecs2311.group2.wh40k_easycombat.model.instance.BattleState;
import eecs2311.group2.wh40k_easycombat.model.instance.Player;
import eecs2311.group2.wh40k_easycombat.model.instance.UnitInstance;

import java.util.ArrayList;
import java.util.List;

public class CombatService {
    private BattleState currentState;

    public CombatService() {
        this.currentState = new BattleState();
    }

    public BattleState getCurrentState() {
        return currentState;
    }

    public void createNewBattle(String missionName) {
        this.currentState = new BattleState();
        this.currentState.setMissionName(missionName);
    }
}
