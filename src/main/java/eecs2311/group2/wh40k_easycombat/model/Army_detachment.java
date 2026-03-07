package eecs2311.group2.wh40k_easycombat.model;
import eecs2311.group2.wh40k_easycombat.annotation.*;

@Table("Army_detachment")
public record Army_detachment(
		@PK @AutoIncrement int auto_id,
        int army_id,
        String datasheet_id,
        String detachment_id
) {}
