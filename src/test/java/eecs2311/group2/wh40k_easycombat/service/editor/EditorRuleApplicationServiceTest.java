package eecs2311.group2.wh40k_easycombat.service.editor;

import eecs2311.group2.wh40k_easycombat.model.editor.EditorRuleAttackType;
import eecs2311.group2.wh40k_easycombat.model.editor.EditorRuleDefinition;
import eecs2311.group2.wh40k_easycombat.model.editor.EditorRuleDuration;
import eecs2311.group2.wh40k_easycombat.model.editor.EditorRuleModifiers;
import eecs2311.group2.wh40k_easycombat.model.editor.EditorRulePhase;
import eecs2311.group2.wh40k_easycombat.model.editor.EditorRuleTargetRole;
import eecs2311.group2.wh40k_easycombat.model.instance.Phase;
import eecs2311.group2.wh40k_easycombat.model.instance.Player;
import eecs2311.group2.wh40k_easycombat.model.instance.UnitAbilityProfile;
import eecs2311.group2.wh40k_easycombat.model.instance.UnitInstance;
import eecs2311.group2.wh40k_easycombat.model.instance.UnitModelInstance;
import eecs2311.group2.wh40k_easycombat.model.instance.WeaponProfile;
import eecs2311.group2.wh40k_easycombat.service.autobattle.AttackKeywordContext;
import eecs2311.group2.wh40k_easycombat.service.autobattle.AutoBattleMode;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class EditorRuleApplicationServiceTest {

    private final RuleEditorService ruleEditorService = RuleEditorService.getInstance();
    private final EditorEffectRuntimeService runtimeService = EditorEffectRuntimeService.getInstance();
    private final EditorRuleApplicationService applicationService = new EditorRuleApplicationService();
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
    @DisplayName("resolveForAttack applies VM modifiers for a matching static rule")
    void resolveForAttackAppliesVmModifiersForMatchingStaticRule() {
        String marker = "JUnit-" + UUID.randomUUID();
        EditorRuleDefinition rule = saveRule(marker + "-HitAndAttacks", """
                hit_modifier + 1 -> hit_modifier
                attacks_modifier + 2 -> attacks_modifier
                """, saved -> {
            saved.setPhase(EditorRulePhase.SHOOTING);
            saved.setAttackType(EditorRuleAttackType.RANGED);
            saved.setAttackerUnitNameContains(marker + " Attacker");
            saved.setWeaponNameContains(marker + " Rifle");
        });

        UnitInstance attacker = unit(marker + " Attacker");
        attacker.addAbility(new UnitAbilityProfile("Disciplined Fire", "", ""));
        UnitInstance defender = unit(marker + " Defender");
        WeaponProfile weapon = rangedWeapon(marker + " Rifle");

        EditorRuleModifiers modifiers = applicationService.resolveForAttack(
                AutoBattleMode.SHOOTING,
                attacker,
                defender,
                weapon,
                AttackKeywordContext.none()
        );

        assertEquals(1, modifiers.hitModifier());
        assertEquals(2, modifiers.attacksModifier());
        assertTrue(modifiers.appliedRuleNames().contains(rule.getName()));
    }

    @Test
    @DisplayName("optional rules are listed for selection but not auto-applied")
    void optionalRulesAreListedButNotAutoApplied() {
        String marker = "JUnit-" + UUID.randomUUID();
        EditorRuleDefinition rule = saveRule(marker + "-Optional", """
                damage_modifier + 3 -> damage_modifier
                """, saved -> {
            saved.setPhase(EditorRulePhase.SHOOTING);
            saved.setAttackType(EditorRuleAttackType.RANGED);
            saved.setOptionalActivation(true);
            saved.setAttackerUnitNameContains(marker + " Attacker");
            saved.setWeaponNameContains(marker + " Rifle");
        });

        UnitInstance attacker = unit(marker + " Attacker");
        UnitInstance defender = unit(marker + " Defender");
        WeaponProfile weapon = rangedWeapon(marker + " Rifle");

        EditorRuleModifiers modifiers = applicationService.resolveForAttack(
                AutoBattleMode.SHOOTING,
                attacker,
                defender,
                weapon,
                AttackKeywordContext.none()
        );

        assertEquals(0, modifiers.damageModifier());
        assertTrue(applicationService.matchingOptionalRules(
                AutoBattleMode.SHOOTING,
                attacker,
                defender,
                weapon
        ).stream().anyMatch(match -> match.getId().equals(rule.getId())));
    }

    @Test
    @DisplayName("active optional rules apply until the end of the phase and then expire")
    void activeOptionalRulesApplyUntilEndOfPhaseThenExpire() {
        String marker = "JUnit-" + UUID.randomUUID();
        EditorRuleDefinition rule = saveRule(marker + "-PhaseBuff", """
                damage_modifier + 1 -> damage_modifier
                """, saved -> {
            saved.setPhase(EditorRulePhase.SHOOTING);
            saved.setAttackType(EditorRuleAttackType.RANGED);
            saved.setOptionalActivation(true);
            saved.setDuration(EditorRuleDuration.UNTIL_END_OF_PHASE);
            saved.setTargetRole(EditorRuleTargetRole.ATTACKER);
            saved.setAttackerUnitNameContains(marker + " Attacker");
            saved.setWeaponNameContains(marker + " Rifle");
        });

        UnitInstance attacker = unit(marker + " Attacker");
        UnitInstance defender = unit(marker + " Defender");
        WeaponProfile weapon = rangedWeapon(marker + " Rifle");

        runtimeService.activateOptionalRulesForAttack(
                List.of(rule),
                attacker,
                defender,
                Player.ATTACKER,
                Player.ATTACKER,
                Phase.SHOOTING,
                1
        );

        EditorRuleModifiers activeModifiers = applicationService.resolveForAttack(
                AutoBattleMode.SHOOTING,
                attacker,
                defender,
                weapon,
                AttackKeywordContext.none()
        );
        assertEquals(1, activeModifiers.damageModifier());

        runtimeService.clearExpiredEffects(1, Phase.COMMAND, Player.ATTACKER);

        EditorRuleModifiers expiredModifiers = applicationService.resolveForAttack(
                AutoBattleMode.SHOOTING,
                attacker,
                defender,
                weapon,
                AttackKeywordContext.none()
        );
        assertEquals(0, expiredModifiers.damageModifier());
    }

    private EditorRuleDefinition saveRule(
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
        return saved;
    }

    private static UnitInstance unit(String name) {
        UnitInstance unit = new UnitInstance("ds-" + name.replace(" ", "-"), name);
        unit.addModel(new UnitModelInstance(name + " Model", "6\"", "4", "3+", "2", "6+", "1", ""));
        return unit;
    }

    private static WeaponProfile rangedWeapon(String name) {
        return new WeaponProfile(50, name, "", 1, "24\"", "2", "3+", "4", "-1", "1", false);
    }
}
