//-- Auto Generated Java File --

package eecs2311.group2.wh40k_easycombat.repository;

import eecs2311.group2.wh40k_easycombat.db.Dao;
import eecs2311.group2.wh40k_easycombat.model.Datasheets_enhancements;
import eecs2311.group2.wh40k_easycombat.util.IntListCodec;

import java.util.List;
import java.sql.SQLException;

@SuppressWarnings("unused")
public class Datasheets_enhancementsRepository {

	public static int addNewDatasheets_enhancements(Datasheets_enhancements datasheets_enhancements) throws java.sql.SQLException {
		return Dao.update(
			"INSERT INTO Datasheets_enhancements (auto_id, datasheet_id, enhancement_id) VALUES (?, ?, ?)",
			datasheets_enhancements.auto_id(),
			datasheets_enhancements.datasheet_id(),
			datasheets_enhancements.enhancement_id()
		);
	}

	public static Datasheets_enhancements getDatasheets_enhancementsByPk(int auto_id) throws java.sql.SQLException {
		return Dao.query(
			"SELECT * FROM Datasheets_enhancements WHERE auto_id = ?",
			rs -> new Datasheets_enhancements(
				rs.getInt("auto_id"),
				rs.getString("datasheet_id"),
				rs.getString("enhancement_id")
			),
			auto_id
		).stream().findFirst().orElse(null);
	}

	public static java.util.List<Datasheets_enhancements> getAllDatasheets_enhancements() throws java.sql.SQLException {
		return Dao.query(
			"SELECT * FROM Datasheets_enhancements",
			rs -> new Datasheets_enhancements(
				rs.getInt("auto_id"),
				rs.getString("datasheet_id"),
				rs.getString("enhancement_id")
			)
		);
	}

	public static void updateDatasheets_enhancements(Datasheets_enhancements datasheets_enhancements) throws java.sql.SQLException {
		Dao.update(
			"UPDATE Datasheets_enhancements SET datasheet_id = ?, enhancement_id = ? WHERE auto_id = ?",
			datasheets_enhancements.datasheet_id(),
			datasheets_enhancements.enhancement_id(),
			datasheets_enhancements.auto_id()
		);
	}

	public static void deleteDatasheets_enhancements(Datasheets_enhancements datasheets_enhancements) throws java.sql.SQLException {
		Dao.update(
			"DELETE FROM Datasheets_enhancements WHERE auto_id = ?",
			datasheets_enhancements.auto_id()
		);
	}

}
