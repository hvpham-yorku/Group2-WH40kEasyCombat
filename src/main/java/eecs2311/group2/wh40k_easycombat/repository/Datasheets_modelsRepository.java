//-- Auto Generated Java File --

package eecs2311.group2.wh40k_easycombat.repository;

import eecs2311.group2.wh40k_easycombat.db.Dao;
import eecs2311.group2.wh40k_easycombat.model.Datasheets_models;
import eecs2311.group2.wh40k_easycombat.util.IntListCodec;

import java.util.List;
import java.sql.SQLException;

@SuppressWarnings("unused")
public class Datasheets_modelsRepository {

	public static int addNewDatasheets_models(Datasheets_models datasheets_models) throws java.sql.SQLException {
		return Dao.update(
			"INSERT INTO Datasheets_models (datasheet_id, line, name, M, T, Sv, inv_sv, inv_sv_descr, W, Ld, OC, base_size, base_size_descr) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)",
			datasheets_models.datasheet_id(),
			datasheets_models.line(),
			datasheets_models.name(),
			datasheets_models.M(),
			datasheets_models.T(),
			datasheets_models.Sv(),
			datasheets_models.inv_sv(),
			datasheets_models.inv_sv_descr(),
			datasheets_models.W(),
			datasheets_models.Ld(),
			datasheets_models.OC(),
			datasheets_models.base_size(),
			datasheets_models.base_size_descr()
		);
	}

	public static Datasheets_models getDatasheets_modelsByPk(String datasheet_id, String line) throws java.sql.SQLException {
		return Dao.query(
			"SELECT * FROM Datasheets_models WHERE datasheet_id = ? AND line = ?",
			rs -> new Datasheets_models(
				rs.getString("datasheet_id"),
				rs.getString("line"),
				rs.getString("name"),
				rs.getString("M"),
				rs.getString("T"),
				rs.getString("Sv"),
				rs.getString("inv_sv"),
				rs.getString("inv_sv_descr"),
				rs.getString("W"),
				rs.getString("Ld"),
				rs.getString("OC"),
				rs.getString("base_size"),
				rs.getString("base_size_descr")
			),
			datasheet_id, line
		).stream().findFirst().orElse(null);
	}

	public static java.util.List<Datasheets_models> getAllDatasheets_models() throws java.sql.SQLException {
		return Dao.query(
			"SELECT * FROM Datasheets_models",
			rs -> new Datasheets_models(
				rs.getString("datasheet_id"),
				rs.getString("line"),
				rs.getString("name"),
				rs.getString("M"),
				rs.getString("T"),
				rs.getString("Sv"),
				rs.getString("inv_sv"),
				rs.getString("inv_sv_descr"),
				rs.getString("W"),
				rs.getString("Ld"),
				rs.getString("OC"),
				rs.getString("base_size"),
				rs.getString("base_size_descr")
			)
		);
	}

	public static void updateDatasheets_models(Datasheets_models datasheets_models) throws java.sql.SQLException {
		Dao.update(
			"UPDATE Datasheets_models SET name = ?, M = ?, T = ?, Sv = ?, inv_sv = ?, inv_sv_descr = ?, W = ?, Ld = ?, OC = ?, base_size = ?, base_size_descr = ? WHERE datasheet_id = ? AND line = ?",
			datasheets_models.name(),
			datasheets_models.M(),
			datasheets_models.T(),
			datasheets_models.Sv(),
			datasheets_models.inv_sv(),
			datasheets_models.inv_sv_descr(),
			datasheets_models.W(),
			datasheets_models.Ld(),
			datasheets_models.OC(),
			datasheets_models.base_size(),
			datasheets_models.base_size_descr(),
			datasheets_models.datasheet_id(),
			datasheets_models.line()
		);
	}

	public static void deleteDatasheets_models(Datasheets_models datasheets_models) throws java.sql.SQLException {
		Dao.update(
			"DELETE FROM Datasheets_models WHERE datasheet_id = ? AND line = ?",
			datasheets_models.datasheet_id(),
			datasheets_models.line()
		);
	}

}
