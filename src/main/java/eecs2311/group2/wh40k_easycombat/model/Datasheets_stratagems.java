package eecs2311.group2.wh40k_easycombat.model;

import eecs2311.group2.wh40k_easycombat.annotation.*;

@Table("Datasheets_stratagems")
@CompositePK(columns = {"datasheet_id", "stratagem_id"})
public record Datasheets_stratagems(
        @NotNull String datasheet_id,
        @NotNull String stratagem_id
) {}