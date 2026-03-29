package eecs2311.group2.wh40k_easycombat.controller.helper;

import eecs2311.group2.wh40k_easycombat.service.ArmyWh40kAppImportService;

import java.util.List;

public final class ArmyImportSummaryFormatter {

    private ArmyImportSummaryFormatter() {
    }

    public static String buildImportSummary(
            ArmyWh40kAppImportService.ImportResult result,
            int totalPoints,
            List<String> importWarnings
    ) {
        StringBuilder summary = new StringBuilder();
        summary.append("Army Name: ").append(result.armyName()).append('\n');
        summary.append("Imported Units: ").append(result.units().size()).append('\n');
        summary.append("Total Points: ").append(totalPoints).append('\n');

        if (!result.skippedUnits().isEmpty()) {
            summary.append("\nSkipped Units:\n");
            for (String skippedUnit : result.skippedUnits()) {
                summary.append("- ").append(skippedUnit).append('\n');
            }
        }

        if (!result.skippedItems().isEmpty()) {
            summary.append("\nSkipped Equipment:\n");
            for (String skippedItem : result.skippedItems()) {
                summary.append("- ").append(skippedItem).append('\n');
            }
        }

        if (!importWarnings.isEmpty()) {
            summary.append("\nWarnings:\n");
            for (String warning : importWarnings) {
                summary.append("- ").append(warning).append('\n');
            }
        }

        return summary.toString().trim();
    }
}
