package eecs2311.group2.wh40k_easycombat.manager;

import assembler.ArmyStrategyAssembler;
import assembler.SavedArmyGameAssembler.ImportedArmyData;
import eecs2311.group2.wh40k_easycombat.controller.GameUIController.ArmySide;
import eecs2311.group2.wh40k_easycombat.viewmodel.GameArmyUnitVM;
import eecs2311.group2.wh40k_easycombat.viewmodel.GameStrategyVM;
import javafx.collections.ObservableList;

public final class GameStateManager {

    private GameStateManager() {
    }

    public static void applyImportedArmy(
            ArmySide side,
            ImportedArmyData data,
            ObservableList<GameArmyUnitVM> blueArmyUnits,
            ObservableList<GameArmyUnitVM> redArmyUnits,
            ObservableList<GameStrategyVM> blueStrategies,
            ObservableList<GameStrategyVM> redStrategies
    ) throws Exception {
        if (data == null) return;

        ObservableList<GameStrategyVM> importedStrategies =
                javafx.collections.FXCollections.observableArrayList(
                        ArmyStrategyAssembler.importStrategiesForArmy(data.armyId())
                );

        if (side == ArmySide.BLUE) {
            blueArmyUnits.setAll(data.units());
            blueStrategies.setAll(importedStrategies);
        } else {
            redArmyUnits.setAll(data.units());
            redStrategies.setAll(importedStrategies);
        }
    }

    public static void clearSide(
            ArmySide side,
            ObservableList<GameArmyUnitVM> blueArmyUnits,
            ObservableList<GameArmyUnitVM> redArmyUnits,
            ObservableList<GameStrategyVM> blueStrategies,
            ObservableList<GameStrategyVM> redStrategies
    ) {
        if (side == ArmySide.BLUE) {
            blueArmyUnits.clear();
            blueStrategies.clear();
        } else {
            redArmyUnits.clear();
            redStrategies.clear();
        }
    }
}