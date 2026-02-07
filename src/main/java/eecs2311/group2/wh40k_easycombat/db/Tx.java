package eecs2311.group2.wh40k_easycombat.db;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.function.Function;

public final class Tx {

    /**
     * Tx.run(conn -> {
     *   Dao.update(conn, "... insert ...");
     *   Dao.update(conn, "... delete ...");
     *   return null;
     * });
     * 
     * Tx.run(conn -> {
     *     Dao.update(conn,
     *         "UPDATE Unit SET points = ? WHERE id = ?",
     *         150, 1
     *     );
     *
     *     Dao.update(conn,
     *         "INSERT INTO UnitLog (unitId, action) VALUES (?, ?)",
     *         1, "points updated to 150"
     *     );
     *
     *     return null;
     * });
     * 
     * Tx.run(conn -> {
     *     List<Unit> list = Dao.query(conn,
     *         "SELECT * FROM Unit WHERE factionId = ?",
     *         rs -> new Unit(
     *             rs.getInt("id"),
     *             rs.getInt("factionId"),
     *             rs.getString("name"),
     *             rs.getInt("points"),
     *             rs.getInt("M"),
     *             rs.getInt("T"),
     *             rs.getInt("SV"),
     *             rs.getInt("W"),
     *             rs.getInt("LD"),
     *             rs.getInt("OC"),
     *             rs.getInt("category"),
     *             rs.getString("composition"),
     *             StringListCodec.decode(rs.getString("keywordIdList")),
     *             StringListCodec.decode(rs.getString("rangedWeaponIdList")),
     *             StringListCodec.decode(rs.getString("meleeWeaponIdList"))
     *         ),
     *         100  // factionId = 100
     *     );
     *     return list;
     * });
     * 
     * @param body
     * @return
     * @param <T>
     * @throws SQLException
     */
    public static <T> T run(
        Function<Connection, T> body
    ) throws SQLException {

        try (Connection conn = Database.getConnection()) {
            conn.setAutoCommit(false);
            try {
                T result = body.apply(conn);
                conn.commit();
                return result;
            } catch (Exception e) {
                conn.rollback();
                throw e;
            }
        }
    }

    private Tx() {}
}