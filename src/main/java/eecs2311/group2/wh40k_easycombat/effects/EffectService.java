package eecs2311.group2.wh40k_easycombat.effects;

import eecs2311.group2.wh40k_easycombat.model.instance.ArmyInstance;
import eecs2311.group2.wh40k_easycombat.model.instance.UnitInstance;

import java.util.List;

public class EffectService {

    public void applyEffectToUnit(Effect effect, UnitInstance unit) {
        if (effect == null || unit == null) {
            return;
        }
        effect.apply(unit);
    }

    public void applyEffectToArmy(Effect effect, ArmyInstance army) {
        if (effect == null || army == null) {
            return;
        }
        effect.apply(army);
    }

    public void applyEffectsToUnit(List<Effect> effects, UnitInstance unit) {
        if (effects == null || unit == null) {
            return;
        }

        for (Effect effect : effects) {
            if (effect != null) {
                effect.apply(unit);
            }
        }
    }

    public void applyEffectsToArmy(List<Effect> effects, ArmyInstance army) {
        if (effects == null || army == null) {
            return;
        }

        for (Effect effect : effects) {
            if (effect != null) {
                effect.apply(army);
            }
        }
    }

    public Effect decodeAndApplyToUnit(String effectClass, String name, String effectTypeText, String tagText, int value, UnitInstance unit) {
        Effect effect = Decoder.decodeEffect(effectClass, name, effectTypeText, tagText, value);
        applyEffectToUnit(effect, unit);
        return effect;
    }

    public Effect decodeAndApplyToArmy(String effectClass, String name, String effectTypeText, String tagText, int value, ArmyInstance army) {
        Effect effect = Decoder.decodeEffect(effectClass, name, effectTypeText, tagText, value);
        applyEffectToArmy(effect, army);
        return effect;
    }
}