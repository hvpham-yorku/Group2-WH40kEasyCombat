package eecs2311.group2.wh40k_easycombat.service.customizable_calculation_system.expr;

public class PropertyResolver {

    public static Object get(Object obj, String field) {
        if (obj == null) return null;
        try {
            // 优先尝试 getField() -> 如 getStrength()
            String getterName = "get" + field.substring(0, 1).toUpperCase() + field.substring(1);
            try {
                return obj.getClass().getMethod(getterName).invoke(obj);
            } catch (NoSuchMethodException e) {
                // 如果没有 getter，再尝试直接访问字段
                var f = obj.getClass().getDeclaredField(field);
                f.setAccessible(true);
                return f.get(obj);
            }
        } catch (Exception e) {
            throw new RuntimeException("Could not resolve property '" + field + "' on " + obj.getClass().getSimpleName(), e);
        }
    }
}

