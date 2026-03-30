//Comments:
// Concrete implementation of Effect representing negative modifications.
//
// A Debuff weakens a unit or army by applying harmful changes such as:
// - dealing damage to units
// - applying battle shock (morale penalties)
// - disabling actions during a phase
// - reducing command points or victory points
//
// Similar to Buff, the behavior is controlled by the Tag, but instead of improving values, it applies negative effects.
//
// Debuff also contributes to combat calculations by producing negative modifiers, such as reducing hit chance or damage output.
//
// This class mirrors the structure of Buff but applies DECREASE operations, ensuring consistent design while handling the opposite behavior.

package eecs2311.group2.wh40k_easycombat.util.effects;

import eecs2311.group2.wh40k_easycombat.model.editor.EditorRerollType;
import eecs2311.group2.wh40k_easycombat.model.editor.EditorRuleModifiers;
import eecs2311.group2.wh40k_easycombat.model.instance.ArmyInstance;
import eecs2311.group2.wh40k_easycombat.model.instance.UnitInstance;
import eecs2311.group2.wh40k_easycombat.model.instance.WeaponProfile;

import java.util.List;

public class Debuff extends Effect {

    public Debuff(String name, EffectType effectType, Tag tag, int value) {
        super(name, effectType, new EffectFeatures(tag, EffectFeatures.OperationType.DECREASE, value));
    }

    public Debuff(String name, EffectType effectType, Tag tag, int value, String weaponName) {
        super(name, effectType, new EffectFeatures(tag, EffectFeatures.OperationType.DECREASE, value, weaponName));
    }

    public Debuff(String name, EffectType effectType, Tag tag, int value, String weaponName, String keywordText) {
        super(name, effectType, new EffectFeatures(tag, EffectFeatures.OperationType.DECREASE, value, weaponName, keywordText));
    }

    @Override
    public void apply(UnitInstance unit) {
        if (unit == null) {
            return;
        }

        switch (getTag()) {
            case HEALTH, DAMAGE -> dealDamageToUnit(unit, getValue());
            case MORALE -> unit.setBattleShocked(true);
            case UTILITY -> unit.setEligibleToFightThisPhase(false);
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
            case UTILITY -> army.spendCp(getValue());
            case VICTORY_POINTS -> army.setCurrentVp(army.getCurrentVp() - getValue());
            case HEALTH, DAMAGE, MORALE -> {
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
            case HIT_ROLL -> new EditorRuleModifiers(-getValue(), 0, 0, 0, 0, "", EditorRerollType.NONE, EditorRerollType.NONE, List.of(getName()), List.of());
            case WOUND_ROLL -> new EditorRuleModifiers(0, -getValue(), 0, 0, 0, "", EditorRerollType.NONE, EditorRerollType.NONE, List.of(getName()), List.of());
            case WEAPON_ATTACKS -> new EditorRuleModifiers(0, 0, -getValue(), 0, 0, "", EditorRerollType.NONE, EditorRerollType.NONE, List.of(getName()), List.of());
            case WEAPON_DAMAGE -> new EditorRuleModifiers(0, 0, 0, -getValue(), 0, "", EditorRerollType.NONE, EditorRerollType.NONE, List.of(getName()), List.of());
            case WEAPON_AP -> new EditorRuleModifiers(0, 0, 0, 0, -getValue(), "", EditorRerollType.NONE, EditorRerollType.NONE, List.of(getName()), List.of());

            default -> EditorRuleModifiers.none();
        };
    }
}
