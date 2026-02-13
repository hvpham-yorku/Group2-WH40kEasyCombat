import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class CsvRead {

    public List<String[]> readCSV(InputStream inputStream) {
        List<String[]> data = new ArrayList<>();

        try (Scanner scanner = new Scanner(inputStream)) {
            if (scanner.hasNextLine()) {
                scanner.nextLine();
            }
            while (scanner.hasNextLine()) {
                String line = scanner.nextLine();
                line = line.replace("\uFEFF", "");
                String[] inputs = line.split(",");
                for (int i = 0; i < inputs.length; i++) {
                    inputs[i] = inputs[i].trim();
                }
                data.add(inputs);
            }
        }
        return data;
    }
}