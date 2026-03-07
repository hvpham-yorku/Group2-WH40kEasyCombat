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
			"INSERT INTO Datasheets_models_cost (auto_id, datasheet_id, line, description, cost) VALUES (?, ?, ?, ?, ?)",
			datasheets_models_cost.auto_id(),
			datasheets_models_cost.datasheet_id(),
			datasheets_models_cost.line(),
			datasheets_models_cost.description(),
			datasheets_models_cost.cost()
		);
	}

	public static Datasheets_models_cost getDatasheets_models_costByPk(int auto_id) throws java.sql.SQLException {
		return Dao.query(
			"SELECT * FROM Datasheets_models_cost WHERE auto_id = ?",
			rs -> new Datasheets_models_cost(
				rs.getInt("auto_id"),
				rs.getString("datasheet_id"),
				rs.getString("line"),
				rs.getString("description"),
				rs.getString("cost")
			),
			auto_id
		).stream().findFirst().orElse(null);
	}

	public static java.util.List<Datasheets_models_cost> getAllDatasheets_models_cost() throws java.sql.SQLException {
		return Dao.query(
			"SELECT * FROM Datasheets_models_cost",
			rs -> new Datasheets_models_cost(
				rs.getInt("auto_id"),
				rs.getString("datasheet_id"),
				rs.getString("line"),
				rs.getString("description"),
				rs.getString("cost")
			)
		);
	}

	public static void updateDatasheets_models_cost(Datasheets_models_cost datasheets_models_cost) throws java.sql.SQLException {
		Dao.update(
			"UPDATE Datasheets_models_cost SET datasheet_id = ?, line = ?, description = ?, cost = ? WHERE auto_id = ?",
			datasheets_models_cost.datasheet_id(),
			datasheets_models_cost.line(),
			datasheets_models_cost.description(),
			datasheets_models_cost.cost(),
			datasheets_models_cost.auto_id()
		);
	}

	public static void deleteDatasheets_models_cost(Datasheets_models_cost datasheets_models_cost) throws java.sql.SQLException {
		Dao.update(
			"DELETE FROM Datasheets_models_cost WHERE auto_id = ?",
			datasheets_models_cost.auto_id()
		);
	}

}
