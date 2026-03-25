package eecs2311.group2.wh40k_easycombat.service.vm;

import java.nio.file.Path;

public class VMService {
    private static final RuleCompiler compiler = new RuleCompiler();
    private static final RuleService service = new DefaultRuleService(new RuleVM(), compiler);;

    private VMService(){}

    public static void loadFolder(Path folder){
        service.loadRuleFolder(folder);
    }

    public static void loadFile(Path file){
        service.loadRuleFile(file);
    }

    public static void loadRule(String ruleName, String script){
        CompiledRule rule = compiler.compile(ruleName, script);
        service.loadCompiledRule(rule);
    }

    public static RuleResult run(String ruleName, RuleContext ctx){
        return service.run(ruleName, new ExecutionContext(ctx));
    }
}
