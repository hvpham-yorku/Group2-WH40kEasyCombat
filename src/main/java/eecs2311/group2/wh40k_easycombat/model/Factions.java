package eecs2311.group2.wh40k_easycombat.model;

import eecs2311.group2.wh40k_easycombat.annotation.AutoIncrement;
import eecs2311.group2.wh40k_easycombat.annotation.NotNull;
import eecs2311.group2.wh40k_easycombat.annotation.PK;
import eecs2311.group2.wh40k_easycombat.annotation.Table;

@Table("factions")
public record Factions(
    @PK @AutoIncrement int id,
    @NotNull String name
) {
    
}
