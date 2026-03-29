package eecs2311.group2.wh40k_easycombat.util;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class BundledResourceManagerTest {

    @Test
    void ensureBundledResourcesAvailableCopiesBundledCsvAndDslFiles(@TempDir Path tempDir) throws Exception {
        withOverriddenAppHome(tempDir.resolve("app-home"), () -> {
            BundledResourceManager.ensureBundledResourcesAvailable();

            assertTrue(Files.exists(AppPaths.getBundledCsvDirectory().resolve("Factions.csv")));
            assertTrue(Files.exists(AppPaths.getBundledDslDirectory().resolve("dsl_basic_roll.rule")));
        });
    }

    @Test
    void getSeedCsvDirectoryFallsBackToBundledCsvWhenUserOverrideIsIncomplete(@TempDir Path tempDir) throws Exception {
        withOverriddenAppHome(tempDir.resolve("app-home"), () -> {
            BundledResourceManager.ensureBundledResourcesAvailable();

            Path bundledCsvDirectory = AppPaths.getBundledCsvDirectory();
            Path userCsvDirectory = AppPaths.getUserCsvDirectory();
            Files.createDirectories(userCsvDirectory);

            try (Stream<Path> paths = Files.list(bundledCsvDirectory)) {
                Path firstCsv = paths.filter(Files::isRegularFile).findFirst().orElseThrow();
                Files.copy(firstCsv, userCsvDirectory.resolve(firstCsv.getFileName()));
            }

            assertEquals(bundledCsvDirectory, AppPaths.getSeedCsvDirectory());
        });
    }

    @Test
    void getSeedCsvDirectoryPrefersUserOverrideWhenAllBundledCsvFilesArePresent(@TempDir Path tempDir) throws Exception {
        withOverriddenAppHome(tempDir.resolve("app-home"), () -> {
            BundledResourceManager.ensureBundledResourcesAvailable();

            Path bundledCsvDirectory = AppPaths.getBundledCsvDirectory();
            Path userCsvDirectory = AppPaths.getUserCsvDirectory();
            Files.createDirectories(userCsvDirectory);

            try (Stream<Path> paths = Files.list(bundledCsvDirectory)) {
                for (Path csv : paths.filter(Files::isRegularFile).toList()) {
                    Files.copy(csv, userCsvDirectory.resolve(csv.getFileName()));
                }
            }

            assertEquals(userCsvDirectory, AppPaths.getSeedCsvDirectory());
        });
    }

    private static void withOverriddenAppHome(Path appHome, ThrowingRunnable runnable) throws Exception {
        String previous = System.getProperty(AppPaths.HOME_OVERRIDE_PROPERTY);
        System.setProperty(AppPaths.HOME_OVERRIDE_PROPERTY, appHome.toString());
        try {
            runnable.run();
        } finally {
            if (previous == null) {
                System.clearProperty(AppPaths.HOME_OVERRIDE_PROPERTY);
            } else {
                System.setProperty(AppPaths.HOME_OVERRIDE_PROPERTY, previous);
            }
        }
    }

    @FunctionalInterface
    private interface ThrowingRunnable {
        void run() throws Exception;
    }
}
