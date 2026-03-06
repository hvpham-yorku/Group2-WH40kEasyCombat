package eecs2311.group2.wh40k_easycombat.viewmodel;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

public class GameSubUnitVM {

    private final StringProperty name = new SimpleStringProperty("");
    private final StringProperty m = new SimpleStringProperty("");
    private final StringProperty t = new SimpleStringProperty("");
    private final StringProperty sv = new SimpleStringProperty("");
    private final StringProperty w = new SimpleStringProperty("");
    private final StringProperty ld = new SimpleStringProperty("");
    private final StringProperty oc = new SimpleStringProperty("");
    private final StringProperty inv = new SimpleStringProperty("");
    private final StringProperty hp = new SimpleStringProperty("");

    public GameSubUnitVM(String name, String m, String t, String sv, String w,
                         String ld, String oc, String inv, String hp) {
        this.name.set(name == null ? "" : name);
        this.m.set(m == null ? "" : m);
        this.t.set(t == null ? "" : t);
        this.sv.set(sv == null ? "" : sv);
        this.w.set(w == null ? "" : w);
        this.ld.set(ld == null ? "" : ld);
        this.oc.set(oc == null ? "" : oc);
        this.inv.set(inv == null ? "" : inv);
        this.hp.set(hp == null ? "" : hp);
    }

    public StringProperty nameProperty() { return name; }
    public StringProperty mProperty() { return m; }
    public StringProperty tProperty() { return t; }
    public StringProperty svProperty() { return sv; }
    public StringProperty wProperty() { return w; }
    public StringProperty ldProperty() { return ld; }
    public StringProperty ocProperty() { return oc; }
    public StringProperty invProperty() { return inv; }
    public StringProperty hpProperty() { return hp; }

    public String getName() { return name.get(); }
    public String getM() { return m.get(); }
    public String getT() { return t.get(); }
    public String getSv() { return sv.get(); }
    public String getW() { return w.get(); }
    public String getLd() { return ld.get(); }
    public String getOc() { return oc.get(); }
    public String getInv() { return inv.get(); }
    public String getHp() { return hp.get(); }
}