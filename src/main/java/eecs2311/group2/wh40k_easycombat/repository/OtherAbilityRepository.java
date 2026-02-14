//-- Auto Generated Java File --

package eecs2311.group2.wh40k_easycombat.repository;

import eecs2311.group2.wh40k_easycombat.db.Dao;
import eecs2311.group2.wh40k_easycombat.model.OtherAbilities;
import eecs2311.group2.wh40k_easycombat.util.IntListCodec;

import java.util.List;
import java.sql.SQLException;

@SuppressWarnings("unused")
public class OtherAbilityRepository {
		public static int addNewOtherAbility(OtherAbilities otherability) throws SQLException {
				return Dao.update(
						"INSERT INTO other_abilities (ability) VALUES (?)",
						otherability.ability()
				);
		}
		public static OtherAbilities getOtherAbilityById(int id) throws SQLException {
				return Dao.query(
						"SELECT * FROM other_abilities WHERE id = ?",
						rs -> new OtherAbilities(
								rs.getInt("id"),
								rs.getString("ability")						),
						id
				).stream().findFirst().orElse(null);
		}
		public static List<OtherAbilities> getAllOtherAbilities() throws SQLException {
				return Dao.query(
						"SELECT * FROM other_abilities",
						rs -> new OtherAbilities(
								rs.getInt("id"),
								rs.getString("ability")						)
				);
		}
		public static void updateOtherAbility(OtherAbilities otherability) throws SQLException {
				Dao.update(
						"UPDATE other_abilities SET ability = ? WHERE id = ?",
						otherability.ability(),
						otherability.id()
				);
		}
		public static void deleteOtherAbility(OtherAbilities otherability) throws SQLException {
				Dao.update(
						"DELETE FROM other_abilities WHERE id = ?",
						otherability.id()
				);
		}
}
