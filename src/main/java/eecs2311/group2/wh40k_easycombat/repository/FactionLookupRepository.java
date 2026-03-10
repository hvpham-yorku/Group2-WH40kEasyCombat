package eecs2311.group2.wh40k_easycombat.repository;

import eecs2311.group2.wh40k_easycombat.db.Dao;
import eecs2311.group2.wh40k_easycombat.model.Factions;

import java.sql.SQLException;

public final class FactionLookupRepository {

    public static Factions findById(String factionId) throws SQLException {
        if (factionId == null || factionId.isBlank()) {
            return null;
        }

        return Dao.query(
                "SELECT * FROM Factions WHERE id = ?",
                rs -> new Factions(
                        rs.getInt("auto_id"),
                        rs.getString("id"),
                        rs.getString("name"),
                        rs.getString("link")
                ),
                factionId
        ).stream().findFirst().orElse(null);
    }

    public static String findNameById(String factionId) throws SQLException {
        Factions faction = findById(factionId);
        return faction == null ? null : faction.name();
    }

    private FactionLookupRepository() {
    }
}