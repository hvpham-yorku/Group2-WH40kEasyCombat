package eecs2311.group2.wh40k_easycombat.model;
import eecs2311.group2.wh40k_easycombat.annotation.*;
import java.util.List;

@Table("ranged_weapons")
public record RangeWeapons(
    @PK @AutoIncrement int id,
    @NotNull String name,
    Integer range,
    String A,
    Integer BS,
    Integer S,
    Integer AP,
    String D,
    List<Integer> keywordIdList
) {
}