package eecs2311.group2.wh40k_easycombat.support;

import eecs2311.group2.wh40k_easycombat.db.Dao;
import eecs2311.group2.wh40k_easycombat.db.Database;
import eecs2311.group2.wh40k_easycombat.model.Army;
import eecs2311.group2.wh40k_easycombat.model.Army_detachment;
import eecs2311.group2.wh40k_easycombat.model.Army_units;
import eecs2311.group2.wh40k_easycombat.model.Army_wargear;
import eecs2311.group2.wh40k_easycombat.model.Datasheets;
import eecs2311.group2.wh40k_easycombat.model.Datasheets_abilities;
import eecs2311.group2.wh40k_easycombat.model.Datasheets_detachment_abilities;
import eecs2311.group2.wh40k_easycombat.model.Datasheets_enhancements;
import eecs2311.group2.wh40k_easycombat.model.Datasheets_keywords;
import eecs2311.group2.wh40k_easycombat.model.Datasheets_leader;
import eecs2311.group2.wh40k_easycombat.model.Datasheets_models;
import eecs2311.group2.wh40k_easycombat.model.Datasheets_models_cost;
import eecs2311.group2.wh40k_easycombat.model.Datasheets_options;
import eecs2311.group2.wh40k_easycombat.model.Datasheets_stratagems;
import eecs2311.group2.wh40k_easycombat.model.Datasheets_unit_composition;
import eecs2311.group2.wh40k_easycombat.model.Datasheets_wargear;
import eecs2311.group2.wh40k_easycombat.util.SqlGenerator;

import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;

public final class TestDatabaseSupport {

    private static final List<Class<?>> STATIC_AND_ARMY_TABLES = List.of(
            Army.class,
            Army_detachment.class,
            Army_units.class,
            Army_wargear.class,
            Datasheets.class,
            Datasheets_abilities.class,
            Datasheets_detachment_abilities.class,
            Datasheets_enhancements.class,
            Datasheets_keywords.class,
            Datasheets_leader.class,
            Datasheets_models.class,
            Datasheets_models_cost.class,
            Datasheets_options.class,
            Datasheets_stratagems.class,
            Datasheets_unit_composition.class,
            Datasheets_wargear.class
    );

    private TestDatabaseSupport() {
    }

    public static void useFreshStaticAndArmyTestDatabase() throws Exception {
        Database.useTestDatabase();
        deleteTestDatabaseFiles();
        createTables(STATIC_AND_ARMY_TABLES);
    }

    public static void clearAllTables() throws SQLException {
        Dao.update("PRAGMA foreign_keys = OFF");

        List<String> tables = Dao.query(
                "SELECT name FROM sqlite_master WHERE type='table' AND name NOT LIKE 'sqlite_%'",
                rs -> rs.getString("name")
        );

        for (String table : tables) {
            Dao.update("DELETE FROM " + table);
        }

        boolean hasSqliteSequence = !Dao.query(
                "SELECT name FROM sqlite_master WHERE type='table' AND name='sqlite_sequence'",
                rs -> rs.getString("name")
        ).isEmpty();
        if (hasSqliteSequence) {
            Dao.update("DELETE FROM sqlite_sequence");
        }

        Dao.update("PRAGMA foreign_keys = ON");
    }

    private static void createTables(List<Class<?>> tableClasses) throws Exception {
        try (Connection conn = Database.getConnection();
             Statement statement = conn.createStatement()) {
            for (Class<?> tableClass : tableClasses) {
                statement.execute(SqlGenerator.generateCreateTable(tableClass));
            }
        }
    }

    private static void deleteTestDatabaseFiles() throws Exception {
        Files.deleteIfExists(Path.of("test.db"));
        Files.deleteIfExists(Path.of("test.db-wal"));
        Files.deleteIfExists(Path.of("test.db-shm"));
    }
}
