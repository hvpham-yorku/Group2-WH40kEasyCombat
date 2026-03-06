package eecs2311.group2.wh40k_easycombat.cell;

import eecs2311.group2.wh40k_easycombat.viewmodel.GameArmyUnitVM;
import eecs2311.group2.wh40k_easycombat.viewmodel.GameSubUnitVM;
import eecs2311.group2.wh40k_easycombat.viewmodel.GameWeaponVM;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

public class GameArmyUnitCell extends ListCell<GameArmyUnitVM> {

    @Override
    protected void updateItem(GameArmyUnitVM item, boolean empty) {
        super.updateItem(item, empty);

        if (empty || item == null) {
            setText(null);
            setGraphic(null);
            return;
        }

        VBox root = new VBox(6);
        root.setPadding(new Insets(8));

        Button expandButton = new Button(item.expandedProperty().get() ? "▼" : "▶");
        expandButton.setOnAction(e -> {
            e.consume();
            item.expandedProperty().set(!item.expandedProperty().get());
            if (getListView() != null) {
                getListView().refresh();
            }
        });

        Label unitName = new Label(item.getUnitName());
        unitName.setStyle("-fx-font-weight: bold;");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        HBox header = new HBox(8, expandButton, unitName, spacer);
        header.setAlignment(Pos.CENTER_LEFT);

        VBox detailBox = new VBox(6);
        detailBox.visibleProperty().bind(item.expandedProperty());
        detailBox.managedProperty().bind(item.expandedProperty());

        for (GameSubUnitVM sub : item.getSubUnits()) {
            detailBox.getChildren().add(buildSubUnitBox(sub));
        }

        if (!item.getRangedWeapons().isEmpty()) {
            Label title = new Label("Ranged Weapons");
            title.setStyle("-fx-font-weight: bold;");
            detailBox.getChildren().add(title);

            for (GameWeaponVM row : item.getRangedWeapons()) {
                detailBox.getChildren().add(buildWeaponBox(row, false));
            }
        }

        if (!item.getMeleeWeapons().isEmpty()) {
            Label title = new Label("Melee Weapons");
            title.setStyle("-fx-font-weight: bold;");
            detailBox.getChildren().add(title);

            for (GameWeaponVM row : item.getMeleeWeapons()) {
                detailBox.getChildren().add(buildWeaponBox(row, true));
            }
        }

        root.getChildren().addAll(header, detailBox);
        setGraphic(root);
    }

    private VBox buildSubUnitBox(GameSubUnitVM sub) {
        Label name = new Label(sub.getName());

        TextField hpField = new TextField();
        hpField.setPrefWidth(60);
        hpField.textProperty().bindBidirectional(sub.hpProperty());

        Label hpLabel = new Label("HP");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        HBox topRow = new HBox(8, name, spacer, hpLabel, hpField);
        topRow.setAlignment(Pos.CENTER_LEFT);

        Label statRow = new Label(buildSubUnitStatText(sub));
        statRow.setWrapText(true);

        VBox box = new VBox(2, topRow, statRow);
        box.setPadding(new Insets(2, 0, 2, 20));
        return box;
    }

    private VBox buildWeaponBox(GameWeaponVM row, boolean melee) {
        Label name = new Label(row.getName());

        Label count = new Label("x" + row.getCount());

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        HBox topRow = new HBox(8, name, spacer, count);
        topRow.setAlignment(Pos.CENTER_LEFT);

        Label statRow = new Label(
                "Range: " + safe(row.getRange())
                        + "   A: " + safe(row.getA())
                        + "   " + (melee ? "WS" : "BS") + ": " + safe(row.getSkill())
                        + "   S: " + safe(row.getS())
                        + "   AP: " + safe(row.getAp())
                        + "   D: " + safe(row.getD())
        );
        statRow.setWrapText(true);

        VBox box = new VBox(2, topRow, statRow);
        box.setPadding(new Insets(2, 0, 2, 20));
        return box;
    }

    private String buildSubUnitStatText(GameSubUnitVM sub) {
        String text = "M:" + safe(sub.getM())
                + "   T:" + safe(sub.getT())
                + "   SV:" + safe(sub.getSv())
                + "   W:" + safe(sub.getW())
                + "   Ld:" + safe(sub.getLd())
                + "   OC:" + safe(sub.getOc());

        if (sub.getInv() != null && !sub.getInv().isBlank() && !"-".equals(sub.getInv())) {
            text += "   Inv:" + sub.getInv();
        }

        return text;
    }

    private String safe(String s) {
        return s == null ? "" : s;
    }
}