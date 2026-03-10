package eecs2311.group2.wh40k_easycombat.viewmodel;

import eecs2311.group2.wh40k_easycombat.model.instance.UnitInstance;
import eecs2311.group2.wh40k_easycombat.model.instance.UnitModelInstance;
import eecs2311.group2.wh40k_easycombat.model.instance.WeaponProfile;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;

import java.util.List;

public class GameArmyUnitVM {

    private final UnitInstance unit;
    private final BooleanProperty expanded = new SimpleBooleanProperty(false);

    public GameArmyUnitVM(UnitInstance unit) {
        this.unit = unit == null ? new UnitInstance("", "") : unit;
    }

    public UnitInstance getUnit() {
        return unit;
    }

    public String getUnitName() {
        return unit.getUnitName();
    }

    public List<UnitModelInstance> getSubUnits() {
        return unit.getModels();
    }

    public List<WeaponProfile> getRangedWeapons() {
        return unit.getRangedWeapons();
    }

    public List<WeaponProfile> getMeleeWeapons() {
        return unit.getMeleeWeapons();
    }

    public BooleanProperty expandedProperty() {
        return expanded;
    }
}
