package eecs2311.group2.wh40k_easycombat.service;

import eecs2311.group2.wh40k_easycombat.model.aggregate.ArmyWriteAggregate;
import eecs2311.group2.wh40k_easycombat.model.Army;
import eecs2311.group2.wh40k_easycombat.repository.ArmyBundleRepository;

import java.sql.SQLException;

public final class ArmyBundleService {

    private ArmyBundleService() {
    }

    public static int createArmyBundle(ArmyWriteAggregate command) throws SQLException {
        validateForCreate(command);

        int newArmyId = ArmyBundleRepository.createBundle(toRepositoryBundle(command));
        StaticDataService.reloadFromSqlite();
        return newArmyId;
    }

    public static void updateArmyBundle(ArmyWriteAggregate command) throws SQLException {
        validateForUpdate(command);

        ArmyBundleRepository.updateBundle(toRepositoryBundle(command));
        StaticDataService.reloadFromSqlite();
    }

    public static void deleteArmyBundle(int armyId) throws SQLException {
        ArmyBundleRepository.deleteBundle(armyId);
        StaticDataService.reloadFromSqlite();
    }

    public static boolean toggleFavorite(int armyId) throws SQLException {
        Army army = StaticDataService.getArmy(armyId);
        if (army == null) {
            throw new IllegalStateException("Army could not be loaded.");
        }

        boolean newMarked = !army.isMarked();
        ArmyBundleRepository.updateMarked(armyId, newMarked);
        StaticDataService.reloadFromSqlite();
        return newMarked;
    }

    private static ArmyBundleRepository.ArmyWriteRecordBundle toRepositoryBundle(ArmyWriteAggregate command) {
        return new ArmyBundleRepository.ArmyWriteRecordBundle(
                command.army,
                command.detachments,
                command.units,
                command.wargear
        );
    }

    private static void validateForCreate(ArmyWriteAggregate command) {
        if (command == null || command.army == null) {
            throw new IllegalArgumentException("army command must not be null");
        }
    }

    private static void validateForUpdate(ArmyWriteAggregate command) {
        if (command == null || command.army == null) {
            throw new IllegalArgumentException("army command must not be null");
        }
        if (command.army.auto_id() <= 0) {
            throw new IllegalArgumentException("army.auto_id must be > 0 for update");
        }
    }
}
