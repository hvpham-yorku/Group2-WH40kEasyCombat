package eecs2311.group2.wh40k_easycombat.db;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public final class Database {
    private static final String URL = "jdbc:sqlite:app.db";

    public static Connection getConnection() throws SQLException {
        Connection conn = DriverManager.getConnection(URL);
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

    public static void executeSqlFolder(String folderPath) throws IOException, SQLException {
        Path folder = Path.of(folderPath);
        if (!Files.exists(folder) || !Files.isDirectory(folder)) {
            throw new IllegalArgumentException("Folder not found: " + folderPath);
        }

        try (Connection conn = Database.getConnection();
             Statement st = conn.createStatement()) {

            DirectoryStream<Path> stream = Files.newDirectoryStream(folder, "*.sql");
            for (Path file : stream) {
                String sql = Files.readString(file);
                String[] statements = sql.split(";");

                for (String stmt : statements) {
                    stmt = stmt.trim();
                    if (!stmt.isEmpty()) {
                        st.execute(stmt);
                    }
                }
            }
        }
    }

    private Database() {
    }
}
