package eecs2311.group2.wh40k_easycombat.repository;

import eecs2311.group2.wh40k_easycombat.db.Dao;
import eecs2311.group2.wh40k_easycombat.model.*;
import eecs2311.group2.wh40k_easycombat.service.StaticDataCrudService;
import eecs2311.group2.wh40k_easycombat.service.StaticDataService;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Constructor;
import java.sql.SQLException;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class StaticDataServiceCrudTest {

    @Test
    @DisplayName("CRUD lifecycle via StaticDataCrudService.saveBundle (replace strategy + reload)")
    void testCrudLifecycleSaveBundle() throws Exception {

        String id = "TEST_DS_" + UUID.randomUUID();

        try {
            // -------------------------
            // CREATE (seed DB rows)
            // -------------------------
            insertDatasheet(id, "Original Name");
            insertModel(id, "1", "Model A");
            insertWargear(id, "1", "1", "Gun A");
            insertWargear(id, "1", "2", "Gun B");
            insertKeyword(id, "INFANTRY", "ALL");

            // -------------------------
            // READ (StaticDataService)
            // -------------------------
            StaticDataService.reloadFromSqlite();

            var before = StaticDataService.getDatasheetBundle(id);
            assertNotNull(before);
            assertEquals("Original Name", before.datasheet.name());
            assertEquals(1, count("Datasheets_models", "datasheet_id", id));
            assertEquals(2, count("Datasheets_wargear", "datasheet_id", id));
            assertEquals(1, count("Datasheets_keywords", "datasheet_id", id));

            // -------------------------
            // UPDATE (saveBundle)
            // Replace strategy:
            // - Update datasheet fields
            // - Replace children with new sets
            // -------------------------
            Datasheets updatedDatasheet = new Datasheets(
                    id,
                    "Updated Name",
                    "TEST_FACTION",
                    "TEST_SOURCE",
                    null,
                    "HQ",
                    "Loadout v2",
                    null,
                    false,
                    null,
                    null,
                    null,
                    null,
                    null
            );

            List<Datasheets_models> newModels = List.of(
                    new Datasheets_models(id, "1", "Model A (Updated)",
                            "6", "4", "3+", null, null, "2", "6+", "1", null, null)
            );

            List<Datasheets_wargear> newWargear = List.of(
                    new Datasheets_wargear(id, "1", "1",
                            null, "Gun A", "desc", "24", "Rapid Fire", "2", "3+", "4", "-1", "1"),
                    new Datasheets_wargear(id, "2", "1",
                            null, "Gun C", "desc2", "18", "Heavy", "D3", "4+", "6", "-3", "3")
            );

            List<Datasheets_keywords> newKeywords = List.of(
                    new Datasheets_keywords(id, "CHARACTER", "ALL", "0")
            );

            StaticDataService.DatasheetBundle newBundle = newBundleByReflection(
                    updatedDatasheet,
                    newModels,
                    newWargear,
                    List.of(), // abilities
                    List.of(), // compositions
                    List.of(), // costs
                    newKeywords,
                    List.of(), // options
                    List.of(), // leaders
                    List.of(), // stratagems
                    List.of(), // enhancements
                    List.of()  // detachmentAbilities
            );

            StaticDataCrudService.saveBundle(newBundle);

            // -------------------------
            // READ AFTER UPDATE
            // saveBundle ends with reloadFromSqlite(), so cache should already be fresh
            // -------------------------
            var after = StaticDataService.getDatasheetBundle(id);
            assertNotNull(after);
            assertEquals("Updated Name", after.datasheet.name());
            assertEquals("Loadout v2", after.datasheet.loadout());

            // children replaced
            assertEquals(1, count("Datasheets_models", "datasheet_id", id));
            assertEquals(2, count("Datasheets_wargear", "datasheet_id", id));
            assertEquals(1, count("Datasheets_keywords", "datasheet_id", id));

            // verify old Gun B removed, new Gun C exists
            assertEquals(0, countWargearByName(id, "Gun B"));
            assertEquals(1, countWargearByName(id, "Gun C"));

        } finally {
            // -------------------------
            // DELETE / Cleanup
            // -------------------------
            deleteAllForDatasheet(id);
            StaticDataService.reloadFromSqlite();
        }
    }

    // -------------------------
    // Reflection helper
    // -------------------------
    private static StaticDataService.DatasheetBundle newBundleByReflection(
            Datasheets datasheet,
            List<Datasheets_models> models,
            List<Datasheets_wargear> wargear,
            List<Datasheets_abilities> abilities,
            List<Datasheets_unit_composition> compositions,
            List<Datasheets_models_cost> costs,
            List<Datasheets_keywords> keywords,
            List<Datasheets_options> options,
            List<Datasheets_leader> leaders,
            List<Datasheets_stratagems> stratagems,
            List<Datasheets_enhancements> enhancements,
            List<Datasheets_detachment_abilities> detachmentAbilities
    ) throws Exception {

        Constructor<StaticDataService.DatasheetBundle> c =
                StaticDataService.DatasheetBundle.class.getDeclaredConstructor(
                        Datasheets.class,
                        List.class,
                        List.class,
                        List.class,
                        List.class,
                        List.class,
                        List.class,
                        List.class,
                        List.class,
                        List.class,
                        List.class,
                        List.class
                );

        c.setAccessible(true);

        return c.newInstance(
                datasheet,
                models,
                wargear,
                abilities,
                compositions,
                costs,
                keywords,
                options,
                leaders,
                stratagems,
                enhancements,
                detachmentAbilities
        );
    }

    // -------------------------
    // DB helpers
    // -------------------------
    private static void insertDatasheet(String id, String name) throws SQLException {
        Dao.update(
                "INSERT INTO Datasheets " +
                        "(id, name, faction_id, source_id, legend, role, loadout, transport, virtual, leader_head, leader_footer, damaged_w, damaged_description, link) " +
                        "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)",
                id,
                name,
                "TEST_FACTION",
                "TEST_SOURCE",
                null,
                "HQ",
                "Loadout v1",
                null,
                false,
                null,
                null,
                null,
                null,
                null
        );
    }

    private static void insertModel(String id, String line, String name) throws SQLException {
        Dao.update(
                "INSERT INTO Datasheets_models VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)",
                id, line, name,
                "6", "4", "3+",
                null, null,
                "2", "6+", "1",
                null, null
        );
    }

    private static void insertWargear(String id, String line, String liw, String name) throws SQLException {
        Dao.update(
                "INSERT INTO Datasheets_wargear VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)",
                id, line, liw,
                null, name, "desc",
                "24", "Rapid Fire", "2",
                "3+", "4", "-1", "1"
        );
    }

    private static void insertKeyword(String id, String keyword, String model) throws SQLException {
        Dao.update(
                "INSERT INTO Datasheets_keywords VALUES (?, ?, ?, ?)",
                id, keyword, model, "0"
        );
    }

    private static int count(String table, String col, String value) throws SQLException {
        return Dao.query(
                "SELECT COUNT(*) AS c FROM " + table + " WHERE " + col + " = ?",
                rs -> rs.getInt("c"),
                value
        ).stream().findFirst().orElse(0);
    }

    private static int countWargearByName(String datasheetId, String name) throws SQLException {
        return Dao.query(
                "SELECT COUNT(*) AS c FROM Datasheets_wargear WHERE datasheet_id = ? AND name = ?",
                rs -> rs.getInt("c"),
                datasheetId, name
        ).stream().findFirst().orElse(0);
    }

    private static void deleteAllForDatasheet(String id) throws SQLException {
        Dao.update("DELETE FROM Datasheets_models WHERE datasheet_id = ?", id);
        Dao.update("DELETE FROM Datasheets_models_cost WHERE datasheet_id = ?", id);
        Dao.update("DELETE FROM Datasheets_unit_composition WHERE datasheet_id = ?", id);
        Dao.update("DELETE FROM Datasheets_wargear WHERE datasheet_id = ?", id);
        Dao.update("DELETE FROM Datasheets_abilities WHERE datasheet_id = ?", id);
        Dao.update("DELETE FROM Datasheets_keywords WHERE datasheet_id = ?", id);
        Dao.update("DELETE FROM Datasheets_options WHERE datasheet_id = ?", id);
        Dao.update("DELETE FROM Datasheets_stratagems WHERE datasheet_id = ?", id);
        Dao.update("DELETE FROM Datasheets_enhancements WHERE datasheet_id = ?", id);
        Dao.update("DELETE FROM Datasheets_detachment_abilities WHERE datasheet_id = ?", id);
        Dao.update("DELETE FROM Datasheets_leader WHERE attached_id = ?", id);
        Dao.update("DELETE FROM Datasheets WHERE id = ?", id);
    }
}