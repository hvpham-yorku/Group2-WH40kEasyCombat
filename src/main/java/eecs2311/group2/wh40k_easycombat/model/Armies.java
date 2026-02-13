package eecs2311.group2.wh40k_easycombat.model;
import eecs2311.group2.wh40k_easycombat.annotation.*;
import java.util.List;

@Table("armies")
public record Armies(
    @PK @AutoIncrement int id,
    @NotNull String name,
    Boolean isFavorite,
    Integer totalPoints,
    Integer warlordId,
    @NotNull @FK(table = "factions", column = "id") int factionId,
    @FK(table = "detachments", column = "id") Integer detachmentId,
    List<Integer> unitIdList,
    List<Integer> equippedRangedWeaponIdList,
    List<Integer> equippedMeleeWeaponIdList
) {
}