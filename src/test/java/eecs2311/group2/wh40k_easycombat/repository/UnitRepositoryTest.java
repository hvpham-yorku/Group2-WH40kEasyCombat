package eecs2311.group2.wh40k_easycombat.repository;

import eecs2311.group2.wh40k_easycombat.model.Factions;
import eecs2311.group2.wh40k_easycombat.model.Units;
import org.junit.jupiter.api.Test;

import java.sql.SQLException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class UnitRepositoryTest extends TestSetup {

    private Factions createSampleFaction() {
        return new Factions(-1, "Test Faction");
    }

    private Units createSampleUnit(int factionId) {
        return new Units(
                -1,
                factionId,
                "Test Unit",
                100,
                6,
                4,
                3,
                2,
                7,
                1,
                0,
                1,
                "5 models",
                List.of(1, 2),
                List.of(3),
                List.of(4),
                List.of(5),
                List.of(6)
        );
    }

    private Units createSecondSampleUnit(int factionId) {
        return new Units(
                -1,
                factionId,
                "Second Unit",
                150,
                7,
                5,
                2,
                3,
                6,
                2,
                5,
                2,
                "10 models",
                List.of(9),
                List.of(8, 7),
                List.of(),
                List.of(11, 12),
                List.of(13)
        );
    }

    @Test
    void returnCorrectUnitTest() throws SQLException {
        FactionRepository.addNewFaction(createSampleFaction());
        int factionId = FactionRepository.getAllFactions().get(0).id();

        Units unit = createSampleUnit(factionId);
        int returnedId = UnitRepository.addNewUnit(unit);

        Units result = UnitRepository.getUnitById(returnedId);

        assertNotNull(result);
        assertTrue(returnedId > 0);
        assertEquals("Test Unit", result.name());
        assertEquals(100, result.points());
        assertEquals(6, result.M());
        assertEquals(List.of(1, 2), result.coreAbilityIdList());
    }

    @Test
    void returnErrorWrongITest() throws SQLException {
        Units result = UnitRepository.getUnitById(0);
        assertNull(result);
    }

    @Test
    void updateUnit_shouldModifyExistingUnit() throws SQLException {
        FactionRepository.addNewFaction(createSampleFaction());
        int factionId = FactionRepository.getAllFactions().get(0).id();

        Units unit = createSampleUnit(factionId);
        UnitRepository.addNewUnit(unit);

        Units inserted = UnitRepository.getAllUnits().get(0);

        Units updated = new Units(
                inserted.id(),
                inserted.factionId(),
                "Updated Name",
                inserted.points(),
                inserted.M(),
                inserted.T(),
                inserted.SV(),
                inserted.W(),
                inserted.LD(),
                inserted.OC(),
                inserted.invulnerableSave(),
                inserted.category(),
                inserted.composition(),
                inserted.coreAbilityIdList(),
                inserted.otherAbilityIdList(),
                inserted.keywordIdList(),
                inserted.rangedWeaponIdList(),
                inserted.meleeWeaponIdList()
        );

        UnitRepository.updateUnit(updated);

        Units result = UnitRepository.getUnitById(inserted.id());
        assertNotNull(result);
        assertEquals("Updated Name", result.name());
    }

    @Test
    void deleteUnit_shouldRemoveUnit() throws SQLException {
        FactionRepository.addNewFaction(createSampleFaction());
        int factionId = FactionRepository.getAllFactions().get(0).id();

        Units unit = createSampleUnit(factionId);
        UnitRepository.addNewUnit(unit);

        Units inserted = UnitRepository.getAllUnits().get(0);

        UnitRepository.deleteUnit(inserted);

        assertTrue(UnitRepository.getAllUnits().isEmpty());
    }

    @Test
    void getAllUnits_shouldReturnMultipleUnits() throws SQLException {
        FactionRepository.addNewFaction(createSampleFaction());
        int factionId = FactionRepository.getAllFactions().get(0).id();

        int id1 = UnitRepository.addNewUnit(createSampleUnit(factionId));
        int id2 = UnitRepository.addNewUnit(createSecondSampleUnit(factionId));

        List<Units> all = UnitRepository.getAllUnits();

        assertEquals(2, all.size());

        Units u1 = UnitRepository.getUnitById(id1);
        Units u2 = UnitRepository.getUnitById(id2);

        assertNotNull(u1);
        assertNotNull(u2);

        assertEquals(List.of(1, 2), u1.coreAbilityIdList());
        assertEquals("Second Unit", u2.name());
        assertEquals(150, u2.points());
        assertEquals(List.of(11, 12), u2.rangedWeaponIdList());
        assertEquals(List.of(), u2.keywordIdList());
    }

    @Test
    void updateUnit_nonExistingId_shouldNotCreateNewRow() throws SQLException {
        FactionRepository.addNewFaction(createSampleFaction());
        int factionId = FactionRepository.getAllFactions().get(0).id();

        Units fake = new Units(
                999999,
                factionId,
                "Ghost Unit",
                999,
                1,
                1,
                1,
                1,
                1,
                1,
                0,
                1,
                "none",
                List.of(),
                List.of(),
                List.of(),
                List.of(),
                List.of()
        );

        UnitRepository.updateUnit(fake);

        assertTrue(UnitRepository.getAllUnits().isEmpty());
        assertNull(UnitRepository.getUnitById(999999));
    }

    @Test
    void addUnit_withoutValidFaction_shouldThrowException() {
        Units unit = createSampleUnit(99999);

        assertThrows(SQLException.class, () -> {
            UnitRepository.addNewUnit(unit);
        });
    }

}
