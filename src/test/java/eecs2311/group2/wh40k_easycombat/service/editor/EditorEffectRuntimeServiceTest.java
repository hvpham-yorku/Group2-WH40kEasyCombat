package eecs2311.group2.wh40k_easycombat.service.editor;

import eecs2311.group2.wh40k_easycombat.model.editor.EditorActiveEffect;
import eecs2311.group2.wh40k_easycombat.model.editor.EditorRuleDefinition;
import eecs2311.group2.wh40k_easycombat.model.editor.EditorRuleDuration;
import eecs2311.group2.wh40k_easycombat.model.editor.EditorRuleTargetRole;
import eecs2311.group2.wh40k_easycombat.model.instance.Phase;
import eecs2311.group2.wh40k_easycombat.model.instance.Player;
import eecs2311.group2.wh40k_easycombat.model.instance.StratagemInstance;
import eecs2311.group2.wh40k_easycombat.model.instance.UnitInstance;
import eecs2311.group2.wh40k_easycombat.model.instance.UnitModelInstance;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class EditorEffectRuntimeServiceTest {

    private final RuleEditorService ruleEditorService = RuleEditorService.getInstance();
    private final EditorEffectRuntimeService runtimeService = EditorEffectRuntimeService.getInstance();
    private final List<String> createdRuleIds = new ArrayList<>();
    private boolean originalAutoApplyEnabled;

    @BeforeEach
    void setUp() {
        originalAutoApplyEnabled = ruleEditorService.isAutoApplyEnabled();
        ruleEditorService.setAutoApplyEnabled(true);
        runtimeService.clearAll();
    }

    @AfterEach
    void tearDown() {
        runtimeService.clearAll();
        for (String ruleId : createdRuleIds) {
            ruleEditorService.deleteRule(ruleId);
        }
        createdRuleIds.clear();
        ruleEditorService.setAutoApplyEnabled(originalAutoApplyEnabled);
    }

    @Test
    @DisplayName("matchingStratagemRules finds enabled rules triggered by a stratagem name")
    void matchingStratagemRulesFindsTriggeredRules() {
        String marker = "JUnit-" + UUID.randomUUID();
        saveRule(marker + "-Triggered", "damage_modifier + 1 -> damage_modifier", rule -> {
            rule.setTriggeringStratagemNameContains(marker + " volley");
        });

        List<EditorRuleDefinition> matches = runtimeService.matchingStratagemRules(
                new StratagemInstance(marker + " Volley", "1", "Any", "Shooting", "")
        );

        assertEquals(1, matches.size());
        assertTrue(matches.getFirst().getName().contains(marker));
    }

    @Test
    @DisplayName("activateStratagemRules creates active effects for the selected unit")
    void activateStratagemRulesCreatesActiveEffects() {
        String marker = "JUnit-" + UUID.randomUUID();
        saveRule(marker + "-Buff", "ap_modifier - 1 -> ap_modifier", rule -> {
            rule.setTriggeringStratagemNameContains(marker + " barrage");
            rule.setDuration(EditorRuleDuration.UNTIL_END_OF_TURN);
            rule.setTargetRole(EditorRuleTargetRole.ATTACKER);
        });

        UnitInstance targetUnit = unit(marker + " Unit");

        List<EditorActiveEffect> created = runtimeService.activateStratagemRules(
                new StratagemInstance(marker + " Barrage", "1", "Any", "Shooting", ""),
                targetUnit,
                Player.ATTACKER,
                Player.ATTACKER,
                Phase.SHOOTING,
                2
        );

        assertEquals(1, created.size());
        assertEquals(targetUnit.getInstanceId(), created.getFirst().targetUnitId());
        assertTrue(created.getFirst().displayName().contains(targetUnit.getUnitName()));
        assertEquals(1, runtimeService.getActiveEffects().size());
    }

    @Test
    @DisplayName("until start of your next command effects expire at the next command phase")
    void untilStartOfYourNextCommandExpiresAtNextCommand() {
        String marker = "JUnit-" + UUID.randomUUID();
        saveRule(marker + "-Command", "hit_modifier + 1 -> hit_modifier", rule -> {
            rule.setTriggeringStratagemNameContains(marker + " order");
            rule.setDuration(EditorRuleDuration.UNTIL_START_OF_YOUR_NEXT_COMMAND);
            rule.setTargetRole(EditorRuleTargetRole.ATTACKER);
        });

        UnitInstance targetUnit = unit(marker + " Unit");
        runtimeService.activateStratagemRules(
                new StratagemInstance(marker + " Order", "1", "Any", "Shooting", ""),
                targetUnit,
                Player.ATTACKER,
                Player.ATTACKER,
                Phase.SHOOTING,
                1
        );

        runtimeService.clearExpiredEffects(1, Phase.COMMAND, Player.ATTACKER);
        assertEquals(1, runtimeService.getActiveEffects().size());

        runtimeService.clearExpiredEffects(2, Phase.COMMAND, Player.ATTACKER);
        assertTrue(runtimeService.getActiveEffects().isEmpty());
    }

    private void saveRule(
            String name,
            String script,
            java.util.function.Consumer<EditorRuleDefinition> mutator
    ) {
        EditorRuleDefinition rule = new EditorRuleDefinition();
        rule.setName(name);
        rule.setDslScript(script);
        mutator.accept(rule);
        EditorRuleDefinition saved = ruleEditorService.saveRule(rule);
        createdRuleIds.add(saved.getId());
    }

    private static UnitInstance unit(String name) {
        UnitInstance unit = new UnitInstance("ds-" + name.replace(" ", "-"), name);
        unit.addModel(new UnitModelInstance(name + " Model", "6\"", "4", "3+", "2", "6+", "1", ""));
        return unit;
    }
}
