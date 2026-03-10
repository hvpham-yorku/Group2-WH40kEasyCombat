package eecs2311.group2.wh40k_easycombat.aggregate;

import eecs2311.group2.wh40k_easycombat.model.Army;
import eecs2311.group2.wh40k_easycombat.model.Army_detachment;
import eecs2311.group2.wh40k_easycombat.model.Army_units;
import eecs2311.group2.wh40k_easycombat.model.Army_wargear;

import java.util.List;

public final class ArmyAggregate {
    public final Army army;
    public final List<Army_detachment> detachments;
    public final List<Army_units> units;
    public final List<Army_wargear> wargear;

    public ArmyAggregate(
            Army army,
            List<Army_detachment> detachments,
            List<Army_units> units,
            List<Army_wargear> wargear
    ) {
        this.army = army;
        this.detachments = immutableList(detachments);
        this.units = immutableList(units);
        this.wargear = immutableList(wargear);
    }

    private static <T> List<T> immutableList(List<T> source) {
        return source == null ? List.of() : List.copyOf(source);
    }
}