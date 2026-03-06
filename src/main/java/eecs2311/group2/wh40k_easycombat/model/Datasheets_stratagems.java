package eecs2311.group2.wh40k_easycombat.model;

import eecs2311.group2.wh40k_easycombat.annotation.*;

@Table("Datasheets_stratagems")
public record Datasheets_stratagems(
		@PK @AutoIncrement int auto_id,
        @NotNull String datasheet_id,
        @NotNull String stratagem_id
) {}