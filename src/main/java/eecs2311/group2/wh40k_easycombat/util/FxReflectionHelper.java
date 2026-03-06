package eecs2311.group2.wh40k_easycombat.util;

import java.lang.reflect.Method;

public final class FxReflectionHelper {

    private FxReflectionHelper() {
    }

    public static String s(Object o) {
        return o == null ? "" : String.valueOf(o).trim();
    }

    public static Object getAny(Object obj, String... methodNames) {
        if (obj == null) return null;

        for (String name : methodNames) {
            try {
                Method m = obj.getClass().getMethod(name);
                return m.invoke(obj);
            } catch (Exception ignored) {
            }
        }

        return null;
    }
}