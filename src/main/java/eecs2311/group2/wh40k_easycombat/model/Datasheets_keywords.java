package eecs2311.group2.wh40k_easycombat.model;

import eecs2311.group2.wh40k_easycombat.annotation.*;

@Table("Datasheets_keywords")
@CompositePK(columns = {"datasheet_id", "keyword", "model"})
public record Datasheets_keywords(
        @NotNull String datasheet_id,
        @NotNull String keyword,
        @NotNull String model,
        String is_faction_keyword
) {}