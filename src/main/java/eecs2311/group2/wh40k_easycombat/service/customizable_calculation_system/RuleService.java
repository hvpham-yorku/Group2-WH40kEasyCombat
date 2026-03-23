package eecs2311.group2.wh40k_easycombat.service.customizable_calculation_system;

import java.nio.file.Path;

public interface RuleService {

    RuleResult run(String ruleName, ExecutionContext ctx, boolean traceEnabled);

    void fire(EventType event, ExecutionContext ctx);

    void loadRuleFolder(Path folder);
    void loadRuleFile(Path file);

    void loadCompiledRule(CompiledRule rule);
}