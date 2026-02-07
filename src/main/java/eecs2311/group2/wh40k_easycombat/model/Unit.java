package eecs2311.group2.wh40k_easycombat.model;

import eecs2311.group2.wh40k_easycombat.annotation.PK;

import java.util.List;

public record Unit(
    @PK int id,
    int factionId,
    String name,
    int points,
    int M,
    int T,
    int SV,
    int W,
    int LD,
    int OC,
    int category,
    String composition,
    List<Integer> keywordIdList,
    List<Integer> rangedWeaponIdList,
    List<Integer> meleeWeaponIdList
) {
}
