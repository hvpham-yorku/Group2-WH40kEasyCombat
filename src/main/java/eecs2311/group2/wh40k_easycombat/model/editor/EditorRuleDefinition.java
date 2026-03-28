package eecs2311.group2.wh40k_easycombat.model.editor;

import java.util.UUID;

public class EditorRuleDefinition {
    private String id = UUID.randomUUID().toString();
    private String name = "";
    private EditorRuleType type = EditorRuleType.ABILITY;
    private EditorRuleActivationMode activationMode = EditorRuleActivationMode.PASSIVE;
    private EditorRulePhase phase = EditorRulePhase.ANY;
    private EditorRuleAttackType attackType = EditorRuleAttackType.ANY;
    private boolean enabled = true;
    private String attackerUnitNameContains = "";
    private String defenderUnitNameContains = "";
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
    private boolean requireWithinHalfRange;
    private boolean requireRemainedStationary;
    private boolean requireChargedThisTurn;
    private boolean requireTargetHasCover;
    private boolean requireTargetInfantry;
    private boolean requireTargetVehicle;
    private boolean requireTargetMonster;
    private boolean requireTargetCharacter;
    private boolean requireTargetPsyker;
    private int hitModifier;
    private int woundModifier;
    private int attacksModifier;
    private int damageModifier;
    private int apModifier;
    private String extraWeaponKeywords = "";
    private EditorRerollType hitReroll = EditorRerollType.NONE;
    private EditorRerollType woundReroll = EditorRerollType.NONE;
    private String notes = "";

    public EditorRuleDefinition copy() {
        EditorRuleDefinition copy = new EditorRuleDefinition();
        copy.id = id;
        copy.name = name;
        copy.type = type;
        copy.activationMode = activationMode;
        copy.phase = phase;
        copy.attackType = attackType;
        copy.enabled = enabled;
        copy.attackerUnitNameContains = attackerUnitNameContains;
        copy.defenderUnitNameContains = defenderUnitNameContains;
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
        copy.requireWithinHalfRange = requireWithinHalfRange;
        copy.requireRemainedStationary = requireRemainedStationary;
        copy.requireChargedThisTurn = requireChargedThisTurn;
        copy.requireTargetHasCover = requireTargetHasCover;
        copy.requireTargetInfantry = requireTargetInfantry;
        copy.requireTargetVehicle = requireTargetVehicle;
        copy.requireTargetMonster = requireTargetMonster;
        copy.requireTargetCharacter = requireTargetCharacter;
        copy.requireTargetPsyker = requireTargetPsyker;
        copy.hitModifier = hitModifier;
        copy.woundModifier = woundModifier;
        copy.attacksModifier = attacksModifier;
        copy.damageModifier = damageModifier;
        copy.apModifier = apModifier;
        copy.extraWeaponKeywords = extraWeaponKeywords;
        copy.hitReroll = hitReroll;
        copy.woundReroll = woundReroll;
        copy.notes = notes;
        return copy;
    }

    public String displayName() {
        String baseName = name == null || name.isBlank() ? "Untitled Rule" : name.trim();
        String modeLabel = getActivationMode().label();
        String enabledLabel = enabled ? "" : " (Disabled)";
        return "[" + modeLabel + "] " + baseName + enabledLabel;
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

    public EditorRuleActivationMode getActivationMode() {
        return activationMode == null ? EditorRuleActivationMode.PASSIVE : activationMode;
    }

    public void setActivationMode(EditorRuleActivationMode activationMode) {
        this.activationMode = activationMode == null ? EditorRuleActivationMode.PASSIVE : activationMode;
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

    public String getAttackerUnitNameContains() {
        return attackerUnitNameContains;
    }

    public void setAttackerUnitNameContains(String attackerUnitNameContains) {
        this.attackerUnitNameContains = attackerUnitNameContains == null ? "" : attackerUnitNameContains.trim();
    }

    public String getDefenderUnitNameContains() {
        return defenderUnitNameContains;
    }

    public void setDefenderUnitNameContains(String defenderUnitNameContains) {
        this.defenderUnitNameContains = defenderUnitNameContains == null ? "" : defenderUnitNameContains.trim();
    }

    public String getAttackerKeyword() {
        return attackerKeyword;
    }

    public void setAttackerKeyword(String attackerKeyword) {
        this.attackerKeyword = attackerKeyword == null ? "" : attackerKeyword.trim();
    }

    public String getDefenderKeyword() {
        return defenderKeyword;
    }

    public void setDefenderKeyword(String defenderKeyword) {
        this.defenderKeyword = defenderKeyword == null ? "" : defenderKeyword.trim();
    }

    public String getAttackerAbilityNameContains() {
        return attackerAbilityNameContains;
    }

    public void setAttackerAbilityNameContains(String attackerAbilityNameContains) {
        this.attackerAbilityNameContains = attackerAbilityNameContains == null ? "" : attackerAbilityNameContains.trim();
    }

    public String getDefenderAbilityNameContains() {
        return defenderAbilityNameContains;
    }

    public void setDefenderAbilityNameContains(String defenderAbilityNameContains) {
        this.defenderAbilityNameContains = defenderAbilityNameContains == null ? "" : defenderAbilityNameContains.trim();
    }

    public String getAttackerFactionNameContains() {
        return attackerFactionNameContains;
    }

    public void setAttackerFactionNameContains(String attackerFactionNameContains) {
        this.attackerFactionNameContains = attackerFactionNameContains == null ? "" : attackerFactionNameContains.trim();
    }

    public String getAttackerFactionAbilityNameContains() {
        return attackerFactionAbilityNameContains;
    }

    public void setAttackerFactionAbilityNameContains(String attackerFactionAbilityNameContains) {
        this.attackerFactionAbilityNameContains = attackerFactionAbilityNameContains == null ? "" : attackerFactionAbilityNameContains.trim();
    }

    public String getDefenderFactionAbilityNameContains() {
        return defenderFactionAbilityNameContains;
    }

    public void setDefenderFactionAbilityNameContains(String defenderFactionAbilityNameContains) {
        this.defenderFactionAbilityNameContains = defenderFactionAbilityNameContains == null ? "" : defenderFactionAbilityNameContains.trim();
    }

    public String getAttackerDetachmentAbilityNameContains() {
        return attackerDetachmentAbilityNameContains;
    }

    public void setAttackerDetachmentAbilityNameContains(String attackerDetachmentAbilityNameContains) {
        this.attackerDetachmentAbilityNameContains = attackerDetachmentAbilityNameContains == null ? "" : attackerDetachmentAbilityNameContains.trim();
    }

    public String getDefenderDetachmentAbilityNameContains() {
        return defenderDetachmentAbilityNameContains;
    }

    public void setDefenderDetachmentAbilityNameContains(String defenderDetachmentAbilityNameContains) {
        this.defenderDetachmentAbilityNameContains = defenderDetachmentAbilityNameContains == null ? "" : defenderDetachmentAbilityNameContains.trim();
    }

    public String getAttackerDetachmentNameContains() {
        return attackerDetachmentNameContains;
    }

    public void setAttackerDetachmentNameContains(String attackerDetachmentNameContains) {
        this.attackerDetachmentNameContains = attackerDetachmentNameContains == null ? "" : attackerDetachmentNameContains.trim();
    }

    public String getDefenderDetachmentNameContains() {
        return defenderDetachmentNameContains;
    }

    public void setDefenderDetachmentNameContains(String defenderDetachmentNameContains) {
        this.defenderDetachmentNameContains = defenderDetachmentNameContains == null ? "" : defenderDetachmentNameContains.trim();
    }

    public String getAttackerEnhancementNameContains() {
        return attackerEnhancementNameContains;
    }

    public void setAttackerEnhancementNameContains(String attackerEnhancementNameContains) {
        this.attackerEnhancementNameContains = attackerEnhancementNameContains == null ? "" : attackerEnhancementNameContains.trim();
    }

    public String getDefenderEnhancementNameContains() {
        return defenderEnhancementNameContains;
    }

    public void setDefenderEnhancementNameContains(String defenderEnhancementNameContains) {
        this.defenderEnhancementNameContains = defenderEnhancementNameContains == null ? "" : defenderEnhancementNameContains.trim();
    }

    public String getDefenderFactionNameContains() {
        return defenderFactionNameContains;
    }

    public void setDefenderFactionNameContains(String defenderFactionNameContains) {
        this.defenderFactionNameContains = defenderFactionNameContains == null ? "" : defenderFactionNameContains.trim();
    }

    public String getTriggeringStratagemNameContains() {
        return triggeringStratagemNameContains;
    }

    public void setTriggeringStratagemNameContains(String triggeringStratagemNameContains) {
        this.triggeringStratagemNameContains = triggeringStratagemNameContains == null ? "" : triggeringStratagemNameContains.trim();
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

    public boolean isRequireWithinHalfRange() {
        return requireWithinHalfRange;
    }

    public void setRequireWithinHalfRange(boolean requireWithinHalfRange) {
        this.requireWithinHalfRange = requireWithinHalfRange;
    }

    public boolean isRequireRemainedStationary() {
        return requireRemainedStationary;
    }

    public void setRequireRemainedStationary(boolean requireRemainedStationary) {
        this.requireRemainedStationary = requireRemainedStationary;
    }

    public boolean isRequireChargedThisTurn() {
        return requireChargedThisTurn;
    }

    public void setRequireChargedThisTurn(boolean requireChargedThisTurn) {
        this.requireChargedThisTurn = requireChargedThisTurn;
    }

    public boolean isRequireTargetHasCover() {
        return requireTargetHasCover;
    }

    public void setRequireTargetHasCover(boolean requireTargetHasCover) {
        this.requireTargetHasCover = requireTargetHasCover;
    }

    public boolean isRequireTargetInfantry() {
        return requireTargetInfantry;
    }

    public void setRequireTargetInfantry(boolean requireTargetInfantry) {
        this.requireTargetInfantry = requireTargetInfantry;
    }

    public boolean isRequireTargetVehicle() {
        return requireTargetVehicle;
    }

    public void setRequireTargetVehicle(boolean requireTargetVehicle) {
        this.requireTargetVehicle = requireTargetVehicle;
    }

    public boolean isRequireTargetMonster() {
        return requireTargetMonster;
    }

    public void setRequireTargetMonster(boolean requireTargetMonster) {
        this.requireTargetMonster = requireTargetMonster;
    }

    public boolean isRequireTargetCharacter() {
        return requireTargetCharacter;
    }

    public void setRequireTargetCharacter(boolean requireTargetCharacter) {
        this.requireTargetCharacter = requireTargetCharacter;
    }

    public boolean isRequireTargetPsyker() {
        return requireTargetPsyker;
    }

    public void setRequireTargetPsyker(boolean requireTargetPsyker) {
        this.requireTargetPsyker = requireTargetPsyker;
    }

    public int getHitModifier() {
        return hitModifier;
    }

    public void setHitModifier(int hitModifier) {
        this.hitModifier = hitModifier;
    }

    public int getWoundModifier() {
        return woundModifier;
    }

    public void setWoundModifier(int woundModifier) {
        this.woundModifier = woundModifier;
    }

    public int getAttacksModifier() {
        return attacksModifier;
    }

    public void setAttacksModifier(int attacksModifier) {
        this.attacksModifier = attacksModifier;
    }

    public int getDamageModifier() {
        return damageModifier;
    }

    public void setDamageModifier(int damageModifier) {
        this.damageModifier = damageModifier;
    }

    public int getApModifier() {
        return apModifier;
    }

    public void setApModifier(int apModifier) {
        this.apModifier = apModifier;
    }

    public String getExtraWeaponKeywords() {
        return extraWeaponKeywords;
    }

    public void setExtraWeaponKeywords(String extraWeaponKeywords) {
        this.extraWeaponKeywords = extraWeaponKeywords == null ? "" : extraWeaponKeywords.trim();
    }

    public EditorRerollType getHitReroll() {
        return hitReroll == null ? EditorRerollType.NONE : hitReroll;
    }

    public void setHitReroll(EditorRerollType hitReroll) {
        this.hitReroll = hitReroll == null ? EditorRerollType.NONE : hitReroll;
    }

    public EditorRerollType getWoundReroll() {
        return woundReroll == null ? EditorRerollType.NONE : woundReroll;
    }

    public void setWoundReroll(EditorRerollType woundReroll) {
        this.woundReroll = woundReroll == null ? EditorRerollType.NONE : woundReroll;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes == null ? "" : notes.trim();
    }
}
