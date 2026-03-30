package eecs2311.group2.wh40k_easycombat.service;

import eecs2311.group2.wh40k_easycombat.db.Dao;
import eecs2311.group2.wh40k_easycombat.support.TestDatabaseSupport;
import eecs2311.group2.wh40k_easycombat.viewmodel.ArmyUnitVM;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.sql.SQLException;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class ArmyWh40kAppImportServiceTest {

    @BeforeAll
    static void initTestDatabase() throws Exception {
        TestDatabaseSupport.useFreshStaticAndArmyTestDatabase();
    }

    @AfterEach
    void clearTables() throws SQLException {
        TestDatabaseSupport.clearAllTables();
    }

    @Test
    @DisplayName("importArmyText imports known units, skips unknown units and unknown wargear, and uses the first line as the army name")
    void importArmyTextImportsKnownUnitsAndSkipsUnknownEntries() throws Exception {
        seedIntercessorDatasheet();
        StaticDataService.reloadFromSqlite();

        String rawText = """
                Test Army
                Space Marines
                Strike Force (1000 points)
                Incursion
                Gladius Task Force

                BATTLELINE

                Intercessor Squad (160 points)
                5x Intercessor
                5x Bolt rifle
                1x Missing gun
                Enhancement: Honour Vehement
                Warlord

                OTHER

                Unknown Squad (50 points)
                1x Unknown Trooper
                """;

        ArmyWh40kAppImportService.ImportResult result = ArmyWh40kAppImportService.importArmyText(
                rawText,
                Map.of("enh-1", new ArmyUnitVM.EnhancementEntry("enh-1", "Honour Vehement", 15))
        );

        assertEquals("Test Army", result.armyName());
        assertEquals("Space Marines", result.factionName());
        assertEquals("Gladius Task Force", result.detachmentName());
        assertEquals(1, result.units().size());
        assertEquals(List.of("Unknown Squad"), result.skippedUnits());
        assertTrue(result.skippedItems().stream().anyMatch(item -> item.contains("Missing gun")));

        ArmyUnitVM imported = result.units().getFirst();
        assertEquals("Intercessor Squad", imported.getUnitName());
        assertEquals(5, imported.modelCountProperty().get());
        assertEquals(5, imported.getWargears().stream()
                .filter(wargear -> "Bolt rifle".equals(wargear.getName()))
                .findFirst()
                .orElseThrow()
                .getCount());
        assertTrue(imported.warlordProperty().get());
        assertEquals("enh-1", imported.getEnhancementId());
    }

    @Test
    @DisplayName("importArmyText rejects input without a valid army header")
    void importArmyTextRejectsMissingArmyHeader() {
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> ArmyWh40kAppImportService.importArmyText("", Map.of())
        );

        assertTrue(exception.getMessage().contains("WH40K App export"));
    }

    @Test
    @DisplayName("importArmyText prefers datasheet cost tiers over weapon counts when inferring model count")
    void importArmyTextUsesCostTierToInferModelCount() throws Exception {
        seedPistolSquadDatasheet();
        StaticDataService.reloadFromSqlite();

        String rawText = """
                Pistol Army
                Space Marines
                Strike Force (1000 points)
                Incursion
                Gladius Task Force

                OTHER

                Pistol Squad (100 points)
                10x Twin pistol
                """;

        ArmyWh40kAppImportService.ImportResult result = ArmyWh40kAppImportService.importArmyText(rawText, Map.of());

        assertEquals(1, result.units().size());

        ArmyUnitVM imported = result.units().getFirst();
        assertEquals("Pistol Squad", imported.getUnitName());
        assertEquals(5, imported.modelCountProperty().get());
        assertEquals(
                10,
                imported.getWargears().stream()
                        .filter(wargear -> "Twin pistol".equals(wargear.getName()))
                        .findFirst()
                        .orElseThrow()
                        .getCount()
        );
    }

    private void seedIntercessorDatasheet() throws SQLException {
        Dao.update(
                "INSERT INTO Datasheets (id, name, faction_id, source_id, legend, role, loadout, transport, virtual, leader_head, leader_footer, damaged_w, damaged_description, link) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)",
                "ds-intercessor",
                "Intercessor Squad",
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
                "ds-intercessor",
                "1",
                "Intercessor",
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
                "ds-intercessor",
                "1",
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
        Dao.update(
                "INSERT INTO Datasheets_models_cost (datasheet_id, line, description, cost) VALUES (?, ?, ?, ?)",
                "ds-intercessor",
                "1",
                "5 models",
                "160"
        );
        Dao.update(
                "INSERT INTO Datasheets_enhancements (datasheet_id, enhancement_id) VALUES (?, ?)",
                "ds-intercessor",
                "enh-1"
        );
    }

    private void seedPistolSquadDatasheet() throws SQLException {
        Dao.update(
                "INSERT INTO Datasheets (id, name, faction_id, source_id, legend, role, loadout, transport, virtual, leader_head, leader_footer, damaged_w, damaged_description, link) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)",
                "ds-pistol",
                "Pistol Squad",
                "space-marines",
                "source-core",
                "",
                "Other",
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
                "ds-pistol",
                "1",
                "Pistol Marine",
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
                "ds-pistol",
                "1",
                "1",
                "",
                "Twin pistol",
                "",
                "12\"",
                "Ranged",
                "2",
                "3+",
                "4",
                "0",
                "1"
        );
        Dao.update(
                "INSERT INTO Datasheets_models_cost (datasheet_id, line, description, cost) VALUES (?, ?, ?, ?)",
                "ds-pistol",
                "1",
                "5 models",
                "100"
        );
        Dao.update(
                "INSERT INTO Datasheets_models_cost (datasheet_id, line, description, cost) VALUES (?, ?, ?, ?)",
                "ds-pistol",
                "2",
                "10 models",
                "200"
        );
    }
}
