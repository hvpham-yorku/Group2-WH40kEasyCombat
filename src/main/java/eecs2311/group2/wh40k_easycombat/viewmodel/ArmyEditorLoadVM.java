package eecs2311.group2.wh40k_easycombat.viewmodel;

import eecs2311.group2.wh40k_easycombat.model.Army;

import java.util.List;

public record ArmyEditorLoadVM(
        Army army,
        String detachmentId,
        int sizeLimit,
        List<ArmyUnitVM> units
) {
    public ArmyEditorLoadVM {
        units = units == null ? List.of() : List.copyOf(units);
    }
}