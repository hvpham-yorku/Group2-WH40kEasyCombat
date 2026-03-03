package eecs2311.group2.wh40k_easycombat.model;

import eecs2311.group2.wh40k_easycombat.annotation.*;

@Table("Datasheets_unit_composition")
@CompositePK(columns = {"datasheet_id", "line"})
public record Datasheets_unit_composition(
        @NotNull String datasheet_id,
        @NotNull String line,
        String description
) {}