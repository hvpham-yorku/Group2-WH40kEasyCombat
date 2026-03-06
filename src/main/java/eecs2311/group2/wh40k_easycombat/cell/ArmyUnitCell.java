package eecs2311.group2.wh40k_easycombat.cell;

import eecs2311.group2.wh40k_easycombat.viewmodel.ArmyUnitVM;

import javafx.scene.control.*;
import javafx.scene.layout.*;

public class ArmyUnitCell extends ListCell<ArmyUnitVM> {

    private VBox root = new VBox(5);

    private Label name = new Label();
    private Label stat = new Label();

    private VBox expandBox = new VBox(5);

    private Button minusModel = new Button("-");
    private Button plusModel = new Button("+");

    private Button minusWeapon = new Button("-");
    private Button plusWeapon = new Button("+");

    private Label modelLabel = new Label();
    private Label weaponLabel = new Label();
    private Label costLabel = new Label();

    public ArmyUnitCell() {

        HBox modelRow = new HBox(5,
                new Label("Models"),
                minusModel,
                modelLabel,
                plusModel);

        HBox weaponRow = new HBox(5,
                new Label("Weapons"),
                minusWeapon,
                weaponLabel,
                plusWeapon);

        HBox costRow = new HBox(5,
                new Label("Cost"),
                costLabel);

        expandBox.getChildren().addAll(modelRow, weaponRow, costRow);

        root.getChildren().addAll(name, stat, expandBox);

        root.setOnMouseClicked(e -> {
            if (getItem() != null)
                getItem().expandedProperty().set(!getItem().expandedProperty().get());
        });

        minusModel.setOnMouseClicked(e -> {
            e.consume();
            getItem().decModels();
        });

        plusModel.setOnMouseClicked(e -> {
            e.consume();
            getItem().incModels();
        });

        minusWeapon.setOnMouseClicked(e -> {
            e.consume();
            getItem().decWeapons();
        });

        plusWeapon.setOnMouseClicked(e -> {
            e.consume();
            getItem().incWeapons();
        });
    }

    @Override
    protected void updateItem(ArmyUnitVM item, boolean empty) {

        super.updateItem(item, empty);

        if (empty || item == null) {
            setGraphic(null);
            return;
        }

        name.setText(item.getUnitName());
        stat.setText(item.getStatLine());

        modelLabel.textProperty().bind(item.modelCountProperty().asString());
        weaponLabel.textProperty().bind(item.weaponCountProperty().asString());
        costLabel.textProperty().bind(item.pointsProperty().asString().concat(" pts"));

        expandBox.visibleProperty().bind(item.expandedProperty());
        expandBox.managedProperty().bind(item.expandedProperty());

        setGraphic(root);
    }
}