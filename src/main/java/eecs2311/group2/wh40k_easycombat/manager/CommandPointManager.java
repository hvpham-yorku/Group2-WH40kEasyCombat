package eecs2311.group2.wh40k_easycombat.manager;

public final class CommandPointManager {

    private CommandPointManager() {
    }

    public static int parseCp(String text) {
        if (text == null || text.isBlank()) {
            return 0;
        }

        String cleaned = text.replaceAll("[^0-9-]", "").trim();
        if (cleaned.isBlank()) {
            return 0;
        }

        try {
            return Integer.parseInt(cleaned);
        } catch (Exception e) {
            return 0;
        }
    }

    public static String spendCp(String currentCpText, String costText) {
        int current = parseCp(currentCpText);
        int cost = parseCp(costText);
        int result = current - cost;

        if (result < 0) {
            result = 0;
        }

        return String.valueOf(result);
    }

    public static boolean hasEnoughCp(String currentCpText, String costText) {
        return parseCp(currentCpText) >= parseCp(costText);
    }

    public static String addCp(String currentCpText, int amount) {
        int result = parseCp(currentCpText) + amount;
        if (result < 0) {
            result = 0;
        }
        return String.valueOf(result);
    }

    public static int spendCpInt(int current, int cost) {
    return Math.max(0, current - cost);
}

    public static boolean hasEnoughCpInt(int current, int cost) {
        return current >= cost;
    }
}