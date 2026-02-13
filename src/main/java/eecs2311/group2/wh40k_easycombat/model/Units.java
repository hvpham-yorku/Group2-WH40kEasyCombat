package eecs2311.group2.wh40k_easycombat.model;

import eecs2311.group2.wh40k_easycombat.annotation.*;

import java.util.List;

@Table("units")
public record Units(
    @PK @AutoIncrement int id,
    @NotNull @FK(table = "factions", column = "id") int factionId,
    @NotNull String name,
    @NotNull @Check("points >= 0")
    int points,
    @NotNull int M,
    @NotNull int T,
    @NotNull int SV,
    @NotNull int W,
    @NotNull int LD,
    @NotNull int OC,
    @NotNull int invulnerableSave,
    @NotNull int category,
    @NotNull String composition,
    @NotNull List<Integer> coreAbilityIdList,
    @NotNull List<Integer> otherAbilityIdList,
    @NotNull List<Integer> keywordIdList,
    @NotNull List<Integer> rangedWeaponIdList,
    @NotNull List<Integer> meleeWeaponIdList
) {
}
