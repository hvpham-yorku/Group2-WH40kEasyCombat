package eecs2311.group2.wh40k_easycombat.model;

import eecs2311.group2.wh40k_easycombat.annotation.*;

@Table("Detachments")
public record Detachments(
		@PK @AutoIncrement int auto_id,
        @NotNull String id,
        @NotNull String faction_id,
        String name,
        String legend,
        String type
) {}