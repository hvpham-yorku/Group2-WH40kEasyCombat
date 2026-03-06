package eecs2311.group2.wh40k_easycombat.service;

import eecs2311.group2.wh40k_easycombat.db.Tx;
import eecs2311.group2.wh40k_easycombat.model.Army;
import eecs2311.group2.wh40k_easycombat.model.Army_detachment;
import eecs2311.group2.wh40k_easycombat.model.Army_units;
import eecs2311.group2.wh40k_easycombat.model.Army_wargear;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public final class ArmyCrudService {

    private ArmyCrudService() {}

    /**
     * Public write DTO for creating/updating an army bundle.
     * auto_id of Army is ignored on create.
     * auto_id of detachments / units / wargear is ignored on write.
     */
    public static final class ArmyWriteBundle {
        public final Army army;
        public final List<Army_detachment> detachments;
        public final List<Army_units> units;
        public final List<Army_wargear> wargear;

        public ArmyWriteBundle(
                Army army,
                List<Army_detachment> detachments,
                List<Army_units> units,
                List<Army_wargear> wargear
        ) {
            this.army = army;
            this.detachments = detachments != null ? detachments : List.of();
            this.units = units != null ? units : List.of();
            this.wargear = wargear != null ? wargear : List.of();
        }
    }

    // =========================
    // READ
    // =========================

    public static StaticDataService.ArmyBundle getArmyBundle(int armyId) throws SQLException {
        return StaticDataService.getArmyBundle(armyId);
    }

    // =========================
    // CREATE
    // =========================

    public static int createArmyBundle(ArmyWriteBundle b) throws SQLException {
        validateBundleForCreate(b);

        try {
            int newArmyId = Tx.run(conn -> {
                try {
                    // 1) insert Army
                    int armyId = insertArmy(conn, b.army);

                    // 2) insert detachments
                    for (Army_detachment d : b.detachments) {
                        execUpdate(conn,
                                "INSERT INTO Army_detachment " +
                                        "(army_id, datasheet_id, detachment_id) " +
                                        "VALUES (?, ?, ?)",
                                armyId,
                                d.datasheet_id(),
                                d.detachment_id()
                        );
                    }

                    // 3) insert units, and keep old temp key -> new db unit id mapping
                    List<UnitIdMap> unitIdMaps = new ArrayList<>();
                    for (Army_units u : b.units) {
                        int newUnitId = insertArmyUnit(conn, armyId, u);
                        unitIdMaps.add(new UnitIdMap(u.auto_id(), newUnitId));
                    }

                    // 4) insert wargear
                    for (Army_wargear w : b.wargear) {
                        int mappedUnitsId = mapTempUnitId(unitIdMaps, w.units_id());
                        execUpdate(conn,
                                "INSERT INTO Army_wargear " +
                                        "(wargear_id, units_id, wargear_count) " +
                                        "VALUES (?, ?, ?)",
                                w.wargear_id(),
                                mappedUnitsId,
                                w.wargear_count()
                        );
                    }

                    return armyId;

                } catch (SQLException e) {
                    throw new RuntimeException(e);
                }
            });

            StaticDataService.reloadFromSqlite();
            return newArmyId;

        } catch (RuntimeException re) {
            if (re.getCause() instanceof SQLException se) throw se;
            throw re;
        }
    }

    // =========================
    // UPDATE
    // =========================

    public static void updateArmyBundle(ArmyWriteBundle b) throws SQLException {
        validateBundleForUpdate(b);

        try {
            Tx.run(conn -> {
                try {
                    int armyId = b.army.auto_id();

                    // 1) update army row
                    execUpdate(conn,
                            "UPDATE Army SET name=?, faction_id=?, warlord_id=?, total_points=?, isMarked=? WHERE auto_id=?",
                            b.army.name(),
                            b.army.faction_id(),
                            b.army.warlord_id(),
                            b.army.total_points(),
                            b.army.isMarked(),
                            armyId
                    );

                    // 2) replace children
                    deleteArmyChildren(conn, armyId);

                    // 3) reinsert detachments
                    for (Army_detachment d : b.detachments) {
                        execUpdate(conn,
                                "INSERT INTO Army_detachment " +
                                        "(army_id, datasheet_id, detachment_id) " +
                                        "VALUES (?, ?, ?)",
                                armyId,
                                d.datasheet_id(),
                                d.detachment_id()
                        );
                    }

                    // 4) reinsert units with mapping
                    List<UnitIdMap> unitIdMaps = new ArrayList<>();
                    for (Army_units u : b.units) {
                        int newUnitId = insertArmyUnit(conn, armyId, u);
                        unitIdMaps.add(new UnitIdMap(u.auto_id(), newUnitId));
                    }

                    // 5) reinsert wargear with mapped unit ids
                    for (Army_wargear w : b.wargear) {
                        int mappedUnitsId = mapTempUnitId(unitIdMaps, w.units_id());
                        execUpdate(conn,
                                "INSERT INTO Army_wargear " +
                                        "(wargear_id, units_id, wargear_count) " +
                                        "VALUES (?, ?, ?)",
                                w.wargear_id(),
                                mappedUnitsId,
                                w.wargear_count()
                        );
                    }

                    return null;

                } catch (SQLException e) {
                    throw new RuntimeException(e);
                }
            });

            StaticDataService.reloadFromSqlite();

        } catch (RuntimeException re) {
            if (re.getCause() instanceof SQLException se) throw se;
            throw re;
        }
    }

    // =========================
    // DELETE
    // =========================

    public static void deleteArmyBundle(int armyId) throws SQLException {
        try {
            Tx.run(conn -> {
                try {
                    deleteArmyChildren(conn, armyId);
                    execUpdate(conn, "DELETE FROM Army WHERE auto_id = ?", armyId);
                    return null;
                } catch (SQLException e) {
                    throw new RuntimeException(e);
                }
            });

            StaticDataService.reloadFromSqlite();

        } catch (RuntimeException re) {
            if (re.getCause() instanceof SQLException se) throw se;
            throw re;
        }
    }

    // =========================
    // helpers
    // =========================

    private static void validateBundleForCreate(ArmyWriteBundle b) {
        if (b == null || b.army == null) {
            throw new IllegalArgumentException("army bundle must not be null");
        }
    }

    private static void validateBundleForUpdate(ArmyWriteBundle b) {
        if (b == null || b.army == null) {
            throw new IllegalArgumentException("army bundle must not be null");
        }
        if (b.army.auto_id() <= 0) {
            throw new IllegalArgumentException("army.auto_id must be > 0 for update");
        }
    }

    private static int insertArmy(Connection conn, Army a) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement(
        		"INSERT INTO Army (name, faction_id, warlord_id, total_points, isMarked) VALUES (?, ?, ?, ?, ?)",
                Statement.RETURN_GENERATED_KEYS
        )) {
        	ps.setObject(1, a.name());
        	ps.setObject(2, a.faction_id());
        	ps.setObject(3, a.warlord_id());
        	ps.setObject(4, a.total_points());
        	ps.setObject(5, a.isMarked());
            ps.executeUpdate();

            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) return rs.getInt(1);
            }
        }
        throw new SQLException("Failed to insert Army: no generated key returned.");
    }

    private static int insertArmyUnit(Connection conn, int armyId, Army_units u) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement(
                "INSERT INTO Army_units " +
                        "(army_id, datasheet_id, enhancements_id, model_count, unit_cost) " +
                        "VALUES (?, ?, ?, ?, ?)",
                Statement.RETURN_GENERATED_KEYS
        )) {
            ps.setObject(1, armyId);
            ps.setObject(2, u.datasheet_id());
            ps.setObject(3, u.enhancements_id());
            ps.setObject(4, u.model_count());
            ps.setObject(5, u.unit_cost());
            ps.executeUpdate();

            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) return rs.getInt(1);
            }
        }
        throw new SQLException("Failed to insert Army_units: no generated key returned.");
    }

    private static void deleteArmyChildren(Connection conn, int armyId) throws SQLException {
        // delete wargear first (depends on units_id)
        execUpdate(conn,
                "DELETE FROM Army_wargear WHERE units_id IN (SELECT auto_id FROM Army_units WHERE army_id = ?)",
                armyId
        );

        execUpdate(conn, "DELETE FROM Army_detachment WHERE army_id = ?", armyId);
        execUpdate(conn, "DELETE FROM Army_units WHERE army_id = ?", armyId);
    }

    private static int mapTempUnitId(List<UnitIdMap> maps, int oldUnitsId) {
        for (UnitIdMap m : maps) {
            if (m.oldId == oldUnitsId) return m.newId;
        }
        throw new IllegalArgumentException("No mapped units_id found for old id: " + oldUnitsId);
    }

    private static int execUpdate(Connection conn, String sql, Object... params) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            for (int i = 0; i < params.length; i++) {
                ps.setObject(i + 1, params[i]);
            }
            return ps.executeUpdate();
        }
    }

    private static final class UnitIdMap {
        final int oldId;
        final int newId;

        UnitIdMap(int oldId, int newId) {
            this.oldId = oldId;
            this.newId = newId;
        }
    }
}