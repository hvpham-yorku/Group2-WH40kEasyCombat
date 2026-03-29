package eecs2311.group2.wh40k_easycombat.util;

import eecs2311.group2.wh40k_easycombat.db.Database;
import eecs2311.group2.wh40k_easycombat.service.vm.VMService;

import java.io.IOException;
import java.sql.SQLException;

public final class AppBootstrap {
    private AppBootstrap() {
    }

    public static void initializeApplication() throws IOException, SQLException {
        BundledResourceManager.ensureBundledResourcesAvailable();
        Database.useApplicationDatabase();
        Database.ensureSchema();
        VMService.loadFolder(AppPaths.getBundledDslDirectory());
        CsvToSqliteImporter.importDefaultCsvSeedLike(false);
    }
}
