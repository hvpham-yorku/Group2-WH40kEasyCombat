package eecs2311.group2.wh40k_easycombat.service;

import eecs2311.group2.wh40k_easycombat.viewmodel.ArmyUnitVM;
import javafx.collections.ObservableList;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;

import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;

public final class ArmyEditorStateService {

    private ArmyEditorStateService() {
    }

    public static EditorResetResult resetEditor(
            ObservableList<ArmyUnitVM> currentArmy,
            TextField armyNameField,
            ComboBox<String> factionComboBox,
            ComboBox<Integer> sizeComboBox,
            Supplier<String> defaultFactionSupplier,
            Runnable refreshDetachmentOptionsAction,
            Runnable rebuildUnitTreeAction
    ) {
        ArmyBuilderManager.clearArmy(currentArmy);

        if (armyNameField != null) {
            armyNameField.clear();
        }

        if (sizeComboBox != null) {
            sizeComboBox.setValue(2000);
        }

        if (factionComboBox != null
                && factionComboBox.getValue() == null
                && !factionComboBox.getItems().isEmpty()) {
            factionComboBox.setValue(defaultFactionSupplier.get());
        }

        if (refreshDetachmentOptionsAction != null) {
            refreshDetachmentOptionsAction.run();
        }

        if (rebuildUnitTreeAction != null) {
            rebuildUnitTreeAction.run();
        }

        return new EditorResetResult(null, false);
    }

    public static LoadedEditorState applyLoadedArmy(
            ArmyControllerPersistence.LoadedArmyData loaded,
            ObservableList<ArmyUnitVM> currentArmy,
            Consumer<String> setFactionByIdAction,
            Runnable refreshDetachmentOptionsAction,
            Consumer<String> setDetachmentByIdAction,
            ComboBox<Integer> sizeComboBox,
            TextField armyNameField,
            Runnable rebuildUnitTreeAction
    ) {
        if (loaded == null || loaded.army() == null) {
            throw new IllegalArgumentException("Loaded army data is null.");
        }

        if (armyNameField != null) {
            armyNameField.setText(loaded.army().name());
        }

        if (setFactionByIdAction != null) {
            setFactionByIdAction.accept(loaded.army().faction_id());
        }

        if (refreshDetachmentOptionsAction != null) {
            refreshDetachmentOptionsAction.run();
        }

        if (setDetachmentByIdAction != null) {
            setDetachmentByIdAction.accept(loaded.detachmentId());
        }

        if (sizeComboBox != null) {
            sizeComboBox.setValue(loaded.sizeLimit());
        }

        ArmyBuilderManager.clearArmy(currentArmy);
        currentArmy.addAll(loaded.units());
        ArmyBuilderManager.sortArmy(currentArmy);

        if (rebuildUnitTreeAction != null) {
            rebuildUnitTreeAction.run();
        }

        return new LoadedEditorState(
                loaded.army().auto_id(),
                loaded.army().isMarked(),
                loaded.units()
        );
    }

    public record EditorResetResult(
            Integer editingArmyId,
            boolean editingArmyMarked
    ) {
    }

    public record LoadedEditorState(
            Integer editingArmyId,
            boolean editingArmyMarked,
            List<ArmyUnitVM> units
    ) {
    }
}