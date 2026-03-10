package eecs2311.group2.wh40k_easycombat.model.instance;

import java.util.UUID;

public class UnitModelInstance {
    private final String instanceId;
    private final String modelName;

    private final String m;
    private final String t;
    private final String sv;
    private final String w;
    private final String ld;
    private final String oc;
    private final String inv;

    private int currentHp;
    private boolean destroyed;

    public UnitModelInstance(
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
        this.modelName = modelName == null ? "" : modelName;

        this.m = m == null ? "" : m;
        this.t = t == null ? "" : t;
        this.sv = sv == null ? "" : sv;
        this.w = w == null ? "" : w;
        this.ld = ld == null ? "" : ld;
        this.oc = oc == null ? "" : oc;
        this.inv = inv == null ? "" : inv;

        this.currentHp = getMaxHp();
        this.destroyed = this.currentHp <= 0;
    }

    public String getInstanceId() {
        return instanceId;
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

    public int getMaxHp() {
        return parseIntSafe(w);
    }

    public int getBaseOc() {
        return parseIntSafe(oc);
    }

    public boolean isDestroyed() {
        return destroyed;
    }

    public void setCurrentHp(int currentHp) {
        int maxHp = getMaxHp();
        int clamped = Math.max(0, currentHp);

        if (maxHp > 0) {
            clamped = Math.min(maxHp, clamped);
        }

        this.currentHp = clamped;
        this.destroyed = this.currentHp <= 0;
    }

    public void heal(int amount) {
        if (amount <= 0 || destroyed) return;

        int maxHp = getMaxHp();
        if (maxHp <= 0) return;

        currentHp = Math.min(maxHp, currentHp + amount);
    }

    public void takeDamage(int amount) {
        if (amount <= 0 || destroyed) return;

        currentHp = Math.max(0, currentHp - amount);
        destroyed = currentHp <= 0;
    }

    public boolean isBelowHalfHealth() {
        int maxHp = getMaxHp();
        return maxHp > 0 && currentHp <= (maxHp / 2);
    }

    private static int parseIntSafe(String text) {
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