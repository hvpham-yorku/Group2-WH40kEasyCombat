package eecs2311.group2.wh40k_easycombat.util;

import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;

public final class GameHtmlTextBuilder {

    private static final double DEFAULT_FONT_SIZE = 14.0;

    private GameHtmlTextBuilder() {
    }

    public static void setHtmlLikeText(TextFlow flow, String html) {
        setHtmlLikeText(flow, html, DEFAULT_FONT_SIZE);
    }

    public static void setHtmlLikeText(TextFlow flow, String html, double fontSize) {
        if (flow == null) return;

        flow.getChildren().clear();
        if (html == null || html.isBlank()) return;

        String working = html
                .replace("<br><br>", "\n\n")
                .replace("<br/>", "\n")
                .replace("<br />", "\n")
                .replace("<br>", "\n");

        String[] lines = working.split("\n", -1);

        for (int i = 0; i < lines.length; i++) {
            addFormattedLine(flow, lines[i], fontSize);
            if (i < lines.length - 1) {
                flow.getChildren().add(buildText("\n", false, fontSize));
            }
        }
    }

    private static void addFormattedLine(TextFlow flow, String line, double fontSize) {
        if (line == null) return;

        String working = line;

        while (!working.isEmpty()) {
            int bStart = working.toLowerCase().indexOf("<b>");
            if (bStart < 0) {
                String plain = htmlToPlainText(working);
                if (!plain.isEmpty()) {
                    flow.getChildren().add(buildText(plain, false, fontSize));
                }
                break;
            }

            if (bStart > 0) {
                String plainBefore = htmlToPlainText(working.substring(0, bStart));
                if (!plainBefore.isEmpty()) {
                    flow.getChildren().add(buildText(plainBefore, false, fontSize));
                }
            }

            int bEnd = working.toLowerCase().indexOf("</b>", bStart);
            if (bEnd < 0) {
                String plain = htmlToPlainText(working);
                if (!plain.isEmpty()) {
                    flow.getChildren().add(buildText(plain, false, fontSize));
                }
                break;
            }

            String boldContent = working.substring(bStart + 3, bEnd);
            String cleanedBold = htmlToPlainText(boldContent);

            if (!cleanedBold.isEmpty()) {
                flow.getChildren().add(buildText(cleanedBold, true, fontSize));
            }

            working = working.substring(bEnd + 4);
        }
    }

    private static Text buildText(String value, boolean bold, double fontSize) {
        Text text = new Text(value);
        String style = (bold ? "-fx-font-weight: bold; " : "")
                + "-fx-font-size: " + fontSize + "px;";
        text.setStyle(style);
        return text;
    }

    private static String htmlToPlainText(String html) {
        if (html == null || html.isBlank()) return "";

        String s = html;
        s = s.replace("&nbsp;", " ");
        s = s.replace("&lt;", "<");
        s = s.replace("&gt;", ">");
        s = s.replace("&amp;", "&");
        s = s.replace("&quot;", "\"");
        s = s.replace("&#39;", "'");
        s = s.replaceAll("(?is)<(?!/?b\\b)[^>]+>", "");
        return s.trim();
    }
}
