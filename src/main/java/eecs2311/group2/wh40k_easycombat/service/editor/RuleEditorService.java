package eecs2311.group2.wh40k_easycombat.service.editor;

import eecs2311.group2.wh40k_easycombat.model.editor.EditorRuleDefinition;
import eecs2311.group2.wh40k_easycombat.repository.EditorRuleRepository;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class RuleEditorService {
    private static final RuleEditorService INSTANCE = new RuleEditorService(new EditorRuleRepository());

    private final EditorRuleRepository repository;
    private final List<EditorRuleDefinition> rules;
    private boolean autoApplyEnabled = true;

    public static RuleEditorService getInstance() {
        return INSTANCE;
    }

    public RuleEditorService(EditorRuleRepository repository) {
        this.repository = repository;
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
        List<EditorRuleDefinition> loaded = repository.loadAll();
        if (loaded != null) {
            for (EditorRuleDefinition rule : loaded) {
                if (rule != null) {
                    rules.add(rule.copy());
                }
            }
        }
    }

    public synchronized EditorRuleDefinition saveRule(EditorRuleDefinition incoming) {
        if (incoming == null) {
            throw new IllegalArgumentException("rule must not be null");
        }

        EditorRuleDefinition saved = incoming.copy();
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

        persist();
        return saved.copy();
    }

    public synchronized boolean deleteRule(String ruleId) {
        if (ruleId == null || ruleId.isBlank()) {
            return false;
        }

        boolean removed = rules.removeIf(rule -> ruleId.equals(rule.getId()));
        if (removed) {
            persist();
        }
        return removed;
    }

    public synchronized boolean isAutoApplyEnabled() {
        return autoApplyEnabled;
    }

    public synchronized void setAutoApplyEnabled(boolean autoApplyEnabled) {
        this.autoApplyEnabled = autoApplyEnabled;
    }

    private void persist() {
        repository.saveAll(rules.stream().map(EditorRuleDefinition::copy).toList());
    }

    private static String safe(String value) {
        return value == null ? "" : value.trim().toLowerCase();
    }
}
