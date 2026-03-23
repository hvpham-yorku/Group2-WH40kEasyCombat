package eecs2311.group2.wh40k_easycombat.service.customizable_calculation_system;

import eecs2311.group2.wh40k_easycombat.model.instance.UnitInstance;
import eecs2311.group2.wh40k_easycombat.service.game.DiceService;

public class ExecutionContext {

    private final RuleContext ruleContext;
    private DiceService diceService;

    private RuleResult result;

    private ExecutionTrace trace;

    private UnitInstance attacker, defender;

    public ExecutionContext(RuleContext ruleContext){
        this.ruleContext = ruleContext;
    }

    public ExecutionTrace getTrace() {
        return trace;
    }

    public void setTrace(ExecutionTrace trace) {
        this.trace = trace;
    }

    public DiceService getDiceService() {
        return diceService;
    }

    public RuleResult getResult() {
        return result;
    }

    public void setResult(RuleResult result) {
        this.result = result;
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

    public Object getAttacker() {
        return attacker;
    }

    public Object getDefender() {
        return defender;
    }
}