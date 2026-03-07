package eecs2311.group2.wh40k_easycombat;
	
import java.io.IOException;
import java.sql.SQLException;

import eecs2311.group2.wh40k_easycombat.db.Database;
import eecs2311.group2.wh40k_easycombat.util.CsvToSqliteImporter;
import javafx.application.Application;
import javafx.stage.Stage;
import javafx.scene.Scene;
import javafx.scene.layout.HBox;
import javafx.fxml.FXMLLoader;


public class Main extends Application {
	@Override
	public void start(Stage primaryStage) {
		try {
			HBox root = (HBox)FXMLLoader.load(getClass().getResource("MainUI.fxml"));
			Scene scene = new Scene(root,1200,800);
			scene.getStylesheets().add(getClass().getResource("application.css").toExternalForm());
			primaryStage.setTitle("WARHAMMER40000 Easy Combat");
			primaryStage.setScene(scene);
			primaryStage.show();
		} catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	public static void main(String[] args) throws IOException, SQLException {
        Database.generateSchemaFile();
        Database.executeSqlFolder("src/main/resources/sql/");
        System.out.println("SQL scripts executed!");
        
        Database.generateJavaCrudFile();
        
        CsvToSqliteImporter.importDefaultCsvSeedLike(false);
		
		launch(args);
	}
}
