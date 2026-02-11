package eecs2311.group2.wh40k_easycombat.classes.entity;

public class MeleeWeapon extends Weapon{

  private int weaponSkill;
 
  public MeleeWeapon() {
    super();
  }

  public int getSkill(){
    return weaponSkill;
  }

  public int getWeaponSkill() {
      return weaponSkill;
  }

  public void setWeaponSkill(int weaponSkill) {
      this.weaponSkill = weaponSkill;
  }
}
