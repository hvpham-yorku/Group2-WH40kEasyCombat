package eecs2311.group2.wh40k_easycombat.service.autobattle;

import eecs2311.group2.wh40k_easycombat.model.combat.PendingDamage;
import eecs2311.group2.wh40k_easycombat.model.instance.Player;
import eecs2311.group2.wh40k_easycombat.model.instance.UnitInstance;
import eecs2311.group2.wh40k_easycombat.service.game.ArmyListStateService;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public class PendingDamageSession {
    private final AutoBattleMode mode;
    private final Player attackingPlayer;
    private final String attackerUnitName;
    private final String defenderUnitName;
    private final UnitInstance defender;
    private final Set<String> destroyedBefore;
    private final List<PendingDamage> pendingDamages;

    public PendingDamageSession(
            AutoBattleMode mode,
            Player attackingPlayer,
            String attackerUnitName,
            String defenderUnitName,
            UnitInstance defender,
            Set<String> destroyedBefore,
            List<PendingDamage> pendingDamages
    ) {
        this.mode = mode == null ? AutoBattleMode.SHOOTING : mode;
        this.attackingPlayer = attackingPlayer == null ? Player.ATTACKER : attackingPlayer;
        this.attackerUnitName = attackerUnitName == null ? "" : attackerUnitName;
        this.defenderUnitName = defenderUnitName == null ? "" : defenderUnitName;
        this.defender = defender;
        this.destroyedBefore = destroyedBefore == null ? Set.of() : new LinkedHashSet<>(destroyedBefore);
        this.pendingDamages = pendingDamages == null ? new ArrayList<>() : new ArrayList<>(pendingDamages);
    }

    public AutoBattleMode mode() {
        return mode;
    }

    public Player attackingPlayer() {
        return attackingPlayer;
    }

    public String attackerUnitName() {
        return attackerUnitName;
    }

    public String defenderUnitName() {
        return defenderUnitName;
    }

    public UnitInstance defender() {
        return defender;
    }

    public Set<String> destroyedBefore() {
        return Set.copyOf(destroyedBefore);
    }

    public void markCurrentDestroyedAsSettled() {
        if (defender == null) {
            return;
        }

        destroyedBefore.clear();
        destroyedBefore.addAll(ArmyListStateService.destroyedModelIds(defender));
    }

    public boolean hasPendingDamage() {
        return defender != null && !defender.isDestroyed() && !pendingDamages.isEmpty();
    }

    public PendingDamage currentDamage() {
        return hasPendingDamage() ? pendingDamages.get(0) : null;
    }

    public int pendingDamageCount() {
        return pendingDamages.size();
    }

    public int totalPendingDamage() {
        int total = 0;
        for (PendingDamage pendingDamage : pendingDamages) {
            total += pendingDamage.damage();
        }
        return total;
    }

    public PendingDamage consumeCurrentDamage() {
        if (!hasPendingDamage()) {
            return null;
        }
        return pendingDamages.remove(0);
    }

    public void clearRemainingDamages() {
        pendingDamages.clear();
    }
}
