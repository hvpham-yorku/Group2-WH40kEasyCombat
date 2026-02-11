package eecs2311.group2.wh40k_easycombat.model;
import eecs2311.group2.wh40k_easycombat.annotation.*;
import java.util.List;

@Table("melee_weapons")
public record MeleeWeapons(
    @PK @AutoIncrement int id,
    @NotNull String name,
    @NotNull String A,
    @NotNull Integer WS,
    @NotNull Integer S,
    @NotNull Integer AP,
    @NotNull String D,
    @NotNull List<Integer> keywordIdList
) {
}