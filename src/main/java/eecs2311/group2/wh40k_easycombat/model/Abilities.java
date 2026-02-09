package eecs2311.group2.wh40k_easycombat.model;

import eecs2311.group2.wh40k_easycombat.annotation.*;

@Table("abilities")
public record Abilities(
    @PK @AutoIncrement int id,
    @NotNull String name,
    @NotNull String description
) {

    
}


