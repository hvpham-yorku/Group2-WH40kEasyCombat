//-- Auto Generated Java File --
package eecs2311.group2.wh40k_easycombat.repository;

import eecs2311.group2.wh40k_easycombat.db.Dao;
import eecs2311.group2.wh40k_easycombat.model.Detachments;
import eecs2311.group2.wh40k_easycombat.util.IntListCodec;

import java.sql.SQLException;

@SuppressWarnings("unused")
public class DetachmentRepository {
		public static void insertDetachment(Detachments detachment) throws SQLException {
				Dao.update(
						"INSERT INTO detachments (name, factionId, strategyId, detachmentRule VALUES (?, ?, ?, ?)",
						detachment.name(),
						detachment.factionId(),
						detachment.strategyId(),
						detachment.detachmentRule()
				);
		}
		public static Detachments getDetachmentById(int id) throws SQLException {
				return Dao.query(
						"SELECT * FROM detachments WHERE id = ?",
						rs -> new Detachments(
								rs.getInt("id"),
								rs.getString("name"),
								rs.getInt("factionId"),
								rs.getInt("strategyId"),
								rs.getString("detachmentRule")
						),
						id
				).stream().findFirst().orElse(null);
		}
		public static void updateDetachment(Detachments detachment) throws SQLException {
				Dao.update(
						"UPDATE detachments SET name = ?, factionId = ?, strategyId = ?, detachmentRule = ? WHERE id = ?",
						detachment.name(),
						detachment.factionId(),
						detachment.strategyId(),
						detachment.detachmentRule(),
						detachment.id()
				);
		}
		public static void deleteDetachment(Detachments detachment) throws SQLException {
				Dao.update(
						"DELETE FROM detachments WHERE id = ?",
						detachment.id()
				);
		}
}