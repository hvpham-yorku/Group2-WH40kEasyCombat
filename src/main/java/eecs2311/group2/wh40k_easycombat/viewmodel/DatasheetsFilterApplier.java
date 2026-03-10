package eecs2311.group2.wh40k_easycombat.viewmodel;

import java.util.stream.Collectors;

import static eecs2311.group2.wh40k_easycombat.util.FxReflectionHelper.getAny;
import static eecs2311.group2.wh40k_easycombat.util.FxReflectionHelper.s;

public final class DatasheetsFilterApplier {

    private DatasheetsFilterApplier() {
    }

    public static void applyFilters(
            DatasheetsPageState state,
            String keyword,
            String selectedFaction
    ) {
        String search = keyword == null ? "" : keyword.trim().toLowerCase();
        String faction = selectedFaction == null ? "ALL" : selectedFaction;
        String selectedFactionId = state.getFactionNameToId().getOrDefault(faction, faction);

        state.getFilteredDatasheets().setAll(
                state.getAllDatasheets().stream()
                        .filter(d -> {
                            if (!"ALL".equalsIgnoreCase(faction)) {
                                String fName = s(getAny(d, "faction_name", "faction", "army"));
                                String fId = s(getAny(d, "faction_id"));

                                boolean match = false;
                                if (!fId.isBlank()) match = fId.equalsIgnoreCase(selectedFactionId);
                                if (!match && !fName.isBlank()) match = fName.equalsIgnoreCase(faction);

                                if (!match) return false;
                            }

                            if (!search.isBlank()) {
                                String name = s(getAny(d, "name", "datasheet_name", "title")).toLowerCase();
                                String id = s(getAny(d, "id", "datasheet_id")).toLowerCase();
                                return name.contains(search) || id.contains(search);
                            }

                            return true;
                        })
                        .collect(Collectors.toList())
        );
    }
}