package eecs2311.group2.wh40k_easycombat.util;

import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;

public final class GameHtmlTextBuilder {

    private GameHtmlTextBuilder() {
    }

    public static void setHtmlLikeText(TextFlow flow, String html) {
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
            addFormattedLine(flow, lines[i]);
            if (i < lines.length - 1) {
                flow.getChildren().add(new Text("\n"));
            }
        }
    }

    private static void addFormattedLine(TextFlow flow, String line) {
        if (line == null) return;

        String working = line;

        while (!working.isEmpty()) {
            int bStart = working.toLowerCase().indexOf("<b>");
            if (bStart < 0) {
                String plain = htmlToPlainText(working);
                if (!plain.isEmpty()) {
                    Text text = new Text(plain);
                    text.setStyle("-fx-font-size: 12px;");
                    flow.getChildren().add(text);
                }
                break;
            }

            if (bStart > 0) {
                String plainBefore = htmlToPlainText(working.substring(0, bStart));
                if (!plainBefore.isEmpty()) {
                    Text text = new Text(plainBefore);
                    text.setStyle("-fx-font-size: 12px;");
                    flow.getChildren().add(text);
                }
            }

            int bEnd = working.toLowerCase().indexOf("</b>", bStart);
            if (bEnd < 0) {
                String plain = htmlToPlainText(working);
                if (!plain.isEmpty()) {
                    Text text = new Text(plain);
                    text.setStyle("-fx-font-size: 12px;");
                    flow.getChildren().add(text);
                }
                break;
            }

            String boldContent = working.substring(bStart + 3, bEnd);
            String cleanedBold = htmlToPlainText(boldContent);

            if (!cleanedBold.isEmpty()) {
                Text boldText = new Text(cleanedBold);
                boldText.setStyle("-fx-font-weight: bold; -fx-font-size: 12px;");
                flow.getChildren().add(boldText);
            }

            working = working.substring(bEnd + 4);
        }
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