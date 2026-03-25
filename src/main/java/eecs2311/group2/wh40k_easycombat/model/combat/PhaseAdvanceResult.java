package eecs2311.group2.wh40k_easycombat.model.combat;

import eecs2311.group2.wh40k_easycombat.model.instance.Phase;
import eecs2311.group2.wh40k_easycombat.model.instance.Player;

public record PhaseAdvanceResult(
        int currentRound,
        Phase currentPhase,
        Player activePlayer,
        Player commandPointRecipient
) {
    public boolean awardedCommandPoint() {
        return commandPointRecipient != null;
    }
}
