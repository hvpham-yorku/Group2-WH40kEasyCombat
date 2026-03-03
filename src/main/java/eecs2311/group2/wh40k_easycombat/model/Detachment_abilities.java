package eecs2311.group2.wh40k_easycombat.model;

import eecs2311.group2.wh40k_easycombat.annotation.*;

@Table("Detachment_abilities")
@CompositePK(columns = {"id", "detachment_id"})
public record Detachment_abilities(
        @NotNull String id,
        @NotNull String detachment_id,
        String faction_id,
        String name,
        String legend,
        String description,
        String detachment
) {}