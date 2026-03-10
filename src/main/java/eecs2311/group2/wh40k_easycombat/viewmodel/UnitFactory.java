package eecs2311.group2.wh40k_easycombat.viewmodel;

import eecs2311.group2.wh40k_easycombat.aggregate.DatasheetAggregate;
import eecs2311.group2.wh40k_easycombat.model.Datasheets;
import eecs2311.group2.wh40k_easycombat.model.Datasheets_enhancements;
import eecs2311.group2.wh40k_easycombat.model.Datasheets_keywords;
import eecs2311.group2.wh40k_easycombat.model.Datasheets_wargear;
import eecs2311.group2.wh40k_easycombat.util.CostParser;
import eecs2311.group2.wh40k_easycombat.util.CostTier;
import eecs2311.group2.wh40k_easycombat.util.StatFormatter;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public final class UnitFactory {

    private UnitFactory() {
    }

    public static ArmyUnitVM create(
            DatasheetAggregate bundle,
            Map<String, ArmyUnitVM.EnhancementEntry> enhancementInfoById
    ) {
        if (bundle == null || bundle.datasheet == null) {
            throw new IllegalArgumentException("Datasheet bundle is null.");
        }

        Datasheets d = bundle.datasheet;

        String datasheetId = d.id();
        String unitName = safeText(d.name(), d.id());
        String statLine = StatFormatter.buildStatLine(bundle.models);
        String role = safeText(d.role(), "Other");

        boolean character = isCharacterRole(role) || hasCharacterKeyword(bundle.keywords);

        List<CostTier> tiers = CostParser.parseTiers(bundle.costs);

        ArmyUnitVM vm = new ArmyUnitVM(
                datasheetId,
                unitName,
                statLine,
                role,
                character,
                tiers
        );

        for (Datasheets_wargear w : bundle.wargear) {
            String wargearName = (w.name() != null && !w.name().isBlank())
                    ? w.name()
                    : (w.description() != null && !w.description().isBlank() ? w.description() : "Wargear");

            vm.addWargear(w.auto_id(), wargearName);
        }

        for (Datasheets_enhancements e : bundle.enhancements) {
            String enhancementId = e.enhancement_id();

            ArmyUnitVM.EnhancementEntry info =
                    enhancementInfoById == null ? null : enhancementInfoById.get(enhancementId);

            if (info != null) {
                vm.addEnhancement(
                        info.getId(),
                        info.getName(),
                        info.getCost(),
                        info.getDetachmentId(),
                        info.getFactionId()
                );
            } else {
                vm.addEnhancement(enhancementId, enhancementId, 0);
            }
        }

        return vm;
    }

    private static boolean isCharacterRole(String role) {
        return role != null && role.toLowerCase(Locale.ROOT).contains("character");
    }

    private static boolean hasCharacterKeyword(List<Datasheets_keywords> keywords) {
        if (keywords == null) return false;

        for (Datasheets_keywords k : keywords) {
            String keyword = callString(k, "keyword", "name");
            if (keyword != null && keyword.trim().equalsIgnoreCase("character")) {
                return true;
            }
        }
        return false;
    }

    private static String callString(Object obj, String... methodNames) {
        if (obj == null) return null;

        for (String name : methodNames) {
            try {
                Method m = obj.getClass().getMethod(name);
                Object value = m.invoke(obj);
                if (value != null) {
                    return String.valueOf(value);
                }
            } catch (Exception ignored) {
            }
        }
        return null;
    }

    private static String safeText(String primary, String fallback) {
        if (primary != null && !primary.isBlank()) return primary;
        return fallback == null ? "" : fallback;
    }
}