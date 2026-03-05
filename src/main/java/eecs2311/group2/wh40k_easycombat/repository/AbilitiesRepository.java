//-- Auto Generated Java File --

package eecs2311.group2.wh40k_easycombat.repository;

import eecs2311.group2.wh40k_easycombat.db.Dao;
import eecs2311.group2.wh40k_easycombat.model.Abilities;
import eecs2311.group2.wh40k_easycombat.util.IntListCodec;

import java.util.List;
import java.sql.SQLException;

@SuppressWarnings("unused")
public class AbilitiesRepository {

	public static int addNewAbilities(Abilities abilities) throws java.sql.SQLException {
		return Dao.update(
			"INSERT INTO Abilities (id, faction_id, name, legend, description) VALUES (?, ?, ?, ?, ?)",
			abilities.id(),
			abilities.faction_id(),
			abilities.name(),
			abilities.legend(),
			abilities.description()
		);
	}

	public static Abilities getAbilitiesByPk(String id, String faction_id) throws java.sql.SQLException {
		return Dao.query(
			"SELECT * FROM Abilities WHERE id = ? AND faction_id = ?",
			rs -> new Abilities(
				rs.getString("id"),
				rs.getString("faction_id"),
				rs.getString("name"),
				rs.getString("legend"),
				rs.getString("description")
			),
			id, faction_id
		).stream().findFirst().orElse(null);
	}

	public static java.util.List<Abilities> getAllAbilities() throws java.sql.SQLException {
		return Dao.query(
			"SELECT * FROM Abilities",
			rs -> new Abilities(
				rs.getString("id"),
				rs.getString("faction_id"),
				rs.getString("name"),
				rs.getString("legend"),
				rs.getString("description")
			)
		);
	}

	public static void updateAbilities(Abilities abilities) throws java.sql.SQLException {
		Dao.update(
			"UPDATE Abilities SET name = ?, legend = ?, description = ? WHERE id = ? AND faction_id = ?",
			abilities.name(),
			abilities.legend(),
			abilities.description(),
			abilities.id(),
			abilities.faction_id()
		);
	}

	public static void deleteAbilities(Abilities abilities) throws java.sql.SQLException {
		Dao.update(
			"DELETE FROM Abilities WHERE id = ? AND faction_id = ?",
			abilities.id(),
			abilities.faction_id()
		);
	}

}
