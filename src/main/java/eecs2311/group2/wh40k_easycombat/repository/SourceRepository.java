//-- Auto Generated Java File --

package eecs2311.group2.wh40k_easycombat.repository;

import eecs2311.group2.wh40k_easycombat.db.Dao;
import eecs2311.group2.wh40k_easycombat.model.Source;
import eecs2311.group2.wh40k_easycombat.util.IntListCodec;

import java.util.List;
import java.sql.SQLException;

@SuppressWarnings("unused")
public class SourceRepository {

	public static int addNewSource(Source source) throws java.sql.SQLException {
		return Dao.update(
			"INSERT INTO Source (id, name, type, edition, version, errata_date, errata_link) VALUES (?, ?, ?, ?, ?, ?, ?)",
			source.id(),
			source.name(),
			source.type(),
			source.edition(),
			source.version(),
			source.errata_date(),
			source.errata_link()
		);
	}

	public static Source getSourceByPk(String id) throws java.sql.SQLException {
		return Dao.query(
			"SELECT * FROM Source WHERE id = ?",
			rs -> new Source(
				rs.getString("id"),
				rs.getString("name"),
				rs.getString("type"),
				rs.getString("edition"),
				rs.getString("version"),
				rs.getString("errata_date"),
				rs.getString("errata_link")
			),
			id
		).stream().findFirst().orElse(null);
	}

	public static java.util.List<Source> getAllSource() throws java.sql.SQLException {
		return Dao.query(
			"SELECT * FROM Source",
			rs -> new Source(
				rs.getString("id"),
				rs.getString("name"),
				rs.getString("type"),
				rs.getString("edition"),
				rs.getString("version"),
				rs.getString("errata_date"),
				rs.getString("errata_link")
			)
		);
	}

	public static void updateSource(Source source) throws java.sql.SQLException {
		Dao.update(
			"UPDATE Source SET name = ?, type = ?, edition = ?, version = ?, errata_date = ?, errata_link = ? WHERE id = ?",
			source.name(),
			source.type(),
			source.edition(),
			source.version(),
			source.errata_date(),
			source.errata_link(),
			source.id()
		);
	}

	public static void deleteSource(Source source) throws java.sql.SQLException {
		Dao.update(
			"DELETE FROM Source WHERE id = ?",
			source.id()
		);
	}

}
