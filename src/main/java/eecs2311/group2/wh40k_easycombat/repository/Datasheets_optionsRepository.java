//-- Auto Generated Java File --

package eecs2311.group2.wh40k_easycombat.repository;

import eecs2311.group2.wh40k_easycombat.db.Dao;
import eecs2311.group2.wh40k_easycombat.model.Datasheets_options;
import eecs2311.group2.wh40k_easycombat.util.IntListCodec;

import java.util.List;
import java.sql.SQLException;

@SuppressWarnings("unused")
public class Datasheets_optionsRepository {

	public static int addNewDatasheets_options(Datasheets_options datasheets_options) throws java.sql.SQLException {
		return Dao.update(
			"INSERT INTO Datasheets_options (datasheet_id, line, button, description) VALUES (?, ?, ?, ?)",
			datasheets_options.datasheet_id(),
			datasheets_options.line(),
			datasheets_options.button(),
			datasheets_options.description()
		);
	}

	public static Datasheets_options getDatasheets_optionsByPk(String datasheet_id, String line) throws java.sql.SQLException {
		return Dao.query(
			"SELECT * FROM Datasheets_options WHERE datasheet_id = ? AND line = ?",
			rs -> new Datasheets_options(
				rs.getString("datasheet_id"),
				rs.getString("line"),
				rs.getString("button"),
				rs.getString("description")
			),
			datasheet_id, line
		).stream().findFirst().orElse(null);
	}

	public static java.util.List<Datasheets_options> getAllDatasheets_options() throws java.sql.SQLException {
		return Dao.query(
			"SELECT * FROM Datasheets_options",
			rs -> new Datasheets_options(
				rs.getString("datasheet_id"),
				rs.getString("line"),
				rs.getString("button"),
				rs.getString("description")
			)
		);
	}

	public static void updateDatasheets_options(Datasheets_options datasheets_options) throws java.sql.SQLException {
		Dao.update(
			"UPDATE Datasheets_options SET button = ?, description = ? WHERE datasheet_id = ? AND line = ?",
			datasheets_options.button(),
			datasheets_options.description(),
			datasheets_options.datasheet_id(),
			datasheets_options.line()
		);
	}

	public static void deleteDatasheets_options(Datasheets_options datasheets_options) throws java.sql.SQLException {
		Dao.update(
			"DELETE FROM Datasheets_options WHERE datasheet_id = ? AND line = ?",
			datasheets_options.datasheet_id(),
			datasheets_options.line()
		);
	}

}
