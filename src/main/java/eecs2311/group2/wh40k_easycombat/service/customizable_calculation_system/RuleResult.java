package eecs2311.group2.wh40k_easycombat.service.customizable_calculation_system;

import java.util.ArrayList;
import java.util.List;

public class RuleResult {

    private final boolean success;
    private final Object value;
    private final String error;

    private final List<String> logs;

    private RuleResult(boolean success, Object value, String error, List<String> logs) {
        this.success = success;
        this.value = value;
        this.error = error;
        this.logs = logs;
    }

    // ===== factory methods =====

    public static RuleResult success() {
        return new RuleResult(true, null, null, new ArrayList<>());
    }

    public static RuleResult success(Object value) {
        return new RuleResult(true, value, null, new ArrayList<>());
    }

    public static RuleResult failure(String error) {
        return new RuleResult(false, null, error, new ArrayList<>());
    }

    // ===== getters =====

    public boolean isSuccess() {
        return success;
    }

    public Object getValue() {
        return value;
    }

    public String getError() {
        return error;
    }

    public List<String> getLogs() {
        return logs;
    }

    // ===== logging =====

    public void addLog(String log) {
        logs.add(log);
    }
}