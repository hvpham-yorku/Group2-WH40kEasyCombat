package eecs2311.group2.wh40k_easycombat.service.game;

import eecs2311.group2.wh40k_easycombat.model.combat.AttackResult;
import eecs2311.group2.wh40k_easycombat.model.instance.Phase;
import eecs2311.group2.wh40k_easycombat.model.instance.Player;
import eecs2311.group2.wh40k_easycombat.model.instance.UnitInstance;
import eecs2311.group2.wh40k_easycombat.model.instance.WeaponProfile;
import eecs2311.group2.wh40k_easycombat.service.autobattle.AttackKeywordContext;
import eecs2311.group2.wh40k_easycombat.service.autobattle.AutoBattler;

public class GameEngine {
    CombatService stateManager;
    AutoBattler autoBattler = new AutoBattler();

    int mainMissionValue;
    int attackerSecondaryMissionValue;
    int defenderSecondaryMissionValue;
    int currentTurn;

    public void start() {
        stateManager = new CombatService();
        autoBattler = new AutoBattler();
        currentTurn = 1;
    }

    public void selectMainMission(String missionName, int pointValue) {
        stateManager.getCurrentState().setMissionName(missionName);
        this.mainMissionValue = pointValue;
    }

    public void selectAttackerMission(String missionName, int pointValue) {
        stateManager.getCurrentState().getAttackerArmy().setSecondaryMissionName(missionName);
        this.attackerSecondaryMissionValue = pointValue;
    }

    public void selectDefenderMission(String missionName, int pointValue) {
        stateManager.getCurrentState().getDefenderArmy().setSecondaryMissionName(missionName);
        this.defenderSecondaryMissionValue = pointValue;
    }

    public void endPlayerTurn() {
        stateManager.switchActivePlayer();
        if (currentTurn % 2 == 0) {
            stateManager.advancePhase();
            stateManager.addCp(stateManager.getActivePlayer(), 1);
            stateManager.addCp(stateManager.getInactivePlayer(), 1);
        }
        currentTurn++;
    }

    public AttackResult performBattle(String defenderId, String attackerId, String weaponName) {
        return performBattle(defenderId, attackerId, weaponName, AttackKeywordContext.none());
    }

    public AttackResult performBattle(
            String defenderId,
            String attackerId,
            String weaponName,
            AttackKeywordContext keywordContext
    ) {
        if (stateManager == null) {
            return AttackResult.notResolved("Battle state has not been started.");
        }
        if (autoBattler == null) {
            autoBattler = new AutoBattler();
        }

        Player attackerPlayer = stateManager.getActivePlayer();
        Player defenderPlayer = stateManager.getInactivePlayer();

        UnitInstance attacker = getUnitInstance(attackerPlayer, attackerId);
        UnitInstance defender = getUnitInstance(defenderPlayer, defenderId);
        WeaponProfile weapon = getWeaponProfile(attackerPlayer, attackerId, weaponName);

        if (attacker == null) {
            return AttackResult.notResolved("Attacker unit not found.");
        }
        if (defender == null) {
            return AttackResult.notResolved("Defender unit not found.");
        }
        if (weapon == null) {
            return AttackResult.notResolved("Weapon not found.");
        }

        Phase phase = stateManager.getCurrentState().getCurrentPhase();
        boolean phaseAllowed =
                (phase == Phase.SHOOTING && !weapon.melee())
                        || (phase == Phase.FIGHT && weapon.melee());

        if (!phaseAllowed) {
            return AttackResult.notResolved("That weapon cannot be resolved in the current phase.");
        }

        return autoBattler.simulateAttack(attacker, defender, weapon, keywordContext);
    }

    public void useStrategem(String name) {
        int cpCost = Integer.parseInt(stateManager.getActiveArmy().getStrategemInstanceByName(name).cpCost());
        stateManager.spendCp(stateManager.getActivePlayer(), cpCost);
    }

    public void completedMission() {
        stateManager.addVp(stateManager.getActivePlayer(), mainMissionValue);
    }

    public void completedSecondaryMission() {
        int missionValue = stateManager.getActivePlayer() == Player.ATTACKER
                ? attackerSecondaryMissionValue
                : defenderSecondaryMissionValue;
        stateManager.addVp(stateManager.getActivePlayer(), missionValue);
        stateManager.getActiveArmy().setSecondaryMissionName("");
    }

    private UnitInstance getUnitInstance(Player player, String id) {
        return stateManager.getCurrentState().getArmy(player).getUnitInstanceById(id);
    }

    private WeaponProfile getWeaponProfile(Player player, String unitId, String weaponName) {
        return getUnitInstance(player, unitId).getWeaponProfileByName(weaponName);
    }
}
