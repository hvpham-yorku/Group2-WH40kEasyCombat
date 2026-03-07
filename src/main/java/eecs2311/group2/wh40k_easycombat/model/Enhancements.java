package eecs2311.group2.wh40k_easycombat.model;

import eecs2311.group2.wh40k_easycombat.annotation.*;

@Table("Enhancements")
public record Enhancements(
		@PK @AutoIncrement int auto_id,
        @NotNull String id,
        String faction_id,
        String name,
        String legend,
        String description,
        String cost,
        String detachment,
        String detachment_id
) {}