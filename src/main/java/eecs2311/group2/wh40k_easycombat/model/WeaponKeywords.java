package eecs2311.group2.wh40k_easycombat.model;
import eecs2311.group2.wh40k_easycombat.annotation.*;

@Table("weapon_keywords")
public record WeaponKeywords(
    @PK @AutoIncrement int id,
    @NotNull String keyword
) {
}