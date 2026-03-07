//-- Auto Generated Java File --

package eecs2311.group2.wh40k_easycombat.repository;

import eecs2311.group2.wh40k_easycombat.db.Dao;
import eecs2311.group2.wh40k_easycombat.model.Datasheets_unit_composition;
import eecs2311.group2.wh40k_easycombat.util.IntListCodec;

import java.util.List;
import java.sql.SQLException;

@SuppressWarnings("unused")
public class Datasheets_unit_compositionRepository {

	public static int addNewDatasheets_unit_composition(Datasheets_unit_composition datasheets_unit_composition) throws java.sql.SQLException {
		return Dao.update(
			"INSERT INTO Datasheets_unit_composition (auto_id, datasheet_id, line, description) VALUES (?, ?, ?, ?)",
			datasheets_unit_composition.auto_id(),
			datasheets_unit_composition.datasheet_id(),
			datasheets_unit_composition.line(),
			datasheets_unit_composition.description()
		);
	}

	public static Datasheets_unit_composition getDatasheets_unit_compositionByPk(int auto_id) throws java.sql.SQLException {
		return Dao.query(
			"SELECT * FROM Datasheets_unit_composition WHERE auto_id = ?",
			rs -> new Datasheets_unit_composition(
				rs.getInt("auto_id"),
				rs.getString("datasheet_id"),
				rs.getString("line"),
				rs.getString("description")
			),
			auto_id
		).stream().findFirst().orElse(null);
	}

	public static java.util.List<Datasheets_unit_composition> getAllDatasheets_unit_composition() throws java.sql.SQLException {
		return Dao.query(
			"SELECT * FROM Datasheets_unit_composition",
			rs -> new Datasheets_unit_composition(
				rs.getInt("auto_id"),
				rs.getString("datasheet_id"),
				rs.getString("line"),
				rs.getString("description")
			)
		);
	}

	public static void updateDatasheets_unit_composition(Datasheets_unit_composition datasheets_unit_composition) throws java.sql.SQLException {
		Dao.update(
			"UPDATE Datasheets_unit_composition SET datasheet_id = ?, line = ?, description = ? WHERE auto_id = ?",
			datasheets_unit_composition.datasheet_id(),
			datasheets_unit_composition.line(),
			datasheets_unit_composition.description(),
			datasheets_unit_composition.auto_id()
		);
	}

	public static void deleteDatasheets_unit_composition(Datasheets_unit_composition datasheets_unit_composition) throws java.sql.SQLException {
		Dao.update(
			"DELETE FROM Datasheets_unit_composition WHERE auto_id = ?",
			datasheets_unit_composition.auto_id()
		);
	}

}
