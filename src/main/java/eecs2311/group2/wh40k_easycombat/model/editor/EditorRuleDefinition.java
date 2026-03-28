package eecs2311.group2.wh40k_easycombat.model.editor;

import java.util.UUID;

public class EditorRuleDefinition {
    private static final String VM_RULE_PREFIX = "editor::";

    private String id = UUID.randomUUID().toString();
    private String name = "";
    private EditorRuleType type = EditorRuleType.ABILITY;
    private EditorRulePhase phase = EditorRulePhase.ANY;
    private EditorRuleAttackType attackType = EditorRuleAttackType.ANY;
    private boolean enabled = true;
    private boolean optionalActivation;
    private String attackerUnitNameContains = "";
    private String defenderUnitNameContains = "";
    private String weaponNameContains = "";
    private String attackerKeyword = "";
    private String defenderKeyword = "";
    private String attackerAbilityNameContains = "";
    private String defenderAbilityNameContains = "";
    private String attackerFactionAbilityNameContains = "";
    private String defenderFactionAbilityNameContains = "";
    private String attackerDetachmentAbilityNameContains = "";
    private String defenderDetachmentAbilityNameContains = "";
    private String attackerDetachmentNameContains = "";
    private String defenderDetachmentNameContains = "";
    private String attackerEnhancementNameContains = "";
    private String defenderEnhancementNameContains = "";
    private String attackerFactionNameContains = "";
    private String defenderFactionNameContains = "";
    private String triggeringStratagemNameContains = "";
    private EditorRuleDuration duration = EditorRuleDuration.UNTIL_END_OF_PHASE;
    private EditorRuleTargetRole targetRole = EditorRuleTargetRole.ATTACKER;
    private String dslScript = defaultDslScript();
    private boolean visualWithinHalfRange;
    private boolean visualRemainedStationary;
    private boolean visualAdvancedThisTurn;
    private boolean visualFellBackThisTurn;
    private boolean visualChargedThisTurn;
    private boolean visualAttackerCanFight;
    private boolean visualTargetHasCover;
    private boolean visualBlastIsLegal;
    private boolean visualTargetIsInfantry;
    private boolean visualTargetIsVehicle;
    private boolean visualTargetIsMonster;
    private boolean visualTargetIsCharacter;
    private boolean visualTargetIsPsyker;
    private int visualHitModifier;
    private int visualWoundModifier;
    private int visualAttacksModifier;
    private int visualDamageModifier;
    private int visualApModifier;
    private EditorRerollType visualHitReroll = EditorRerollType.NONE;
    private EditorRerollType visualWoundReroll = EditorRerollType.NONE;
    private String visualExtraWeaponKeywords = "";

    public EditorRuleDefinition copy() {
        EditorRuleDefinition copy = new EditorRuleDefinition();
        copy.id = id;
        copy.name = name;
        copy.type = type;
        copy.phase = phase;
        copy.attackType = attackType;
        copy.enabled = enabled;
        copy.optionalActivation = optionalActivation;
        copy.attackerUnitNameContains = attackerUnitNameContains;
        copy.defenderUnitNameContains = defenderUnitNameContains;
        copy.weaponNameContains = weaponNameContains;
        copy.attackerKeyword = attackerKeyword;
        copy.defenderKeyword = defenderKeyword;
        copy.attackerAbilityNameContains = attackerAbilityNameContains;
        copy.defenderAbilityNameContains = defenderAbilityNameContains;
        copy.attackerFactionAbilityNameContains = attackerFactionAbilityNameContains;
        copy.defenderFactionAbilityNameContains = defenderFactionAbilityNameContains;
        copy.attackerDetachmentAbilityNameContains = attackerDetachmentAbilityNameContains;
        copy.defenderDetachmentAbilityNameContains = defenderDetachmentAbilityNameContains;
        copy.attackerDetachmentNameContains = attackerDetachmentNameContains;
        copy.defenderDetachmentNameContains = defenderDetachmentNameContains;
        copy.attackerEnhancementNameContains = attackerEnhancementNameContains;
        copy.defenderEnhancementNameContains = defenderEnhancementNameContains;
        copy.attackerFactionNameContains = attackerFactionNameContains;
        copy.defenderFactionNameContains = defenderFactionNameContains;
        copy.triggeringStratagemNameContains = triggeringStratagemNameContains;
        copy.duration = duration;
        copy.targetRole = targetRole;
        copy.dslScript = dslScript;
        copy.visualWithinHalfRange = visualWithinHalfRange;
        copy.visualRemainedStationary = visualRemainedStationary;
        copy.visualAdvancedThisTurn = visualAdvancedThisTurn;
        copy.visualFellBackThisTurn = visualFellBackThisTurn;
        copy.visualChargedThisTurn = visualChargedThisTurn;
        copy.visualAttackerCanFight = visualAttackerCanFight;
        copy.visualTargetHasCover = visualTargetHasCover;
        copy.visualBlastIsLegal = visualBlastIsLegal;
        copy.visualTargetIsInfantry = visualTargetIsInfantry;
        copy.visualTargetIsVehicle = visualTargetIsVehicle;
        copy.visualTargetIsMonster = visualTargetIsMonster;
        copy.visualTargetIsCharacter = visualTargetIsCharacter;
        copy.visualTargetIsPsyker = visualTargetIsPsyker;
        copy.visualHitModifier = visualHitModifier;
        copy.visualWoundModifier = visualWoundModifier;
        copy.visualAttacksModifier = visualAttacksModifier;
        copy.visualDamageModifier = visualDamageModifier;
        copy.visualApModifier = visualApModifier;
        copy.visualHitReroll = visualHitReroll;
        copy.visualWoundReroll = visualWoundReroll;
        copy.visualExtraWeaponKeywords = visualExtraWeaponKeywords;
        return copy;
    }

    public String displayName() {
        String baseName = name == null || name.isBlank() ? "Untitled Rule" : name.trim();
        String modeLabel = safe(triggeringStratagemNameContains).isBlank()
                ? (optionalActivation ? "Optional" : "Passive")
                : "Triggered";
        String enabledLabel = enabled ? "" : " (Disabled)";
        return "[" + modeLabel + "] " + baseName + enabledLabel;
    }

    public String vmRuleName() {
        return VM_RULE_PREFIX + getId();
    }

    public static String defaultDslScript() {
        return """
                # Generated by the visual VM editor
                0 -> hit_modifier
                0 -> wound_modifier
                0 -> attacks_modifier
                0 -> damage_modifier
                0 -> ap_modifier
                0 -> hit_reroll
                0 -> wound_reroll
                "" -> extra_weapon_keywords
                """;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id == null || id.isBlank() ? UUID.randomUUID().toString() : id.trim();
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name == null ? "" : name.trim();
    }

    public EditorRuleType getType() {
        return type == null ? EditorRuleType.ABILITY : type;
    }

    public void setType(EditorRuleType type) {
        this.type = type == null ? EditorRuleType.ABILITY : type;
    }

    public EditorRulePhase getPhase() {
        return phase == null ? EditorRulePhase.ANY : phase;
    }

    public void setPhase(EditorRulePhase phase) {
        this.phase = phase == null ? EditorRulePhase.ANY : phase;
    }

    public EditorRuleAttackType getAttackType() {
        return attackType == null ? EditorRuleAttackType.ANY : attackType;
    }

    public void setAttackType(EditorRuleAttackType attackType) {
        this.attackType = attackType == null ? EditorRuleAttackType.ANY : attackType;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public boolean isOptionalActivation() {
        return optionalActivation;
    }

    public void setOptionalActivation(boolean optionalActivation) {
        this.optionalActivation = optionalActivation;
    }

    public String getAttackerUnitNameContains() {
        return attackerUnitNameContains;
    }

    public void setAttackerUnitNameContains(String attackerUnitNameContains) {
        this.attackerUnitNameContains = safe(attackerUnitNameContains);
    }

    public String getDefenderUnitNameContains() {
        return defenderUnitNameContains;
    }

    public void setDefenderUnitNameContains(String defenderUnitNameContains) {
        this.defenderUnitNameContains = safe(defenderUnitNameContains);
    }

    public String getWeaponNameContains() {
        return weaponNameContains;
    }

    public void setWeaponNameContains(String weaponNameContains) {
        this.weaponNameContains = safe(weaponNameContains);
    }

    public String getAttackerKeyword() {
        return attackerKeyword;
    }

    public void setAttackerKeyword(String attackerKeyword) {
        this.attackerKeyword = safe(attackerKeyword);
    }

    public String getDefenderKeyword() {
        return defenderKeyword;
    }

    public void setDefenderKeyword(String defenderKeyword) {
        this.defenderKeyword = safe(defenderKeyword);
    }

    public String getAttackerAbilityNameContains() {
        return attackerAbilityNameContains;
    }

    public void setAttackerAbilityNameContains(String attackerAbilityNameContains) {
        this.attackerAbilityNameContains = safe(attackerAbilityNameContains);
    }

    public String getDefenderAbilityNameContains() {
        return defenderAbilityNameContains;
    }

    public void setDefenderAbilityNameContains(String defenderAbilityNameContains) {
        this.defenderAbilityNameContains = safe(defenderAbilityNameContains);
    }

    public String getAttackerFactionAbilityNameContains() {
        return attackerFactionAbilityNameContains;
    }

    public void setAttackerFactionAbilityNameContains(String attackerFactionAbilityNameContains) {
        this.attackerFactionAbilityNameContains = safe(attackerFactionAbilityNameContains);
    }

    public String getDefenderFactionAbilityNameContains() {
        return defenderFactionAbilityNameContains;
    }

    public void setDefenderFactionAbilityNameContains(String defenderFactionAbilityNameContains) {
        this.defenderFactionAbilityNameContains = safe(defenderFactionAbilityNameContains);
    }

    public String getAttackerDetachmentAbilityNameContains() {
        return attackerDetachmentAbilityNameContains;
    }

    public void setAttackerDetachmentAbilityNameContains(String attackerDetachmentAbilityNameContains) {
        this.attackerDetachmentAbilityNameContains = safe(attackerDetachmentAbilityNameContains);
    }

    public String getDefenderDetachmentAbilityNameContains() {
        return defenderDetachmentAbilityNameContains;
    }

    public void setDefenderDetachmentAbilityNameContains(String defenderDetachmentAbilityNameContains) {
        this.defenderDetachmentAbilityNameContains = safe(defenderDetachmentAbilityNameContains);
    }

    public String getAttackerDetachmentNameContains() {
        return attackerDetachmentNameContains;
    }

    public void setAttackerDetachmentNameContains(String attackerDetachmentNameContains) {
        this.attackerDetachmentNameContains = safe(attackerDetachmentNameContains);
    }

    public String getDefenderDetachmentNameContains() {
        return defenderDetachmentNameContains;
    }

    public void setDefenderDetachmentNameContains(String defenderDetachmentNameContains) {
        this.defenderDetachmentNameContains = safe(defenderDetachmentNameContains);
    }

    public String getAttackerEnhancementNameContains() {
        return attackerEnhancementNameContains;
    }

    public void setAttackerEnhancementNameContains(String attackerEnhancementNameContains) {
        this.attackerEnhancementNameContains = safe(attackerEnhancementNameContains);
    }

    public String getDefenderEnhancementNameContains() {
        return defenderEnhancementNameContains;
    }

    public void setDefenderEnhancementNameContains(String defenderEnhancementNameContains) {
        this.defenderEnhancementNameContains = safe(defenderEnhancementNameContains);
    }

    public String getAttackerFactionNameContains() {
        return attackerFactionNameContains;
    }

    public void setAttackerFactionNameContains(String attackerFactionNameContains) {
        this.attackerFactionNameContains = safe(attackerFactionNameContains);
    }

    public String getDefenderFactionNameContains() {
        return defenderFactionNameContains;
    }

    public void setDefenderFactionNameContains(String defenderFactionNameContains) {
        this.defenderFactionNameContains = safe(defenderFactionNameContains);
    }

    public String getTriggeringStratagemNameContains() {
        return triggeringStratagemNameContains;
    }

    public void setTriggeringStratagemNameContains(String triggeringStratagemNameContains) {
        this.triggeringStratagemNameContains = safe(triggeringStratagemNameContains);
    }

    public EditorRuleDuration getDuration() {
        return duration == null ? EditorRuleDuration.UNTIL_END_OF_PHASE : duration;
    }

    public void setDuration(EditorRuleDuration duration) {
        this.duration = duration == null ? EditorRuleDuration.UNTIL_END_OF_PHASE : duration;
    }

    public EditorRuleTargetRole getTargetRole() {
        return targetRole == null ? EditorRuleTargetRole.ATTACKER : targetRole;
    }

    public void setTargetRole(EditorRuleTargetRole targetRole) {
        this.targetRole = targetRole == null ? EditorRuleTargetRole.ATTACKER : targetRole;
    }

    public String getDslScript() {
        return dslScript;
    }

    public void setDslScript(String dslScript) {
        this.dslScript = dslScript == null ? "" : normalizeLineEndings(dslScript).trim();
    }

    public boolean isVisualWithinHalfRange() {
        return visualWithinHalfRange;
    }

    public void setVisualWithinHalfRange(boolean visualWithinHalfRange) {
        this.visualWithinHalfRange = visualWithinHalfRange;
    }

    public boolean isVisualRemainedStationary() {
        return visualRemainedStationary;
    }

    public void setVisualRemainedStationary(boolean visualRemainedStationary) {
        this.visualRemainedStationary = visualRemainedStationary;
    }

    public boolean isVisualAdvancedThisTurn() {
        return visualAdvancedThisTurn;
    }

    public void setVisualAdvancedThisTurn(boolean visualAdvancedThisTurn) {
        this.visualAdvancedThisTurn = visualAdvancedThisTurn;
    }

    public boolean isVisualFellBackThisTurn() {
        return visualFellBackThisTurn;
    }

    public void setVisualFellBackThisTurn(boolean visualFellBackThisTurn) {
        this.visualFellBackThisTurn = visualFellBackThisTurn;
    }

    public boolean isVisualChargedThisTurn() {
        return visualChargedThisTurn;
    }

    public void setVisualChargedThisTurn(boolean visualChargedThisTurn) {
        this.visualChargedThisTurn = visualChargedThisTurn;
    }

    public boolean isVisualAttackerCanFight() {
        return visualAttackerCanFight;
    }

    public void setVisualAttackerCanFight(boolean visualAttackerCanFight) {
        this.visualAttackerCanFight = visualAttackerCanFight;
    }

    public boolean isVisualTargetHasCover() {
        return visualTargetHasCover;
    }

    public void setVisualTargetHasCover(boolean visualTargetHasCover) {
        this.visualTargetHasCover = visualTargetHasCover;
    }

    public boolean isVisualBlastIsLegal() {
        return visualBlastIsLegal;
    }

    public void setVisualBlastIsLegal(boolean visualBlastIsLegal) {
        this.visualBlastIsLegal = visualBlastIsLegal;
    }

    public boolean isVisualTargetIsInfantry() {
        return visualTargetIsInfantry;
    }

    public void setVisualTargetIsInfantry(boolean visualTargetIsInfantry) {
        this.visualTargetIsInfantry = visualTargetIsInfantry;
    }

    public boolean isVisualTargetIsVehicle() {
        return visualTargetIsVehicle;
    }

    public void setVisualTargetIsVehicle(boolean visualTargetIsVehicle) {
        this.visualTargetIsVehicle = visualTargetIsVehicle;
    }

    public boolean isVisualTargetIsMonster() {
        return visualTargetIsMonster;
    }

    public void setVisualTargetIsMonster(boolean visualTargetIsMonster) {
        this.visualTargetIsMonster = visualTargetIsMonster;
    }

    public boolean isVisualTargetIsCharacter() {
        return visualTargetIsCharacter;
    }

    public void setVisualTargetIsCharacter(boolean visualTargetIsCharacter) {
        this.visualTargetIsCharacter = visualTargetIsCharacter;
    }

    public boolean isVisualTargetIsPsyker() {
        return visualTargetIsPsyker;
    }

    public void setVisualTargetIsPsyker(boolean visualTargetIsPsyker) {
        this.visualTargetIsPsyker = visualTargetIsPsyker;
    }

    public int getVisualHitModifier() {
        return visualHitModifier;
    }

    public void setVisualHitModifier(int visualHitModifier) {
        this.visualHitModifier = visualHitModifier;
    }

    public int getVisualWoundModifier() {
        return visualWoundModifier;
    }

    public void setVisualWoundModifier(int visualWoundModifier) {
        this.visualWoundModifier = visualWoundModifier;
    }

    public int getVisualAttacksModifier() {
        return visualAttacksModifier;
    }

    public void setVisualAttacksModifier(int visualAttacksModifier) {
        this.visualAttacksModifier = visualAttacksModifier;
    }

    public int getVisualDamageModifier() {
        return visualDamageModifier;
    }

    public void setVisualDamageModifier(int visualDamageModifier) {
        this.visualDamageModifier = visualDamageModifier;
    }

    public int getVisualApModifier() {
        return visualApModifier;
    }

    public void setVisualApModifier(int visualApModifier) {
        this.visualApModifier = visualApModifier;
    }

    public EditorRerollType getVisualHitReroll() {
        return visualHitReroll == null ? EditorRerollType.NONE : visualHitReroll;
    }

    public void setVisualHitReroll(EditorRerollType visualHitReroll) {
        this.visualHitReroll = visualHitReroll == null ? EditorRerollType.NONE : visualHitReroll;
    }

    public EditorRerollType getVisualWoundReroll() {
        return visualWoundReroll == null ? EditorRerollType.NONE : visualWoundReroll;
    }

    public void setVisualWoundReroll(EditorRerollType visualWoundReroll) {
        this.visualWoundReroll = visualWoundReroll == null ? EditorRerollType.NONE : visualWoundReroll;
    }

    public String getVisualExtraWeaponKeywords() {
        return visualExtraWeaponKeywords;
    }

    public void setVisualExtraWeaponKeywords(String visualExtraWeaponKeywords) {
        this.visualExtraWeaponKeywords = safe(visualExtraWeaponKeywords);
    }

    private String safe(String value) {
        return value == null ? "" : value.trim();
    }

    private String normalizeLineEndings(String value) {
        return value == null ? "" : value.replace("\r\n", "\n").replace('\r', '\n');
    }
}
