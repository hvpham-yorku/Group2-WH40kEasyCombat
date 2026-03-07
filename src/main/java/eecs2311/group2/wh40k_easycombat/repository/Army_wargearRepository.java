//-- Auto Generated Java File --

package eecs2311.group2.wh40k_easycombat.repository;

import eecs2311.group2.wh40k_easycombat.db.Dao;
import eecs2311.group2.wh40k_easycombat.model.Army_wargear;
import eecs2311.group2.wh40k_easycombat.util.IntListCodec;

import java.util.List;
import java.sql.SQLException;

@SuppressWarnings("unused")
public class Army_wargearRepository {

	public static int addNewArmy_wargear(Army_wargear army_wargear) throws java.sql.SQLException {
		return Dao.update(
			"INSERT INTO Army_wargear (auto_id, wargear_id, units_id, wargear_count) VALUES (?, ?, ?, ?)",
			army_wargear.auto_id(),
			army_wargear.wargear_id(),
			army_wargear.units_id(),
			army_wargear.wargear_count()
		);
	}

	public static Army_wargear getArmy_wargearByPk(int auto_id) throws java.sql.SQLException {
		return Dao.query(
			"SELECT * FROM Army_wargear WHERE auto_id = ?",
			rs -> new Army_wargear(
				rs.getInt("auto_id"),
				rs.getInt("wargear_id"),
				rs.getInt("units_id"),
				rs.getInt("wargear_count")
			),
			auto_id
		).stream().findFirst().orElse(null);
	}

	public static java.util.List<Army_wargear> getAllArmy_wargear() throws java.sql.SQLException {
		return Dao.query(
			"SELECT * FROM Army_wargear",
			rs -> new Army_wargear(
				rs.getInt("auto_id"),
				rs.getInt("wargear_id"),
				rs.getInt("units_id"),
				rs.getInt("wargear_count")
			)
		);
	}

	public static void updateArmy_wargear(Army_wargear army_wargear) throws java.sql.SQLException {
		Dao.update(
			"UPDATE Army_wargear SET wargear_id = ?, units_id = ?, wargear_count = ? WHERE auto_id = ?",
			army_wargear.wargear_id(),
			army_wargear.units_id(),
			army_wargear.wargear_count(),
			army_wargear.auto_id()
		);
	}

	public static void deleteArmy_wargear(Army_wargear army_wargear) throws java.sql.SQLException {
		Dao.update(
			"DELETE FROM Army_wargear WHERE auto_id = ?",
			army_wargear.auto_id()
		);
	}

}
