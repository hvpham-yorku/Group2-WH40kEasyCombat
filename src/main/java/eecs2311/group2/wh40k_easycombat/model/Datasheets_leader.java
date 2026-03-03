package eecs2311.group2.wh40k_easycombat.model;

import eecs2311.group2.wh40k_easycombat.annotation.*;

@Table("Datasheets_leader")
@CompositePK(columns = {"datasheet_id", "attached_datasheet_id"})
public record Datasheets_leader(
        @NotNull String datasheet_id,
        @NotNull String attached_datasheet_id
) {}