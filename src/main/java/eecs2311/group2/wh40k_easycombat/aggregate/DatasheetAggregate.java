package eecs2311.group2.wh40k_easycombat.aggregate;

import eecs2311.group2.wh40k_easycombat.model.Datasheets;
import eecs2311.group2.wh40k_easycombat.model.Datasheets_abilities;
import eecs2311.group2.wh40k_easycombat.model.Datasheets_detachment_abilities;
import eecs2311.group2.wh40k_easycombat.model.Datasheets_enhancements;
import eecs2311.group2.wh40k_easycombat.model.Datasheets_keywords;
import eecs2311.group2.wh40k_easycombat.model.Datasheets_leader;
import eecs2311.group2.wh40k_easycombat.model.Datasheets_models;
import eecs2311.group2.wh40k_easycombat.model.Datasheets_models_cost;
import eecs2311.group2.wh40k_easycombat.model.Datasheets_options;
import eecs2311.group2.wh40k_easycombat.model.Datasheets_stratagems;
import eecs2311.group2.wh40k_easycombat.model.Datasheets_unit_composition;
import eecs2311.group2.wh40k_easycombat.model.Datasheets_wargear;

import java.util.List;

public final class DatasheetAggregate {
    public final Datasheets datasheet;
    public final List<Datasheets_models> models;
    public final List<Datasheets_wargear> wargear;
    public final List<Datasheets_abilities> abilities;
    public final List<Datasheets_unit_composition> compositions;
    public final List<Datasheets_models_cost> costs;
    public final List<Datasheets_keywords> keywords;
    public final List<Datasheets_options> options;
    public final List<Datasheets_leader> leaders;
    public final List<Datasheets_stratagems> stratagems;
    public final List<Datasheets_enhancements> enhancements;
    public final List<Datasheets_detachment_abilities> detachmentAbilities;

    public DatasheetAggregate(
            Datasheets datasheet,
            List<Datasheets_models> models,
            List<Datasheets_wargear> wargear,
            List<Datasheets_abilities> abilities,
            List<Datasheets_unit_composition> compositions,
            List<Datasheets_models_cost> costs,
            List<Datasheets_keywords> keywords,
            List<Datasheets_options> options,
            List<Datasheets_leader> leaders,
            List<Datasheets_stratagems> stratagems,
            List<Datasheets_enhancements> enhancements,
            List<Datasheets_detachment_abilities> detachmentAbilities
    ) {
        this.datasheet = datasheet;
        this.models = immutableList(models);
        this.wargear = immutableList(wargear);
        this.abilities = immutableList(abilities);
        this.compositions = immutableList(compositions);
        this.costs = immutableList(costs);
        this.keywords = immutableList(keywords);
        this.options = immutableList(options);
        this.leaders = immutableList(leaders);
        this.stratagems = immutableList(stratagems);
        this.enhancements = immutableList(enhancements);
        this.detachmentAbilities = immutableList(detachmentAbilities);
    }

    private static <T> List<T> immutableList(List<T> source) {
        return source == null ? List.of() : List.copyOf(source);
    }
}