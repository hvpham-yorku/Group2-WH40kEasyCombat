package eecs2311.group2.wh40k_easycombat.service.game;

import eecs2311.group2.wh40k_easycombat.model.instance.ArmyInstance;
import eecs2311.group2.wh40k_easycombat.model.instance.BattleState;
import eecs2311.group2.wh40k_easycombat.model.instance.Phase;
import eecs2311.group2.wh40k_easycombat.model.instance.Player;
import eecs2311.group2.wh40k_easycombat.service.autobattle.AutoBattleMode;
import eecs2311.group2.wh40k_easycombat.model.combat.PhaseAdvanceResult;

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

    public void resetBattle(String missionName, int maxRounds) {
        this.currentState = new BattleState(missionName, maxRounds);
    }

    public void startBattle(String missionName, ArmyInstance attackerArmy, ArmyInstance defenderArmy) {
        startBattle(missionName, 5, attackerArmy, defenderArmy);
    }

    public void startBattle(String missionName, int maxRounds, ArmyInstance attackerArmy, ArmyInstance defenderArmy) {
        BattleState state = new BattleState(missionName, maxRounds);
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

    public PhaseAdvanceResult advancePhaseState() {
        return ensureState().advancePhaseState();
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

    public Phase getCurrentPhase() {
        return ensureState().getCurrentPhase();
    }

    public int getCurrentRound() {
        return ensureState().getCurrentRound();
    }

    public int getMaxRounds() {
        return ensureState().getMaxRounds();
    }

    public void setMaxRounds(int maxRounds) {
        ensureState().setMaxRounds(maxRounds);
    }

    public boolean isBattleOver() {
        return ensureState().isBattleOver();
    }

    public void setBattleOver(boolean battleOver) {
        ensureState().setBattleOver(battleOver);
    }

    public boolean canOpenAutoBattle() {
        return ensureState().canOpenAutoBattle();
    }

    public AutoBattleMode currentAutoBattleMode() {
        return ensureState().currentAutoBattleMode();
    }

    public String phaseText() {
        return ensureState().phaseText();
    }

    public String phaseLabelFor(Player player) {
        return ensureState().phaseLabelFor(player);
    }

    public int getCurrentCp(Player player) {
        return ensureState().getCurrentCp(player);
    }

    public void setCurrentCp(Player player, int cp) {
        ensureState().setCurrentCp(player, cp);
    }

    public int getCurrentVp(Player player) {
        return ensureState().getCurrentVp(player);
    }

    public void setCurrentVp(Player player, int vp) {
        ensureState().setCurrentVp(player, vp);
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
