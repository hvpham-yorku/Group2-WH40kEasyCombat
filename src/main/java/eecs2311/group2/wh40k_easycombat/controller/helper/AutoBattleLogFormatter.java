package eecs2311.group2.wh40k_easycombat.controller.helper;

import eecs2311.group2.wh40k_easycombat.model.combat.AttackResult;
import eecs2311.group2.wh40k_easycombat.model.combat.AutoBattleResolution;
import eecs2311.group2.wh40k_easycombat.model.combat.CasualtyUpdate;
import eecs2311.group2.wh40k_easycombat.model.combat.PendingDamage;
import eecs2311.group2.wh40k_easycombat.model.combat.PendingDamageStepResult;
import eecs2311.group2.wh40k_easycombat.model.combat.ResolvedAttack;
import eecs2311.group2.wh40k_easycombat.model.instance.Phase;
import eecs2311.group2.wh40k_easycombat.model.instance.Player;
import eecs2311.group2.wh40k_easycombat.service.BattleLogService;
import eecs2311.group2.wh40k_easycombat.service.autobattle.AutoBattleMode;
import eecs2311.group2.wh40k_easycombat.service.autobattle.PendingDamageSession;
import javafx.scene.control.TextArea;

public final class AutoBattleLogFormatter {

    private AutoBattleLogFormatter() {
    }

    public static void logResolution(
            BattleLogService battleLogService,
            TextArea battleResultBox,
            TextArea rollLogBox,
            int currentRound,
            Phase currentPhase,
            Player attackerSide,
            AutoBattleMode battleMode,
            String attackerUnitName,
            String defenderUnitName,
            AutoBattleResolution resolution
    ) {
        StringBuilder battle = new StringBuilder();
        battle.append("==================================================\n")
                .append(AutoBattleViewHelper.label(attackerSide))
                .append(" | ")
                .append(battleMode.attackLabel())
                .append("\n")
                .append("Attacker Unit: ")
                .append(attackerUnitName)
                .append("\n")
                .append("Target Unit: ")
                .append(defenderUnitName)
                .append("\n\n");

        int totalPendingCount = 0;
        int totalPendingDamage = 0;
        for (ResolvedAttack attack : resolution.attacks()) {
            AttackResult result = attack.result();
            totalPendingCount += result.pendingDamages().size();
            totalPendingDamage += result.pendingDamages().stream().mapToInt(PendingDamage::damage).sum();
            battle.append(attack.label()).append(" - ").append(attack.weaponName()).append("\n")
                    .append("Attacks: ").append(result.attacks()).append("\n")
                    .append("Hits: ").append(result.hits()).append("\n")
                    .append("Wounds: ").append(result.wounds()).append("\n")
                    .append("Unsaved Attacks: ").append(result.unsaved()).append("\n")
                    .append("Pending Damage: ").append(result.totalDamage()).append("\n");
            if (!result.notes().isEmpty()) {
                battle.append("Notes:\n");
                for (String note : result.notes()) {
                    battle.append("- ").append(note).append("\n");
                }
            }
            battle.append("\n");

            rollLogBox.appendText("==================================================\n" + attack.label() + " - " + attack.weaponName() + "\n");
            for (String line : result.rollLog()) {
                rollLogBox.appendText(line + "\n");
            }
            rollLogBox.appendText("\n");

            StringBuilder summary = new StringBuilder();
            summary.append(AutoBattleViewHelper.label(attackerSide))
                    .append(" ")
                    .append(attackerUnitName)
                    .append(" resolved ")
                    .append(attack.label())
                    .append(" with ")
                    .append(attack.weaponName())
                    .append(" against ")
                    .append(defenderUnitName)
                    .append(": attacks ")
                    .append(result.attacks())
                    .append(", hits ")
                    .append(result.hits())
                    .append(", wounds ")
                    .append(result.wounds())
                    .append(", unsaved ")
                    .append(result.unsaved())
                    .append(", pending damage ")
                    .append(result.totalDamage())
                    .append(".");
            if (!result.notes().isEmpty()) {
                summary.append(" Notes: ").append(String.join(" ", result.notes()));
            }
            battleLogService.logTurnEvent(currentRound, currentPhase, attackerSide, summary.toString());
        }
        if (totalPendingCount > 0) {
            battle.append("Pending Allocation:\n- Unsaved attacks waiting for defender choice: ")
                    .append(totalPendingCount)
                    .append("\n- Total pending damage across those attacks: ")
                    .append(totalPendingDamage)
                    .append("\n\n");
        } else {
            battle.append("No unsaved damage was generated.\n\n");
        }
        if (resolution.hazardousTriggered()) {
            battle.append("Hazardous: resolve the Hazardous test manually after this attack sequence.\n\n");
        }
        battleResultBox.appendText(battle.toString());
    }

    public static void logAllocation(
            BattleLogService battleLogService,
            TextArea battleResultBox,
            int currentRound,
            Phase currentPhase,
            PendingDamageSession currentPendingSession,
            PendingDamageStepResult result
    ) {
        StringBuilder battle = new StringBuilder();
        CasualtyUpdate casualty = result.casualtyUpdate();
        battle.append("Allocation Result\n");
        if (result.resolvedDamage() != null) {
            battle.append("- Source: ").append(result.resolvedDamage().displayLabel()).append("\n");
        }
        battle.append("- Target Model: ").append(result.targetModelName()).append("\n")
                .append("- Applied Damage: ").append(result.appliedDamage()).append("\n");
        if (result.wastedDamage() > 0) {
            battle.append("- Overflow Lost: ").append(result.wastedDamage()).append("\n");
        }
        if (result.targetDestroyed()) {
            battle.append("- Target Model Status: Destroyed\n");
        }
        if (casualty.newlyDestroyedModels() > 0) {
            battle.append("- Destroyed Models Finalized: ")
                    .append(String.join(", ", casualty.destroyedModelNames()))
                    .append("\n");
        }
        if (!casualty.removedWeaponNames().isEmpty()) {
            battle.append("- Weapon Count Changes: ")
                    .append(String.join(", ", casualty.removedWeaponNames()))
                    .append("\n");
        }
        if (result.sessionComplete()) {
            if (casualty.defenderDestroyed()) {
                battle.append("- Defender Unit Status: Destroyed and moved to the bottom of the army list.\n");
            }
        } else {
            battle.append("- Remaining Pending Unsaved Attacks: ").append(result.remainingPendingCount()).append("\n")
                    .append("- Remaining Pending Damage: ").append(result.remainingPendingDamage()).append("\n");
        }
        battle.append("\n");
        battleResultBox.appendText(battle.toString());

        if (currentPendingSession != null) {
            StringBuilder summary = new StringBuilder();
            summary.append(AutoBattleViewHelper.label(AutoBattleViewHelper.opposite(currentPendingSession.attackingPlayer())))
                    .append(" allocated ")
                    .append(result.resolvedDamage() == null ? "pending damage" : result.resolvedDamage().displayLabel())
                    .append(" to ")
                    .append(result.targetModelName())
                    .append(": applied ")
                    .append(result.appliedDamage())
                    .append(" damage");
            if (result.wastedDamage() > 0) {
                summary.append(", ").append(result.wastedDamage()).append(" overflow lost");
            }
            summary.append(".");
            if (result.targetDestroyed()) {
                summary.append(" Target model destroyed.");
            }
            if (casualty.newlyDestroyedModels() > 0) {
                summary.append(" Destroyed models finalized: ")
                        .append(String.join(", ", casualty.destroyedModelNames()))
                        .append(".");
            }
            if (casualty.defenderDestroyed()) {
                summary.append(" Defender unit destroyed.");
            } else if (!result.sessionComplete()) {
                summary.append(" Remaining pending attacks: ")
                        .append(result.remainingPendingCount())
                        .append(", remaining pending damage: ")
                        .append(result.remainingPendingDamage())
                        .append(".");
            }
            battleLogService.logTurnEvent(
                    currentRound,
                    currentPhase,
                    AutoBattleViewHelper.opposite(currentPendingSession.attackingPlayer()),
                    summary.toString()
            );
        }
    }
}
