package eecs2311.group2.wh40k_easycombat.util.effects;

import eecs2311.group2.wh40k_easycombat.model.editor.EditorRuleModifiers;
import eecs2311.group2.wh40k_easycombat.model.instance.ArmyInstance;
import eecs2311.group2.wh40k_easycombat.model.instance.UnitInstance;
import eecs2311.group2.wh40k_easycombat.model.instance.UnitModelInstance;
import eecs2311.group2.wh40k_easycombat.model.instance.WeaponProfile;

public abstract class Effect {
    private String name;
    private EffectType effectType;
    private EffectFeatures features;

    public Effect(String name, EffectType effectType, EffectFeatures features) {
        this.name = name == null ? "" : name.trim();
        this.effectType = effectType;
        this.features = features;
    }

    public String getName() {
        return name;
    }

    public EffectType getEffectType() {
        return effectType;
    }

    public EffectFeatures getFeatures() {
        return features;
    }

    public Tag getTag() {
        return features.getTag();
    }

    public int getValue() {
        return features.getValue();
    }

    public String getWeaponName() {
        return features.getWeaponName();
    }

    public String getKeywordText() {
        return features.getKeywordText();
    }

    public void setName(String name) {
        this.name = name == null ? "" : name.trim();
    }

    public void setEffectType(EffectType effectType) {
        this.effectType = effectType;
    }

    public void setFeatures(EffectFeatures features) {
        this.features = features;
    }

    public abstract void apply(UnitInstance unit);

    public abstract void apply(ArmyInstance army);

    public EditorRuleModifiers toAttackModifiers(WeaponProfile weapon) {
        return EditorRuleModifiers.none();
    }

    protected boolean matchesWeapon(WeaponProfile weapon) {
        if (weapon == null) {
            return false;
        }

        String wantedWeapon = getWeaponName();
        if (wantedWeapon == null || wantedWeapon.isBlank()) {
            return true;
        }

        return weapon.name().equalsIgnoreCase(wantedWeapon.trim());
    }

    protected void dealDamageToUnit(UnitInstance unit, int amount) {
        if (unit == null || amount <= 0) {
            return;
        }

        int remaining = amount;

        for (UnitModelInstance model : unit.getModels()) {
            if (model == null || model.isDestroyed()) {
                continue;
            }

            int before = model.getCurrentHp();
            model.takeDamage(remaining);
            int damageApplied = before - model.getCurrentHp();
            remaining -= damageApplied;

            if (remaining <= 0) {
                break;
            }
        }
    }

    protected void healUnit(UnitInstance unit, int amount) {
        if (unit == null || amount <= 0) {
            return;
        }

        int remaining = amount;

        for (UnitModelInstance model : unit.getModels()) {
            if (model == null || model.isDestroyed()) {
                continue;
            }

            int before = model.getCurrentHp();
            model.heal(remaining);
            int healed = model.getCurrentHp() - before;
            remaining -= healed;

            if (remaining <= 0) {
                break;
            }
        }
    }

    @Override
    public String toString() {
        return "Effect{" + "name='" + name + '\'' + ", effectType=" + effectType + ", features=" + features + '}';
    }
}

// Comments: 
// Abstract base class that defines the core structure of all effects in the system.
//
// This class acts as a template for any effect (buff or debuff) that can be applied
// to either a UnitInstance or an ArmyInstance. It centralizes all shared data such as:
// - the effect name (for identification)
// - the effect type (category of effect)
// - the EffectFeatures object (which describes what the effect modifies and how)
//
// The class enforces a consistent interface through abstract apply() methods,
// which are implemented differently by subclasses (Buff and Debuff).
//
// It also provides reusable helper logic for common operations such as:
// - distributing damage across models in a unit
// - healing models within a unit
// - checking whether an effect applies to a specific weapon
//
// This design allows new types of effects to be added easily by extending this class
// without modifying existing logic (follows open/closed principle).
