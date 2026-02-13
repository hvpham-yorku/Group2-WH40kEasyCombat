import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.io.FileNotFoundException;

public class CsvRead {

    public List<String[]> readCSV(String filename) {
        List<String[]> data = new ArrayList<>();

        try (Scanner scanner = new Scanner(new File(filename))) {
            if (scanner.hasNextLine()) {
                scanner.nextLine();
            }
            while (scanner.hasNextLine()) {
                String line = scanner.nextLine();
                line = line.replace("\uFEFF", "");
                String[] inputs = line.split(",");
                data.add(inputs);
            }
        } catch (FileNotFoundException e) {
            System.out.println("The file " + filename + " is not found");
        }
        return data;
    }
}