package eecs2311.group2.wh40k_easycombat.effects;

import eecs2311.group2.wh40k_easycombat.model.instance.ArmyInstance;
import eecs2311.group2.wh40k_easycombat.model.instance.UnitInstance;

public class Buff extends Effect {

    public Buff(String name, EffectType effectType, Tag tag, int value) {
        super(name, effectType, new EffectFeatures(tag, EffectFeatures.OperationType.INCREASE, value));
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

            default -> throw new UnsupportedOperationException("Buff for tag " + getTag() + " is not supported yet.");
        }
    }

    @Override
    public void apply(ArmyInstance army) {
        if (army == null) {
            return;
        }

        switch (getTag()) {
            case UTILITY -> army.addCp(getValue());
            case HEALTH, MORALE -> {
                for (UnitInstance unit : army.getUnits()) {
                    apply(unit);
                }
            }

            default -> throw new UnsupportedOperationException("Army buff for tag " + getTag() + " is not supported yet.");
        }
    }
}