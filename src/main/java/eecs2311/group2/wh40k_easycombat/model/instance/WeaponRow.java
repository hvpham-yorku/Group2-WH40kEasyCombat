package eecs2311.group2.wh40k_easycombat.model.instance;

import javafx.beans.property.SimpleStringProperty;

import static eecs2311.group2.wh40k_easycombat.util.FxReflectionHelper.getAny;
import static eecs2311.group2.wh40k_easycombat.util.FxReflectionHelper.s;

public final class WeaponRow {

    private final SimpleStringProperty name = new SimpleStringProperty("");
    private final SimpleStringProperty description = new SimpleStringProperty("");
    private final SimpleStringProperty range = new SimpleStringProperty("");
    private final SimpleStringProperty a = new SimpleStringProperty("");
    private final SimpleStringProperty skill = new SimpleStringProperty("");
    private final SimpleStringProperty ap = new SimpleStringProperty("");
    private final SimpleStringProperty d = new SimpleStringProperty("");
    private final boolean melee;

    private WeaponRow(boolean melee) {
        this.melee = melee;
    }

    public static WeaponRow fromWargear(Object w) {
        if (w == null) return null;

        String name = s(getAny(w, "wargear", "weapon", "name"));
        String desc = s(getAny(w, "description", "desc", "text", "line_text", "special_rules"));

        String range = s(getAny(w, "range", "weapon_range"));
        String attacks = s(getAny(w, "a", "attacks", "A"));
        String bs = s(getAny(w, "bs", "BS", "BS_WS"));
        String ws = s(getAny(w, "ws", "WS", "BS_WS"));
        String ap = s(getAny(w, "ap", "AP"));
        String dmg = s(getAny(w, "d", "damage", "D"));

        String type = s(getAny(w, "type", "category", "weapon_type", "profile_type")).toLowerCase();

        boolean isMelee = type.contains("melee")
                || range.toLowerCase().contains("melee")
                || (range.isBlank() && !ws.isBlank());

        WeaponRow row = new WeaponRow(isMelee);

        row.name.set(name);
        row.description.set(desc);
        row.range.set(isMelee ? "Melee" : range);
        row.a.set(attacks);
        row.skill.set(isMelee ? ws : bs);
        row.ap.set(ap);
        row.d.set(dmg);

        return row;
    }

    public SimpleStringProperty nameProperty() {
        return name;
    }

    public SimpleStringProperty descriptionProperty() {
        return description;
    }

    public SimpleStringProperty rangeProperty() {
        return range;
    }

    public SimpleStringProperty aProperty() {
        return a;
    }

    public SimpleStringProperty skillProperty() {
        return skill;
    }

    public SimpleStringProperty apProperty() {
        return ap;
    }

    public SimpleStringProperty dProperty() {
        return d;
    }

    public boolean isMelee() {
        return melee;
    }
}