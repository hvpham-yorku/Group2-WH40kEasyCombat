package eecs2311.group2.wh40k_easycombat.service.vm;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;

public class DefaultRuleService implements RuleService {

    private final Map<String, CompiledRule> rules = new HashMap<>();

    private final RuleVM vm;

    private final RuleCompiler compiler;

    public DefaultRuleService(RuleVM vm, RuleCompiler compiler) {
        this.vm = vm;
        this.compiler = compiler;
    }

    @Override
    public RuleResult run(String ruleName, ExecutionContext ctx) {
        if(!rules.containsKey(ruleName)){
            return RuleResult.failure("The rule [" + ruleName + "] doesn't exist.");
        }

        CompiledRule rule = rules.get(ruleName);

        try {
            vm.execute(rule, ctx);
            return RuleResult.success(ctx);
        } catch (DSLException e) {
            // Catch our custom domain-specific exception
            return RuleResult.failure(e.getMessage());
        } catch (Exception e) {
            // Catch any other unexpected system exceptions
            return RuleResult.failure("Unexpected System Error: " + e.getMessage());
        }
    }

    @Override
    public void loadRuleFolder(Path folder) {

        try (Stream<Path> paths = Files.walk(folder)) {

            paths.filter(p -> p.toString().endsWith(".rule")).forEach(this::loadSingleRuleFile);

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void loadRuleFile(Path file) {
        loadSingleRuleFile(file);
    }

    @Override
    public void loadCompiledRule(CompiledRule rule) {
        loadRule(rule);
    }

    private void loadSingleRuleFile(Path file) {

        try {
            String source = Files.readString(file);

            CompiledRule rule = compiler.compile(file.toFile().getName(), source);

            loadRule(rule);

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void loadRule(CompiledRule rule) {
        rules.put(rule.getName(), rule);
    }
}