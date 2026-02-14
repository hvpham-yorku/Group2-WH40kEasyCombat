module wh40k_easycomba {
    requires javafx.controls;
    requires javafx.fxml;
    requires transitive javafx.graphics;
    requires java.sql;
    requires org.xerial.sqlitejdbc;

    opens eecs2311.group2.wh40k_easycombat.controller to javafx.fxml;
    exports eecs2311.group2.wh40k_easycombat;
}
