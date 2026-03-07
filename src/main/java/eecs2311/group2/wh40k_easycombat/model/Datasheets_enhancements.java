package eecs2311.group2.wh40k_easycombat.model;

import eecs2311.group2.wh40k_easycombat.annotation.*;

@Table("Datasheets_enhancements")
public record Datasheets_enhancements(
		@PK @AutoIncrement int auto_id,
        @NotNull String datasheet_id,
        @NotNull String enhancement_id
) {}