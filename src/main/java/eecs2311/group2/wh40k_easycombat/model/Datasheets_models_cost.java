package eecs2311.group2.wh40k_easycombat.model;

import eecs2311.group2.wh40k_easycombat.annotation.*;

@Table("Datasheets_models_cost")
@CompositePK(columns = {"datasheet_id", "line"})
public record Datasheets_models_cost(
        @NotNull String datasheet_id,
        @NotNull String line,
        String description,
        String cost
) {}