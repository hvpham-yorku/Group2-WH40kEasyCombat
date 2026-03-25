package eecs2311.group2.wh40k_easycombat.service.vm;

public class RuleResult {

    private final boolean success;
    private final String error;
    private final ExecutionContext context;

    private RuleResult(boolean success, String error, ExecutionContext context) {
        this.success = success;
        this.error = error;
        this.context = context;
    }

    public static RuleResult success(ExecutionContext context) {
        return new RuleResult(true, null, context);
    }

    public static RuleResult failure(String error) {
        return new RuleResult(false, error, null);
    }


    public Object getValue(String name) {
        return (context != null) ? context.getRuleContext().get(name) : null;
    }

    public boolean isSuccess() {
        return success;
    }

    public String getError() {
        return error;
    }
}