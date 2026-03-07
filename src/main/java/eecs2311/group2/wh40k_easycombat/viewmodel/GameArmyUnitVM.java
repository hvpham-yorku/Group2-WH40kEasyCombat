package eecs2311.group2.wh40k_easycombat.viewmodel;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

public class GameArmyUnitVM {

    private final String unitName;
    private final ObservableList<GameSubUnitVM> subUnits = FXCollections.observableArrayList();
    private final ObservableList<GameWeaponVM> rangedWeapons = FXCollections.observableArrayList();
    private final ObservableList<GameWeaponVM> meleeWeapons = FXCollections.observableArrayList();
    private final BooleanProperty expanded = new SimpleBooleanProperty(false);

    public GameArmyUnitVM(String unitName) {
        this.unitName = unitName == null ? "" : unitName;
    }

    public String getUnitName() {
        return unitName;
    }

    public ObservableList<GameSubUnitVM> getSubUnits() {
        return subUnits;
    }

    public ObservableList<GameWeaponVM> getRangedWeapons() {
        return rangedWeapons;
    }

    public ObservableList<GameWeaponVM> getMeleeWeapons() {
        return meleeWeapons;
    }

    public BooleanProperty expandedProperty() {
        return expanded;
    }
}