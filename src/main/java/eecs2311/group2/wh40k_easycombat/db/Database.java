package eecs2311.group2.wh40k_easycombat.db;

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
            Path schemaPath = Paths.get("src/main/java/eecs2311/group2/wh40k_easycombat/repository/" + JavaGenerator.pluralToSingular(clazz.getSimpleName()) + "Repository.java");

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

            // IMPORTANT: run in filename order: 001_schema.sql -> 002_seed.sql -> ...
            List<Path> sqlFiles;
            try (var paths = Files.list(folder)) {
                sqlFiles = paths
                        .filter(p -> p.getFileName().toString().toLowerCase().endsWith(".sql"))
                        .sorted((a, b) -> a.getFileName().toString().compareToIgnoreCase(b.getFileName().toString()))
                        .toList();
            }

            for (Path file : sqlFiles) {
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