package eecs2311.group2.wh40k_easycombat.model.instance;

import eecs2311.group2.wh40k_easycombat.viewmodel.GameStrategyVM;

import java.util.ArrayList;
import java.util.List;

public class ArmyInstance {
    private final int armyId;
    private final String armyName;
    private final String factionId;
    private final String factionName;
    private final String detachmentId;

    private int currentCp;
    private int currentVp;

    private final List<UnitInstance> units = new ArrayList<>();
    private final List<GameStrategyVM> strategies = new ArrayList<>();

    public ArmyInstance(
            int armyId,
            String armyName,
            String factionId,
            String factionName,
            String detachmentId
    ) {
        this.armyId = armyId;
        this.armyName = armyName == null ? "" : armyName;
        this.factionId = factionId == null ? "" : factionId;
        this.factionName = factionName == null ? "" : factionName;
        this.detachmentId = detachmentId == null ? "" : detachmentId;
        this.currentCp = 0;
        this.currentVp = 0;
    }

    public int getArmyId() {
        return armyId;
    }

    public String getArmyName() {
        return armyName;
    }

    public String getFactionId() {
        return factionId;
    }

    public String getFactionName() {
        return factionName;
    }

    public String getDetachmentId() {
        return detachmentId;
    }

    public int getCurrentCp() {
        return currentCp;
    }

    public void setCurrentCp(int currentCp) {
        this.currentCp = Math.max(0, currentCp);
    }

    public int getCurrentVp() {
        return currentVp;
    }

    public void setCurrentVp(int currentVp) {
        this.currentVp = Math.max(0, currentVp);
    }

    public List<UnitInstance> getUnits() {
        return units;
    }

    public List<GameStrategyVM> getStrategies() {
        return strategies;
    }

    public void addUnit(UnitInstance unit) {
        if (unit != null) {
            units.add(unit);
        }
    }

    public void addStrategy(GameStrategyVM strategy) {
        if (strategy != null) {
            strategies.add(strategy);
        }
    }

    public void addCp(int amount) {
        setCurrentCp(this.currentCp + amount);
    }

    public void spendCp(int amount) {
        setCurrentCp(this.currentCp - amount);
    }

    public void addVp(int amount) {
        setCurrentVp(this.currentVp + amount);
    }
}