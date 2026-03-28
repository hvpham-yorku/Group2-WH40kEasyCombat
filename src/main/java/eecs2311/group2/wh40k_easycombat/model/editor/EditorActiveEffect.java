package eecs2311.group2.wh40k_easycombat.model.editor;

import eecs2311.group2.wh40k_easycombat.model.instance.Phase;
import eecs2311.group2.wh40k_easycombat.model.instance.Player;

public record EditorActiveEffect(
        String ruleId,
        String ruleName,
        String targetUnitId,
        String targetUnitName,
        EditorRuleTargetRole targetRole,
        EditorRuleDuration duration,
        int appliedRound,
        Phase appliedPhase,
        Player ownerPlayer,
        Player turnOwnerAtApplication,
        EditorRuleDefinition rule
) {
    public EditorActiveEffect {
        ruleId = ruleId == null ? "" : ruleId.trim();
        ruleName = ruleName == null ? "" : ruleName.trim();
        targetUnitId = targetUnitId == null ? "" : targetUnitId.trim();
        targetUnitName = targetUnitName == null ? "" : targetUnitName.trim();
        targetRole = targetRole == null ? EditorRuleTargetRole.ATTACKER : targetRole;
        duration = duration == null ? EditorRuleDuration.UNTIL_END_OF_PHASE : duration;
        appliedPhase = appliedPhase == null ? Phase.COMMAND : appliedPhase;
        ownerPlayer = ownerPlayer == null ? Player.ATTACKER : ownerPlayer;
        turnOwnerAtApplication = turnOwnerAtApplication == null ? Player.ATTACKER : turnOwnerAtApplication;
        rule = rule == null ? new EditorRuleDefinition() : rule.copy();
    }

    public String displayName() {
        String name = ruleName.isBlank() ? "Custom Rule" : ruleName;
        String target = targetUnitName.isBlank() ? "Selected Unit" : targetUnitName;
        return name + " -> " + target + " (" + duration + ")";
    }
}
