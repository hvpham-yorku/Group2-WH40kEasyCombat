//-- Auto Generated Java File --

package eecs2311.group2.wh40k_easycombat.repository;

import eecs2311.group2.wh40k_easycombat.db.Dao;
import eecs2311.group2.wh40k_easycombat.model.Factions;
import eecs2311.group2.wh40k_easycombat.util.IntListCodec;

import java.util.List;
import java.sql.SQLException;

@SuppressWarnings("unused")
public class FactionsRepository {

	public static int addNewFactions(Factions factions) throws java.sql.SQLException {
		return Dao.update(
			"INSERT INTO Factions (auto_id, id, name, link) VALUES (?, ?, ?, ?)",
			factions.auto_id(),
			factions.id(),
			factions.name(),
			factions.link()
		);
	}

	public static Factions getFactionsByPk(int auto_id) throws java.sql.SQLException {
		return Dao.query(
			"SELECT * FROM Factions WHERE auto_id = ?",
			rs -> new Factions(
				rs.getInt("auto_id"),
				rs.getString("id"),
				rs.getString("name"),
				rs.getString("link")
			),
			auto_id
		).stream().findFirst().orElse(null);
	}

	public static java.util.List<Factions> getAllFactions() throws java.sql.SQLException {
		return Dao.query(
			"SELECT * FROM Factions",
			rs -> new Factions(
				rs.getInt("auto_id"),
				rs.getString("id"),
				rs.getString("name"),
				rs.getString("link")
			)
		);
	}

	public static void updateFactions(Factions factions) throws java.sql.SQLException {
		Dao.update(
			"UPDATE Factions SET id = ?, name = ?, link = ? WHERE auto_id = ?",
			factions.id(),
			factions.name(),
			factions.link(),
			factions.auto_id()
		);
	}

	public static void deleteFactions(Factions factions) throws java.sql.SQLException {
		Dao.update(
			"DELETE FROM Factions WHERE auto_id = ?",
			factions.auto_id()
		);
	}

}
