package eecs2311.group2.wh40k_easycombat.util.populator;

import eecs2311.group2.wh40k_easycombat.db.Dao;
import eecs2311.group2.wh40k_easycombat.db.Tx;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.*;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.*;

public class DataPopulator {

    public static void main(String[] args) {
        CsvRead reader = new CsvRead();
        Path csvDir = Path.of("src/main/resources/csv");
        Map<String, eecs2311.group2.wh40k_easycombat.util.populator.CsvHandler> handlers = new HashMap<>();
        handlers.put("Units.csv", (conn, rows) -> insertUnits(conn, rows));
        handlers.put("Factions.csv", (conn, rows) -> insertFactions(conn, rows));

        try {
            Tx.run(conn -> {
                try {
                    Dao.update(conn, "DELETE FROM units");
                } catch (SQLException e) {
                    throw new RuntimeException(e);
                }
                try {
                    Dao.update(conn, "DELETE FROM factions");
                } catch (SQLException e) {
                    throw new RuntimeException(e);
                }

                try (var files = Files.list(csvDir)) {
                    files.filter(p -> p.toString().toLowerCase().endsWith(".csv"))
                            .sorted()
                            .forEach(p -> {
                                String filename = p.getFileName().toString();

                                CsvHandler handler = handlers.get(filename);
                                if (handler == null) {
                                    System.out.println("Skipping (no handler): " + filename);
                                    return;
                                }

                                try (InputStream in = Files.newInputStream(p)) {
                                    List<String[]> rows = reader.readCSV(in);
                                    handler.handle(conn, rows);
                                    System.out.println("Loaded: " + filename);
                                } catch (Exception e) {
                                    throw new RuntimeException("Failed loading " + filename, e);
                                }
                            });
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
                return null;
            });
            System.out.println("All data inserted successfully!");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void insertUnits(Connection conn, List<String[]> rows) throws Exception {
        for (String[] row : rows) {
            try {
                int categoryId = Integer.parseInt(row[0]);
                String name = row[1];
                String faction = row[2];
                String type = row[3];
                int cost = Integer.parseInt(row[4]);

                Dao.update(conn,
                        "INSERT OR IGNORE INTO Unit (CategoryID, UnitName, UnitFaction, UnitType, UnitCost) VALUES (?, ?, ?, ?, ?)",
                        categoryId, name, faction, type, cost
                );
            } catch (Exception e) {
                System.out.println("Invalid Units row: " + String.join("|", row));
            }
        }
    }

    private static void insertFactions(Connection conn, List<String[]> rows) throws Exception {
        for (String[] row : rows) {
            try {
                String id = row[0];
                String name = row[1];
                String link = row[2];

                Dao.update(conn,
                        "INSERT OR IGNORE INTO Faction (FactionID, FactionName, FactionLink) VALUES (?, ?, ?)",
                        id, name, link
                );
            } catch (Exception e) {
                System.out.println("Invalid Factions row: " + String.join("|", row));
            }
        }
    }
}