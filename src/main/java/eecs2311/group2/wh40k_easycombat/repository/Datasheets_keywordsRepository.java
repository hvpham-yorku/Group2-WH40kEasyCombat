//-- Auto Generated Java File --

package eecs2311.group2.wh40k_easycombat.repository;

import eecs2311.group2.wh40k_easycombat.db.Dao;
import eecs2311.group2.wh40k_easycombat.model.Datasheets_keywords;
import eecs2311.group2.wh40k_easycombat.util.IntListCodec;

import java.util.List;
import java.sql.SQLException;

@SuppressWarnings("unused")
public class Datasheets_keywordsRepository {

	public static int addNewDatasheets_keywords(Datasheets_keywords datasheets_keywords) throws java.sql.SQLException {
		return Dao.update(
			"INSERT INTO Datasheets_keywords (datasheet_id, keyword, model, is_faction_keyword) VALUES (?, ?, ?, ?)",
			datasheets_keywords.datasheet_id(),
			datasheets_keywords.keyword(),
			datasheets_keywords.model(),
			datasheets_keywords.is_faction_keyword()
		);
	}

	public static Datasheets_keywords getDatasheets_keywordsByPk(String datasheet_id, String keyword, String model) throws java.sql.SQLException {
		return Dao.query(
			"SELECT * FROM Datasheets_keywords WHERE datasheet_id = ? AND keyword = ? AND model = ?",
			rs -> new Datasheets_keywords(
				rs.getString("datasheet_id"),
				rs.getString("keyword"),
				rs.getString("model"),
				rs.getString("is_faction_keyword")
			),
			datasheet_id, keyword, model
		).stream().findFirst().orElse(null);
	}

	public static java.util.List<Datasheets_keywords> getAllDatasheets_keywords() throws java.sql.SQLException {
		return Dao.query(
			"SELECT * FROM Datasheets_keywords",
			rs -> new Datasheets_keywords(
				rs.getString("datasheet_id"),
				rs.getString("keyword"),
				rs.getString("model"),
				rs.getString("is_faction_keyword")
			)
		);
	}

	public static void updateDatasheets_keywords(Datasheets_keywords datasheets_keywords) throws java.sql.SQLException {
		Dao.update(
			"UPDATE Datasheets_keywords SET is_faction_keyword = ? WHERE datasheet_id = ? AND keyword = ? AND model = ?",
			datasheets_keywords.is_faction_keyword(),
			datasheets_keywords.datasheet_id(),
			datasheets_keywords.keyword(),
			datasheets_keywords.model()
		);
	}

	public static void deleteDatasheets_keywords(Datasheets_keywords datasheets_keywords) throws java.sql.SQLException {
		Dao.update(
			"DELETE FROM Datasheets_keywords WHERE datasheet_id = ? AND keyword = ? AND model = ?",
			datasheets_keywords.datasheet_id(),
			datasheets_keywords.keyword(),
			datasheets_keywords.model()
		);
	}

}
