package eecs2311.group2.wh40k_easycombat.service;

import java.util.Collections;
import java.util.List;

public class ruleSorter {
    public static void sort(List<String> rules) {
        Collections.sort(rules, String.CASE_INSENSITIVE_ORDER);
    }
}