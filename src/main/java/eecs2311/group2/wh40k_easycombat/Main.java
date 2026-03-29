package eecs2311.group2.wh40k_easycombat;

import java.io.IOException;
import java.sql.SQLException;
import eecs2311.group2.wh40k_easycombat.util.AppBootstrap;
import eecs2311.group2.wh40k_easycombat.util.FixedAspectView;
import javafx.application.Application;
import javafx.application.HostServices;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.stage.Stage;

public class Main extends Application {
    private static HostServices hostServices;

    @Override
    public void start(Stage primaryStage) {
        try {
            hostServices = getHostServices();
            Parent root = FXMLLoader.load(getClass().getResource("MainUI.fxml"));
            primaryStage.setTitle("WARHAMMER40000 Easy Combat");
            FixedAspectView.showResponsive(primaryStage, root, 800.0, 600.0, 1200.0, 800.0);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static HostServices getAppHostServices() {
        return hostServices;
    }

    public static void main(String[] args) throws IOException, SQLException {
        AppBootstrap.initializeApplication();
        launch(args);
    }
}
