package eecs2311.group2.wh40k_easycombat.model;
import eecs2311.group2.wh40k_easycombat.annotation.*;

@Table("unit_keywords")
public record UnitKeywords(
    @PK @AutoIncrement int id,
    @NotNull String keyword
) {
}