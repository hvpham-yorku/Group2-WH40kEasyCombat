package eecs2311.group2.wh40k_easycombat.model;

import eecs2311.group2.wh40k_easycombat.annotation.*;

@Table("Datasheets_detachment_abilities")
public record Datasheets_detachment_abilities(
		@PK @AutoIncrement int auto_id,
        @NotNull String datasheet_id,
        @NotNull String detachment_ability_id
) {}