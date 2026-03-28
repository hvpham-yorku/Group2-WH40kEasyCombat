package eecs2311.group2.wh40k_easycombat.cell;

import eecs2311.group2.wh40k_easycombat.model.instance.UnitModelInstance;
import eecs2311.group2.wh40k_easycombat.model.instance.WeaponProfile;
import eecs2311.group2.wh40k_easycombat.service.BattleLogService;
import eecs2311.group2.wh40k_easycombat.service.editor.EditorEffectRuntimeService;
import eecs2311.group2.wh40k_easycombat.service.game.ArmyListStateService;
import eecs2311.group2.wh40k_easycombat.viewmodel.GameArmyUnitVM;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

public class GameArmyUnitCell extends ListCell<GameArmyUnitVM> {
    private final EditorEffectRuntimeService effectRuntimeService = EditorEffectRuntimeService.getInstance();
    private final BattleLogService battleLogService = BattleLogService.getInstance();

    private final Runnable onStateChanged;

    public GameArmyUnitCell() {
        this(null);
    }

    public GameArmyUnitCell(Runnable onStateChanged) {
        this.onStateChanged = onStateChanged;
    }

    @Override
    protected void updateItem(GameArmyUnitVM item, boolean empty) {
        super.updateItem(item, empty);

        setText(null);
        setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
        getStyleClass().remove("game-unit-row-destroyed");

        if (empty || item == null) {
            setGraphic(null);
            return;
        }

        if (item.isDestroyed()) {
            getStyleClass().add("game-unit-row-destroyed");
        }

        VBox root = new VBox(8);
        root.setPadding(new Insets(10));
        root.getStyleClass().add("game-army-unit-card");
        if (item.isDestroyed()) {
            root.getStyleClass().add("game-army-unit-card-destroyed");
        }
        bindToListWidth(root, 22);

        Button expandButton = new Button(item.expandedProperty().get() ? "-" : "+");
        expandButton.getStyleClass().add("game-army-inline-button");
        expandButton.setPrefWidth(28);
        expandButton.setMinWidth(28);

        expandButton.setOnAction(e -> {
            e.consume();
            item.expandedProperty().set(!item.expandedProperty().get());
            if (getListView() != null) {
                getListView().refresh();
            }
        });

        Label unitName = new Label(item.getUnitName());
        unitName.getStyleClass().add("game-army-unit-name");
        if (item.isDestroyed()) {
            unitName.getStyleClass().add("game-army-unit-name-destroyed");
        }
        unitName.setWrapText(true);
        unitName.setMaxWidth(Double.MAX_VALUE);

        Label unitSummary = new Label(buildUnitSummary(item));
        unitSummary.getStyleClass().add("game-army-unit-summary");
        if (item.isDestroyed()) {
            unitSummary.getStyleClass().add("game-army-unit-summary-destroyed");
        }
        unitSummary.setWrapText(true);

        CheckBox battleShockBox = new CheckBox("Battle-shocked");
        battleShockBox.getStyleClass().add("game-checkbox");
        battleShockBox.setSelected(item.getUnit().isBattleShocked());
        battleShockBox.setDisable(item.isDestroyed());
        battleShockBox.selectedProperty().addListener((obs, oldValue, newValue) -> {
            item.getUnit().setBattleShocked(newValue);
            if (oldValue != newValue) {
                battleLogService.log("Manual status change: "
                        + item.getUnitName()
                        + " is now "
                        + (newValue ? "Battle-shocked." : "not Battle-shocked."));
            }
            if (onStateChanged != null) {
                onStateChanged.run();
            }
            if (getListView() != null) {
                getListView().refresh();
            }
        });

        Label currentOcLabel = new Label("Current OC: " + item.getUnit().getCurrentOc());
        currentOcLabel.getStyleClass().add("game-army-unit-summary");
        currentOcLabel.setWrapText(true);
        if (item.getUnit().isBattleShocked()) {
            currentOcLabel.getStyleClass().add("game-army-unit-summary-battleshocked");
        }

        Region statusSpacer = new Region();
        HBox.setHgrow(statusSpacer, Priority.ALWAYS);
        HBox statusRow = new HBox(10, battleShockBox, statusSpacer, currentOcLabel);
        statusRow.setAlignment(Pos.CENTER_LEFT);

        Region spacer = new Region();
        HBox.setHgrow(unitName, Priority.ALWAYS);
        HBox.setHgrow(spacer, Priority.ALWAYS);

        HBox header = new HBox(8, expandButton, unitName, spacer);
        header.setAlignment(Pos.CENTER_LEFT);

        String activeEffectsText = buildActiveEffectsText(item);
        Label activeEffectsLabel = new Label(activeEffectsText);
        activeEffectsLabel.getStyleClass().add("game-army-unit-summary");
        activeEffectsLabel.setWrapText(true);
        activeEffectsLabel.setManaged(!activeEffectsText.isBlank());
        activeEffectsLabel.setVisible(!activeEffectsText.isBlank());

        VBox detailBox = new VBox(6);
        detailBox.visibleProperty().bind(item.expandedProperty());
        detailBox.managedProperty().bind(item.expandedProperty());

        for (UnitModelInstance sub : item.getSubUnits()) {
            detailBox.getChildren().add(buildSubUnitBox(sub));
        }

        if (!item.getRangedWeapons().isEmpty()) {
            Label title = buildSectionTitle("Ranged Weapons");
            detailBox.getChildren().add(title);

            for (WeaponProfile row : item.getRangedWeapons()) {
                detailBox.getChildren().add(buildWeaponBox(row, false));
            }
        }

        if (!item.getMeleeWeapons().isEmpty()) {
            Label title = buildSectionTitle("Melee Weapons");
            detailBox.getChildren().add(title);

            for (WeaponProfile row : item.getMeleeWeapons()) {
                detailBox.getChildren().add(buildWeaponBox(row, true));
            }
        }

        root.getChildren().addAll(header, unitSummary, statusRow, activeEffectsLabel, detailBox);
        setGraphic(root);
    }

    private Label buildSectionTitle(String text) {
        Label title = new Label(text);
        title.getStyleClass().add("game-army-section-title");
        return title;
    }

    private VBox buildSubUnitBox(UnitModelInstance sub) {
        Label name = new Label(sub.getModelName());
        name.getStyleClass().add("game-army-model-name");
        name.setWrapText(true);
        if (sub.isDestroyed()) {
            name.getStyleClass().add("game-army-model-name-destroyed");
        }

        TextField hpField = new TextField(String.valueOf(sub.getCurrentHp()));
        hpField.getStyleClass().add("game-army-hp-field");
        hpField.setPrefWidth(64);
        hpField.setOnAction(e -> syncHpField(sub, hpField));
        hpField.focusedProperty().addListener((obs, oldValue, focused) -> {
            if (!focused) {
                syncHpField(sub, hpField);
            }
        });

        Label hpLabel = new Label("HP");
        hpLabel.getStyleClass().add("game-army-inline-label");

        Region spacer = new Region();
        HBox.setHgrow(name, Priority.ALWAYS);
        HBox.setHgrow(spacer, Priority.ALWAYS);

        HBox topRow = new HBox(8, name, spacer, hpLabel, hpField);
        topRow.setAlignment(Pos.CENTER_LEFT);

        Label statRow = new Label(buildSubUnitStatText(sub));
        statRow.getStyleClass().add("game-army-model-stats");
        statRow.setWrapText(true);
        if (sub.isDestroyed()) {
            statRow.getStyleClass().add("game-army-model-stats-destroyed");
        }

        VBox box = new VBox(3, topRow, statRow);
        box.setPadding(new Insets(2, 0, 2, 20));
        box.getStyleClass().add("game-army-model-card");
        if (sub.isDestroyed()) {
            box.getStyleClass().add("game-army-model-card-destroyed");
        }
        return box;
    }

    private void syncHpField(UnitModelInstance sub, TextField hpField) {
        int beforeHp = sub.getCurrentHp();
        try {
            sub.setCurrentHp(Integer.parseInt(hpField.getText().trim()));
        } catch (Exception ignored) {
        }

        GameArmyUnitVM currentItem = getItem();
        if (currentItem != null) {
            ArmyListStateService.reconcileUnitState(currentItem.getUnit());
        }

        hpField.setText(String.valueOf(sub.getCurrentHp()));

        if (currentItem != null && beforeHp != sub.getCurrentHp()) {
            battleLogService.log("Manual model update: "
                    + currentItem.getUnitName()
                    + " -> "
                    + sub.getModelName()
                    + " HP "
                    + beforeHp
                    + " -> "
                    + sub.getCurrentHp()
                    + ".");
        }

        if (onStateChanged != null) {
            onStateChanged.run();
        }

        if (getListView() != null) {
            getListView().refresh();
        }
    }

    private VBox buildWeaponBox(WeaponProfile row, boolean melee) {
        Label name = new Label(row.name());
        name.getStyleClass().add("game-army-weapon-name");
        name.setWrapText(true);
        name.setMaxWidth(Double.MAX_VALUE);

        Label count = new Label("x" + row.count());
        count.getStyleClass().add("game-army-weapon-count");

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
        statRow.getStyleClass().add("game-army-weapon-stats");
        statRow.setWrapText(true);

        VBox box = new VBox(3, topRow, statRow);
        box.setPadding(new Insets(2, 0, 2, 20));
        box.getStyleClass().add("game-army-weapon-card");
        return box;
    }

    private String buildUnitSummary(GameArmyUnitVM item) {
        String status = item.isDestroyed()
                ? "Destroyed"
                : item.getUnit().isBattleShocked() ? "Battle-shocked" : "Active";

        if (!item.isDestroyed() && item.getUnit().isBelowHalfStrength()) {
            status += " | Below Half-strength";
        }

        return "Models Alive: " + item.getAliveModelCount()
                + "/" + item.getSubUnits().size()
                + "   Status: " + status;
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

    private String buildActiveEffectsText(GameArmyUnitVM item) {
        if (item == null || item.getUnit() == null) {
            return "";
        }

        return effectRuntimeService.activeEffectsForUnit(item.getUnit().getInstanceId()).stream()
                .map(effect -> effect.ruleName() + " [" + effect.duration() + "]")
                .reduce((left, right) -> left + " | " + right)
                .map(text -> "Active Effects: " + text)
                .orElse("");
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
