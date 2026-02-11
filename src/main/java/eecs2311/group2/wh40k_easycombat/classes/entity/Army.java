package eecs2311.group2.wh40k_easycombat.classes.entity;
import java.util.ArrayList;

public class Army {

  private int id;
  private int factionID;
  private String detachment;
  private boolean isFavorite;
  private int unitNumber;

  private ArrayList<String> equippedWeapon;

  public Army() {
    this.equippedWeapon = new ArrayList<>();
  }

  public int getId() {
    return id;
  }

  public void setId(int id) {
    this.id = id;
  }

  public int getFactionID() {
    return factionID;
  }

  public void setFactionID(int factionID) {
    this.factionID = factionID;
  }

  public String getDetachment() {
    return detachment;
  }

  public void setDetachment(String detachment) {
    this.detachment = detachment;
  }

  public boolean isFavorite() {
    return isFavorite;
  }

  public void setFavorite(boolean favorite) {
    isFavorite = favorite;
  }

  public void toggleFavourite() {
    this.isFavorite = !isFavorite;
  }

  public int getUnitNumber() {
    return unitNumber;
  }

  public void setUnitNumber(int unitNumber) {
    this.unitNumber = unitNumber;
  }

  public ArrayList<String> getEquippedWeapon() {
    return equippedWeapon;
  }

  public void addEquippedWeapon(String weapon) {
    equippedWeapon.add(weapon);
  }
}
