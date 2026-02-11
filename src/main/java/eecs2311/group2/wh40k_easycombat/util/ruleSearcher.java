import java.util.ArrayList;


public class ruleSearcher {
    public Rules searchByName(ArrayList<Rules> rules, String name) {
        for (Rules rule : rules) {
            if (name.equals(rule.getName())) {
                System.out.println("Rule is found!");
                return rule;
            }
        }
        return null;
    }
}