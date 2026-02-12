//-- Auto Generated Java File --
package eecs2311.group2.wh40k_easycombat.repository;

import eecs2311.group2.wh40k_easycombat.db.Dao;
import eecs2311.group2.wh40k_easycombat.model.CoreAbilities;
import eecs2311.group2.wh40k_easycombat.util.IntListCodec;

import java.sql.SQLException;

@SuppressWarnings("unused")
public class CoreAbilityRepository {
		public static void insertCoreAbility(CoreAbilities coreability) throws SQLException {
				Dao.update(
						"INSERT INTO core_abilities (ability VALUES (?)",
						coreability.ability()
				);
		}
		public static CoreAbilities getCoreAbilityById(int id) throws SQLException {
				return Dao.query(
						"SELECT * FROM core_abilities WHERE id = ?",
						rs -> new CoreAbilities(
								rs.getInt("id"),
								rs.getString("ability")
						),
						id
				).stream().findFirst().orElse(null);
		}
		public static void updateCoreAbility(CoreAbilities coreability) throws SQLException {
				Dao.update(
						"UPDATE core_abilities SET ability = ? WHERE id = ?",
						coreability.ability(),
						coreability.id()
				);
		}
		public static void deleteCoreAbility(CoreAbilities coreability) throws SQLException {
				Dao.update(
						"DELETE FROM core_abilities WHERE id = ?",
						coreability.id()
				);
		}
}