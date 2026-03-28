package eecs2311.group2.wh40k_easycombat.viewmodel;

import eecs2311.group2.wh40k_easycombat.model.mission.MissionCard;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

public class MissionEntryVM {
    private final MissionCard missionCard;
    private final ReadOnlyStringWrapper name;
    private final StringProperty state = new SimpleStringProperty("Available");
    private final StringProperty mode = new SimpleStringProperty("Tactical");

    public MissionEntryVM(MissionCard missionCard) {
        this.missionCard = missionCard;
        this.name = new ReadOnlyStringWrapper(missionCard == null ? "" : missionCard.title());
    }

    public MissionCard getMissionCard() {
        return missionCard;
    }

    public String getName() {
        return name.get();
    }

    public ReadOnlyStringWrapper nameProperty() {
        return name;
    }

    public String getState() {
        return state.get();
    }

    public void setState(String value) {
        state.set(value == null || value.isBlank() ? "Available" : value.trim());
    }

    public StringProperty stateProperty() {
        return state;
    }

    public String getMode() {
        return mode.get();
    }

    public void setMode(String value) {
        mode.set(value == null || value.isBlank() ? "Tactical" : value.trim());
    }

    public StringProperty modeProperty() {
        return mode;
    }
}
