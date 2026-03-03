package eecs2311.group2.wh40k_easycombat.model;

import eecs2311.group2.wh40k_easycombat.annotation.*;

@Table("Datasheets_leader")
@CompositePK(columns = {"leader_id", "attached_id"})
public record Datasheets_leader(
        @NotNull String leader_id,
        @NotNull String attached_id
) {}