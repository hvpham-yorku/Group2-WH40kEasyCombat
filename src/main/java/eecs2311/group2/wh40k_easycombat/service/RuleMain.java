package eecs2311.group2.wh40k_easycombat.service;

import java.util.List;
import java.util.Scanner;

public class RuleMain {
    public static void main(String[] args) throws Exception {
        String path = "/Users/sank/Desktop/WarHammer 40k PDF Rules.json";
        ruleSearcher searcher = new ruleSearcher(path);

        Scanner scanner = new Scanner(System.in);
        System.out.println("WH40k Rule Search (type 'exit' to quit)\n");

        while (true) {
            System.out.print("Search for: ");
            String q = scanner.nextLine();
            if (q == null) continue;

            q = q.trim();
            if (q.equalsIgnoreCase("exit")) break;
            if (q.isEmpty()) continue;

            List<String> matches = searcher.searchAll(q, 3);

            if (matches.isEmpty()) {
                System.out.println("No matches found.\n");
            } else {
                for (int i = 0; i < matches.size(); i++) {
                    System.out.println("Match " + (i + 1) + ":\n");
                    System.out.println(matches.get(i));
                    System.out.println("\n------------------------------\n");
                }
            }
        }
        scanner.close();
    }
}