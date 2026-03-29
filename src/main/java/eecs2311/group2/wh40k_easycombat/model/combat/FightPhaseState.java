package eecs2311.group2.wh40k_easycombat.model.combat;

import eecs2311.group2.wh40k_easycombat.model.instance.Player;
import eecs2311.group2.wh40k_easycombat.service.autobattle.FightStep;

public record FightPhaseState(
        FightStep step,
        Player nextPlayer,
        String message
) {
    public static FightPhaseState complete(String message) {
        return new FightPhaseState(
                FightStep.COMPLETE,
                null,
                message == null || message.isBlank()
                        ? "Fight phase complete."
                        : message
        );
    }
}
