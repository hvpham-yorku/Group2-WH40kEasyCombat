package eecs2311.group2.wh40k_easycombat.service.autobattle;

import eecs2311.group2.wh40k_easycombat.model.combat.AttackResult;
import eecs2311.group2.wh40k_easycombat.model.combat.AutoBattleResolution;
import eecs2311.group2.wh40k_easycombat.model.combat.CasualtyUpdate;
import eecs2311.group2.wh40k_easycombat.model.combat.FightPhaseState;
import eecs2311.group2.wh40k_easycombat.model.combat.PendingDamage;
import eecs2311.group2.wh40k_easycombat.model.combat.PendingDamageStepResult;
import eecs2311.group2.wh40k_easycombat.model.combat.ResolvedAttack;
import eecs2311.group2.wh40k_easycombat.model.instance.Player;
import eecs2311.group2.wh40k_easycombat.model.instance.UnitInstance;
import eecs2311.group2.wh40k_easycombat.model.instance.UnitModelInstance;
import eecs2311.group2.wh40k_easycombat.model.instance.WeaponProfile;
import eecs2311.group2.wh40k_easycombat.service.game.ArmyListStateService;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class AutoBattleService {
    private final AutoBattler autoBattler = new AutoBattler();

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
                AttackResult fightResult = autoBattler.simulateAttack(attacker, defender, fightWeapon, context);
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
            AttackResult primary = autoBattler.simulateAttack(attacker, defender, selectedWeapon, context);
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
