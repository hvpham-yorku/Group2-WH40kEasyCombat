package eecs2311.group2.wh40k_easycombat.classes.entity;

public class RangedWeapon extends Weapon{

  private int range;
  private int ballisticSkill;

  public RangedWeapon() {
    super();
  }

  public int getSkill(){
    return ballisticSkill;
  }

  public int getRange() {
    return range;
  }

  public void setRange(int range) {
    this.range = range;
  }

  public int getBallisticSkill() {
    return ballisticSkill;
  }

  public void setBallisticSkill(int ballisticSkill) {
    this.ballisticSkill = ballisticSkill;
  }
}