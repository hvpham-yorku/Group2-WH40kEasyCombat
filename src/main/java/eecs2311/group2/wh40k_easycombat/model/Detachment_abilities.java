package eecs2311.group2.wh40k_easycombat.model;

import eecs2311.group2.wh40k_easycombat.annotation.*;

@Table("Detachment_abilities")
public record Detachment_abilities(
        @PK String id,
        String faction_id,
        String name,
        String legend,
        String description,
        String detachment,
        String detachment_id
) {}