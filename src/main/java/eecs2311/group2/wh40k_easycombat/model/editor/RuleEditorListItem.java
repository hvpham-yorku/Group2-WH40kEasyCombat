package eecs2311.group2.wh40k_easycombat.model.editor;

public class RuleEditorListItem {
    private final EditorRuleDefinition savedRule;
    private final String builtInName;
    private final String builtInScript;
    private final String sourceLabel;

    private RuleEditorListItem(
            EditorRuleDefinition savedRule,
            String builtInName,
            String builtInScript,
            String sourceLabel
    ) {
        this.savedRule = savedRule == null ? null : savedRule.copy();
        this.builtInName = builtInName == null ? "" : builtInName.trim();
        this.builtInScript = builtInScript == null ? "" : builtInScript.replace("\r\n", "\n").replace('\r', '\n');
        this.sourceLabel = sourceLabel == null ? "" : sourceLabel.trim();
    }

    public static RuleEditorListItem fromSaved(EditorRuleDefinition rule) {
        return new RuleEditorListItem(rule, "", "", "Custom Rule");
    }

    public static RuleEditorListItem fromBuiltIn(String name, String script, String sourceLabel) {
        return new RuleEditorListItem(null, name, script, sourceLabel);
    }

    public boolean isEditable() {
        return savedRule != null;
    }

    public EditorRuleDefinition getSavedRule() {
        return savedRule == null ? null : savedRule.copy();
    }

    public String getScript() {
        return isEditable() ? savedRule.getDslScript() : builtInScript;
    }

    public String getDisplayTitle() {
        return isEditable() ? savedRule.displayName() : "[Built-in VM] " + builtInName;
    }

    public String getDisplaySubtitle() {
        return isEditable()
                ? savedRule.getType() + " | " + savedRule.getPhase() + " | " + savedRule.getAttackType()
                : sourceLabel;
    }

    public String getDisplayName() {
        return isEditable() ? savedRule.getName() : builtInName;
    }
}
