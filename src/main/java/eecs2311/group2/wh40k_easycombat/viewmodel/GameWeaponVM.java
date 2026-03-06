package eecs2311.group2.wh40k_easycombat.viewmodel;

public class GameWeaponVM {

    private final String name;
    private final int count;
    private final String range;
    private final String a;
    private final String skill;
    private final String s;
    private final String ap;
    private final String d;
    private final boolean melee;

    public GameWeaponVM(String name, int count, String range, String a, String skill,
                        String s, String ap, String d, boolean melee) {
        this.name = name == null ? "" : name;
        this.count = Math.max(1, count);
        this.range = range == null ? "" : range;
        this.a = a == null ? "" : a;
        this.skill = skill == null ? "" : skill;
        this.s = s == null ? "" : s;
        this.ap = ap == null ? "" : ap;
        this.d = d == null ? "" : d;
        this.melee = melee;
    }

    public String getName() {
        return name;
    }

    public int getCount() {
        return count;
    }

    public String getRange() {
        return range;
    }

    public String getA() {
        return a;
    }

    public String getSkill() {
        return skill;
    }

    public String getS() {
        return s;
    }

    public String getAp() {
        return ap;
    }

    public String getD() {
        return d;
    }

    public boolean isMelee() {
        return melee;
    }
}