package eecs2311.group2.wh40k_easycombat.application;

import eecs2311.group2.wh40k_easycombat.model.Army;
import eecs2311.group2.wh40k_easycombat.model.Army_detachment;
import eecs2311.group2.wh40k_easycombat.model.Army_units;
import eecs2311.group2.wh40k_easycombat.model.Army_wargear;

import java.util.List;

public final class ArmyWriteCommand {
    public final Army army;
    public final List<Army_detachment> detachments;
    public final List<Army_units> units;
    public final List<Army_wargear> wargear;

    public ArmyWriteCommand(
            Army army,
            List<Army_detachment> detachments,
            List<Army_units> units,
            List<Army_wargear> wargear
    ) {
        this.army = army;
        this.detachments = detachments != null ? List.copyOf(detachments) : List.of();
        this.units = units != null ? List.copyOf(units) : List.of();
        this.wargear = wargear != null ? List.copyOf(wargear) : List.of();
    }
}