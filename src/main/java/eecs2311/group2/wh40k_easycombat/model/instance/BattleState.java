package eecs2311.group2.wh40k_easycombat.model.instance;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class BattleState {
    private String missionName;
    private int currentRound = 1;
    private Phase currentPhase = Phase.COMMAND;
    private Player activePlayer = Player.ATTACKER; // Using your new Enum

    private int attackerVP = 0;
    private int defenderVP = 0;
    private int attackerCP = 0;
    private int defenderCP = 0;

    private List<UnitInstance> attackerArmy;
    private List<UnitInstance> defenderArmy;

    // --- Getters & Setters ---

    public String getMissionName() { return missionName; }
    public void setMissionName(String missionName) { this.missionName = missionName; }

    public int getCurrentRound() { return currentRound; }
    public void setCurrentRound(int currentRound) { this.currentRound = currentRound; }

    public Phase getCurrentPhase() { return currentPhase; }
    public void setCurrentPhase(Phase currentPhase) { this.currentPhase = currentPhase; }

    public Player getActivePlayer() { return activePlayer; }
    public void setActivePlayer(Player activePlayer) { this.activePlayer = activePlayer; }

    public int getAttackerVP() { return attackerVP; }
    public void setAttackerVP(int attackerVP) { this.attackerVP = attackerVP; }

    public int getDefenderVP() { return defenderVP; }
    public void setDefenderVP(int defenderVP) { this.defenderVP = defenderVP; }

    public int getAttackerCP() { return attackerCP; }
    public void setAttackerCP(int attackerCP) { this.attackerCP = attackerCP; }

    public int getDefenderCP() { return defenderCP; }
    public void setDefenderCP(int defenderCP) { this.defenderCP = defenderCP; }

    public List<UnitInstance> getAttackerArmy() { return attackerArmy; }
    public void setAttackerArmy(List<UnitInstance> attackerArmy) { this.attackerArmy = attackerArmy; }

    public List<UnitInstance> getDefenderArmy() { return defenderArmy; }
    public void setDefenderArmy(List<UnitInstance> defenderArmy) { this.defenderArmy = defenderArmy; }

    // --- Logic Helpers ---

    public void switchActivePlayer() {
        this.activePlayer = (this.activePlayer == Player.ATTACKER)
                ? Player.DEFENDER : Player.ATTACKER;
    }

    /**
     * Deep copy for snapshots
     */
    public BattleState deepCopy() {
        BattleState copy = new BattleState();
        copy.setMissionName(this.missionName);
        copy.setCurrentRound(this.currentRound);
        copy.setCurrentPhase(this.currentPhase);
        copy.setActivePlayer(this.activePlayer);
        copy.setAttackerVP(this.attackerVP);
        copy.setDefenderVP(this.defenderVP);
        copy.setAttackerCP(this.attackerCP);
        copy.setDefenderCP(this.defenderCP);

        copy.setAttackerArmy(this.attackerArmy.stream()
                .map(UnitInstance::copy)
                .collect(Collectors.toList()));

        copy.setDefenderArmy(this.defenderArmy.stream()
                .map(UnitInstance::copy)
                .collect(Collectors.toList()));

        return copy;
    }
}