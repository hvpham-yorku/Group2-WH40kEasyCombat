package eecs2311.group2.wh40k_easycombat.service.editor;

import eecs2311.group2.wh40k_easycombat.model.editor.EditorRuleDefinition;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RuleEditorServiceTest {

    @TempDir
    Path tempDir;

    @Test
    void importRuleLoadsRuleFileAndStoresItAsCustomRule() throws Exception {
        InMemoryRuleFileStore fileStore = new InMemoryRuleFileStore();
        RuleEditorService service = new RuleEditorService(fileStore);
        VisualVmScriptBuilder scriptBuilder = new VisualVmScriptBuilder();

        Path importFile = tempDir.resolve("intercessor.rule");
        EditorRuleDefinition exportedRule = new EditorRuleDefinition();
        exportedRule.setId("imported-rule");
        exportedRule.setName("Imported Intercessor Rule");
        exportedRule.setWeaponNameContains("Bolt rifle");
        exportedRule.setPhase(eecs2311.group2.wh40k_easycombat.model.editor.EditorRulePhase.SHOOTING);
        exportedRule.setAttackType(eecs2311.group2.wh40k_easycombat.model.editor.EditorRuleAttackType.RANGED);
        exportedRule.setVisualAttacksModifier(2);
        exportedRule.setDslScript(scriptBuilder.build(exportedRule));
        service.exportRule(exportedRule, importFile);

        EditorRuleDefinition imported = service.importRule(importFile);

        assertEquals("Imported Intercessor Rule", imported.getName());
        assertEquals("Bolt rifle", imported.getWeaponNameContains());
        assertEquals(1, service.getRules().size());
        assertEquals("Imported Intercessor Rule", service.getRules().get(0).getName());
    }

    @Test
    void exportRuleWritesMetadataAndDslScriptToChosenFile() throws Exception {
        InMemoryRuleFileStore fileStore = new InMemoryRuleFileStore();
        RuleEditorService service = new RuleEditorService(fileStore);

        EditorRuleDefinition rule = new EditorRuleDefinition();
        rule.setName("Exported Rule");
        rule.setWeaponNameContains("Bolt rifle");
        rule.setDslScript("""
                0 -> hit_modifier
                0 -> wound_modifier
                2 -> attacks_modifier
                0 -> damage_modifier
                0 -> ap_modifier
                0 -> hit_reroll
                0 -> wound_reroll
                "" -> extra_weapon_keywords
                """);

        Path exportFile = tempDir.resolve("exported.rule");
        service.exportRule(rule, exportFile);

        String exportedText = Files.readString(exportFile);
        assertTrue(exportedText.contains("# @name=Exported Rule"));
        assertTrue(exportedText.contains("# @weaponNameContains=Bolt rifle"));
        assertTrue(exportedText.contains("2 -> attacks_modifier"));
    }

    @Test
    void importRuleRejectsVmScriptThatDoesNotMatchVisualEditorFormat() throws Exception {
        InMemoryRuleFileStore fileStore = new InMemoryRuleFileStore();
        RuleEditorService service = new RuleEditorService(fileStore);

        Path importFile = tempDir.resolve("manual.rule");
        Files.writeString(importFile, """
                # @id=manual-rule
                # @name=Manual Rule

                0 -> hit_modifier
                0 -> wound_modifier
                5 -> attacks_modifier
                0 -> damage_modifier
                0 -> ap_modifier
                0 -> hit_reroll
                0 -> wound_reroll
                "ASSAULT" -> extra_weapon_keywords
                """);

        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> service.importRule(importFile)
        );

        assertTrue(ex.getMessage().contains("valid Rule Editor visual export"));
    }

    private static final class InMemoryRuleFileStore extends EditorRuleFileStore {
        private final Map<String, EditorRuleDefinition> storedRules = new LinkedHashMap<>();

        @Override
        public List<EditorRuleDefinition> loadAll() {
            return new ArrayList<>(storedRules.values());
        }

        @Override
        public void save(EditorRuleDefinition rule) {
            storedRules.put(rule.getId(), rule.copy());
        }

        @Override
        public boolean delete(String ruleId) {
            return storedRules.remove(ruleId) != null;
        }
    }
}
