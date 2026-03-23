package eecs2311.group2.wh40k_easycombat.service.customizable_calculation_system;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

public class DefaultRuleService implements RuleService {

    private final Map<String, CompiledRule> rules = new HashMap<>();

    private final Map<EventType, List<CompiledRule>> eventRules = new HashMap<>();

    private final RuleVM vm;

    private final RuleCompiler compiler;

    public DefaultRuleService(RuleVM vm, RuleCompiler compiler) {
        this.vm = vm;
        this.compiler = compiler;
    }

    @Override
    public RuleResult run(String ruleName, ExecutionContext ctx, boolean traceEnabled) {
        RuleResult result = RuleResult.success();
        ctx.setResult(result);

        if (traceEnabled) {
            ctx.setTrace(new ExecutionTrace());
        }

        CompiledRule rule = rules.get(ruleName);

        try {
            vm.execute(rule, ctx);
            return result;
        } catch (Exception e) {
            return RuleResult.failure(e.getMessage());
        }
    }

    @Override
    public void fire(EventType event, ExecutionContext ctx) {

        List<CompiledRule> list = eventRules.get(event);

        if (list == null) return;

        for (CompiledRule rule : list) {
            vm.execute(rule, ctx);
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

        if (rule.getEvent() != null) {
            eventRules
                    .computeIfAbsent(rule.getEvent(), k -> new ArrayList<>())
                    .add(rule);
        }
    }
}