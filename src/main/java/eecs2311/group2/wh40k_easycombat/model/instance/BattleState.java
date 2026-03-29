package eecs2311.group2.wh40k_easycombat.model.instance;

import eecs2311.group2.wh40k_easycombat.model.combat.PhaseAdvanceResult;
import eecs2311.group2.wh40k_easycombat.service.autobattle.AutoBattleMode;

import java.util.ArrayList;
import java.util.List;

public class BattleState {
    private String missionName;
    private int maxRounds;
    private int currentRound;
    private Phase currentPhase;
    private Player activePlayer;
    private boolean battleOver;

    private ArmyInstance attackerArmy;
    private ArmyInstance defenderArmy;

    public BattleState() {
        reset("", 5);
    }

    public BattleState(String missionName) {
        reset(missionName, 5);
    }

    public BattleState(String missionName, int maxRounds) {
        reset(missionName, maxRounds);
    }

    public void reset(String missionName) {
        reset(missionName, 5);
    }

    public void reset(String missionName, int maxRounds) {
        this.missionName = missionName == null ? "" : missionName;
        this.maxRounds = Math.max(1, maxRounds);
        this.currentRound = 1;
        this.currentPhase = Phase.COMMAND;
        this.activePlayer = Player.ATTACKER;
        this.battleOver = false;
        this.attackerArmy = null;
        this.defenderArmy = null;
    }

    public String getMissionName() {
        return missionName;
    }

    public void setMissionName(String missionName) {
        this.missionName = missionName == null ? "" : missionName;
    }

    public int getMaxRounds() {
        return maxRounds;
    }

    public void setMaxRounds(int maxRounds) {
        this.maxRounds = Math.max(1, maxRounds);
    }

    public int getCurrentRound() {
        return currentRound;
    }

    public void setCurrentRound(int currentRound) {
        this.currentRound = Math.max(1, currentRound);
    }

    public Phase getCurrentPhase() {
        return currentPhase;
    }

    public void setCurrentPhase(Phase currentPhase) {
        this.currentPhase = currentPhase == null ? Phase.COMMAND : currentPhase;
    }

    public Player getActivePlayer() {
        return activePlayer;
    }

    public Player getInactivePlayer() {
        return activePlayer == Player.ATTACKER ? Player.DEFENDER : Player.ATTACKER;
    }

    public void setActivePlayer(Player activePlayer) {
        this.activePlayer = activePlayer == null ? Player.ATTACKER : activePlayer;
    }

    public boolean isBattleOver() {
        return battleOver;
    }

    public void setBattleOver(boolean battleOver) {
        this.battleOver = battleOver;
    }

    public ArmyInstance getAttackerArmy() {
        return attackerArmy;
    }

    public void setAttackerArmy(ArmyInstance attackerArmy) {
        this.attackerArmy = attackerArmy;
    }

    public ArmyInstance getDefenderArmy() {
        return defenderArmy;
    }

    public void setDefenderArmy(ArmyInstance defenderArmy) {
        this.defenderArmy = defenderArmy;
    }

    public ArmyInstance getArmy(Player player) {
        if (player == null) {
            return null;
        }
        return player == Player.ATTACKER ? attackerArmy : defenderArmy;
    }

    public void setArmy(Player player, ArmyInstance army) {
        if (player == null) {
            throw new IllegalArgumentException("player must not be null");
        }

        if (player == Player.ATTACKER) {
            attackerArmy = army;
        } else {
            defenderArmy = army;
        }
    }

    public ArmyInstance getActiveArmy() {
        return getArmy(activePlayer);
    }

    public ArmyInstance getInactiveArmy() {
        return getArmy(getInactivePlayer());
    }

    public void switchActivePlayer() {
        activePlayer = getInactivePlayer();
    }

    public boolean canOpenAutoBattle() {
        return currentAutoBattleMode() != null;
    }

    public AutoBattleMode currentAutoBattleMode() {
        return switch (currentPhase) {
            case MOVEMENT, CHARGE -> AutoBattleMode.REACTION_SHOOTING;
            case SHOOTING -> AutoBattleMode.SHOOTING;
            case FIGHT -> AutoBattleMode.FIGHT;
            case COMMAND -> null;
        };
    }

    public String phaseText() {
        return switch (currentPhase) {
            case COMMAND -> "Command";
            case MOVEMENT -> "Movement";
            case SHOOTING -> "Shooting";
            case CHARGE -> "Charge";
            case FIGHT -> "Fight";
        };
    }

    public String phaseLabelFor(Player player) {
        return player == activePlayer ? phaseText() + " (Active)" : phaseText();
    }

    public void advancePhase() {
        advancePhaseState();
    }

    public PhaseAdvanceResult advancePhaseState() {
        if (battleOver) {
            return snapshot(null);
        }

        if (currentPhase == Phase.FIGHT) {
            activePlayer = getInactivePlayer();
            currentPhase = Phase.COMMAND;

            if (activePlayer == Player.ATTACKER) {
                currentRound++;
            }

            clearBattleShockForCommandPhase(getArmy(activePlayer));
            resetForNewTurn();
            addCp(activePlayer, 1);
            return snapshot(activePlayer);
        }

        currentPhase = currentPhase.next();

        if (currentPhase == Phase.SHOOTING) {
            resetForNewShootingPhase(getArmy(activePlayer));
        }

        if (currentPhase == Phase.FIGHT) {
            resetForNewFightPhase();
        }

        return snapshot(null);
    }

    public void nextRound() {
        currentRound++;
        currentPhase = Phase.COMMAND;
        activePlayer = Player.ATTACKER;
        clearBattleShockForCommandPhase(getArmy(activePlayer));
        resetForNewTurn();
    }

    public int getCurrentCp(Player player) {
        ArmyInstance army = getArmy(player);
        return army == null ? 0 : army.getCurrentCp();
    }

    public void setCurrentCp(Player player, int cp) {
        ArmyInstance army = getArmy(player);
        if (army != null) {
            army.setCurrentCp(cp);
        }
    }

    public void addCp(Player player, int amount) {
        ArmyInstance army = getArmy(player);
        if (army != null && amount != 0) {
            army.addCp(amount);
        }
    }

    public boolean spendCp(Player player, int amount) {
        ArmyInstance army = getArmy(player);
        return army != null && army.spendCp(amount);
    }

    public int getCurrentVp(Player player) {
        ArmyInstance army = getArmy(player);
        return army == null ? 0 : army.getCurrentVp();
    }

    public void setCurrentVp(Player player, int amount) {
        ArmyInstance army = getArmy(player);
        if (army != null) {
            army.setCurrentVp(amount);
        }
    }

    public void addVp(Player player, int amount) {
        ArmyInstance army = getArmy(player);
        if (army != null && amount > 0) {
            army.addVp(amount);
        }
    }

    public boolean hasExceededMaxRounds() {
        return currentRound > maxRounds;
    }

    public BattleState deepCopy() {
        BattleState copy = new BattleState(missionName, maxRounds);
        copy.setCurrentRound(currentRound);
        copy.setCurrentPhase(currentPhase);
        copy.setActivePlayer(activePlayer);
        copy.setBattleOver(battleOver);
        copy.setAttackerArmy(copyArmy(attackerArmy));
        copy.setDefenderArmy(copyArmy(defenderArmy));
        return copy;
    }

    private PhaseAdvanceResult snapshot(Player commandPointRecipient) {
        return new PhaseAdvanceResult(currentRound, currentPhase, activePlayer, commandPointRecipient);
    }

    private void resetForNewTurn() {
        for (UnitInstance unit : allUnits()) {
            if (unit != null) {
                unit.resetForNewTurn();
            }
        }
    }

    private void resetForNewShootingPhase(ArmyInstance army) {
        if (army == null) {
            return;
        }

        for (UnitInstance unit : army.getUnits()) {
            if (unit != null) {
                unit.resetForNewShootingPhase();
            }
        }
    }

    private void resetForNewFightPhase() {
        for (UnitInstance unit : allUnits()) {
            if (unit != null) {
                unit.resetForNewFightPhase();
            }
        }
    }

    private void clearBattleShockForCommandPhase(ArmyInstance army) {
        if (army == null) {
            return;
        }

        for (UnitInstance unit : army.getUnits()) {
            if (unit != null) {
                unit.setBattleShocked(false);
            }
        }
    }

    private List<UnitInstance> allUnits() {
        List<UnitInstance> result = new ArrayList<>();
        if (attackerArmy != null) {
            result.addAll(attackerArmy.getUnits());
        }
        if (defenderArmy != null) {
            result.addAll(defenderArmy.getUnits());
        }
        return result;
    }

    private static ArmyInstance copyArmy(ArmyInstance source) {
        if (source == null) {
            return null;
        }

        ArmyInstance copy = new ArmyInstance(
                source.getArmyId(),
                source.getArmyName(),
                source.getFactionId(),
                source.getFactionName(),
                source.getDetachmentId()
        );

        copy.setSecondaryMissionName(source.getSecondaryMissionName());
        copy.setCurrentCp(source.getCurrentCp());
        copy.setCurrentVp(source.getCurrentVp());

        for (UnitInstance unit : source.getUnits()) {
            copy.addUnit(copyUnit(unit));
        }

        for (StratagemInstance strategy : source.getStrategies()) {
            copy.addStrategy(copyStrategy(strategy));
        }

        return copy;
    }

    private static UnitInstance copyUnit(UnitInstance source) {
        UnitInstance copy = new UnitInstance(
                source.getDatasheetId(),
                source.getUnitName()
        );
        copy.setFactionId(source.getFactionId());
        copy.setFactionName(source.getFactionName());
        copy.setDetachmentId(source.getDetachmentId());
        copy.setDetachmentName(source.getDetachmentName());
        copy.setEnhancementId(source.getEnhancementId());
        copy.setEnhancementName(source.getEnhancementName());

        copy.setBattleShocked(source.isBattleShocked());
        copy.setEligibleToFightThisPhase(source.isEligibleToFightThisPhase());
        copy.setFoughtThisPhase(source.hasFoughtThisPhase());
        copy.setChargedThisTurn(source.hasChargedThisTurn());
        copy.setWasChargedThisTurn(source.hasBeenChargedThisTurn());

        for (String keyword : source.getKeywords()) {
            copy.addKeyword(keyword);
        }

        for (String abilityName : source.getFactionAbilityNames()) {
            copy.addFactionAbilityName(abilityName);
        }

        for (String abilityName : source.getDetachmentAbilityNames()) {
            copy.addDetachmentAbilityName(abilityName);
        }

        for (UnitAbilityProfile ability : source.getAbilities()) {
            copy.addAbility(ability);
        }

        for (var entry : source.getUsedRangedWeaponCountsThisPhase().entrySet()) {
            copy.markRangedWeaponUsedThisPhaseByKey(entry.getKey(), entry.getValue());
        }

        for (var entry : source.getUsedOneShotWeaponCountsThisBattle().entrySet()) {
            copy.markOneShotWeaponUsedByKey(entry.getKey(), entry.getValue());
        }

        for (String weaponKey : source.getRemovedWeaponKeysForDestroyedModels()) {
            copy.markRemovedWeaponKey(weaponKey);
        }

        for (UnitModelInstance model : source.getModels()) {
            copy.addModel(copyModel(model));
        }

        for (WeaponProfile weapon : source.getRangedWeapons()) {
            copy.addRangedWeapon(copyWeapon(weapon));
        }

        for (WeaponProfile weapon : source.getMeleeWeapons()) {
            copy.addMeleeWeapon(copyWeapon(weapon));
        }

        return copy;
    }

    private static UnitModelInstance copyModel(UnitModelInstance source) {
        UnitModelInstance copy = new UnitModelInstance(
                source.getModelName(),
                source.getM(),
                source.getT(),
                source.getSv(),
                source.getW(),
                source.getLd(),
                source.getOc(),
                source.getInv()
        );

        copy.setCurrentHp(source.getCurrentHp());
        return copy;
    }

    private static WeaponProfile copyWeapon(WeaponProfile source) {
        return new WeaponProfile(
                source.weaponID(),
                source.name(),
                source.description(),
                source.count(),
                source.range(),
                source.a(),
                source.skill(),
                source.s(),
                source.ap(),
                source.d(),
                source.melee()
        );
    }

    private static StratagemInstance copyStrategy(StratagemInstance source) {
        return new StratagemInstance(
                source.name(),
                source.cpCost(),
                source.turn(),
                source.phase(),
                source.descriptionHtml()
        );
    }
}
