package eecs2311.group2.wh40k_easycombat.model;

import eecs2311.group2.wh40k_easycombat.annotation.*;

@Table("Datasheets_leader")
public record Datasheets_leader(
		@PK @AutoIncrement int auto_id,
        @NotNull String leader_id,
        @NotNull String attached_id
) {}