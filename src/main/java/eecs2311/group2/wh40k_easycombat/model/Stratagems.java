package eecs2311.group2.wh40k_easycombat.model;

import eecs2311.group2.wh40k_easycombat.annotation.*;

@Table("Stratagems")
public record Stratagems(
		@PK @AutoIncrement int auto_id,
        @NotNull String id,
        @NotNull String faction_id,
        String name,
        String type,
        String cp_cost,
        String legend,
        String turn,
        String phase,
        String description,
        String detachment,
        String detachment_id
) {}