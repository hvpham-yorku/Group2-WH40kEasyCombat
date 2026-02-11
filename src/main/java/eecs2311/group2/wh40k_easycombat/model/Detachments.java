package eecs2311.group2.wh40k_easycombat.model;
import eecs2311.group2.wh40k_easycombat.annotation.*;

@Table("detachments")
public record Detachments(
    @PK @AutoIncrement int id,
    @NotNull String name,
    @NotNull @FK(table = "factions", column = "id") int factionId,
    Integer strategyId,
    String detachmentRule
) {
}