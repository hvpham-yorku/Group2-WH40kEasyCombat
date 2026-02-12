//-- Auto Generated Java File --
package eecs2311.group2.wh40k_easycombat.repository;

import eecs2311.group2.wh40k_easycombat.db.Dao;
import eecs2311.group2.wh40k_easycombat.model.Factions;
import eecs2311.group2.wh40k_easycombat.util.IntListCodec;

import java.sql.SQLException;

@SuppressWarnings("unused")
public class FactionRepository {
		public static void insertFaction(Factions faction) throws SQLException {
				Dao.update(
						"INSERT INTO factions (name VALUES (?)",
						faction.name()
				);
		}
		public static Factions getFactionById(int id) throws SQLException {
				return Dao.query(
						"SELECT * FROM factions WHERE id = ?",
						rs -> new Factions(
								rs.getInt("id"),
								rs.getString("name")
						),
						id
				).stream().findFirst().orElse(null);
		}
		public static void updateFaction(Factions faction) throws SQLException {
				Dao.update(
						"UPDATE factions SET name = ? WHERE id = ?",
						faction.name(),
						faction.id()
				);
		}
		public static void deleteFaction(Factions faction) throws SQLException {
				Dao.update(
						"DELETE FROM factions WHERE id = ?",
						faction.id()
				);
		}
}