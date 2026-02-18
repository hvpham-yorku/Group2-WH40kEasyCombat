module wh40k_easycomba {
    requires javafx.controls;
    requires javafx.fxml;
    requires transitive javafx.graphics;
    requires java.sql;
    requires org.xerial.sqlitejdbc;
    requires com.fasterxml.jackson.databind;

    opens eecs2311.group2.wh40k_easycombat.controller to javafx.fxml;
    opens eecs2311.group2.wh40k_easycombat.service to com.fasterxml.jackson.databind;

    exports eecs2311.group2.wh40k_easycombat;
}