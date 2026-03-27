package eecs2311.group2.wh40k_easycombat.service.autobattle;

import eecs2311.group2.wh40k_easycombat.model.combat.AttackResult;
import eecs2311.group2.wh40k_easycombat.model.combat.PendingDamage;
import eecs2311.group2.wh40k_easycombat.model.editor.EditorRerollType;
import eecs2311.group2.wh40k_easycombat.model.instance.UnitInstance;
import eecs2311.group2.wh40k_easycombat.model.instance.UnitModelInstance;
import eecs2311.group2.wh40k_easycombat.model.instance.WeaponProfile;
import eecs2311.group2.wh40k_easycombat.service.calculations.DiceService;
import eecs2311.group2.wh40k_easycombat.service.calculations.ShootingCalculations;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class AutoBattler {
    private static final Pattern DICE_PATTERN = Pattern.compile("^(\\d*)D(3|6)([+-]\\d+)?$");

    public AttackResult simulateAttack(
            UnitInstance attacker,
            UnitInstance defender,
            WeaponProfile selectedWeapon,
            AttackKeywordContext keywordContext
    ) {
        if (attacker == null) {
            return AttackResult.notResolved("Attacker unit not found.");
        }
        if (defender == null) {
            return AttackResult.notResolved("Defender unit not found.");
        }
        if (selectedWeapon == null) {
            return AttackResult.notResolved("Weapon not found.");
        }

        AttackKeywordContext context = keywordContext == null
                ? AttackKeywordContext.none()
                : keywordContext;

        DiceService dice = new DiceService();
        WeaponKeywordsService keywords = WeaponKeywordsService.parse(selectedWeapon.description());
        List<String> notes = new ArrayList<>();
        List<String> rollLog = new ArrayList<>();
        List<PendingDamage> pendingDamages = new ArrayList<>();
        addManualNotes(notes, keywords);

        if (!selectedWeapon.melee() && context.advancedThisTurn() && !keywords.assault()) {
            return AttackResult.notResolved("This unit Advanced, so only Assault weapons can shoot.");
        }

        if (selectedWeapon.melee() && context.fellBackThisTurn()) {
            return AttackResult.notResolved("A unit that Fell Back is not eligible to fight unless another rule says otherwise.");
        }

        if (selectedWeapon.melee() && !context.attackerIsEligibleToFight()) {
            return AttackResult.notResolved("Selected unit is not confirmed eligible to fight.");
        }

        int attacks = resolveTotalAttacks(selectedWeapon, defender, context, keywords, dice, rollLog);
        int hits = 0;
        int wounds = 0;
        int unsaved = 0;
        int totalDamage = 0;
        int attackSequence = 0;

        for (int attackIndex = 1; attackIndex <= attacks; attackIndex++) {
            boolean autoHit = keywords.torrent();
            @SuppressWarnings("unused")
			int hitRoll = 0;
            boolean criticalHit = false;

            if (!autoHit) {
                HitAttempt hitAttempt = resolveHitAttempt(
                        attackIndex,
                        selectedWeapon,
                        keywords,
                        context,
                        dice,
                        rollLog
                );
                if (!hitAttempt.success()) {
                    continue;
                }
                hitRoll = hitAttempt.finalRoll();
                criticalHit = hitAttempt.critical();
            } else {
                rollLog.add("Attack " + attackIndex + ": TORRENT -> automatic hit.");
            }

            int generatedHits = 1;
            if (criticalHit && !isBlank(keywords.sustainedHitsBonus())) {
                RolledValue sustained = rollValueDetailed(keywords.sustainedHitsBonus(), dice);
                generatedHits += sustained.total();
                rollLog.add("Attack " + attackIndex + ": SUSTAINED HITS adds " + sustained.total()
                        + " extra hit(s) from " + sustained.description() + ".");
            }

            hits += generatedHits;

            for (int generatedHitIndex = 1; generatedHitIndex <= generatedHits; generatedHitIndex++) {
                attackSequence++;
                String attemptLabel = "Attack " + attackIndex + "." + generatedHitIndex;

                boolean autoWound = criticalHit
                        && keywords.lethalHits()
                        && generatedHitIndex == 1;

                WoundAttempt woundAttempt = resolveWoundAttempt(
                        attemptLabel,
                        selectedWeapon,
                        defender,
                        context,
                        keywords,
                        autoWound,
                        dice,
                        rollLog
                );

                if (!woundAttempt.success()) {
                    continue;
                }

                wounds++;

                RolledValue damageRoll = rollValueDetailed(selectedWeapon.d(), dice);
                int rawDamage = damageRoll.total();

                if (context.withinHalfRange() && !isBlank(keywords.meltaBonus())) {
                    RolledValue melta = rollValueDetailed(keywords.meltaBonus(), dice);
                    rawDamage += melta.total();
                    rollLog.add(attemptLabel + ": MELTA adds " + melta.total() + " damage from " + melta.description() + ".");
                }

                if (rawDamage <= 0) {
                    rollLog.add(attemptLabel + ": damage roll " + damageRoll.description() + " -> 0 damage.");
                    continue;
                }

                if (keywords.devastatingWounds() && woundAttempt.critical()) {
                    pendingDamages.add(new PendingDamage(
                            selectedWeapon.melee() ? "Fight" : "Shooting",
                            selectedWeapon.name(),
                            attackSequence,
                            rawDamage,
                            true
                    ));
                    unsaved++;
                    totalDamage += rawDamage;
                    rollLog.add(attemptLabel + ": critical wound with DEVASTATING WOUNDS -> pending unsaveable damage " + rawDamage
                            + " from " + damageRoll.description() + ".");
                    continue;
                }

                SaveAttempt saveAttempt = resolveSaveAttempt(
                        attemptLabel,
                        defender,
                        selectedWeapon,
                        context,
                        keywords,
                        dice,
                        rollLog
                );

                if (saveAttempt.saved()) {
                    continue;
                }

                pendingDamages.add(new PendingDamage(
                        selectedWeapon.melee() ? "Fight" : "Shooting",
                        selectedWeapon.name(),
                        attackSequence,
                        rawDamage,
                        false
                ));
                unsaved++;
                totalDamage += rawDamage;
                rollLog.add(attemptLabel + ": save failed -> pending damage " + rawDamage + " from " + damageRoll.description() + ".");
            }
        }

        return new AttackResult(
                true,
                selectedWeapon.name(),
                attacks,
                hits,
                wounds,
                unsaved,
                totalDamage,
                0,
                notes,
                pendingDamages,
                rollLog
        );
    }

    private int resolveTotalAttacks(
            WeaponProfile weapon,
            UnitInstance defender,
            AttackKeywordContext context,
            WeaponKeywordsService keywords,
            DiceService dice,
            List<String> rollLog
    ) {
        int total = 0;

        int weaponCount = context.attackingWeaponBearerCount() > 0
                ? Math.min(context.attackingWeaponBearerCount(), weapon.count())
                : Math.max(1, weapon.count());

        for (int weaponIndex = 1; weaponIndex <= weaponCount; weaponIndex++) {
            RolledValue attacksForOneWeapon = rollValueDetailed(weapon.a(), dice);
            int attackCount = attacksForOneWeapon.total();
            StringBuilder description = new StringBuilder();
            description.append("Weapon bearer ").append(weaponIndex).append(": base attacks ").append(attacksForOneWeapon.description());

            if (context.withinHalfRange() && !isBlank(keywords.rapidFireBonus())) {
                RolledValue rapidFire = rollValueDetailed(keywords.rapidFireBonus(), dice);
                attackCount += rapidFire.total();
                description.append(", RAPID FIRE +").append(rapidFire.total())
                        .append(" from ").append(rapidFire.description());
            }

            if (keywords.blast() && context.blastIsLegal() && !weapon.melee()) {
                int blastBonus = defender.getAliveModelCount() / 5;
                if (blastBonus > 0) {
                    attackCount += blastBonus;
                    description.append(", BLAST +").append(blastBonus);
                }
            }

            attackCount = Math.max(0, attackCount);
            total += attackCount;
            description.append(" -> total ").append(attackCount).append(" attack(s).");
            rollLog.add(description.toString());
        }

        return total;
    }

    private WoundAttempt resolveWoundAttempt(
            String attemptLabel,
            WeaponProfile weapon,
            UnitInstance defender,
            AttackKeywordContext context,
            WeaponKeywordsService keywords,
            boolean autoWound,
            DiceService dice,
            List<String> rollLog
    ) {
        if (autoWound) {
            rollLog.add(attemptLabel + ": LETHAL HITS -> automatic wound.");
            return new WoundAttempt(true, false);
        }

        int woundModifier = (keywords.lance() && context.bearerChargedThisTurn()) ? 1 : 0;
        woundModifier += context.customWoundModifier();

        DieRoll woundRoll = rollD6Detailed(dice);
        boolean criticalWound = isCriticalWound(woundRoll.value(), keywords, context);

        if (scoresWound(woundRoll.value(), woundModifier, weapon, defender, context, criticalWound)) {
            rollLog.add(attemptLabel + ": wound roll " + woundRoll.value()
                    + formatModifier(woundModifier) + " -> wound" + (criticalWound ? " (critical)." : "."));
            return new WoundAttempt(true, criticalWound);
        }

        rollLog.add(attemptLabel + ": wound roll " + woundRoll.value()
                + formatModifier(woundModifier) + " -> failed wound.");

        boolean canRerollFromRule = shouldRerollWound(context.woundRerollType(), woundRoll.value(), woundModifier, weapon, defender, context, criticalWound);
        boolean canRerollFromWeapon = keywords.twinLinked();

        if (canRerollFromRule || canRerollFromWeapon) {
            DieRoll reroll = rollD6Detailed(dice);
            boolean rerollCritical = isCriticalWound(reroll.value(), keywords, context);
            String rerollSource = canRerollFromWeapon && canRerollFromRule
                    ? "available wound reroll"
                    : canRerollFromWeapon
                    ? "TWIN-LINKED reroll"
                    : "custom wound reroll";

            if (scoresWound(reroll.value(), woundModifier, weapon, defender, context, rerollCritical)) {
                rollLog.add(attemptLabel + ": " + rerollSource + " " + reroll.value()
                        + formatModifier(woundModifier) + " -> wound" + (rerollCritical ? " (critical)." : "."));
                return new WoundAttempt(true, rerollCritical);
            }

            rollLog.add(attemptLabel + ": " + rerollSource + " " + reroll.value()
                    + formatModifier(woundModifier) + " -> failed wound.");
        }

        return new WoundAttempt(false, false);
    }

    private HitAttempt resolveHitAttempt(
            int attackIndex,
            WeaponProfile weapon,
            WeaponKeywordsService keywords,
            AttackKeywordContext context,
            DiceService dice,
            List<String> rollLog
    ) {
        int hitModifier = (!weapon.melee() && keywords.heavy() && context.remainedStationary()) ? 1 : 0;
        hitModifier += context.customHitModifier();

        DieRoll firstRoll = rollD6Detailed(dice);
        int requiredSkill = parseRequiredValue(weapon.skill());
        boolean hit = scoresHit(firstRoll.value(), hitModifier, requiredSkill);

        if (hit) {
            rollLog.add("Attack " + attackIndex + ": hit roll " + firstRoll.value()
                    + formatModifier(hitModifier) + " against " + weapon.skill() + " -> hit.");
            return new HitAttempt(true, firstRoll.value() == 6, firstRoll.value());
        }

        rollLog.add("Attack " + attackIndex + ": hit roll " + firstRoll.value()
                + formatModifier(hitModifier) + " against " + weapon.skill() + " -> miss.");

        if (!shouldRerollHit(context.hitRerollType(), firstRoll.value(), hitModifier, requiredSkill)) {
            return new HitAttempt(false, false, firstRoll.value());
        }

        DieRoll reroll = rollD6Detailed(dice);
        boolean rerolledHit = scoresHit(reroll.value(), hitModifier, requiredSkill);
        rollLog.add("Attack " + attackIndex + ": custom hit reroll " + reroll.value()
                + formatModifier(hitModifier) + " against " + weapon.skill() + " -> " + (rerolledHit ? "hit." : "miss."));
        return new HitAttempt(rerolledHit, reroll.value() == 6, reroll.value());
    }

    private SaveAttempt resolveSaveAttempt(
            String attemptLabel,
            UnitInstance defender,
            WeaponProfile weapon,
            AttackKeywordContext context,
            WeaponKeywordsService keywords,
            DiceService dice,
            List<String> rollLog
    ) {
        UnitModelInstance targetModel = referenceTargetModel(defender, context);
        if (targetModel == null) {
            rollLog.add(attemptLabel + ": no valid target model remained for the save.");
            return new SaveAttempt(false);
        }

        int armourSave = parseRequiredValue(targetModel.getSv());

        if (context.targetHasBenefitOfCover() && !keywords.ignoresCover() && !weapon.melee()) {
            armourSave = Math.max(2, armourSave - 1);
        }

        int armourAfterAp = ShootingCalculations.effectiveSave(armourSave, parseSignedValue(weapon.ap()));
        int invulnerableSave = parseOptionalRequiredValue(targetModel.getInv());

        int requiredSave = invulnerableSave > 0
                ? Math.min(armourAfterAp, invulnerableSave)
                : armourAfterAp;

        requiredSave = Math.max(2, requiredSave);

        DieRoll saveRoll = rollD6Detailed(dice);
        if (saveRoll.value() == 1) {
            rollLog.add(attemptLabel + ": save roll 1 -> automatic failed save.");
            return new SaveAttempt(false);
        }

        boolean saved = saveRoll.value() >= requiredSave;
        rollLog.add(attemptLabel + ": save roll " + saveRoll.value() + " against " + requiredSave
                + "+ using " + targetModel.getModelName() + " -> " + (saved ? "saved." : "failed."));
        return new SaveAttempt(saved);
    }

    private boolean scoresHit(int unmodifiedHitRoll, int modifier, int requiredSkill) {
        if (unmodifiedHitRoll == 1) {
            return false;
        }
        if (unmodifiedHitRoll == 6) {
            return true;
        }
        return (unmodifiedHitRoll + modifier) >= requiredSkill;
    }

    private boolean scoresWound(
            int unmodifiedWoundRoll,
            int modifier,
            WeaponProfile weapon,
            UnitInstance defender,
            AttackKeywordContext context,
            boolean criticalWound
    ) {
        if (criticalWound) {
            return true;
        }
        if (unmodifiedWoundRoll == 1) {
            return false;
        }
        if (unmodifiedWoundRoll == 6) {
            return true;
        }

        UnitModelInstance targetModel = referenceTargetModel(defender, context);
        if (targetModel == null) {
            return false;
        }

        int strength = parseStatValue(weapon.s());
        int toughness = parseStatValue(targetModel.getT());
        int required = ShootingCalculations.requiredWoundRoll(strength, toughness);

        return (unmodifiedWoundRoll + modifier) >= required;
    }

    private boolean isCriticalWound(
            int unmodifiedWoundRoll,
            WeaponKeywordsService keywords,
            AttackKeywordContext context
    ) {
        return unmodifiedWoundRoll == 6 || keywords.antiTriggers(context, unmodifiedWoundRoll);
    }

    private boolean shouldRerollHit(EditorRerollType rerollType, int roll, int modifier, int requiredSkill) {
        if (rerollType == null || rerollType == EditorRerollType.NONE) {
            return false;
        }
        if (rerollType == EditorRerollType.ONES) {
            return roll == 1;
        }
        return !scoresHit(roll, modifier, requiredSkill);
    }

    private boolean shouldRerollWound(
            EditorRerollType rerollType,
            int roll,
            int modifier,
            WeaponProfile weapon,
            UnitInstance defender,
            AttackKeywordContext context,
            boolean criticalWound
    ) {
        if (rerollType == null || rerollType == EditorRerollType.NONE) {
            return false;
        }
        if (rerollType == EditorRerollType.ONES) {
            return roll == 1;
        }
        return !scoresWound(roll, modifier, weapon, defender, context, criticalWound);
    }

    private UnitModelInstance referenceTargetModel(
            UnitInstance defender,
            AttackKeywordContext context
    ) {
        if (defender == null || defender.getModels().isEmpty()) {
            return null;
        }

        if (context != null
                && context.applyPrecisionToChosenModel()
                && context.preferredDefenderModelId() != null
                && !context.preferredDefenderModelId().isBlank()) {
            for (UnitModelInstance model : defender.getModels()) {
                if (!model.isDestroyed()
                        && context.preferredDefenderModelId().equals(model.getInstanceId())) {
                    return model;
                }
            }
        }

        for (UnitModelInstance model : defender.getModels()) {
            if (!model.isDestroyed() && model.getCurrentHp() < model.getMaxHp()) {
                return model;
            }
        }

        for (UnitModelInstance model : defender.getModels()) {
            if (!model.isDestroyed()) {
                return model;
            }
        }

        return null;
    }

    private RolledValue rollValueDetailed(String raw, DiceService dice) {
        if (raw == null || raw.isBlank()) {
            return new RolledValue(0, "0");
        }

        String normalized = raw.trim()
                .toUpperCase(Locale.ROOT)
                .replace(" ", "");

        String numberLike = normalized.replace("+", "");
        try {
            int fixed = Integer.parseInt(numberLike);
            return new RolledValue(fixed, normalized);
        } catch (NumberFormatException ignored) {
        }

        Matcher matcher = DICE_PATTERN.matcher(normalized);
        if (!matcher.matches()) {
            return new RolledValue(0, normalized);
        }

        int count = matcher.group(1).isBlank() ? 1 : Integer.parseInt(matcher.group(1));
        int sides = Integer.parseInt(matcher.group(2));
        int modifier = matcher.group(3) == null ? 0 : Integer.parseInt(matcher.group(3));

        dice.rollDice(count);

        int total = 0;
        List<Integer> rawRolls = new ArrayList<>(dice.getResults());
        for (Integer roll : rawRolls) {
            if (sides == 3) {
                total += (roll + 1) / 2;
            } else {
                total += roll;
            }
        }

        int finalTotal = total + modifier;
        return new RolledValue(finalTotal, normalized + " " + rawRolls + (modifier == 0 ? "" : " " + formatModifier(modifier)) + " = " + finalTotal);
    }

    private DieRoll rollD6Detailed(DiceService dice) {
        dice.rollDice(1);
        return new DieRoll(dice.getResults().get(0));
    }

    private int parseRequiredValue(String raw) {
        if (raw == null || raw.isBlank() || "N/A".equalsIgnoreCase(raw)) {
            return 7;
        }
        String cleaned = raw.replace("+", "").trim();
        try {
            return Integer.parseInt(cleaned);
        } catch (Exception e) {
            return 7;
        }
    }

    private int parseOptionalRequiredValue(String raw) {
        if (raw == null || raw.isBlank() || "-".equals(raw) || "N/A".equalsIgnoreCase(raw)) {
            return -1;
        }
        return parseRequiredValue(raw);
    }

    private int parseStatValue(String raw) {
        if (raw == null || raw.isBlank() || "N/A".equalsIgnoreCase(raw)) {
            return 0;
        }
        String cleaned = raw.replaceAll("[^0-9-]", "").trim();
        if (cleaned.isBlank()) {
            return 0;
        }
        try {
            return Integer.parseInt(cleaned);
        } catch (Exception e) {
            return 0;
        }
    }

    private int parseSignedValue(String raw) {
        return parseStatValue(raw);
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }

    private String formatModifier(int modifier) {
        if (modifier == 0) {
            return "";
        }
        return modifier > 0 ? " + " + modifier : " - " + Math.abs(modifier);
    }

    private void addManualNotes(List<String> notes, WeaponKeywordsService keywords) {
        if (keywords.assault()) {
            notes.add("[ASSAULT] Confirm in the UI whether this unit Advanced this turn.");
        }
        if (keywords.pistol()) {
            notes.add("[PISTOL] Confirm engagement-range restrictions in the UI.");
        }
        if (keywords.extraAttacks()) {
            notes.add("[EXTRA ATTACKS] This weapon is resolved in addition to the selected main melee weapon.");
        }
        if (keywords.precision()) {
            notes.add("[PRECISION] If applicable, choose the specific defending model in the UI.");
        }
        if (keywords.oneShot()) {
            notes.add("[ONE SHOT] This weapon should only be selected once per battle.");
        }
        if (keywords.hazardous()) {
            notes.add("[HAZARDOUS] Resolve a Hazardous test after this weapon finishes resolving.");
        }
        if (keywords.antiInfantry() != null
                || keywords.antiVehicle() != null
                || keywords.antiMonster() != null
                || keywords.antiCharacter() != null
                || keywords.antiPsyker() != null) {
            notes.add("[ANTI-X] Confirm the target keywords in the UI.");
        }
        if (keywords.blast()) {
            notes.add("[BLAST] Confirm Blast legality in the UI.");
        }
    }

    private record WoundAttempt(boolean success, boolean critical) {
    }

    private record HitAttempt(boolean success, boolean critical, int finalRoll) {
    }

    private record SaveAttempt(boolean saved) {
    }

    private record DieRoll(int value) {
    }

    private record RolledValue(int total, String description) {
    }
}
