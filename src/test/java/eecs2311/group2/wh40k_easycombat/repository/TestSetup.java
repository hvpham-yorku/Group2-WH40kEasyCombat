package eecs2311.group2.wh40k_easycombat.repository;

import eecs2311.group2.wh40k_easycombat.db.Dao;
import eecs2311.group2.wh40k_easycombat.db.Database;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;

import java.sql.SQLException;
import java.util.List;

abstract class TestSetup {

    @BeforeAll
    static void initTestDatabase() throws Exception {
        Database.useTestDatabase();
        Database.generateSchemaFile();
        Database.executeSqlFolder("src/main/resources/sql/");
    }

    @AfterEach
    void clearTables() throws SQLException {
        Dao.update("PRAGMA foreign_keys = OFF");
        
        List<String> tables = Dao.query(
            "SELECT name FROM sqlite_master WHERE type='table' AND name NOT LIKE 'sqlite_%'",
            rs -> rs.getString("name")
        );

        for (String table : tables) {
            Dao.update("DELETE FROM " + table);
        }

        Dao.update("DELETE FROM sqlite_sequence");
        Dao.update("PRAGMA foreign_keys = ON");
    }
}

