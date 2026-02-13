import eecs2311.group2.wh40k_easycombat.db.Dao;
import eecs2311.group2.wh40k_easycombat.db.Tx;
import java.io.InputStream;
import java.sql.SQLException;
import java.util.List;

public class DataPopulator {

    public static void main(String[] args) {
        CsvRead read = new CsvRead();
        InputStream csvStream = DataPopulator.class.getResourceAsStream("/units.csv");

        if (csvStream == null) {
            System.out.println("units.csv not found in resources!");
            return;
        }

        List<String[]> data = read.readCSV(csvStream);

        try {
            Tx.run(conn -> {
                try {
                    Dao.update(conn, "DELETE FROM Unit", new Object[]{});
                } catch (SQLException e) {
                    throw new RuntimeException(e);
                }

                for (String[] row : data) {
                    try {
                        int categoryId = Integer.parseInt(row[0]);
                        String name = row[1];
                        String faction = row[2];
                        String type = row[3];
                        int cost = Integer.parseInt(row[4]);
                        Dao.update(
                                conn,
                                "INSERT INTO Unit (CategoryID, UnitName, UnitFaction, UnitType, UnitCost) VALUES (?, ?, ?, ?, ?)",
                                categoryId, name, faction, type, cost
                        );
                    } catch (Exception e) {
                        System.out.println("Invalid row: " + String.join(",", row));
                        e.printStackTrace();
                    }
                }
                return null;
            });
            System.out.println("Data inserted successfully!");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}