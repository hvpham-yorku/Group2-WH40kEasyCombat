package eecs2311.group2.wh40k_easycombat.model;

import eecs2311.group2.wh40k_easycombat.annotation.*;

@Table("Army_units")
public record Army_units(
		@PK @AutoIncrement int auto_id,
        int army_id,
        String datasheet_id,
        int model_count
) {}