package eecs2311.group2.wh40k_easycombat.service.ruleservice;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ruleSorterTest {

    @Test
    @DisplayName("Sort orders strings case-insensitively")
    void sortCaseInsensitive() {
        List<String> rules = new ArrayList<>(List.of("Charge", "advance", "Battle", "fight"));
        ruleSorter.sort(rules);

        assertEquals("advance", rules.get(0));
        assertEquals("Battle", rules.get(1));
        assertEquals("Charge", rules.get(2));
        assertEquals("fight", rules.get(3));
    }

    @Test
    @DisplayName("Sort handles empty list without error")
    void sortEmptyList() {
        List<String> rules = new ArrayList<>();
        ruleSorter.sort(rules);
        assertTrue(rules.isEmpty());
    }

    @Test
    @DisplayName("Sort handles single element list")
    void sortSingleElement() {
        List<String> rules = new ArrayList<>(List.of("only"));
        ruleSorter.sort(rules);
        assertEquals(1, rules.size());
        assertEquals("only", rules.get(0));
    }

    @Test
    @DisplayName("Sort handles already-sorted list")
    void sortAlreadySorted() {
        List<String> rules = new ArrayList<>(List.of("alpha", "beta", "gamma"));
        ruleSorter.sort(rules);
        assertEquals(List.of("alpha", "beta", "gamma"), rules);
    }
}
