//-- Auto Generated Java File --
package eecs2311.group2.wh40k_easycombat.repository;

import eecs2311.group2.wh40k_easycombat.db.Dao;
import eecs2311.group2.wh40k_easycombat.model.WeaponKeywords;
import eecs2311.group2.wh40k_easycombat.util.IntListCodec;

import java.sql.SQLException;

@SuppressWarnings("unused")
public class WeaponKeywordRepository {
		public static void insertWeaponKeyword(WeaponKeywords weaponkeyword) throws SQLException {
				Dao.update(
						"INSERT INTO weapon_keywords (keyword VALUES (?)",
						weaponkeyword.keyword()
				);
		}
		public static WeaponKeywords getWeaponKeywordById(int id) throws SQLException {
				return Dao.query(
						"SELECT * FROM weapon_keywords WHERE id = ?",
						rs -> new WeaponKeywords(
								rs.getInt("id"),
								rs.getString("keyword")
						),
						id
				).stream().findFirst().orElse(null);
		}
		public static void updateWeaponKeyword(WeaponKeywords weaponkeyword) throws SQLException {
				Dao.update(
						"UPDATE weapon_keywords SET keyword = ? WHERE id = ?",
						weaponkeyword.keyword(),
						weaponkeyword.id()
				);
		}
		public static void deleteWeaponKeyword(WeaponKeywords weaponkeyword) throws SQLException {
				Dao.update(
						"DELETE FROM weapon_keywords WHERE id = ?",
						weaponkeyword.id()
				);
		}
}