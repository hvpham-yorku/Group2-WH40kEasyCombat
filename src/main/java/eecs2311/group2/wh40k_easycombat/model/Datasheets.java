package eecs2311.group2.wh40k_easycombat.model;

import eecs2311.group2.wh40k_easycombat.annotation.*;

@Table("Datasheets")
public record Datasheets(
		@PK @AutoIncrement int auto_id,
        @NotNull String id,
        String name,
        String faction_id,
        String source_id,
        String legend,
        String role,
        String loadout,
        String transport,
        boolean virtual,
        String leader_head,
        String leader_footer,
        String damaged_w,
        String damaged_description,
        String link
) {}