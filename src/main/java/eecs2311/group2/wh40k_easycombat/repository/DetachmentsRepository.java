//-- Auto Generated Java File --

package eecs2311.group2.wh40k_easycombat.repository;

import eecs2311.group2.wh40k_easycombat.db.Dao;
import eecs2311.group2.wh40k_easycombat.model.Detachments;
import eecs2311.group2.wh40k_easycombat.util.IntListCodec;

import java.util.List;
import java.sql.SQLException;

@SuppressWarnings("unused")
public class DetachmentsRepository {

	public static int addNewDetachments(Detachments detachments) throws java.sql.SQLException {
		return Dao.update(
			"INSERT INTO Detachments (id, faction_id, name, legend, type) VALUES (?, ?, ?, ?, ?)",
			detachments.id(),
			detachments.faction_id(),
			detachments.name(),
			detachments.legend(),
			detachments.type()
		);
	}

	public static Detachments getDetachmentsByPk(String id, String faction_id) throws java.sql.SQLException {
		return Dao.query(
			"SELECT * FROM Detachments WHERE id = ? AND faction_id = ?",
			rs -> new Detachments(
				rs.getString("id"),
				rs.getString("faction_id"),
				rs.getString("name"),
				rs.getString("legend"),
				rs.getString("type")
			),
			id, faction_id
		).stream().findFirst().orElse(null);
	}

	public static java.util.List<Detachments> getAllDetachments() throws java.sql.SQLException {
		return Dao.query(
			"SELECT * FROM Detachments",
			rs -> new Detachments(
				rs.getString("id"),
				rs.getString("faction_id"),
				rs.getString("name"),
				rs.getString("legend"),
				rs.getString("type")
			)
		);
	}

	public static void updateDetachments(Detachments detachments) throws java.sql.SQLException {
		Dao.update(
			"UPDATE Detachments SET name = ?, legend = ?, type = ? WHERE id = ? AND faction_id = ?",
			detachments.name(),
			detachments.legend(),
			detachments.type(),
			detachments.id(),
			detachments.faction_id()
		);
	}

	public static void deleteDetachments(Detachments detachments) throws java.sql.SQLException {
		Dao.update(
			"DELETE FROM Detachments WHERE id = ? AND faction_id = ?",
			detachments.id(),
			detachments.faction_id()
		);
	}

}
