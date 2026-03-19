package eecs2311.group2.wh40k_easycombat.util;

import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.geometry.Rectangle2D;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.shape.Rectangle;
import javafx.stage.Screen;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.Objects;

public final class FixedAspectView {

    private FixedAspectView() {}

    private static final String KEY_WL = "fixedAspect.widthListener";
    private static final String KEY_HL = "fixedAspect.heightListener";
    private static final String KEY_MAXIMIZED_LISTENER = "fixedAspect.maximizedListener";
    private static final String KEY_SCENE_TOKEN = "fixedAspect.sceneToken";

    private static final double SCALE_SCREEN_USAGE = 0.98;
    private static final double RESPONSIVE_SCREEN_USAGE = 0.96;
    private static final String LETTERBOX_STYLE = "-fx-background-color: #0f0f0f;";

    public static void switchTo(Node eventSourceNode,
                                String fxmlPath,
                                double baseW,
                                double baseH) throws IOException {
        Objects.requireNonNull(eventSourceNode, "eventSourceNode");
        Stage stage = (Stage) eventSourceNode.getScene().getWindow();
        show(stage, fxmlPath, baseW, baseH);
    }

    public static void show(Stage stage,
                            String fxmlPath,
                            double baseW,
                            double baseH) throws IOException {
        show(stage, loadRoot(fxmlPath), baseW, baseH);
    }

    public static void show(Stage stage,
                            Parent root,
                            double baseW,
                            double baseH) {
        Objects.requireNonNull(stage, "stage");
        Objects.requireNonNull(root, "root");
        if (baseW <= 0 || baseH <= 0) {
            throw new IllegalArgumentException("baseW/baseH must be > 0");
        }

        removeOldListeners(stage);

        Object sceneToken = new Object();
        stage.getProperties().put(KEY_SCENE_TOKEN, sceneToken);

        if (root instanceof Region region) {
            region.setPrefSize(baseW, baseH);
        }

        Group content = new Group(root);

        StackPane viewport = new StackPane(content);
        viewport.setAlignment(Pos.CENTER);
        viewport.setStyle(LETTERBOX_STYLE);

        Rectangle clip = new Rectangle();
        clip.widthProperty().bind(viewport.widthProperty());
        clip.heightProperty().bind(viewport.heightProperty());
        viewport.setClip(clip);

        Scene scene = new Scene(viewport);
        stage.setScene(scene);
        stage.setResizable(true);

        Runnable rescale = () -> {
            if (!isCurrentScene(stage, scene, sceneToken)) {
                return;
            }

            double availableW = viewport.getWidth();
            double availableH = viewport.getHeight();
            if (availableW <= 0 || availableH <= 0) {
                return;
            }

            double scale = Math.min(availableW / baseW, availableH / baseH);
            content.setScaleX(scale);
            content.setScaleY(scale);
        };

        scene.widthProperty().addListener((obs, oldV, newV) -> rescale.run());
        scene.heightProperty().addListener((obs, oldV, newV) -> rescale.run());

        ChangeListener<Boolean> maximizedListener = (obs, oldV, newV) ->
                Platform.runLater(rescale);

        stage.maximizedProperty().addListener(maximizedListener);
        stage.getProperties().put(KEY_MAXIMIZED_LISTENER, maximizedListener);

        boolean keepMaximized = stage.isMaximized();

        stage.show();

        Platform.runLater(() -> {
            if (!isCurrentScene(stage, scene, sceneToken)) {
                return;
            }

            if (!keepMaximized) {
                fitScaledStageToScreen(stage, scene, baseW, baseH);
            }

            rescale.run();
        });
    }

    public static void switchResponsiveTo(Node eventSourceNode,
                                          String fxmlPath,
                                          double minSceneW,
                                          double minSceneH,
                                          double prefSceneW,
                                          double prefSceneH) throws IOException {
        Objects.requireNonNull(eventSourceNode, "eventSourceNode");
        Stage stage = (Stage) eventSourceNode.getScene().getWindow();
        showResponsive(stage, fxmlPath, minSceneW, minSceneH, prefSceneW, prefSceneH);
    }

    public static void showResponsive(Stage stage,
                                      String fxmlPath,
                                      double minSceneW,
                                      double minSceneH,
                                      double prefSceneW,
                                      double prefSceneH) throws IOException {
        showResponsive(stage, loadRoot(fxmlPath), minSceneW, minSceneH, prefSceneW, prefSceneH);
    }

    public static void showResponsive(Stage stage,
                                      Parent root,
                                      double minSceneW,
                                      double minSceneH,
                                      double prefSceneW,
                                      double prefSceneH) {
        Objects.requireNonNull(stage, "stage");
        Objects.requireNonNull(root, "root");
        if (minSceneW <= 0 || minSceneH <= 0 || prefSceneW <= 0 || prefSceneH <= 0) {
            throw new IllegalArgumentException("scene sizes must be > 0");
        }

        removeOldListeners(stage);

        Object sceneToken = new Object();
        stage.getProperties().put(KEY_SCENE_TOKEN, sceneToken);

        Scene scene = new Scene(root);
        stage.setScene(scene);
        stage.setResizable(true);

        boolean keepMaximized = stage.isMaximized();

        stage.show();

        Platform.runLater(() -> {
            if (!isCurrentScene(stage, scene, sceneToken)) {
                return;
            }

            double decoW = stage.getWidth() - scene.getWidth();
            double decoH = stage.getHeight() - scene.getHeight();
            Rectangle2D vb = resolveCurrentScreen(stage).getVisualBounds();

            stage.setMinWidth(Math.min(minSceneW + decoW, vb.getWidth()));
            stage.setMinHeight(Math.min(minSceneH + decoH, vb.getHeight()));

            if (!keepMaximized) {
                fitResponsiveStageToScreen(stage, scene, minSceneW, minSceneH, prefSceneW, prefSceneH);
            }
        });
    }

    private static Parent loadRoot(String fxmlPath) throws IOException {
        FXMLLoader loader = new FXMLLoader(
                Objects.requireNonNull(
                        FixedAspectView.class.getResource(fxmlPath),
                        "Cannot find FXML: " + fxmlPath
                )
        );
        return loader.load();
    }

    private static void fitScaledStageToScreen(Stage stage,
                                               Scene scene,
                                               double baseW,
                                               double baseH) {
        double decoW = stage.getWidth() - scene.getWidth();
        double decoH = stage.getHeight() - scene.getHeight();
        Rectangle2D vb = resolveCurrentScreen(stage).getVisualBounds();

        double usableSceneW = Math.max(320.0, vb.getWidth() * SCALE_SCREEN_USAGE - decoW);
        double usableSceneH = Math.max(240.0, vb.getHeight() * SCALE_SCREEN_USAGE - decoH);
        double scale = Math.min(usableSceneW / baseW, usableSceneH / baseH);

        double targetSceneW = baseW * scale;
        double targetSceneH = baseH * scale;
        double targetStageW = targetSceneW + decoW;
        double targetStageH = targetSceneH + decoH;

        stage.setWidth(targetStageW);
        stage.setHeight(targetStageH);
        stage.setX(vb.getMinX() + (vb.getWidth() - targetStageW) / 2.0);
        stage.setY(vb.getMinY() + (vb.getHeight() - targetStageH) / 2.0);
    }

    private static void fitResponsiveStageToScreen(Stage stage,
                                                   Scene scene,
                                                   double minSceneW,
                                                   double minSceneH,
                                                   double prefSceneW,
                                                   double prefSceneH) {
        double decoW = stage.getWidth() - scene.getWidth();
        double decoH = stage.getHeight() - scene.getHeight();
        Rectangle2D vb = resolveCurrentScreen(stage).getVisualBounds();

        double maxSceneW = Math.max(320.0, vb.getWidth() * RESPONSIVE_SCREEN_USAGE - decoW);
        double maxSceneH = Math.max(240.0, vb.getHeight() * RESPONSIVE_SCREEN_USAGE - decoH);

        double targetSceneW = clampSceneSize(prefSceneW, minSceneW, maxSceneW);
        double targetSceneH = clampSceneSize(prefSceneH, minSceneH, maxSceneH);

        double targetStageW = targetSceneW + decoW;
        double targetStageH = targetSceneH + decoH;

        stage.setWidth(targetStageW);
        stage.setHeight(targetStageH);
        stage.setX(vb.getMinX() + (vb.getWidth() - targetStageW) / 2.0);
        stage.setY(vb.getMinY() + (vb.getHeight() - targetStageH) / 2.0);
    }

    private static double clampSceneSize(double preferred, double minimum, double maximum) {
        if (maximum < minimum) {
            return maximum;
        }
        return Math.max(minimum, Math.min(preferred, maximum));
    }

    @SuppressWarnings("unchecked")
    private static void removeOldListeners(Stage stage) {
        Object oldWL = stage.getProperties().remove(KEY_WL);
        if (oldWL instanceof ChangeListener<?> wl) {
            stage.widthProperty().removeListener((ChangeListener<? super Number>) wl);
        }

        Object oldHL = stage.getProperties().remove(KEY_HL);
        if (oldHL instanceof ChangeListener<?> hl) {
            stage.heightProperty().removeListener((ChangeListener<? super Number>) hl);
        }

        Object oldML = stage.getProperties().remove(KEY_MAXIMIZED_LISTENER);
        if (oldML instanceof ChangeListener<?> ml) {
            stage.maximizedProperty().removeListener((ChangeListener<? super Boolean>) ml);
        }
    }

    private static boolean isCurrentScene(Stage stage, Scene scene, Object token) {
        return stage.getScene() == scene && stage.getProperties().get(KEY_SCENE_TOKEN) == token;
    }

    private static Screen resolveCurrentScreen(Stage stage) {
        return Screen.getScreensForRectangle(
                stage.getX(),
                stage.getY(),
                Math.max(stage.getWidth(), 1.0),
                Math.max(stage.getHeight(), 1.0)
        ).stream().findFirst().orElse(Screen.getPrimary());
    }
}
