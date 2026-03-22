package eecs2311.group2.wh40k_easycombat.service.game;

import eecs2311.group2.wh40k_easycombat.model.instance.ArmyInstance;
import eecs2311.group2.wh40k_easycombat.model.instance.BattleState;
import eecs2311.group2.wh40k_easycombat.model.instance.Player;

public final class CombatService {
    private BattleState currentState;

    public CombatService() {
        this("");
    }

    public CombatService(String missionName) {
        this.currentState = new BattleState(missionName);
    }

    public BattleState getCurrentState() {
        return ensureState();
    }

    public void createNewBattle(String missionName) {
        this.currentState = new BattleState(missionName);
    }

    public void startBattle(String missionName, ArmyInstance attackerArmy, ArmyInstance defenderArmy) {
        BattleState state = new BattleState(missionName);
        state.setAttackerArmy(attackerArmy);
        state.setDefenderArmy(defenderArmy);
        this.currentState = state;
    }

    public void setCurrentBattle(BattleState battleState) {
        if (battleState == null) {
            throw new IllegalArgumentException("battleState must not be null");
        }
        this.currentState = battleState;
    }

    public void setArmy(Player player, ArmyInstance army) {
        ensureState().setArmy(player, army);
    }

    public ArmyInstance getArmy(Player player) {
        return ensureState().getArmy(player);
    }

    public ArmyInstance getActiveArmy() {
        return ensureState().getActiveArmy();
    }

    public ArmyInstance getInactiveArmy() {
        return ensureState().getInactiveArmy();
    }

    public Player getActivePlayer() {
        return ensureState().getActivePlayer();
    }
    
    public Player getInactivePlayer() {
        return ensureState().getInactivePlayer();
    }
    
    public void switchActivePlayer() {
        ensureState().switchActivePlayer();
    }

    public void advancePhase() {
        ensureState().advancePhase();
    }

    public void nextRound() {
        ensureState().nextRound();
    }

    public void addCp(Player player, int amount) {
        ensureState().addCp(player, amount);
    }

    public boolean spendCp(Player player, int amount) {
        return ensureState().spendCp(player, amount);
    }

    public void addVp(Player player, int amount) {
        ensureState().addVp(player, amount);
    }

    public BattleState snapshot() {
        return ensureState().deepCopy();
    }

    private BattleState ensureState() {
        if (currentState == null) {
            currentState = new BattleState();
        }
        return currentState;
    }
}
