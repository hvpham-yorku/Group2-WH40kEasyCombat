//-- Auto Generated Java File --

package eecs2311.group2.wh40k_easycombat.repository;

import eecs2311.group2.wh40k_easycombat.db.Dao;
import eecs2311.group2.wh40k_easycombat.model.Datasheets;
import eecs2311.group2.wh40k_easycombat.util.IntListCodec;

import java.util.List;
import java.sql.SQLException;

@SuppressWarnings("unused")
public class DatasheetsRepository {

	public static int addNewDatasheets(Datasheets datasheets) throws java.sql.SQLException {
		return Dao.update(
			"INSERT INTO Datasheets (id, name, faction_id, source_id, legend, role, loadout, transport, virtual, leader_head, leader_footer, damaged_w, damaged_description, link) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)",
			datasheets.id(),
			datasheets.name(),
			datasheets.faction_id(),
			datasheets.source_id(),
			datasheets.legend(),
			datasheets.role(),
			datasheets.loadout(),
			datasheets.transport(),
			datasheets.virtual(),
			datasheets.leader_head(),
			datasheets.leader_footer(),
			datasheets.damaged_w(),
			datasheets.damaged_description(),
			datasheets.link()
		);
	}

	public static Datasheets getDatasheetsByPk(String id) throws java.sql.SQLException {
		return Dao.query(
			"SELECT * FROM Datasheets WHERE id = ?",
			rs -> new Datasheets(
				rs.getString("id"),
				rs.getString("name"),
				rs.getString("faction_id"),
				rs.getString("source_id"),
				rs.getString("legend"),
				rs.getString("role"),
				rs.getString("loadout"),
				rs.getString("transport"),
				rs.getBoolean("virtual"),
				rs.getString("leader_head"),
				rs.getString("leader_footer"),
				rs.getString("damaged_w"),
				rs.getString("damaged_description"),
				rs.getString("link")
			),
			id
		).stream().findFirst().orElse(null);
	}

	public static java.util.List<Datasheets> getAllDatasheets() throws java.sql.SQLException {
		return Dao.query(
			"SELECT * FROM Datasheets",
			rs -> new Datasheets(
				rs.getString("id"),
				rs.getString("name"),
				rs.getString("faction_id"),
				rs.getString("source_id"),
				rs.getString("legend"),
				rs.getString("role"),
				rs.getString("loadout"),
				rs.getString("transport"),
				rs.getBoolean("virtual"),
				rs.getString("leader_head"),
				rs.getString("leader_footer"),
				rs.getString("damaged_w"),
				rs.getString("damaged_description"),
				rs.getString("link")
			)
		);
	}

	public static void updateDatasheets(Datasheets datasheets) throws java.sql.SQLException {
		Dao.update(
			"UPDATE Datasheets SET name = ?, faction_id = ?, source_id = ?, legend = ?, role = ?, loadout = ?, transport = ?, virtual = ?, leader_head = ?, leader_footer = ?, damaged_w = ?, damaged_description = ?, link = ? WHERE id = ?",
			datasheets.name(),
			datasheets.faction_id(),
			datasheets.source_id(),
			datasheets.legend(),
			datasheets.role(),
			datasheets.loadout(),
			datasheets.transport(),
			datasheets.virtual(),
			datasheets.leader_head(),
			datasheets.leader_footer(),
			datasheets.damaged_w(),
			datasheets.damaged_description(),
			datasheets.link(),
			datasheets.id()
		);
	}

	public static void deleteDatasheets(Datasheets datasheets) throws java.sql.SQLException {
		Dao.update(
			"DELETE FROM Datasheets WHERE id = ?",
			datasheets.id()
		);
	}

}
