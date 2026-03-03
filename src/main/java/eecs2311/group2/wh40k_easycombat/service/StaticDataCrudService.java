package eecs2311.group2.wh40k_easycombat.service;

import eecs2311.group2.wh40k_easycombat.db.Tx;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public final class StaticDataCrudService {

    private StaticDataCrudService() {}

    public static void saveBundle(StaticDataService.DatasheetBundle b) throws SQLException {

        if (b == null || b.datasheet == null || b.datasheet.id() == null) {
            throw new IllegalArgumentException("bundle/datasheet/id must not be null");
        }

        try {
            Tx.run(conn -> {
                try {
                    String id = b.datasheet.id();

                    // 1) Update main datasheet
                    execUpdate(conn,
                            "UPDATE Datasheets SET " +
                                    "name=?, faction_id=?, source_id=?, legend=?, role=?, loadout=?, transport=?, virtual=?, " +
                                    "leader_head=?, leader_footer=?, damaged_w=?, damaged_description=?, link=? " +
                                    "WHERE id=?",
                            b.datasheet.name(),
                            b.datasheet.faction_id(),
                            b.datasheet.source_id(),
                            b.datasheet.legend(),
                            b.datasheet.role(),
                            b.datasheet.loadout(),
                            b.datasheet.transport(),
                            b.datasheet.virtual(),
                            b.datasheet.leader_head(),
                            b.datasheet.leader_footer(),
                            b.datasheet.damaged_w(),
                            b.datasheet.damaged_description(),
                            b.datasheet.link(),
                            b.datasheet.id()
                    );

                    // 2) Replace strategy: delete children
                    deleteChildren(conn, id);

                    // 3) Re-insert children
                    for (var x : b.models) {
                        execUpdate(conn,
                                "INSERT INTO Datasheets_models VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)",
                                x.datasheet_id(), x.line(), x.name(), x.M(), x.T(),
                                x.Sv(), x.inv_sv(), x.inv_sv_descr(), x.W(),
                                x.Ld(), x.OC(), x.base_size(), x.base_size_descr()
                        );
                    }

                    for (var x : b.costs) {
                        execUpdate(conn,
                                "INSERT INTO Datasheets_models_cost VALUES (?, ?, ?, ?)",
                                x.datasheet_id(), x.line(), x.description(), x.cost()
                        );
                    }

                    for (var x : b.compositions) {
                        execUpdate(conn,
                                "INSERT INTO Datasheets_unit_composition VALUES (?, ?, ?)",
                                x.datasheet_id(), x.line(), x.description()
                        );
                    }

                    for (var x : b.wargear) {
                        execUpdate(conn,
                                "INSERT INTO Datasheets_wargear VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)",
                                x.datasheet_id(), x.line(), x.line_in_wargear(),
                                x.dice(), x.name(), x.description(),
                                x.range(), x.type(), x.A(),
                                x.BS_WS(), x.S(), x.AP(), x.D()
                        );
                    }

                    for (var x : b.abilities) {
                        execUpdate(conn,
                                "INSERT INTO Datasheets_abilities VALUES (?, ?, ?, ?, ?, ?, ?, ?)",
                                x.datasheet_id(), x.line(),
                                x.ability_id(), x.model(), x.name(),
                                x.description(), x.type(), x.parameter()
                        );
                    }

                    for (var x : b.keywords) {
                        execUpdate(conn,
                                "INSERT INTO Datasheets_keywords VALUES (?, ?, ?, ?)",
                                x.datasheet_id(), x.keyword(), x.model(), x.is_faction_keyword()
                        );
                    }

                    for (var x : b.options) {
                        execUpdate(conn,
                                "INSERT INTO Datasheets_options VALUES (?, ?, ?, ?)",
                                x.datasheet_id(), x.line(), x.button(), x.description()
                        );
                    }

                    for (var x : b.stratagems) {
                        execUpdate(conn,
                                "INSERT INTO Datasheets_stratagems VALUES (?, ?)",
                                x.datasheet_id(), x.stratagem_id()
                        );
                    }

                    for (var x : b.enhancements) {
                        execUpdate(conn,
                                "INSERT INTO Datasheets_enhancements VALUES (?, ?)",
                                x.datasheet_id(), x.enhancement_id()
                        );
                    }

                    for (var x : b.detachmentAbilities) {
                        execUpdate(conn,
                                "INSERT INTO Datasheets_detachment_abilities VALUES (?, ?)",
                                x.datasheet_id(), x.detachment_ability_id()
                        );
                    }

                    for (var x : b.leaders) {
                        execUpdate(conn,
                                "INSERT INTO Datasheets_leader VALUES (?, ?)",
                                x.leader_id(), x.attached_id()
                        );
                    }

                    return null;

                } catch (SQLException e) {
                    // IMPORTANT: convert checked to unchecked so Tx.run can rollback
                    throw new RuntimeException(e);
                }
            });

        } catch (RuntimeException re) {
            if (re.getCause() instanceof SQLException se) throw se;
            throw re;
        }

        StaticDataService.reloadFromSqlite();
    }

    private static void deleteChildren(Connection conn, String datasheetId) throws SQLException {
        execUpdate(conn, "DELETE FROM Datasheets_models WHERE datasheet_id = ?", datasheetId);
        execUpdate(conn, "DELETE FROM Datasheets_models_cost WHERE datasheet_id = ?", datasheetId);
        execUpdate(conn, "DELETE FROM Datasheets_unit_composition WHERE datasheet_id = ?", datasheetId);
        execUpdate(conn, "DELETE FROM Datasheets_wargear WHERE datasheet_id = ?", datasheetId);
        execUpdate(conn, "DELETE FROM Datasheets_abilities WHERE datasheet_id = ?", datasheetId);
        execUpdate(conn, "DELETE FROM Datasheets_keywords WHERE datasheet_id = ?", datasheetId);
        execUpdate(conn, "DELETE FROM Datasheets_options WHERE datasheet_id = ?", datasheetId);
        execUpdate(conn, "DELETE FROM Datasheets_stratagems WHERE datasheet_id = ?", datasheetId);
        execUpdate(conn, "DELETE FROM Datasheets_enhancements WHERE datasheet_id = ?", datasheetId);
        execUpdate(conn, "DELETE FROM Datasheets_detachment_abilities WHERE datasheet_id = ?", datasheetId);

        // leaders uses attached_id
        execUpdate(conn, "DELETE FROM Datasheets_leader WHERE attached_id = ?", datasheetId);
    }

    /**
     * Execute INSERT/UPDATE/DELETE without requiring generated keys.
     * This avoids Dao.update(conn, ...) throwing "No ID returned." on non-AI tables.
     */
    private static int execUpdate(Connection conn, String sql, Object... params) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            for (int i = 0; i < params.length; i++) {
                ps.setObject(i + 1, params[i]);
            }
            return ps.executeUpdate();
        }
    }
}