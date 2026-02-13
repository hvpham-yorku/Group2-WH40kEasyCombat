package eecs2311.group2.wh40k_easycombat.util;

import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import java.io.IOException;
import java.util.Objects;

public final class FixedAspectView {

    private FixedAspectView() {}

    private static final String KEY_WL = "fixedAspect.widthListener";
    private static final String KEY_HL = "fixedAspect.heightListener";

    @SuppressWarnings("unchecked")
    public static void switchTo(Node eventSourceNode,
                                String fxmlPath,
                                double baseW,
                                double baseH) throws IOException {

        Objects.requireNonNull(eventSourceNode, "eventSourceNode");
        Objects.requireNonNull(fxmlPath, "fxmlPath");
        if (baseW <= 0 || baseH <= 0) throw new IllegalArgumentException("baseW/baseH must be > 0");

        FXMLLoader loader = new FXMLLoader(FixedAspectView.class.getResource(fxmlPath));
        Parent root = loader.load();

        // Wrap in Group to prevent layout reflow (keeps absolute positions intact)
        Group content = new Group(root);

        StackPane wrapper = new StackPane(content);
        wrapper.setAlignment(Pos.CENTER);
        wrapper.setStyle("-fx-background-color: transparent;");

        Stage stage = (Stage) eventSourceNode.getScene().getWindow();
        Scene scene = new Scene(wrapper);

        stage.setScene(scene);
        stage.setResizable(true);

        // Remove old listeners to avoid stacking
        Object oldWL = stage.getProperties().get(KEY_WL);
        if (oldWL instanceof ChangeListener<?> wl) {
            stage.widthProperty().removeListener((ChangeListener<? super Number>) wl);
        }
        Object oldHL = stage.getProperties().get(KEY_HL);
        if (oldHL instanceof ChangeListener<?> hl) {
            stage.heightProperty().removeListener((ChangeListener<? super Number>) hl);
        }

        // Set an initial size and center
        stage.setWidth(baseW);
        stage.setHeight(baseH);
        stage.centerOnScreen();
        stage.show();

        final double ratio = baseW / baseH;

        // Cache "decoration" sizes (title bar + borders) after first layout pass.
        // decorationW = stageWidth - sceneWidth, decorationH = stageHeight - sceneHeight
        final double[] deco = new double[]{0.0, 0.0}; // [0]=decoW, [1]=decoH

        // Guard flag to prevent recursive resize handling
        final boolean[] adjusting = {false};

        // Uniform scaling based on actual scene size (content area)
        Runnable rescale = () -> {
            double cw = scene.getWidth();
            double ch = scene.getHeight();
            if (cw <= 0 || ch <= 0) return;

            double scale = Math.min(cw / baseW, ch / baseH);
            content.setScaleX(scale);
            content.setScaleY(scale);
        };

        Platform.runLater(() -> {
            // Now scene sizes are valid; cache decoration sizes once
            deco[0] = stage.getWidth() - scene.getWidth();
            deco[1] = stage.getHeight() - scene.getHeight();

            // Re-apply size so content area is exactly baseW/baseH
            adjusting[0] = true;
            stage.setWidth(baseW + deco[0]);
            stage.setHeight(baseH + deco[1]);
            adjusting[0] = false;

            stage.centerOnScreen();
            rescale.run();
        });

        // Lock aspect ratio: width drives height (stable, less jitter)
        ChangeListener<Number> widthListener = (obs, oldW, newW) -> {
            if (adjusting[0]) return;
            if (deco[1] <= 0) { // decoration not ready yet
                rescale.run();
                return;
            }

            adjusting[0] = true;

            double stageW = newW.doubleValue();
            double contentW = stageW - deco[0];
            double contentH = contentW / ratio;
            double targetStageH = contentH + deco[1];

            stage.setHeight(targetStageH);

            adjusting[0] = false;
            rescale.run();
        };

        // Height changes only rescale (do not force width back to avoid oscillation)
        ChangeListener<Number> heightListener = (obs, oldH, newH) -> rescale.run();

        stage.widthProperty().addListener(widthListener);
        stage.heightProperty().addListener(heightListener);

        stage.getProperties().put(KEY_WL, widthListener);
        stage.getProperties().put(KEY_HL, heightListener);
    }
}
