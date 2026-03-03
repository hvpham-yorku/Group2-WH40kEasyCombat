package eecs2311.group2.wh40k_easycombat.model;

import eecs2311.group2.wh40k_easycombat.annotation.*;

@Table("Abilities")
public record Abilities(
        @PK String id,
        String name,
        String legend,
        String faction_id,
        String description
) {}