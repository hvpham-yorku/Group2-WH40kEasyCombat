package eecs2311.group2.wh40k_easycombat.service;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.util.HashMap;
import java.util.Map;

public class DatasheetsPageState {

    private final ObservableList<Object> allDatasheets = FXCollections.observableArrayList();
    private final ObservableList<Object> filteredDatasheets = FXCollections.observableArrayList();

    private final Map<String, String> factionNameToId = new HashMap<>();
    private final Map<String, Object> abilitiesById = new HashMap<>();

    public ObservableList<Object> getAllDatasheets() {
        return allDatasheets;
    }

    public ObservableList<Object> getFilteredDatasheets() {
        return filteredDatasheets;
    }

    public Map<String, String> getFactionNameToId() {
        return factionNameToId;
    }

    public Map<String, Object> getAbilitiesById() {
        return abilitiesById;
    }
}