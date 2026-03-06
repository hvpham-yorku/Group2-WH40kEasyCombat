package eecs2311.group2.wh40k_easycombat.cell;

import eecs2311.group2.wh40k_easycombat.util.GameHtmlTextBuilder;
import eecs2311.group2.wh40k_easycombat.viewmodel.GameStrategyVM;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.text.TextFlow;

public class GameStrategyCell extends ListCell<GameStrategyVM> {

    @Override
    protected void updateItem(GameStrategyVM item, boolean empty) {
        super.updateItem(item, empty);

        if (empty || item == null) {
            setText(null);
            setGraphic(null);
            return;
        }

        VBox root = new VBox(4);
        root.setPadding(new Insets(6));

        Button expandButton = new Button(item.expandedProperty().get() ? "▼" : "▶");
        expandButton.setPrefWidth(22);

        expandButton.setOnAction(e -> {
            e.consume();
            item.expandedProperty().set(!item.expandedProperty().get());
            if (getListView() != null) {
                getListView().refresh();
            }
        });

        Label nameLabel = new Label(item.getName());
        nameLabel.setWrapText(true);
        nameLabel.setMaxWidth(170);
        nameLabel.setStyle("-fx-font-weight: bold;");

        Label cpLabel = new Label(item.getCpCost() + " CP");
        cpLabel.setStyle("-fx-font-weight: bold;");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        HBox topRow = new HBox(6, expandButton, nameLabel, spacer, cpLabel);
        topRow.setAlignment(Pos.CENTER_LEFT);

        VBox detailBox = new VBox(4);
        detailBox.visibleProperty().bind(item.expandedProperty());
        detailBox.managedProperty().bind(item.expandedProperty());

        if (!item.getTurn().isBlank()) {
            Label turnLabel = new Label("Turn: " + item.getTurn());
            turnLabel.setWrapText(true);
            detailBox.getChildren().add(turnLabel);
        }

        if (!item.getPhase().isBlank()) {
            Label phaseLabel = new Label("Phase: " + item.getPhase());
            phaseLabel.setWrapText(true);
            detailBox.getChildren().add(phaseLabel);
        }

        TextFlow descFlow = new TextFlow();
        descFlow.setPrefWidth(240);
        GameHtmlTextBuilder.setHtmlLikeText(descFlow, item.getDescriptionHtml());

        detailBox.getChildren().add(descFlow);

        root.getChildren().addAll(topRow, detailBox);

        setGraphic(root);
    }
}