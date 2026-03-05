//-- Auto Generated Java File --

package eecs2311.group2.wh40k_easycombat.repository;

import eecs2311.group2.wh40k_easycombat.db.Dao;
import eecs2311.group2.wh40k_easycombat.model.Datasheets_stratagems;
import eecs2311.group2.wh40k_easycombat.util.IntListCodec;

import java.util.List;
import java.sql.SQLException;

@SuppressWarnings("unused")
public class Datasheets_stratagemsRepository {

	public static int addNewDatasheets_stratagems(Datasheets_stratagems datasheets_stratagems) throws java.sql.SQLException {
		return Dao.update(
			"INSERT INTO Datasheets_stratagems (datasheet_id, stratagem_id) VALUES (?, ?)",
			datasheets_stratagems.datasheet_id(),
			datasheets_stratagems.stratagem_id()
		);
	}

	public static Datasheets_stratagems getDatasheets_stratagemsByPk(String datasheet_id, String stratagem_id) throws java.sql.SQLException {
		return Dao.query(
			"SELECT * FROM Datasheets_stratagems WHERE datasheet_id = ? AND stratagem_id = ?",
			rs -> new Datasheets_stratagems(
				rs.getString("datasheet_id"),
				rs.getString("stratagem_id")
			),
			datasheet_id, stratagem_id
		).stream().findFirst().orElse(null);
	}

	public static java.util.List<Datasheets_stratagems> getAllDatasheets_stratagems() throws java.sql.SQLException {
		return Dao.query(
			"SELECT * FROM Datasheets_stratagems",
			rs -> new Datasheets_stratagems(
				rs.getString("datasheet_id"),
				rs.getString("stratagem_id")
			)
		);
	}


	public static void deleteDatasheets_stratagems(Datasheets_stratagems datasheets_stratagems) throws java.sql.SQLException {
		Dao.update(
			"DELETE FROM Datasheets_stratagems WHERE datasheet_id = ? AND stratagem_id = ?",
			datasheets_stratagems.datasheet_id(),
			datasheets_stratagems.stratagem_id()
		);
	}

}
