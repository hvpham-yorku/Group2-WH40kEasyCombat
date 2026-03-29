package eecs2311.group2.wh40k_easycombat.service.autobattle;

import eecs2311.group2.wh40k_easycombat.model.combat.AutoBattleResolution;
import eecs2311.group2.wh40k_easycombat.model.combat.FightPhaseState;
import eecs2311.group2.wh40k_easycombat.model.combat.PendingDamage;
import eecs2311.group2.wh40k_easycombat.model.combat.PendingDamageStepResult;
import eecs2311.group2.wh40k_easycombat.model.editor.EditorRerollType;
import eecs2311.group2.wh40k_easycombat.model.instance.Player;
import eecs2311.group2.wh40k_easycombat.model.instance.UnitInstance;
import eecs2311.group2.wh40k_easycombat.model.instance.UnitModelInstance;
import eecs2311.group2.wh40k_easycombat.model.instance.WeaponProfile;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class AutoBattleServiceTest {

    private final AutoBattleService service = new AutoBattleService();

    @Test
    @DisplayName("availableWeapons returns the remaining ranged weapon count for the current phase")
    void availableWeaponsReturnsRemainingRangedWeaponCount() {
        UnitInstance attacker = unit("Heavy Intercessors");
        WeaponProfile heavyBoltRifle = rangedWeapon("Heavy Bolt Rifle", 3, "2", "");
        attacker.addRangedWeapon(heavyBoltRifle);
        attacker.markRangedWeaponUsedThisPhase(heavyBoltRifle, 1);

        List<WeaponProfile> available = service.availableWeapons(AutoBattleMode.SHOOTING, attacker);

        assertEquals(1, available.size());
        assertEquals("Heavy Bolt Rifle", available.getFirst().name());
        assertEquals(2, available.getFirst().count());
    }

    @Test
    @DisplayName("resolve shooting only spends the requested number of identical weapons")
    void resolveShootingOnlySpendsRequestedWeaponCount() {
        UnitInstance attacker = unit("Desolation Squad");
        WeaponProfile superfrag = rangedWeapon("Superfrag Rocket Launcher", 3, "0", "");
        attacker.addRangedWeapon(superfrag);

        UnitInstance defender = unit("Termagants");

        AutoBattleResolution resolution = service.resolve(
                AutoBattleMode.SHOOTING,
                Player.ATTACKER,
                Player.ATTACKER,
                attacker,
                defender,
                superfrag,
                context(1, false),
                null
        );

        assertTrue(resolution.resolved());
        assertEquals(1, attacker.getUsedRangedWeaponCountThisPhase(superfrag));
        assertEquals(2, attacker.getRemainingRangedWeaponCountThisPhase(superfrag));
        assertEquals(2, service.availableWeapons(AutoBattleMode.SHOOTING, attacker).getFirst().count());
    }

    @Test
    @DisplayName("fight resolution uses every melee weapon profile on the unit and marks it as fought")
    void resolveFightUsesEveryMeleeWeaponProfile() {
        UnitInstance attacker = unit("Aggressors");
        WeaponProfile fist = meleeWeapon("Power Fist", "0", "");
        WeaponProfile knife = meleeWeapon("Combat Knife", "0", "Extra Attacks");
        attacker.addMeleeWeapon(fist);
        attacker.addMeleeWeapon(knife);
        attacker.setEligibleToFightThisPhase(true);

        UnitInstance defender = unit("Cultists");
        FightPhaseState fightState = new FightPhaseState(FightStep.REMAINING_COMBATANTS, Player.ATTACKER, "Attacker chooses next.");

        AutoBattleResolution resolution = service.resolve(
                AutoBattleMode.FIGHT,
                Player.ATTACKER,
                Player.ATTACKER,
                attacker,
                defender,
                fist,
                context(1, true),
                fightState
        );

        assertTrue(resolution.resolved());
        assertEquals(2, resolution.attacks().size());
        assertTrue(attacker.hasFoughtThisPhase());
        assertEquals(
                Set.of("Power Fist", "Combat Knife"),
                resolution.attacks().stream().map(attack -> attack.weaponName()).collect(java.util.stream.Collectors.toSet())
        );
    }

    @Test
    @DisplayName("applyNextPendingDamage only damages the selected model and wastes overflow damage")
    void applyNextPendingDamageOnlyAffectsSelectedModel() {
        UnitInstance defender = new UnitInstance("ds-def", "Necron Warriors");
        UnitModelInstance first = new UnitModelInstance("Warrior A", "5\"", "4", "4+", "3", "7+", "1", "");
        UnitModelInstance second = new UnitModelInstance("Warrior B", "5\"", "4", "4+", "3", "7+", "1", "");
        defender.addModel(first);
        defender.addModel(second);

        PendingDamageSession session = new PendingDamageSession(
                AutoBattleMode.SHOOTING,
                Player.ATTACKER,
                "Hellblaster Squad",
                defender.getUnitName(),
                defender,
                Set.of(),
                List.of(new PendingDamage("Shooting", "Plasma Incinerator", 1, 10, false))
        );

        PendingDamageStepResult result = service.applyNextPendingDamage(session, first);

        assertTrue(result.applied());
        assertEquals(3, result.appliedDamage());
        assertEquals(7, result.wastedDamage());
        assertTrue(result.targetDestroyed());
        assertTrue(result.sessionComplete());
        assertEquals(0, result.remainingPendingCount());
        assertEquals(0, result.remainingPendingDamage());
        assertEquals(second.getMaxHp(), second.getCurrentHp());
        assertEquals(1, result.casualtyUpdate().newlyDestroyedModels());
        assertEquals(List.of("Warrior A"), result.casualtyUpdate().destroyedModelNames());
    }

    private static UnitInstance unit(String name) {
        UnitInstance unit = new UnitInstance("ds-" + name.replace(" ", "-"), name);
        unit.addModel(new UnitModelInstance(name + " Model", "6\"", "4", "3+", "2", "6+", "1", ""));
        return unit;
    }

    private static WeaponProfile rangedWeapon(String name, int count, String attacks, String description) {
        return new WeaponProfile(1, name, description, count, "24\"", attacks, "3+", "4", "-1", "1", false);
    }

    private static WeaponProfile meleeWeapon(String name, String attacks, String description) {
        return new WeaponProfile(2, name, description, 1, "Melee", attacks, "3+", "5", "-2", "2", true);
    }

    private static AttackKeywordContext context(int weaponBearers, boolean eligibleToFight) {
        return new AttackKeywordContext(
                weaponBearers,
                false,
                false,
                false,
                false,
                false,
                eligibleToFight,
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
        );
    }
}
