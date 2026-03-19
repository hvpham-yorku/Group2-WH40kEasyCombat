package eecs2311.group2.wh40k_easycombat.cell;

import eecs2311.group2.wh40k_easycombat.viewmodel.ArmyUnitVM;
import eecs2311.group2.wh40k_easycombat.viewmodel.ArmyUnitVM.EnhancementEntry;
import eecs2311.group2.wh40k_easycombat.viewmodel.ArmyUnitVM.WargearEntry;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;

public class ArmyUnitCell extends ListCell<ArmyUnitVM> {

    private final Runnable onChanged;
    @SuppressWarnings("unused")
    private final Consumer<ArmyUnitVM> onRemove;
    private final Consumer<ArmyUnitVM> onSetWarlord;
    private final Function<ArmyUnitVM, List<EnhancementEntry>> enhancementProvider;

    public ArmyUnitCell(Runnable onChanged,
                        Consumer<ArmyUnitVM> onRemove,
                        Consumer<ArmyUnitVM> onSetWarlord,
                        Function<ArmyUnitVM, List<EnhancementEntry>> enhancementProvider) {
        this.onChanged = onChanged;
        this.onRemove = onRemove;
        this.onSetWarlord = onSetWarlord;
        this.enhancementProvider = enhancementProvider;
    }

    @Override
    protected void updateItem(ArmyUnitVM item, boolean empty) {
        super.updateItem(item, empty);

        setText(null);
        setContentDisplay(ContentDisplay.GRAPHIC_ONLY);

        if (empty || item == null) {
            setGraphic(null);
            return;
        }

        VBox root = new VBox(8);
        root.setPadding(new Insets(10));
        root.getStyleClass().add("army-list-card");
        bindToListWidth(root, 22);

        String previousRole = null;
        if (getListView() != null && getIndex() > 0 && getIndex() < getListView().getItems().size()) {
            ArmyUnitVM previous = getListView().getItems().get(getIndex() - 1);
            if (previous != null) {
                previousRole = previous.getRole();
            }
        }

        if (previousRole == null || !previousRole.equalsIgnoreCase(item.getRole())) {
            Label roleHeader = new Label(item.getRole());
            roleHeader.getStyleClass().add("army-list-role");
            root.getChildren().add(roleHeader);
        }

        Button expandButton = inlineButton(item.expandedProperty().get() ? "-" : "+");
        expandButton.setPrefWidth(28);
        expandButton.setMinWidth(28);
        expandButton.setOnAction(e -> {
            e.consume();
            item.expandedProperty().set(!item.expandedProperty().get());
            if (getListView() != null) {
                getListView().refresh();
            }
        });

        String unitPrefix = item.warlordProperty().get() ? "[Warlord] " : "";
        Label name = new Label(unitPrefix + item.getUnitName());
        name.getStyleClass().add("army-list-name");
        name.setWrapText(true);
        name.setMaxWidth(Double.MAX_VALUE);

        Region spacer = new Region();
        HBox.setHgrow(name, Priority.ALWAYS);
        HBox.setHgrow(spacer, Priority.ALWAYS);

        HBox topRow = new HBox(8, expandButton, name, spacer);
        topRow.setAlignment(Pos.CENTER_LEFT);

        Label stat = new Label(item.getStatLine());
        stat.getStyleClass().add("army-list-stat");
        stat.setWrapText(true);

        VBox expandBox = new VBox(8);
        expandBox.visibleProperty().bind(item.expandedProperty());
        expandBox.managedProperty().bind(item.expandedProperty());

        Button minusModel = inlineButton("-");
        Button plusModel = inlineButton("+");

        Label modelLabel = new Label();
        modelLabel.getStyleClass().add("army-inline-label");
        modelLabel.textProperty().bind(item.modelCountProperty().asString());

        minusModel.setOnAction(e -> {
            e.consume();
            item.decModels();
            onChanged.run();
        });

        plusModel.setOnAction(e -> {
            e.consume();
            item.incModels();
            onChanged.run();
        });

        Label modelsText = new Label("Models");
        modelsText.getStyleClass().add("army-inline-label");

        Label rangeText = new Label("Range: " + item.getMinModels() + "-" + item.getMaxModels());
        rangeText.getStyleClass().add("army-inline-label");

        HBox modelRow = new HBox(8, modelsText, minusModel, modelLabel, plusModel, rangeText);
        modelRow.setAlignment(Pos.CENTER_LEFT);
        expandBox.getChildren().add(modelRow);

        if (!item.getWargears().isEmpty()) {
            VBox wgBox = new VBox(6);

            for (WargearEntry wg : item.getWargears()) {
                Button minus = inlineButton("-");
                Button plus = inlineButton("+");

                Label count = new Label();
                count.getStyleClass().add("army-inline-label");
                count.textProperty().bind(wg.countProperty().asString());

                Label nameLabel = new Label(wg.getName());
                nameLabel.getStyleClass().add("army-inline-label");
                nameLabel.setWrapText(true);
                nameLabel.setMaxWidth(Double.MAX_VALUE);

                minus.setOnAction(e -> {
                    e.consume();
                    wg.dec();
                    onChanged.run();
                });

                plus.setOnAction(e -> {
                    e.consume();
                    if (wg.getCount() < item.modelCountProperty().get()) {
                        wg.inc();
                        onChanged.run();
                    }
                });

                Region rowSpacer = new Region();
                HBox.setHgrow(nameLabel, Priority.ALWAYS);
                HBox.setHgrow(rowSpacer, Priority.ALWAYS);

                HBox row = new HBox(8, nameLabel, rowSpacer, minus, count, plus);
                row.setAlignment(Pos.CENTER_LEFT);
                wgBox.getChildren().add(row);
            }

            expandBox.getChildren().add(wgBox);
        }

        if (item.isCharacter()) {
            ComboBox<EnhancementEntry> enhancementBox = new ComboBox<>();
            enhancementBox.getItems().addAll(enhancementProvider.apply(item));
            enhancementBox.setValue(item.getSelectedEnhancement());
            enhancementBox.setMaxWidth(Double.MAX_VALUE);

            enhancementBox.setOnAction(e -> {
                e.consume();
                EnhancementEntry selected = enhancementBox.getValue();
                item.setEnhancement(selected);
                onChanged.run();
            });

            Button warlordButton = inlineButton(
                    item.warlordProperty().get() ? "Unset Warlord" : "Set Warlord"
            );
            warlordButton.setOnAction(e -> {
                e.consume();
                onSetWarlord.accept(item);
            });

            Label enhancementLabel = new Label("Enhancement");
            enhancementLabel.getStyleClass().add("army-inline-label");

            HBox enhancementRow = new HBox(8, enhancementLabel, enhancementBox, warlordButton);
            enhancementRow.setAlignment(Pos.CENTER_LEFT);
            HBox.setHgrow(enhancementBox, Priority.ALWAYS);

            expandBox.getChildren().add(enhancementRow);
        }

        Label costText = new Label("Cost");
        costText.getStyleClass().add("army-inline-label");

        Label cost = new Label();
        cost.getStyleClass().add("army-inline-label");
        cost.textProperty().bind(item.pointsProperty().asString().concat(" pts"));

        expandBox.getChildren().add(new HBox(8, costText, cost));

        root.getChildren().addAll(topRow, stat, expandBox);
        setGraphic(root);
    }

    private Button inlineButton(String text) {
        Button button = new Button(text);
        button.getStyleClass().add("army-inline-button");
        return button;
    }

    private void bindToListWidth(Region region, double offset) {
        if (getListView() == null) return;
        region.prefWidthProperty().bind(getListView().widthProperty().subtract(offset));
        region.maxWidthProperty().bind(region.prefWidthProperty());
    }
}
