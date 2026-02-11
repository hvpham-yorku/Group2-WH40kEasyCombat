package eecs2311.group2.wh40k_easycombat.classes.battle;
import eecs2311.group2.wh40k_easycombat.classes.entity.*;
import eecs2311.group2.wh40k_easycombat.classes.utility.*;


public class BattleSimulator {
  
  public static BattleResult simulateAttack(Unit defender, Weapon selectedWeapon, Unit attacker){
    int attacks = 0;

    try {
      attacks = Integer.parseInt(selectedWeapon.getAttacks());
    }
    catch(NumberFormatException e){
      attacks = DiceUtility.convertStringIntoRoll(selectedWeapon.getAttacks());
    }
    
    int hits = 0;

    for (int i = 0; i < attacks; i++){
      int roll = DiceUtility.rollD6();
      if (roll >= selectedWeapon.getSkill()){
        hits++;
      }
    }
    
    int wounds = 0;
    for (int i = 0; i < hits; i++){
      int roll = DiceUtility.rollD6();
      if (roll >= calculateWoundThreshold(defender.getToughness(), selectedWeapon.getStrength())){
        wounds++;
      }
    }

    int unsaved = 0;
    for (int i = 0; i < wounds; i++) {
      int Roll = DiceUtility.rollD6();
      int saveTarget = defender.getSave();
      if (selectedWeapon.getArmorPenetration() < 0) {
          saveTarget -= selectedWeapon.getArmorPenetration();
      }
      if (Roll < saveTarget) {
          unsaved++;
      }
    }

    int damage = 0;
    try {
      damage = Integer.parseInt(selectedWeapon.getDamage());
    }
    catch(NumberFormatException e){
      attacks = DiceUtility.convertStringIntoRoll(selectedWeapon.getDamage());
    }
    
    int totalDamage = unsaved * damage;

    return new BattleResult(attacks, hits, wounds, unsaved, totalDamage, defender, attacker);
  }

  private static int calculateWoundThreshold(int targetToughness, int weaponStrength) {
    if (weaponStrength >= targetToughness * 2) return 2;
    if (weaponStrength > targetToughness) return 3;
    if (weaponStrength == targetToughness) return 4;
    if (weaponStrength * 2 <= targetToughness) return 6;
    return 5;
  }

}
