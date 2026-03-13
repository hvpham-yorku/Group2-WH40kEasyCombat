package eecs2311.group2.wh40k_easycombat.manager;

import eecs2311.group2.wh40k_easycombat.model.instance.StratagemInstance;

public final class StratagemUseManager {

    private StratagemUseManager() {
    }

    public static UseResult useStrategy(
            BattleSide side,
            StratagemInstance selected,
            String currentCpText
    ) {
        if (selected == null) {
            return UseResult.failure("No Stratagem Selected", "Please select one stratagem first.");
        }

        if (!CommandPointManager.hasEnoughCp(currentCpText, selected.cpCost())) {
            return UseResult.failure("Not Enough CP", "You do not have enough CP to use this stratagem.");
        }

        String nextCp = CommandPointManager.spendCp(currentCpText, selected.cpCost());
        String content = buildStrategyUseText(side, selected);

        return UseResult.success(selected.name(), content, nextCp);
    }

    private static String buildStrategyUseText(BattleSide side, StratagemInstance strategy) {
        String sideName = side == BattleSide.BLUE ? "Blue" : "Red";

        StringBuilder sb = new StringBuilder();
        sb.append(sideName)
                .append(" used ")
                .append(strategy.name());

        if (strategy.cpCost() != null && !strategy.cpCost().isBlank()) {
            sb.append(" (").append(strategy.cpCost()).append(" CP)");
        }

        sb.append("\n");

        if (strategy.turn() != null && !strategy.turn().isBlank()) {
            sb.append("Turn: ").append(strategy.turn()).append("\n");
        }

        if (strategy.phase() != null && !strategy.phase().isBlank()) {
            sb.append("Phase: ").append(strategy.phase()).append("\n");
        }

        String description = htmlToPlainText(strategy.descriptionHtml());
        if (!description.isBlank()) {
            sb.append("\n").append(description);
        }

        return sb.toString().trim();
    }

    private static String htmlToPlainText(String html) {
        if (html == null || html.isBlank()) return "";

        String s = html;
        s = s.replace("<br><br>", "\n\n");
        s = s.replace("<br/>", "\n");
        s = s.replace("<br />", "\n");
        s = s.replace("<br>", "\n");

        s = s.replaceAll("(?i)</b>", "");
        s = s.replaceAll("(?i)<b>", "");

        s = s.replace("&nbsp;", " ");
        s = s.replace("&lt;", "<");
        s = s.replace("&gt;", ">");
        s = s.replace("&amp;", "&");
        s = s.replace("&quot;", "\"");
        s = s.replace("&#39;", "'");

        s = s.replaceAll("(?is)<[^>]+>", "");
        return s.trim();
    }

    public enum BattleSide {
        BLUE, RED
    }

    public record UseResult(
            boolean success,
            String title,
            String message,
            String nextCpText
    ) {
        public static UseResult success(String title, String message, String nextCpText) {
            return new UseResult(true, title, message, nextCpText);
        }

        public static UseResult failure(String title, String message) {
            return new UseResult(false, title, message, null);
        }
    }
}
