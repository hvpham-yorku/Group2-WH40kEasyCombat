package eecs2311.group2.wh40k_easycombat.model;

import eecs2311.group2.wh40k_easycombat.annotation.*;

@Table("weapons")
public record Weapons(
    @PK @AutoIncrement int id,
    @NotNull String name,
    @NotNull @Check("weaponType IN ('ranged','melee')") String weaponType,

    @NotNull int attacks,   // A
    @NotNull int skill,     // BS or WS depending on weapon type 
    @NotNull int strength,  // S
    @NotNull int ap,        // AP (armor penetration)
    @NotNull int damage,    // D
    int range               
) {

}

