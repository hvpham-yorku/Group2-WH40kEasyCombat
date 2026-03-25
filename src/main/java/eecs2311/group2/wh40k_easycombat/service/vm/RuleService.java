package eecs2311.group2.wh40k_easycombat.service.vm;

import java.nio.file.Path;

public interface RuleService {

    RuleResult run(String ruleName, ExecutionContext ctx);

    void loadRuleFolder(Path folder);
    void loadRuleFile(Path file);

    void loadCompiledRule(CompiledRule rule);
}