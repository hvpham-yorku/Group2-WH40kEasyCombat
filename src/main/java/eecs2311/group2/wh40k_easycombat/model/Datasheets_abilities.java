package eecs2311.group2.wh40k_easycombat.model;

import eecs2311.group2.wh40k_easycombat.annotation.*;

@Table("Datasheets_abilities")
@CompositePK(columns = {"datasheet_id", "line"})
public record Datasheets_abilities(
        @NotNull String datasheet_id,
        @NotNull String line,
        String ability_id,
        String model,
        String name,
        String description,
        String type,
        String parameter
) {}