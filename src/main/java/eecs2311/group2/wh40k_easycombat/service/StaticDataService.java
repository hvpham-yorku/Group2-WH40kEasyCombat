package eecs2311.group2.wh40k_easycombat.service;

import eecs2311.group2.wh40k_easycombat.model.Army;
import eecs2311.group2.wh40k_easycombat.model.Army_detachment;
import eecs2311.group2.wh40k_easycombat.model.Army_units;
import eecs2311.group2.wh40k_easycombat.model.Army_wargear;
import eecs2311.group2.wh40k_easycombat.model.Datasheets;
import eecs2311.group2.wh40k_easycombat.model.Datasheets_abilities;
import eecs2311.group2.wh40k_easycombat.model.Datasheets_detachment_abilities;
import eecs2311.group2.wh40k_easycombat.model.Datasheets_enhancements;
import eecs2311.group2.wh40k_easycombat.model.Datasheets_keywords;
import eecs2311.group2.wh40k_easycombat.model.Datasheets_leader;
import eecs2311.group2.wh40k_easycombat.model.Datasheets_models;
import eecs2311.group2.wh40k_easycombat.model.Datasheets_models_cost;
import eecs2311.group2.wh40k_easycombat.model.Datasheets_options;
import eecs2311.group2.wh40k_easycombat.model.Datasheets_stratagems;
import eecs2311.group2.wh40k_easycombat.model.Datasheets_unit_composition;
import eecs2311.group2.wh40k_easycombat.model.Datasheets_wargear;
import eecs2311.group2.wh40k_easycombat.repository.ArmyBundleRepository;
import eecs2311.group2.wh40k_easycombat.repository.DatasheetBundleRepository;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public final class StaticDataService {

    private StaticDataService() {
    }

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
            this.models = listOrEmpty(models);
            this.wargear = listOrEmpty(wargear);
            this.abilities = listOrEmpty(abilities);
            this.compositions = listOrEmpty(compositions);
            this.costs = listOrEmpty(costs);
            this.keywords = listOrEmpty(keywords);
            this.options = listOrEmpty(options);
            this.leaders = listOrEmpty(leaders);
            this.stratagems = listOrEmpty(stratagems);
            this.enhancements = listOrEmpty(enhancements);
            this.detachmentAbilities = listOrEmpty(detachmentAbilities);
        }
    }

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
            this.detachments = listOrEmpty(detachments);
            this.units = listOrEmpty(units);
            this.wargear = listOrEmpty(wargear);
        }
    }

    private static volatile boolean loaded = false;

    private static final Map<String, DatasheetBundle> datasheetBundlesById = new ConcurrentHashMap<>();
    private static final Map<Integer, ArmyBundle> armyBundlesById = new ConcurrentHashMap<>();
    private static final Map<Integer, List<Army_wargear>> wargearByUnitsId = new ConcurrentHashMap<>();

    public static synchronized void reloadFromSqlite() throws SQLException {
        loaded = false;
        loadAllFromSqlite();
    }

    public static synchronized void loadAllFromSqlite() throws SQLException {
        if (loaded) {
            return;
        }

        clearCache();

        try {
            for (DatasheetBundleRepository.DatasheetRecordBundle recordBundle : DatasheetBundleRepository.findAllBundles()) {
                DatasheetBundle bundle = toServiceBundle(recordBundle);
                datasheetBundlesById.put(bundle.datasheet.id(), bundle);
            }

            for (ArmyBundleRepository.ArmyRecordBundle recordBundle : ArmyBundleRepository.findAllBundles()) {
                ArmyBundle bundle = toServiceBundle(recordBundle);
                armyBundlesById.put(bundle.army.auto_id(), bundle);

                Map<Integer, List<Army_wargear>> groupedByUnitId = new HashMap<>();
                for (Army_wargear wargear : bundle.wargear) {
                    groupedByUnitId.computeIfAbsent(wargear.units_id(), ignored -> new ArrayList<>()).add(wargear);
                }

                for (Map.Entry<Integer, List<Army_wargear>> entry : groupedByUnitId.entrySet()) {
                    wargearByUnitsId.put(entry.getKey(), List.copyOf(entry.getValue()));
                }
            }

            loaded = true;
        } catch (SQLException e) {
            clearCache();
            loaded = false;
            throw e;
        }
    }

    public static DatasheetBundle getDatasheetBundle(String datasheetId) throws SQLException {
        ensureLoaded();
        return datasheetBundlesById.get(datasheetId);
    }

    public static Army getArmy(int armyId) throws SQLException {
        ensureLoaded();
        ArmyBundle bundle = armyBundlesById.get(armyId);
        return bundle == null ? null : bundle.army;
    }

    public static List<Army> getAllArmies() throws SQLException {
        ensureLoaded();

        List<Army> armies = new ArrayList<>();
        for (ArmyBundle bundle : armyBundlesById.values()) {
            armies.add(bundle.army);
        }

        armies.sort(Comparator.comparingInt(Army::auto_id));
        return List.copyOf(armies);
    }

    public static List<Army_detachment> getArmyDetachments(int armyId) throws SQLException {
        ensureLoaded();
        ArmyBundle bundle = armyBundlesById.get(armyId);
        return bundle == null ? List.of() : bundle.detachments;
    }

    public static List<Army_units> getArmyUnits(int armyId) throws SQLException {
        ensureLoaded();
        ArmyBundle bundle = armyBundlesById.get(armyId);
        return bundle == null ? List.of() : bundle.units;
    }

    public static List<Army_wargear> getArmyWargearByUnitId(int unitsId) throws SQLException {
        ensureLoaded();
        return wargearByUnitsId.getOrDefault(unitsId, List.of());
    }

    public static ArmyBundle getArmyBundle(int armyId) throws SQLException {
        ensureLoaded();
        return armyBundlesById.get(armyId);
    }

    private static void ensureLoaded() throws SQLException {
        if (!loaded) {
            loadAllFromSqlite();
        }
    }

    private static void clearCache() {
        datasheetBundlesById.clear();
        armyBundlesById.clear();
        wargearByUnitsId.clear();
    }

    private static DatasheetBundle toServiceBundle(DatasheetBundleRepository.DatasheetRecordBundle recordBundle) {
        return new DatasheetBundle(
                recordBundle.datasheet(),
                recordBundle.models(),
                recordBundle.wargear(),
                recordBundle.abilities(),
                recordBundle.compositions(),
                recordBundle.costs(),
                recordBundle.keywords(),
                recordBundle.options(),
                recordBundle.leaders(),
                recordBundle.stratagems(),
                recordBundle.enhancements(),
                recordBundle.detachmentAbilities()
        );
    }

    private static ArmyBundle toServiceBundle(ArmyBundleRepository.ArmyRecordBundle recordBundle) {
        return new ArmyBundle(
                recordBundle.army(),
                recordBundle.detachments(),
                recordBundle.units(),
                recordBundle.wargear()
        );
    }

    private static <T> List<T> listOrEmpty(List<T> rows) {
        return rows == null ? List.of() : List.copyOf(rows);
    }
}
