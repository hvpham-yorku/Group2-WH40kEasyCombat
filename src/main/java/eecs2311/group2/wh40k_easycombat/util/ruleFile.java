import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class ruleFile {

    public static final ArrayList<Rules> readFile(String fileName) {
        List<String> list = new ArrayList<>();
        ArrayList<Rules> rules = new ArrayList<>();

        try (Scanner scanner = new Scanner(new File(fileName))) {
            while (scanner.hasNextLine()) {
                String line = scanner.nextLine();
                list.add(line);
            }
        } catch (FileNotFoundException e) {
            System.out.println("The file " + fileName + " is not found");
        }

        for (String line : list) {
            Rules rule = new Rules(line, "", "", "", new ArrayList<>(), 0, "");
            rules.add(rule);
        }
        return rules;
    }

    public static void main(String[] args) {
        ArrayList<Rules> rules = readFile("/Users/sank/Desktop/WarHammer 40k PDF Rules.json");
    }
}
