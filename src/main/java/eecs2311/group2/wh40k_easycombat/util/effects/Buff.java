package eecs2311.group2.wh40k_easycombat.util.effects;

import eecs2311.group2.wh40k_easycombat.model.editor.EditorRerollType;
import eecs2311.group2.wh40k_easycombat.model.editor.EditorRuleModifiers;
import eecs2311.group2.wh40k_easycombat.model.instance.ArmyInstance;
import eecs2311.group2.wh40k_easycombat.model.instance.UnitInstance;
import eecs2311.group2.wh40k_easycombat.model.instance.WeaponProfile;

import java.util.List;

public class Buff extends Effect {

    public Buff(String name, EffectType effectType, Tag tag, int value) {
        super(name, effectType, new EffectFeatures(tag, EffectFeatures.OperationType.INCREASE, value));
    }

    public Buff(String name, EffectType effectType, Tag tag, int value, String weaponName) {
        super(name, effectType, new EffectFeatures(tag, EffectFeatures.OperationType.INCREASE, value, weaponName));
    }

    public Buff(String name, EffectType effectType, Tag tag, int value, String weaponName, String keywordText) {
        super(name, effectType, new EffectFeatures(tag, EffectFeatures.OperationType.INCREASE, value, weaponName, keywordText));
    }

    @Override
    public void apply(UnitInstance unit) {
        if (unit == null) {
            return;
        }

        switch (getTag()) {
            case HEALTH -> healUnit(unit, getValue());
            case MORALE -> unit.setBattleShocked(false);
            case UTILITY -> unit.setEligibleToFightThisPhase(true);
            default -> {
            }
        }
    }

    @Override
    public void apply(ArmyInstance army) {
        if (army == null) {
            return;
        }

        switch (getTag()) {
            case UTILITY -> army.addCp(getValue());
            case VICTORY_POINTS -> army.addVp(getValue());
            case HEALTH, MORALE -> {
                for (UnitInstance unit : army.getUnits()) {
                    apply(unit);
                }
            }

            default -> {
            }
        }
    }

    @Override
    public EditorRuleModifiers toAttackModifiers(WeaponProfile weapon) {
        if (!matchesWeapon(weapon)) {
            return EditorRuleModifiers.none();
        }

        return switch (getTag()) {
            case HIT_ROLL -> new EditorRuleModifiers(getValue(), 0, 0, 0, 0, "", EditorRerollType.NONE, EditorRerollType.NONE, List.of(getName()), List.of());
            case WOUND_ROLL -> new EditorRuleModifiers(0, getValue(), 0, 0, 0, "", EditorRerollType.NONE, EditorRerollType.NONE, List.of(getName()), List.of());
            case WEAPON_ATTACKS -> new EditorRuleModifiers(0, 0, getValue(), 0, 0, "", EditorRerollType.NONE, EditorRerollType.NONE, List.of(getName()), List.of());
            case WEAPON_DAMAGE -> new EditorRuleModifiers(0, 0, 0, getValue(), 0, "", EditorRerollType.NONE, EditorRerollType.NONE, List.of(getName()), List.of());
            case WEAPON_AP -> new EditorRuleModifiers(0, 0, 0, 0, getValue(), "", EditorRerollType.NONE, EditorRerollType.NONE, List.of(getName()), List.of());
            case WEAPON_KEYWORD -> new EditorRuleModifiers(0, 0, 0, 0, 0, getKeywordText(), EditorRerollType.NONE, EditorRerollType.NONE, List.of(getName()), List.of());
            case HIT_REROLL -> new EditorRuleModifiers(0, 0, 0, 0, 0, "", getValue() >= 2 ? EditorRerollType.FAILS : EditorRerollType.ONES, EditorRerollType.NONE, List.of(getName()), List.of());
            case WOUND_REROLL -> new EditorRuleModifiers(0, 0, 0, 0, 0, "", EditorRerollType.NONE, getValue() >= 2 ? EditorRerollType.FAILS : EditorRerollType.ONES, List.of(getName()), List.of());

            default -> EditorRuleModifiers.none();
        };
    }
}