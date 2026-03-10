package eecs2311.group2.wh40k_easycombat.service;

import eecs2311.group2.wh40k_easycombat.model.Army;
import eecs2311.group2.wh40k_easycombat.repository.ArmyBundleRepository;

public final class ArmyFavoriteService {

    private ArmyFavoriteService() {
    }

    public static boolean toggleFavorite(int armyId) throws Exception {
        Army army = StaticDataService.getArmy(armyId);
        if (army == null) {
            throw new IllegalStateException("Army could not be loaded.");
        }

        boolean newMarked = !army.isMarked();
        ArmyBundleRepository.updateMarked(armyId, newMarked);
        StaticDataService.reloadFromSqlite();
        return newMarked;
    }
}
