package eecs2311.group2.wh40k_easycombat.service.editor;

import eecs2311.group2.wh40k_easycombat.model.editor.EditorRuleDefinition;
import eecs2311.group2.wh40k_easycombat.service.vm.RuleCompiler;
import eecs2311.group2.wh40k_easycombat.service.vm.VMService;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class RuleEditorService {
    private static final String VM_RULE_PREFIX = "editor::";
    private static final RuleEditorService INSTANCE = new RuleEditorService(new EditorRuleFileStore());

    private final RuleCompiler compiler = new RuleCompiler();
    private final EditorRuleFileStore fileStore;
    private final List<EditorRuleDefinition> rules;
    private boolean autoApplyEnabled = true;

    public static RuleEditorService getInstance() {
        return INSTANCE;
    }

    public RuleEditorService(EditorRuleFileStore fileStore) {
        this.fileStore = fileStore;
        this.rules = new ArrayList<>();
        reload();
    }

    public synchronized List<EditorRuleDefinition> getRules() {
        return rules.stream()
                .map(EditorRuleDefinition::copy)
                .sorted(Comparator.comparing(rule -> safe(rule.getName())))
                .toList();
    }

    public synchronized void reload() {
        rules.clear();
        List<EditorRuleDefinition> loaded = fileStore.loadAll();
        if (loaded != null) {
            for (EditorRuleDefinition rule : loaded) {
                if (rule != null) {
                    EditorRuleDefinition validated = rule.copy();
                    validateRule(validated);
                    rules.add(validated);
                }
            }
        }
        syncVmRules();
    }

    public synchronized EditorRuleDefinition saveRule(EditorRuleDefinition incoming) {
        if (incoming == null) {
            throw new IllegalArgumentException("rule must not be null");
        }

        EditorRuleDefinition saved = incoming.copy();
        validateRule(saved);

        boolean replaced = false;
        for (int i = 0; i < rules.size(); i++) {
            if (rules.get(i).getId().equals(saved.getId())) {
                rules.set(i, saved);
                replaced = true;
                break;
            }
        }

        if (!replaced) {
            rules.add(saved);
        }

        fileStore.save(saved);
        loadIntoVm(saved);
        return saved.copy();
    }

    public synchronized boolean deleteRule(String ruleId) {
        if (ruleId == null || ruleId.isBlank()) {
            return false;
        }

        EditorRuleDefinition removedRule = null;
        for (EditorRuleDefinition rule : rules) {
            if (ruleId.equals(rule.getId())) {
                removedRule = rule;
                break;
            }
        }

        boolean removed = rules.removeIf(rule -> ruleId.equals(rule.getId()));
        if (removed) {
            fileStore.delete(ruleId);
            if (removedRule != null) {
                VMService.removeLoadedRule(removedRule.vmRuleName());
            }
        }
        return removed;
    }

    public synchronized boolean isAutoApplyEnabled() {
        return autoApplyEnabled;
    }

    public synchronized void setAutoApplyEnabled(boolean autoApplyEnabled) {
        this.autoApplyEnabled = autoApplyEnabled;
    }

    private void validateRule(EditorRuleDefinition rule) {
        String name = safe(rule == null ? "" : rule.getName());
        if (name.isBlank()) {
            throw new IllegalArgumentException("Rule name must not be blank.");
        }

        String script = rule == null ? "" : rule.getDslScript();
        if (safe(script).isBlank()) {
            throw new IllegalArgumentException("VM script must not be blank.");
        }

        try {
            compiler.compile(rule.vmRuleName(), script);
        } catch (RuntimeException ex) {
            throw new IllegalArgumentException("VM script is invalid: " + ex.getMessage(), ex);
        }
    }

    private void syncVmRules() {
        Set<String> loadedNames = new HashSet<>(VMService.getLoadedRules());
        for (String loadedName : loadedNames) {
            if (loadedName != null && loadedName.startsWith(VM_RULE_PREFIX)) {
                VMService.removeLoadedRule(loadedName);
            }
        }

        for (EditorRuleDefinition rule : rules) {
            loadIntoVm(rule);
        }
    }

    private void loadIntoVm(EditorRuleDefinition rule) {
        VMService.loadRule(rule.vmRuleName(), rule.getDslScript());
    }

    private static String safe(String value) {
        return value == null ? "" : value.trim().toLowerCase();
    }
}
