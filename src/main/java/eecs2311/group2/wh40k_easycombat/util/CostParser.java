package eecs2311.group2.wh40k_easycombat.util;

import eecs2311.group2.wh40k_easycombat.model.Datasheets_models_cost;

import java.util.*;
import java.util.regex.*;

public class CostParser {

    private static final Pattern NUMBER = Pattern.compile("\\d+");

    public static int extractModels(String description) {

        if (description == null) return 1;

        Matcher m = NUMBER.matcher(description);

        int sum = 0;

        while (m.find()) {
            sum += Integer.parseInt(m.group());
        }

        return sum > 0 ? sum : 1;
    }

    public static List<CostTier> parseTiers(List<Datasheets_models_cost> costs) {

        List<CostTier> tiers = new ArrayList<>();

        if (costs == null) return tiers;

        for (Datasheets_models_cost c : costs) {

            int models = extractModels(c.description());

            int points = 0;

            try {
                points = Integer.parseInt(c.cost());
            } catch (Exception ignored) {}

            tiers.add(new CostTier(models, points));
        }

        tiers.sort(Comparator.comparingInt(CostTier::models));

        return tiers;
    }

    public static int pointsForModels(int models, List<CostTier> tiers) {

        if (tiers.isEmpty()) return 0;

        for (CostTier t : tiers) {
            if (models <= t.models()) {
                return t.points();
            }
        }

        return tiers.get(tiers.size() - 1).points();
    }
}