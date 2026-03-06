package eecs2311.group2.wh40k_easycombat.service;

import eecs2311.group2.wh40k_easycombat.service.StaticDataService.DatasheetBundle;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import static eecs2311.group2.wh40k_easycombat.util.FxReflectionHelper.getAny;
import static eecs2311.group2.wh40k_easycombat.util.FxReflectionHelper.s;

public final class DatasheetsTextFormatter {

    private DatasheetsTextFormatter() {
    }

    public static String joinKeywordsComma(List<?> keywords) {
        if (keywords == null || keywords.isEmpty()) return "";

        List<String> ks = new ArrayList<>();
        for (Object k : keywords) {
            String kw = s(getAny(k, "keyword", "name", "text"));
            if (!kw.isBlank()) ks.add(kw);
        }
        return String.join(", ", ks);
    }

    public static String formatNonFactionAbilities(List<?> list, DatasheetsPageState state) {
        if (list == null || list.isEmpty()) return "";

        List<String> coreNames = new ArrayList<>();
        List<String> otherAbilities = new ArrayList<>();

        Set<String> seenCore = new LinkedHashSet<>();
        Set<String> seenOther = new LinkedHashSet<>();

        for (Object a : list) {
            String type = s(getAny(a, "type")).trim().toLowerCase();

            if (type.contains("faction")) continue;

            if (type.contains("core")) {
                String name = resolveAbilityName(a, state);
                if (!name.isBlank() && seenCore.add(name)) {
                    coreNames.add(name);
                }
                continue;
            }

            String line = resolveAbilityFullTextForDisplay(a, state);
            if (!line.isBlank() && seenOther.add(line)) {
                otherAbilities.add(line);
            }
        }

        List<String> blocks = new ArrayList<>();

        if (!coreNames.isEmpty()) {
            blocks.add("<b>Core:</b> " + String.join(", ", coreNames));
        }

        blocks.addAll(otherAbilities);

        return String.join("\n", blocks);
    }

    public static String formatFactionAbilityNames(List<?> abilities, List<?> detachmentAbilities) {
        List<String> out = new ArrayList<>();
        Set<String> seen = new LinkedHashSet<>();

        if (abilities != null) {
            for (Object a : abilities) {
                String type = s(getAny(a, "type")).trim().toLowerCase();
                if (!type.contains("faction")) continue;

                String name = s(getAny(a, "name"));
                if (!name.isBlank() && seen.add(name)) {
                    out.add("<b>" + name + "</b>");
                }
            }
        }

        if (detachmentAbilities != null) {
            for (Object a : detachmentAbilities) {
                String name = s(getAny(a,
                        "name",
                        "ability",
                        "detachment_ability",
                        "title",
                        "rule"));

                if (!name.isBlank() && seen.add(name)) {
                    out.add("<b>" + name + "</b>");
                }
            }
        }

        return String.join("\n", out);
    }

    public static String buildModelCost(List<?> costs) {
        if (costs == null || costs.isEmpty()) return "";

        List<String> lines = new ArrayList<>();
        for (Object c : costs) {
            String desc = s(getAny(c, "model", "description", "text", "line_text", "name"));
            String cost = s(getAny(c, "cost", "points", "value"));

            if (desc.isBlank() && cost.isBlank()) continue;

            if (!desc.isBlank() && !cost.isBlank()) lines.add(desc + " " + cost);
            else if (!desc.isBlank()) lines.add(desc);
            else lines.add(cost);
        }
        return String.join("\n", lines);
    }

    public static String buildOtherSection(DatasheetBundle bundle) {
        List<String> sections = new ArrayList<>();

        String leadersText = joinLines(bundle.leaders, "text", "rule", "description", "line_text", "name");
        if (!leadersText.isBlank()) sections.add("ATTACHED UNIT\n" + leadersText);

        String optionsText = joinLines(bundle.options, "text", "option", "description", "line_text", "name");
        if (!optionsText.isBlank()) sections.add("OPTIONS\n" + optionsText);

        String stratsText = joinLines(bundle.stratagems, "text", "name", "description", "line_text");
        if (!stratsText.isBlank()) sections.add("STRATAGEMS\n" + stratsText);

        String enhText = joinLines(bundle.enhancements, "text", "name", "description", "line_text");
        if (!enhText.isBlank()) sections.add("ENHANCEMENTS\n" + enhText);

        return String.join("\n\n", sections).trim();
    }

    public static String joinLines(List<?> items, String... fields) {
        if (items == null || items.isEmpty()) return "";

        List<String> out = new ArrayList<>();
        for (Object x : items) {
            String name = s(getAny(x, "name", "ability", "title"));
            String text = s(getAny(x, "text", "description", "line_text"));

            if (!name.isBlank() && !text.isBlank()) {
                out.add(name + ": " + text);
                continue;
            }

            String line = "";
            for (String f : fields) {
                String v = s(getAny(x, f));
                if (!v.isBlank()) {
                    line = v;
                    break;
                }
            }

            if (!line.isBlank()) out.add(line);
        }

        return String.join("\n", out);
    }

    public static String htmlToPlainText(String html) {
        if (html == null || html.isBlank()) return "";

        String s = html;
        s = s.replaceAll("(?i)<li[^>]*>", "• ");
        s = s.replaceAll("(?i)</li>", " ");
        s = s.replaceAll("(?i)<a[^>]*>", "");
        s = s.replaceAll("(?i)</a>", "");
        s = s.replaceAll("(?is)<(?!/?b\\b)[^>]+>", "");
        s = s.replace("&nbsp;", " ");
        s = s.replace("&lt;", "<");
        s = s.replace("&gt;", ">");
        s = s.replace("&amp;", "&");
        s = s.replace("&quot;", "\"");
        s = s.replace("&#39;", "'");
        s = s.replace("\r", "");
        s = s.replaceAll("\\s+", " ");

        return s.trim();
    }

    public static String resolveAbilityFullTextForDisplay(Object datasheetAbility, DatasheetsPageState state) {
        if (datasheetAbility == null) return "";

        String localName = s(getAny(datasheetAbility, "name"));
        String localDesc = s(getAny(datasheetAbility, "description"));
        String abilityId = s(getAny(datasheetAbility, "ability_id"));

        Object master = abilityId.isBlank() ? null : state.getAbilitiesById().get(abilityId);
        String masterName = s(getAny(master, "name"));
        String masterDesc = s(getAny(master, "description"));

        String name = !localName.isBlank() ? localName : masterName;
        String desc = !localDesc.isBlank() ? localDesc : masterDesc;

        if (!name.isBlank() && !desc.isBlank()) return "<b>" + name + ":</b> " + desc;
        if (!name.isBlank()) return "<b>" + name + "</b>";
        if (!desc.isBlank()) return desc;

        return "";
    }

    public static String resolveAbilityName(Object datasheetAbility, DatasheetsPageState state) {
        if (datasheetAbility == null) return "";

        String localName = s(getAny(datasheetAbility, "name"));
        if (!localName.isBlank()) return localName;

        String abilityId = s(getAny(datasheetAbility, "ability_id"));
        if (abilityId.isBlank()) return "";

        Object master = state.getAbilitiesById().get(abilityId);
        return s(getAny(master, "name"));
    }
}