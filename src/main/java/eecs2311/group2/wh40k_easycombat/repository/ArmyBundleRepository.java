package eecs2311.group2.wh40k_easycombat.repository;

import eecs2311.group2.wh40k_easycombat.db.Dao;
import eecs2311.group2.wh40k_easycombat.db.Tx;
import eecs2311.group2.wh40k_easycombat.model.Army;
import eecs2311.group2.wh40k_easycombat.model.Army_detachment;
import eecs2311.group2.wh40k_easycombat.model.Army_units;
import eecs2311.group2.wh40k_easycombat.model.Army_wargear;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.BiConsumer;
import java.util.function.Function;

public final class ArmyBundleRepository {

    public record ArmyRecordBundle(
            Army army,
            List<Army_detachment> detachments,
            List<Army_units> units,
            List<Army_wargear> wargear
    ) {
        public ArmyRecordBundle {
            Objects.requireNonNull(army, "army must not be null");
            detachments = immutableList(detachments);
            units = immutableList(units);
            wargear = immutableList(wargear);
        }
    }

    public record ArmyWriteRecordBundle(
            Army army,
            List<Army_detachment> detachments,
            List<Army_units> units,
            List<Army_wargear> wargear
    ) {
        public ArmyWriteRecordBundle {
            Objects.requireNonNull(army, "army must not be null");
            detachments = immutableList(detachments);
            units = immutableList(units);
            wargear = immutableList(wargear);
        }
    }

    public static List<ArmyRecordBundle> findAllBundles() throws SQLException {
        Map<Integer, ArmyAccumulator> byArmyId = new LinkedHashMap<>();
        Map<Integer, Integer> unitToArmyId = new HashMap<>();

        for (Army army : ArmyRepository.getAllArmy()) {
            if (army == null) {
                continue;
            }
            byArmyId.put(army.auto_id(), new ArmyAccumulator(army));
        }

        attach(byArmyId, Army_detachmentRepository.getAllArmy_detachment(),
                Army_detachment::army_id, (acc, row) -> acc.detachments.add(row));

        attach(byArmyId, Army_unitsRepository.getAllArmy_units(),
                Army_units::army_id, (acc, row) -> {
                    acc.units.add(row);
                    unitToArmyId.put(row.auto_id(), row.army_id());
                });

        for (Army_wargear wargear : Army_wargearRepository.getAllArmy_wargear()) {
            if (wargear == null) {
                continue;
            }

            Integer armyId = unitToArmyId.get(wargear.units_id());
            if (armyId == null) {
                continue;
            }

            ArmyAccumulator accumulator = byArmyId.get(armyId);
            if (accumulator != null) {
                accumulator.wargear.add(wargear);
            }
        }

        List<ArmyRecordBundle> result = new ArrayList<>(byArmyId.size());
        for (ArmyAccumulator accumulator : byArmyId.values()) {
            result.add(accumulator.toBundle());
        }

        return List.copyOf(result);
    }

    public static int createBundle(ArmyWriteRecordBundle bundle) throws SQLException {
        validate(bundle);

        return runInTransaction(conn -> {
            int armyId = Dao.update(conn,
                    "INSERT INTO Army (name, faction_id, warlord_id, total_points, isMarked) VALUES (?, ?, ?, ?, ?)",
                    bundle.army().name(),
                    bundle.army().faction_id(),
                    bundle.army().warlord_id(),
                    bundle.army().total_points(),
                    bundle.army().isMarked()
            );

            insertChildren(conn, armyId, bundle);
            return armyId;
        });
    }

    public static void updateBundle(ArmyWriteRecordBundle bundle) throws SQLException {
        validate(bundle);

        runInTransaction(conn -> {
            int armyId = bundle.army().auto_id();

            Dao.update(conn,
                    "UPDATE Army SET name=?, faction_id=?, warlord_id=?, total_points=?, isMarked=? WHERE auto_id=?",
                    bundle.army().name(),
                    bundle.army().faction_id(),
                    bundle.army().warlord_id(),
                    bundle.army().total_points(),
                    bundle.army().isMarked(),
                    armyId
            );

            deleteChildren(conn, armyId);
            insertChildren(conn, armyId, bundle);
            return null;
        });
    }

    public static void deleteBundle(int armyId) throws SQLException {
        runInTransaction(conn -> {
            deleteChildren(conn, armyId);
            Dao.update(conn, "DELETE FROM Army WHERE auto_id = ?", armyId);
            return null;
        });
    }

    public static void updateMarked(int armyId, boolean marked) throws SQLException {
        Dao.update("UPDATE Army SET isMarked = ? WHERE auto_id = ?", marked, armyId);
    }

    private static void insertChildren(Connection conn, int armyId, ArmyWriteRecordBundle bundle) throws SQLException {
        for (Army_detachment detachment : bundle.detachments()) {
            Dao.update(conn,
                    "INSERT INTO Army_detachment (army_id, datasheet_id, detachment_id) VALUES (?, ?, ?)",
                    armyId,
                    detachment.datasheet_id(),
                    detachment.detachment_id()
            );
        }

        Map<Integer, Integer> unitIdMap = new HashMap<>();
        for (Army_units unit : bundle.units()) {
            int newUnitId = Dao.update(conn,
                    "INSERT INTO Army_units (army_id, datasheet_id, enhancements_id, model_count, unit_cost) " +
                            "VALUES (?, ?, ?, ?, ?)",
                    armyId,
                    unit.datasheet_id(),
                    unit.enhancements_id(),
                    unit.model_count(),
                    unit.unit_cost()
            );
            unitIdMap.put(unit.auto_id(), newUnitId);
        }

        for (Army_wargear wargear : bundle.wargear()) {
            Integer mappedUnitId = unitIdMap.get(wargear.units_id());
            if (mappedUnitId == null) {
                throw new IllegalArgumentException("No mapped units_id found for old id: " + wargear.units_id());
            }

            Dao.update(conn,
                    "INSERT INTO Army_wargear (wargear_id, units_id, wargear_count) VALUES (?, ?, ?)",
                    wargear.wargear_id(),
                    mappedUnitId,
                    wargear.wargear_count()
            );
        }
    }

    private static void deleteChildren(Connection conn, int armyId) throws SQLException {
        Dao.update(conn,
                "DELETE FROM Army_wargear WHERE units_id IN (SELECT auto_id FROM Army_units WHERE army_id = ?)",
                armyId
        );
        Dao.update(conn, "DELETE FROM Army_detachment WHERE army_id = ?", armyId);
        Dao.update(conn, "DELETE FROM Army_units WHERE army_id = ?", armyId);
    }

    private static void validate(ArmyWriteRecordBundle bundle) {
        if (bundle == null || bundle.army() == null) {
            throw new IllegalArgumentException("army bundle must not be null");
        }
    }

    private static <T> void attach(
            Map<Integer, ArmyAccumulator> byArmyId,
            List<T> rows,
            Function<T, Integer> armyIdExtractor,
            BiConsumer<ArmyAccumulator, T> rowConsumer
    ) {
        for (T row : rows) {
            if (row == null) {
                continue;
            }

            ArmyAccumulator accumulator = byArmyId.get(armyIdExtractor.apply(row));
            if (accumulator != null) {
                rowConsumer.accept(accumulator, row);
            }
        }
    }

    private static <T> List<T> immutableList(List<T> rows) {
        return rows == null ? List.of() : List.copyOf(rows);
    }

    private static <T> T runInTransaction(SqlSupplier<T> work) throws SQLException {
        try {
            return Tx.run(conn -> {
                try {
                    return work.run(conn);
                } catch (SQLException e) {
                    throw new RuntimeException(e);
                }
            });
        } catch (RuntimeException e) {
            if (e.getCause() instanceof SQLException sqlException) {
                throw sqlException;
            }
            throw e;
        }
    }

    @FunctionalInterface
    private interface SqlSupplier<T> {
        T run(Connection conn) throws SQLException;
    }

    private static final class ArmyAccumulator {
        private final Army army;
        private final List<Army_detachment> detachments = new ArrayList<>();
        private final List<Army_units> units = new ArrayList<>();
        private final List<Army_wargear> wargear = new ArrayList<>();

        private ArmyAccumulator(Army army) {
            this.army = army;
        }

        private ArmyRecordBundle toBundle() {
            detachments.sort(Comparator.comparingInt(Army_detachment::auto_id));
            units.sort(Comparator.comparingInt(Army_units::auto_id));
            wargear.sort(Comparator.comparingInt(Army_wargear::auto_id));
            return new ArmyRecordBundle(army, detachments, units, wargear);
        }
    }

    private ArmyBundleRepository() {
    }
}