package eecs2311.group2.wh40k_easycombat.service.customizable_calculation_system;

public class ServiceTesting {
    public static void main(String[] args) {
        RuleService service = new DefaultRuleService(new RuleVM(), new RuleCompiler());
        ExecutionContext ctx = new ExecutionContext(new RuleContext());
        RuleCompiler compiler = new RuleCompiler();
        CompiledRule rule = compiler.compile("rule1", """
                10 -> attacks
                0 -> extra_bonus
                
                while (attacks > 0)
                    roll 1 -> r
                    if (r == 6)
                        extra_bonus += 1
                    endif
                    attacks -= 1
                endwhile
                
                roll extra_bonus -> bonus_hits
                """);
        service.loadCompiledRule(rule);

        var result = service.run("rule1", ctx, true);
        if (result.isSuccess()) {
            result.getLogs().forEach(System.out::println);
        } else {
            System.out.println("Err: " + result.getError());
        }
    }
}
