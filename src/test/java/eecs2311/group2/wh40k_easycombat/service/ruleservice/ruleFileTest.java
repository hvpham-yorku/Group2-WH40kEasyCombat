package eecs2311.group2.wh40k_easycombat.service.ruleservice;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ruleFileTest {

    @Test
    @DisplayName("loadJson parses test JSON resource successfully")
    void loadJsonValid() throws Exception {
        WHRoot root = ruleFile.loadJson("test_rules.json");
        assertNotNull(root);
        assertNotNull(root.pages);
        assertEquals(2, root.pages.size());
        assertEquals("1.0", root.version);
        assertEquals(2, root.success_count);
        assertEquals(2, root.total_count);
    }

    @Test
    @DisplayName("loadJson throws for non-existent resource")
    void loadJsonMissing() {
        assertThrows(IllegalArgumentException.class, () -> ruleFile.loadJson("does_not_exist.json"));
    }

    @Test
    @DisplayName("Loaded pages contain expected content entries")
    void loadJsonContent() throws Exception {
        WHRoot root = ruleFile.loadJson("test_rules.json");
        WHPage firstPage = root.pages.get(0);
        assertNotNull(firstPage.content);
        assertFalse(firstPage.content.isEmpty());

        WHContent firstContent = firstPage.content.get(0);
        assertEquals("MOVEMENT PHASE", firstContent.text);
        assertEquals("paragraph", firstContent.type);
    }

    @Test
    @DisplayName("WHContent fields are deserialized correctly")
    void contentFieldsDeserialized() throws Exception {
        WHRoot root = ruleFile.loadJson("test_rules.json");
        WHPage secondPage = root.pages.get(1);
        // The image entry
        WHContent imageContent = secondPage.content.get(2);
        assertEquals("image", imageContent.type);
        assertEquals("This is an image caption", imageContent.text);
    }
}
