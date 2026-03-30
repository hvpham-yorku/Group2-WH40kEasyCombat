package eecs2311.group2.wh40k_easycombat;

import eecs2311.group2.wh40k_easycombat.controller.LoadingController;
import eecs2311.group2.wh40k_easycombat.controller.helper.DialogHelper;
import eecs2311.group2.wh40k_easycombat.util.AppBootstrap;
import eecs2311.group2.wh40k_easycombat.util.FixedAspectView;
import javafx.application.Platform;
import javafx.application.Application;
import javafx.application.HostServices;
import javafx.concurrent.Task;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.stage.Stage;

public class Main extends Application {
    private static final double LOADING_MIN_WIDTH = 520.0;
    private static final double LOADING_MIN_HEIGHT = 320.0;
    private static final double LOADING_PREF_WIDTH = 720.0;
    private static final double LOADING_PREF_HEIGHT = 420.0;

    private static HostServices hostServices;

    @Override
    public void start(Stage primaryStage) {
        try {
            hostServices = getHostServices();
            primaryStage.setTitle("WARHAMMER40000 Easy Combat");

            FXMLLoader loader = new FXMLLoader(getClass().getResource("Loading.fxml"));
            Parent loadingRoot = loader.load();
            LoadingController loadingController = loader.getController();

            FixedAspectView.showResponsive(
                    primaryStage,
                    loadingRoot,
                    LOADING_MIN_WIDTH,
                    LOADING_MIN_HEIGHT,
                    LOADING_PREF_WIDTH,
                    LOADING_PREF_HEIGHT
            );

            Task<Void> startupTask = createStartupTask();
            loadingController.bind(startupTask);

            startupTask.setOnSucceeded(event -> {
                try {
                    showMainMenu(primaryStage);
                } catch (Exception e) {
                    handleStartupFailure(loadingController, "Failed to open the main menu.", e);
                }
            });

            startupTask.setOnFailed(event ->
                    handleStartupFailure(loadingController, "Startup failed.", startupTask.getException()));

            Thread startupThread = new Thread(startupTask, "wh40k-startup");
            startupThread.setDaemon(true);
            Platform.runLater(startupThread::start);
        } catch (Exception e) {
            handleStartupFailure(null, "Failed to open the startup screen.", e);
        }
    }

    public static HostServices getAppHostServices() {
        return hostServices;
    }

    public static void main(String[] args) {
        launch(args);
    }

    private Task<Void> createStartupTask() {
        return new Task<>() {
            @Override
            protected Void call() throws Exception {
                updateMessage("Preparing startup...");
                updateProgress(0, 1);
                AppBootstrap.initializeApplication((message, progress) -> {
                    updateMessage(message);
                    if (progress < 0) {
                        updateProgress(-1, 1);
                    } else {
                        updateProgress(progress, 1);
                    }
                });
                return null;
            }
        };
    }

    private void showMainMenu(Stage primaryStage) throws Exception {
        Parent root = FXMLLoader.load(getClass().getResource("MainUI.fxml"));
        FixedAspectView.showMainMenu(primaryStage, root);
    }

    private void handleStartupFailure(LoadingController loadingController, String title, Throwable error) {
        if (loadingController != null) {
            loadingController.showFailure("Startup failed. See the error dialog for details.");
        }

        if (error != null) {
            error.printStackTrace();
        }

        DialogHelper.showError(title, buildStartupErrorMessage(error));
        Platform.exit();
    }

    private String buildStartupErrorMessage(Throwable error) {
        Throwable rootCause = unwrap(error);
        if (rootCause == null) {
            return "Unknown error.";
        }

        String message = rootCause.getMessage();
        if (message == null || message.isBlank()) {
            return rootCause.getClass().getSimpleName();
        }

        return message;
    }

    private Throwable unwrap(Throwable error) {
        Throwable current = error;
        while (current != null && current.getCause() != null && current.getCause() != current) {
            current = current.getCause();
        }
        return current;
    }
}
