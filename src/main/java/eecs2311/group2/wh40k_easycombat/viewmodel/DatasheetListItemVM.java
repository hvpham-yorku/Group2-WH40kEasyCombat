package eecs2311.group2.wh40k_easycombat.viewmodel;

import eecs2311.group2.wh40k_easycombat.model.Datasheets;

import java.util.Locale;

public final class DatasheetListItemVM {

    private final Datasheets datasheet;
    private final String factionName;

    public DatasheetListItemVM(Datasheets datasheet, String factionName) {
        if (datasheet == null) {
            throw new IllegalArgumentException("datasheet must not be null");
        }
        this.datasheet = datasheet;
        this.factionName = safeText(factionName, datasheet.faction_id());
    }

    public Datasheets getDatasheet() {
        return datasheet;
    }

    public String getDatasheetId() {
        return safeText(datasheet.id(), "");
    }

    public String getName() {
        return safeText(datasheet.name(), getDatasheetId());
    }

    public String getFactionId() {
        return safeText(datasheet.faction_id(), "");
    }

    public String getFactionName() {
        return factionName;
    }

    public boolean matches(String search, String selectedFaction, String selectedFactionId) {
        boolean factionOk = "ALL".equalsIgnoreCase(selectedFaction)
                || getFactionId().equalsIgnoreCase(selectedFactionId)
                || getFactionName().equalsIgnoreCase(selectedFaction);

        if (!factionOk) {
            return false;
        }

        if (search == null || search.isBlank()) {
            return true;
        }

        String normalizedSearch = search.trim().toLowerCase(Locale.ROOT);
        return getName().toLowerCase(Locale.ROOT).contains(normalizedSearch)
                || getDatasheetId().toLowerCase(Locale.ROOT).contains(normalizedSearch);
    }

    @Override
    public String toString() {
        return getName();
    }

    private static String safeText(String value, String fallback) {
        if (value != null && !value.isBlank()) {
            return value;
        }
        return fallback == null ? "" : fallback;
    }
}