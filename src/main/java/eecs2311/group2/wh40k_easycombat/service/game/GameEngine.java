package eecs2311.group2.wh40k_easycombat.service.game;

import eecs2311.group2.wh40k_easycombat.model.instance.Phase;
import eecs2311.group2.wh40k_easycombat.model.instance.Player;
import eecs2311.group2.wh40k_easycombat.model.instance.UnitInstance;
import eecs2311.group2.wh40k_easycombat.model.instance.UnitModelInstance;
import eecs2311.group2.wh40k_easycombat.model.instance.WeaponProfile;

public class GameEngine {
    CombatService stateManager;
    AutoBattler autoBattler;
    
    int mainMissionValue;
    int attackerSecondaryMissionValue;
    int defenderSecondaryMissionValue;
    int currentTurn;
    
    //Actions like battle, using strategems and completing missions assumes that the current active player is the one that did the action

    //When you click start game
    public void start(){
      stateManager = new CombatService();
      currentTurn = 1;
    }

    //Players will get prompted to select a main mission, while each player will be able to select their own side objective
    public void selectMainMission(String missionName, int pointValue){
      stateManager.getCurrentState().setMissionName(missionName);
      this.mainMissionValue = pointValue;
    }

    public void selectAttackerMission(String missionName, int pointValue){
      stateManager.getCurrentState().getAttackerArmy().setSecondaryMissionName(missionName);
      this.attackerSecondaryMissionValue = pointValue;
    }

    public void selectDefenderMission(String missionName, int pointValue){
      stateManager.getCurrentState().getDefenderArmy().setSecondaryMissionName(missionName);
      this.defenderSecondaryMissionValue = pointValue;
    }
    
    //The player can do a select amount of actions in their individual phase then end their turn
    public void endPlayerTurn(){
      stateManager.switchActivePlayer();
      if (currentTurn % 2 == 0){
        stateManager.advancePhase();
        stateManager.addCp(stateManager.getActivePlayer(), 1);
        stateManager.addCp(stateManager.getInactivePlayer(), 1);
      }
      currentTurn++;
    }
    
    //Input the id of the appropriate entities
    //Will output false if the current phase does not allow that move (Using a melee in another other phase except fight)
    public boolean performBattle(String defenderId, String attackerId, String weaponName){
        WeaponProfile weapon = getWeaponProfile(stateManager.getActivePlayer(), attackerId, weaponName);
        UnitModelInstance defender = getUnitModelInstance(stateManager.getActivePlayer(), defenderId);
        
        if (stateManager.getCurrentState().getCurrentPhase() == Phase.SHOOTING && (!weapon.melee()) 
             || stateManager.getCurrentState().getCurrentPhase() == Phase.FIGHT && (weapon.melee())){
                autoBattler.simulateAttack(defender, weapon);
                return true;
        }
        
        return false;
    }

    public void useStrategem(String name){
        int cpCost = Integer.parseInt(stateManager.getActiveArmy().getStrategemInstanceByName(name).cpCost());
        stateManager.spendCp(stateManager.getActivePlayer(), cpCost);
    }
    
    //Assumes the current active player was the one who who completed it 
    public void completedMission(){
      stateManager.addVp(stateManager.getActivePlayer(), mainMissionValue);
    }

    public void completedSecondaryMission(){
      int missionValue = stateManager.getActivePlayer() == Player.ATTACKER ? attackerSecondaryMissionValue : defenderSecondaryMissionValue;
      stateManager.addVp(stateManager.getActivePlayer(), missionValue);
      stateManager.getActiveArmy().setSecondaryMissionName("");
    }

    //Search Helper Methods
    
    private UnitInstance getUnitInstance(Player player, String id){
      return stateManager.getCurrentState().getArmy(player).getUnitInstanceById(id);
    }

    private UnitModelInstance getUnitModelInstance(Player player, String id){
      return getUnitInstance(player, id).getModels().getFirst();
    }

    private WeaponProfile getWeaponProfile(Player player, String unitId, String weaponName){
      return getUnitInstance(player, unitId).getWeaponProfileByName(weaponName);
    }
  
}