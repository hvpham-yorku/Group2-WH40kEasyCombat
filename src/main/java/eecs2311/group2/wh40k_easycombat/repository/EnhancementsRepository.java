//-- Auto Generated Java File --

package eecs2311.group2.wh40k_easycombat.repository;

import eecs2311.group2.wh40k_easycombat.db.Dao;
import eecs2311.group2.wh40k_easycombat.model.Enhancements;
import eecs2311.group2.wh40k_easycombat.util.IntListCodec;

import java.util.List;
import java.sql.SQLException;

@SuppressWarnings("unused")
public class EnhancementsRepository {

	public static int addNewEnhancements(Enhancements enhancements) throws java.sql.SQLException {
		return Dao.update(
			"INSERT INTO Enhancements (auto_id, id, faction_id, name, legend, description, cost, detachment, detachment_id) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)",
			enhancements.auto_id(),
			enhancements.id(),
			enhancements.faction_id(),
			enhancements.name(),
			enhancements.legend(),
			enhancements.description(),
			enhancements.cost(),
			enhancements.detachment(),
			enhancements.detachment_id()
		);
	}

	public static Enhancements getEnhancementsByPk(int auto_id) throws java.sql.SQLException {
		return Dao.query(
			"SELECT * FROM Enhancements WHERE auto_id = ?",
			rs -> new Enhancements(
				rs.getInt("auto_id"),
				rs.getString("id"),
				rs.getString("faction_id"),
				rs.getString("name"),
				rs.getString("legend"),
				rs.getString("description"),
				rs.getString("cost"),
				rs.getString("detachment"),
				rs.getString("detachment_id")
			),
			auto_id
		).stream().findFirst().orElse(null);
	}

	public static java.util.List<Enhancements> getAllEnhancements() throws java.sql.SQLException {
		return Dao.query(
			"SELECT * FROM Enhancements",
			rs -> new Enhancements(
				rs.getInt("auto_id"),
				rs.getString("id"),
				rs.getString("faction_id"),
				rs.getString("name"),
				rs.getString("legend"),
				rs.getString("description"),
				rs.getString("cost"),
				rs.getString("detachment"),
				rs.getString("detachment_id")
			)
		);
	}

	public static void updateEnhancements(Enhancements enhancements) throws java.sql.SQLException {
		Dao.update(
			"UPDATE Enhancements SET id = ?, faction_id = ?, name = ?, legend = ?, description = ?, cost = ?, detachment = ?, detachment_id = ? WHERE auto_id = ?",
			enhancements.id(),
			enhancements.faction_id(),
			enhancements.name(),
			enhancements.legend(),
			enhancements.description(),
			enhancements.cost(),
			enhancements.detachment(),
			enhancements.detachment_id(),
			enhancements.auto_id()
		);
	}

	public static void deleteEnhancements(Enhancements enhancements) throws java.sql.SQLException {
		Dao.update(
			"DELETE FROM Enhancements WHERE auto_id = ?",
			enhancements.auto_id()
		);
	}

}
