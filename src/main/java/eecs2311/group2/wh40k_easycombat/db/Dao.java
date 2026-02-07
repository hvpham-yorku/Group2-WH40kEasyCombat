package eecs2311.group2.wh40k_easycombat.db;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class Dao {
    public interface RowMapper<T> {
        T map(ResultSet rs) throws SQLException;
    }

    /**
     * Dao.query(
     *     "SELECT * FROM Unit WHERE id = ?",
     *     rs -> new Unit(...),
     *     123
     * ).stream().findFirst().orElse(null);
     *
     * For SELECT
     * 
     * @param sql
     * @param mapper
     * @param params
     * @param <T>
     * @return
     * @throws SQLException
     */
    public static <T> List<T> query(
        String sql,
        RowMapper<T> mapper,
        Object... params
    ) throws SQLException {

        try (Connection conn = Database.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            bind(ps, params);

            try (ResultSet rs = ps.executeQuery()) {
                List<T> result = new ArrayList<>();
                while (rs.next()) {
                    result.add(mapper.map(rs));
                }
                return result;
            }
        }
    }

    /**
     * Dao.update(
     *     "INSERT INTO Unit (id, name, points, keywordIdList) VALUES (?, ?, ?, ?)",
     *     unit.id(), unit.name(), unit.points(),
     *     StringListCodec.encode(unit.keywordIdList())
     * );
     *
     * For INSERT/UPDATE/DELETE
     * 
     * @param sql
     * @param params
     * @return
     * @throws SQLException
     */
    public static int update(
        String sql,
        Object... params
    ) throws SQLException {

        try (Connection conn = Database.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            bind(ps, params);
            return ps.executeUpdate();
        }
    }

    // For Tx
    public static <T> List<T> query(
        Connection conn,
        String sql,
        RowMapper<T> mapper,
        Object... params
    ) throws SQLException {

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            bind(ps, params);
            try (ResultSet rs = ps.executeQuery()) {
                List<T> result = new ArrayList<>();
                while (rs.next()) {
                    result.add(mapper.map(rs));
                }
                return result;
            }
        }
    }

    // For Tx
    public static int update(
        Connection conn,
        String sql,
        Object... params
    ) throws SQLException {

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            bind(ps, params);
            return ps.executeUpdate();
        }
    }

    private static void bind(
        PreparedStatement ps,
        Object[] params
    ) throws SQLException {

        for (int i = 0; i < params.length; i++) {
            ps.setObject(i + 1, params[i]);
        }
    }

    private Dao() {
    }
}
