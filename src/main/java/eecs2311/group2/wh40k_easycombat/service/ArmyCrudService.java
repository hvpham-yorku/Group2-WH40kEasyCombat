package eecs2311.group2.wh40k_easycombat.service;

import eecs2311.group2.wh40k_easycombat.aggregate.ArmyWriteAggregate;
import eecs2311.group2.wh40k_easycombat.repository.ArmyBundleRepository;

import java.sql.SQLException;

public final class ArmyCrudService {

    private ArmyCrudService() {
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
