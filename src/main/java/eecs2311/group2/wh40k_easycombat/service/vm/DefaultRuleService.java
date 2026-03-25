package eecs2311.group2.wh40k_easycombat.service.vm;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
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
        if (!rules.containsKey(ruleName)) {
            return RuleResult.failure("Execution Error: The rule [" + ruleName + "] was not found. Please check if it was loaded correctly.");
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
            return RuleResult.failure("Unexpected System Error during [" + ruleName + "]: " + e.getMessage());
        }
    }

    @Override
    public void loadRuleFolder(Path folder) {
        if (!Files.isDirectory(folder)) {
            throw new RuntimeException("Path is not a directory: " + folder);
        }
        try (Stream<Path> paths = Files.walk(folder)) {
            paths.filter(Files::isRegularFile)
                    .filter(p -> p.toString().endsWith(".rule"))
                    .forEach(this::loadSingleRuleFile);
        } catch (IOException e) {
            throw new RuntimeException("Failed to scan rule folder: " + e.getMessage(), e);
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

    @Override
    public Set<String> getLoadedRules() {
        return Collections.unmodifiableSet(rules.keySet());
    }

    @Override
    public void removeRule(String ruleName) {
        rules.remove(ruleName);
    }

    private void loadSingleRuleFile(Path file) {
        try {
            String source = Files.readString(file);
            String ruleName = file.getFileName().toString();

            CompiledRule rule = compiler.compile(ruleName, source);
            loadRule(rule);
        } catch (DSLException e) {
            throw new RuntimeException("Compile error in file [" + file + "]: " + e.getMessage(), e);
        } catch (IOException e) {
            throw new RuntimeException("IO error loading file [" + file + "]: " + e.getMessage(), e);
        }
    }

    private void loadRule(CompiledRule rule) {
        rules.put(rule.getName(), rule);
    }
}