//-- Auto Generated Java File --

package eecs2311.group2.wh40k_easycombat.repository;

import eecs2311.group2.wh40k_easycombat.db.Dao;
import eecs2311.group2.wh40k_easycombat.model.Detachment_abilities;
import eecs2311.group2.wh40k_easycombat.util.IntListCodec;

import java.util.List;
import java.sql.SQLException;

@SuppressWarnings("unused")
public class Detachment_abilitiesRepository {

	public static int addNewDetachment_abilities(Detachment_abilities detachment_abilities) throws java.sql.SQLException {
		return Dao.update(
			"INSERT INTO Detachment_abilities (auto_id, id, detachment_id, faction_id, name, legend, description, detachment) VALUES (?, ?, ?, ?, ?, ?, ?, ?)",
			detachment_abilities.auto_id(),
			detachment_abilities.id(),
			detachment_abilities.detachment_id(),
			detachment_abilities.faction_id(),
			detachment_abilities.name(),
			detachment_abilities.legend(),
			detachment_abilities.description(),
			detachment_abilities.detachment()
		);
	}

	public static Detachment_abilities getDetachment_abilitiesByPk(int auto_id) throws java.sql.SQLException {
		return Dao.query(
			"SELECT * FROM Detachment_abilities WHERE auto_id = ?",
			rs -> new Detachment_abilities(
				rs.getInt("auto_id"),
				rs.getString("id"),
				rs.getString("detachment_id"),
				rs.getString("faction_id"),
				rs.getString("name"),
				rs.getString("legend"),
				rs.getString("description"),
				rs.getString("detachment")
			),
			auto_id
		).stream().findFirst().orElse(null);
	}

	public static java.util.List<Detachment_abilities> getAllDetachment_abilities() throws java.sql.SQLException {
		return Dao.query(
			"SELECT * FROM Detachment_abilities",
			rs -> new Detachment_abilities(
				rs.getInt("auto_id"),
				rs.getString("id"),
				rs.getString("detachment_id"),
				rs.getString("faction_id"),
				rs.getString("name"),
				rs.getString("legend"),
				rs.getString("description"),
				rs.getString("detachment")
			)
		);
	}

	public static void updateDetachment_abilities(Detachment_abilities detachment_abilities) throws java.sql.SQLException {
		Dao.update(
			"UPDATE Detachment_abilities SET id = ?, detachment_id = ?, faction_id = ?, name = ?, legend = ?, description = ?, detachment = ? WHERE auto_id = ?",
			detachment_abilities.id(),
			detachment_abilities.detachment_id(),
			detachment_abilities.faction_id(),
			detachment_abilities.name(),
			detachment_abilities.legend(),
			detachment_abilities.description(),
			detachment_abilities.detachment(),
			detachment_abilities.auto_id()
		);
	}

	public static void deleteDetachment_abilities(Detachment_abilities detachment_abilities) throws java.sql.SQLException {
		Dao.update(
			"DELETE FROM Detachment_abilities WHERE auto_id = ?",
			detachment_abilities.auto_id()
		);
	}

}
