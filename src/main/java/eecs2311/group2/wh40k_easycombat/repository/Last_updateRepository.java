//-- Auto Generated Java File --

package eecs2311.group2.wh40k_easycombat.repository;

import eecs2311.group2.wh40k_easycombat.db.Dao;
import eecs2311.group2.wh40k_easycombat.model.Last_update;
import eecs2311.group2.wh40k_easycombat.util.IntListCodec;

import java.util.List;
import java.sql.SQLException;

@SuppressWarnings("unused")
public class Last_updateRepository {

	public static int addNewLast_update(Last_update last_update) throws java.sql.SQLException {
		return Dao.update(
			"INSERT INTO Last_update (last_update) VALUES (?)",
			last_update.last_update()
		);
	}


	public static java.util.List<Last_update> getAllLast_update() throws java.sql.SQLException {
		return Dao.query(
			"SELECT * FROM Last_update",
			rs -> new Last_update(
				rs.getString("last_update")
			)
		);
	}



}
