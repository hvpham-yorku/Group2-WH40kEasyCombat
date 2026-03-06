package eecs2311.group2.wh40k_easycombat.util.populator;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;

public class CsvRead {

    public List<String[]> readCSV(InputStream inputStream) {
        List<String[]> data = new ArrayList<>();

        try (BufferedReader br = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8))) {
            String line;

            br.readLine();

            while ((line = br.readLine()) != null) {
                line = line.replace("\uFEFF", "");
                String[] cols = line.split("\\|", -1);
                for (int i = 0; i < cols.length; i++) cols[i] = cols[i].trim();
                data.add(cols);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return data;
    }
}