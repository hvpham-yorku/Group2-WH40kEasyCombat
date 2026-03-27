package eecs2311.group2.wh40k_easycombat.service.editor;

import eecs2311.group2.wh40k_easycombat.model.editor.EditorRerollType;
import eecs2311.group2.wh40k_easycombat.model.editor.EditorRuleDefinition;
import eecs2311.group2.wh40k_easycombat.model.editor.EditorRuleModifiers;
import eecs2311.group2.wh40k_easycombat.model.editor.EditorRulePhase;
import eecs2311.group2.wh40k_easycombat.model.instance.UnitInstance;
import eecs2311.group2.wh40k_easycombat.model.instance.WeaponProfile;
import eecs2311.group2.wh40k_easycombat.service.autobattle.AttackKeywordContext;
import eecs2311.group2.wh40k_easycombat.service.autobattle.AutoBattleMode;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

public class EditorRuleApplicationService {
    private final RuleEditorService ruleEditorService = RuleEditorService.getInstance();

    public EditorRuleModifiers resolveForAttack(
            AutoBattleMode mode,
            UnitInstance attacker,
            UnitInstance defender,
            WeaponProfile weapon,
            AttackKeywordContext context
    ) {
        if (mode == null
                || attacker == null
                || defender == null
                || weapon == null
                || context == null
                || !ruleEditorService.isAutoApplyEnabled()) {
            return EditorRuleModifiers.none();
        }

        int hitModifier = 0;
        int woundModifier = 0;
        int attacksModifier = 0;
        int damageModifier = 0;
        int apModifier = 0;
        Set<String> grantedKeywords = new LinkedHashSet<>();
        EditorRerollType hitReroll = EditorRerollType.NONE;
        EditorRerollType woundReroll = EditorRerollType.NONE;
        List<String> appliedRules = new ArrayList<>();

        for (EditorRuleDefinition rule : ruleEditorService.getRules()) {
            if (!matches(rule, mode, attacker, defender, weapon, context)) {
                continue;
            }

            hitModifier += rule.getHitModifier();
            woundModifier += rule.getWoundModifier();
            attacksModifier += rule.getAttacksModifier();
            damageModifier += rule.getDamageModifier();
            apModifier += rule.getApModifier();
            mergeKeywords(grantedKeywords, rule.getExtraWeaponKeywords());
            hitReroll = EditorRerollType.stronger(hitReroll, rule.getHitReroll());
            woundReroll = EditorRerollType.stronger(woundReroll, rule.getWoundReroll());
            appliedRules.add(rule.getName().isBlank() ? "Untitled Rule" : rule.getName());
        }

        return new EditorRuleModifiers(
                hitModifier,
                woundModifier,
                attacksModifier,
                damageModifier,
                apModifier,
                String.join(", ", grantedKeywords),
                hitReroll,
                woundReroll,
                appliedRules
        );
    }

    private boolean matches(
            EditorRuleDefinition rule,
            AutoBattleMode mode,
            UnitInstance attacker,
            UnitInstance defender,
            WeaponProfile weapon,
            AttackKeywordContext context
    ) {
        if (rule == null || !rule.isEnabled()) {
            return false;
        }
        if (rule.getActivationMode() != eecs2311.group2.wh40k_easycombat.model.editor.EditorRuleActivationMode.PASSIVE) {
            return false;
        }
        if (!phaseMatches(rule.getPhase(), mode)) {
            return false;
        }
        if (!rule.getAttackType().matches(weapon.melee())) {
            return false;
        }
        if (!containsIgnoreCase(attacker.getUnitName(), rule.getAttackerUnitNameContains())) {
            return false;
        }
        if (!containsIgnoreCase(defender.getUnitName(), rule.getDefenderUnitNameContains())) {
            return false;
        }
        if (!keywordMatches(attacker, rule.getAttackerKeyword())) {
            return false;
        }
        if (!keywordMatches(defender, rule.getDefenderKeyword())) {
            return false;
        }
        if (rule.isRequireWithinHalfRange() && !context.withinHalfRange()) {
            return false;
        }
        if (rule.isRequireRemainedStationary() && !context.remainedStationary()) {
            return false;
        }
        if (rule.isRequireChargedThisTurn() && !context.bearerChargedThisTurn()) {
            return false;
        }
        if (rule.isRequireTargetHasCover() && !context.targetHasBenefitOfCover()) {
            return false;
        }
        if (rule.isRequireTargetInfantry() && !context.targetIsInfantry()) {
            return false;
        }
        if (rule.isRequireTargetVehicle() && !context.targetIsVehicle()) {
            return false;
        }
        if (rule.isRequireTargetMonster() && !context.targetIsMonster()) {
            return false;
        }
        if (rule.isRequireTargetCharacter() && !context.targetIsCharacter()) {
            return false;
        }
        return !rule.isRequireTargetPsyker() || context.targetIsPsyker();
    }

    private boolean phaseMatches(EditorRulePhase phase, AutoBattleMode mode) {
        if (phase == null || phase == EditorRulePhase.ANY) {
            return true;
        }
        return switch (mode) {
            case SHOOTING -> phase == EditorRulePhase.SHOOTING;
            case REACTION_SHOOTING -> phase == EditorRulePhase.REACTION_SHOOTING;
            case FIGHT -> phase == EditorRulePhase.FIGHT;
        };
    }

    private boolean keywordMatches(UnitInstance unit, String keyword) {
        if (keyword == null || keyword.isBlank()) {
            return true;
        }
        return unit != null && unit.hasKeyword(keyword);
    }

    private boolean containsIgnoreCase(String source, String filter) {
        if (filter == null || filter.isBlank()) {
            return true;
        }
        if (source == null || source.isBlank()) {
            return false;
        }
        return source.toLowerCase(Locale.ROOT).contains(filter.trim().toLowerCase(Locale.ROOT));
    }

    private void mergeKeywords(Set<String> sink, String rawKeywords) {
        if (rawKeywords == null || rawKeywords.isBlank()) {
            return;
        }
        String[] tokens = rawKeywords.split(",");
        for (String token : tokens) {
            String normalized = token == null ? "" : token.trim();
            if (!normalized.isBlank()) {
                sink.add(normalized);
            }
        }
    }
}
