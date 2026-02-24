package eecs2311.group2.wh40k_easycombat.ruleservice;

import java.util.ArrayList;
import java.util.List;

public class ruleSearcher {
    private final List<String> rules;

    public ruleSearcher(String jsonPath) throws Exception {
        WHRoot root = ruleFile.loadJson(jsonPath);
        this.rules = buildRules(root);
    }

    public List<String> searchAll(String query, int maxResults) {
        List<String> results = new ArrayList<>();
        if (query == null)
            return results;

        String q = query.trim().toLowerCase();
        if (q.isEmpty())
            return results;

        for (String ruleBody : rules) {
            if (ruleBody.toLowerCase().contains(q)) {
                results.add(ruleBody);
                if (results.size() >= maxResults)
                    break;
            }
        }
        return results;
    }

    private static List<String> buildRules(WHRoot root) {
        List<String> out = new ArrayList<>();
        if (root == null || root.pages == null)
            return out;

        for (WHPage page : root.pages) {
            if (page == null || page.content == null)
                continue;

            StringBuilder body = new StringBuilder();
            boolean seenTitle = false;

            for (WHContent c : page.content) {
                if (c == null || c.text == null)
                    continue;
                if (!"paragraph".equalsIgnoreCase(c.type))
                    continue;

                String text = c.text.trim();
                if (text.isEmpty())
                    continue;

                if (looksLikeTitle(text)) {
                    flush(out, body, seenTitle);
                    body = new StringBuilder();
                    seenTitle = true;
                } else {
                    if (body.length() > 0) body.append("\n\n");
                    body.append(text);
                }
            }
            flush(out, body, seenTitle);
        }
        return out;
    }

    private static void flush(List<String> out, StringBuilder body, boolean seenTitle) {
        String b = (body == null) ? "" : body.toString().trim();

        if (seenTitle && !b.isBlank()) {
            out.add(b);
        }
    }

    private static boolean looksLikeTitle(String text) {
        if (text.length() < 4)
            return false;
        if (text.length() > 80)
            return false;

        int letters = 0;
        int upper = 0;

        for (char ch : text.toCharArray()) {
            if (Character.isLetter(ch)) {
                letters++;
                if (Character.isUpperCase(ch)) upper++;
            }
        }
        if (letters == 0)
            return false;
        return ((double) upper / letters) > 0.80;
    }
}