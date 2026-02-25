package eecs2311.group2.wh40k_easycombat;
	
import eecs2311.group2.wh40k_easycombat.db.Database;
import eecs2311.group2.wh40k_easycombat.tools.DatabaseSetupTool;
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
	
public static void main(String[] args) throws Exception {
    if (args.length > 0 && args[0].equals("setup")) {
        DatabaseSetupTool.runSetup();
        return; 
    }
    launch(args);
}
}
