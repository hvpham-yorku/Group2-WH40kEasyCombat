package eecs2311.group2.wh40k_easycombat.service;

import eecs2311.group2.wh40k_easycombat.db.Dao;
import eecs2311.group2.wh40k_easycombat.model.aggregate.DatasheetAggregate;
import eecs2311.group2.wh40k_easycombat.model.Datasheets_models;
import eecs2311.group2.wh40k_easycombat.model.Datasheets_wargear;
import eecs2311.group2.wh40k_easycombat.support.TestDatabaseSupport;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.sql.SQLException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class StaticDataServiceTest {

    @BeforeAll
    static void initTestDatabase() throws Exception {
        TestDatabaseSupport.useFreshStaticAndArmyTestDatabase();
    }

    @AfterEach
    void clearTables() throws SQLException {
        TestDatabaseSupport.clearAllTables();
    }

    @Test
    @DisplayName("Should load all static data and return a complete non-null bundle for an existing datasheet")
    void testLoadAndGetBundle() throws SQLException {
        seedDatasheetBundle();

        StaticDataService.reloadFromSqlite();

        String existingId = Dao.query(
                "SELECT id FROM Datasheets LIMIT 1",
                rs -> rs.getString("id")
        ).stream().findFirst().orElse(null);

        assertNotNull(existingId, "DB must contain at least 1 datasheet for this test");

        DatasheetAggregate b = StaticDataService.getDatasheetBundle(existingId);

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
        seedDatasheetBundle();
        StaticDataService.reloadFromSqlite();
        assertNull(StaticDataService.getDatasheetBundle("NON_EXISTING_ID"));
    }

    private void seedDatasheetBundle() throws SQLException {
        Dao.update(
                "INSERT INTO Datasheets (id, name, faction_id, source_id, legend, role, loadout, transport, virtual, leader_head, leader_footer, damaged_w, damaged_description, link) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)",
                "ds-static",
                "Static Test Squad",
                "space-marines",
                "source-core",
                "",
                "Battleline",
                "",
                "",
                0,
                "",
                "",
                "",
                "",
                ""
        );
        Dao.update(
                "INSERT INTO Datasheets_models (datasheet_id, line, name, M, T, Sv, inv_sv, inv_sv_descr, W, Ld, OC, base_size, base_size_descr) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)",
                "ds-static",
                "2",
                "Marine",
                "6\"",
                "4",
                "3+",
                "",
                "",
                "2",
                "6+",
                "1",
                "",
                ""
        );
        Dao.update(
                "INSERT INTO Datasheets_models (datasheet_id, line, name, M, T, Sv, inv_sv, inv_sv_descr, W, Ld, OC, base_size, base_size_descr) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)",
                "ds-static",
                "1",
                "Sergeant",
                "6\"",
                "4",
                "3+",
                "",
                "",
                "2",
                "6+",
                "1",
                "",
                ""
        );
        Dao.update(
                "INSERT INTO Datasheets_wargear (datasheet_id, line, line_in_wargear, dice, name, description, range, type, A, BS_WS, S, AP, D) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)",
                "ds-static",
                "2",
                "2",
                "",
                "Chainsword",
                "",
                "Melee",
                "Melee",
                "4",
                "3+",
                "4",
                "0",
                "1"
        );
        Dao.update(
                "INSERT INTO Datasheets_wargear (datasheet_id, line, line_in_wargear, dice, name, description, range, type, A, BS_WS, S, AP, D) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)",
                "ds-static",
                "2",
                "1",
                "",
                "Bolt rifle",
                "",
                "24\"",
                "Ranged",
                "2",
                "3+",
                "4",
                "-1",
                "1"
        );
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
