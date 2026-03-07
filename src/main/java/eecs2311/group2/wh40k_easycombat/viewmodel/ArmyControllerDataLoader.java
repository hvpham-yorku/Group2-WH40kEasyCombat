package eecs2311.group2.wh40k_easycombat.viewmodel;

import eecs2311.group2.wh40k_easycombat.controller.ArmyController;
import eecs2311.group2.wh40k_easycombat.model.Army;
import eecs2311.group2.wh40k_easycombat.model.Datasheets;
import eecs2311.group2.wh40k_easycombat.model.Datasheets_models_cost;
import eecs2311.group2.wh40k_easycombat.service.StaticDataService;
import eecs2311.group2.wh40k_easycombat.util.CostParser;
import eecs2311.group2.wh40k_easycombat.util.CostTier;
import javafx.scene.control.TreeItem;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

public final class ArmyControllerDataLoader {

    private ArmyControllerDataLoader() {
    }

    public static FactionDisplayData loadFactionDisplayData() {
        LinkedHashMap<String, String> displayToId = new LinkedHashMap<>();
        LinkedHashMap<String, String> idToDisplay = new LinkedHashMap<>();

        List<?> factionRows = loadAllRecords(
                "eecs2311.group2.wh40k_easycombat.repository.FactionsRepository"
        );

        if (!factionRows.isEmpty()) {
            for (Object obj : factionRows) {
                String id = callString(obj, "id", "faction_id");
                String name = callString(obj, "name", "faction");
                if (id == null || id.isBlank()) continue;

                String display = (name == null || name.isBlank()) ? id : name;
                displayToId.put(display, id);
                idToDisplay.put(id, display);
            }
        } else {
            List<Datasheets> allDatasheets = castList(loadAllRecords(
                    "eecs2311.group2.wh40k_easycombat.repository.DatasheetsRepository"
            ), Datasheets.class);

            for (Datasheets d : allDatasheets) {
                if (d == null || d.faction_id() == null || d.faction_id().isBlank()) continue;
                if ("all".equalsIgnoreCase(d.faction_id())) continue;

                String id = d.faction_id().trim();
                displayToId.putIfAbsent(id, id);
                idToDisplay.putIfAbsent(id, id);
            }
        }

        return new FactionDisplayData(displayToId, idToDisplay);
    }

    public static Map<String, ArmyUnitVM.EnhancementEntry> loadEnhancementInfoById() {
        Map<String, ArmyUnitVM.EnhancementEntry> map = new LinkedHashMap<>();

        List<?> enhancementRows = loadAllRecords(
                "eecs2311.group2.wh40k_easycombat.repository.EnhancementsRepository"
        );

        for (Object obj : enhancementRows) {
            String id = callString(obj, "id", "enhancement_id");
            String name = callString(obj, "name", "title", "enhancement");
            int cost = callInt(obj, "cost", "points", "pts");
            String detachmentId = callString(obj, "detachment_id");
            String factionId = callString(obj, "faction_id");

            if (id != null && !id.isBlank()) {
                map.put(id, new ArmyUnitVM.EnhancementEntry(
                        id,
                        (name == null || name.isBlank()) ? id : name,
                        cost,
                        detachmentId == null ? "" : detachmentId,
                        factionId == null ? "" : factionId
                ));
            }
        }

        return map;
    }

    public static DetachmentDisplayData loadDetachmentDisplayData(String factionId) {
        LinkedHashMap<String, String> displayToId = new LinkedHashMap<>();
        LinkedHashMap<String, String> idToDisplay = new LinkedHashMap<>();
        List<Object> records = new ArrayList<>();

        List<?> detachments = loadAllRecords(
                "eecs2311.group2.wh40k_easycombat.repository.DetachmentsRepository"
        );

        for (Object obj : detachments) {
            String id = callString(obj, "id");
            String name = callString(obj, "name");
            String rowFactionId = callString(obj, "faction_id");

            if (id == null || id.isBlank()) continue;
            if (factionId != null && !factionId.isBlank()
                    && rowFactionId != null && !rowFactionId.isBlank()
                    && !factionId.equalsIgnoreCase(rowFactionId)) {
                continue;
            }

            String display = (name == null || name.isBlank()) ? id : name;
            displayToId.put(display, id);
            idToDisplay.put(id, display);
            records.add(obj);
        }

        return new DetachmentDisplayData(displayToId, idToDisplay, records);
    }

    public static List<ArmyController.SavedArmyRow> loadSavedArmyRows() {
        List<ArmyController.SavedArmyRow> rows = new ArrayList<>();

        try {
            for (Army a : StaticDataService.getAllArmies()) {
                rows.add(new ArmyController.SavedArmyRow(
                        a.auto_id(),
                        a.name(),
                        a.total_points(),
                        a.isMarked()
                ));
            }
        } catch (Exception ignored) {
        }

        rows.sort(Comparator
                .comparing(ArmyController.SavedArmyRow::marked).reversed()
                .thenComparing(ArmyController.SavedArmyRow::armyName, String.CASE_INSENSITIVE_ORDER));

        return rows;
    }

    public static TreeItem<ArmyController.UnitTreeRow> buildUnitTree(
            String factionId,
            Function<SimpleUnitTreeRow, ArmyController.UnitTreeRow> unitMapper,
            Function<String, ArmyController.UnitTreeRow> groupMapper
    ) {
        TreeItem<ArmyController.UnitTreeRow> root = new TreeItem<>(groupMapper.apply("ROOT"));
        root.setExpanded(true);

        if (factionId == null || factionId.isBlank()) {
            return root;
        }

        List<Datasheets> allDatasheets = castList(loadAllRecords(
                "eecs2311.group2.wh40k_easycombat.repository.DatasheetsRepository"
        ), Datasheets.class);

        LinkedHashMap<String, List<SimpleUnitTreeRow>> grouped = new LinkedHashMap<>();

        for (Datasheets d : allDatasheets) {
            if (d == null) continue;
            if (!factionId.equalsIgnoreCase(d.faction_id())) continue;

            try {
                StaticDataService.DatasheetBundle bundle = StaticDataService.getDatasheetBundle(d.id());
                if (bundle == null) continue;

                int minPoints = basePoints(bundle.costs);
                String role = normalizeRole(d.role());

                grouped.computeIfAbsent(role, k -> new ArrayList<>())
                        .add(new SimpleUnitTreeRow(
                                d.name() == null || d.name().isBlank() ? d.id() : d.name(),
                                d.id(),
                                role,
                                minPoints
                        ));
            } catch (Exception ignored) {
            }
        }

        for (var e : grouped.entrySet()) {
            e.getValue().sort(Comparator.comparing(SimpleUnitTreeRow::name, String.CASE_INSENSITIVE_ORDER));

            TreeItem<ArmyController.UnitTreeRow> roleNode = new TreeItem<>(groupMapper.apply(e.getKey()));
            roleNode.setExpanded(true);

            for (SimpleUnitTreeRow row : e.getValue()) {
                roleNode.getChildren().add(new TreeItem<>(unitMapper.apply(row)));
            }

            root.getChildren().add(roleNode);
        }

        return root;
    }

    public record FactionDisplayData(
            LinkedHashMap<String, String> displayToId,
            LinkedHashMap<String, String> idToDisplay
    ) {
    }

    public record DetachmentDisplayData(
            LinkedHashMap<String, String> displayToId,
            LinkedHashMap<String, String> idToDisplay,
            List<Object> records
    ) {
    }

    public record SimpleUnitTreeRow(String name, String datasheetId, String role, int points) {
    }

    private static int basePoints(List<Datasheets_models_cost> costs) {
        List<CostTier> tiers = CostParser.parseTiers(costs);
        return tiers.isEmpty() ? 0 : tiers.get(0).points();
    }

    private static String normalizeRole(String role) {
        return (role == null || role.isBlank()) ? "Other" : role.trim();
    }

    @SuppressWarnings("unchecked")
    private static <T> List<T> castList(List<?> src, Class<T> type) {
        List<T> out = new ArrayList<>();
        for (Object o : src) {
            if (type.isInstance(o)) out.add((T) o);
        }
        return out;
    }

    private static List<?> loadAllRecords(String repositoryClassName) {
        try {
            Class<?> repoClass = Class.forName(repositoryClassName);

            for (Method m : repoClass.getDeclaredMethods()) {
                if (m.getParameterCount() == 0
                        && java.lang.reflect.Modifier.isStatic(m.getModifiers())
                        && List.class.isAssignableFrom(m.getReturnType())
                        && m.getName().startsWith("getAll")) {
                    Object result = m.invoke(null);
                    if (result instanceof List<?>) {
                        return (List<?>) result;
                    }
                }
            }
        } catch (Exception ignored) {
        }

        return List.of();
    }

    private static String callString(Object obj, String... methodNames) {
        if (obj == null) return null;

        for (String name : methodNames) {
            try {
                Method m = obj.getClass().getMethod(name);
                Object value = m.invoke(obj);
                if (value != null) return String.valueOf(value);
            } catch (Exception ignored) {
            }
        }
        return null;
    }

    private static int callInt(Object obj, String... methodNames) {
        for (String name : methodNames) {
            try {
                Method m = obj.getClass().getMethod(name);
                Object value = m.invoke(obj);
                return Integer.parseInt(String.valueOf(value).trim());
            } catch (Exception ignored) {
            }
        }
        return 0;
    }
}