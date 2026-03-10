package eecs2311.group2.wh40k_easycombat.controller.helper;

import eecs2311.group2.wh40k_easycombat.model.Abilities;
import eecs2311.group2.wh40k_easycombat.model.Datasheets;
import eecs2311.group2.wh40k_easycombat.model.Detachment_abilities;
import eecs2311.group2.wh40k_easycombat.model.Enhancements;
import eecs2311.group2.wh40k_easycombat.model.Factions;
import eecs2311.group2.wh40k_easycombat.model.Stratagems;
import eecs2311.group2.wh40k_easycombat.repository.AbilitiesRepository;
import eecs2311.group2.wh40k_easycombat.repository.DatasheetsRepository;
import eecs2311.group2.wh40k_easycombat.repository.Detachment_abilitiesRepository;
import eecs2311.group2.wh40k_easycombat.repository.EnhancementsRepository;
import eecs2311.group2.wh40k_easycombat.repository.FactionsRepository;
import eecs2311.group2.wh40k_easycombat.repository.StratagemsRepository;
import eecs2311.group2.wh40k_easycombat.service.StaticDataService;
import eecs2311.group2.wh40k_easycombat.viewmodel.DatasheetListItemVM;
import eecs2311.group2.wh40k_easycombat.viewmodel.DatasheetsPageState;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.TreeSet;

public final class DatasheetsPageControllerHelper {

    private DatasheetsPageControllerHelper() {
    }

    public static void loadPageData(
            DatasheetsPageState state,
            ListView<DatasheetListItemVM> datasheetsList,
            ComboBox<String> factionComboBox
    ) throws Exception {
        StaticDataService.loadAllFromSqlite();

        loadAbilitiesMaster(state);
        loadDetachmentAbilitiesMaster(state);
        loadStratagemsMaster(state);
        loadEnhancementsMaster(state);

        LinkedHashMap<String, String> factionIdToName = loadFactionMaps(state);
        loadDatasheets(state, datasheetsList, factionIdToName);
        loadFactionsIntoComboBox(state, factionComboBox);
    }

    public static boolean applyFilters(
            DatasheetsPageState state,
            ListView<DatasheetListItemVM> datasheetsList,
            String keyword,
            String selectedFaction
    ) {
        if (state == null) {
            return false;
        }

        state.applyFilters(keyword, selectedFaction);

        if (datasheetsList != null) {
            datasheetsList.setItems(state.getFilteredDatasheets());

            if (!state.getFilteredDatasheets().isEmpty()) {
                datasheetsList.getSelectionModel().select(0);
                return true;
            }

            datasheetsList.getSelectionModel().clearSelection();
        }

        return !state.getFilteredDatasheets().isEmpty();
    }

    private static void loadAbilitiesMaster(DatasheetsPageState state) throws SQLException {
        state.getAbilitiesById().clear();

        for (Abilities ability : AbilitiesRepository.getAllAbilities()) {
            if (ability != null && ability.id() != null && !ability.id().isBlank()) {
                state.getAbilitiesById().put(ability.id(), ability);
            }
        }
    }

    private static void loadDetachmentAbilitiesMaster(DatasheetsPageState state) throws SQLException {
        state.getDetachmentAbilitiesById().clear();

        for (Detachment_abilities ability : Detachment_abilitiesRepository.getAllDetachment_abilities()) {
            if (ability != null && ability.id() != null && !ability.id().isBlank()) {
                state.getDetachmentAbilitiesById().put(ability.id(), ability);
            }
        }
    }

    private static void loadStratagemsMaster(DatasheetsPageState state) throws SQLException {
        state.getStratagemsById().clear();

        for (Stratagems stratagem : StratagemsRepository.getAllStratagems()) {
            if (stratagem != null && stratagem.id() != null && !stratagem.id().isBlank()) {
                state.getStratagemsById().put(stratagem.id(), stratagem);
            }
        }
    }

    private static void loadEnhancementsMaster(DatasheetsPageState state) throws SQLException {
        state.getEnhancementsById().clear();

        for (Enhancements enhancement : EnhancementsRepository.getAllEnhancements()) {
            if (enhancement != null && enhancement.id() != null && !enhancement.id().isBlank()) {
                state.getEnhancementsById().put(enhancement.id(), enhancement);
            }
        }
    }

    private static LinkedHashMap<String, String> loadFactionMaps(DatasheetsPageState state) throws SQLException {
        state.getFactionNameToId().clear();

        LinkedHashMap<String, String> factionIdToName = new LinkedHashMap<>();
        for (Factions faction : FactionsRepository.getAllFactions()) {
            if (faction == null || faction.id() == null || faction.id().isBlank()) {
                continue;
            }

            String display = faction.name() == null || faction.name().isBlank()
                    ? faction.id()
                    : faction.name();

            state.getFactionNameToId().put(display, faction.id());
            factionIdToName.put(faction.id(), display);
        }

        return factionIdToName;
    }

    private static void loadDatasheets(
            DatasheetsPageState state,
            ListView<DatasheetListItemVM> datasheetsList,
            LinkedHashMap<String, String> factionIdToName
    ) throws SQLException {
        List<DatasheetListItemVM> items = new ArrayList<>();

        for (Datasheets datasheet : DatasheetsRepository.getAllDatasheets()) {
            if (datasheet == null) continue;

            String factionName = factionIdToName.getOrDefault(
                    datasheet.faction_id(),
                    datasheet.faction_id() == null ? "" : datasheet.faction_id()
            );

            items.add(new DatasheetListItemVM(datasheet, factionName));
        }

        items.sort(Comparator.comparing(DatasheetListItemVM::getName, String.CASE_INSENSITIVE_ORDER));
        state.replaceDatasheets(items);

        if (datasheetsList != null) {
            datasheetsList.setItems(state.getFilteredDatasheets());
            datasheetsList.setCellFactory(lv -> new ListCell<>() {
                @Override
                protected void updateItem(DatasheetListItemVM item, boolean empty) {
                    super.updateItem(item, empty);
                    setText(empty || item == null ? null : item.getName());
                }
            });
        }
    }

    private static void loadFactionsIntoComboBox(
            DatasheetsPageState state,
            ComboBox<String> factionComboBox
    ) {
        if (factionComboBox == null) {
            return;
        }

        if (state.getFactionNameToId().isEmpty()) {
            for (DatasheetListItemVM item : state.getAllDatasheets()) {
                String factionName = item.getFactionName();
                if (!factionName.isBlank()) {
                    state.getFactionNameToId().putIfAbsent(factionName, item.getFactionId());
                }
            }
        }

        factionComboBox.getItems().clear();
        factionComboBox.getItems().add("ALL");
        factionComboBox.getItems().addAll(new TreeSet<>(state.getFactionNameToId().keySet()));
        factionComboBox.setValue("ALL");
    }
}
