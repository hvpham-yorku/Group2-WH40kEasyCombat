package eecs2311.group2.wh40k_easycombat.service;

import eecs2311.group2.wh40k_easycombat.model.Army;
import eecs2311.group2.wh40k_easycombat.model.Army_detachment;

import java.util.List;

public final class ArmyFavoriteService {

    private ArmyFavoriteService() {
    }

    public static boolean toggleFavorite(int armyId) throws Exception {
        StaticDataService.ArmyBundle bundle = StaticDataService.getArmyBundle(armyId);
        if (bundle == null || bundle.army == null) {
            throw new IllegalStateException("Army bundle could not be loaded.");
        }

        List<Army_detachment> detachments = StaticDataService.getArmyDetachments(armyId);

        boolean newMarked = !bundle.army.isMarked();

        Army updated = new Army(
                bundle.army.auto_id(),
                bundle.army.name(),
                bundle.army.faction_id(),
                bundle.army.warlord_id(),
                bundle.army.total_points(),
                newMarked
        );

        ArmyCrudService.updateArmyBundle(
                new ArmyCrudService.ArmyWriteBundle(updated, detachments, bundle.units, bundle.wargear)
        );

        return newMarked;
    }
}