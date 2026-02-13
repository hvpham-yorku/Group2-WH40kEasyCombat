package eecs2311.group2.wh40k_easycombat.model;
import eecs2311.group2.wh40k_easycombat.annotation.*;

@Table("other_abilities")
public record OtherAbilities(
    @PK @AutoIncrement int id,
    @NotNull String ability
) {
}