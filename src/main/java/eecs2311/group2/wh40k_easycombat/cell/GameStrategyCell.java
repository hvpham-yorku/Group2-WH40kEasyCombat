package eecs2311.group2.wh40k_easycombat.cell;

import eecs2311.group2.wh40k_easycombat.util.GameHtmlTextBuilder;
import eecs2311.group2.wh40k_easycombat.viewmodel.GameStrategyVM;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.text.TextFlow;

public class GameStrategyCell extends ListCell<GameStrategyVM> {

    @Override
    protected void updateItem(GameStrategyVM item, boolean empty) {
        super.updateItem(item, empty);

        setText(null);
        setContentDisplay(ContentDisplay.GRAPHIC_ONLY);

        if (empty || item == null) {
            setGraphic(null);
            return;
        }

        VBox root = new VBox(6);
        root.setPadding(new Insets(8));
        bindToListWidth(root, 24);

        Button expandButton = new Button(item.expandedProperty().get() ? "-" : "+");
        expandButton.setPrefWidth(26);
        expandButton.setMinWidth(26);

        expandButton.setOnAction(e -> {
            e.consume();
            item.expandedProperty().set(!item.expandedProperty().get());
            if (getListView() != null) {
                getListView().refresh();
            }
        });

        Label nameLabel = new Label(item.getName());
        nameLabel.setWrapText(true);
        nameLabel.setMaxWidth(Double.MAX_VALUE);
        nameLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 14px;");

        String cpText = item.getCpCost() == null || item.getCpCost().isBlank()
                ? "0 CP"
                : item.getCpCost() + " CP";

        Label cpLabel = new Label(cpText);
        cpLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 13px;");
        cpLabel.setMinWidth(Region.USE_PREF_SIZE);

        Region spacer = new Region();
        HBox.setHgrow(nameLabel, Priority.ALWAYS);
        HBox.setHgrow(spacer, Priority.ALWAYS);

        HBox topRow = new HBox(8, expandButton, nameLabel, spacer, cpLabel);
        topRow.setAlignment(Pos.CENTER_LEFT);

        VBox detailBox = new VBox(6);
        detailBox.visibleProperty().bind(item.expandedProperty());
        detailBox.managedProperty().bind(item.expandedProperty());

        if (!item.getTurn().isBlank()) {
            Label turnLabel = new Label("Turn: " + item.getTurn());
            turnLabel.setWrapText(true);
            turnLabel.setStyle("-fx-font-size: 13px;");
            detailBox.getChildren().add(turnLabel);
        }

        if (!item.getPhase().isBlank()) {
            Label phaseLabel = new Label("Phase: " + item.getPhase());
            phaseLabel.setWrapText(true);
            phaseLabel.setStyle("-fx-font-size: 13px;");
            detailBox.getChildren().add(phaseLabel);
        }

        TextFlow descFlow = new TextFlow();
        descFlow.setLineSpacing(2);
        bindToListWidth(descFlow, 44);
        GameHtmlTextBuilder.setHtmlLikeText(descFlow, item.getDescriptionHtml(), 14.0);

        detailBox.getChildren().add(descFlow);
        root.getChildren().addAll(topRow, detailBox);

        setGraphic(root);
    }

    private void bindToListWidth(Region region, double offset) {
        if (getListView() == null) return;
        region.prefWidthProperty().bind(getListView().widthProperty().subtract(offset));
        region.maxWidthProperty().bind(region.prefWidthProperty());
    }
}
