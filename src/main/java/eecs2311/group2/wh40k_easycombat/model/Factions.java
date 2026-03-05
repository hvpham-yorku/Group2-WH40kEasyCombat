package eecs2311.group2.wh40k_easycombat.model;

import eecs2311.group2.wh40k_easycombat.annotation.*;

@Table("Factions")
public record Factions(
		@PK @AutoIncrement int auto_id,
        @NotNull String id,
        String name,
        String link
) {}
