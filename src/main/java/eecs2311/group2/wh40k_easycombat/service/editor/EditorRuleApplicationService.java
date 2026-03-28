package eecs2311.group2.wh40k_easycombat.service.editor;

import eecs2311.group2.wh40k_easycombat.model.editor.EditorActiveEffect;
import eecs2311.group2.wh40k_easycombat.model.editor.EditorRerollType;
import eecs2311.group2.wh40k_easycombat.model.editor.EditorRuleDefinition;
import eecs2311.group2.wh40k_easycombat.model.editor.EditorRuleModifiers;
import eecs2311.group2.wh40k_easycombat.model.editor.EditorRulePhase;
import eecs2311.group2.wh40k_easycombat.model.editor.EditorRuleTargetRole;
import eecs2311.group2.wh40k_easycombat.model.instance.UnitInstance;
import eecs2311.group2.wh40k_easycombat.model.instance.WeaponProfile;
import eecs2311.group2.wh40k_easycombat.service.autobattle.AttackKeywordContext;
import eecs2311.group2.wh40k_easycombat.service.autobattle.AutoBattleMode;
import eecs2311.group2.wh40k_easycombat.service.vm.RuleContext;
import eecs2311.group2.wh40k_easycombat.service.vm.RuleResult;
import eecs2311.group2.wh40k_easycombat.service.vm.VMService;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

public class EditorRuleApplicationService {
    private final RuleEditorService ruleEditorService = RuleEditorService.getInstance();
    private final EditorEffectRuntimeService runtimeService = EditorEffectRuntimeService.getInstance();

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
        List<String> errors = new ArrayList<>();

        for (EditorRuleDefinition rule : ruleEditorService.getRules()) {
            if (!matchesStaticRule(rule, mode, attacker, defender, weapon)) {
                continue;
            }

            EditorRuleModifiers resolved = executeVmRule(rule, rule.getName(), mode, attacker, defender, weapon, context);
            hitModifier += resolved.hitModifier();
            woundModifier += resolved.woundModifier();
            attacksModifier += resolved.attacksModifier();
            damageModifier += resolved.damageModifier();
            apModifier += resolved.apModifier();
            mergeKeywords(grantedKeywords, resolved.extraWeaponKeywords());
            hitReroll = EditorRerollType.stronger(hitReroll, resolved.hitReroll());
            woundReroll = EditorRerollType.stronger(woundReroll, resolved.woundReroll());
            appliedRules.addAll(resolved.appliedRuleNames());
            errors.addAll(resolved.errorMessages());
        }

        for (EditorActiveEffect effect : runtimeService.getActiveEffects()) {
            if (!matchesActiveEffect(effect, mode, attacker, defender, weapon)) {
                continue;
            }

            EditorRuleDefinition rule = effect.rule();
            EditorRuleModifiers resolved = executeVmRule(
                    rule,
                    effect.displayName(),
                    mode,
                    attacker,
                    defender,
                    weapon,
                    context
            );

            hitModifier += resolved.hitModifier();
            woundModifier += resolved.woundModifier();
            attacksModifier += resolved.attacksModifier();
            damageModifier += resolved.damageModifier();
            apModifier += resolved.apModifier();
            mergeKeywords(grantedKeywords, resolved.extraWeaponKeywords());
            hitReroll = EditorRerollType.stronger(hitReroll, resolved.hitReroll());
            woundReroll = EditorRerollType.stronger(woundReroll, resolved.woundReroll());
            appliedRules.addAll(resolved.appliedRuleNames());
            errors.addAll(resolved.errorMessages());
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
                appliedRules,
                errors
        );
    }

    public List<EditorRuleDefinition> matchingOptionalRules(
            AutoBattleMode mode,
            UnitInstance attacker,
            UnitInstance defender,
            WeaponProfile weapon
    ) {
        if (mode == null
                || attacker == null
                || defender == null
                || weapon == null
                || !ruleEditorService.isAutoApplyEnabled()) {
            return List.of();
        }

        List<EditorRuleDefinition> matches = new ArrayList<>();
        for (EditorRuleDefinition rule : ruleEditorService.getRules()) {
            if (matchesOptionalRule(rule, mode, attacker, defender, weapon)) {
                matches.add(rule.copy());
            }
        }

        matches.sort(Comparator.comparing(rule -> safe(rule.getName()).toLowerCase(Locale.ROOT)));
        return List.copyOf(matches);
    }

    private EditorRuleModifiers executeVmRule(
            EditorRuleDefinition rule,
            String displayName,
            AutoBattleMode mode,
            UnitInstance attacker,
            UnitInstance defender,
            WeaponProfile weapon,
            AttackKeywordContext context
    ) {
        if (rule == null) {
            return EditorRuleModifiers.none();
        }

        ensureVmRuleLoaded(rule);

        RuleContext vmContext = buildVmContext(mode, attacker, defender, weapon, context);
        RuleResult result = VMService.run(rule.vmRuleName(), vmContext);
        if (!result.isSuccess()) {
            return new EditorRuleModifiers(
                    0,
                    0,
                    0,
                    0,
                    0,
                    "",
                    EditorRerollType.NONE,
                    EditorRerollType.NONE,
                    List.of(),
                    List.of(safe(displayName, "Custom Rule") + ": " + result.getError())
            );
        }

        EditorRuleModifiers modifiers = new EditorRuleModifiers(
                readInt(result, "hit_modifier"),
                readInt(result, "wound_modifier"),
                readInt(result, "attacks_modifier"),
                readInt(result, "damage_modifier"),
                readInt(result, "ap_modifier"),
                readString(result, "extra_weapon_keywords"),
                readReroll(result, "hit_reroll"),
                readReroll(result, "wound_reroll"),
                List.of(),
                List.of()
        );

        if (!modifiers.hasAnyEffect()) {
            return modifiers;
        }

        return new EditorRuleModifiers(
                modifiers.hitModifier(),
                modifiers.woundModifier(),
                modifiers.attacksModifier(),
                modifiers.damageModifier(),
                modifiers.apModifier(),
                modifiers.extraWeaponKeywords(),
                modifiers.hitReroll(),
                modifiers.woundReroll(),
                List.of(safe(displayName, "Custom Rule")),
                List.of()
        );
    }

    private void ensureVmRuleLoaded(EditorRuleDefinition rule) {
        if (rule == null) {
            return;
        }
        if (!VMService.getLoadedRules().contains(rule.vmRuleName())) {
            VMService.loadRule(rule.vmRuleName(), rule.getDslScript());
        }
    }

    private RuleContext buildVmContext(
            AutoBattleMode mode,
            UnitInstance attacker,
            UnitInstance defender,
            WeaponProfile weapon,
            AttackKeywordContext context
    ) {
        RuleContext vmContext = new RuleContext();

        vmContext.set("mode", mode.name());
        vmContext.set("within_half_range", context.withinHalfRange());
        vmContext.set("remained_stationary", context.remainedStationary());
        vmContext.set("advanced_this_turn", context.advancedThisTurn());
        vmContext.set("fell_back_this_turn", context.fellBackThisTurn());
        vmContext.set("charged_this_turn", context.bearerChargedThisTurn());
        vmContext.set("attacker_can_fight", context.attackerIsEligibleToFight());
        vmContext.set("target_has_cover", context.targetHasBenefitOfCover());
        vmContext.set("blast_is_legal", context.blastIsLegal());
        vmContext.set("target_is_infantry", context.targetIsInfantry());
        vmContext.set("target_is_vehicle", context.targetIsVehicle());
        vmContext.set("target_is_monster", context.targetIsMonster());
        vmContext.set("target_is_character", context.targetIsCharacter());
        vmContext.set("target_is_psyker", context.targetIsPsyker());
        vmContext.set("weapon_bearer_count", context.attackingWeaponBearerCount());
        vmContext.set("weapon_is_melee", weapon.melee());

        vmContext.set("attacker", buildUnitMap(attacker));
        vmContext.set("defender", buildUnitMap(defender));
        vmContext.set("weapon", buildWeaponMap(weapon));

        vmContext.set("hit_modifier", 0);
        vmContext.set("wound_modifier", 0);
        vmContext.set("attacks_modifier", 0);
        vmContext.set("damage_modifier", 0);
        vmContext.set("ap_modifier", 0);
        vmContext.set("hit_reroll", 0);
        vmContext.set("wound_reroll", 0);
        vmContext.set("extra_weapon_keywords", "");
        return vmContext;
    }

    private Map<String, Object> buildUnitMap(UnitInstance unit) {
        Map<String, Object> values = new LinkedHashMap<>();
        values.put("name", safe(unit == null ? "" : unit.getUnitName()));
        values.put("faction_name", safe(unit == null ? "" : unit.getFactionName()));
        values.put("detachment_name", safe(unit == null ? "" : unit.getDetachmentName()));
        values.put("alive_models", unit == null ? 0 : unit.getAliveModelCount());
        values.put("total_models", unit == null ? 0 : unit.getModelCount());
        values.put("current_oc", unit == null ? 0 : unit.getCurrentOc());
        values.put("battle_shocked", unit != null && unit.isBattleShocked());
        values.put("charged_this_turn", unit != null && unit.hasChargedThisTurn());
        values.put("fights_first", unit != null && unit.hasFightsFirst());
        values.put("below_half_strength", unit != null && unit.isBelowHalfStrength());
        return values;
    }

    private Map<String, Object> buildWeaponMap(WeaponProfile weapon) {
        Map<String, Object> values = new LinkedHashMap<>();
        values.put("name", safe(weapon == null ? "" : weapon.name()));
        values.put("description", safe(weapon == null ? "" : weapon.description()));
        values.put("count", weapon == null ? 0 : weapon.count());
        values.put("melee", weapon != null && weapon.melee());
        values.put("range", safe(weapon == null ? "" : weapon.range()));
        values.put("attacks", safe(weapon == null ? "" : weapon.a()));
        values.put("skill", safe(weapon == null ? "" : weapon.skill()));
        values.put("strength", safe(weapon == null ? "" : weapon.s()));
        values.put("ap", safe(weapon == null ? "" : weapon.ap()));
        values.put("damage", safe(weapon == null ? "" : weapon.d()));
        values.put("strength_value", parseSignedInt(weapon == null ? "" : weapon.s()));
        values.put("ap_value", parseSignedInt(weapon == null ? "" : weapon.ap()));
        values.put("damage_value", parseSignedInt(weapon == null ? "" : weapon.d()));
        return values;
    }

    private boolean matchesStaticRule(
            EditorRuleDefinition rule,
            AutoBattleMode mode,
            UnitInstance attacker,
            UnitInstance defender,
            WeaponProfile weapon
    ) {
        if (rule == null || !rule.isEnabled()) {
            return false;
        }
        if (rule.isOptionalActivation()) {
            return false;
        }
        if (!safe(rule.getTriggeringStratagemNameContains()).isBlank()) {
            return false;
        }
        return matchesCore(rule, mode, attacker, defender, weapon);
    }

    private boolean matchesOptionalRule(
            EditorRuleDefinition rule,
            AutoBattleMode mode,
            UnitInstance attacker,
            UnitInstance defender,
            WeaponProfile weapon
    ) {
        if (rule == null || !rule.isEnabled() || !rule.isOptionalActivation()) {
            return false;
        }
        if (!safe(rule.getTriggeringStratagemNameContains()).isBlank()) {
            return false;
        }
        return matchesCore(rule, mode, attacker, defender, weapon);
    }

    private boolean matchesActiveEffect(
            EditorActiveEffect effect,
            AutoBattleMode mode,
            UnitInstance attacker,
            UnitInstance defender,
            WeaponProfile weapon
    ) {
        if (effect == null || effect.rule() == null) {
            return false;
        }
        if (!activeEffectTargetMatches(effect, attacker, defender)) {
            return false;
        }
        return matchesCore(effect.rule(), mode, attacker, defender, weapon);
    }

    private boolean matchesCore(
            EditorRuleDefinition rule,
            AutoBattleMode mode,
            UnitInstance attacker,
            UnitInstance defender,
            WeaponProfile weapon
    ) {
        if (rule == null || !rule.isEnabled()) {
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
        if (!containsIgnoreCase(weapon.name(), rule.getWeaponNameContains())) {
            return false;
        }
        if (!keywordMatches(attacker, rule.getAttackerKeyword())) {
            return false;
        }
        if (!keywordMatches(defender, rule.getDefenderKeyword())) {
            return false;
        }
        if (!abilityMatches(attacker, rule.getAttackerAbilityNameContains())) {
            return false;
        }
        if (!abilityMatches(defender, rule.getDefenderAbilityNameContains())) {
            return false;
        }
        if (!factionAbilityMatches(attacker, rule.getAttackerFactionAbilityNameContains())) {
            return false;
        }
        if (!factionAbilityMatches(defender, rule.getDefenderFactionAbilityNameContains())) {
            return false;
        }
        if (!detachmentAbilityMatches(attacker, rule.getAttackerDetachmentAbilityNameContains())) {
            return false;
        }
        if (!detachmentAbilityMatches(defender, rule.getDefenderDetachmentAbilityNameContains())) {
            return false;
        }
        if (!containsIgnoreCase(attacker.getFactionName(), rule.getAttackerFactionNameContains())) {
            return false;
        }
        if (!containsIgnoreCase(defender.getFactionName(), rule.getDefenderFactionNameContains())) {
            return false;
        }
        if (!containsIgnoreCase(attacker.getDetachmentName(), rule.getAttackerDetachmentNameContains())) {
            return false;
        }
        if (!containsIgnoreCase(defender.getDetachmentName(), rule.getDefenderDetachmentNameContains())) {
            return false;
        }
        if (!enhancementMatches(attacker, rule.getAttackerEnhancementNameContains())) {
            return false;
        }
        return enhancementMatches(defender, rule.getDefenderEnhancementNameContains());
    }

    private boolean activeEffectTargetMatches(
            EditorActiveEffect effect,
            UnitInstance attacker,
            UnitInstance defender
    ) {
        String targetUnitId = safe(effect.targetUnitId());
        if (targetUnitId.isBlank()) {
            return false;
        }

        EditorRuleTargetRole targetRole = effect.targetRole();
        boolean matchesAttacker = attacker != null && targetUnitId.equals(attacker.getInstanceId());
        boolean matchesDefender = defender != null && targetUnitId.equals(defender.getInstanceId());

        if (targetRole == EditorRuleTargetRole.ATTACKER) {
            return matchesAttacker;
        }
        if (targetRole == EditorRuleTargetRole.DEFENDER) {
            return matchesDefender;
        }
        return matchesAttacker || matchesDefender;
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

    private boolean abilityMatches(UnitInstance unit, String abilityFilter) {
        if (abilityFilter == null || abilityFilter.isBlank()) {
            return true;
        }
        if (unit == null) {
            return false;
        }

        return unit.hasAbilityNameContaining(abilityFilter)
                || unit.hasAbilityNamed(abilityFilter)
                || unit.hasAbilityTextContaining(abilityFilter);
    }

    private boolean factionAbilityMatches(UnitInstance unit, String abilityFilter) {
        if (abilityFilter == null || abilityFilter.isBlank()) {
            return true;
        }
        return unit != null && unit.hasFactionAbilityNameContaining(abilityFilter);
    }

    private boolean detachmentAbilityMatches(UnitInstance unit, String abilityFilter) {
        if (abilityFilter == null || abilityFilter.isBlank()) {
            return true;
        }
        return unit != null && unit.hasDetachmentAbilityNameContaining(abilityFilter);
    }

    private boolean enhancementMatches(UnitInstance unit, String enhancementFilter) {
        if (enhancementFilter == null || enhancementFilter.isBlank()) {
            return true;
        }
        return unit != null && unit.hasEnhancementNameContaining(enhancementFilter);
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

    private int readInt(RuleResult result, String key) {
        Object value = result == null ? null : result.getValue(key);
        if (value instanceof Number number) {
            return number.intValue();
        }
        if (value instanceof Boolean bool) {
            return bool ? 1 : 0;
        }
        if (value instanceof String string) {
            try {
                return Integer.parseInt(string.trim());
            } catch (NumberFormatException ignored) {
            }
        }
        return 0;
    }

    private String readString(RuleResult result, String key) {
        Object value = result == null ? null : result.getValue(key);
        return value == null ? "" : value.toString().trim();
    }

    private EditorRerollType readReroll(RuleResult result, String key) {
        Object directValue = result == null ? null : result.getValue(key);
        if (directValue instanceof Number number) {
            return rerollFromCode(number.intValue());
        }
        if (directValue instanceof String string) {
            String normalized = string.trim().toUpperCase(Locale.ROOT);
            if (normalized.equals("ONES") || normalized.equals("REROLL_ONES") || normalized.equals("1")) {
                return EditorRerollType.ONES;
            }
            if (normalized.equals("FAILS") || normalized.equals("REROLL_FAILS") || normalized.equals("2")) {
                return EditorRerollType.FAILS;
            }
        }
        return EditorRerollType.NONE;
    }

    private EditorRerollType rerollFromCode(int code) {
        return switch (code) {
            case 1 -> EditorRerollType.ONES;
            case 2 -> EditorRerollType.FAILS;
            default -> EditorRerollType.NONE;
        };
    }

    private int parseSignedInt(String raw) {
        String normalized = safe(raw).replaceAll("[^0-9-]", "");
        if (normalized.isBlank() || "-".equals(normalized)) {
            return 0;
        }
        try {
            return Integer.parseInt(normalized);
        } catch (NumberFormatException ignored) {
            return 0;
        }
    }

    private String safe(String value) {
        return value == null ? "" : value.trim();
    }

    private String safe(String value, String fallback) {
        String normalized = safe(value);
        return normalized.isBlank() ? fallback : normalized;
    }
}
