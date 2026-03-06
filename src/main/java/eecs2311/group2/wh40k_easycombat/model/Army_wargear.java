package eecs2311.group2.wh40k_easycombat.model;

import eecs2311.group2.wh40k_easycombat.annotation.*;

@Table("Army_wargear")
public record Army_wargear(
		@PK @AutoIncrement int auto_id,
        @NotNull int wargear_id,
        int units_id,
        int wargear_count
) {}
