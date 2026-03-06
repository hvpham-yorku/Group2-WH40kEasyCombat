package eecs2311.group2.wh40k_easycombat.viewmodel;

import eecs2311.group2.wh40k_easycombat.util.CostParser;
import eecs2311.group2.wh40k_easycombat.util.CostTier;

import javafx.beans.property.*;

import java.util.List;

public class ArmyUnitVM {

    private final String unitName;
    private final String statLine;

    private final IntegerProperty modelCount = new SimpleIntegerProperty();
    private final IntegerProperty weaponCount = new SimpleIntegerProperty();
    private final IntegerProperty points = new SimpleIntegerProperty();

    private final BooleanProperty expanded = new SimpleBooleanProperty(false);

    private final List<CostTier> tiers;

    private final int minModels;
    private final int maxModels;

    public ArmyUnitVM(String unitName,
                      String statLine,
                      List<CostTier> tiers) {

        this.unitName = unitName;
        this.statLine = statLine;
        this.tiers = tiers;

        if (tiers.isEmpty()) {
            minModels = 1;
            maxModels = 1;
        } else {
            minModels = tiers.get(0).models();
            maxModels = tiers.get(tiers.size() - 1).models();
        }

        modelCount.set(minModels);

        updatePoints();
    }

    private void updatePoints() {
        points.set(CostParser.pointsForModels(modelCount.get(), tiers));
    }

    public void incModels() {
        if (modelCount.get() < maxModels) {
            modelCount.set(modelCount.get() + 1);
            updatePoints();
        }
    }

    public void decModels() {
        if (modelCount.get() > minModels) {
            modelCount.set(modelCount.get() - 1);
            updatePoints();
        }
    }

    public void incWeapons() {
        weaponCount.set(weaponCount.get() + 1);
    }

    public void decWeapons() {
        if (weaponCount.get() > 0)
            weaponCount.set(weaponCount.get() - 1);
    }

    public String getUnitName() {
        return unitName;
    }

    public String getStatLine() {
        return statLine;
    }

    public IntegerProperty modelCountProperty() {
        return modelCount;
    }

    public IntegerProperty weaponCountProperty() {
        return weaponCount;
    }

    public IntegerProperty pointsProperty() {
        return points;
    }

    public BooleanProperty expandedProperty() {
        return expanded;
    }

    public int getMinModels() {
        return minModels;
    }

    public int getMaxModels() {
        return maxModels;
    }
}