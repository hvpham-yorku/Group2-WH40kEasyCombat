package eecs2311.group2.wh40k_easycombat.service;

import eecs2311.group2.wh40k_easycombat.model.*;
import eecs2311.group2.wh40k_easycombat.repository.*;

import java.sql.SQLException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public final class StaticDataService {

    private StaticDataService() {}

    // =========================
    // Datasheet Bundle
    // =========================
    public static final class DatasheetBundle {
        public final Datasheets datasheet;

        public final List<Datasheets_models> models;
        public final List<Datasheets_wargear> wargear;
        public final List<Datasheets_abilities> abilities;
        public final List<Datasheets_unit_composition> compositions;
        public final List<Datasheets_models_cost> costs;
        public final List<Datasheets_keywords> keywords;

        public final List<Datasheets_options> options;
        public final List<Datasheets_leader> leaders;
        public final List<Datasheets_stratagems> stratagems;
        public final List<Datasheets_enhancements> enhancements;
        public final List<Datasheets_detachment_abilities> detachmentAbilities;

        private DatasheetBundle(
                Datasheets datasheet,
                List<Datasheets_models> models,
                List<Datasheets_wargear> wargear,
                List<Datasheets_abilities> abilities,
                List<Datasheets_unit_composition> compositions,
                List<Datasheets_models_cost> costs,
                List<Datasheets_keywords> keywords,
                List<Datasheets_options> options,
                List<Datasheets_leader> leaders,
                List<Datasheets_stratagems> stratagems,
                List<Datasheets_enhancements> enhancements,
                List<Datasheets_detachment_abilities> detachmentAbilities
        ) {
            this.datasheet = datasheet;
            this.models = models;
            this.wargear = wargear;
            this.abilities = abilities;
            this.compositions = compositions;
            this.costs = costs;
            this.keywords = keywords;
            this.options = options;
            this.leaders = leaders;
            this.stratagems = stratagems;
            this.enhancements = enhancements;
            this.detachmentAbilities = detachmentAbilities;
        }
    }

    // =========================
    // Army Bundle
    // =========================
    public static final class ArmyBundle {
        public final Army army;
        public final List<Army_detachment> detachments;
        public final List<Army_units> units;
        public final List<Army_wargear> wargear;

        private ArmyBundle(
                Army army,
                List<Army_detachment> detachments,
                List<Army_units> units,
                List<Army_wargear> wargear
        ) {
            this.army = army;
            this.detachments = detachments;
            this.units = units;
            this.wargear = wargear;
        }
    }

    private static volatile boolean loaded = false;

    // =========================
    // Datasheet caches
    // =========================
    private static final Map<String, Datasheets> datasheetsById = new ConcurrentHashMap<>();

    private static final Map<String, List<Datasheets_models>> modelsByDatasheetId = new ConcurrentHashMap<>();
    private static final Map<String, List<Datasheets_wargear>> wargearByDatasheetId = new ConcurrentHashMap<>();
    private static final Map<String, List<Datasheets_abilities>> abilitiesByDatasheetId = new ConcurrentHashMap<>();
    private static final Map<String, List<Datasheets_unit_composition>> compositionByDatasheetId = new ConcurrentHashMap<>();
    private static final Map<String, List<Datasheets_models_cost>> costsByDatasheetId = new ConcurrentHashMap<>();
    private static final Map<String, List<Datasheets_keywords>> keywordsByDatasheetId = new ConcurrentHashMap<>();

    private static final Map<String, List<Datasheets_options>> optionsByDatasheetId = new ConcurrentHashMap<>();
    private static final Map<String, List<Datasheets_leader>> leadersByDatasheetId = new ConcurrentHashMap<>();
    private static final Map<String, List<Datasheets_stratagems>> stratagemsByDatasheetId = new ConcurrentHashMap<>();
    private static final Map<String, List<Datasheets_enhancements>> enhancementsByDatasheetId = new ConcurrentHashMap<>();
    private static final Map<String, List<Datasheets_detachment_abilities>> detachmentAbilitiesByDatasheetId = new ConcurrentHashMap<>();

    // =========================
    // Army caches
    // =========================
    private static final Map<Integer, Army> armyById = new ConcurrentHashMap<>();
    private static final Map<Integer, List<Army_detachment>> detachmentsByArmyId = new ConcurrentHashMap<>();
    private static final Map<Integer, List<Army_units>> unitsByArmyId = new ConcurrentHashMap<>();
    private static final Map<Integer, List<Army_wargear>> wargearByUnitsId = new ConcurrentHashMap<>();

    private static void clearCache() {
        // datasheets
        datasheetsById.clear();

        modelsByDatasheetId.clear();
        wargearByDatasheetId.clear();
        abilitiesByDatasheetId.clear();
        compositionByDatasheetId.clear();
        costsByDatasheetId.clear();
        keywordsByDatasheetId.clear();

        optionsByDatasheetId.clear();
        leadersByDatasheetId.clear();
        stratagemsByDatasheetId.clear();
        enhancementsByDatasheetId.clear();
        detachmentAbilitiesByDatasheetId.clear();

        // armies
        armyById.clear();
        detachmentsByArmyId.clear();
        unitsByArmyId.clear();
        wargearByUnitsId.clear();
    }

    public static synchronized void reloadFromSqlite() throws SQLException {
        loaded = false;
        clearCache();
        loadAllFromSqlite();
    }

    public static synchronized void loadAllFromSqlite() throws SQLException {
        if (loaded) return;

        // =========================
        // Datasheets
        // =========================

        for (Datasheets d : DatasheetsRepository.getAllDatasheets()) {
            if (d != null && d.id() != null) datasheetsById.put(d.id(), d);
        }

        {
            Map<String, List<Datasheets_models>> tmp = new HashMap<>();
            for (Datasheets_models m : Datasheets_modelsRepository.getAllDatasheets_models()) {
                if (m == null || m.datasheet_id() == null) continue;
                tmp.computeIfAbsent(m.datasheet_id(), k -> new ArrayList<>()).add(m);
            }
            for (var e : tmp.entrySet()) {
                e.getValue().sort(Comparator.comparingInt(x -> safeLineInt(x.line())));
                modelsByDatasheetId.put(e.getKey(), Collections.unmodifiableList(e.getValue()));
            }
        }

        {
            Map<String, List<Datasheets_wargear>> tmp = new HashMap<>();
            for (Datasheets_wargear w : Datasheets_wargearRepository.getAllDatasheets_wargear()) {
                if (w == null || w.datasheet_id() == null) continue;
                tmp.computeIfAbsent(w.datasheet_id(), k -> new ArrayList<>()).add(w);
            }
            for (var e : tmp.entrySet()) {
                e.getValue().sort(
                        Comparator.comparingInt((Datasheets_wargear x) -> safeLineInt(x.line()))
                                .thenComparingInt(x -> safeLineInt(x.line_in_wargear()))
                );
                wargearByDatasheetId.put(e.getKey(), Collections.unmodifiableList(e.getValue()));
            }
        }

        {
            Map<String, List<Datasheets_abilities>> tmp = new HashMap<>();
            for (Datasheets_abilities a : Datasheets_abilitiesRepository.getAllDatasheets_abilities()) {
                if (a == null || a.datasheet_id() == null) continue;
                tmp.computeIfAbsent(a.datasheet_id(), k -> new ArrayList<>()).add(a);
            }
            for (var e : tmp.entrySet()) {
                e.getValue().sort(Comparator.comparingInt(x -> safeLineInt(x.line())));
                abilitiesByDatasheetId.put(e.getKey(), Collections.unmodifiableList(e.getValue()));
            }
        }

        {
            Map<String, List<Datasheets_unit_composition>> tmp = new HashMap<>();
            for (Datasheets_unit_composition c : Datasheets_unit_compositionRepository.getAllDatasheets_unit_composition()) {
                if (c == null || c.datasheet_id() == null) continue;
                tmp.computeIfAbsent(c.datasheet_id(), k -> new ArrayList<>()).add(c);
            }
            for (var e : tmp.entrySet()) {
                e.getValue().sort(Comparator.comparingInt(x -> safeLineInt(x.line())));
                compositionByDatasheetId.put(e.getKey(), Collections.unmodifiableList(e.getValue()));
            }
        }

        {
            Map<String, List<Datasheets_models_cost>> tmp = new HashMap<>();
            for (Datasheets_models_cost c : Datasheets_models_costRepository.getAllDatasheets_models_cost()) {
                if (c == null || c.datasheet_id() == null) continue;
                tmp.computeIfAbsent(c.datasheet_id(), k -> new ArrayList<>()).add(c);
            }
            for (var e : tmp.entrySet()) {
                e.getValue().sort(Comparator.comparingInt(x -> safeLineInt(x.line())));
                costsByDatasheetId.put(e.getKey(), Collections.unmodifiableList(e.getValue()));
            }
        }

        try {
            Map<String, List<Datasheets_keywords>> tmp = new HashMap<>();
            for (Datasheets_keywords k : Datasheets_keywordsRepository.getAllDatasheets_keywords()) {
                if (k == null || k.datasheet_id() == null) continue;
                tmp.computeIfAbsent(k.datasheet_id(), x -> new ArrayList<>()).add(k);
            }
            for (var e : tmp.entrySet()) {
                e.getValue().sort(
                        Comparator.comparing(Datasheets_keywords::keyword, Comparator.nullsLast(String::compareToIgnoreCase))
                                .thenComparing(Datasheets_keywords::model, Comparator.nullsLast(String::compareToIgnoreCase))
                );
                keywordsByDatasheetId.put(e.getKey(), Collections.unmodifiableList(e.getValue()));
            }
        } catch (Exception ignored) {}

        try {
            Map<String, List<Datasheets_options>> tmp = new HashMap<>();
            for (Datasheets_options o : Datasheets_optionsRepository.getAllDatasheets_options()) {
                if (o == null || o.datasheet_id() == null) continue;
                tmp.computeIfAbsent(o.datasheet_id(), k -> new ArrayList<>()).add(o);
            }
            for (var e : tmp.entrySet()) {
                e.getValue().sort(Comparator.comparingInt(x -> safeLineInt(x.line())));
                optionsByDatasheetId.put(e.getKey(), Collections.unmodifiableList(e.getValue()));
            }
        } catch (Exception ignored) {}

        try {
            Map<String, List<Datasheets_leader>> tmp = new HashMap<>();
            for (Datasheets_leader l : Datasheets_leaderRepository.getAllDatasheets_leader()) {
                if (l == null || l.attached_id() == null) continue;
                tmp.computeIfAbsent(l.attached_id(), k -> new ArrayList<>()).add(l);
            }
            for (var e : tmp.entrySet()) {
                leadersByDatasheetId.put(e.getKey(), Collections.unmodifiableList(e.getValue()));
            }
        } catch (Exception ignored) {}

        try {
            Map<String, List<Datasheets_stratagems>> tmp = new HashMap<>();
            for (Datasheets_stratagems s : Datasheets_stratagemsRepository.getAllDatasheets_stratagems()) {
                if (s == null || s.datasheet_id() == null) continue;
                tmp.computeIfAbsent(s.datasheet_id(), k -> new ArrayList<>()).add(s);
            }
            for (var e : tmp.entrySet()) {
                stratagemsByDatasheetId.put(e.getKey(), Collections.unmodifiableList(e.getValue()));
            }
        } catch (Exception ignored) {}

        try {
            Map<String, List<Datasheets_enhancements>> tmp = new HashMap<>();
            for (Datasheets_enhancements x : Datasheets_enhancementsRepository.getAllDatasheets_enhancements()) {
                if (x == null || x.datasheet_id() == null) continue;
                tmp.computeIfAbsent(x.datasheet_id(), k -> new ArrayList<>()).add(x);
            }
            for (var e : tmp.entrySet()) {
                enhancementsByDatasheetId.put(e.getKey(), Collections.unmodifiableList(e.getValue()));
            }
        } catch (Exception ignored) {}

        try {
            Map<String, List<Datasheets_detachment_abilities>> tmp = new HashMap<>();
            for (Datasheets_detachment_abilities x : Datasheets_detachment_abilitiesRepository.getAllDatasheets_detachment_abilities()) {
                if (x == null || x.datasheet_id() == null) continue;
                tmp.computeIfAbsent(x.datasheet_id(), k -> new ArrayList<>()).add(x);
            }
            for (var e : tmp.entrySet()) {
                detachmentAbilitiesByDatasheetId.put(e.getKey(), Collections.unmodifiableList(e.getValue()));
            }
        } catch (Exception ignored) {}

        // =========================
        // Army
        // =========================

        try {
            for (Army a : ArmyRepository.getAllArmy()) {
                if (a != null) armyById.put(a.auto_id(), a);
            }
        } catch (Exception ignored) {}

        try {
            Map<Integer, List<Army_detachment>> tmp = new HashMap<>();
            for (Army_detachment d : Army_detachmentRepository.getAllArmy_detachment()) {
                if (d == null) continue;
                tmp.computeIfAbsent(d.army_id(), k -> new ArrayList<>()).add(d);
            }
            for (var e : tmp.entrySet()) {
                e.getValue().sort(Comparator.comparingInt(Army_detachment::auto_id));
                detachmentsByArmyId.put(e.getKey(), Collections.unmodifiableList(e.getValue()));
            }
        } catch (Exception ignored) {}

        try {
            Map<Integer, List<Army_units>> tmp = new HashMap<>();
            for (Army_units u : Army_unitsRepository.getAllArmy_units()) {
                if (u == null) continue;
                tmp.computeIfAbsent(u.army_id(), k -> new ArrayList<>()).add(u);
            }
            for (var e : tmp.entrySet()) {
                e.getValue().sort(Comparator.comparingInt(Army_units::auto_id));
                unitsByArmyId.put(e.getKey(), Collections.unmodifiableList(e.getValue()));
            }
        } catch (Exception ignored) {}

        try {
            Map<Integer, List<Army_wargear>> tmp = new HashMap<>();
            for (Army_wargear w : Army_wargearRepository.getAllArmy_wargear()) {
                if (w == null) continue;
                tmp.computeIfAbsent(w.units_id(), k -> new ArrayList<>()).add(w);
            }
            for (var e : tmp.entrySet()) {
                e.getValue().sort(Comparator.comparingInt(Army_wargear::auto_id));
                wargearByUnitsId.put(e.getKey(), Collections.unmodifiableList(e.getValue()));
            }
        } catch (Exception ignored) {}

        loaded = true;
    }

    // =========================
    // Datasheet APIs
    // =========================

    public static DatasheetBundle getDatasheetBundle(String datasheetId) throws SQLException {
        if (!loaded) loadAllFromSqlite();

        Datasheets d = datasheetsById.get(datasheetId);
        if (d == null) return null;

        return new DatasheetBundle(
                d,
                modelsByDatasheetId.getOrDefault(datasheetId, List.of()),
                wargearByDatasheetId.getOrDefault(datasheetId, List.of()),
                abilitiesByDatasheetId.getOrDefault(datasheetId, List.of()),
                compositionByDatasheetId.getOrDefault(datasheetId, List.of()),
                costsByDatasheetId.getOrDefault(datasheetId, List.of()),
                keywordsByDatasheetId.getOrDefault(datasheetId, List.of()),
                optionsByDatasheetId.getOrDefault(datasheetId, List.of()),
                leadersByDatasheetId.getOrDefault(datasheetId, List.of()),
                stratagemsByDatasheetId.getOrDefault(datasheetId, List.of()),
                enhancementsByDatasheetId.getOrDefault(datasheetId, List.of()),
                detachmentAbilitiesByDatasheetId.getOrDefault(datasheetId, List.of())
        );
    }

    // =========================
    // Army APIs
    // =========================

    public static Army getArmy(int armyId) throws SQLException {
        if (!loaded) loadAllFromSqlite();
        return armyById.get(armyId);
    }

    public static List<Army> getAllArmies() throws SQLException {
        if (!loaded) loadAllFromSqlite();
        List<Army> list = new ArrayList<>(armyById.values());
        list.sort(Comparator.comparingInt(Army::auto_id));
        return Collections.unmodifiableList(list);
    }

    public static List<Army_detachment> getArmyDetachments(int armyId) throws SQLException {
        if (!loaded) loadAllFromSqlite();
        return detachmentsByArmyId.getOrDefault(armyId, List.of());
    }

    public static List<Army_units> getArmyUnits(int armyId) throws SQLException {
        if (!loaded) loadAllFromSqlite();
        return unitsByArmyId.getOrDefault(armyId, List.of());
    }

    public static List<Army_wargear> getArmyWargearByUnitId(int unitsId) throws SQLException {
        if (!loaded) loadAllFromSqlite();
        return wargearByUnitsId.getOrDefault(unitsId, List.of());
    }

    public static ArmyBundle getArmyBundle(int armyId) throws SQLException {
        if (!loaded) loadAllFromSqlite();

        Army army = armyById.get(armyId);
        if (army == null) return null;

        List<Army_detachment> detachments = detachmentsByArmyId.getOrDefault(armyId, List.of());
        List<Army_units> units = unitsByArmyId.getOrDefault(armyId, List.of());

        List<Army_wargear> flatWargear = new ArrayList<>();
        for (Army_units u : units) {
            flatWargear.addAll(wargearByUnitsId.getOrDefault(u.auto_id(), List.of()));
        }
        flatWargear.sort(Comparator.comparingInt(Army_wargear::auto_id));

        return new ArmyBundle(
                army,
                detachments,
                units,
                Collections.unmodifiableList(flatWargear)
        );
    }

    // =========================
    // utils
    // =========================

    private static int safeLineInt(String s) {
        if (s == null) return Integer.MAX_VALUE;
        try {
            return Integer.parseInt(s.trim());
        } catch (Exception e) {
            return Integer.MAX_VALUE;
        }
    }
}