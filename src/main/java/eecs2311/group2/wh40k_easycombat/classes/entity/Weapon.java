package eecs2311.group2.wh40k_easycombat.classes.entity;
import java.util.ArrayList;

public abstract class Weapon {

    private int id;
    private String name;
    private String attacks;
    private int strength;
    private int armorPenetration;
    private String damage;

    private ArrayList<Integer> keywordsID;
    private ArrayList<String> keywords;

    public Weapon() {
      this.keywordsID = new ArrayList<>();
      this.keywords = new ArrayList<>();
    }

    public abstract int getSkill(); 
    //Used to get skill which functions the same for both ranged and melee just named differently

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAttacks() {
        return attacks;
    }

    public void setAttacks(String attacks) {
        this.attacks = attacks;
    }

    public int getStrength() {
        return strength;
    }

    public void setStrength(int strength) {
        this.strength = strength;
    }

    public int getArmorPenetration() {
        return armorPenetration;
    }

    public void setArmorPenetration(int armorPenetration) {
        this.armorPenetration = armorPenetration;
    }

    public String getDamage() {
        return damage;
    }

    public void setDamage(String damage) {
        this.damage = damage;
    }

    public ArrayList<Integer> getKeywordsID() {
        return keywordsID;
    }

    public ArrayList<String> getKeywords() {
        return keywords;
    }

    public void addKeywordID(int keywordId) {
        keywordsID.add(keywordId);
    }

    public void addKeyword(String keyword) {
        keywords.add(keyword);
    }
}
