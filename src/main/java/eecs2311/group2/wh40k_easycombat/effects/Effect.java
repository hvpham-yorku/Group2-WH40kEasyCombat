package eecs2311.group2.wh40k_easycombat.effects;

import eecs2311.group2.wh40k_easycombat.model.instance.ArmyInstance;
import eecs2311.group2.wh40k_easycombat.model.instance.UnitInstance;
import eecs2311.group2.wh40k_easycombat.model.instance.UnitModelInstance;

public abstract class Effect {
    private String name;
    private EffectType effectType;
    private EffectFeatures features;

    public Effect(String name, EffectType effectType, EffectFeatures features) {
        this.name = name;
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

    public void setName(String name) {
        this.name = name;
    }

    public void setEffectType(EffectType effectType) {
        this.effectType = effectType;
    }

    public void setFeatures(EffectFeatures features) {
        this.features = features;
    }

    public abstract void apply(UnitInstance unit);

    public abstract void apply(ArmyInstance army);

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

            int maxHp = model.getMaxHp();
            int currentHp = model.getCurrentHp();

            if (currentHp >= maxHp) {
                continue;
            }

            int missing = maxHp - currentHp;
            int healAmount = Math.min(missing, remaining);

            model.setCurrentHp(currentHp + healAmount);
            remaining -= healAmount;

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