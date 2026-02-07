package eecs2311.group2.wh40k_easycombat.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class StringListCodec {
    private StringListCodec() {}

    /** List<Integer> → "1,2,3" */
    public static String encode(List<Integer> list) {
        if (list == null || list.isEmpty()) return "";
        return String.join(",", list.stream()
            .map(String::valueOf)
            .toArray(String[]::new));
    }

    /** "1,2,3" → List<Integer> */
    public static List<Integer> decode(String s) {
        if (s == null || s.isBlank()) return Collections.emptyList();
        String[] parts = s.split(",");
        List<Integer> list = new ArrayList<>(parts.length);
        for (String part : parts) {
            try {
                list.add(Integer.parseInt(part.trim()));
            } catch (NumberFormatException e) {
                //XXX: skip the value if there is issue
            }
        }
        return list;
    }
}
