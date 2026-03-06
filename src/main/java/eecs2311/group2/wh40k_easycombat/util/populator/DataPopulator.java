package eecs2311.group2.wh40k_easycombat.util.populator;

import eecs2311.group2.wh40k_easycombat.db.Dao;
import eecs2311.group2.wh40k_easycombat.db.Tx;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DataPopulator {
    public static void main(String[] args) {
        CsvRead reader = new CsvRead();
        Path csvDir = Path.of("src/main/resources/csv");
        Map<String, CsvHandler> handlers = new HashMap<>();
        handlers.put("Datasheets.csv", DataPopulator::insertDatasheets);
        handlers.put("Factions.csv", DataPopulator::insertFactions);

        try {
            Tx.run(conn -> {
                try {
                    Dao.update(conn, "DELETE FROM Datasheets");
                    Dao.update(conn, "DELETE FROM Factions");
                } catch (SQLException e) {
                    throw new RuntimeException(e);
                }

                try (var files = Files.list(csvDir)) {
                    files.filter(p -> p.toString().toLowerCase().endsWith(".csv")).sorted().forEach(p -> {String filename = p.getFileName().toString();
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

    private static void insertDatasheets(Connection conn, List<String[]> rows) throws Exception {
        for (String[] row : rows) {
            try {
                String id = row[0];
                String name = row[1];
                String factionId = row[2];
                String sourceId = row[3];
                String legend = row[4];
                String role = row[5];
                String loadout = row[6];
                String transport = row[7];
                String virtual = row[8];
                String leaderHead = row[9];
                String leaderFooter = row[10];
                String damagedW = row[11];
                String damagedDescription = row[12];
                String link = row[13];

                Dao.update(conn,
                        "INSERT OR IGNORE INTO Datasheets " +
                                "(id, name, faction_id, source_id, legend, role, loadout, transport, virtual, leader_head, leader_footer, damaged_w, damaged_description, link) " +
                                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)",
                        id, name, factionId, sourceId, legend, role, loadout, transport,
                        virtual, leaderHead, leaderFooter, damagedW, damagedDescription, link
                );
            } catch (Exception e) {
                System.out.println("Invalid Datasheets row: " + String.join("|", row));
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
                        "INSERT OR IGNORE INTO Factions (id, name, link) VALUES (?, ?, ?)",
                        id, name, link
                );
            } catch (Exception e) {
                System.out.println("Invalid Factions row: " + String.join("|", row));
            }
        }
    }
}