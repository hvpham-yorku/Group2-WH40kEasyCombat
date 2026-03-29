package eecs2311.group2.wh40k_easycombat.db;

import eecs2311.group2.wh40k_easycombat.util.AppPaths;
import eecs2311.group2.wh40k_easycombat.util.SqlGenerator;
import eecs2311.group2.wh40k_easycombat.util.JavaGenerator;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;

public final class Database {
    private static Path databasePath = AppPaths.getDatabasePath();
    private static String URL = buildSqliteUrl(databasePath);

    public static void useApplicationDatabase() throws IOException {
        AppPaths.ensureRuntimeDirectories();
        useDatabase(AppPaths.getDatabasePath());
    }

    public static void useTestDatabase() {
        useDatabase(Path.of("test.db"));
    }

    public static Path getCurrentDatabasePath() {
        return databasePath;
    }

    public static Connection getConnection() throws SQLException {
        Path parent = databasePath.getParent();
        if (parent != null) {
            try {
                Files.createDirectories(parent);
            } catch (IOException e) {
                throw new SQLException("Failed to create database directory: " + parent, e);
            }
        }

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

    public static void ensureSchema() throws SQLException {
        try (Connection conn = Database.getConnection();
             Statement st = conn.createStatement()) {
            executeSqlStatements(st, SqlGenerator.generateFullSchema());
        } catch (SQLException e) {
            throw e;
        } catch (Exception e) {
            throw new SQLException("Failed to generate application schema.", e);
        }
    }

    public static void generateSchemaFile() {
        Path schemaPath = Paths.get("src/main/resources/sql/001_schema.sql");

        try {
            String fullSql = SqlGenerator.generateFullSchema();

            if (schemaPath.getParent() != null) {
                Files.createDirectories(schemaPath.getParent());
            }

            Files.writeString(schemaPath, fullSql, StandardCharsets.UTF_8);

            System.out.println("=== Schema Generation Success ===");
            System.out.println("File: " + schemaPath.toAbsolutePath());
            System.out.println("=================================");

        } catch (Exception e) {
            System.err.println("Failed to generate schema file!");
            e.printStackTrace();
            throw new RuntimeException("Schema generation failed", e);
        }
    }

    public static void generateJavaCrudFile() {
        final String MODEL_PACKAGE = "eecs2311.group2.wh40k_easycombat.model";
        List<Class<?>> tableClasses;

        try{
            tableClasses = JavaGenerator.getClassesWithTableAnnotation(MODEL_PACKAGE);
        }
        catch (Exception e) {
            System.err.println("Error loading classes");
            e.printStackTrace();
            throw new RuntimeException("Java CRUD File generation failed", e);
        }
        System.out.println("===== Starting Java CRUD File Generation ====");
        for (Class<?> clazz : tableClasses) {
        	Path schemaPath = Paths.get(
        		    "src/main/java/eecs2311/group2/wh40k_easycombat/repository/"
        		    + clazz.getSimpleName() + "Repository.java"
        		);

            try {
                String fullJava = JavaGenerator.generateCrudCode(clazz);

                if (schemaPath.getParent() != null) {
                    Files.createDirectories(schemaPath.getParent());
                }

                Files.writeString(schemaPath, fullJava, StandardCharsets.UTF_8);

                System.out.println("New Java CRUD File Generated: " + schemaPath.toAbsolutePath());
            } catch (Exception e) {
                System.err.println("Failed to generate java file!");
                e.printStackTrace();
                throw new RuntimeException("Java CRUD File generation failed", e);
            } 
        }
        System.out.println("=== File Generation Finished Successfully ===");
    }

    public static void executeSqlFolder(String folderPath) throws IOException, SQLException {
        Path folder = Path.of(folderPath);
        if (!Files.exists(folder) || !Files.isDirectory(folder)) {
            throw new IllegalArgumentException("Folder not found: " + folderPath);
        }

        try (Connection conn = Database.getConnection();
             Statement st = conn.createStatement()) {

            List<Path> sqlFiles;
            try (var paths = Files.list(folder)) {
            	sqlFiles = paths
            	        .filter(p -> p.getFileName().toString().equalsIgnoreCase("001_schema.sql"))
            	        .toList();
            }

            for (Path file : sqlFiles) {
                String sql = Files.readString(file);
                executeSqlStatements(st, sql);
            }
        }
    }

    private static void useDatabase(Path path) {
        databasePath = path.toAbsolutePath().normalize();
        URL = buildSqliteUrl(databasePath);
    }

    private static String buildSqliteUrl(Path path) {
        return "jdbc:sqlite:" + path.toAbsolutePath().normalize();
    }

    private static void executeSqlStatements(Statement statement, String sql) throws SQLException {
        String[] statements = sql.split(";");
        for (String stmt : statements) {
            String trimmed = stmt.trim();
            if (!trimmed.isEmpty()) {
                statement.execute(trimmed);
            }
        }
    }

    private Database() {
    }
}
