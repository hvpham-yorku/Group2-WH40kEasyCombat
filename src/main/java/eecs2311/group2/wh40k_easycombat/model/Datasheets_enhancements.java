package eecs2311.group2.wh40k_easycombat.model;

import eecs2311.group2.wh40k_easycombat.annotation.*;

@Table("Datasheets_enhancements")
@CompositePK(columns = {"datasheet_id", "enhancement_id"})
public record Datasheets_enhancements(
        @NotNull String datasheet_id,
        @NotNull String enhancement_id
) {}