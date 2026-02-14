import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

public class ruleSorter {

    public void sortByName(ArrayList<Rules> rules) {
        Collections.sort(rules, new Comparator<Rules>() {
            @Override
            public int compare(Rules o1, Rules o2) {
                return o1.getName().compareToIgnoreCase(o2.getName());
            }
        });
    }
}