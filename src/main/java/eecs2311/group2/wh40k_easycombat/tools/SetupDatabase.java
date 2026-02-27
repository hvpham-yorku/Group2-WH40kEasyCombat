package eecs2311.group2.wh40k_easycombat.tools;

import eecs2311.group2.wh40k_easycombat.db.Database;


public class SetupDatabase {
	public static void main(String[] args) throws Exception {
		Database.generateSchemaFile();
		Database.executeSqlFolder("src/main/resources/sql/");
		System.out.println("SQL scripts executed!");

		Database.generateJavaCrudFile();
		System.out.println("Database setup complete!");
	}
}
