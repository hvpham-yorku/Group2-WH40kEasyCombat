//-- Auto Generated Java File --
package eecs2311.group2.wh40k_easycombat.repository;

import eecs2311.group2.wh40k_easycombat.db.Dao;
import eecs2311.group2.wh40k_easycombat.model.Units;
import eecs2311.group2.wh40k_easycombat.util.IntListCodec;

import java.sql.SQLException;

@SuppressWarnings("unused")
public class UnitRepository {
		public static void insertUnit(Units unit) throws SQLException {
				Dao.update(
						"INSERT INTO units (factionId, name, points, M, T, SV, W, LD, OC, invulnerableSave, category, composition, keywordIdList, rangedWeaponIdList, meleeWeaponIdList VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)",
						unit.factionId(),
						unit.name(),
						unit.points(),
						unit.M(),
						unit.T(),
						unit.SV(),
						unit.W(),
						unit.LD(),
						unit.OC(),
						unit.invulnerableSave(),
						unit.category(),
						unit.composition(),
						IntListCodec.encode(unit.keywordIdList()),
						IntListCodec.encode(unit.rangedWeaponIdList()),
						IntListCodec.encode(unit.meleeWeaponIdList())
				);
		}
		public static Units getUnitById(int id) throws SQLException {
				return Dao.query(
						"SELECT * FROM units WHERE id = ?",
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
								rs.getInt("invulnerableSave"),
								rs.getInt("category"),
								rs.getString("composition"),
								IntListCodec.decode(rs.getString("keywordIdList")),
								IntListCodec.decode(rs.getString("rangedWeaponIdList")),
								IntListCodec.decode(rs.getString("meleeWeaponIdList"))
						),
						id
				).stream().findFirst().orElse(null);
		}
		public static void updateUnit(Units unit) throws SQLException {
				Dao.update(
						"UPDATE units SET factionId = ?, name = ?, points = ?, M = ?, T = ?, SV = ?, W = ?, LD = ?, OC = ?, invulnerableSave = ?, category = ?, composition = ?, keywordIdList = ?, rangedWeaponIdList = ?, meleeWeaponIdList = ? WHERE id = ?",
						unit.factionId(),
						unit.name(),
						unit.points(),
						unit.M(),
						unit.T(),
						unit.SV(),
						unit.W(),
						unit.LD(),
						unit.OC(),
						unit.invulnerableSave(),
						unit.category(),
						unit.composition(),
						IntListCodec.encode(unit.keywordIdList()),
						IntListCodec.encode(unit.rangedWeaponIdList()),
						IntListCodec.encode(unit.meleeWeaponIdList()),
						unit.id()
				);
		}
		public static void deleteUnit(Units unit) throws SQLException {
				Dao.update(
						"DELETE FROM units WHERE id = ?",
						unit.id()
				);
		}
}