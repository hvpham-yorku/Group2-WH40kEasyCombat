package eecs2311.group2.wh40k_easycombat.repository;

import eecs2311.group2.wh40k_easycombat.model.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration tests to verify complex data relationships and bulk updates.
 */
class StaticDataPersistenceTest extends TestSetup {

    @Test
    @DisplayName("Should maintain data integrity across Factions, Weapons, and Units during bulk updates")
    void fullRulebookLifecycleTest() throws SQLException {
        // 1. Setup Phase: Create Faction
        int factionId = FactionRepository.addNewFaction(new Factions(-1, "Aeldari"));

        // 2. Setup Phase: Create Weapons
        // Weapons often have specific keywords (e.g., [ASSAULT], [HEAVY])
        int weaponId1 = RangeWeaponRepository.addNewRangeWeapon(new RangeWeapons(
            -1, "Shuriken Cannon", 24, "3", 3, 6, -1, "2", List.of(1, 2)
        ));
        int weaponId2 = RangeWeaponRepository.addNewRangeWeapon(new RangeWeapons(
            -1, "Bright Lance", 36, "1", 3, 12, -4, "D6+2", List.of(3)
        ));

        // 3. Setup Phase: Create Unit linked to multiple weapons
        Units wraithlordTemplate = new Units(
            -1, factionId, "Wraithlord", 160, 8, 11, 2, 10, 6, 4, 0, 1,
            "1 model", List.of(10), List.of(11, 12), List.of(5, 6),
            List.of(weaponId1, weaponId2), // Linked weapons
            List.of(1)
        );
        int unitId = UnitRepository.addNewUnit(wraithlordTemplate);

        // --- SCENARIO: Game Balance Patch (Data Mutation) ---

        // 4. Update Weapon: Nerf the Bright Lance AP (Armor Penetration)
        RangeWeapons oldLance = RangeWeaponRepository.getRangeWeaponById(weaponId2);
        RangeWeapons nerfedLance = new RangeWeapons(
            oldLance.id(), oldLance.name(), oldLance.range(), oldLance.A(),
            oldLance.BS(), oldLance.S(), -3, // AP changed from -4 to -3
            oldLance.D(), oldLance.keywordIdList()
        );
        RangeWeaponRepository.updateRangeWeapon(nerfedLance);

        // 5. Update Unit: Increase points cost and rename
        Units oldUnit = UnitRepository.getUnitById(unitId);
        Units balancedUnit = new Units(
            oldUnit.id(), oldUnit.factionId(), "Wraithlord (Balanced)",
            175, // Points increased from 160 to 175
            oldUnit.M(), oldUnit.T(), oldUnit.SV(), oldUnit.W(), oldUnit.LD(), oldUnit.OC(),
            oldUnit.invulnerableSave(), oldUnit.category(), oldUnit.composition(),
            oldUnit.coreAbilityIdList(), oldUnit.otherAbilityIdList(),
            oldUnit.keywordIdList(), oldUnit.rangedWeaponIdList(), oldUnit.meleeWeaponIdList()
        );
        UnitRepository.updateUnit(balancedUnit);

        // --- VERIFICATION PHASE ---

        // Verify Unit details
        Units finalUnit = UnitRepository.getUnitById(unitId);
        assertNotNull(finalUnit);
        assertEquals("Wraithlord (Balanced)", finalUnit.name());
        assertEquals(175, finalUnit.points());

        // Verify Weapon link integrity
        // The unit should still be linked to the weapon, but the weapon attributes should be updated
        int linkedWeaponId = finalUnit.rangedWeaponIdList().get(1);
        RangeWeapons finalWeapon = RangeWeaponRepository.getRangeWeaponById(linkedWeaponId);
        assertEquals(-3, finalWeapon.AP(), "The weapon's AP should reflect the balance patch update.");
        assertEquals(weaponId2, finalWeapon.id(), "The ID must remain consistent.");
    }

    @Test
    @DisplayName("Should verify Cascade Delete: Removing a Faction must remove all its Units")
    void testCascadeDeleteFaction() throws SQLException {
        // Setup: Faction + Multiple Units
        int fid = FactionRepository.addNewFaction(new Factions(-1, "DeleteMe"));
        UnitRepository.addNewUnit(new Units(-1, fid, "Unit A", 10, 5, 5, 3, 1, 7, 2, 0, 1, "Comp", List.of(), List.of(), List.of(), List.of(), List.of()));
        UnitRepository.addNewUnit(new Units(-1, fid, "Unit B", 20, 5, 5, 3, 1, 7, 2, 0, 1, "Comp", List.of(), List.of(), List.of(), List.of(), List.of()));

        // Verification before delete
        assertEquals(2, UnitRepository.getAllUnits().size());

        // Execution: Delete the Faction (Triggering SQL ON DELETE CASCADE)
        FactionRepository.deleteFaction(new Factions(fid, "DeleteMe"));

        // Final Verification: Units should be gone
        assertTrue(UnitRepository.getAllUnits().isEmpty(), "Units should be deleted automatically by database cascade.");
    }

    @Test
    @DisplayName("Should verify that Unit weapon and ability lists are persisted and retrieved correctly")
    void unitFullLoadoutAndAbilityTest() throws SQLException {
        // 1. Setup Phase: Create prerequisite data
        int factionId = FactionRepository.addNewFaction(new Factions(-1, "Necrons"));

        // Create multiple weapons to test list integrity
        int gaussId = RangeWeaponRepository.addNewRangeWeapon(new RangeWeapons(-1, "Gauss Flayer", 24, "1", 3, 4, -1, "1", List.of()));
        int reaperId = RangeWeaponRepository.addNewRangeWeapon(new RangeWeapons(-1, "Gauss Reaper", 12, "2", 3, 5, -2, "1", List.of()));
        int bladeId = MeleeWeaponRepository.addNewMeleeWeapon(new MeleeWeapons(-1, "Hyperphase Blade", "3", 3, 5, -2, "1", List.of()));

        // Create IDs for abilities and keywords
        List<Integer> coreAbilities = List.of(101, 102); // e.g., Fights First, Stealth
        List<Integer> otherAbilities = List.of(201);     // e.g., Reanimation Protocols
        List<Integer> keywords = List.of(50, 51, 52);    // e.g., Infantry, Battleline, Necron Warriors

        // 2. Execution: Create a Unit with these complex lists
        Units warriorTemplate = new Units(
            -1,
            factionId,
            "Necron Warriors",
            100, 5, 4, 4, 1, 7, 2, 0, 1,
            "10-20 models",
            coreAbilities,
            otherAbilities,
            keywords,
            List.of(gaussId, reaperId), // Ranged Weapons list
            List.of(bladeId)            // Melee Weapons list
        );

        int savedUnitId = UnitRepository.addNewUnit(warriorTemplate);

        // 3. Retrieval: Fetch from DB
        Units retrieved = UnitRepository.getUnitById(savedUnitId);

        // 4. Verification: Deep list comparison
        assertNotNull(retrieved, "Unit should be retrievable from database");

        // Verify Ranged Weapons
        assertEquals(2, retrieved.rangedWeaponIdList().size(), "Should have exactly 2 ranged weapons");
        assertTrue(retrieved.rangedWeaponIdList().containsAll(List.of(gaussId, reaperId)),
            "Retrieved ranged weapon IDs must match the original input");

        // Verify Melee Weapons
        assertEquals(1, retrieved.meleeWeaponIdList().size());
        assertEquals(bladeId, retrieved.meleeWeaponIdList().get(0));

        // Verify Abilities (Testing IntListCodec logic)
        assertEquals(coreAbilities, retrieved.coreAbilityIdList(), "Core ability IDs must maintain exact order and values");
        assertEquals(otherAbilities, retrieved.otherAbilityIdList(), "Other ability IDs must match");

        // Verify Keywords
        assertEquals(3, retrieved.keywordIdList().size());
        assertTrue(retrieved.keywordIdList().contains(51), "Keyword ID 51 should be present in the list");
    }

    @Test
    @DisplayName("Should verify data consistency when modifying loadouts (Removing/Adding items)")
    void testUpdateUnitLoadout() throws SQLException {
        // Setup: Unit with 3 weapons
        int fid = FactionRepository.addNewFaction(new Factions(-1, "Orks"));
        Units boyz = new Units(-1, fid, "Ork Boyz", 85, 5, 5, 5, 1, 7, 2, 0, 1, "10 models",
            List.of(), List.of(), List.of(), List.of(10, 11, 12), List.of(20));

        int id = UnitRepository.addNewUnit(boyz);
        Units inserted = UnitRepository.getUnitById(id);

        // Action: Modify the weapon list (remove one, add a new one)
        List<Integer> newRangedLoadout = List.of(10, 99); // Removed 11, 12; added 99

        Units updatedBoyz = new Units(
            inserted.id(), inserted.factionId(), inserted.name(), inserted.points(),
            inserted.M(), inserted.T(), inserted.SV(), inserted.W(), inserted.LD(), inserted.OC(),
            inserted.invulnerableSave(), inserted.category(), inserted.composition(),
            inserted.coreAbilityIdList(), inserted.otherAbilityIdList(), inserted.keywordIdList(),
            newRangedLoadout,
            inserted.meleeWeaponIdList()
        );

        UnitRepository.updateUnit(updatedBoyz);

        // Verify: Check if the list was correctly updated in the DB
        Units result = UnitRepository.getUnitById(id);
        assertEquals(2, result.rangedWeaponIdList().size());
        assertFalse(result.rangedWeaponIdList().contains(11), "Weapon 11 should have been removed");
        assertTrue(result.rangedWeaponIdList().contains(99), "Weapon 99 should have been added");
    }

    @Test
    @DisplayName("Should maintain the exact order of IDs for Weapons and Abilities")
    void testListSequencePersistence() throws SQLException {
        // 1. Define a specific, non-sequential order of IDs
        // This ensures the test doesn't pass by "accident" due to sorting
        List<Integer> specificWeaponOrder = Arrays.asList(99, 1, 55, 20);
        List<Integer> specificAbilityOrder = Arrays.asList(500, 10, 250);

        // 2. Setup: Create prerequisite Faction
        int factionId = FactionRepository.addNewFaction(new Factions(-1, "Sequence Test Faction"));

        // 3. Execution: Create a Unit with the specifically ordered lists
        Units sequenceUnit = new Units(
            -1, factionId, "Order Specialist", 100, 6, 4, 3, 2, 7, 1, 0, 1,
            "Single Model",
            specificAbilityOrder, // Core Abilities
            List.of(), List.of(),
            specificWeaponOrder,  // Ranged Weapons
            List.of()
        );

        int savedId = UnitRepository.addNewUnit(sequenceUnit);

        // 4. Retrieval: Fetch from DB
        Units retrieved = UnitRepository.getUnitById(savedId);

        // 5. Verification: Check exact list equality (Content + Order)
        assertNotNull(retrieved);

        // In Java, List.equals() returns true only if elements are the same AND in the same order
        assertEquals(specificWeaponOrder, retrieved.rangedWeaponIdList(),
            "The retrieved weapon list must match the exact input order: 99, 1, 55, 20");

        assertEquals(specificAbilityOrder, retrieved.coreAbilityIdList(),
            "The retrieved ability list must match the exact input order: 500, 10, 250");

        // Extra Check: Ensure the first and last elements specifically
        assertEquals(99, retrieved.rangedWeaponIdList().get(0), "First weapon ID mismatch");
        assertEquals(20, retrieved.rangedWeaponIdList().get(3), "Last weapon ID mismatch");
    }

    @Test
    @DisplayName("Should verify that shuffling or changing order is correctly updated in DB")
    void testListReorderingUpdate() throws SQLException {
        int fid = FactionRepository.addNewFaction(new Factions(-1, "Orks"));

        // Initial order: 1, 2, 3
        Units unit = new Units(-1, fid, "Reorder Unit", 100, 5, 5, 4, 2, 7, 2, 0, 1, "Comp",
            List.of(1, 2, 3), List.of(), List.of(), List.of(), List.of());

        int id = UnitRepository.addNewUnit(unit);

        // Action: Reverse the order: 3, 2, 1
        List<Integer> reversedOrder = List.of(3, 2, 1);
        Units updatedUnit = new Units(
            id, unit.factionId(), unit.name(), unit.points(), unit.M(), unit.T(), unit.SV(), unit.W(), unit.LD(), unit.OC(),
            unit.invulnerableSave(), unit.category(), unit.composition(),
            reversedOrder, // Updated order
            unit.otherAbilityIdList(), unit.keywordIdList(), unit.rangedWeaponIdList(), unit.meleeWeaponIdList()
        );

        UnitRepository.updateUnit(updatedUnit);

        // Verify: Check if DB stored the reversed order
        Units result = UnitRepository.getUnitById(id);
        assertEquals(reversedOrder, result.coreAbilityIdList(), "The database should have stored the reversed sequence [3, 2, 1]");
        assertNotEquals(List.of(1, 2, 3), result.coreAbilityIdList(), "The old order should no longer exist");
    }
}