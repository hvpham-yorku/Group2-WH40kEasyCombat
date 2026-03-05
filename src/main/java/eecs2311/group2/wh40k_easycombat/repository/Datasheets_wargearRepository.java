//-- Auto Generated Java File --

package eecs2311.group2.wh40k_easycombat.repository;

import eecs2311.group2.wh40k_easycombat.db.Dao;
import eecs2311.group2.wh40k_easycombat.model.Datasheets_wargear;
import eecs2311.group2.wh40k_easycombat.util.IntListCodec;

import java.util.List;
import java.sql.SQLException;

@SuppressWarnings("unused")
public class Datasheets_wargearRepository {

	public static int addNewDatasheets_wargear(Datasheets_wargear datasheets_wargear) throws java.sql.SQLException {
		return Dao.update(
			"INSERT INTO Datasheets_wargear (datasheet_id, line, line_in_wargear, dice, name, description, range, type, A, BS_WS, S, AP, D) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)",
			datasheets_wargear.datasheet_id(),
			datasheets_wargear.line(),
			datasheets_wargear.line_in_wargear(),
			datasheets_wargear.dice(),
			datasheets_wargear.name(),
			datasheets_wargear.description(),
			datasheets_wargear.range(),
			datasheets_wargear.type(),
			datasheets_wargear.A(),
			datasheets_wargear.BS_WS(),
			datasheets_wargear.S(),
			datasheets_wargear.AP(),
			datasheets_wargear.D()
		);
	}

	public static Datasheets_wargear getDatasheets_wargearByPk(String datasheet_id, String line, String line_in_wargear) throws java.sql.SQLException {
		return Dao.query(
			"SELECT * FROM Datasheets_wargear WHERE datasheet_id = ? AND line = ? AND line_in_wargear = ?",
			rs -> new Datasheets_wargear(
				rs.getString("datasheet_id"),
				rs.getString("line"),
				rs.getString("line_in_wargear"),
				rs.getString("dice"),
				rs.getString("name"),
				rs.getString("description"),
				rs.getString("range"),
				rs.getString("type"),
				rs.getString("A"),
				rs.getString("BS_WS"),
				rs.getString("S"),
				rs.getString("AP"),
				rs.getString("D")
			),
			datasheet_id, line, line_in_wargear
		).stream().findFirst().orElse(null);
	}

	public static java.util.List<Datasheets_wargear> getAllDatasheets_wargear() throws java.sql.SQLException {
		return Dao.query(
			"SELECT * FROM Datasheets_wargear",
			rs -> new Datasheets_wargear(
				rs.getString("datasheet_id"),
				rs.getString("line"),
				rs.getString("line_in_wargear"),
				rs.getString("dice"),
				rs.getString("name"),
				rs.getString("description"),
				rs.getString("range"),
				rs.getString("type"),
				rs.getString("A"),
				rs.getString("BS_WS"),
				rs.getString("S"),
				rs.getString("AP"),
				rs.getString("D")
			)
		);
	}

	public static void updateDatasheets_wargear(Datasheets_wargear datasheets_wargear) throws java.sql.SQLException {
		Dao.update(
			"UPDATE Datasheets_wargear SET dice = ?, name = ?, description = ?, range = ?, type = ?, A = ?, BS_WS = ?, S = ?, AP = ?, D = ? WHERE datasheet_id = ? AND line = ? AND line_in_wargear = ?",
			datasheets_wargear.dice(),
			datasheets_wargear.name(),
			datasheets_wargear.description(),
			datasheets_wargear.range(),
			datasheets_wargear.type(),
			datasheets_wargear.A(),
			datasheets_wargear.BS_WS(),
			datasheets_wargear.S(),
			datasheets_wargear.AP(),
			datasheets_wargear.D(),
			datasheets_wargear.datasheet_id(),
			datasheets_wargear.line(),
			datasheets_wargear.line_in_wargear()
		);
	}

	public static void deleteDatasheets_wargear(Datasheets_wargear datasheets_wargear) throws java.sql.SQLException {
		Dao.update(
			"DELETE FROM Datasheets_wargear WHERE datasheet_id = ? AND line = ? AND line_in_wargear = ?",
			datasheets_wargear.datasheet_id(),
			datasheets_wargear.line(),
			datasheets_wargear.line_in_wargear()
		);
	}

}
