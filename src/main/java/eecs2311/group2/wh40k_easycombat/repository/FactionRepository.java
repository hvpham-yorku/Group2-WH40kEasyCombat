//-- Auto Generated Java File --
package eecs2311.group2.wh40k_easycombat.repository;

import eecs2311.group2.wh40k_easycombat.db.Dao;
import eecs2311.group2.wh40k_easycombat.model.Factions;
import eecs2311.group2.wh40k_easycombat.util.IntListCodec;

import java.util.List;
import java.sql.SQLException;

@SuppressWarnings("unused")
public class FactionRepository {
		public static int addNewFaction(Factions faction) throws SQLException {
				return Dao.update(
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
		public static List<Factions> getAllFactions() throws SQLException {
				return Dao.query(
						"SELECT * FROM factions",
						rs -> new Factions(
								rs.getInt("id"),
								rs.getString("name")
						)						
				);
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