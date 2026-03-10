package eecs2311.group2.wh40k_easycombat.model.instance;

public record StratagemInstance(
        String name,
        String cpCost,
        String turn,
        String phase,
        String descriptionHtml
) {
    public StratagemInstance {
        name = name == null ? "" : name;
        cpCost = cpCost == null ? "" : cpCost;
        turn = turn == null ? "" : turn;
        phase = phase == null ? "" : phase;
        descriptionHtml = descriptionHtml == null ? "" : descriptionHtml;
    }
}
