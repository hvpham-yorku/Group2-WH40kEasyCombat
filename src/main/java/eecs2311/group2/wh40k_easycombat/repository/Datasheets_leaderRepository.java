//-- Auto Generated Java File --

package eecs2311.group2.wh40k_easycombat.repository;

import eecs2311.group2.wh40k_easycombat.db.Dao;
import eecs2311.group2.wh40k_easycombat.model.Datasheets_leader;
import eecs2311.group2.wh40k_easycombat.util.IntListCodec;

import java.util.List;
import java.sql.SQLException;

@SuppressWarnings("unused")
public class Datasheets_leaderRepository {

	public static int addNewDatasheets_leader(Datasheets_leader datasheets_leader) throws java.sql.SQLException {
		return Dao.update(
			"INSERT INTO Datasheets_leader (leader_id, attached_id) VALUES (?, ?)",
			datasheets_leader.leader_id(),
			datasheets_leader.attached_id()
		);
	}

	public static Datasheets_leader getDatasheets_leaderByPk(String leader_id, String attached_id) throws java.sql.SQLException {
		return Dao.query(
			"SELECT * FROM Datasheets_leader WHERE leader_id = ? AND attached_id = ?",
			rs -> new Datasheets_leader(
				rs.getString("leader_id"),
				rs.getString("attached_id")
			),
			leader_id, attached_id
		).stream().findFirst().orElse(null);
	}

	public static java.util.List<Datasheets_leader> getAllDatasheets_leader() throws java.sql.SQLException {
		return Dao.query(
			"SELECT * FROM Datasheets_leader",
			rs -> new Datasheets_leader(
				rs.getString("leader_id"),
				rs.getString("attached_id")
			)
		);
	}


	public static void deleteDatasheets_leader(Datasheets_leader datasheets_leader) throws java.sql.SQLException {
		Dao.update(
			"DELETE FROM Datasheets_leader WHERE leader_id = ? AND attached_id = ?",
			datasheets_leader.leader_id(),
			datasheets_leader.attached_id()
		);
	}

}
