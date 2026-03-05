//-- Auto Generated Java File --

package eecs2311.group2.wh40k_easycombat.repository;

import eecs2311.group2.wh40k_easycombat.db.Dao;
import eecs2311.group2.wh40k_easycombat.model.Datasheets_models_cost;
import eecs2311.group2.wh40k_easycombat.util.IntListCodec;

import java.util.List;
import java.sql.SQLException;

@SuppressWarnings("unused")
public class Datasheets_models_costRepository {

	public static int addNewDatasheets_models_cost(Datasheets_models_cost datasheets_models_cost) throws java.sql.SQLException {
		return Dao.update(
			"INSERT INTO Datasheets_models_cost (datasheet_id, line, description, cost) VALUES (?, ?, ?, ?)",
			datasheets_models_cost.datasheet_id(),
			datasheets_models_cost.line(),
			datasheets_models_cost.description(),
			datasheets_models_cost.cost()
		);
	}

	public static Datasheets_models_cost getDatasheets_models_costByPk(String datasheet_id, String line) throws java.sql.SQLException {
		return Dao.query(
			"SELECT * FROM Datasheets_models_cost WHERE datasheet_id = ? AND line = ?",
			rs -> new Datasheets_models_cost(
				rs.getString("datasheet_id"),
				rs.getString("line"),
				rs.getString("description"),
				rs.getString("cost")
			),
			datasheet_id, line
		).stream().findFirst().orElse(null);
	}

	public static java.util.List<Datasheets_models_cost> getAllDatasheets_models_cost() throws java.sql.SQLException {
		return Dao.query(
			"SELECT * FROM Datasheets_models_cost",
			rs -> new Datasheets_models_cost(
				rs.getString("datasheet_id"),
				rs.getString("line"),
				rs.getString("description"),
				rs.getString("cost")
			)
		);
	}

	public static void updateDatasheets_models_cost(Datasheets_models_cost datasheets_models_cost) throws java.sql.SQLException {
		Dao.update(
			"UPDATE Datasheets_models_cost SET description = ?, cost = ? WHERE datasheet_id = ? AND line = ?",
			datasheets_models_cost.description(),
			datasheets_models_cost.cost(),
			datasheets_models_cost.datasheet_id(),
			datasheets_models_cost.line()
		);
	}

	public static void deleteDatasheets_models_cost(Datasheets_models_cost datasheets_models_cost) throws java.sql.SQLException {
		Dao.update(
			"DELETE FROM Datasheets_models_cost WHERE datasheet_id = ? AND line = ?",
			datasheets_models_cost.datasheet_id(),
			datasheets_models_cost.line()
		);
	}

}
