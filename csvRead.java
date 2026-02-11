import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.io.FileNotFoundException;

public class csvRead {

    public List<String[]> readCSV(String filename) {
        List<String[]> data = new ArrayList<>();

        try {
            Scanner scanner = new Scanner(new File("/Users/sank/Desktop/units.csv"));
            while (scanner.hasNextLine()) {
                String line = scanner.nextLine();
                String[] inputs = line.split(",");
                data.add(inputs);
            }
        } catch (FileNotFoundException e) {
            System.out.println("The file " + filename + " is not found");
        }
        return data;
    }
}