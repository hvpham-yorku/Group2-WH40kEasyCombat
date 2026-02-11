package eecs2311.group2.wh40k_easycombat.classes.entity;
import java.util.ArrayList;

public class Unit {

  private int id;
  private String unitName;
  private String faction;
  private int points;

  private int movement;
  private int toughness;
  private int save;
  private int wounds;
  private int leadership;
  private int objectiveControl;

  private int invulnerableSave;
  private String unitComposition;
  private boolean isFavorite;
  private int category;

  private ArrayList<Integer> keywordsID;
  private ArrayList<String> keywords;
  private ArrayList<Integer> equippedRangedWeaponsID;
  private ArrayList<Integer> equippedMeleeWeaponsID;

  private ArrayList<Integer> coreAbilitiesID;
  private ArrayList<Integer> mainAbilitiesID;
  private int factionAbilityID;

  public Unit() {
    this.keywordsID = new ArrayList<>();
    this.keywords = new ArrayList<>();
    this.equippedRangedWeaponsID = new ArrayList<>();
    this.equippedMeleeWeaponsID = new ArrayList<>();
    this.coreAbilitiesID = new ArrayList<>();
    this.mainAbilitiesID = new ArrayList<>();
  }

  public int getId() {
    return id;
  }

  public void setId(int id) {
    this.id = id;
  }

  public String getUnitName() {
    return unitName;
  }

  public void setUnitName(String unitName) {
    this.unitName = unitName;
  }

  public String getFaction() {
    return faction;
  }

  public void setFaction(String faction) {
    this.faction = faction;
  }

  public int getPoints() {
    return points;
  }

  public void setPoints(int points) {
    this.points = points;
  }

  public int getMovement() {
    return movement;
  }

  public void setMovement(int movement) {
    this.movement = movement;
  }

  public int getToughness() {
    return toughness;
  }

  public void setToughness(int toughness) {
    this.toughness = toughness;
  }

  public int getSave() {
    return save;
  }

  public void setSave(int save) {
    this.save = save;
  }

  public int getWounds() {
    return wounds;
  }

  public void setWounds(int wounds) {
    this.wounds = wounds;
  }

  public int getLeadership() {
    return leadership;
  }

  public void setLeadership(int leadership) {
    this.leadership = leadership;
  }

  public int getObjectiveControl() {
    return objectiveControl;
  }

  public void setObjectiveControl(int objectiveControl) {
    this.objectiveControl = objectiveControl;
  }

  public int getInvulnerableSave() {
    return invulnerableSave;
  }

  public void setInvulnerableSave(int invulnerableSave) {
    this.invulnerableSave = invulnerableSave;
  }

  public String getUnitComposition() {
    return unitComposition;
  }

  public void setUnitComposition(String unitComposition) {
    this.unitComposition = unitComposition;
  }

  public boolean isFavorite() {
    return isFavorite;
  }

  public void setFavorite(boolean isFavorite) {
    this.isFavorite = isFavorite;
  }

  public void toggleFavourite() {
    this.isFavorite = !isFavorite;
  }

  public int getCategory() {
    return category;
  }

  public void setCategory(int category) {
    this.category = category;
  }

  public int getFactionAbilityID() {
    return factionAbilityID;
  }

  public void setFactionAbilityID(int factionAbilityId) {
    this.factionAbilityID = factionAbilityId;
  }

  public ArrayList<Integer> getKeywordsID() {
    return keywordsID;
  }

  public ArrayList<String> getKeywords() {
    return keywords;
  }

  public ArrayList<Integer> getEquippedRangedWeaponsID() {
    return equippedRangedWeaponsID;
  }

  public ArrayList<Integer> getEquippedMeleeWeaponsID() {
    return equippedMeleeWeaponsID;
  }

  public ArrayList<Integer> getCoreAbilitiesID() {
    return coreAbilitiesID;
  }

  public ArrayList<Integer> getMainAbilitiesID() {
    return mainAbilitiesID;
  }

  public void addKeywordID(int keywordId) {
    keywordsID.add(keywordId);
  }

  public void addKeyword(String keyword) {
    keywords.add(keyword);
  }

  public void addRangedWeapon(int weaponId) {
    equippedRangedWeaponsID.add(weaponId);
  }

  public void addMeleeWeapon(int weaponId) {
    equippedMeleeWeaponsID.add(weaponId);
  }

  public void addCoreAbilityID(int ability) {
    coreAbilitiesID.add(ability);
  }

  public void addMainAbilityID(int ability) {
    mainAbilitiesID.add(ability);
  }
}
