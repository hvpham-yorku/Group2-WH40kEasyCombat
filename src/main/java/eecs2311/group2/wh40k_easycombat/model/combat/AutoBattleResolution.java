package eecs2311.group2.wh40k_easycombat.model.combat;

import java.util.List;

import eecs2311.group2.wh40k_easycombat.service.autobattle.PendingDamageSession;

public record AutoBattleResolution(
        boolean resolved,
        String failureMessage,
        List<ResolvedAttack> attacks,
        CasualtyUpdate casualtyUpdate,
        boolean hazardousTriggered,
        PendingDamageSession allocationSession
) {
    public AutoBattleResolution {
        failureMessage = failureMessage == null ? "" : failureMessage;
        attacks = attacks == null ? List.of() : List.copyOf(attacks);
        casualtyUpdate = casualtyUpdate == null ? CasualtyUpdate.none() : casualtyUpdate;
    }

    public static AutoBattleResolution failure(String message) {
        return new AutoBattleResolution(false, message, List.of(), CasualtyUpdate.none(), false, null);
    }
}
