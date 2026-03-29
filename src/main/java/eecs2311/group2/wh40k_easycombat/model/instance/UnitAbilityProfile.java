package eecs2311.group2.wh40k_easycombat.model.instance;

public record UnitAbilityProfile(
        String name,
        String description,
        String type
) {
    public UnitAbilityProfile {
        name = name == null ? "" : name.trim();
        description = description == null ? "" : description.trim();
        type = type == null ? "" : type.trim();
    }
}
