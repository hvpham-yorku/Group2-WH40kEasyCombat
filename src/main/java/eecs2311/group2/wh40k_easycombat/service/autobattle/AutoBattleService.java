package eecs2311.group2.wh40k_easycombat.service.autobattle;

import eecs2311.group2.wh40k_easycombat.model.combat.AttackResult;
import eecs2311.group2.wh40k_easycombat.model.combat.AutoBattleResolution;
import eecs2311.group2.wh40k_easycombat.model.combat.CasualtyUpdate;
import eecs2311.group2.wh40k_easycombat.model.combat.FightPhaseState;
import eecs2311.group2.wh40k_easycombat.model.combat.PendingDamage;
import eecs2311.group2.wh40k_easycombat.model.combat.PendingDamageStepResult;
import eecs2311.group2.wh40k_easycombat.model.combat.ResolvedAttack;
import eecs2311.group2.wh40k_easycombat.model.editor.EditorRuleModifiers;
import eecs2311.group2.wh40k_easycombat.model.instance.Player;
import eecs2311.group2.wh40k_easycombat.model.instance.UnitInstance;
import eecs2311.group2.wh40k_easycombat.model.instance.UnitModelInstance;
import eecs2311.group2.wh40k_easycombat.model.instance.WeaponProfile;
import eecs2311.group2.wh40k_easycombat.service.editor.EditorRuleApplicationService;
import eecs2311.group2.wh40k_easycombat.service.game.ArmyListStateService;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class AutoBattleService {
    private static final Pattern DICE_PATTERN = Pattern.compile("^(\\d*)D(3|6)([+-]\\d+)?$");

    private final AutoBattler autoBattler = new AutoBattler();
    private final EditorRuleApplicationService editorRuleApplicationService = new EditorRuleApplicationService();

    public List<WeaponProfile> availableWeapons(AutoBattleMode mode, UnitInstance unit) {
        if (mode == null || unit == null || unit.isDestroyed()) {
            return List.of();
        }

        return switch (mode) {
            case SHOOTING -> availableRangedWeapons(unit, true);
            case REACTION_SHOOTING -> availableRangedWeapons(unit, false);
            case FIGHT -> availableFightWeapons(unit);
        };
    }

    public boolean canPlayerAct(
            AutoBattleMode mode,
            Player activeTurnPlayer,
            Player attackingPlayer,
            UnitInstance attacker,
            FightPhaseState fightPhaseState
    ) {
        if (mode == null || attackingPlayer == null || attacker == null || attacker.isDestroyed()) {
            return false;
        }

        return switch (mode) {
            case SHOOTING -> attackingPlayer == activeTurnPlayer;
            case REACTION_SHOOTING -> attackingPlayer != activeTurnPlayer;
            case FIGHT -> canFight(attackingPlayer, attacker, fightPhaseState);
        };
    }

    public AutoBattleResolution resolve(
            AutoBattleMode mode,
            Player activeTurnPlayer,
            Player attackingPlayer,
            UnitInstance attacker,
            UnitInstance defender,
            WeaponProfile selectedWeapon,
            AttackKeywordContext context,
            FightPhaseState fightPhaseState
    ) {
        if (mode == null) {
            return AutoBattleResolution.failure("Auto battle is not available in the current phase.");
        }
        if (attacker == null || defender == null) {
            return AutoBattleResolution.failure("Attacker or defender unit is missing.");
        }
        if (selectedWeapon == null) {
            return AutoBattleResolution.failure("Please select one weapon.");
        }
        if (mode.usesRangedWeapons() && selectedWeapon.melee()) {
            return AutoBattleResolution.failure("Please select a ranged weapon for this attack.");
        }
        if (!mode.usesRangedWeapons() && !selectedWeapon.melee()) {
            return AutoBattleResolution.failure("Please select a melee weapon for this fight.");
        }
        if (!canPlayerAct(mode, activeTurnPlayer, attackingPlayer, attacker, fightPhaseState)) {
            return AutoBattleResolution.failure(failureMessage(mode, activeTurnPlayer, attackingPlayer, fightPhaseState));
        }

        Set<String> destroyedBefore = ArmyListStateService.destroyedModelIds(defender);
        List<ResolvedAttack> resolvedAttacks = new ArrayList<>();
        List<PendingDamage> allPendingDamages = new ArrayList<>();
        boolean hazardousTriggered = false;

        if (mode == AutoBattleMode.FIGHT) {
            List<WeaponProfile> fightWeapons = availableFightWeapons(attacker);
            if (fightWeapons.isEmpty()) {
                return AutoBattleResolution.failure("This unit has no melee weapon available for this fight.");
            }

            for (WeaponProfile fightWeapon : fightWeapons) {
                EditorRuleModifiers modifiers = editorRuleApplicationService.resolveForAttack(
                        mode,
                        attacker,
                        defender,
                        fightWeapon,
                        context
                );
                WeaponProfile resolvedWeapon = applyEditorModifiersToWeapon(fightWeapon, modifiers);
                AttackKeywordContext resolvedContext = context.withEditorModifiers(modifiers);
                AttackResult fightResult = autoBattler.simulateAttack(attacker, defender, resolvedWeapon, resolvedContext);
                fightResult = annotateCustomRules(fightResult, modifiers);
                if (!fightResult.resolved()) {
                    return AutoBattleResolution.failure(
                            fightResult.notes().isEmpty()
                                    ? "A melee weapon on the selected unit could not be resolved."
                                    : fightResult.notes().get(0)
                    );
                }

                String attackLabel = WeaponKeywordsService.parse(fightWeapon.description()).extraAttacks()
                        ? "Fight (Extra Attacks)"
                        : mode.attackLabel();

                resolvedAttacks.add(new ResolvedAttack(attackLabel, fightWeapon.name(), fightResult));
                allPendingDamages.addAll(remapPendingDamages(fightResult.pendingDamages(), attackLabel, fightWeapon.name()));
                hazardousTriggered = hazardousTriggered || WeaponKeywordsService.parse(fightWeapon.description()).hazardous();
            }

            attacker.setFoughtThisPhase(true);
        } else {
            EditorRuleModifiers modifiers = editorRuleApplicationService.resolveForAttack(
                    mode,
                    attacker,
                    defender,
                    selectedWeapon,
                    context
            );
            WeaponProfile resolvedWeapon = applyEditorModifiersToWeapon(selectedWeapon, modifiers);
            AttackKeywordContext resolvedContext = context.withEditorModifiers(modifiers);
            AttackResult primary = autoBattler.simulateAttack(attacker, defender, resolvedWeapon, resolvedContext);
            primary = annotateCustomRules(primary, modifiers);
            if (!primary.resolved()) {
                return AutoBattleResolution.failure(primary.notes().isEmpty() ? "The selected attack could not be resolved." : primary.notes().get(0));
            }

            resolvedAttacks.add(new ResolvedAttack(mode.attackLabel(), selectedWeapon.name(), primary));
            allPendingDamages.addAll(remapPendingDamages(primary.pendingDamages(), mode.attackLabel(), selectedWeapon.name()));

            WeaponKeywordsService selectedKeywords = WeaponKeywordsService.parse(selectedWeapon.description());
            hazardousTriggered = selectedKeywords.hazardous();

            if (mode == AutoBattleMode.SHOOTING) {
                attacker.markRangedWeaponUsedThisPhase(selectedWeapon);
            }

            if ((mode == AutoBattleMode.SHOOTING || mode == AutoBattleMode.REACTION_SHOOTING) && selectedKeywords.oneShot()) {
                attacker.markOneShotWeaponUsed(selectedWeapon);
            }
        }

        PendingDamageSession allocationSession = new PendingDamageSession(
                mode,
                attackingPlayer,
                attacker.getUnitName(),
                defender.getUnitName(),
                defender,
                destroyedBefore,
                allPendingDamages
        );

        return new AutoBattleResolution(
                true,
                "",
                resolvedAttacks,
                CasualtyUpdate.none(),
                hazardousTriggered,
                allocationSession
        );
    }

    public PendingDamageStepResult applyNextPendingDamage(
            PendingDamageSession session,
            UnitModelInstance targetModel
    ) {
        if (session == null || !session.hasPendingDamage()) {
            return PendingDamageStepResult.failure("There is no pending damage to allocate.");
        }
        if (targetModel == null || targetModel.isDestroyed()) {
            return PendingDamageStepResult.failure("Please select a living target model.");
        }

        PendingDamage currentDamage = session.consumeCurrentDamage();
        if (currentDamage == null) {
            return PendingDamageStepResult.failure("There is no pending damage to allocate.");
        }

        int beforeHp = targetModel.getCurrentHp();
        int appliedDamage = Math.min(beforeHp, currentDamage.damage());
        int wastedDamage = Math.max(0, currentDamage.damage() - appliedDamage);

        targetModel.takeDamage(appliedDamage);

        boolean targetDestroyed = targetModel.isDestroyed();
        boolean sessionComplete = !session.hasPendingDamage() || session.defender().isDestroyed();
        CasualtyUpdate casualtyUpdate = CasualtyUpdate.none();

        if (session.defender().isDestroyed()) {
            session.clearRemainingDamages();
            sessionComplete = true;
        }

        if (targetDestroyed || sessionComplete) {
            casualtyUpdate = ArmyListStateService.applyCasualties(session.defender(), session.destroyedBefore());
            session.markCurrentDestroyedAsSettled();
        }

        return new PendingDamageStepResult(
                true,
                "",
                currentDamage,
                targetModel.getModelName(),
                appliedDamage,
                wastedDamage,
                targetDestroyed,
                sessionComplete,
                session.pendingDamageCount(),
                session.totalPendingDamage(),
                casualtyUpdate
        );
    }

    public List<WeaponProfile> extraAttackWeapons(UnitInstance unit, WeaponProfile selectedWeapon) {
        if (unit == null) {
            return List.of();
        }

        String selectedKey = UnitInstance.buildWeaponKey(selectedWeapon);
        return unit.getMeleeWeapons().stream()
                .filter(weapon -> WeaponKeywordsService.parse(weapon.description()).extraAttacks())
                .filter(weapon -> !UnitInstance.buildWeaponKey(weapon).equals(selectedKey))
                .collect(Collectors.toList());
    }

    private List<PendingDamage> remapPendingDamages(
            List<PendingDamage> pendingDamages,
            String sourceLabel,
            String weaponName
    ) {
        if (pendingDamages == null || pendingDamages.isEmpty()) {
            return List.of();
        }

        List<PendingDamage> result = new ArrayList<>();
        for (PendingDamage pendingDamage : pendingDamages) {
            result.add(new PendingDamage(
                    sourceLabel,
                    weaponName,
                    pendingDamage.attackSequence(),
                    pendingDamage.damage(),
                    pendingDamage.devastatingWounds()
            ));
        }
        return result;
    }

    private AttackResult annotateCustomRules(AttackResult result, EditorRuleModifiers modifiers) {
        if (result == null || modifiers == null || modifiers.appliedRuleNames().isEmpty()) {
            return result;
        }

        List<String> notes = new ArrayList<>(result.notes());
        notes.add(0, "Custom rules applied: " + String.join(", ", modifiers.appliedRuleNames()) + ".");

        List<String> rollLog = new ArrayList<>();
        rollLog.add("Custom rules applied: " + String.join(", ", modifiers.appliedRuleNames()) + ".");
        String summary = modifierSummary(modifiers);
        if (!summary.isBlank()) {
            rollLog.add(summary);
        }
        rollLog.addAll(result.rollLog());

        return new AttackResult(
                result.resolved(),
                result.weaponName(),
                result.attacks(),
                result.hits(),
                result.wounds(),
                result.unsaved(),
                result.totalDamage(),
                result.modelsDestroyed(),
                notes,
                result.pendingDamages(),
                rollLog
        );
    }

    private WeaponProfile applyEditorModifiersToWeapon(WeaponProfile weapon, EditorRuleModifiers modifiers) {
        if (weapon == null || modifiers == null || !modifiers.hasAnyEffect()) {
            return weapon;
        }

        return new WeaponProfile(
                weapon.weaponID(),
                weapon.name(),
                mergeKeywords(weapon.description(), modifiers.extraWeaponKeywords()),
                weapon.count(),
                weapon.range(),
                applyDiceOrFlatModifier(weapon.a(), modifiers.attacksModifier()),
                weapon.skill(),
                weapon.s(),
                applyIntegerModifier(weapon.ap(), modifiers.apModifier()),
                applyDiceOrFlatModifier(weapon.d(), modifiers.damageModifier()),
                weapon.melee()
        );
    }

    private String modifierSummary(EditorRuleModifiers modifiers) {
        List<String> parts = new ArrayList<>();

        if (modifiers.hitModifier() != 0) {
            parts.add("Hit " + signed(modifiers.hitModifier()));
        }
        if (modifiers.woundModifier() != 0) {
            parts.add("Wound " + signed(modifiers.woundModifier()));
        }
        if (modifiers.attacksModifier() != 0) {
            parts.add("Attacks " + signed(modifiers.attacksModifier()));
        }
        if (modifiers.damageModifier() != 0) {
            parts.add("Damage " + signed(modifiers.damageModifier()));
        }
        if (modifiers.apModifier() != 0) {
            parts.add("AP " + signed(modifiers.apModifier()));
        }
        if (!modifiers.extraWeaponKeywords().isBlank()) {
            parts.add("Added keywords: " + modifiers.extraWeaponKeywords());
        }
        if (modifiers.hitReroll() != null && modifiers.hitReroll().name() != null && modifiers.hitReroll() != eecs2311.group2.wh40k_easycombat.model.editor.EditorRerollType.NONE) {
            parts.add("Hit reroll: " + modifiers.hitReroll());
        }
        if (modifiers.woundReroll() != null && modifiers.woundReroll().name() != null && modifiers.woundReroll() != eecs2311.group2.wh40k_easycombat.model.editor.EditorRerollType.NONE) {
            parts.add("Wound reroll: " + modifiers.woundReroll());
        }

        return parts.isEmpty() ? "" : "Rule effect summary: " + String.join(", ", parts) + ".";
    }

    private String mergeKeywords(String currentDescription, String addedKeywords) {
        String left = currentDescription == null ? "" : currentDescription.trim();
        String right = addedKeywords == null ? "" : addedKeywords.trim();

        if (left.isBlank()) {
            return right;
        }
        if (right.isBlank()) {
            return left;
        }
        return left + ", " + right;
    }

    private String applyIntegerModifier(String rawValue, int modifier) {
        if (modifier == 0) {
            return rawValue;
        }

        String raw = rawValue == null ? "" : rawValue.trim();
        if (raw.isBlank()) {
            return String.valueOf(modifier);
        }

        try {
            return String.valueOf(Integer.parseInt(raw) + modifier);
        } catch (NumberFormatException ignored) {
            return rawValue;
        }
    }

    private String applyDiceOrFlatModifier(String rawValue, int modifier) {
        if (modifier == 0) {
            return rawValue;
        }

        String raw = rawValue == null ? "" : rawValue.trim().toUpperCase().replace(" ", "");
        if (raw.isBlank()) {
            return String.valueOf(modifier);
        }

        try {
            return String.valueOf(Integer.parseInt(raw) + modifier);
        } catch (NumberFormatException ignored) {
        }

        Matcher matcher = DICE_PATTERN.matcher(raw);
        if (!matcher.matches()) {
            return rawValue;
        }

        String count = matcher.group(1).isBlank() ? "1" : matcher.group(1);
        String sides = matcher.group(2);
        int existingModifier = matcher.group(3) == null ? 0 : Integer.parseInt(matcher.group(3));
        int nextModifier = existingModifier + modifier;

        String prefix = "1".equals(count) ? "D" + sides : count + "D" + sides;
        if (nextModifier == 0) {
            return prefix;
        }
        return prefix + (nextModifier > 0 ? "+" + nextModifier : String.valueOf(nextModifier));
    }

    private String signed(int value) {
        return value > 0 ? "+" + value : String.valueOf(value);
    }

    private List<WeaponProfile> availableRangedWeapons(UnitInstance unit, boolean enforcePhaseUsage) {
        List<WeaponProfile> result = new ArrayList<>();

        for (WeaponProfile weapon : unit.getRangedWeapons()) {
            WeaponKeywordsService keywords = WeaponKeywordsService.parse(weapon.description());

            if (enforcePhaseUsage && unit.hasUsedRangedWeaponThisPhase(weapon)) {
                continue;
            }

            if (keywords.oneShot() && unit.hasUsedOneShotWeaponThisBattle(weapon)) {
                continue;
            }

            result.add(weapon);
        }

        return result;
    }

    private List<WeaponProfile> availableFightWeapons(UnitInstance unit) {
        if (unit.hasFoughtThisPhase()) {
            return List.of();
        }

        return List.copyOf(unit.getMeleeWeapons());
    }

    private boolean canFight(Player attackingPlayer, UnitInstance attacker, FightPhaseState fightPhaseState) {
        if (!attacker.isEligibleToFightThisPhase() || attacker.hasFoughtThisPhase()) {
            return false;
        }
        if (fightPhaseState == null || fightPhaseState.step() == FightStep.COMPLETE) {
            return true;
        }
        if (fightPhaseState.step() == FightStep.FIGHTS_FIRST
                && !(attacker.hasFightsFirst() || attacker.hasChargedThisTurn())) {
            return false;
        }

        return fightPhaseState.nextPlayer() == null || fightPhaseState.nextPlayer() == attackingPlayer;
    }

    private String failureMessage(
            AutoBattleMode mode,
            Player activeTurnPlayer,
            Player attackingPlayer,
            FightPhaseState fightPhaseState
    ) {
        if (mode == AutoBattleMode.FIGHT) {
            return fightPhaseState == null ? "That unit cannot fight now." : fightPhaseState.message();
        }
        if (mode == AutoBattleMode.SHOOTING) {
            return activeTurnPlayer == attackingPlayer
                    ? "That unit cannot shoot right now."
                    : "Only the active player may make normal shooting attacks in this phase.";
        }
        return activeTurnPlayer == attackingPlayer
                ? "Only the inactive player may resolve reaction shooting in this phase."
                : "That reaction attack cannot be resolved right now.";
    }
}
