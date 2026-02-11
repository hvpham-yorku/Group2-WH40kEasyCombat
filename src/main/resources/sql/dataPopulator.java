import java.sql.*;
import java.util.List;

public class dataPopulator {

    private static final String DB_URL = "jdbc:sqlite:app.db";
    private static final String USER = "";
    private static final String PASS = "";
    Connection conn = null;
    PreparedStatement stm = null;
    static ResultSet result = null;

    public static Connection getConnection() throws SQLException {
        Connection conn = DriverManager.getConnection(DB_URL);
        initPragma(conn);
        return conn;
    }

    private static void initPragma(Connection conn) throws SQLException {
        try (Statement st = conn.createStatement()) {
            st.execute("PRAGMA foreign_keys = ON");
            st.execute("PRAGMA journal_mode = WAL");
            st.execute("PRAGMA synchronous = NORMAL");
        }
    }

    public static void insertUnit(int id, String name, String faction, String type, int cost) throws SQLException {
        String sql = "INSERT INTO Unit (UnitID, UnitName, UnitFaction, UnitType, UnitCost) VALUES (?, ?, ?, ?, ?)";

        try (Connection conn = DriverManager.getConnection(DB_URL, USER, PASS);
             PreparedStatement stm = conn.prepareStatement(sql)) {
            stm.setInt(1, id);
            stm.setString(2, name);
            stm.setString(3, faction);
            stm.setString(4, type);
            stm.setInt(5, cost);
            stm.executeUpdate();
        }
    }

    public static void main(String[] args) {
        csvRead read = new csvRead();
        dataPopulator populator = new dataPopulator();
        List<String[]> data = read.readCSV("/Users/sank/Desktop/units.csv");

        for (String[] row : data) {
            try {
                int id = Integer.parseInt(row[0].trim());
                String name = row[1].trim();
                String faction = row[2].trim();
                String type = row[3].trim();
                int cost = Integer.parseInt(row[4].trim());
                populator.insertUnit(id, name, faction, type, cost);
            } catch (Exception e) {
                System.out.println("Skipping invalid row: " + String.join(",", row));
            }
        }
    }
}