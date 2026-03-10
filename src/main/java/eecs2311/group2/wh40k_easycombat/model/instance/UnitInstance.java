package eecs2311.group2.wh40k_easycombat.model.instance;

import eecs2311.group2.wh40k_easycombat.viewmodel.GameWeaponVM;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class UnitInstance {
    private final String instanceId;
    private final String datasheetId;
    private final String modelName;

    // base stats
    private final String m;
    private final String t;
    private final String sv;
    private final String w;
    private final String ld;
    private final String oc;
    private final String inv;

    // runtime state
    private int currentHp;
    private int currentOc;
    private boolean battleShocked;
    private boolean destroyed;

    private final List<GameWeaponVM> rangedWeapons = new ArrayList<>();
    private final List<GameWeaponVM> meleeWeapons = new ArrayList<>();

    public UnitInstance(
            String datasheetId,
            String modelName,
            String m,
            String t,
            String sv,
            String w,
            String ld,
            String oc,
            String inv
    ) {
        this.instanceId = UUID.randomUUID().toString();
        this.datasheetId = datasheetId == null ? "" : datasheetId;
        this.modelName = modelName == null ? "" : modelName;

        this.m = m == null ? "" : m;
        this.t = t == null ? "" : t;
        this.sv = sv == null ? "" : sv;
        this.w = w == null ? "" : w;
        this.ld = ld == null ? "" : ld;
        this.oc = oc == null ? "" : oc;
        this.inv = inv == null ? "" : inv;

        this.currentHp = parseIntSafe(this.w);
        this.currentOc = parseIntSafe(this.oc);
        this.battleShocked = false;
        this.destroyed = false;
    }

    public String getInstanceId() {
        return instanceId;
    }

    public String getDatasheetId() {
        return datasheetId;
    }

    public String getModelName() {
        return modelName;
    }

    public String getM() {
        return m;
    }

    public String getT() {
        return t;
    }

    public String getSv() {
        return sv;
    }

    public String getW() {
        return w;
    }

    public String getLd() {
        return ld;
    }

    public String getOc() {
        return oc;
    }

    public String getInv() {
        return inv;
    }

    public int getCurrentHp() {
        return currentHp;
    }

    public int getCurrentOc() {
        return currentOc;
    }

    public boolean isBattleShocked() {
        return battleShocked;
    }

    public boolean isDestroyed() {
        return destroyed;
    }

    public List<GameWeaponVM> getRangedWeapons() {
        return rangedWeapons;
    }

    public List<GameWeaponVM> getMeleeWeapons() {
        return meleeWeapons;
    }

    public void setCurrentHp(int currentHp) {
        this.currentHp = Math.max(0, currentHp);
        this.destroyed = this.currentHp <= 0;
    }

    public void heal(int amount) {
        if (amount <= 0 || destroyed) return;
        int max = parseIntSafe(w);
        this.currentHp = Math.min(max, this.currentHp + amount);
    }

    public void takeDamage(int amount) {
        if (amount <= 0 || destroyed) return;
        this.currentHp = Math.max(0, this.currentHp - amount);
        this.destroyed = this.currentHp <= 0;
    }

    public boolean isBelowHalfHealth() {
        int max = parseIntSafe(w);
        return max > 0 && currentHp <= (max / 2);
    }

    public void setBattleShocked(boolean battleShocked) {
        this.battleShocked = battleShocked;
        this.currentOc = battleShocked ? 0 : parseIntSafe(oc);
    }

    public void addRangedWeapon(GameWeaponVM weapon) {
        if (weapon != null) {
            rangedWeapons.add(weapon);
        }
    }

    public void addMeleeWeapon(GameWeaponVM weapon) {
        if (weapon != null) {
            meleeWeapons.add(weapon);
        }
    }

    private int parseIntSafe(String text) {
        if (text == null || text.isBlank()) {
            return 0;
        }

        String cleaned = text.replaceAll("[^0-9-]", "").trim();
        if (cleaned.isBlank()) {
            return 0;
        }

        try {
            return Integer.parseInt(cleaned);
        } catch (Exception e) {
            return 0;
        }
    }
}