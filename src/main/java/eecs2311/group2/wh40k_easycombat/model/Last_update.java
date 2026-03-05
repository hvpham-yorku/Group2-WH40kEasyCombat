package eecs2311.group2.wh40k_easycombat.model;

import eecs2311.group2.wh40k_easycombat.annotation.*;

@Table("Last_update")
public record Last_update(
		@PK @AutoIncrement int auto_id,
        String last_update
) {}