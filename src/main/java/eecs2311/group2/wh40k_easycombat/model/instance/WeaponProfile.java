package eecs2311.group2.wh40k_easycombat.model.instance;

import eecs2311.group2.wh40k_easycombat.model.Datasheets_wargear;

import java.util.Locale;

public record WeaponProfile(
        String name,
        String description,
        int count,
        String range,
        String a,
        String skill,
        String s,
        String ap,
        String d,
        boolean melee
) {
    public WeaponProfile {
        name = safe(name);
        description = safe(description);
        count = Math.max(1, count);
        range = safe(range);
        a = safe(a);
        skill = safe(skill);
        s = safe(s);
        ap = safe(ap);
        d = safe(d);
    }

    public static WeaponProfile fromDatasheetWargear(Datasheets_wargear wargear) {
        if (wargear == null) {
            return null;
        }

        String type = safe(wargear.type());
        String range = safe(wargear.range());
        boolean melee = isMelee(type, range);

        return new WeaponProfile(
                safe(wargear.name()),
                safe(wargear.description()),
                1,
                melee ? "Melee" : range,
                safe(wargear.A()),
                safe(wargear.BS_WS()),
                safe(wargear.S()),
                safe(wargear.AP()),
                safe(wargear.D()),
                melee
        );
    }

    private static boolean isMelee(String type, String range) {
        String normalizedType = safe(type).toLowerCase(Locale.ROOT);
        String normalizedRange = safe(range).toLowerCase(Locale.ROOT);

        return normalizedType.contains("melee")
                || "melee".equals(normalizedRange);
    }

    private static String safe(String value) {
        return value == null ? "" : value.trim();
    }
}