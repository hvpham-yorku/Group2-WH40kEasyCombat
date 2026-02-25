package eecs2311.group2.wh40k_easycombat.tools;

import eecs2311.group2.wh40k_easycombat.db.Database;

public class DatabaseSetupTool {

    public static void runSetup() throws Exception {

        Database.generateSchemaFile();
        Database.executeSqlFolder("src/main/resources/sql/");
        System.out.println("SQL scripts executed!");
        Database.generateJavaCrudFile();

        System.out.println("Database setup complete!");
    }
}