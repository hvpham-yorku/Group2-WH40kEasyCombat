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
			"INSERT INTO Datasheets_wargear (auto_id, datasheet_id, line, line_in_wargear, dice, name, description, range, type, A, BS_WS, S, AP, D) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)",
			datasheets_wargear.auto_id(),
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

	public static Datasheets_wargear getDatasheets_wargearByPk(int auto_id) throws java.sql.SQLException {
		return Dao.query(
			"SELECT * FROM Datasheets_wargear WHERE auto_id = ?",
			rs -> new Datasheets_wargear(
				rs.getInt("auto_id"),
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
			auto_id
		).stream().findFirst().orElse(null);
	}

	public static java.util.List<Datasheets_wargear> getAllDatasheets_wargear() throws java.sql.SQLException {
		return Dao.query(
			"SELECT * FROM Datasheets_wargear",
			rs -> new Datasheets_wargear(
				rs.getInt("auto_id"),
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
			"UPDATE Datasheets_wargear SET datasheet_id = ?, line = ?, line_in_wargear = ?, dice = ?, name = ?, description = ?, range = ?, type = ?, A = ?, BS_WS = ?, S = ?, AP = ?, D = ? WHERE auto_id = ?",
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
			datasheets_wargear.D(),
			datasheets_wargear.auto_id()
		);
	}

	public static void deleteDatasheets_wargear(Datasheets_wargear datasheets_wargear) throws java.sql.SQLException {
		Dao.update(
			"DELETE FROM Datasheets_wargear WHERE auto_id = ?",
			datasheets_wargear.auto_id()
		);
	}

}
