package eecs2311.group2.wh40k_easycombat.service;

import eecs2311.group2.wh40k_easycombat.aggregate.DatasheetAggregate;
import eecs2311.group2.wh40k_easycombat.repository.DatasheetBundleRepository;

import java.sql.SQLException;

public final class StaticDataCrudService {

    private StaticDataCrudService() {
    }

    public static void saveBundle(DatasheetAggregate bundle) throws SQLException {
        if (bundle == null || bundle.datasheet == null || bundle.datasheet.id() == null) {
            throw new IllegalArgumentException("bundle/datasheet/id must not be null");
        }

        DatasheetBundleRepository.saveBundle(
                new DatasheetBundleRepository.DatasheetRecordBundle(
                        bundle.datasheet,
                        bundle.models,
                        bundle.wargear,
                        bundle.abilities,
                        bundle.compositions,
                        bundle.costs,
                        bundle.keywords,
                        bundle.options,
                        bundle.leaders,
                        bundle.stratagems,
                        bundle.enhancements,
                        bundle.detachmentAbilities
                )
        );

        StaticDataService.reloadFromSqlite();
    }
}