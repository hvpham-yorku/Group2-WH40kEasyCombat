package eecs2311.group2.wh40k_easycombat.model;
import eecs2311.group2.wh40k_easycombat.annotation.*;

@Table("core_abilities")
public record CoreAbilities(
    @PK @AutoIncrement int id,
    @NotNull String ability
) {
}