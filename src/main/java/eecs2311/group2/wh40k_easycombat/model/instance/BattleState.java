package eecs2311.group2.wh40k_easycombat.model.instance;

public class BattleState {
    private String missionName;
    private int currentRound;
    private Phase currentPhase;
    private Player activePlayer;

    private ArmyInstance attackerArmy;
    private ArmyInstance defenderArmy;

    public BattleState() {
        reset("");
    }

    public BattleState(String missionName) {
        reset(missionName);
    }

    public void reset(String missionName) {
        this.missionName = missionName == null ? "" : missionName;
        this.currentRound = 1;
        this.currentPhase = Phase.COMMAND;
        this.activePlayer = Player.ATTACKER;
        this.attackerArmy = null;
        this.defenderArmy = null;
    }

    public String getMissionName() {
        return missionName;
    }

    public void setMissionName(String missionName) {
        this.missionName = missionName == null ? "" : missionName;
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

    public void setActivePlayer(Player activePlayer) {
        this.activePlayer = activePlayer == null ? Player.ATTACKER : activePlayer;
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
        return getArmy(activePlayer == Player.ATTACKER ? Player.DEFENDER : Player.ATTACKER);
    }

    public void switchActivePlayer() {
        activePlayer = (activePlayer == Player.ATTACKER) ? Player.DEFENDER : Player.ATTACKER;
    }

    public void advancePhase() {
        if (currentPhase == Phase.FIGHT) {
            Player previousPlayer = activePlayer;
            currentPhase = Phase.COMMAND;
            switchActivePlayer();

            if (previousPlayer == Player.DEFENDER && activePlayer == Player.ATTACKER) {
                currentRound++;
            }
            return;
        }

        currentPhase = currentPhase.next();
    }

    public void nextRound() {
        currentRound++;
        currentPhase = Phase.COMMAND;
        activePlayer = Player.ATTACKER;
    }

    public void addCp(Player player, int amount) {
        ArmyInstance army = getArmy(player);
        if (army != null && amount != 0) {
            army.addCp(amount);
        }
    }

    public boolean spendCp(Player player, int amount) {
        if (amount <= 0) {
            return true;
        }

        ArmyInstance army = getArmy(player);
        if (army == null || army.getCurrentCp() < amount) {
            return false;
        }

        army.spendCp(amount);
        return true;
    }

    public void addVp(Player player, int amount) {
        ArmyInstance army = getArmy(player);
        if (army != null && amount > 0) {
            army.addVp(amount);
        }
    }

    public BattleState deepCopy() {
        BattleState copy = new BattleState(missionName);
        copy.setCurrentRound(currentRound);
        copy.setCurrentPhase(currentPhase);
        copy.setActivePlayer(activePlayer);
        copy.setAttackerArmy(copyArmy(attackerArmy));
        copy.setDefenderArmy(copyArmy(defenderArmy));
        return copy;
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

        copy.setBattleShocked(source.isBattleShocked());

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
