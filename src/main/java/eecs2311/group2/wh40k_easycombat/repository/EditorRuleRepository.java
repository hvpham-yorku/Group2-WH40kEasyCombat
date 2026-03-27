package eecs2311.group2.wh40k_easycombat.repository;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import eecs2311.group2.wh40k_easycombat.model.editor.EditorRuleDefinition;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class EditorRuleRepository {
    private static final Path STORAGE_PATH = Path.of("data", "editor-rules.json");

    private final ObjectMapper mapper = new ObjectMapper()
            .enable(SerializationFeature.INDENT_OUTPUT);

    public List<EditorRuleDefinition> loadAll() {
        if (!Files.exists(STORAGE_PATH)) {
            return new ArrayList<>();
        }

        try {
            return mapper.readValue(
                    STORAGE_PATH.toFile(),
                    new TypeReference<List<EditorRuleDefinition>>() {}
            );
        } catch (IOException e) {
            throw new RuntimeException("Failed to load custom editor rules from " + STORAGE_PATH + ": " + e.getMessage(), e);
        }
    }

    public void saveAll(List<EditorRuleDefinition> rules) {
        try {
            Files.createDirectories(STORAGE_PATH.getParent());
            mapper.writeValue(STORAGE_PATH.toFile(), rules == null ? List.of() : rules);
        } catch (IOException e) {
            throw new RuntimeException("Failed to save custom editor rules to " + STORAGE_PATH + ": " + e.getMessage(), e);
        }
    }
}
