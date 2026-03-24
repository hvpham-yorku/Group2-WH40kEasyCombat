package eecs2311.group2.wh40k_easycombat.service.customizable_calculation_system.expr;

import eecs2311.group2.wh40k_easycombat.service.customizable_calculation_system.ExecutionContext;

import java.lang.reflect.Field;
import java.util.Map;
import java.util.Optional;

public class PropertyExpr implements Expression {
    private final String objectName;
    private final String propertyName;

    public PropertyExpr(String objectName, String propertyName) {
        this.objectName = objectName;
        this.propertyName = propertyName;
    }

    @Override
    public Optional<Object> eval(ExecutionContext ctx) {
        Object obj = ctx.getRuleContext().get(objectName);
        if (obj == null) return Optional.empty();

        try {
            // 方案 A: 如果 obj 是 Map
            if (obj instanceof Map) {
                return Optional.ofNullable(((Map<?, ?>) obj).get(propertyName));
            }

            // 方案 B: 如果是标准的 Java Bean (反射获取字段)
            Field field = obj.getClass().getDeclaredField(propertyName);
            field.setAccessible(true);
            return Optional.ofNullable(field.get(obj));
        } catch (Exception e) {
            throw new RuntimeException("Runtime Error: Cannot resolve the property");
        }
    }
}