package eecs2311.group2.wh40k_easycombat.model.mission;

import eecs2311.group2.wh40k_easycombat.model.instance.Player;

public record MissionResolution(
        MissionDecision decision,
        Player awardedPlayer,
        int vpAwarded
) {
    public MissionResolution {
        decision = decision == null ? MissionDecision.CLOSED : decision;
        awardedPlayer = awardedPlayer == null ? Player.ATTACKER : awardedPlayer;
        vpAwarded = Math.max(0, vpAwarded);
    }

    public static MissionResolution closed() {
        return new MissionResolution(MissionDecision.CLOSED, Player.ATTACKER, 0);
    }
}
