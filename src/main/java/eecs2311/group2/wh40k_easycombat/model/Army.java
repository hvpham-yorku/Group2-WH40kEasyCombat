package eecs2311.group2.wh40k_easycombat.model;

import eecs2311.group2.wh40k_easycombat.annotation.*;

@Table("Army")
public record Army(
		@PK @AutoIncrement int auto_id,
        @NotNull String name,
        int total_points
) {}