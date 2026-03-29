package eecs2311.group2.wh40k_easycombat.service.vm;

import java.util.HashMap;
import java.util.Map;

public class RuleContext {
    private final Map<String, Object> vars = new HashMap<>();

    public void set(String name, Object value) {
        vars.put(name, value);
    }

    public Object get(String name) {
        return vars.get(name);
    }
}