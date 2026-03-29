package eecs2311.group2.wh40k_easycombat.util;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public final class AppPaths {
    public static final String HOME_OVERRIDE_PROPERTY = "wh40k.easycombat.home";

    private static final String APP_DIRECTORY_NAME = "WH40KEasyCombat";

    private AppPaths() {
    }

    public static Path getApplicationHome() {
        String override = System.getProperty(HOME_OVERRIDE_PROPERTY);
        if (override != null && !override.isBlank()) {
            return Path.of(override.trim()).toAbsolutePath().normalize();
        }

        String osName = System.getProperty("os.name", "").toLowerCase(Locale.ROOT);
        if (osName.contains("win")) {
            String localAppData = System.getenv("LOCALAPPDATA");
            if (localAppData != null && !localAppData.isBlank()) {
                return Path.of(localAppData, APP_DIRECTORY_NAME).toAbsolutePath().normalize();
            }

            return Path.of(System.getProperty("user.home"), "AppData", "Local", APP_DIRECTORY_NAME)
                    .toAbsolutePath()
                    .normalize();
        }

        return Path.of(System.getProperty("user.home"), ".wh40k-easycombat")
                .toAbsolutePath()
                .normalize();
    }

    public static Path getBundledResourcesDirectory() {
        return getApplicationHome().resolve("bundled");
    }

    public static Path getBundledCsvDirectory() {
        return getBundledResourcesDirectory().resolve("csv");
    }

    public static Path getBundledDslDirectory() {
        return getBundledResourcesDirectory().resolve("dsl");
    }

    public static Path getDataDirectory() {
        return getApplicationHome().resolve("data");
    }

    public static Path getUserCsvDirectory() {
        return getDataDirectory().resolve("csv");
    }

    public static Path getUserDslDirectory() {
        return getDataDirectory().resolve("dsl");
    }

    public static Path getDatabasePath() {
        return getDataDirectory().resolve("app.db");
    }

    public static Path getDatabaseWalPath() {
        return getDataDirectory().resolve("app.db-wal");
    }

    public static Path getDatabaseShmPath() {
        return getDataDirectory().resolve("app.db-shm");
    }

    public static void ensureRuntimeDirectories() throws IOException {
        Files.createDirectories(getBundledCsvDirectory());
        Files.createDirectories(getBundledDslDirectory());
        Files.createDirectories(getUserCsvDirectory());
        Files.createDirectories(getUserDslDirectory());
    }

    public static Path getSeedCsvDirectory() throws IOException {
        Path bundledCsvDirectory = getBundledCsvDirectory();
        Path userCsvDirectory = getUserCsvDirectory();

        if (containsAllCsvFiles(userCsvDirectory, bundledCsvDirectory)) {
            return userCsvDirectory;
        }

        return bundledCsvDirectory;
    }

    private static boolean containsAllCsvFiles(Path candidateDirectory, Path referenceDirectory) throws IOException {
        if (!Files.isDirectory(candidateDirectory) || !Files.isDirectory(referenceDirectory)) {
            return false;
        }

        Set<String> referenceFiles = listCsvFileNames(referenceDirectory);
        if (referenceFiles.isEmpty()) {
            return false;
        }

        Set<String> candidateFiles = listCsvFileNames(candidateDirectory);
        return candidateFiles.containsAll(referenceFiles);
    }

    private static Set<String> listCsvFileNames(Path directory) throws IOException {
        try (Stream<Path> paths = Files.list(directory)) {
            return paths
                    .filter(Files::isRegularFile)
                    .map(Path::getFileName)
                    .map(Path::toString)
                    .filter(name -> name.toLowerCase(Locale.ROOT).endsWith(".csv"))
                    .collect(Collectors.toSet());
        }
    }
}
