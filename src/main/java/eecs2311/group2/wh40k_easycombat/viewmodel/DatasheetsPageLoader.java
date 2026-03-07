package eecs2311.group2.wh40k_easycombat.viewmodel;

import eecs2311.group2.wh40k_easycombat.repository.DatasheetsRepository;
import eecs2311.group2.wh40k_easycombat.service.StaticDataService;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import static eecs2311.group2.wh40k_easycombat.util.FxReflectionHelper.getAny;
import static eecs2311.group2.wh40k_easycombat.util.FxReflectionHelper.s;

public final class DatasheetsPageLoader {

    private DatasheetsPageLoader() {
    }

    public static void loadAbilitiesMaster(DatasheetsPageState state) {
        state.getAbilitiesById().clear();

        try {
            Class<?> repo = Class.forName("eecs2311.group2.wh40k_easycombat.repository.AbilitiesRepository");
            Method m = repo.getMethod("getAllAbilities");
            Object result = m.invoke(null);

            if (result instanceof List<?> list) {
                for (Object a : list) {
                    String id = s(getAny(a, "id"));
                    if (!id.isBlank()) {
                        state.getAbilitiesById().put(id, a);
                    }
                }
            }
        } catch (Exception ignored) {
        }
    }

    public static void loadDatasheets(DatasheetsPageState state, ListView<Object> datasheetsList) throws Exception {
        StaticDataService.loadAllFromSqlite();

        List<?> list = DatasheetsRepository.getAllDatasheets();
        state.getAllDatasheets().setAll((Collection<? extends Object>) list);
        state.getFilteredDatasheets().setAll(state.getAllDatasheets());

        if (datasheetsList != null) {
            datasheetsList.setItems(state.getFilteredDatasheets());
            datasheetsList.setCellFactory(lv -> new ListCell<>() {
                @Override
                protected void updateItem(Object item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty || item == null) {
                        setText(null);
                        return;
                    }
                    String name = s(getAny(item, "name", "datasheet_name", "title"));
                    if (name.isBlank()) name = s(getAny(item, "id", "datasheet_id"));
                    setText(name);
                }
            });
        }
    }

    public static void loadFactionsIntoComboBox(
            DatasheetsPageState state,
            ComboBox<String> factionComboBox
    ) {
        if (factionComboBox == null) return;

        factionComboBox.getItems().clear();
        factionComboBox.getItems().add("ALL");
        state.getFactionNameToId().clear();

        boolean loadedFromFactionRepo = false;
        try {
            Class<?> repo = Class.forName("eecs2311.group2.wh40k_easycombat.repository.FactionsRepository");
            Method m = repo.getMethod("getAllFactions");
            Object result = m.invoke(null);
            if (result instanceof List<?> factions) {
                for (Object f : factions) {
                    String id = s(getAny(f, "id", "faction_id"));
                    String name = s(getAny(f, "name", "faction_name"));
                    if (!id.isBlank() && !name.isBlank()) {
                        state.getFactionNameToId().put(name, id);
                    }
                }
                loadedFromFactionRepo = !state.getFactionNameToId().isEmpty();
            }
        } catch (Exception ignored) {
        }

        if (loadedFromFactionRepo) {
            List<String> names = new ArrayList<>(state.getFactionNameToId().keySet());
            Collections.sort(names);
            factionComboBox.getItems().addAll(names);
            factionComboBox.setValue("ALL");
            return;
        }

        Set<String> factions = state.getAllDatasheets().stream()
                .map(d -> s(getAny(d, "faction_name", "faction", "army")))
                .filter(x -> !x.isBlank())
                .collect(java.util.stream.Collectors.toCollection(TreeSet::new));

        factionComboBox.getItems().addAll(factions);
        factionComboBox.setValue("ALL");
    }
}