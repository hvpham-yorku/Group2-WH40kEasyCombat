module wh40k_easycombat {
    requires javafx.controls;
    requires javafx.fxml;
    requires transitive javafx.graphics;

    opens eecs2311.group2.wh40k_easycombat to javafx.fxml;
    exports eecs2311.group2.wh40k_easycombat;
}
