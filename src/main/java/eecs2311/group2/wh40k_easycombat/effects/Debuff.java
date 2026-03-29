package eecs2311.group2.wh40k_easycombat.effects;

import eecs2311.group2.wh40k_easycombat.model.instance.ArmyInstance;
import eecs2311.group2.wh40k_easycombat.model.instance.UnitInstance;

public class Debuff extends Effect {

    public Debuff(String name, EffectType effectType, Tag tag, int value) {
        super(name, effectType, new EffectFeatures(tag, EffectFeatures.OperationType.DECREASE, value));
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

            default -> throw new UnsupportedOperationException("Debuff for tag " + getTag() + " is not supported yet.");
        }
    }

    @Override
    public void apply(ArmyInstance army) {
        if (army == null) {
            return;
        }

        switch (getTag()) {
            case UTILITY -> army.spendCp(getValue());
            case HEALTH, DAMAGE, MORALE -> {
                for (UnitInstance unit : army.getUnits()) {
                    apply(unit);
                }
            }

            default -> throw new UnsupportedOperationException("Army debuff for tag " + getTag() + " is not supported yet.");
        }
    }
}