//-- Auto Generated Java File --

package eecs2311.group2.wh40k_easycombat.repository;

import eecs2311.group2.wh40k_easycombat.db.Dao;
import eecs2311.group2.wh40k_easycombat.model.Army;
import eecs2311.group2.wh40k_easycombat.util.IntListCodec;

import java.util.List;
import java.sql.SQLException;

@SuppressWarnings("unused")
public class ArmyRepository {

	public static int addNewArmy(Army army) throws java.sql.SQLException {
		return Dao.update(
			"INSERT INTO Army (auto_id, name, faction_id, warlord_id, total_points, isMarked) VALUES (?, ?, ?, ?, ?, ?)",
			army.auto_id(),
			army.name(),
			army.faction_id(),
			army.warlord_id(),
			army.total_points(),
			army.isMarked()
		);
	}

	public static Army getArmyByPk(int auto_id) throws java.sql.SQLException {
		return Dao.query(
			"SELECT * FROM Army WHERE auto_id = ?",
			rs -> new Army(
				rs.getInt("auto_id"),
				rs.getString("name"),
				rs.getString("faction_id"),
				rs.getString("warlord_id"),
				rs.getInt("total_points"),
				rs.getBoolean("isMarked")
			),
			auto_id
		).stream().findFirst().orElse(null);
	}

	public static java.util.List<Army> getAllArmy() throws java.sql.SQLException {
		return Dao.query(
			"SELECT * FROM Army",
			rs -> new Army(
				rs.getInt("auto_id"),
				rs.getString("name"),
				rs.getString("faction_id"),
				rs.getString("warlord_id"),
				rs.getInt("total_points"),
				rs.getBoolean("isMarked")
			)
		);
	}

	public static void updateArmy(Army army) throws java.sql.SQLException {
		Dao.update(
			"UPDATE Army SET name = ?, faction_id = ?, warlord_id = ?, total_points = ?, isMarked = ? WHERE auto_id = ?",
			army.name(),
			army.faction_id(),
			army.warlord_id(),
			army.total_points(),
			army.isMarked(),
			army.auto_id()
		);
	}

	public static void deleteArmy(Army army) throws java.sql.SQLException {
		Dao.update(
			"DELETE FROM Army WHERE auto_id = ?",
			army.auto_id()
		);
	}

}
