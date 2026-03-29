package eecs2311.group2.wh40k_easycombat.service.game;

import eecs2311.group2.wh40k_easycombat.model.combat.AttackResult;
import eecs2311.group2.wh40k_easycombat.model.combat.PhaseAdvanceResult;
import eecs2311.group2.wh40k_easycombat.model.instance.ArmyInstance;
import eecs2311.group2.wh40k_easycombat.model.instance.Phase;
import eecs2311.group2.wh40k_easycombat.model.instance.Player;
import eecs2311.group2.wh40k_easycombat.model.instance.UnitInstance;
import eecs2311.group2.wh40k_easycombat.model.instance.WeaponProfile;
import eecs2311.group2.wh40k_easycombat.service.autobattle.AttackKeywordContext;
import eecs2311.group2.wh40k_easycombat.service.autobattle.AutoBattleMode;
import eecs2311.group2.wh40k_easycombat.service.autobattle.AutoBattler;
import eecs2311.group2.wh40k_easycombat.viewmodel.GameArmyImportVM;
import eecs2311.group2.wh40k_easycombat.viewmodel.GameArmyUnitVM;
import eecs2311.group2.wh40k_easycombat.viewmodel.GameStrategyVM;

import java.util.Locale;

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
        mainMissionValue = 0;
        attackerSecondaryMissionValue = 0;
        defenderSecondaryMissionValue = 0;
        configureBattle("", 5);
    }

    public void configureBattle(String missionName, int maxRounds) {
        ensureStateManager().resetBattle(missionName, maxRounds);
        stateManager.setArmy(Player.ATTACKER, defaultArmy(Player.ATTACKER));
        stateManager.setArmy(Player.DEFENDER, defaultArmy(Player.DEFENDER));
        stateManager.setCurrentCp(Player.ATTACKER, 1);
        stateManager.setCurrentCp(Player.DEFENDER, 1);
        stateManager.addCp(Player.ATTACKER, 1);
        stateManager.setCurrentVp(Player.ATTACKER, 0);
        stateManager.setCurrentVp(Player.DEFENDER, 0);
        mainMissionValue = 0;
        attackerSecondaryMissionValue = 0;
        defenderSecondaryMissionValue = 0;
        currentTurn = 1;
    }

    public void replaceArmy(Player player, GameArmyImportVM data) {
        if (player == null || data == null) {
            return;
        }

        ArmyInstance existing = stateManager.getArmy(player);
        ArmyInstance replacement = new ArmyInstance(
                data.armyId(),
                data.armyName(),
                data.factionId(),
                data.factionName(),
                existing == null ? "" : existing.getDetachmentId()
        );

        if (existing != null) {
            replacement.setSecondaryMissionName(existing.getSecondaryMissionName());
            replacement.setCurrentCp(existing.getCurrentCp());
            replacement.setCurrentVp(existing.getCurrentVp());
        }

        replacement.replaceUnits(data.units().stream().map(GameArmyUnitVM::getUnit).toList());
        replacement.replaceStrategies(data.strategies().stream().map(GameStrategyVM::getStrategy).toList());
        stateManager.setArmy(player, replacement);
    }

    public PhaseAdvanceResult advancePhase() {
        PhaseAdvanceResult result = ensureStateManager().advancePhaseState();
        if (stateManager.getCurrentRound() > stateManager.getMaxRounds()) {
            stateManager.setBattleOver(true);
        }
        return result;
    }

    public Phase getCurrentPhase() {
        return ensureStateManager().getCurrentPhase();
    }

    public int getCurrentRound() {
        return ensureStateManager().getCurrentRound();
    }

    public int getMaxRounds() {
        return ensureStateManager().getMaxRounds();
    }

    public Player getActivePlayer() {
        return ensureStateManager().getActivePlayer();
    }

    public Player getInactivePlayer() {
        return ensureStateManager().getInactivePlayer();
    }

    public String phaseLabelFor(Player player) {
        return ensureStateManager().phaseLabelFor(player);
    }

    public AutoBattleMode currentAutoBattleMode() {
        return ensureStateManager().currentAutoBattleMode();
    }

    public int currentCp(Player player) {
        return ensureStateManager().getCurrentCp(player);
    }

    public void setCommandPoints(Player player, int value) {
        ensureStateManager().setCurrentCp(player, value);
    }

    public void addCommandPoint(Player player) {
        ensureStateManager().addCp(player, 1);
    }

    public void adjustCommandPoints(Player player, int delta) {
        if (delta == 0) {
            return;
        }

        if (delta > 0) {
            ensureStateManager().addCp(player, delta);
            return;
        }

        ensureStateManager().setCurrentCp(player, Math.max(0, currentCp(player) + delta));
    }

    public int currentVp(Player player) {
        return ensureStateManager().getCurrentVp(player);
    }

    public void addVictoryPoints(Player player, int amount) {
        ensureStateManager().addVp(player, amount);
    }

    public boolean isBattleOver() {
        return ensureStateManager().isBattleOver();
    }

    public void finishBattle() {
        ensureStateManager().setBattleOver(true);
    }

    public ArmyInstance getArmy(Player player) {
        return ensureStateManager().getArmy(player);
    }

    public String winnerText() {
        int attackerVp = currentVp(Player.ATTACKER);
        int defenderVp = currentVp(Player.DEFENDER);

        if (attackerVp == defenderVp) {
            return "Battle Over. The game is a draw at " + attackerVp + " VP each.";
        }

        boolean attackerWon = attackerVp > defenderVp;
        return String.format(
                Locale.ROOT,
                "Battle Over. %s wins %d to %d VP.",
                attackerWon ? "Attacker" : "Defender",
                attackerWon ? attackerVp : defenderVp,
                attackerWon ? defenderVp : attackerVp
        );
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

    private CombatService ensureStateManager() {
        if (stateManager == null) {
            start();
        }
        return stateManager;
    }

    private ArmyInstance defaultArmy(Player player) {
        return new ArmyInstance(
                player == Player.ATTACKER ? 0 : -1,
                player == Player.ATTACKER ? "Attacker Army" : "Defender Army",
                "",
                "",
                ""
        );
    }
}
