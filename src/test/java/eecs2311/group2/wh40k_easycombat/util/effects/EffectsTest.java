package eecs2311.group2.wh40k_easycombat.util.effects;

import eecs2311.group2.wh40k_easycombat.model.editor.EditorRerollType;
import eecs2311.group2.wh40k_easycombat.model.editor.EditorRuleModifiers;
import eecs2311.group2.wh40k_easycombat.model.instance.ArmyInstance;
import eecs2311.group2.wh40k_easycombat.model.instance.UnitInstance;
import eecs2311.group2.wh40k_easycombat.model.instance.UnitModelInstance;
import eecs2311.group2.wh40k_easycombat.model.instance.WeaponProfile;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class EffectsTest {

    private UnitInstance makeUnitWithOneModel(int wounds) {
        UnitInstance unit = new UnitInstance("ds-1", "Test Unit");

        UnitModelInstance model = new UnitModelInstance(
                "Test Model",
                "6",
                "4",
                "3+",
                String.valueOf(wounds),
                "7+",
                "1",
                "0"
        );

        unit.addModel(model);
        return unit;
    }

    private UnitInstance makeUnitWithTwoModels(int woundsEach) {
        UnitInstance unit = new UnitInstance("ds-2", "Two Model Unit");

        UnitModelInstance model1 = new UnitModelInstance(
                "Model 1",
                "6",
                "4",
                "3+",
                String.valueOf(woundsEach),
                "7+",
                "1",
                "0"
        );

        UnitModelInstance model2 = new UnitModelInstance(
                "Model 2",
                "6",
                "4",
                "3+",
                String.valueOf(woundsEach),
                "7+",
                "1",
                "0"
        );

        unit.addModel(model1);
        unit.addModel(model2);
        return unit;
    }

    private ArmyInstance makeArmy() {
        return new ArmyInstance(1, "Test Army", "f1", "Faction", "det1");
    }

    private WeaponProfile makeWeapon(String name) {
        return new WeaponProfile(
                1,
                name,
                "Test weapon",
                2,
                "24",
                "4",
                "3+",
                "4",
                "-1",
                "2",
                false
        );
    }

    @Test
    void buffHealthShouldHealAUnit() {
        UnitInstance unit = makeUnitWithOneModel(5);
        unit.getModels().get(0).takeDamage(3);

        Buff healBuff = new Buff("Med Kit", EffectType.HEAL, Tag.HEALTH, 2);
        healBuff.apply(unit);

        assertEquals(4, unit.getModels().get(0).getCurrentHp());
    }

    @Test
    void healShouldNotGoAboveMaxHp() {
        UnitInstance unit = makeUnitWithOneModel(5);
        unit.getModels().get(0).takeDamage(1);

        Buff healBuff = new Buff("Big Heal", EffectType.HEAL, Tag.HEALTH, 10);
        healBuff.apply(unit);

        assertEquals(5, unit.getModels().get(0).getCurrentHp());
    }

    @Test
    void debuffHealthShouldDamageAUnit() {
        UnitInstance unit = makeUnitWithOneModel(5);

        Debuff poison = new Debuff("Poison", EffectType.POISON, Tag.HEALTH, 2);
        poison.apply(unit);

        assertEquals(3, unit.getModels().get(0).getCurrentHp());
    }

    @Test
    void debuffDamageShouldAlsoDamageAUnit() {
        UnitInstance unit = makeUnitWithOneModel(5);

        Debuff fire = new Debuff("Fire", EffectType.FIRE, Tag.DAMAGE, 3);
        fire.apply(unit);

        assertEquals(2, unit.getModels().get(0).getCurrentHp());
    }

    @Test
    void damageShouldCarryIntoNextModelIfNeeded() {
        UnitInstance unit = makeUnitWithTwoModels(3);

        Debuff bigHit = new Debuff("Big Hit", EffectType.DAMAGE, Tag.HEALTH, 4);
        bigHit.apply(unit);

        assertTrue(unit.getModels().get(0).isDestroyed());
        assertEquals(2, unit.getModels().get(1).getCurrentHp());
    }

    @Test
    void moraleDebuffShouldBattleShockUnit() {
        UnitInstance unit = makeUnitWithOneModel(5);
        unit.setBattleShocked(false);

        Debuff fear = new Debuff("Fear Aura", EffectType.MORALE, Tag.MORALE, 1);
        fear.apply(unit);

        assertTrue(unit.isBattleShocked());
    }

    @Test
    void moraleBuffShouldRemoveBattleShockFromUnit() {
        UnitInstance unit = makeUnitWithOneModel(5);
        unit.setBattleShocked(true);

        Buff inspire = new Buff("Inspiring Presence", EffectType.MORALE, Tag.MORALE, 1);
        inspire.apply(unit);

        assertFalse(unit.isBattleShocked());
    }

    @Test
    void utilityBuffShouldMakeUnitEligibleToFight() {
        UnitInstance unit = makeUnitWithOneModel(5);
        unit.setEligibleToFightThisPhase(false);

        Buff fightBuff = new Buff("Battle Focus", EffectType.SPEED, Tag.UTILITY, 1);
        fightBuff.apply(unit);

        assertTrue(unit.isEligibleToFightThisPhase());
    }

    @Test
    void utilityDebuffShouldMakeUnitNotEligibleToFight() {
        UnitInstance unit = makeUnitWithOneModel(5);
        unit.setEligibleToFightThisPhase(true);

        Debuff stun = new Debuff("Stun", EffectType.SLOWNESS, Tag.UTILITY, 1);
        stun.apply(unit);

        assertFalse(unit.isEligibleToFightThisPhase());
    }

    @Test
    void utilityBuffShouldAddCpToArmy() {
        ArmyInstance army = makeArmy();
        army.setCurrentCp(1);

        Buff cpBuff = new Buff("Command Surge", EffectType.COMMAND, Tag.UTILITY, 2);
        cpBuff.apply(army);

        assertEquals(3, army.getCurrentCp());
    }

    @Test
    void utilityDebuffShouldSpendCpFromArmy() {
        ArmyInstance army = makeArmy();
        army.setCurrentCp(4);

        Debuff cpLoss = new Debuff("Disruption", EffectType.COMMAND, Tag.UTILITY, 3);
        cpLoss.apply(army);

        assertEquals(1, army.getCurrentCp());
    }

    @Test
    void victoryPointBuffShouldAddVpToArmy() {
        ArmyInstance army = makeArmy();
        army.setCurrentVp(2);

        Buff vpBuff = new Buff("Objective Secured", EffectType.VICTORY, Tag.VICTORY_POINTS, 3);
        vpBuff.apply(army);

        assertEquals(5, army.getCurrentVp());
    }

    @Test
    void victoryPointDebuffShouldRemoveVpFromArmy() {
        ArmyInstance army = makeArmy();
        army.setCurrentVp(5);

        Debuff vpLoss = new Debuff("Sabotage", EffectType.VICTORY, Tag.VICTORY_POINTS, 2);
        vpLoss.apply(army);

        assertEquals(3, army.getCurrentVp());
    }

    @Test
    void victoryPointDebuffShouldNotDropBelowZero() {
        ArmyInstance army = makeArmy();
        army.setCurrentVp(1);

        Debuff vpLoss = new Debuff("Major Sabotage", EffectType.VICTORY, Tag.VICTORY_POINTS, 5);
        vpLoss.apply(army);

        assertEquals(0, army.getCurrentVp());
    }

    @Test
    void armyMoraleDebuffShouldBattleShockAllUnits() {
        ArmyInstance army = makeArmy();
        UnitInstance unit1 = makeUnitWithOneModel(5);
        UnitInstance unit2 = makeUnitWithOneModel(5);

        army.addUnit(unit1);
        army.addUnit(unit2);

        Debuff fear = new Debuff("Fear Wave", EffectType.MORALE, Tag.MORALE, 1);
        fear.apply(army);

        assertTrue(unit1.isBattleShocked());
        assertTrue(unit2.isBattleShocked());
    }

    @Test
    void armyMoraleBuffShouldRemoveBattleShockFromAllUnits() {
        ArmyInstance army = makeArmy();
        UnitInstance unit1 = makeUnitWithOneModel(5);
        UnitInstance unit2 = makeUnitWithOneModel(5);

        unit1.setBattleShocked(true);
        unit2.setBattleShocked(true);

        army.addUnit(unit1);
        army.addUnit(unit2);

        Buff inspire = new Buff("Inspiring Aura", EffectType.MORALE, Tag.MORALE, 1);
        inspire.apply(army);

        assertFalse(unit1.isBattleShocked());
        assertFalse(unit2.isBattleShocked());
    }

    @Test
    void armyHealthBuffShouldHealUnitsInArmy() {
        ArmyInstance army = makeArmy();
        UnitInstance unit1 = makeUnitWithOneModel(5);
        UnitInstance unit2 = makeUnitWithOneModel(5);

        unit1.getModels().get(0).takeDamage(2);
        unit2.getModels().get(0).takeDamage(1);

        army.addUnit(unit1);
        army.addUnit(unit2);

        Buff healArmy = new Buff("Field Medic Aura", EffectType.HEAL, Tag.HEALTH, 1);
        healArmy.apply(army);

        assertEquals(4, unit1.getModels().get(0).getCurrentHp());
        assertEquals(5, unit2.getModels().get(0).getCurrentHp());
    }

    @Test
    void armyDamageDebuffShouldDamageUnitsInArmy() {
        ArmyInstance army = makeArmy();
        UnitInstance unit1 = makeUnitWithOneModel(5);
        UnitInstance unit2 = makeUnitWithOneModel(5);

        army.addUnit(unit1);
        army.addUnit(unit2);

        Debuff blast = new Debuff("Blast Wave", EffectType.DAMAGE, Tag.DAMAGE, 2);
        blast.apply(army);

        assertEquals(3, unit1.getModels().get(0).getCurrentHp());
        assertEquals(3, unit2.getModels().get(0).getCurrentHp());
    }

    @Test
    void decoderShouldCreateBuffCorrectly() {
        Effect effect = Decoder.decodeEffect("buff", "Heal", "heal", "health", 3);

        assertNotNull(effect);
        assertTrue(effect instanceof Buff);
        assertEquals("Heal", effect.getName());
        assertEquals(EffectType.HEAL, effect.getEffectType());
        assertEquals(Tag.HEALTH, effect.getTag());
        assertEquals(3, effect.getValue());
    }

    @Test
    void decoderShouldCreateDebuffCorrectly() {
        Effect effect = Decoder.decodeEffect("debuff", "Poison", "poison", "health", 2);

        assertNotNull(effect);
        assertTrue(effect instanceof Debuff);
        assertEquals("Poison", effect.getName());
        assertEquals(EffectType.POISON, effect.getEffectType());
        assertEquals(Tag.HEALTH, effect.getTag());
        assertEquals(2, effect.getValue());
    }

    @Test
    void decoderShouldCreateWeaponSpecificEffect() {
        Effect effect = Decoder.decodeWeaponEffect(
                "buff",
                "Sharpshooter",
                "weapon",
                "hit_roll",
                1,
                "Bolt Rifle"
        );

        assertNotNull(effect);
        assertTrue(effect instanceof Buff);
        assertEquals("Bolt Rifle", effect.getWeaponName());
        assertEquals(Tag.HIT_ROLL, effect.getTag());
        assertEquals(1, effect.getValue());
    }

    @Test
    void decoderShouldCreateKeywordEffect() {
        Effect effect = Decoder.decodeKeywordEffect(
                "buff",
                "Lethal Hits Aura",
                "weapon",
                "weapon_keyword",
                "Bolt Rifle",
                "LETHAL HITS"
        );

        assertNotNull(effect);
        assertTrue(effect instanceof Buff);
        assertEquals("Bolt Rifle", effect.getWeaponName());
        assertEquals("LETHAL HITS", effect.getKeywordText());
        assertEquals(Tag.WEAPON_KEYWORD, effect.getTag());
    }

    @Test
    void decoderShouldThrowForInvalidEffectType() {
        assertThrows(IllegalArgumentException.class, () ->
                Decoder.decodeEffect("buff", "Bad", "not_real", "health", 1)
        );
    }

    @Test
    void decoderShouldThrowForInvalidTag() {
        assertThrows(IllegalArgumentException.class, () ->
                Decoder.decodeEffect("buff", "Bad", "heal", "not_real", 1)
        );
    }

    @Test
    void decoderShouldThrowForInvalidEffectClass() {
        assertThrows(IllegalArgumentException.class, () ->
                Decoder.decodeEffect("somethingElse", "Bad", "heal", "health", 1)
        );
    }

    @Test
    void effectServiceShouldApplySingleEffectToUnit() {
        UnitInstance unit = makeUnitWithOneModel(5);
        unit.getModels().get(0).takeDamage(2);

        EffectService service = new EffectService();
        Effect heal = new Buff("Heal", EffectType.HEAL, Tag.HEALTH, 1);

        service.applyEffectToUnit(heal, unit);

        assertEquals(4, unit.getModels().get(0).getCurrentHp());
    }

    @Test
    void effectServiceShouldApplyListOfEffectsToUnit() {
        UnitInstance unit = makeUnitWithOneModel(5);

        EffectService service = new EffectService();
        List<Effect> effects = List.of(
                new Debuff("Poison", EffectType.POISON, Tag.HEALTH, 2),
                new Buff("Small Heal", EffectType.HEAL, Tag.HEALTH, 1)
        );

        service.applyEffectsToUnit(effects, unit);

        assertEquals(4, unit.getModels().get(0).getCurrentHp());
    }

    @Test
    void effectServiceShouldDecodeAndApplyToArmy() {
        ArmyInstance army = makeArmy();
        army.setCurrentCp(0);

        EffectService service = new EffectService();
        service.decodeAndApplyToArmy("buff", "Command Gain", "command", "utility", 2, army);

        assertEquals(2, army.getCurrentCp());
    }

    @Test
    void buffHitRollShouldCreatePositiveModifier() {
        WeaponProfile weapon = makeWeapon("Bolt Rifle");
        Buff effect = new Buff("Aim Assist", EffectType.WEAPON, Tag.HIT_ROLL, 1, "Bolt Rifle");

        EditorRuleModifiers mods = effect.toAttackModifiers(weapon);

        assertEquals(1, mods.hitModifier());
        assertEquals(0, mods.woundModifier());
        assertTrue(mods.appliedRuleNames().contains("Aim Assist"));
    }

    @Test
    void debuffHitRollShouldCreateNegativeModifier() {
        WeaponProfile weapon = makeWeapon("Bolt Rifle");
        Debuff effect = new Debuff("Smoke", EffectType.WEAPON, Tag.HIT_ROLL, 1, "Bolt Rifle");

        EditorRuleModifiers mods = effect.toAttackModifiers(weapon);

        assertEquals(-1, mods.hitModifier());
    }

    @Test
    void buffWoundRollShouldCreatePositiveModifier() {
        WeaponProfile weapon = makeWeapon("Bolt Rifle");
        Buff effect = new Buff("Wound Boost", EffectType.WEAPON, Tag.WOUND_ROLL, 1, "Bolt Rifle");

        EditorRuleModifiers mods = effect.toAttackModifiers(weapon);

        assertEquals(1, mods.woundModifier());
    }

    @Test
    void debuffWoundRollShouldCreateNegativeModifier() {
        WeaponProfile weapon = makeWeapon("Bolt Rifle");
        Debuff effect = new Debuff("Tough Hide", EffectType.WEAPON, Tag.WOUND_ROLL, 1, "Bolt Rifle");

        EditorRuleModifiers mods = effect.toAttackModifiers(weapon);

        assertEquals(-1, mods.woundModifier());
    }

    @Test
    void buffWeaponAttacksShouldCreatePositiveModifier() {
        WeaponProfile weapon = makeWeapon("Bolt Rifle");
        Buff effect = new Buff("Fury", EffectType.WEAPON, Tag.WEAPON_ATTACKS, 2, "Bolt Rifle");

        EditorRuleModifiers mods = effect.toAttackModifiers(weapon);

        assertEquals(2, mods.attacksModifier());
    }

    @Test
    void debuffWeaponAttacksShouldCreateNegativeModifier() {
        WeaponProfile weapon = makeWeapon("Bolt Rifle");
        Debuff effect = new Debuff("Suppression", EffectType.WEAPON, Tag.WEAPON_ATTACKS, 1, "Bolt Rifle");

        EditorRuleModifiers mods = effect.toAttackModifiers(weapon);

        assertEquals(-1, mods.attacksModifier());
    }

    @Test
    void buffWeaponDamageShouldCreatePositiveModifier() {
        WeaponProfile weapon = makeWeapon("Bolt Rifle");
        Buff effect = new Buff("Overcharge", EffectType.WEAPON, Tag.WEAPON_DAMAGE, 1, "Bolt Rifle");

        EditorRuleModifiers mods = effect.toAttackModifiers(weapon);

        assertEquals(1, mods.damageModifier());
    }

    @Test
    void debuffWeaponDamageShouldCreateNegativeModifier() {
        WeaponProfile weapon = makeWeapon("Bolt Rifle");
        Debuff effect = new Debuff("Dampen", EffectType.WEAPON, Tag.WEAPON_DAMAGE, 1, "Bolt Rifle");

        EditorRuleModifiers mods = effect.toAttackModifiers(weapon);

        assertEquals(-1, mods.damageModifier());
    }

    @Test
    void buffWeaponApShouldCreatePositiveModifier() {
        WeaponProfile weapon = makeWeapon("Bolt Rifle");
        Buff effect = new Buff("Armour Piercing Boost", EffectType.WEAPON, Tag.WEAPON_AP, 1, "Bolt Rifle");

        EditorRuleModifiers mods = effect.toAttackModifiers(weapon);

        assertEquals(1, mods.apModifier());
    }

    @Test
    void debuffWeaponApShouldCreateNegativeModifier() {
        WeaponProfile weapon = makeWeapon("Bolt Rifle");
        Debuff effect = new Debuff("AP Loss", EffectType.WEAPON, Tag.WEAPON_AP, 1, "Bolt Rifle");

        EditorRuleModifiers mods = effect.toAttackModifiers(weapon);

        assertEquals(-1, mods.apModifier());
    }

    @Test
    void weaponKeywordBuffShouldAddKeyword() {
        WeaponProfile weapon = makeWeapon("Bolt Rifle");
        Buff effect = new Buff(
                "Lethal Hits Aura",
                EffectType.WEAPON,
                Tag.WEAPON_KEYWORD,
                0,
                "Bolt Rifle",
                "LETHAL HITS"
        );

        EditorRuleModifiers mods = effect.toAttackModifiers(weapon);

        assertEquals("LETHAL HITS", mods.extraWeaponKeywords());
    }

    @Test
    void hitRerollBuffValueOneShouldGrantOnesReroll() {
        WeaponProfile weapon = makeWeapon("Bolt Rifle");
        Buff effect = new Buff("Targeting Data", EffectType.WEAPON, Tag.HIT_REROLL, 1, "Bolt Rifle");

        EditorRuleModifiers mods = effect.toAttackModifiers(weapon);

        assertEquals(EditorRerollType.ONES, mods.hitReroll());
    }

    @Test
    void hitRerollBuffValueTwoShouldGrantFailsReroll() {
        WeaponProfile weapon = makeWeapon("Bolt Rifle");
        Buff effect = new Buff("Full Reroll Aura", EffectType.WEAPON, Tag.HIT_REROLL, 2, "Bolt Rifle");

        EditorRuleModifiers mods = effect.toAttackModifiers(weapon);

        assertEquals(EditorRerollType.FAILS, mods.hitReroll());
    }

    @Test
    void woundRerollBuffValueOneShouldGrantOnesReroll() {
        WeaponProfile weapon = makeWeapon("Bolt Rifle");
        Buff effect = new Buff("Wound Precision", EffectType.WEAPON, Tag.WOUND_REROLL, 1, "Bolt Rifle");

        EditorRuleModifiers mods = effect.toAttackModifiers(weapon);

        assertEquals(EditorRerollType.ONES, mods.woundReroll());
    }

    @Test
    void woundRerollBuffValueTwoShouldGrantFailsReroll() {
        WeaponProfile weapon = makeWeapon("Bolt Rifle");
        Buff effect = new Buff("Full Wound Reroll Aura", EffectType.WEAPON, Tag.WOUND_REROLL, 2, "Bolt Rifle");

        EditorRuleModifiers mods = effect.toAttackModifiers(weapon);

        assertEquals(EditorRerollType.FAILS, mods.woundReroll());
    }

    @Test
    void weaponSpecificEffectShouldNotApplyToWrongWeapon() {
        WeaponProfile weapon = makeWeapon("Plasma Gun");
        Buff effect = new Buff("Bolt Rifle Buff", EffectType.WEAPON, Tag.HIT_ROLL, 1, "Bolt Rifle");

        EditorRuleModifiers mods = effect.toAttackModifiers(weapon);

        assertFalse(mods.hasAnyEffect());
        assertEquals(0, mods.hitModifier());
    }

    @Test
    void resolveAttackModifiersShouldCombineMultipleEffects() {
        WeaponProfile weapon = makeWeapon("Bolt Rifle");

        List<Effect> effects = List.of(
                new Buff("Hit Boost", EffectType.WEAPON, Tag.HIT_ROLL, 1, "Bolt Rifle"),
                new Buff("Wound Boost", EffectType.WEAPON, Tag.WOUND_ROLL, 1, "Bolt Rifle"),
                new Buff("Extra Attacks", EffectType.WEAPON, Tag.WEAPON_ATTACKS, 2, "Bolt Rifle"),
                new Buff("Extra Damage", EffectType.WEAPON, Tag.WEAPON_DAMAGE, 1, "Bolt Rifle"),
                new Buff("Extra AP", EffectType.WEAPON, Tag.WEAPON_AP, 1, "Bolt Rifle"),
                new Buff("Keyword", EffectType.WEAPON, Tag.WEAPON_KEYWORD, 0, "Bolt Rifle", "LETHAL HITS"),
                new Buff("Reroll Hits", EffectType.WEAPON, Tag.HIT_REROLL, 1, "Bolt Rifle"),
                new Buff("Reroll Wounds", EffectType.WEAPON, Tag.WOUND_REROLL, 2, "Bolt Rifle")
        );

        EffectService service = new EffectService();
        EditorRuleModifiers mods = service.resolveAttackModifiers(effects, weapon);

        assertEquals(1, mods.hitModifier());
        assertEquals(1, mods.woundModifier());
        assertEquals(2, mods.attacksModifier());
        assertEquals(1, mods.damageModifier());
        assertEquals(1, mods.apModifier());
        assertEquals("LETHAL HITS", mods.extraWeaponKeywords());
        assertEquals(EditorRerollType.ONES, mods.hitReroll());
        assertEquals(EditorRerollType.FAILS, mods.woundReroll());
    }

    @Test
    void resolveAttackModifiersShouldIgnoreEffectsForOtherWeapons() {
        WeaponProfile weapon = makeWeapon("Bolt Rifle");

        List<Effect> effects = List.of(
                new Buff("Wrong Weapon Buff", EffectType.WEAPON, Tag.HIT_ROLL, 2, "Plasma Gun"),
                new Buff("Correct Weapon Buff", EffectType.WEAPON, Tag.WEAPON_DAMAGE, 1, "Bolt Rifle")
        );

        EffectService service = new EffectService();
        EditorRuleModifiers mods = service.resolveAttackModifiers(effects, weapon);

        assertEquals(0, mods.hitModifier());
        assertEquals(1, mods.damageModifier());
    }

    @Test
    void resolveAttackModifiersShouldMergeKeywordStringsWithoutDuplicates() {
        WeaponProfile weapon = makeWeapon("Bolt Rifle");

        List<Effect> effects = List.of(
                new Buff("Keyword 1", EffectType.WEAPON, Tag.WEAPON_KEYWORD, 0, "Bolt Rifle", "LETHAL HITS"),
                new Buff("Keyword 2", EffectType.WEAPON, Tag.WEAPON_KEYWORD, 0, "Bolt Rifle", "SUSTAINED HITS 1"),
                new Buff("Keyword 3", EffectType.WEAPON, Tag.WEAPON_KEYWORD, 0, "Bolt Rifle", "LETHAL HITS")
        );

        EffectService service = new EffectService();
        EditorRuleModifiers mods = service.resolveAttackModifiers(effects, weapon);

        assertTrue(mods.extraWeaponKeywords().contains("LETHAL HITS"));
        assertTrue(mods.extraWeaponKeywords().contains("SUSTAINED HITS 1"));
    }

    @Test
    void resolveAttackModifiersShouldChooseStrongerReroll() {
        WeaponProfile weapon = makeWeapon("Bolt Rifle");

        List<Effect> effects = List.of(
                new Buff("Hit Reroll Ones", EffectType.WEAPON, Tag.HIT_REROLL, 1, "Bolt Rifle"),
                new Buff("Hit Reroll Fails", EffectType.WEAPON, Tag.HIT_REROLL, 2, "Bolt Rifle")
        );

        EffectService service = new EffectService();
        EditorRuleModifiers mods = service.resolveAttackModifiers(effects, weapon);

        assertEquals(EditorRerollType.FAILS, mods.hitReroll());
    }
}