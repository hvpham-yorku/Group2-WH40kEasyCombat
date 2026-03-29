package eecs2311.group2.wh40k_easycombat.controller.helper;

import eecs2311.group2.wh40k_easycombat.controller.GameUIController;
import eecs2311.group2.wh40k_easycombat.manager.StratagemUseManager;
import eecs2311.group2.wh40k_easycombat.model.editor.EditorActiveEffect;
import eecs2311.group2.wh40k_easycombat.model.editor.EditorRuleDefinition;
import eecs2311.group2.wh40k_easycombat.model.instance.Player;
import eecs2311.group2.wh40k_easycombat.service.BattleLogService;
import eecs2311.group2.wh40k_easycombat.service.editor.EditorEffectRuntimeService;
import eecs2311.group2.wh40k_easycombat.service.game.GameEngine;
import eecs2311.group2.wh40k_easycombat.viewmodel.GameArmyUnitVM;
import eecs2311.group2.wh40k_easycombat.viewmodel.GameStrategyVM;

import java.util.List;
import java.util.function.IntSupplier;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public final class GameUIStrategyHelper {

    private GameUIStrategyHelper() {
    }

    public static void useSelectedStrategy(
            GameEngine gameEngine,
            EditorEffectRuntimeService editorEffectRuntimeService,
            BattleLogService battleLogService,
            GameUIController.ArmySide side,
            GameStrategyVM selectedStrategy,
            StratagemUseManager.BattleSide battleSide,
            String currentCpText,
            Supplier<List<GameArmyUnitVM>> candidateUnitsSupplier,
            StratagemTargetOpener targetOpener,
            Runnable syncScoreLabels,
            Runnable refreshArmyViews,
            IntSupplier currentCpSupplier,
            SideTextResolver sideTextResolver
    ) {
        if (gameEngine.isBattleOver()) {
            DialogHelper.showInfo("Battle Over", gameEngine.winnerText());
            return;
        }

        int beforeCp = currentCpSupplier.getAsInt();
        StratagemUseManager.UseResult result = StratagemUseManager.useStrategy(
                battleSide,
                selectedStrategy == null ? null : selectedStrategy.getStrategy(),
                currentCpText
        );

        if (!result.success()) {
            DialogHelper.showWarning(result.title(), result.message());
            return;
        }

        List<EditorRuleDefinition> matchingRules =
                editorEffectRuntimeService.matchingStratagemRules(selectedStrategy == null ? null : selectedStrategy.getStrategy());

        String confirmText = "Use stratagem \"" + result.title() + "\"?";
        if (!matchingRules.isEmpty()) {
            confirmText += "\n\nThis will also trigger "
                    + matchingRules.size()
                    + " custom rule"
                    + (matchingRules.size() == 1 ? "" : "s")
                    + " and prompt you to choose one affected unit.";
        }

        if (!DialogHelper.confirmYesNo("Confirm Stratagem", confirmText)) {
            return;
        }

        GameArmyUnitVM targetedUnit = null;
        if (!matchingRules.isEmpty()) {
            List<GameArmyUnitVM> candidates = candidateUnitsSupplier.get().stream()
                    .filter(vm -> vm != null && !vm.isDestroyed())
                    .collect(Collectors.toList());

            if (candidates.isEmpty()) {
                DialogHelper.showWarning(
                        "No Valid Unit",
                        "This stratagem has matching custom effects, but there is no living unit to receive them."
                );
                return;
            }

            targetedUnit = targetOpener.open(
                    sideTextResolver.sideLabel().apply(side),
                    selectedStrategy.getName(),
                    matchingRules,
                    candidates
            );
            if (targetedUnit == null) {
                return;
            }
        }

        Player sidePlayer = side == GameUIController.ArmySide.BLUE ? Player.ATTACKER : Player.DEFENDER;
        gameEngine.setCommandPoints(sidePlayer, parseInt(result.nextCpText()));
        syncScoreLabels.run();

        List<String> activatedLabels = List.of();
        if (targetedUnit != null && selectedStrategy != null) {
            activatedLabels = editorEffectRuntimeService.activateStratagemRules(
                            selectedStrategy.getStrategy(),
                            targetedUnit.getUnit(),
                            sidePlayer,
                            gameEngine.getActivePlayer(),
                            gameEngine.getCurrentPhase(),
                            gameEngine.getCurrentRound()
                    ).stream()
                    .map(EditorActiveEffect::displayName)
                    .collect(Collectors.toList());
        }

        StringBuilder info = new StringBuilder(result.message());
        if (!activatedLabels.isEmpty()) {
            info.append("\n\nAffected Unit: ").append(targetedUnit.getUnitName());
            info.append("\nActivated Effects:");
            for (String label : activatedLabels) {
                info.append("\n- ").append(label);
            }
        }

        DialogHelper.showInfo(result.title(), info.toString());
        int afterCp = parseInt(result.nextCpText());
        StringBuilder log = new StringBuilder();
        log.append(sideTextResolver.playerLabel().apply(sidePlayer))
                .append(" used stratagem \"")
                .append(result.title())
                .append("\". CP ")
                .append(beforeCp)
                .append(" -> ")
                .append(afterCp)
                .append(".");
        if (targetedUnit != null) {
            log.append(" Affected unit: ").append(targetedUnit.getUnitName()).append(".");
        }
        if (!activatedLabels.isEmpty()) {
            log.append(" Activated effects: ").append(String.join(", ", activatedLabels)).append(".");
        }
        battleLogService.logTurnEvent(
                gameEngine.getCurrentRound(),
                gameEngine.getCurrentPhase(),
                sidePlayer,
                log.toString()
        );
        refreshArmyViews.run();
    }

    private static int parseInt(String text) {
        try {
            return Integer.parseInt(text == null ? "" : text.trim());
        } catch (Exception ignored) {
            return 0;
        }
    }

    @FunctionalInterface
    public interface StratagemTargetOpener {
        GameArmyUnitVM open(
                String sideLabel,
                String stratagemName,
                List<EditorRuleDefinition> matchingRules,
                List<GameArmyUnitVM> candidates
        );
    }

    public record SideTextResolver(
            java.util.function.Function<GameUIController.ArmySide, String> sideLabel,
            java.util.function.Function<Player, String> playerLabel
    ) {
    }
}
