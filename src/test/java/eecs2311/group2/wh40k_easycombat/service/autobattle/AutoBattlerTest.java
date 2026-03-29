package eecs2311.group2.wh40k_easycombat.service.autobattle;

import eecs2311.group2.wh40k_easycombat.model.combat.AttackResult;
import eecs2311.group2.wh40k_easycombat.model.editor.EditorRerollType;
import eecs2311.group2.wh40k_easycombat.model.instance.UnitInstance;
import eecs2311.group2.wh40k_easycombat.model.instance.UnitModelInstance;
import eecs2311.group2.wh40k_easycombat.model.instance.WeaponProfile;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class AutoBattlerTest {

    private final AutoBattler autoBattler = new AutoBattler();

    @Test
    @DisplayName("simulateAttack rejects non-assault shooting after advancing")
    void simulateAttackRejectsAdvancedNonAssaultShooting() {
        AttackResult result = autoBattler.simulateAttack(
                unit("Intercessors", 1),
                unit("Gaunts", 1),
                rangedWeapon("Bolt Rifle", 1, "2", ""),
                context(false, true, false, false)
        );

        assertFalse(result.resolved());
        assertEquals("This unit Advanced, so only Assault weapons can shoot.", result.notes().getFirst());
    }

    @Test
    @DisplayName("simulateAttack allows assault weapons after advancing")
    void simulateAttackAllowsAssaultWeaponAfterAdvance() {
        AttackResult result = autoBattler.simulateAttack(
                unit("Assault Intercessors", 1),
                unit("Gaunts", 1),
                rangedWeapon("Assault Bolter", 1, "0", "Assault"),
                context(false, true, false, false)
        );

        assertTrue(result.resolved());
        assertEquals(0, result.attacks());
    }

    @Test
    @DisplayName("simulateAttack rejects melee attacks after falling back")
    void simulateAttackRejectsFightAfterFallback() {
        AttackResult result = autoBattler.simulateAttack(
                unit("Bladeguard", 1),
                unit("Ork Boyz", 1),
                meleeWeapon("Master-crafted Power Weapon", "2", ""),
                new AttackKeywordContext(
                        0,
                        false,
                        false,
                        false,
                        true,
                        false,
                        true,
                        false,
                        false,
                        false,
                        "",
                        false,
                        false,
                        false,
                        false,
                        false,
                        0,
                        0,
                        EditorRerollType.NONE,
                        EditorRerollType.NONE
                )
        );

        assertFalse(result.resolved());
        assertEquals("A unit that Fell Back is not eligible to fight unless another rule says otherwise.", result.notes().getFirst());
    }

    @Test
    @DisplayName("simulateAttack rejects melee attacks when the unit is not eligible to fight")
    void simulateAttackRejectsFightWhenNotEligible() {
        AttackResult result = autoBattler.simulateAttack(
                unit("Terminators", 1),
                unit("Grots", 1),
                meleeWeapon("Power Fist", "3", ""),
                new AttackKeywordContext(
                        0,
                        false,
                        false,
                        false,
                        false,
                        false,
                        false,
                        false,
                        false,
                        false,
                        "",
                        false,
                        false,
                        false,
                        false,
                        false,
                        0,
                        0,
                        EditorRerollType.NONE,
                        EditorRerollType.NONE
                )
        );

        assertFalse(result.resolved());
        assertEquals("Selected unit is not confirmed eligible to fight.", result.notes().getFirst());
    }

    @Test
    @DisplayName("simulateAttack adds rapid fire attacks for each weapon bearer")
    void simulateAttackAddsRapidFireAttacks() {
        AttackResult result = autoBattler.simulateAttack(
                unit("Intercessors", 1),
                unit("Termagants", 1),
                rangedWeapon("Bolt Rifle", 2, "2", "Rapid Fire 1"),
                context(false, false, false, true)
        );

        assertTrue(result.resolved());
        assertEquals(6, result.attacks());
    }

    @Test
    @DisplayName("simulateAttack adds blast attacks for every five target models")
    void simulateAttackAddsBlastBonusAttacks() {
        AttackResult result = autoBattler.simulateAttack(
                unit("Desolators", 1),
                unit("Gaunts", 10),
                rangedWeapon("Frag Launcher", 1, "2", "Blast"),
                context(false, false, true, false)
        );

        assertTrue(result.resolved());
        assertEquals(4, result.attacks());
    }

    private static UnitInstance unit(String name, int modelCount) {
        UnitInstance unit = new UnitInstance("ds-" + name.replace(" ", "-"), name);
        for (int i = 0; i < modelCount; i++) {
            unit.addModel(new UnitModelInstance(name + " " + i, "6\"", "4", "3+", "2", "6+", "1", ""));
        }
        return unit;
    }

    private static WeaponProfile rangedWeapon(String name, int count, String attacks, String description) {
        return new WeaponProfile(10, name, description, count, "24\"", attacks, "3+", "4", "-1", "1", false);
    }

    private static WeaponProfile meleeWeapon(String name, String attacks, String description) {
        return new WeaponProfile(11, name, description, 1, "Melee", attacks, "3+", "6", "-2", "2", true);
    }

    private static AttackKeywordContext context(
            boolean eligibleToFight,
            boolean advanced,
            boolean blastLegal,
            boolean withinHalfRange
    ) {
        return new AttackKeywordContext(
                0,
                withinHalfRange,
                false,
                advanced,
                false,
                false,
                eligibleToFight,
                false,
                blastLegal,
                false,
                "",
                true,
                false,
                false,
                false,
                false,
                0,
                0,
                EditorRerollType.NONE,
                EditorRerollType.NONE
        );
    }
}
