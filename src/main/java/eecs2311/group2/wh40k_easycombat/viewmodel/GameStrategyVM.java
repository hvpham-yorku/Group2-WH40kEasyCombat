package eecs2311.group2.wh40k_easycombat.viewmodel;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;

public class GameStrategyVM {

    private final String name;
    private final String cpCost;
    private final String turn;
    private final String phase;
    private final String descriptionHtml;
    private final BooleanProperty expanded = new SimpleBooleanProperty(false);

    public GameStrategyVM(String name, String cpCost, String turn, String phase, String descriptionHtml) {
        this.name = name == null ? "" : name;
        this.cpCost = cpCost == null ? "" : cpCost;
        this.turn = turn == null ? "" : turn;
        this.phase = phase == null ? "" : phase;
        this.descriptionHtml = descriptionHtml == null ? "" : descriptionHtml;
    }

    public String getName() {
        return name;
    }

    public String getCpCost() {
        return cpCost;
    }

    public String getTurn() {
        return turn;
    }

    public String getPhase() {
        return phase;
    }

    public String getDescriptionHtml() {
        return descriptionHtml;
    }

    public BooleanProperty expandedProperty() {
        return expanded;
    }
}