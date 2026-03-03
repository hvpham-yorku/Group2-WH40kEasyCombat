package eecs2311.group2.wh40k_easycombat.model;

import eecs2311.group2.wh40k_easycombat.annotation.*;

@Table("Datasheets_options")
@CompositePK(columns = {"datasheet_id", "line"})
public record Datasheets_options(
        @NotNull String datasheet_id,
        @NotNull String line,
        String button,
        String description
) {}