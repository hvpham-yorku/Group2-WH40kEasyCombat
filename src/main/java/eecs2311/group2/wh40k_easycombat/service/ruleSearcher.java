package eecs2311.group2.wh40k_easycombat.service;

import java.util.ArrayList;
import java.util.List;

public class ruleSearcher {

    private final List<String> blocks;

    public ruleSearcher(String jsonPath) throws Exception {
        WHRoot root = ruleFile.loadJson(jsonPath);
        this.blocks = buildBlocks(root);
    }

    public List<String> searchAll(String query, int maxResults) {
        List<String> results = new ArrayList<>();
        if (query == null) return results;

        String q = query.trim().toLowerCase();
        if (q.isEmpty()) return results;

        for (String block : blocks) {
            if (block.toLowerCase().contains(q)) {
                results.add(block);
                if (results.size() >= maxResults) break;
            }
        }
        return results;
    }

    private static List<String> buildBlocks(WHRoot root) {
        List<String> out = new ArrayList<>();
        if (root == null || root.pages == null) return out;

        for (WHPage page : root.pages) {
            if (page == null || page.content == null) continue;

            String currentTitle = null;
            StringBuilder currentBody = new StringBuilder();

            for (WHContent c : page.content) {
                if (c == null || c.text == null) continue;
                if (!"paragraph".equalsIgnoreCase(c.type)) continue;

                String text = c.text.trim();
                if (text.isEmpty()) continue;

                if (looksLikeTitle(text)) {
                    flush(out, page.page_id, currentTitle, currentBody);
                    currentTitle = text;
                    currentBody = new StringBuilder();
                } else {
                    if (currentBody.length() > 0) currentBody.append("\n\n");
                    currentBody.append(text);
                }
            }
            flush(out, page.page_id, currentTitle, currentBody);
        }
        return out;
    }

    private static void flush(List<String> out, int pageId, String title, StringBuilder body) {
        String t = (title == null) ? "" : title.trim();
        String b = (body == null) ? "" : body.toString().trim();

        if (!t.isBlank() && !b.isBlank()) {
            out.add(formatBlock(pageId, t, b));
        }
    }

    private static String formatBlock(int pageId, String title, String body) {
        return "Page: " + pageId + "\n" +
                "Title: " + title + "\n\n" +
                body;
    }

    private static boolean looksLikeTitle(String text) {
        if (text.length() < 4) return false;
        if (text.length() > 80) return false;

        int letters = 0;
        int upper = 0;

        for (char ch : text.toCharArray()) {
            if (Character.isLetter(ch)) {
                letters++;
                if (Character.isUpperCase(ch)) upper++;
            }
        }

        if (letters == 0) return false;

        double upperRatio = (double) upper / letters;
        return upperRatio > 0.80;
    }
}