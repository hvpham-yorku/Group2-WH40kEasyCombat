package eecs2311.group2.wh40k_easycombat.util;

public final class StringUtils {

    private StringUtils() {}

    public static String safe(String value) {
        return value == null ? "" : value.trim();
    }

    public static String safeLower(String value) {
        return value == null ? "" : value.trim().toLowerCase();
    }
}