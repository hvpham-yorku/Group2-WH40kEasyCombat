package eecs2311.group2.wh40k_easycombat.model.combat;

import java.util.List;

public record CasualtyUpdate(
        int newlyDestroyedModels,
        List<String> destroyedModelNames,
        List<String> removedWeaponNames,
        boolean defenderDestroyed
) {
    public CasualtyUpdate {
        destroyedModelNames = destroyedModelNames == null ? List.of() : List.copyOf(destroyedModelNames);
        removedWeaponNames = removedWeaponNames == null ? List.of() : List.copyOf(removedWeaponNames);
    }

    public static CasualtyUpdate none() {
        return new CasualtyUpdate(0, List.of(), List.of(), false);
    }
}
