package eecs2311.group2.wh40k_easycombat.model;

import eecs2311.group2.wh40k_easycombat.annotation.*;

@Table("Datasheets_options")
public record Datasheets_options(
		@PK @AutoIncrement int auto_id,
        @NotNull String datasheet_id,
        @NotNull String line,
        String button,
        String description
) {}