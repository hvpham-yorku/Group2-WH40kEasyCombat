//-- Auto Generated Java File --

package eecs2311.group2.wh40k_easycombat.repository;

import eecs2311.group2.wh40k_easycombat.db.Dao;
import eecs2311.group2.wh40k_easycombat.model.Army_units;
import eecs2311.group2.wh40k_easycombat.util.IntListCodec;

import java.util.List;
import java.sql.SQLException;

@SuppressWarnings("unused")
public class Army_unitsRepository {

	public static int addNewArmy_units(Army_units army_units) throws java.sql.SQLException {
		return Dao.update(
			"INSERT INTO Army_units (auto_id, army_id, datasheet_id, enhancements_id, model_count, unit_cost) VALUES (?, ?, ?, ?, ?, ?)",
			army_units.auto_id(),
			army_units.army_id(),
			army_units.datasheet_id(),
			army_units.enhancements_id(),
			army_units.model_count(),
			army_units.unit_cost()
		);
	}

	public static Army_units getArmy_unitsByPk(int auto_id) throws java.sql.SQLException {
		return Dao.query(
			"SELECT * FROM Army_units WHERE auto_id = ?",
			rs -> new Army_units(
				rs.getInt("auto_id"),
				rs.getInt("army_id"),
				rs.getString("datasheet_id"),
				rs.getString("enhancements_id"),
				rs.getInt("model_count"),
				rs.getInt("unit_cost")
			),
			auto_id
		).stream().findFirst().orElse(null);
	}

	public static java.util.List<Army_units> getAllArmy_units() throws java.sql.SQLException {
		return Dao.query(
			"SELECT * FROM Army_units",
			rs -> new Army_units(
				rs.getInt("auto_id"),
				rs.getInt("army_id"),
				rs.getString("datasheet_id"),
				rs.getString("enhancements_id"),
				rs.getInt("model_count"),
				rs.getInt("unit_cost")
			)
		);
	}

	public static void updateArmy_units(Army_units army_units) throws java.sql.SQLException {
		Dao.update(
			"UPDATE Army_units SET army_id = ?, datasheet_id = ?, enhancements_id = ?, model_count = ?, unit_cost = ? WHERE auto_id = ?",
			army_units.army_id(),
			army_units.datasheet_id(),
			army_units.enhancements_id(),
			army_units.model_count(),
			army_units.unit_cost(),
			army_units.auto_id()
		);
	}

	public static void deleteArmy_units(Army_units army_units) throws java.sql.SQLException {
		Dao.update(
			"DELETE FROM Army_units WHERE auto_id = ?",
			army_units.auto_id()
		);
	}

}
