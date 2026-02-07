module wh40k_easycomba {
    requires javafx.controls;
    requires javafx.fxml;
    requires transitive javafx.graphics;
    requires java.sql;

    opens eecs2311.group2.wh40k_easycombat to javafx.fxml;
    exports eecs2311.group2.wh40k_easycombat;
}
