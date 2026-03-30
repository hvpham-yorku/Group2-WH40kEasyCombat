package eecs2311.group2.wh40k_easycombat.db;

import eecs2311.group2.wh40k_easycombat.util.AppPaths;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import static org.junit.jupiter.api.Assertions.assertTrue;

class DatabaseSchemaInitializationTest {

    @Test
    void ensureSchemaCreatesArmyTableFromBundledSchema(@TempDir Path tempDir) throws Exception {
        Path previousDatabasePath = Database.getCurrentDatabasePath();
        String previousHomeOverride = System.getProperty(AppPaths.HOME_OVERRIDE_PROPERTY);

        try {
            System.setProperty(AppPaths.HOME_OVERRIDE_PROPERTY, tempDir.resolve("app-home").toString());

            Database.useApplicationDatabase();
            Database.ensureSchema();

            assertTrue(Files.exists(AppPaths.getDatabasePath()));
            assertTrue(hasTable("Army"));
            assertTrue(hasTable("Datasheets"));
        } finally {
            if (previousHomeOverride == null) {
                System.clearProperty(AppPaths.HOME_OVERRIDE_PROPERTY);
            } else {
                System.setProperty(AppPaths.HOME_OVERRIDE_PROPERTY, previousHomeOverride);
            }

            Database.useDatabasePath(previousDatabasePath);
        }
    }

    private static boolean hasTable(String tableName) throws Exception {
        try (Connection conn = Database.getConnection();
             PreparedStatement statement = conn.prepareStatement(
                     "SELECT name FROM sqlite_master WHERE type='table' AND name = ?"
             )) {
            statement.setString(1, tableName);
            try (ResultSet resultSet = statement.executeQuery()) {
                return resultSet.next();
            }
        }
    }
}
