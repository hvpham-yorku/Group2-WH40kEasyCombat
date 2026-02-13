package eecs2311.group2.wh40k_easycombat.model.instance;

import eecs2311.group2.wh40k_easycombat.model.Units;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class UnitInstance {
    private final String instanceId;
    private final Units template;

    private int currentWounds;
    private int currentOC;

    private List<Integer> meleeWeaponIds;
    private List<Integer> rangedWeaponIds;

    private boolean isBattleShocked = false;
    private boolean hasMoved = false;
    private boolean hasAttacked = false;

    public UnitInstance(Units template) {
        this.template = template;
        this.instanceId = UUID.randomUUID().toString();
        this.currentWounds = template.W();
        this.currentOC = template.OC();
        this.meleeWeaponIds = new ArrayList<>(template.meleeWeaponIdList());
        this.rangedWeaponIds = new ArrayList<>(template.rangedWeaponIdList());
    }

    public boolean isBelowHalfHealth() {
        return this.currentWounds <= (template.W() / 2);
    }

    public void takeDamage(int amount) {
        this.currentWounds = Math.max(0, this.currentWounds - amount);
    }

    public void setBattleShocked(boolean battleShocked) {
        this.isBattleShocked = battleShocked;
        this.currentOC = battleShocked ? 0 : template.OC();
    }

    public Units getTemplate() {
        return template;
    }

    public String getInstanceId() {
        return instanceId;
    }

    public int getCurrentWounds() {
        return currentWounds;
    }

    public int getCurrentOC() {
        return currentOC;
    }

    public boolean isBattleShocked() {
        return isBattleShocked;
    }

    public List<Integer> getMeleeWeaponIds() {
        return meleeWeaponIds;
    }

    public List<Integer> getRangedWeaponIds() {
        return rangedWeaponIds;
    }

    public int getM() {
        return template.M();
    }    // Movement

    public int getT() {
        return template.T();
    }    // Toughness

    public int getSV() {
        return template.SV();
    }  // Save

    public int getW() {
        return template.W();
    }    // Wounds (Max)

    public int getLD() {
        return template.LD();
    }  // Leadership

    public int getOC() {
        return template.OC();
    }  // Objective Control (Base)

    public int getInvulnerableSave() {
        return template.invulnerableSave();
    }

    public int getCategory() {
        return template.category();
    }

    public String getComposition() {
        return template.composition();
    }

    public List<Integer> getKeywords() {
        return template.keywordIdList();
    }

    public UnitInstance copy() {
        UnitInstance copy = new UnitInstance(this.template);
        copy.currentWounds = this.currentWounds;
        copy.currentOC = this.currentOC;
        copy.isBattleShocked = this.isBattleShocked;
        copy.meleeWeaponIds = new ArrayList<>(this.meleeWeaponIds);
        copy.rangedWeaponIds = new ArrayList<>(this.rangedWeaponIds);
        // Important: Keep the same instanceId so UI knows it's the same unit
        return copy;
    }
}
