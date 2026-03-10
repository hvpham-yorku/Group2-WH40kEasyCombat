package eecs2311.group2.wh40k_easycombat.service;

import eecs2311.group2.wh40k_easycombat.model.Army;
import eecs2311.group2.wh40k_easycombat.model.Army_detachment;
import eecs2311.group2.wh40k_easycombat.model.Army_units;
import eecs2311.group2.wh40k_easycombat.model.Army_wargear;
import eecs2311.group2.wh40k_easycombat.repository.ArmyBundleRepository;

import java.sql.SQLException;
import java.util.List;

public final class ArmyCrudService {

    private ArmyCrudService() {
    }

    public static final class ArmyWriteBundle {
        public final Army army;
        public final List<Army_detachment> detachments;
        public final List<Army_units> units;
        public final List<Army_wargear> wargear;

        public ArmyWriteBundle(
                Army army,
                List<Army_detachment> detachments,
                List<Army_units> units,
                List<Army_wargear> wargear
        ) {
            this.army = army;
            this.detachments = detachments != null ? detachments : List.of();
            this.units = units != null ? units : List.of();
            this.wargear = wargear != null ? wargear : List.of();
        }
    }

    public static StaticDataService.ArmyBundle getArmyBundle(int armyId) throws SQLException {
        return StaticDataService.getArmyBundle(armyId);
    }

    public static int createArmyBundle(ArmyWriteBundle bundle) throws SQLException {
        validateBundleForCreate(bundle);

        int newArmyId = ArmyBundleRepository.createBundle(toRepositoryBundle(bundle));
        StaticDataService.reloadFromSqlite();
        return newArmyId;
    }

    public static void updateArmyBundle(ArmyWriteBundle bundle) throws SQLException {
        validateBundleForUpdate(bundle);

        ArmyBundleRepository.updateBundle(toRepositoryBundle(bundle));
        StaticDataService.reloadFromSqlite();
    }

    public static void deleteArmyBundle(int armyId) throws SQLException {
        ArmyBundleRepository.deleteBundle(armyId);
        StaticDataService.reloadFromSqlite();
    }

    private static ArmyBundleRepository.ArmyWriteRecordBundle toRepositoryBundle(ArmyWriteBundle bundle) {
        return new ArmyBundleRepository.ArmyWriteRecordBundle(
                bundle.army,
                bundle.detachments,
                bundle.units,
                bundle.wargear
        );
    }

    private static void validateBundleForCreate(ArmyWriteBundle bundle) {
        if (bundle == null || bundle.army == null) {
            throw new IllegalArgumentException("army bundle must not be null");
        }
    }

    private static void validateBundleForUpdate(ArmyWriteBundle bundle) {
        if (bundle == null || bundle.army == null) {
            throw new IllegalArgumentException("army bundle must not be null");
        }
        if (bundle.army.auto_id() <= 0) {
            throw new IllegalArgumentException("army.auto_id must be > 0 for update");
        }
    }
}
