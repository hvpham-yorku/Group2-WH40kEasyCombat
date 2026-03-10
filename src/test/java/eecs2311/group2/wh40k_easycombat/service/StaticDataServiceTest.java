package eecs2311.group2.wh40k_easycombat.service;

import eecs2311.group2.wh40k_easycombat.db.Dao;
import eecs2311.group2.wh40k_easycombat.model.Datasheets_models;
import eecs2311.group2.wh40k_easycombat.model.Datasheets_wargear;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.sql.SQLException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class StaticDataServiceTest {

    @Test
    @DisplayName("Should load all static data and return a complete non-null bundle for an existing datasheet")
    void testLoadAndGetBundle() throws SQLException {

        StaticDataService.reloadFromSqlite();

        String existingId = Dao.query(
                "SELECT id FROM Datasheets LIMIT 1",
                rs -> rs.getString("id")
        ).stream().findFirst().orElse(null);

        assertNotNull(existingId, "DB must contain at least 1 datasheet for this test");

        StaticDataService.DatasheetBundle b = StaticDataService.getDatasheetBundle(existingId);

        assertNotNull(b, "Bundle should not be null");
        assertNotNull(b.datasheet, "Bundle.datasheet should not be null");

        // All lists should be non-null (may be empty)
        assertNotNull(b.models);
        assertNotNull(b.wargear);
        assertNotNull(b.abilities);
        assertNotNull(b.compositions);
        assertNotNull(b.costs);
        assertNotNull(b.keywords);

        assertNotNull(b.options);
        assertNotNull(b.leaders);
        assertNotNull(b.stratagems);
        assertNotNull(b.enhancements);
        assertNotNull(b.detachmentAbilities);

        // Basic ordering sanity checks (service sorts by line, and wargear by line + line_in_wargear)
        assertTrue(isModelsSortedByLine(b.models), "Models should be sorted by line asc (numeric)");
        assertTrue(isWargearSorted(b.wargear), "Wargear should be sorted by (line, line_in_wargear) asc (numeric)");
    }

    @Test
    @DisplayName("Should return null for non-existing datasheet id")
    void testInvalidId() throws SQLException {
        StaticDataService.reloadFromSqlite();
        assertNull(StaticDataService.getDatasheetBundle("NON_EXISTING_ID"));
    }

    // ---- helpers ----
    private static boolean isModelsSortedByLine(List<Datasheets_models> list) {
        int prev = Integer.MIN_VALUE;
        for (var x : list) {
            int cur = safeInt(x.line());
            if (cur < prev) return false;
            prev = cur;
        }
        return true;
    }

    private static boolean isWargearSorted(List<Datasheets_wargear> list) {
        int prevLine = Integer.MIN_VALUE;
        int prevLiw = Integer.MIN_VALUE;

        for (var x : list) {
            int line = safeInt(x.line());
            int liw = safeInt(x.line_in_wargear());

            if (line < prevLine) return false;
            if (line == prevLine && liw < prevLiw) return false;

            prevLine = line;
            prevLiw = liw;
        }
        return true;
    }

    private static int safeInt(String s) {
        if (s == null) return Integer.MAX_VALUE;
        try { return Integer.parseInt(s.trim()); }
        catch (Exception e) { return Integer.MAX_VALUE; }
    }
}