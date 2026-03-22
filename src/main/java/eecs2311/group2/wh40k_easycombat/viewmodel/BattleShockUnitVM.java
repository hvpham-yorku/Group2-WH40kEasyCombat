package eecs2311.group2.wh40k_easycombat.viewmodel;

import eecs2311.group2.wh40k_easycombat.model.combat.BattleShockTestResult;
import eecs2311.group2.wh40k_easycombat.model.instance.UnitInstance;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

public class BattleShockUnitVM {
    private final UnitInstance unit;
    private final StringProperty unitName = new SimpleStringProperty();
    private final StringProperty strengthText = new SimpleStringProperty();
    private final IntegerProperty leadership = new SimpleIntegerProperty();
    private final StringProperty rollResult = new SimpleStringProperty("Pending");
    private final BooleanProperty battleShocked = new SimpleBooleanProperty(false);
    private final BooleanProperty tested = new SimpleBooleanProperty(false);

    public BattleShockUnitVM(UnitInstance unit) {
        this.unit = unit;
        refreshFromUnit();
        battleShocked.addListener((obs, oldValue, newValue) -> {
            if (this.unit != null) {
                this.unit.setBattleShocked(newValue);
            }
        });
    }

    public UnitInstance getUnit() {
        return unit;
    }

    public StringProperty unitNameProperty() {
        return unitName;
    }

    public StringProperty strengthTextProperty() {
        return strengthText;
    }

    public IntegerProperty leadershipProperty() {
        return leadership;
    }

    public StringProperty rollResultProperty() {
        return rollResult;
    }

    public BooleanProperty battleShockedProperty() {
        return battleShocked;
    }

    public BooleanProperty testedProperty() {
        return tested;
    }

    public boolean isPending() {
        return !tested.get();
    }

    public void refreshFromUnit() {
        if (unit == null) {
            unitName.set("");
            strengthText.set("0/0");
            leadership.set(7);
            battleShocked.set(false);
            return;
        }

        unitName.set(unit.getUnitName());
        strengthText.set(unit.getAliveModelCount() + "/" + unit.getModelCount());
        leadership.set(unit.getBestLeadership());
        battleShocked.set(unit.isBattleShocked());
    }

    public void applyTestResult(BattleShockTestResult result) {
        if (result == null) {
            return;
        }

        tested.set(true);
        rollResult.set(result.rolls() + " = " + result.total() + " vs Ld " + result.leadership()
                + (result.passed() ? " PASS" : " FAIL"));
        battleShocked.set(result.battleShocked());
        refreshFromUnit();
    }
}
