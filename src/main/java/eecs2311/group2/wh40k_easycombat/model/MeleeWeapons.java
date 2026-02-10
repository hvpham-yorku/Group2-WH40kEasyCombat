package eecs2311.group2.wh40k_easycombat.model;
import eecs2311.group2.wh40k_easycombat.annotation.*;
import java.util.List;

@Table("melee_weapons")
public record MeleeWeapons(
    @PK @AutoIncrement int id,
    @NotNull String name,
    String A,
    Integer WS,
    Integer S,
    Integer AP,
    String D,
    List<Integer> keywordIdList
) {
}