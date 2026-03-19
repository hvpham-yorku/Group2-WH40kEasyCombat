package eecs2311.group2.wh40k_easycombat.cell;

import eecs2311.group2.wh40k_easycombat.model.instance.UnitModelInstance;
import eecs2311.group2.wh40k_easycombat.model.instance.WeaponProfile;
import eecs2311.group2.wh40k_easycombat.viewmodel.GameArmyUnitVM;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.ContentDisplay;
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

        Label unitName = new Label(item.getUnitName());
        unitName.setWrapText(true);
        unitName.setMaxWidth(Double.MAX_VALUE);
        unitName.setStyle("-fx-font-weight: bold; -fx-font-size: 14px;");

        Region spacer = new Region();
        HBox.setHgrow(unitName, Priority.ALWAYS);
        HBox.setHgrow(spacer, Priority.ALWAYS);

        HBox header = new HBox(8, expandButton, unitName, spacer);
        header.setAlignment(Pos.CENTER_LEFT);

        VBox detailBox = new VBox(6);
        detailBox.visibleProperty().bind(item.expandedProperty());
        detailBox.managedProperty().bind(item.expandedProperty());

        for (UnitModelInstance sub : item.getSubUnits()) {
            detailBox.getChildren().add(buildSubUnitBox(sub));
        }

        if (!item.getRangedWeapons().isEmpty()) {
            Label title = new Label("Ranged Weapons");
            title.setStyle("-fx-font-weight: bold; -fx-font-size: 13px;");
            detailBox.getChildren().add(title);

            for (WeaponProfile row : item.getRangedWeapons()) {
                detailBox.getChildren().add(buildWeaponBox(row, false));
            }
        }

        if (!item.getMeleeWeapons().isEmpty()) {
            Label title = new Label("Melee Weapons");
            title.setStyle("-fx-font-weight: bold; -fx-font-size: 13px;");
            detailBox.getChildren().add(title);

            for (WeaponProfile row : item.getMeleeWeapons()) {
                detailBox.getChildren().add(buildWeaponBox(row, true));
            }
        }

        root.getChildren().addAll(header, detailBox);
        setGraphic(root);
    }

    private VBox buildSubUnitBox(UnitModelInstance sub) {
        Label name = new Label(sub.getModelName());
        name.setWrapText(true);
        name.setStyle("-fx-font-size: 13px;");

        TextField hpField = new TextField(String.valueOf(sub.getCurrentHp()));
        hpField.setPrefWidth(60);
        hpField.setStyle("-fx-font-size: 13px;");
        hpField.setOnAction(e -> syncHpField(sub, hpField));
        hpField.focusedProperty().addListener((obs, oldV, newV) -> {
            if (!newV) {
                syncHpField(sub, hpField);
            }
        });

        Label hpLabel = new Label("HP");
        hpLabel.setStyle("-fx-font-size: 13px;");

        Region spacer = new Region();
        HBox.setHgrow(name, Priority.ALWAYS);
        HBox.setHgrow(spacer, Priority.ALWAYS);

        HBox topRow = new HBox(8, name, spacer, hpLabel, hpField);
        topRow.setAlignment(Pos.CENTER_LEFT);

        Label statRow = new Label(buildSubUnitStatText(sub));
        statRow.setWrapText(true);
        statRow.setStyle("-fx-font-size: 13px;");

        VBox box = new VBox(3, topRow, statRow);
        box.setPadding(new Insets(2, 0, 2, 20));
        return box;
    }

    private void syncHpField(UnitModelInstance sub, TextField hpField) {
        try {
            sub.setCurrentHp(Integer.parseInt(hpField.getText().trim()));
        } catch (Exception ignored) {
        }

        hpField.setText(String.valueOf(sub.getCurrentHp()));

        if (getListView() != null) {
            getListView().refresh();
        }
    }

    private VBox buildWeaponBox(WeaponProfile row, boolean melee) {
        Label name = new Label(row.name());
        name.setWrapText(true);
        name.setMaxWidth(Double.MAX_VALUE);
        name.setStyle("-fx-font-size: 13px;");

        Label count = new Label("x" + row.count());
        count.setStyle("-fx-font-size: 13px;");

        Region spacer = new Region();
        HBox.setHgrow(name, Priority.ALWAYS);
        HBox.setHgrow(spacer, Priority.ALWAYS);

        HBox topRow = new HBox(8, name, spacer, count);
        topRow.setAlignment(Pos.CENTER_LEFT);

        Label statRow = new Label(
                "Range: " + safe(row.range())
                        + "   A: " + safe(row.a())
                        + "   " + (melee ? "WS" : "BS") + ": " + safe(row.skill())
                        + "   S: " + safe(row.s())
                        + "   AP: " + safe(row.ap())
                        + "   D: " + safe(row.d())
        );
        statRow.setWrapText(true);
        statRow.setStyle("-fx-font-size: 13px;");

        VBox box = new VBox(3, topRow, statRow);
        box.setPadding(new Insets(2, 0, 2, 20));
        return box;
    }

    private String buildSubUnitStatText(UnitModelInstance sub) {
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

    private void bindToListWidth(Region region, double offset) {
        if (getListView() == null) return;
        region.prefWidthProperty().bind(getListView().widthProperty().subtract(offset));
        region.maxWidthProperty().bind(region.prefWidthProperty());
    }
}
