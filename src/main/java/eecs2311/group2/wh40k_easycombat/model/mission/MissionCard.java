package eecs2311.group2.wh40k_easycombat.model.mission;

import java.util.List;

public record MissionCard(
        MissionType type,
        String title,
        String intro,
        List<String> bodyLines
) {
    public MissionCard {
        type = type == null ? MissionType.SECONDARY : type;
        title = title == null ? "" : title.trim();
        intro = intro == null ? "" : intro.trim();
        bodyLines = bodyLines == null ? List.of() : List.copyOf(bodyLines);
    }
}
