package eecs2311.group2.wh40k_easycombat.manager;

import javafx.scene.control.Label;

public final class RoundManager {

    private RoundManager() {
    }

    public static void initialize(Label roundLabel, Label blueCPLabel, Label redCPLabel) {
        if (roundLabel != null && isBlank(roundLabel.getText())) {
            roundLabel.setText("1");
        }

        if (blueCPLabel != null && isBlank(blueCPLabel.getText())) {
            blueCPLabel.setText("0");
        }

        if (redCPLabel != null && isBlank(redCPLabel.getText())) {
            redCPLabel.setText("0");
        }
    }

    public static void nextRound(Label roundLabel, Label blueCPLabel, Label redCPLabel) {
        addRound(roundLabel, 1);
        addCp(blueCPLabel, 1);
        addCp(redCPLabel, 1);
    }

    public static void addBlueCp(Label blueCPLabel, int delta) {
        addCp(blueCPLabel, delta);
    }

    public static void addRedCp(Label redCPLabel, int delta) {
        addCp(redCPLabel, delta);
    }

    private static void addCp(Label label, int delta) {
        int current = parseLabelInt(label);
        int next = current + delta;

        if (next < 0) {
            next = 0;
        }

        setLabelInt(label, next);
    }

    private static void addRound(Label label, int delta) {
        int current = parseLabelInt(label);
        int next = current + delta;

        if (next < 1) {
            next = 1;
        }

        setLabelInt(label, next);
    }

    private static int parseLabelInt(Label label) {
        if (label == null || isBlank(label.getText())) {
            return 0;
        }

        try {
            return Integer.parseInt(label.getText().trim());
        } catch (Exception e) {
            return 0;
        }
    }

    private static void setLabelInt(Label label, int value) {
        if (label != null) {
            label.setText(String.valueOf(value));
        }
    }

    private static boolean isBlank(String text) {
        return text == null || text.isBlank();
    }
}