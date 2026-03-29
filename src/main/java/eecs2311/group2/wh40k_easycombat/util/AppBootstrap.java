package eecs2311.group2.wh40k_easycombat.util;

import eecs2311.group2.wh40k_easycombat.db.Database;
import eecs2311.group2.wh40k_easycombat.service.vm.VMService;

import java.io.IOException;
import java.sql.SQLException;
import java.util.function.BiConsumer;

public final class AppBootstrap {
    private AppBootstrap() {
    }

    public static void initializeApplication() throws IOException, SQLException {
        initializeApplication(null);
    }

    public static void initializeApplication(BiConsumer<String, Double> progressReporter) throws IOException, SQLException {
        report(progressReporter, "Preparing bundled resources...", 0.15);
        BundledResourceManager.ensureBundledResourcesAvailable();

        report(progressReporter, "Opening application database...", 0.35);
        Database.useApplicationDatabase();

        report(progressReporter, "Building database schema...", 0.55);
        Database.ensureSchema();

        report(progressReporter, "Loading built-in rules...", 0.7);
        VMService.loadFolder(AppPaths.getBundledDslDirectory());

        report(progressReporter, "Importing starter data. First launch may take a little while...", -1.0);
        CsvToSqliteImporter.importDefaultCsvSeedLike(false);

        report(progressReporter, "Startup complete.", 1.0);
    }

    private static void report(BiConsumer<String, Double> progressReporter, String message, double progress) {
        if (progressReporter != null) {
            progressReporter.accept(message, progress);
        }
    }
}
