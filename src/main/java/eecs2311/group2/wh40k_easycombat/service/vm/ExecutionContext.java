package eecs2311.group2.wh40k_easycombat.service.vm;

public class ExecutionContext {

    private final RuleContext ruleContext;

    public ExecutionContext(RuleContext ruleContext){
        this.ruleContext = ruleContext;
    }

    public Object getValue(String name) {
        return ruleContext.get(name);
    }

    public void setValue(String name, Object value) {
        ruleContext.set(name, value);
    }

    public RuleContext getRuleContext() {
        return ruleContext;
    }
}