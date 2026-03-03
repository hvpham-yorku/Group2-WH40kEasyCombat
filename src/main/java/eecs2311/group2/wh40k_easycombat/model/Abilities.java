package eecs2311.group2.wh40k_easycombat.model;

import eecs2311.group2.wh40k_easycombat.annotation.*;

@Table("Abilities")
@CompositePK(columns = {"id", "faction_id"})
public record Abilities(
        @NotNull String id,
        @NotNull String faction_id,
        String name,
        String legend,
        String description
) {}