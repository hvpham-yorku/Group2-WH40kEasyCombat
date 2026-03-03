package eecs2311.group2.wh40k_easycombat.util;

import eecs2311.group2.wh40k_easycombat.db.Database;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.sql.*;
import java.util.*;

/**
 * Wahapedia export CSV -> SQLite importer.
 *
 * CSV format (based on your Factions.csv):
 * - UTF-8 (may contain BOM)
 * - delimiter: '|'
 * - each line ends with a trailing '|' (so there is an extra empty column at the end)
 * - header row provides column names
 *
 * Seed-like behavior:
 * - Only import into a table if the table is empty, unless forceReimport=true.
 *
 * Conventions:
 * - table name = CSV filename without ".csv"
 * - columns = header tokens (after trimming trailing empty header)
 * - empty field -> "" (empty string)
 * - literal "NULL" -> SQL NULL
 * - true/false -> 1/0
 */
public final class CsvToSqliteImporter {

    private static final String DEFAULT_CSV_FOLDER = "src/main/resources/csv";
    private static final char DELIMITER = '|';

    private CsvToSqliteImporter() {}

    /** One-liner: import from DEFAULT_CSV_FOLDER, seed-like. */
    public static void importDefaultCsvSeedLike(boolean forceReimport) throws IOException, SQLException {
        importCsvFolderSeedLike(DEFAULT_CSV_FOLDER, forceReimport);
    }

    /** Like Database.executeSqlFolder: provide a folder path. */
    public static void importCsvFolderSeedLike(String folderPath, boolean forceReimport) throws IOException, SQLException {
        Path folder = Path.of(folderPath);
        if (!Files.exists(folder) || !Files.isDirectory(folder)) {
            throw new IllegalArgumentException("CSV folder not found: " + folderPath);
        }

        List<Path> csvFiles;
        try (var paths = Files.list(folder)) {
            csvFiles = paths
                    .filter(p -> p.getFileName().toString().toLowerCase().endsWith(".csv"))
                    .sorted((a, b) -> a.getFileName().toString().compareToIgnoreCase(b.getFileName().toString()))
                    .toList();
        }

        try (Connection conn = Database.getConnection()) {
            conn.setAutoCommit(false);

            for (Path csv : csvFiles) {
                String table = stripExt(csv.getFileName().toString());

                if (!tableExists(conn, table)) {
                    System.out.println("[SKIP] Table not found in DB: " + table + " (from " + csv.getFileName() + ")");
                    continue;
                }

                if (!forceReimport && !isTableEmpty(conn, table)) {
                    System.out.println("[SKIP] " + table + ": already has data, not importing " + csv.getFileName());
                    continue;
                }

                List<List<String>> rows = readPipeDelimitedCsv(csv);
                if (rows.isEmpty()) {
                    System.out.println("[SKIP] " + table + ": empty csv " + csv.getFileName());
                    continue;
                }

                List<String> headers = normalizeHeaders(rows.get(0));
                headers = trimTrailingEmptyHeaders(headers);

                if (headers.isEmpty()) {
                    System.out.println("[SKIP] " + table + ": missing headers in " + csv.getFileName());
                    continue;
                }

                if (forceReimport) {
                    try (Statement st = conn.createStatement()) {
                        st.execute("DELETE FROM \"" + table + "\";");
                    }
                }

                String insertSql = buildInsertSql(table, headers);

                int inserted = 0;
                try (PreparedStatement ps = conn.prepareStatement(insertSql)) {
                    for (int r = 1; r < rows.size(); r++) {
                        List<String> row = rows.get(r);
                        if (isAllEmpty(row)) continue;

                        bindRow(ps, headers.size(), row); // trims extra trailing empty col automatically
                        ps.addBatch();

                        if (++inserted % 2000 == 0) ps.executeBatch();
                    }
                    ps.executeBatch();
                }

                conn.commit();
                System.out.println("[OK] " + table + ": imported " + inserted + " rows from " + csv.getFileName());
            }

            conn.commit();
        }
    }

    // -------------------- SQL --------------------

    private static final Set<String> OR_IGNORE_TABLES = Set.of(
            "Datasheets_keywords",
            "Datasheets_abilities",
            "Datasheets_stratagems",
            "Datasheets_enhancements",
            "Datasheets_detachment_abilities",
            "Detachment_abilities",
            "Datasheets_leader",
            "Datasheets_wargear"
    );

    private static String buildInsertSql(String table, List<String> headers) {

        boolean insertOrIgnore = OR_IGNORE_TABLES.contains(table);

        StringBuilder sb = new StringBuilder();
        sb.append(insertOrIgnore ? "INSERT OR IGNORE INTO \"" : "INSERT INTO \"")
          .append(table)
          .append("\" (");

        for (int i = 0; i < headers.size(); i++) {
            if (i > 0) sb.append(", ");
            sb.append("\"").append(headers.get(i)).append("\"");
        }

        sb.append(") VALUES (");

        for (int i = 0; i < headers.size(); i++) {
            if (i > 0) sb.append(", ");
            sb.append("?");
        }

        sb.append(");");
        return sb.toString();
    }

    private static void bindRow(PreparedStatement ps, int colCount, List<String> row) throws SQLException {
        for (int i = 0; i < colCount; i++) {
            String raw = (i < row.size()) ? row.get(i) : "";
            bindValue(ps, i + 1, raw);
        }
    }

    private static void bindValue(PreparedStatement ps, int idx, String raw) throws SQLException {
        if (raw == null) {
            ps.setString(idx, "");
            return;
        }

        String s = stripBom(raw);
        String t = s.trim();

        if (t.isEmpty()) {
            ps.setString(idx, "");
            return;
        }

        if ("NULL".equalsIgnoreCase(t)) {
            ps.setNull(idx, Types.NULL);
            return;
        }

        if ("true".equalsIgnoreCase(t)) {
            ps.setInt(idx, 1);
            return;
        }
        if ("false".equalsIgnoreCase(t)) {
            ps.setInt(idx, 0);
            return;
        }

        ps.setString(idx, s);
    }

    private static boolean tableExists(Connection conn, String table) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement(
                "SELECT name FROM sqlite_master WHERE type='table' AND name=?")) {
            ps.setString(1, table);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }

    private static boolean isTableEmpty(Connection conn, String table) throws SQLException {
        try (Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery("SELECT 1 FROM \"" + table + "\" LIMIT 1;")) {
            return !rs.next();
        }
    }

    // -------------------- CSV parsing (pipe-delimited, quoted fields, multiline) --------------------

    private static List<List<String>> readPipeDelimitedCsv(Path file) throws IOException {
        List<List<String>> out = new ArrayList<>();
        try (BufferedReader br = Files.newBufferedReader(file, StandardCharsets.UTF_8)) {
            String line;
            StringBuilder pending = null;
            boolean inQuotes = false;

            while ((line = br.readLine()) != null) {
                if (pending == null) pending = new StringBuilder();
                if (pending.length() > 0) pending.append("\n");
                pending.append(line);

                int quotes = countQuotes(line);
                if (quotes % 2 != 0) inQuotes = !inQuotes;

                if (!inQuotes) {
                    out.add(parseDelimitedRecord(pending.toString(), DELIMITER));
                    pending = null;
                }
            }

            if (pending != null && pending.length() > 0) {
                out.add(parseDelimitedRecord(pending.toString(), DELIMITER));
            }
        }
        return out;
    }

    /** Supports quoted fields and escaped quotes (""). */
    private static List<String> parseDelimitedRecord(String record, char delimiter) {
        List<String> fields = new ArrayList<>();
        StringBuilder cur = new StringBuilder();

        boolean quoted = false;
        for (int i = 0; i < record.length(); i++) {
            char c = record.charAt(i);

            if (quoted) {
                if (c == '"') {
                    if (i + 1 < record.length() && record.charAt(i + 1) == '"') {
                        cur.append('"');
                        i++;
                    } else {
                        quoted = false;
                    }
                } else {
                    cur.append(c);
                }
            } else {
                if (c == delimiter) {
                    fields.add(cur.toString());
                    cur.setLength(0);
                } else if (c == '"') {
                    quoted = true;
                } else {
                    cur.append(c);
                }
            }
        }
        fields.add(cur.toString());
        return fields;
    }

    private static int countQuotes(String s) {
        int cnt = 0;
        for (int i = 0; i < s.length(); i++) if (s.charAt(i) == '"') cnt++;
        return cnt;
    }

    // -------------------- utils --------------------

    private static String stripExt(String filename) {
        int dot = filename.lastIndexOf('.');
        return dot >= 0 ? filename.substring(0, dot) : filename;
    }

    private static boolean isAllEmpty(List<String> row) {
        for (String s : row) {
            if (s != null && !s.trim().isEmpty()) return false;
        }
        return true;
    }

    private static List<String> normalizeHeaders(List<String> rawHeaders) {
        List<String> headers = new ArrayList<>(rawHeaders.size());
        for (String h : rawHeaders) {
            if (h == null) h = "";
            headers.add(stripBom(h.trim()));
        }
        return headers;
    }

    private static List<String> trimTrailingEmptyHeaders(List<String> headers) {
        int end = headers.size();
        while (end > 0 && headers.get(end - 1).isEmpty()) end--;
        return headers.subList(0, end);
    }

    private static String stripBom(String s) {
        if (s != null && !s.isEmpty() && s.charAt(0) == '\uFEFF') {
            return s.substring(1);
        }
        return s;
    }
}