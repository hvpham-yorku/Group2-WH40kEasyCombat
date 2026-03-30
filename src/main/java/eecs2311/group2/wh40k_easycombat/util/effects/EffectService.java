//Comments:
// Service class that manages the application and aggregation of effects.
//
// This class acts as the main controller for effect-related operations, separating execution logic from the Effect classes themselves.
//
// Responsibilities include:
// - applying a single effect to a unit or army
// - applying multiple effects in sequence
// - decoding and applying effects from raw input
// - combining multiple effects into final combat modifiers
//
// The resolveAttackModifiers method is especially important, as it:
// - aggregates all modifiers (hit, wound, damage, AP)
// - determines the strongest reroll rules
// - merges keyword effects
// - tracks applied rules and errors
//
// This design ensures that effect logic is centralized, reusable, and easy to maintain without duplicating code across the system.

package eecs2311.group2.wh40k_easycombat.util.effects;

import eecs2311.group2.wh40k_easycombat.model.editor.EditorRerollType;
import eecs2311.group2.wh40k_easycombat.model.editor.EditorRuleModifiers;
import eecs2311.group2.wh40k_easycombat.model.instance.ArmyInstance;
import eecs2311.group2.wh40k_easycombat.model.instance.UnitInstance;
import eecs2311.group2.wh40k_easycombat.model.instance.WeaponProfile;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

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

    public Effect decodeAndApplyToUnit(
            String effectClass,
            String name,
            String effectTypeText,
            String tagText,
            int value,
            UnitInstance unit
    ) {
        Effect effect = Decoder.decodeEffect(effectClass, name, effectTypeText, tagText, value);
        applyEffectToUnit(effect, unit);
        return effect;
    }

    public Effect decodeAndApplyToArmy(
            String effectClass,
            String name,
            String effectTypeText,
            String tagText,
            int value,
            ArmyInstance army
    ) {
        Effect effect = Decoder.decodeEffect(effectClass, name, effectTypeText, tagText, value);
        applyEffectToArmy(effect, army);
        return effect;
    }

    public EditorRuleModifiers resolveAttackModifiers(List<Effect> effects, WeaponProfile weapon) {
        if (effects == null || weapon == null) {
            return EditorRuleModifiers.none();
        }

        int hitModifier = 0;
        int woundModifier = 0;
        int attacksModifier = 0;
        int damageModifier = 0;
        int apModifier = 0;

        EditorRerollType hitReroll = EditorRerollType.NONE;
        EditorRerollType woundReroll = EditorRerollType.NONE;

        Set<String> keywords = new LinkedHashSet<>();
        List<String> appliedRuleNames = new ArrayList<>();
        List<String> errorMessages = new ArrayList<>();

        for (Effect effect : effects) {
            if (effect == null) {
                continue;
            }

            EditorRuleModifiers mods = effect.toAttackModifiers(weapon);
            if (mods == null || !mods.hasAnyEffect()) {
                continue;
            }

            hitModifier += mods.hitModifier();
            woundModifier += mods.woundModifier();
            attacksModifier += mods.attacksModifier();
            damageModifier += mods.damageModifier();
            apModifier += mods.apModifier();

            hitReroll = EditorRerollType.stronger(hitReroll, mods.hitReroll());
            woundReroll = EditorRerollType.stronger(woundReroll, mods.woundReroll());

            if (mods.extraWeaponKeywords() != null && !mods.extraWeaponKeywords().isBlank()) {
                String[] split = mods.extraWeaponKeywords().split(",");
                for (String keyword : split) {
                    String cleaned = keyword == null ? "" : keyword.trim();
                    if (!cleaned.isBlank()) {
                        keywords.add(cleaned);
                    }
                }
            }

            appliedRuleNames.addAll(mods.appliedRuleNames());
            errorMessages.addAll(mods.errorMessages());
        }

        String keywordString = String.join(", ", keywords);

        return new EditorRuleModifiers(
                hitModifier,
                woundModifier,
                attacksModifier,
                damageModifier,
                apModifier,
                keywordString,
                hitReroll,
                woundReroll,
                appliedRuleNames,
                errorMessages
        );
    }
}
