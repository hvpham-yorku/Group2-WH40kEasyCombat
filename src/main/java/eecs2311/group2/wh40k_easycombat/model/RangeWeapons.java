package eecs2311.group2.wh40k_easycombat.model;
import eecs2311.group2.wh40k_easycombat.annotation.*;
import java.util.List;

@Table("ranged_weapons")
public record RangeWeapons(
    @PK @AutoIncrement int id,
    @NotNull String name,
    @NotNull Integer range,
    @NotNull String A,
    @NotNull Integer BS,
    @NotNull Integer S,
    @NotNull Integer AP,
    @NotNull String D,
    @NotNull List<Integer> keywordIdList
) {
}