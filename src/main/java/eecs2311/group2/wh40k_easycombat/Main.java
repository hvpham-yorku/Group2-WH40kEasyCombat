package eecs2311.group2.wh40k_easycombat;

import java.io.IOException;
import java.nio.file.Path;
import java.sql.SQLException;
import java.util.Arrays;

import eecs2311.group2.wh40k_easycombat.db.Database;
import eecs2311.group2.wh40k_easycombat.service.vm.VMService;
import eecs2311.group2.wh40k_easycombat.util.CsvToSqliteImporter;
import eecs2311.group2.wh40k_easycombat.util.FixedAspectView;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.stage.Stage;

public class Main extends Application {
    @Override
    public void start(Stage primaryStage) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("MainUI.fxml"));
            primaryStage.setTitle("WARHAMMER40000 Easy Combat");
            FixedAspectView.showResponsive(primaryStage, root, 800.0, 600.0, 1200.0, 800.0);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) throws IOException, SQLException {
        Database.generateSchemaFile();
        Database.executeSqlFolder("src/main/resources/sql/");
        System.out.println("SQL scripts executed!");
        VMService.loadFolder(Path.of("src/main/resources/dsl/"));

        CsvToSqliteImporter.importDefaultCsvSeedLike(false);

        launch(args);
    }
}
