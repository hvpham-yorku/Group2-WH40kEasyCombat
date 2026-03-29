package eecs2311.group2.wh40k_easycombat.service.vm.expr;

import eecs2311.group2.wh40k_easycombat.service.vm.ExecutionContext;

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
        if (obj == null) {
            throw new RuntimeException("Object '" + objectName + "' is undefined in context");
        }

        try {
            if (obj instanceof Map<?, ?> map) {
                if (!map.containsKey(propertyName)) {
                    throw new RuntimeException("Property '" + propertyName + "' not found in Map '" + objectName + "'");
                }
                return Optional.ofNullable(map.get(propertyName));
            }

            try {
                Field field = obj.getClass().getDeclaredField(propertyName);
                field.setAccessible(true);
                return Optional.ofNullable(field.get(obj));
            } catch (NoSuchFieldException e) {
                throw new RuntimeException("Field '" + propertyName + "' not found in class " + obj.getClass().getSimpleName());
            }

        } catch (RuntimeException re) {
            throw re;
        } catch (Exception e) {
            throw new RuntimeException("Failed to access " + objectName + "." + propertyName + ": " + e.getMessage());
        }
    }
}