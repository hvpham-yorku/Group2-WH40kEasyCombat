//-- Auto Generated Java File --

package eecs2311.group2.wh40k_easycombat.repository;

import eecs2311.group2.wh40k_easycombat.db.Dao;
import eecs2311.group2.wh40k_easycombat.model.Stratagems;
import eecs2311.group2.wh40k_easycombat.util.IntListCodec;

import java.util.List;
import java.sql.SQLException;

@SuppressWarnings("unused")
public class StratagemsRepository {

	public static int addNewStratagems(Stratagems stratagems) throws java.sql.SQLException {
		return Dao.update(
			"INSERT INTO Stratagems (auto_id, id, faction_id, name, type, cp_cost, legend, turn, phase, description, detachment, detachment_id) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)",
			stratagems.auto_id(),
			stratagems.id(),
			stratagems.faction_id(),
			stratagems.name(),
			stratagems.type(),
			stratagems.cp_cost(),
			stratagems.legend(),
			stratagems.turn(),
			stratagems.phase(),
			stratagems.description(),
			stratagems.detachment(),
			stratagems.detachment_id()
		);
	}

	public static Stratagems getStratagemsByPk(int auto_id) throws java.sql.SQLException {
		return Dao.query(
			"SELECT * FROM Stratagems WHERE auto_id = ?",
			rs -> new Stratagems(
				rs.getInt("auto_id"),
				rs.getString("id"),
				rs.getString("faction_id"),
				rs.getString("name"),
				rs.getString("type"),
				rs.getString("cp_cost"),
				rs.getString("legend"),
				rs.getString("turn"),
				rs.getString("phase"),
				rs.getString("description"),
				rs.getString("detachment"),
				rs.getString("detachment_id")
			),
			auto_id
		).stream().findFirst().orElse(null);
	}

	public static java.util.List<Stratagems> getAllStratagems() throws java.sql.SQLException {
		return Dao.query(
			"SELECT * FROM Stratagems",
			rs -> new Stratagems(
				rs.getInt("auto_id"),
				rs.getString("id"),
				rs.getString("faction_id"),
				rs.getString("name"),
				rs.getString("type"),
				rs.getString("cp_cost"),
				rs.getString("legend"),
				rs.getString("turn"),
				rs.getString("phase"),
				rs.getString("description"),
				rs.getString("detachment"),
				rs.getString("detachment_id")
			)
		);
	}

	public static void updateStratagems(Stratagems stratagems) throws java.sql.SQLException {
		Dao.update(
			"UPDATE Stratagems SET id = ?, faction_id = ?, name = ?, type = ?, cp_cost = ?, legend = ?, turn = ?, phase = ?, description = ?, detachment = ?, detachment_id = ? WHERE auto_id = ?",
			stratagems.id(),
			stratagems.faction_id(),
			stratagems.name(),
			stratagems.type(),
			stratagems.cp_cost(),
			stratagems.legend(),
			stratagems.turn(),
			stratagems.phase(),
			stratagems.description(),
			stratagems.detachment(),
			stratagems.detachment_id(),
			stratagems.auto_id()
		);
	}

	public static void deleteStratagems(Stratagems stratagems) throws java.sql.SQLException {
		Dao.update(
			"DELETE FROM Stratagems WHERE auto_id = ?",
			stratagems.auto_id()
		);
	}

}
