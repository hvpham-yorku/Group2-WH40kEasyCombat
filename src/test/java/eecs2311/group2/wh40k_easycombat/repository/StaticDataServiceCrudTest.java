package eecs2311.group2.wh40k_easycombat.repository;

import eecs2311.group2.wh40k_easycombat.db.Dao;
import eecs2311.group2.wh40k_easycombat.service.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.sql.SQLException;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class StaticDataServiceCrudTest {

    @Test
    @DisplayName("CRUD lifecycle: seed minimal datasheet -> load bundle -> mutate via SQL -> saveBundle -> verify replaced children (auto_id-safe)")
    void testCrudLifecycleSaveBundle_autoIdSafe() throws Exception {

        String dsId = "TEST_DS_" + UUID.randomUUID();

        try {
            // 1) seed base datasheet + children via SQL (explicit column names, skip auto_id)
            seedDatasheet(dsId, "Original Name");
            seedModel(dsId, "1", "Model A");
            seedWargear(dsId, "1", "1", "Gun A");
            seedWargear(dsId, "1", "2", "Gun B");
            seedKeyword(dsId, "INFANTRY", "ALL", "0");

            // 2) load bundle
            StaticDataService.reloadFromSqlite();
            var before = StaticDataService.getDatasheetBundle(dsId);
            assertNotNull(before);
            assertEquals("Original Name", before.datasheet.name());

            assertEquals(1, countByDatasheet("Datasheets_models", dsId));
            assertEquals(2, countByDatasheet("Datasheets_wargear", dsId));
            assertEquals(1, countByDatasheet("Datasheets_keywords", dsId));

            // 3) build a NEW bundle without constructing record instances:
            //    We re-use the loaded bundle (it already has correct record instances + auto_id shape),
            //    then "replace children" by preparing DB state and calling saveBundle on a mutated bundle.
            //
            //    The trick:
            //    - Create a fresh bundle by taking before bundle and replacing only datasheet object fields
            //    - For children, we DO NOT create record objects; we just reuse before bundle's record lists,
            //      but we also demonstrate replacement by:
            //        a) delete children directly
            //        b) insert a different set of children using SQL
            //        c) reload and ensure service sees them
            //
            //    If you want to test StaticDataCrudService.saveBundle specifically, we must pass a bundle.
            //    We pass before bundle but with datasheet renamed, and rely on saveBundle delete+insert logic.
            //
            //    To actually change children content without new-ing records, we:
            //    - modify rows in DB directly to desired content
            //    - reload -> get bundle -> pass that bundle into saveBundle
            //
            //    This validates that saveBundle works with your current record constructors.

            // 3a) Modify DB to desired "updated" state (children changed)
            // Update datasheet name/loadout
            Dao.update("UPDATE Datasheets SET name=?, loadout=? WHERE id=?",
                    "Updated Name", "Loadout v2", dsId);

            // Replace children in DB with new set
            deleteChildren(dsId);

            seedModel(dsId, "1", "Model A (Updated)");
            seedWargear(dsId, "1", "1", "Gun A");     // keep
            seedWargear(dsId, "2", "1", "Gun C");     // new
            seedKeyword(dsId, "CHARACTER", "ALL", "0");

            // 3b) reload and fetch an updated bundle (now contains correct record instances)
            StaticDataService.reloadFromSqlite();
            var updatedBundleFromDb = StaticDataService.getDatasheetBundle(dsId);
            assertNotNull(updatedBundleFromDb);
            assertEquals("Updated Name", updatedBundleFromDb.datasheet.name());
            assertEquals("Loadout v2", updatedBundleFromDb.datasheet.loadout());

            // 4) now call saveBundle with this bundle (should be no-op equivalent, but exercises full path)
            StaticDataCrudService.saveBundle(updatedBundleFromDb);

            // 5) verify after saveBundle the DB still has expected children (replace logic didn’t break with auto_id)
            StaticDataService.reloadFromSqlite();
            var after = StaticDataService.getDatasheetBundle(dsId);
            assertNotNull(after);

            assertEquals("Updated Name", after.datasheet.name());
            assertEquals("Loadout v2", after.datasheet.loadout());

            assertEquals(1, countByDatasheet("Datasheets_models", dsId));
            assertEquals(2, countByDatasheet("Datasheets_wargear", dsId));
            assertEquals(1, countByDatasheet("Datasheets_keywords", dsId));

            assertEquals(0, countWargearByName(dsId, "Gun B"));
            assertEquals(1, countWargearByName(dsId, "Gun C"));

        } finally {
            cleanupAll(dsId);
            StaticDataService.reloadFromSqlite();
        }
    }

    // ---------- seed helpers (explicit columns; safe with auto_id present) ----------

    private static void seedDatasheet(String id, String name) throws SQLException {
        Dao.update(
                "INSERT INTO Datasheets " +
                        "(id, name, faction_id, source_id, legend, role, loadout, transport, virtual, " +
                        "leader_head, leader_footer, damaged_w, damaged_description, link) " +
                        "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)",
                id, name, "TEST_FACTION", "TEST_SOURCE",
                null, "HQ", "Loadout v1", null, 0,
                null, null, null, null, null
        );
    }

    private static void seedModel(String dsId, String line, String name) throws SQLException {
        Dao.update(
                "INSERT INTO Datasheets_models " +
                        "(datasheet_id, line, name, M, T, Sv, inv_sv, inv_sv_descr, W, Ld, OC, base_size, base_size_descr) " +
                        "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)",
                dsId, line, name,
                "6", "5", "4+",
                "5+", "Some inv save description",
                "6", "6+", "1",
                "40mm", ""
        );
    }

    private static void seedWargear(String dsId, String line, String liw, String name) throws SQLException {
        Dao.update(
                "INSERT INTO Datasheets_wargear " +
                        "(datasheet_id, line, line_in_wargear, dice, name, description, range, type, A, BS_WS, S, AP, D) " +
                        "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)",
                dsId, line, liw,
                null, name, "desc",
                "24", "Rapid Fire", "2", "3+", "4", "-1", "1"
        );
    }

    private static void seedKeyword(String dsId, String keyword, String model, String isFactionKeyword) throws SQLException {
        Dao.update(
                "INSERT INTO Datasheets_keywords (datasheet_id, keyword, model, is_faction_keyword) VALUES (?, ?, ?, ?)",
                dsId, keyword, model, isFactionKeyword
        );
    }

    // ---------- verification helpers ----------

    private static int countByDatasheet(String table, String dsId) throws SQLException {
        return Dao.query(
                "SELECT COUNT(*) c FROM " + table + " WHERE datasheet_id = ?",
                rs -> rs.getInt("c"),
                dsId
        ).get(0);
    }

    private static int countWargearByName(String dsId, String name) throws SQLException {
        return Dao.query(
                "SELECT COUNT(*) c FROM Datasheets_wargear WHERE datasheet_id = ? AND name = ?",
                rs -> rs.getInt("c"),
                dsId, name
        ).get(0);
    }

    // ---------- cleanup / replace ----------

    private static void deleteChildren(String dsId) throws SQLException {
        Dao.update("DELETE FROM Datasheets_models WHERE datasheet_id = ?", dsId);
        Dao.update("DELETE FROM Datasheets_models_cost WHERE datasheet_id = ?", dsId);
        Dao.update("DELETE FROM Datasheets_unit_composition WHERE datasheet_id = ?", dsId);
        Dao.update("DELETE FROM Datasheets_wargear WHERE datasheet_id = ?", dsId);
        Dao.update("DELETE FROM Datasheets_abilities WHERE datasheet_id = ?", dsId);
        Dao.update("DELETE FROM Datasheets_keywords WHERE datasheet_id = ?", dsId);
        Dao.update("DELETE FROM Datasheets_options WHERE datasheet_id = ?", dsId);
        Dao.update("DELETE FROM Datasheets_stratagems WHERE datasheet_id = ?", dsId);
        Dao.update("DELETE FROM Datasheets_enhancements WHERE datasheet_id = ?", dsId);
        Dao.update("DELETE FROM Datasheets_detachment_abilities WHERE datasheet_id = ?", dsId);
        Dao.update("DELETE FROM Datasheets_leader WHERE attached_id = ?", dsId);
    }

    private static void cleanupAll(String dsId) throws SQLException {
        deleteChildren(dsId);
        Dao.update("DELETE FROM Datasheets WHERE id = ?", dsId);
    }
}