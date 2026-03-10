package eecs2311.group2.wh40k_easycombat.service;

import eecs2311.group2.wh40k_easycombat.aggregate.ArmyAggregate;
import eecs2311.group2.wh40k_easycombat.aggregate.DatasheetAggregate;
import eecs2311.group2.wh40k_easycombat.model.Army;
import eecs2311.group2.wh40k_easycombat.model.Army_detachment;
import eecs2311.group2.wh40k_easycombat.model.Army_units;
import eecs2311.group2.wh40k_easycombat.model.Army_wargear;
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

    private static volatile boolean loaded = false;

    private static final Map<String, DatasheetAggregate> datasheetBundlesById = new ConcurrentHashMap<>();
    private static final Map<Integer, ArmyAggregate> armyBundlesById = new ConcurrentHashMap<>();
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
                DatasheetAggregate bundle = toDatasheetAggregate(recordBundle);
                datasheetBundlesById.put(bundle.datasheet.id(), bundle);
            }

            for (ArmyBundleRepository.ArmyRecordBundle recordBundle : ArmyBundleRepository.findAllBundles()) {
                ArmyAggregate bundle = toArmyAggregate(recordBundle);
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

    public static void saveBundle(DatasheetAggregate bundle) throws SQLException {
        if (bundle == null || bundle.datasheet == null || bundle.datasheet.id() == null) {
            throw new IllegalArgumentException("bundle/datasheet/id must not be null");
        }

        DatasheetBundleRepository.saveBundle(
                new DatasheetBundleRepository.DatasheetRecordBundle(
                        bundle.datasheet,
                        bundle.models,
                        bundle.wargear,
                        bundle.abilities,
                        bundle.compositions,
                        bundle.costs,
                        bundle.keywords,
                        bundle.options,
                        bundle.leaders,
                        bundle.stratagems,
                        bundle.enhancements,
                        bundle.detachmentAbilities
                )
        );

        reloadFromSqlite();
    }

    public static DatasheetAggregate getDatasheetBundle(String datasheetId) throws SQLException {
        ensureLoaded();
        return datasheetBundlesById.get(datasheetId);
    }

    public static Army getArmy(int armyId) throws SQLException {
        ensureLoaded();
        ArmyAggregate bundle = armyBundlesById.get(armyId);
        return bundle == null ? null : bundle.army;
    }

    public static List<Army> getAllArmies() throws SQLException {
        ensureLoaded();

        List<Army> armies = new ArrayList<>();
        for (ArmyAggregate bundle : armyBundlesById.values()) {
            armies.add(bundle.army);
        }

        armies.sort(Comparator.comparingInt(Army::auto_id));
        return List.copyOf(armies);
    }

    public static List<Army_detachment> getArmyDetachments(int armyId) throws SQLException {
        ensureLoaded();
        ArmyAggregate bundle = armyBundlesById.get(armyId);
        return bundle == null ? List.of() : bundle.detachments;
    }

    public static List<Army_units> getArmyUnits(int armyId) throws SQLException {
        ensureLoaded();
        ArmyAggregate bundle = armyBundlesById.get(armyId);
        return bundle == null ? List.of() : bundle.units;
    }

    public static List<Army_wargear> getArmyWargearByUnitId(int unitsId) throws SQLException {
        ensureLoaded();
        return wargearByUnitsId.getOrDefault(unitsId, List.of());
    }

    public static ArmyAggregate getArmyBundle(int armyId) throws SQLException {
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

    private static DatasheetAggregate toDatasheetAggregate(DatasheetBundleRepository.DatasheetRecordBundle recordBundle) {
        return new DatasheetAggregate(
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

    private static ArmyAggregate toArmyAggregate(ArmyBundleRepository.ArmyRecordBundle recordBundle) {
        return new ArmyAggregate(
                recordBundle.army(),
                recordBundle.detachments(),
                recordBundle.units(),
                recordBundle.wargear()
        );
    }
}
