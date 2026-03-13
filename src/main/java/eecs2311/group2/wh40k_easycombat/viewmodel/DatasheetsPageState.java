package eecs2311.group2.wh40k_easycombat.viewmodel;

import eecs2311.group2.wh40k_easycombat.model.Abilities;
import eecs2311.group2.wh40k_easycombat.model.Datasheets;
import eecs2311.group2.wh40k_easycombat.model.Detachment_abilities;
import eecs2311.group2.wh40k_easycombat.model.Enhancements;
import eecs2311.group2.wh40k_easycombat.model.Stratagems;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class DatasheetsPageState {

    private final ObservableList<DatasheetListItemVM> allDatasheets = FXCollections.observableArrayList();
    private final ObservableList<DatasheetListItemVM> filteredDatasheets = FXCollections.observableArrayList();

    private final Map<String, String> factionNameToId = new LinkedHashMap<>();
    private final Map<String, Datasheets> datasheetsById = new LinkedHashMap<>();
    private final Map<String, Abilities> abilitiesById = new LinkedHashMap<>();
    private final Map<String, Detachment_abilities> detachmentAbilitiesById = new LinkedHashMap<>();
    private final Map<String, Stratagems> stratagemsById = new LinkedHashMap<>();
    private final Map<String, Enhancements> enhancementsById = new LinkedHashMap<>();

    public ObservableList<DatasheetListItemVM> getAllDatasheets() {
        return allDatasheets;
    }

    public ObservableList<DatasheetListItemVM> getFilteredDatasheets() {
        return filteredDatasheets;
    }

    public Map<String, String> getFactionNameToId() {
        return factionNameToId;
    }

    public Map<String, Datasheets> getDatasheetsById() {
        return datasheetsById;
    }

    public Map<String, Abilities> getAbilitiesById() {
        return abilitiesById;
    }

    public Map<String, Detachment_abilities> getDetachmentAbilitiesById() {
        return detachmentAbilitiesById;
    }

    public Map<String, Stratagems> getStratagemsById() {
        return stratagemsById;
    }

    public Map<String, Enhancements> getEnhancementsById() {
        return enhancementsById;
    }

    public void replaceDatasheets(Collection<DatasheetListItemVM> datasheets) {
        allDatasheets.setAll(datasheets == null ? List.of() : datasheets);
        filteredDatasheets.setAll(allDatasheets);

        datasheetsById.clear();
        for (DatasheetListItemVM item : allDatasheets) {
            if (item == null || item.getDatasheet() == null || item.getDatasheetId().isBlank()) {
                continue;
            }
            datasheetsById.put(item.getDatasheetId(), item.getDatasheet());
        }
    }

    public void applyFilters(String keyword, String selectedFaction) {
        String faction = selectedFaction == null || selectedFaction.isBlank() ? "ALL" : selectedFaction;
        String selectedFactionId = factionNameToId.getOrDefault(faction, faction);

        filteredDatasheets.setAll(
                allDatasheets.stream()
                        .filter(item -> item.matches(keyword, faction, selectedFactionId))
                        .collect(Collectors.toList())
        );
    }
}
