package eecs2311.group2.wh40k_easycombat.util.effects;

import eecs2311.group2.wh40k_easycombat.model.instance.ArmyInstance;
import eecs2311.group2.wh40k_easycombat.model.instance.UnitInstance;
import eecs2311.group2.wh40k_easycombat.model.instance.UnitModelInstance;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.List;

public class EffectsTest {

    private UnitInstance makeUnitWithOneModel(int wounds) {
        UnitInstance unit = new UnitInstance("ds-1", "Test Unit");
        UnitModelInstance model = new UnitModelInstance("Test Model", "6", "4", "3+", String.valueOf(wounds), "7+", "1", "0");

        unit.addModel(model);
        return unit;
    }

    private UnitInstance makeUnitWithTwoModels(int woundsEach) {
        UnitInstance unit = new UnitInstance("ds-2", "Two Model Unit");

        UnitModelInstance model1 = new UnitModelInstance("Model 1", "6", "4", "3+", String.valueOf(woundsEach), "7+", "1", "0");
        UnitModelInstance model2 = new UnitModelInstance("Model 2", "6", "4", "3+", String.valueOf(woundsEach), "7+", "1", "0");

        unit.addModel(model1);
        unit.addModel(model2);
        return unit;
    }

    private ArmyInstance makeArmy() {
        return new ArmyInstance(1, "Test Army", "f1", "Faction", "det1");
    }

    @Test
    void buffHealthShouldHealAUnit() {
        UnitInstance unit = makeUnitWithOneModel(5);
        unit.getModels().get(0).takeDamage(3);

        Buff healBuff = new Buff("Med Kit", EffectType.HEAL, Tag.HEALTH, 2);
        healBuff.apply(unit);

        Assertions.assertEquals(4, unit.getModels().get(0).getCurrentHp());
    }

    @Test
    void healShouldNotGoAboveMaxHp() {
        UnitInstance unit = makeUnitWithOneModel(5);
        unit.getModels().get(0).takeDamage(1);

        Buff healBuff = new Buff("Med Kit", EffectType.HEAL, Tag.HEALTH, 10);
        healBuff.apply(unit);

        Assertions.assertEquals(5, unit.getModels().get(0).getCurrentHp());
    }

    @Test
    void debuffHealthShouldDamageAUnit() {
        UnitInstance unit = makeUnitWithOneModel(5);

        Debuff poison = new Debuff("Poison", EffectType.POISON, Tag.HEALTH, 2);
        poison.apply(unit);

        Assertions.assertEquals(3, unit.getModels().get(0).getCurrentHp());
    }

    @Test
    void debuffDamageShouldAlsoDamageAUnit() {
        UnitInstance unit = makeUnitWithOneModel(5);

        Debuff fire = new Debuff("Fire", EffectType.FIRE, Tag.DAMAGE, 3);
        fire.apply(unit);

        Assertions.assertEquals(2, unit.getModels().get(0).getCurrentHp());
    }

    @Test
    void damageShouldCarryIntoNextModelIfNeeded() {
        UnitInstance unit = makeUnitWithTwoModels(3);

        Debuff bigHit = new Debuff("Big Hit", EffectType.DAMAGE, Tag.HEALTH, 4);
        bigHit.apply(unit);

        Assertions.assertTrue(unit.getModels().get(0).isDestroyed());
        Assertions.assertEquals(2, unit.getModels().get(1).getCurrentHp());
    }

    @Test
    void moraleDebuffShouldBattleShockUnit() {
        UnitInstance unit = makeUnitWithOneModel(5);
        unit.setBattleShocked(false);

        Debuff fear = new Debuff("Fear Aura", EffectType.POISON, Tag.MORALE, 1);
        fear.apply(unit);

        Assertions.assertTrue(unit.isBattleShocked());
    }

    @Test
    void moraleBuffShouldRemoveBattleShockFromUnit() {
        UnitInstance unit = makeUnitWithOneModel(5);
        unit.setBattleShocked(true);

        Buff inspire = new Buff("Inspiring Presence", EffectType.HEAL, Tag.MORALE, 1);
        inspire.apply(unit);

        Assertions.assertFalse(unit.isBattleShocked());
    }

    @Test
    void utilityBuffShouldMakeUnitEligibleToFight() {
        UnitInstance unit = makeUnitWithOneModel(5);
        unit.setEligibleToFightThisPhase(false);

        Buff fightBuff = new Buff("Battle Focus", EffectType.SPEED, Tag.UTILITY, 1);
        fightBuff.apply(unit);

        Assertions.assertTrue(unit.isEligibleToFightThisPhase());
    }

    @Test
    void utilityDebuffShouldMakeUnitNotEligibleToFight() {
        UnitInstance unit = makeUnitWithOneModel(5);
        unit.setEligibleToFightThisPhase(true);

        Debuff stun = new Debuff("Stun", EffectType.SLOWNESS, Tag.UTILITY, 1);
        stun.apply(unit);

        Assertions.assertFalse(unit.isEligibleToFightThisPhase());
    }

    @Test
    void utilityBuffShouldAddCpToArmy() {
        ArmyInstance army = makeArmy();
        army.setCurrentCp(1);

        Buff cpBuff = new Buff("Command Surge", EffectType.HEAL, Tag.UTILITY, 2);
        cpBuff.apply(army);

        Assertions.assertEquals(3, army.getCurrentCp());
    }

    @Test
    void utilityDebuffShouldSpendCpFromArmy() {
        ArmyInstance army = makeArmy();
        army.setCurrentCp(4);

        Debuff cpLoss = new Debuff("Disruption", EffectType.POISON, Tag.UTILITY, 3);
        cpLoss.apply(army);

        Assertions.assertEquals(1, army.getCurrentCp());
    }

    @Test
    void armyMoraleDebuffShouldBattleShockAllUnits() {
        ArmyInstance army = makeArmy();
        UnitInstance unit1 = makeUnitWithOneModel(5);
        UnitInstance unit2 = makeUnitWithOneModel(5);

        army.addUnit(unit1);
        army.addUnit(unit2);

        Debuff fear = new Debuff("Fear Wave", EffectType.POISON, Tag.MORALE, 1);
        fear.apply(army);

        Assertions.assertTrue(unit1.isBattleShocked());
        Assertions.assertTrue(unit2.isBattleShocked());
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

        Assertions.assertEquals(4, unit1.getModels().get(0).getCurrentHp());
        Assertions.assertEquals(5, unit2.getModels().get(0).getCurrentHp());
    }

    @Test
    void decoderShouldCreateBuffCorrectly() {
        Effect effect = Decoder.decodeEffect("buff", "Heal", "heal", "health", 3);

        Assertions.assertNotNull(effect);
        Assertions.assertTrue(effect instanceof Buff);
        Assertions.assertEquals("Heal", effect.getName());
        Assertions.assertEquals(EffectType.HEAL, effect.getEffectType());
        Assertions.assertEquals(Tag.HEALTH, effect.getTag());
        Assertions.assertEquals(3, effect.getValue());
    }

    @Test
    void decoderShouldCreateDebuffCorrectly() {
        Effect effect = Decoder.decodeEffect("debuff", "Poison", "poison", "health", 2);

        Assertions.assertNotNull(effect);
        Assertions.assertTrue(effect instanceof Debuff);
        Assertions.assertEquals("Poison", effect.getName());
        Assertions.assertEquals(EffectType.POISON, effect.getEffectType());
        Assertions.assertEquals(Tag.HEALTH, effect.getTag());
        Assertions.assertEquals(2, effect.getValue());
    }

    @Test
    void decoderShouldThrowForInvalidEffectType() {
        Assertions.assertThrows(IllegalArgumentException.class, () -> Decoder.decodeEffect("buff", "Bad", "not_real", "health", 1));
    }

    @Test
    void decoderShouldThrowForInvalidTag() {
        Assertions.assertThrows(IllegalArgumentException.class, () -> Decoder.decodeEffect("buff", "Bad", "heal", "not_real", 1));
    }

    @Test
    void decoderShouldThrowForInvalidEffectClass() {
        Assertions.assertThrows(IllegalArgumentException.class, () -> Decoder.decodeEffect("somethingElse", "Bad", "heal", "health", 1));
    }

    @Test
    void effectServiceShouldApplySingleEffectToUnit() {
        UnitInstance unit = makeUnitWithOneModel(5);
        unit.getModels().get(0).takeDamage(2);

        EffectService service = new EffectService();
        Effect heal = new Buff("Heal", EffectType.HEAL, Tag.HEALTH, 1);

        service.applyEffectToUnit(heal, unit);

        Assertions.assertEquals(4, unit.getModels().get(0).getCurrentHp());
    }

    @Test
    void effectServiceShouldApplyListOfEffectsToUnit() {
        UnitInstance unit = makeUnitWithOneModel(5);

        EffectService service = new EffectService();
        List<Effect> effects = List.of(new Debuff("Poison", EffectType.POISON, Tag.HEALTH, 2), new Buff("Small Heal", EffectType.HEAL, Tag.HEALTH, 1));

        service.applyEffectsToUnit(effects, unit);

        Assertions.assertEquals(4, unit.getModels().get(0).getCurrentHp());
    }

    @Test
    void effectServiceShouldDecodeAndApplyToArmy() {
        ArmyInstance army = makeArmy();
        army.setCurrentCp(0);

        EffectService service = new EffectService();
        service.decodeAndApplyToArmy("buff", "Command Gain", "heal", "utility", 2, army);

        Assertions.assertEquals(2, army.getCurrentCp());
    }
}