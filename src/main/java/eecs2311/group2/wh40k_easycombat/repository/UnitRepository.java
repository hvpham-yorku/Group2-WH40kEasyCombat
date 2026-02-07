package eecs2311.group2.wh40k_easycombat.repository;

import eecs2311.group2.wh40k_easycombat.db.Dao;
import eecs2311.group2.wh40k_easycombat.db.Tx;
import eecs2311.group2.wh40k_easycombat.model.Unit;
import eecs2311.group2.wh40k_easycombat.util.StringListCodec;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

public class UnitRepository {
    public static void insertUnit(Unit unit) throws SQLException {
        Dao.update(
            "INSERT INTO Unit (id, factionId, name, points, M, T, SV, W, LD, OC, category, composition, keywordIdList, rangedWeaponIdList, meleeWeaponIdList) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)",
            unit.id(),
            unit.factionId(),
            unit.name(),
            unit.points(),
            unit.M(),
            unit.T(),
            unit.SV(),
            unit.W(),
            unit.LD(),
            unit.OC(),
            unit.category(),
            unit.composition(),
            unit.invulnerableSave(),
            StringListCodec.encode(unit.keywordIdList()),
            StringListCodec.encode(unit.rangedWeaponIdList()),
            StringListCodec.encode(unit.meleeWeaponIdList())
        );
    }

    public static Unit getUnitById(int id) throws SQLException {
        return Dao.query(
            "SELECT * FROM Unit WHERE id = ?",
            rs -> new Unit(
                rs.getInt("id"),
                rs.getInt("factionId"),
                rs.getString("name"),
                rs.getInt("points"),
                rs.getInt("M"),
                rs.getInt("T"),
                rs.getInt("SV"),
                rs.getInt("W"),
                rs.getInt("LD"),
                rs.getInt("OC"),
                rs.getInt("category"),
                rs.getInt("invulnerableSave"),
                rs.getString("composition"),
                StringListCodec.decode(rs.getString("keywordIdList")),
                StringListCodec.decode(rs.getString("rangedWeaponIdList")),
                StringListCodec.decode(rs.getString("meleeWeaponIdList"))
            ),
            id
        ).stream().findFirst().orElse(null);
    }

    public static List<Unit> getUnitsByFaction(int factionId) throws SQLException {
        return Dao.query(
            "SELECT * FROM Unit WHERE factionId = ?",
            rs -> new Unit(
                rs.getInt("id"),
                rs.getInt("factionId"),
                rs.getString("name"),
                rs.getInt("points"),
                rs.getInt("M"),
                rs.getInt("T"),
                rs.getInt("SV"),
                rs.getInt("W"),
                rs.getInt("LD"),
                rs.getInt("OC"),
                rs.getInt("category"),
                rs.getInt("invulnerableSave"),
                rs.getString("composition"),
                StringListCodec.decode(rs.getString("keywordIdList")),
                StringListCodec.decode(rs.getString("rangedWeaponIdList")),
                StringListCodec.decode(rs.getString("meleeWeaponIdList"))
            ),
            factionId
        );
    }

    public static void insertUnitsTransactional(List<Unit> units) throws SQLException {
        Tx.run((Connection conn) -> {
            for (Unit unit : units) {
                try {
                    Dao.update(conn,
                        "INSERT INTO Unit (id, factionId, name, points, M, T, SV, W, LD, OC, category, composition, keywordIdList, rangedWeaponIdList, meleeWeaponIdList) " +
                            "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)",
                        unit.id(),
                        unit.factionId(),
                        unit.name(),
                        unit.points(),
                        unit.M(),
                        unit.T(),
                        unit.SV(),
                        unit.W(),
                        unit.LD(),
                        unit.OC(),
                        unit.category(),
                        unit.invulnerableSave(),
                        unit.composition(),
                        StringListCodec.encode(unit.keywordIdList()),
                        StringListCodec.encode(unit.rangedWeaponIdList()),
                        StringListCodec.encode(unit.meleeWeaponIdList())
                    );
                } catch (SQLException e) {
                    throw new RuntimeException(e);
                }
            }
            return null;
        });
    }

    public static void updateUnitsTransactional(List<Unit> units) throws SQLException {
        Tx.run((Connection conn) -> {
            for (Unit unit : units) {
                try {
                    Dao.update(conn,
                        "UPDATE Unit SET factionId = ?, name = ?, points = ?, M = ?, T = ?, SV = ?, W = ?, LD = ?, OC = ?, " +
                            "category = ?, composition = ?, keywordIdList = ?, rangedWeaponIdList = ?, meleeWeaponIdList = ?, invulnerableSave = ? " +
                            "WHERE id = ?",
                        unit.factionId(),
                        unit.name(),
                        unit.points(),
                        unit.M(),
                        unit.T(),
                        unit.SV(),
                        unit.W(),
                        unit.LD(),
                        unit.OC(),
                        unit.category(),
                        unit.composition(),
                        StringListCodec.encode(unit.keywordIdList()),
                        StringListCodec.encode(unit.rangedWeaponIdList()),
                        StringListCodec.encode(unit.meleeWeaponIdList()),
                        unit.invulnerableSave(),
                        unit.id()
                    );
                } catch (SQLException e) {
                    throw new RuntimeException(e);
                }
            }
            return null;
        });
    }
}