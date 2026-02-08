package eecs2311.group2.wh40k_easycombat.repository;

import eecs2311.group2.wh40k_easycombat.db.Dao;
import eecs2311.group2.wh40k_easycombat.db.Tx;
import eecs2311.group2.wh40k_easycombat.model.Units;
import eecs2311.group2.wh40k_easycombat.util.IntListCodec;
import eecs2311.group2.wh40k_easycombat.util.StringListCodec;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

public class UnitRepository {
    public static void insertUnit(Units unit) throws SQLException {
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
            IntListCodec.encode(unit.rangedWeaponIdList()),
            IntListCodec.encode(unit.meleeWeaponIdList())
        );
    }

    public static Units getUnitById(int id) throws SQLException {
        return Dao.query(
            "SELECT * FROM Unit WHERE id = ?",
            rs -> new Units(
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
                IntListCodec.decode(rs.getString("rangedWeaponIdList")),
                IntListCodec.decode(rs.getString("meleeWeaponIdList"))
            ),
            id
        ).stream().findFirst().orElse(null);
    }

    public static List<Units> getUnitsByFaction(int factionId) throws SQLException {
        return Dao.query(
            "SELECT * FROM Unit WHERE factionId = ?",
            rs -> new Units(
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
                IntListCodec.decode(rs.getString("rangedWeaponIdList")),
                IntListCodec.decode(rs.getString("meleeWeaponIdList"))
            ),
            factionId
        );
    }

    public static void insertUnitsTransactional(List<Units> units) throws SQLException {
        Tx.run((Connection conn) -> {
            for (Units unit : units) {
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
                        IntListCodec.encode(unit.rangedWeaponIdList()),
                        IntListCodec.encode(unit.meleeWeaponIdList())
                    );
                } catch (SQLException e) {
                    throw new RuntimeException(e);
                }
            }
            return null;
        });
    }

    public static void updateUnitsTransactional(List<Units> units) throws SQLException {
        Tx.run((Connection conn) -> {
            for (Units unit : units) {
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
                        IntListCodec.encode(unit.rangedWeaponIdList()),
                        IntListCodec.encode(unit.meleeWeaponIdList()),
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