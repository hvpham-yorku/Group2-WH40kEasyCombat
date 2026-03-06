package eecs2311.group2.wh40k_easycombat.cell;

import eecs2311.group2.wh40k_easycombat.viewmodel.ArmyUnitVM;
import eecs2311.group2.wh40k_easycombat.viewmodel.ArmyUnitVM.EnhancementEntry;
import eecs2311.group2.wh40k_easycombat.viewmodel.ArmyUnitVM.WargearEntry;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;

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

        if (empty || item == null) {
            setText(null);
            setGraphic(null);
            return;
        }

        VBox root = new VBox(6);
        root.setPadding(new Insets(8));

        String previousRole = null;
        if (getListView() != null && getIndex() > 0 && getIndex() < getListView().getItems().size()) {
            ArmyUnitVM previous = getListView().getItems().get(getIndex() - 1);
            if (previous != null) {
                previousRole = previous.getRole();
            }
        }

        if (previousRole == null || !previousRole.equalsIgnoreCase(item.getRole())) {
            Label roleHeader = new Label(item.getRole());
            roleHeader.setStyle("-fx-font-weight: bold; -fx-font-size: 14px;");
            root.getChildren().add(roleHeader);
        }

        Button expandButton = new Button(item.expandedProperty().get() ? "▼" : "▶");
        expandButton.setOnAction(e -> {
            e.consume();
            item.expandedProperty().set(!item.expandedProperty().get());
            if (getListView() != null) {
                getListView().refresh();
            }
        });

        Label name = new Label((item.warlordProperty().get() ? "★ " : "") + item.getUnitName());
        name.setStyle("-fx-font-weight: bold;");

        Label stat = new Label(item.getStatLine());

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        HBox topRow = new HBox(8, expandButton, name, spacer);
        topRow.setAlignment(Pos.CENTER_LEFT);

        VBox expandBox = new VBox(6);
        expandBox.visibleProperty().bind(item.expandedProperty());
        expandBox.managedProperty().bind(item.expandedProperty());

        Button minusModel = new Button("-");
        Button plusModel = new Button("+");
        Label modelLabel = new Label();
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

        HBox modelRow = new HBox(8,
                new Label("Models"),
                minusModel,
                modelLabel,
                plusModel,
                new Label("Range: " + item.getMinModels() + "-" + item.getMaxModels())
        );
        modelRow.setAlignment(Pos.CENTER_LEFT);

        expandBox.getChildren().add(modelRow);

        if (!item.getWargears().isEmpty()) {
            VBox wgBox = new VBox(4);

            for (WargearEntry wg : item.getWargears()) {
                Button minus = new Button("-");
                Button plus = new Button("+");
                Label count = new Label();
                count.textProperty().bind(wg.countProperty().asString());

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

                HBox row = new HBox(8,
                        new Label(wg.getName()),
                        minus,
                        count,
                        plus
                );
                row.setAlignment(Pos.CENTER_LEFT);
                wgBox.getChildren().add(row);
            }

            expandBox.getChildren().add(wgBox);
        }

        if (item.isCharacter()) {
            ComboBox<EnhancementEntry> enhancementBox = new ComboBox<>();
            enhancementBox.getItems().addAll(enhancementProvider.apply(item));
            enhancementBox.setValue(item.getSelectedEnhancement());

            enhancementBox.setOnAction(e -> {
                e.consume();
                EnhancementEntry selected = enhancementBox.getValue();
                item.setEnhancement(selected);
                onChanged.run();
            });

            Button warlordButton = new Button(
                    item.warlordProperty().get() ? "Unset Warlord" : "Set Warlord"
            );
            warlordButton.setOnAction(e -> {
                e.consume();
                onSetWarlord.accept(item);
            });

            HBox enhancementRow = new HBox(8,
                    new Label("Enhancement"),
                    enhancementBox,
                    warlordButton
            );
            enhancementRow.setAlignment(Pos.CENTER_LEFT);

            expandBox.getChildren().add(enhancementRow);
        }

        Label cost = new Label();
        cost.textProperty().bind(item.pointsProperty().asString().concat(" pts"));
        expandBox.getChildren().add(new HBox(8, new Label("Cost"), cost));

        root.getChildren().addAll(topRow, stat, expandBox);

        setGraphic(root);
    }
}