package eecs2311.group2.wh40k_easycombat.model;

import eecs2311.group2.wh40k_easycombat.annotation.*;

@Table("Source")
public record Source(
        @PK String id,
        String name,
        String type,
        String edition,
        String version,
        String errata_date,
        String errata_link
) {}
