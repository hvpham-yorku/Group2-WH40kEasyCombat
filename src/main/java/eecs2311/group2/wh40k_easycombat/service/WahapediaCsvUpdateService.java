package eecs2311.group2.wh40k_easycombat.service;

import eecs2311.group2.wh40k_easycombat.db.Database;
import eecs2311.group2.wh40k_easycombat.util.AppPaths;
import eecs2311.group2.wh40k_easycombat.util.CsvToSqliteImporter;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class WahapediaCsvUpdateService {
    private static final Path LOCAL_CSV_FOLDER = AppPaths.getUserCsvDirectory();
    private static final Path DATABASE_FILE = AppPaths.getDatabasePath();
    private static final Path DATABASE_WAL_FILE = AppPaths.getDatabaseWalPath();
    private static final Path DATABASE_SHM_FILE = AppPaths.getDatabaseShmPath();
    private static final List<String> REQUIRED_FILE_NAMES = List.of(
            "Factions.csv",
            "Source.csv",
            "Datasheets.csv",
            "Datasheets_abilities.csv",
            "Datasheets_keywords.csv",
            "Datasheets_models.csv",
            "Datasheets_options.csv",
            "Datasheets_wargear.csv",
            "Datasheets_unit_composition.csv",
            "Datasheets_models_cost.csv",
            "Datasheets_stratagems.csv",
            "Datasheets_enhancements.csv",
            "Datasheets_detachment_abilities.csv",
            "Datasheets_leader.csv",
            "Stratagems.csv",
            "Abilities.csv",
            "Enhancements.csv",
            "Detachment_abilities.csv",
            "Detachments.csv",
            "Last_update.csv"
    );

    public ValidationResult validateSelection(List<Path> selectedFiles) {
        if (selectedFiles == null || selectedFiles.isEmpty()) {
            return ValidationResult.failure("Please select the Wahapedia CSV files to import.");
        }

        Map<String, List<Path>> filesByName = new LinkedHashMap<>();
        for (Path path : selectedFiles) {
            if (path == null || path.getFileName() == null) {
                continue;
            }
            String fileName = path.getFileName().toString();
            filesByName.computeIfAbsent(fileName, ignored -> new ArrayList<>()).add(path);
        }

        List<String> duplicates = new ArrayList<>();
        for (Map.Entry<String, List<Path>> entry : filesByName.entrySet()) {
            if (entry.getValue().size() > 1) {
                duplicates.add(entry.getKey());
            }
        }

        Set<String> selectedNames = new LinkedHashSet<>(filesByName.keySet());
        List<String> missing = REQUIRED_FILE_NAMES.stream()
                .filter(required -> !selectedNames.contains(required))
                .toList();
        List<String> unexpected = selectedNames.stream()
                .filter(name -> !REQUIRED_FILE_NAMES.contains(name))
                .sorted(String::compareToIgnoreCase)
                .toList();

        if (!duplicates.isEmpty() || !missing.isEmpty() || !unexpected.isEmpty()
                || selectedNames.size() != REQUIRED_FILE_NAMES.size()) {
            StringBuilder message = new StringBuilder();
            message.append("The selected CSV files do not match the Wahapedia export specification.").append('\n');
            message.append("Expected file count: ").append(REQUIRED_FILE_NAMES.size()).append('\n');
            message.append("Selected unique file count: ").append(selectedNames.size()).append('\n');

            if (!duplicates.isEmpty()) {
                message.append('\n').append("Duplicate file names:").append('\n');
                for (String duplicate : duplicates) {
                    message.append("- ").append(duplicate).append('\n');
                }
            }

            if (!missing.isEmpty()) {
                message.append('\n').append("Missing required files:").append('\n');
                for (String miss : missing) {
                    message.append("- ").append(miss).append('\n');
                }
            }

            if (!unexpected.isEmpty()) {
                message.append('\n').append("Unexpected files:").append('\n');
                for (String extra : unexpected) {
                    message.append("- ").append(extra).append('\n');
                }
            }

            return ValidationResult.failure(message.toString().trim());
        }

        List<Path> orderedFiles = REQUIRED_FILE_NAMES.stream()
                .map(name -> filesByName.get(name).get(0))
                .toList();
        return ValidationResult.success(orderedFiles);
    }

    public void importSelectedFiles(List<Path> selectedFiles) throws IOException, SQLException {
        ValidationResult validation = validateSelection(selectedFiles);
        if (!validation.valid()) {
            throw new IllegalArgumentException(validation.message());
        }

        Path stagingFolder = Files.createTempDirectory("wahapedia-csv-update-");
        try {
            for (Path selectedFile : validation.orderedFiles()) {
                Files.copy(
                        selectedFile,
                        stagingFolder.resolve(selectedFile.getFileName().toString()),
                        StandardCopyOption.REPLACE_EXISTING
                );
            }

            rebuildDatabaseFromSelectedCsv(stagingFolder);
            replaceLocalCsvFiles(validation.orderedFiles());
        } finally {
            deleteRecursively(stagingFolder);
        }
    }

    public List<String> requiredFileNames() {
        return REQUIRED_FILE_NAMES;
    }

    private void replaceLocalCsvFiles(List<Path> selectedFiles) throws IOException {
        if (selectedFiles == null || selectedFiles.isEmpty()) {
            return;
        }

        Path parent = LOCAL_CSV_FOLDER.getParent();
        if (parent != null) {
            Files.createDirectories(parent);
        }

        Path stagingFolder = Files.createTempDirectory(parent == null ? Path.of(".") : parent, "csv-override-");
        boolean moved = false;
        try {
            for (Path selectedFile : selectedFiles) {
                if (selectedFile == null || selectedFile.getFileName() == null) {
                    continue;
                }

                Files.copy(
                        selectedFile,
                        stagingFolder.resolve(selectedFile.getFileName().toString()),
                        StandardCopyOption.REPLACE_EXISTING
                );
            }

            deleteRecursively(LOCAL_CSV_FOLDER);
            try {
                Files.move(stagingFolder, LOCAL_CSV_FOLDER, StandardCopyOption.ATOMIC_MOVE);
            } catch (IOException atomicMoveFailure) {
                Files.move(stagingFolder, LOCAL_CSV_FOLDER, StandardCopyOption.REPLACE_EXISTING);
            }
            moved = true;
        } finally {
            if (!moved) {
                deleteRecursively(stagingFolder);
            }
        }
    }

    private void rebuildDatabaseFromSelectedCsv(Path stagingFolder) throws IOException, SQLException {
        Database.useApplicationDatabase();
        deleteDatabaseFiles();
        Database.ensureSchema();
        CsvToSqliteImporter.importCsvFolderSeedLike(stagingFolder.toString(), true);
        StaticDataService.reloadFromSqlite();
    }

    private void deleteDatabaseFiles() throws IOException {
        Files.deleteIfExists(DATABASE_WAL_FILE);
        Files.deleteIfExists(DATABASE_SHM_FILE);
        Files.deleteIfExists(DATABASE_FILE);
    }

    private void deleteRecursively(Path folder) throws IOException {
        if (folder == null || !Files.exists(folder)) {
            return;
        }

        try (var paths = Files.walk(folder)) {
            paths.sorted(java.util.Comparator.reverseOrder())
                    .forEach(path -> {
                        try {
                            Files.deleteIfExists(path);
                        } catch (IOException ignored) {
                        }
                    });
        }
    }

    public record ValidationResult(boolean valid, String message, List<Path> orderedFiles) {
        public static ValidationResult success(List<Path> orderedFiles) {
            return new ValidationResult(true, "", orderedFiles == null ? List.of() : List.copyOf(orderedFiles));
        }

        public static ValidationResult failure(String message) {
            return new ValidationResult(false, message == null ? "Invalid CSV selection." : message.trim(), List.of());
        }
    }
}
