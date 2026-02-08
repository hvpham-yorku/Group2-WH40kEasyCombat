package eecs2311.group2.wh40k_easycombat.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class StringListCodec {
    private StringListCodec() {}

    /** List<String> → "a,b,c" */
    public static String encode(List<String> list) {
        if (list == null || list.isEmpty()) return "";
        return String.join(",", list);
    }

    /** "a,b,c" → List<String> */
    public static List<String> decode(String s) {
        if (s == null || s.isBlank()) return Collections.emptyList();
        String[] parts = s.split(",");
        List<String> list = new ArrayList<>(parts.length);
        for (String part : parts) {
            String trimmed = part.trim();
            if (!trimmed.isEmpty()) {
                list.add(trimmed);
            }
        }
        return list;
    }
}
