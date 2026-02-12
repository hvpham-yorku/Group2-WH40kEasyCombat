//-- Auto Generated Java File --
package eecs2311.group2.wh40k_easycombat.repository;

import eecs2311.group2.wh40k_easycombat.db.Dao;
import eecs2311.group2.wh40k_easycombat.model.UnitKeywords;
import eecs2311.group2.wh40k_easycombat.util.IntListCodec;

import java.sql.SQLException;

@SuppressWarnings("unused")
public class UnitKeywordRepository {
		public static void insertUnitKeyword(UnitKeywords unitkeyword) throws SQLException {
				Dao.update(
						"INSERT INTO unit_keywords (keyword VALUES (?)",
						unitkeyword.keyword()
				);
		}
		public static UnitKeywords getUnitKeywordById(int id) throws SQLException {
				return Dao.query(
						"SELECT * FROM unit_keywords WHERE id = ?",
						rs -> new UnitKeywords(
								rs.getInt("id"),
								rs.getString("keyword")
						),
						id
				).stream().findFirst().orElse(null);
		}
		public static void updateUnitKeyword(UnitKeywords unitkeyword) throws SQLException {
				Dao.update(
						"UPDATE unit_keywords SET keyword = ? WHERE id = ?",
						unitkeyword.keyword(),
						unitkeyword.id()
				);
		}
		public static void deleteUnitKeyword(UnitKeywords unitkeyword) throws SQLException {
				Dao.update(
						"DELETE FROM unit_keywords WHERE id = ?",
						unitkeyword.id()
				);
		}
}