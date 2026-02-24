package eecs2311.group2.wh40k_easycombat.ruleservice;

import java.util.List;
import java.util.Scanner;

public class RuleMain {
    public static void main(String[] args) throws Exception {
        String path = "WarHammer40kRules.json";
        ruleSearcher searcher = new ruleSearcher(path);
        Scanner scanner = new Scanner(System.in);
        System.out.println("WH40k Rule Search (type 'exit' to quit)\n");

        while (true) {
            System.out.print("Search for: ");
            String a = scanner.nextLine();
            if (a == null)
                continue;

            a = a.trim();
            if (a.equalsIgnoreCase("exit"))
                break;
            if (a.isEmpty())
                continue;

            List<String> matches = searcher.searchAll(a, 100);

            if (matches.isEmpty()) {
                System.out.println("No matches found.\n");
            } else {
                for (int i = 0; i < matches.size(); i++) {
                    System.out.println("Match " + (i + 1) + ":\n");
                    System.out.println(matches.get(i));
                    System.out.println("\n------------------------------");
                }
            }
        }
        scanner.close();
    }
}