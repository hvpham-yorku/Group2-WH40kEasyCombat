package eecs2311.group2.wh40k_easycombat.service.game;

import eecs2311.group2.wh40k_easycombat.model.instance.UnitModelInstance;
import eecs2311.group2.wh40k_easycombat.model.instance.WeaponProfile;

public class AutoBattler {
    public void simulateAttack(UnitModelInstance defender, WeaponProfile selectedWeapon){
        DiceService dice = new DiceService();

        int attacks = 0;
        int skill = 0;
        int strength = 0;
        int toughness = 0;
        int save = 0;
        int armorPenetration = 0;
        int damage = 0;

        try {
          attacks = convertStat(selectedWeapon.a(), dice);
          skill = convertStat(selectedWeapon.skill(), dice);
          strength = convertStat(selectedWeapon.s(), dice);
          toughness = convertStat(defender.getT(), dice);
          save = convertStat(defender.getSv(), dice);
          armorPenetration = convertStat(selectedWeapon.ap(), dice);  
          damage = convertStat(selectedWeapon.d(), dice);          
        
        } catch (IllegalArgumentException e) {
          System.err.println("One or more of the stats provided not proper format");
        }
            
        dice.rollDice(attacks);

        int skillTarget = skill;
        
        int hits = (int)dice.getResults()
            .stream()
            .filter(n -> n >= skillTarget)
            .count();

        dice.rollDice(hits);

        int woundsTarget = calculateWoundThreshold(toughness, strength);
        
        int wounds = (int)dice.getResults()
        .stream()
        .filter(n -> n >= woundsTarget)
        .count();
                
        int saveTarget = save - armorPenetration;

        dice.rollDice(wounds);

        int unsaved = (int)dice.getResults()
        .stream()
        .filter(n -> n < saveTarget)
        .count();
        
        int totalDamage = unsaved * damage;
        
        defender.takeDamage(totalDamage);
    }

  //Helper methods

  private int convertStat(String stat, DiceService dice) throws IllegalArgumentException{
    try {
      return Integer.parseInt(stat);
    }
    catch(NumberFormatException e){
      return dice.convertStringIntoRoll(stat);
    }
  }

  private static int calculateWoundThreshold(int targetToughness, int weaponStrength) {
    if (weaponStrength >= targetToughness * 2) return 2;
    if (weaponStrength > targetToughness) return 3;
    if (weaponStrength == targetToughness) return 4;
    if (weaponStrength * 2 <= targetToughness) return 6;
    return 5;
  }
}