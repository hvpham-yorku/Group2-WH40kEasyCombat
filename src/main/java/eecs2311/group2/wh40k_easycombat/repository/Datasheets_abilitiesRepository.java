//-- Auto Generated Java File --

package eecs2311.group2.wh40k_easycombat.repository;

import eecs2311.group2.wh40k_easycombat.db.Dao;
import eecs2311.group2.wh40k_easycombat.model.Datasheets_abilities;
import eecs2311.group2.wh40k_easycombat.util.IntListCodec;

import java.util.List;
import java.sql.SQLException;

@SuppressWarnings("unused")
public class Datasheets_abilitiesRepository {

	public static int addNewDatasheets_abilities(Datasheets_abilities datasheets_abilities) throws java.sql.SQLException {
		return Dao.update(
			"INSERT INTO Datasheets_abilities (datasheet_id, line, ability_id, model, name, description, type, parameter) VALUES (?, ?, ?, ?, ?, ?, ?, ?)",
			datasheets_abilities.datasheet_id(),
			datasheets_abilities.line(),
			datasheets_abilities.ability_id(),
			datasheets_abilities.model(),
			datasheets_abilities.name(),
			datasheets_abilities.description(),
			datasheets_abilities.type(),
			datasheets_abilities.parameter()
		);
	}

	public static Datasheets_abilities getDatasheets_abilitiesByPk(String datasheet_id, String line) throws java.sql.SQLException {
		return Dao.query(
			"SELECT * FROM Datasheets_abilities WHERE datasheet_id = ? AND line = ?",
			rs -> new Datasheets_abilities(
				rs.getString("datasheet_id"),
				rs.getString("line"),
				rs.getString("ability_id"),
				rs.getString("model"),
				rs.getString("name"),
				rs.getString("description"),
				rs.getString("type"),
				rs.getString("parameter")
			),
			datasheet_id, line
		).stream().findFirst().orElse(null);
	}

	public static java.util.List<Datasheets_abilities> getAllDatasheets_abilities() throws java.sql.SQLException {
		return Dao.query(
			"SELECT * FROM Datasheets_abilities",
			rs -> new Datasheets_abilities(
				rs.getString("datasheet_id"),
				rs.getString("line"),
				rs.getString("ability_id"),
				rs.getString("model"),
				rs.getString("name"),
				rs.getString("description"),
				rs.getString("type"),
				rs.getString("parameter")
			)
		);
	}

	public static void updateDatasheets_abilities(Datasheets_abilities datasheets_abilities) throws java.sql.SQLException {
		Dao.update(
			"UPDATE Datasheets_abilities SET ability_id = ?, model = ?, name = ?, description = ?, type = ?, parameter = ? WHERE datasheet_id = ? AND line = ?",
			datasheets_abilities.ability_id(),
			datasheets_abilities.model(),
			datasheets_abilities.name(),
			datasheets_abilities.description(),
			datasheets_abilities.type(),
			datasheets_abilities.parameter(),
			datasheets_abilities.datasheet_id(),
			datasheets_abilities.line()
		);
	}

	public static void deleteDatasheets_abilities(Datasheets_abilities datasheets_abilities) throws java.sql.SQLException {
		Dao.update(
			"DELETE FROM Datasheets_abilities WHERE datasheet_id = ? AND line = ?",
			datasheets_abilities.datasheet_id(),
			datasheets_abilities.line()
		);
	}

}
