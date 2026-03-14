package eecs2311.group2.wh40k_easycombat.service.ruleservice;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ruleSearcherTest {

    private static ruleSearcher searcher;

    @BeforeAll
    static void setUp() throws Exception {
        searcher = new ruleSearcher("test_rules.json");
    }

    // ---- searchAll basic behaviour ----

    @Test
    @DisplayName("Search returns results for a matching query")
    void searchFindsMatch() {
        List<String> results = searcher.searchAll("Movement", 10);
        assertFalse(results.isEmpty(), "Should find at least one result for 'Movement'");
    }

    @Test
    @DisplayName("Search is case-insensitive")
    void searchCaseInsensitive() {
        List<String> upper = searcher.searchAll("CHARGE", 10);
        List<String> lower = searcher.searchAll("charge", 10);
        assertEquals(upper.size(), lower.size(), "Case should not affect results");
    }

    @Test
    @DisplayName("Search returns empty list for non-matching query")
    void searchNoMatch() {
        List<String> results = searcher.searchAll("xyznonexistent", 10);
        assertTrue(results.isEmpty());
    }

    @Test
    @DisplayName("Search returns empty list for null query")
    void searchNullQuery() {
        List<String> results = searcher.searchAll(null, 10);
        assertTrue(results.isEmpty());
    }

    @Test
    @DisplayName("Search returns empty list for empty query")
    void searchEmptyQuery() {
        List<String> results = searcher.searchAll("", 10);
        assertTrue(results.isEmpty());
    }

    @Test
    @DisplayName("Search returns empty list for whitespace-only query")
    void searchWhitespaceQuery() {
        List<String> results = searcher.searchAll("   ", 10);
        assertTrue(results.isEmpty());
    }

    // ---- maxResults limit ----

    @Test
    @DisplayName("Search respects maxResults limit")
    void searchMaxResults() {
        // "phase" should match multiple rules in test data
        List<String> results = searcher.searchAll("phase", 1);
        assertTrue(results.size() <= 1);
    }

    // ---- content filtering ----

    @Test
    @DisplayName("Non-paragraph content types are excluded from rules")
    void nonParagraphExcluded() {
        // "image caption" is type "image" in test data, should not appear
        List<String> results = searcher.searchAll("image caption", 10);
        assertTrue(results.isEmpty(), "Non-paragraph content should be excluded");
    }

    // ---- constructor with invalid path ----

    @Test
    @DisplayName("Constructor throws for non-existent resource")
    void constructorInvalidPath() {
        assertThrows(Exception.class, () -> new ruleSearcher("nonexistent.json"));
    }
}
