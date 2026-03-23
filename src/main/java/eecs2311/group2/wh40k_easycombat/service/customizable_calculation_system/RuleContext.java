package eecs2311.group2.wh40k_easycombat.service.customizable_calculation_system;

import java.util.HashMap;
import java.util.Map;

public class RuleContext {
    // 统一存储 Integer, Boolean, 和 DicePool
    private final Map<String, Object> vars = new HashMap<>();

    public void set(String name, Object value) {
        vars.put(name, value);
    }

    public Object get(String name) {
        // 如果变量不存在，默认返回 0 (为了兼容 hits = hits + 1)
        return vars.getOrDefault(name, 0);
    }
}