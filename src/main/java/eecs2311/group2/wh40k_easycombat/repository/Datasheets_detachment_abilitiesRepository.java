//-- Auto Generated Java File --

package eecs2311.group2.wh40k_easycombat.repository;

import eecs2311.group2.wh40k_easycombat.db.Dao;
import eecs2311.group2.wh40k_easycombat.model.Datasheets_detachment_abilities;
import eecs2311.group2.wh40k_easycombat.util.IntListCodec;

import java.util.List;
import java.sql.SQLException;

@SuppressWarnings("unused")
public class Datasheets_detachment_abilitiesRepository {

	public static int addNewDatasheets_detachment_abilities(Datasheets_detachment_abilities datasheets_detachment_abilities) throws java.sql.SQLException {
		return Dao.update(
			"INSERT INTO Datasheets_detachment_abilities (datasheet_id, detachment_ability_id) VALUES (?, ?)",
			datasheets_detachment_abilities.datasheet_id(),
			datasheets_detachment_abilities.detachment_ability_id()
		);
	}

	public static Datasheets_detachment_abilities getDatasheets_detachment_abilitiesByPk(String datasheet_id, String detachment_ability_id) throws java.sql.SQLException {
		return Dao.query(
			"SELECT * FROM Datasheets_detachment_abilities WHERE datasheet_id = ? AND detachment_ability_id = ?",
			rs -> new Datasheets_detachment_abilities(
				rs.getString("datasheet_id"),
				rs.getString("detachment_ability_id")
			),
			datasheet_id, detachment_ability_id
		).stream().findFirst().orElse(null);
	}

	public static java.util.List<Datasheets_detachment_abilities> getAllDatasheets_detachment_abilities() throws java.sql.SQLException {
		return Dao.query(
			"SELECT * FROM Datasheets_detachment_abilities",
			rs -> new Datasheets_detachment_abilities(
				rs.getString("datasheet_id"),
				rs.getString("detachment_ability_id")
			)
		);
	}


	public static void deleteDatasheets_detachment_abilities(Datasheets_detachment_abilities datasheets_detachment_abilities) throws java.sql.SQLException {
		Dao.update(
			"DELETE FROM Datasheets_detachment_abilities WHERE datasheet_id = ? AND detachment_ability_id = ?",
			datasheets_detachment_abilities.datasheet_id(),
			datasheets_detachment_abilities.detachment_ability_id()
		);
	}

}
