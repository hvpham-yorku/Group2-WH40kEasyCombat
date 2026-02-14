package eecs2311.group2.wh40k_easycombat.repository;

import eecs2311.group2.wh40k_easycombat.model.Factions;
import eecs2311.group2.wh40k_easycombat.model.Units;
import org.junit.jupiter.api.*;

import java.sql.SQLException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class UnitRepositoryTest extends TestSetup {
    private Factions createSampleFaction() {
        return new Factions(-1, "Test Faction");
    }

    private Units createSampleUnit() {
        return new Units(-1,
            1,
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

    @Test
    void returnCorrectUnitTest() throws SQLException {
        Factions faction = createSampleFaction();
        FactionRepository.addNewFaction(faction);
        Units unit = createSampleUnit();
        int returnedId = UnitRepository.addNewUnit(unit);

        Units result = UnitRepository.getUnitById(returnedId);

        assertNotNull(result);
        assertEquals(1, returnedId);
        assertEquals("Test Unit", result.name());
        assertEquals(100, result.points());
        assertEquals(6, result.M());
        //Testing a few of the primitive attributes

        assertEquals(List.of(1, 2), result.coreAbilityIdList());
        //Checks whether the list get properly turn into a string and back into a list
    }

    @Test
    void returnErrorWrongITest() throws SQLException {
        //Empty Database, no instances 
        Units result = UnitRepository.getUnitById(0);
        assertNull(result);
    }

    @Test
    void updateUnit_shouldModifyExistingUnit() throws SQLException {
        Units unit = createSampleUnit();
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

        assertEquals("Updated Name", result.name());
    }

    @Test
    void deleteUnit_shouldRemoveUnit() throws SQLException {
        Units unit = createSampleUnit();
        UnitRepository.addNewUnit(unit);

        Units inserted = UnitRepository.getAllUnits().get(0);

        UnitRepository.deleteUnit(inserted);

        assertTrue(UnitRepository.getAllUnits().isEmpty());
    }
}
