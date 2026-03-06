package eecs2311.group2.wh40k_easycombat.util;

import eecs2311.group2.wh40k_easycombat.model.Units;

public final class SelectedUnitContext {

    private static Units selectedUnit;

    private SelectedUnitContext() {}

    public static void setSelectedUnit(Units unit) {
        selectedUnit = unit;
    }

    public static Units getSelectedUnit() {
        return selectedUnit;
    }

    public static void clear() {
        selectedUnit = null;
    }
}
