package eecs2311.group2.wh40k_easycombat.classes.battle;
import eecs2311.group2.wh40k_easycombat.classes.entity.*;


public class BattleResult{
  private int attacks;
  private int hits;
  private int wounds;
  private int unsaved;
  private int totalDamage;
  private Unit defender;
  private Unit attacker;

  public BattleResult(int attacks, int hits, int wounds, int unsaved, int totalDamage, Unit defender, Unit attacker) {
    this.attacks = attacks;
    this.hits = hits;
    this.wounds = wounds;
    this.unsaved = unsaved;
    this.totalDamage = totalDamage;
    this.defender = defender;
    this.attacker = attacker;
  }

  public int getAttacks() { return attacks; }
  public int getHits() { return hits; }
  public int getWounds() { return wounds; }
  public int getUnsaved() { return unsaved; }
  public int getTotalDamage() { return totalDamage; }
  public Unit getDefender() { return defender; }
  public Unit getAttacker() { return attacker; }

  public String toString(){
    return ""; //Insert proper string like attack did x attacks, x succeed, x wound were applied, and only x was saved causing a total of x damage to defender
  }
}