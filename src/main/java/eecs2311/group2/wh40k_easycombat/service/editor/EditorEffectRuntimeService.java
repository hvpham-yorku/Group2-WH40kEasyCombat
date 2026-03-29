package eecs2311.group2.wh40k_easycombat.service.editor;

import eecs2311.group2.wh40k_easycombat.model.editor.EditorActiveEffect;
import eecs2311.group2.wh40k_easycombat.model.editor.EditorRuleDefinition;
import eecs2311.group2.wh40k_easycombat.model.editor.EditorRuleDuration;
import eecs2311.group2.wh40k_easycombat.model.instance.Phase;
import eecs2311.group2.wh40k_easycombat.model.instance.Player;
import eecs2311.group2.wh40k_easycombat.model.instance.StratagemInstance;
import eecs2311.group2.wh40k_easycombat.model.instance.UnitInstance;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class EditorEffectRuntimeService {
    private static final EditorEffectRuntimeService INSTANCE = new EditorEffectRuntimeService();

    private final RuleEditorService ruleEditorService = RuleEditorService.getInstance();
    private final List<EditorActiveEffect> activeEffects = new ArrayList<>();

    public static EditorEffectRuntimeService getInstance() {
        return INSTANCE;
    }

    public synchronized List<EditorRuleDefinition> matchingStratagemRules(StratagemInstance stratagem) {
        if (stratagem == null
                || stratagem.name() == null
                || stratagem.name().isBlank()
                || !ruleEditorService.isAutoApplyEnabled()) {
            return List.of();
        }

        List<EditorRuleDefinition> result = new ArrayList<>();
        String stratagemName = stratagem.name().trim().toLowerCase(Locale.ROOT);

        for (EditorRuleDefinition rule : ruleEditorService.getRules()) {
            if (rule == null || !rule.isEnabled()) {
                continue;
            }

            String trigger = safe(rule.getTriggeringStratagemNameContains());
            if (trigger.isBlank()) {
                continue;
            }

            if (stratagemName.contains(trigger.toLowerCase(Locale.ROOT))) {
                result.add(rule.copy());
            }
        }

        return result;
    }

    public synchronized List<EditorActiveEffect> activateStratagemRules(
            StratagemInstance stratagem,
            UnitInstance targetUnit,
            Player ownerPlayer,
            Player turnOwner,
            Phase currentPhase,
            int currentRound
    ) {
        if (targetUnit == null) {
            return List.of();
        }

        List<EditorRuleDefinition> matchingRules = matchingStratagemRules(stratagem);
        if (matchingRules.isEmpty()) {
            return List.of();
        }

        List<EditorActiveEffect> created = new ArrayList<>();
        for (EditorRuleDefinition rule : matchingRules) {
            EditorActiveEffect effect = new EditorActiveEffect(
                    rule.getId(),
                    safe(rule.getName(), "Custom Rule"),
                    targetUnit.getInstanceId(),
                    targetUnit.getUnitName(),
                    rule.getTargetRole(),
                    rule.getDuration(),
                    currentRound,
                    currentPhase,
                    ownerPlayer,
                    turnOwner,
                    rule
            );
            activeEffects.add(effect);
            created.add(effect);
        }

        return created;
    }

    public synchronized List<EditorActiveEffect> activateOptionalRulesForAttack(
            List<EditorRuleDefinition> selectedRules,
            UnitInstance attacker,
            UnitInstance defender,
            Player ownerPlayer,
            Player turnOwner,
            Phase currentPhase,
            int currentRound
    ) {
        if (selectedRules == null || selectedRules.isEmpty()) {
            return List.of();
        }

        List<EditorActiveEffect> created = new ArrayList<>();
        for (EditorRuleDefinition rule : selectedRules) {
            if (rule == null || !rule.isEnabled() || !rule.isOptionalActivation()) {
                continue;
            }

            UnitInstance targetUnit = resolveTargetUnit(rule, attacker, defender);
            if (targetUnit == null || isRuleActiveForAttack(rule, attacker, defender)) {
                continue;
            }

            EditorActiveEffect effect = new EditorActiveEffect(
                    rule.getId(),
                    safe(rule.getName(), "Custom Rule"),
                    targetUnit.getInstanceId(),
                    targetUnit.getUnitName(),
                    rule.getTargetRole(),
                    rule.getDuration(),
                    currentRound,
                    currentPhase,
                    ownerPlayer,
                    turnOwner,
                    rule
            );
            activeEffects.add(effect);
            created.add(effect);
        }

        return List.copyOf(created);
    }

    public synchronized List<EditorActiveEffect> getActiveEffects() {
        return List.copyOf(activeEffects);
    }

    public synchronized List<EditorActiveEffect> activeEffectsForUnit(String unitId) {
        String normalizedId = safe(unitId);
        if (normalizedId.isBlank()) {
            return List.of();
        }

        List<EditorActiveEffect> result = new ArrayList<>();
        for (EditorActiveEffect effect : activeEffects) {
            if (effect != null && normalizedId.equals(effect.targetUnitId())) {
                result.add(effect);
            }
        }
        return List.copyOf(result);
    }

    public synchronized boolean isRuleActiveForAttack(
            EditorRuleDefinition rule,
            UnitInstance attacker,
            UnitInstance defender
    ) {
        if (rule == null) {
            return false;
        }

        UnitInstance targetUnit = resolveTargetUnit(rule, attacker, defender);
        if (targetUnit == null) {
            return false;
        }

        String ruleId = safe(rule.getId());
        String unitId = safe(targetUnit.getInstanceId());
        for (EditorActiveEffect effect : activeEffects) {
            if (effect == null) {
                continue;
            }
            if (ruleId.equals(safe(effect.ruleId())) && unitId.equals(safe(effect.targetUnitId()))) {
                return true;
            }
        }

        return false;
    }

    public synchronized void clearExpiredEffects(int currentRound, Phase currentPhase, Player activePlayer) {
        activeEffects.removeIf(effect -> isExpired(effect, currentRound, currentPhase, activePlayer));
    }

    public synchronized void clearAll() {
        activeEffects.clear();
    }

    public synchronized void deactivateEffects(List<EditorActiveEffect> effects) {
        if (effects == null || effects.isEmpty()) {
            return;
        }

        activeEffects.removeIf(effect -> containsEffect(effects, effect));
    }

    private UnitInstance resolveTargetUnit(
            EditorRuleDefinition rule,
            UnitInstance attacker,
            UnitInstance defender
    ) {
        if (rule == null) {
            return null;
        }

        return switch (rule.getTargetRole()) {
            case DEFENDER -> defender;
            case ATTACKER, EITHER -> attacker;
        };
    }

    private boolean containsEffect(List<EditorActiveEffect> effects, EditorActiveEffect candidate) {
        if (candidate == null) {
            return false;
        }

        for (EditorActiveEffect effect : effects) {
            if (effect == null) {
                continue;
            }
            if (safe(effect.ruleId()).equals(safe(candidate.ruleId()))
                    && safe(effect.targetUnitId()).equals(safe(candidate.targetUnitId()))
                    && effect.appliedRound() == candidate.appliedRound()
                    && effect.appliedPhase() == candidate.appliedPhase()) {
                return true;
            }
        }

        return false;
    }

    private boolean isExpired(
            EditorActiveEffect effect,
            int currentRound,
            Phase currentPhase,
            Player activePlayer
    ) {
        if (effect == null) {
            return true;
        }

        EditorRuleDuration duration = effect.duration();
        if (duration == EditorRuleDuration.UNTIL_END_OF_BATTLE) {
            return false;
        }

        if (duration == EditorRuleDuration.UNTIL_END_OF_PHASE) {
            return currentRound != effect.appliedRound()
                    || activePlayer != effect.turnOwnerAtApplication()
                    || currentPhase != effect.appliedPhase();
        }

        if (duration == EditorRuleDuration.UNTIL_END_OF_TURN) {
            return currentRound != effect.appliedRound()
                    || activePlayer != effect.turnOwnerAtApplication();
        }

        if (duration == EditorRuleDuration.UNTIL_START_OF_YOUR_NEXT_COMMAND) {
            return activePlayer == effect.ownerPlayer()
                    && currentPhase == Phase.COMMAND
                    && currentRound > effect.appliedRound();
        }

        return false;
    }

    private String safe(String value) {
        return value == null ? "" : value.trim();
    }

    private String safe(String value, String fallback) {
        String normalized = safe(value);
        return normalized.isBlank() ? fallback : normalized;
    }
}
