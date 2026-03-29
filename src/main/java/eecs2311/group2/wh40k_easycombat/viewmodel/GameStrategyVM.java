package eecs2311.group2.wh40k_easycombat.viewmodel;

import eecs2311.group2.wh40k_easycombat.model.instance.StratagemInstance;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;

public class GameStrategyVM {

    private final StratagemInstance strategy;
    private final BooleanProperty expanded = new SimpleBooleanProperty(false);

    public GameStrategyVM(StratagemInstance strategy) {
        this.strategy = strategy == null
                ? new StratagemInstance("", "", "", "", "")
                : strategy;
    }

    public StratagemInstance getStrategy() {
        return strategy;
    }

    public String getName() {
        return strategy.name();
    }

    public String getCpCost() {
        return strategy.cpCost();
    }

    public String getTurn() {
        return strategy.turn();
    }

    public String getPhase() {
        return strategy.phase();
    }

    public String getDescriptionHtml() {
        return strategy.descriptionHtml();
    }

    public BooleanProperty expandedProperty() {
        return expanded;
    }
}
