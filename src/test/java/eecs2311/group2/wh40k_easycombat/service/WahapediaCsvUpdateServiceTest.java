package eecs2311.group2.wh40k_easycombat.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class WahapediaCsvUpdateServiceTest {

    private final WahapediaCsvUpdateService service = new WahapediaCsvUpdateService();

    @Test
    @DisplayName("requiredFileNames returns the expected Wahapedia export file list")
    void requiredFileNamesReturnsExpectedList() {
        List<String> required = service.requiredFileNames();

        assertEquals(20, required.size());
        assertTrue(required.contains("Factions.csv"));
        assertTrue(required.contains("Datasheets.csv"));
        assertTrue(required.contains("Last_update.csv"));
    }

    @Test
    @DisplayName("validateSelection rejects duplicate, missing, and unexpected file names")
    void validateSelectionRejectsDuplicateMissingAndUnexpectedFiles(@TempDir Path tempDir) throws Exception {
        Path folderOne = Files.createDirectory(tempDir.resolve("one"));
        Path folderTwo = Files.createDirectory(tempDir.resolve("two"));

        Path duplicateA = Files.writeString(folderOne.resolve("Factions.csv"), "id|name\n");
        Path duplicateB = Files.writeString(folderTwo.resolve("Factions.csv"), "id|name\n");
        Path unexpected = Files.writeString(folderOne.resolve("Unexpected.csv"), "x|y\n");

        WahapediaCsvUpdateService.ValidationResult result = service.validateSelection(
                List.of(duplicateA, duplicateB, unexpected)
        );

        assertFalse(result.valid());
        assertTrue(result.message().contains("Duplicate file names"));
        assertTrue(result.message().contains("Missing required files"));
        assertTrue(result.message().contains("Unexpected files"));
    }

    @Test
    @DisplayName("importSelectedFiles throws immediately when the CSV selection is invalid")
    void importSelectedFilesThrowsWhenSelectionIsInvalid(@TempDir Path tempDir) throws Exception {
        Path factions = Files.writeString(tempDir.resolve("Factions.csv"), "id|name\n");

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> service.importSelectedFiles(List.of(factions))
        );

        assertTrue(exception.getMessage().contains("Wahapedia export specification"));
    }
}
