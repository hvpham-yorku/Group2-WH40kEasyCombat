package eecs2311.group2.wh40k_easycombat.model;

import eecs2311.group2.wh40k_easycombat.annotation.*;

@Table("Datasheets_wargear")
@CompositePK(columns = {"datasheet_id", "line", "line_in_wargear"})
public record Datasheets_wargear(
        @NotNull String datasheet_id,
        @NotNull String line,
        @NotNull String line_in_wargear,
        String dice,
        String name,
        String description,
        String range,
        String type,
        String A,
        String BS_WS,
        String S,
        String AP,
        String D
) {}