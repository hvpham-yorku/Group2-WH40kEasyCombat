//-- Auto Generated Java File --

package eecs2311.group2.wh40k_easycombat.repository;

import eecs2311.group2.wh40k_easycombat.db.Dao;
import eecs2311.group2.wh40k_easycombat.model.Army_detachment;
import eecs2311.group2.wh40k_easycombat.util.IntListCodec;

import java.util.List;
import java.sql.SQLException;

@SuppressWarnings("unused")
public class Army_detachmentRepository {

	public static int addNewArmy_detachment(Army_detachment army_detachment) throws java.sql.SQLException {
		return Dao.update(
			"INSERT INTO Army_detachment (auto_id, army_id, datasheet_id, detachment_id) VALUES (?, ?, ?, ?)",
			army_detachment.auto_id(),
			army_detachment.army_id(),
			army_detachment.datasheet_id(),
			army_detachment.detachment_id()
		);
	}

	public static Army_detachment getArmy_detachmentByPk(int auto_id) throws java.sql.SQLException {
		return Dao.query(
			"SELECT * FROM Army_detachment WHERE auto_id = ?",
			rs -> new Army_detachment(
				rs.getInt("auto_id"),
				rs.getInt("army_id"),
				rs.getString("datasheet_id"),
				rs.getString("detachment_id")
			),
			auto_id
		).stream().findFirst().orElse(null);
	}

	public static java.util.List<Army_detachment> getAllArmy_detachment() throws java.sql.SQLException {
		return Dao.query(
			"SELECT * FROM Army_detachment",
			rs -> new Army_detachment(
				rs.getInt("auto_id"),
				rs.getInt("army_id"),
				rs.getString("datasheet_id"),
				rs.getString("detachment_id")
			)
		);
	}

	public static void updateArmy_detachment(Army_detachment army_detachment) throws java.sql.SQLException {
		Dao.update(
			"UPDATE Army_detachment SET army_id = ?, datasheet_id = ?, detachment_id = ? WHERE auto_id = ?",
			army_detachment.army_id(),
			army_detachment.datasheet_id(),
			army_detachment.detachment_id(),
			army_detachment.auto_id()
		);
	}

	public static void deleteArmy_detachment(Army_detachment army_detachment) throws java.sql.SQLException {
		Dao.update(
			"DELETE FROM Army_detachment WHERE auto_id = ?",
			army_detachment.auto_id()
		);
	}

}
