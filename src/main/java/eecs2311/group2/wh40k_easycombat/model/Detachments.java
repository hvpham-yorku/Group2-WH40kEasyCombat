package eecs2311.group2.wh40k_easycombat.model;

import eecs2311.group2.wh40k_easycombat.annotation.*;

@Table("Detachments")
@CompositePK(columns = {"id", "faction_id"})
public record Detachments(
        @NotNull String id,
        @NotNull String faction_id,
        String name,
        String legend,
        String type
) {}