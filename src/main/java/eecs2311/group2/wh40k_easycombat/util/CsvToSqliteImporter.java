package eecs2311.group2.wh40k_easycombat.util;

import eecs2311.group2.wh40k_easycombat.db.Database;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.sql.*;
import java.util.*;

/**
 * Wahapedia export CSV -> SQLite importer.
 *
 * Requirements:
 * - delimiter: '|'
 * - UTF-8 (may contain BOM)
 * - '||' means empty field (must be preserved)
 * - quoted fields may contain delimiter and even newlines (multiline text)
 * - IMPORTANT: treat quotes as CSV quoting ONLY when the quote is the FIRST char of a field.
 *   This prevents normal values like 6" from breaking parsing.
 *
 * Schema note:
 * - Tables may contain auto-increment PK (e.g., auto_id INTEGER PRIMARY KEY AUTOINCREMENT).
 *   CSV usually doesn't have this column. Importer must NOT insert auto_id; let SQLite generate it.
 *
 * Import policy:
 * - Always INSERT (keep ALL rows, including duplicates) to satisfy "read all content".
 *   If your table still has extra UNIQUE constraints besides auto_id, those rows would still fail.
 *   So for "keep all", make sure schema PK is auto_id only (or remove other UNIQUE constraints).
 */
public final class CsvToSqliteImporter {

    private static final String DEFAULT_CSV_FOLDER = "src/main/resources/csv";
    private static final char DELIMITER = '|';

    // If your auto id column is not named this, add more names here.
    private static final Set<String> AUTO_ID_COLS = Set.of("auto_id", "id_auto", "pk", "rowid");

    private CsvToSqliteImporter() {}

    public static void importDefaultCsvSeedLike(boolean forceReimport) throws IOException, SQLException {
        importCsvFolderSeedLike(DEFAULT_CSV_FOLDER, forceReimport);
    }

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

                if ("Datasheets_leader".equalsIgnoreCase(table)) {
                    headers = normalizeLeaderHeaders(headers);
                }

                if (headers.isEmpty()) {
                    System.out.println("[SKIP] " + table + ": missing headers in " + csv.getFileName());
                    continue;
                }

                if (forceReimport) {
                    try (Statement st = conn.createStatement()) {
                        st.execute("DELETE FROM \"" + table + "\";");
                    }
                }

                // DB columns
                LinkedHashSet<String> dbCols = getTableColumns(conn, table);

                // Columns we will insert:
                // 1) present in CSV headers
                // 2) present in DB table
                // 3) NOT auto_id (let SQLite generate it)
                List<Integer> headerIndexes = new ArrayList<>();
                List<String> insertCols = new ArrayList<>();

                for (int i = 0; i < headers.size(); i++) {
                    String h = headers.get(i);
                    if (h == null || h.isBlank()) continue;

                    if (!dbCols.contains(h)) continue;

                    if (isAutoIdColumn(h)) continue;

                    insertCols.add(h);
                    headerIndexes.add(i);
                }

                if (insertCols.isEmpty()) {
                    System.out.println("[SKIP] " + table + ": no matching columns between CSV and DB (after skipping auto_id)");
                    continue;
                }

                String insertSql = buildInsertSql(table, insertCols);

                int inserted = 0;
                try (PreparedStatement ps = conn.prepareStatement(insertSql)) {

                    for (int r = 1; r < rows.size(); r++) {
                        List<String> row = rows.get(r);
                        if (isAllEmpty(row)) continue;

                        bindRow(ps, headerIndexes, row);
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

    // -------------------- SQL helpers --------------------

    private static String buildInsertSql(String table, List<String> cols) {
        StringBuilder sb = new StringBuilder();
        sb.append("INSERT INTO \"").append(table).append("\" (");
        for (int i = 0; i < cols.size(); i++) {
            if (i > 0) sb.append(", ");
            sb.append("\"").append(cols.get(i)).append("\"");
        }
        sb.append(") VALUES (");
        for (int i = 0; i < cols.size(); i++) {
            if (i > 0) sb.append(", ");
            sb.append("?");
        }
        sb.append(");");
        return sb.toString();
    }

    private static void bindRow(PreparedStatement ps, List<Integer> headerIndexes, List<String> row) throws SQLException {
        for (int p = 0; p < headerIndexes.size(); p++) {
            int hIdx = headerIndexes.get(p);
            String raw = (hIdx < row.size()) ? row.get(hIdx) : "";
            bindValue(ps, p + 1, raw);
        }
    }

    private static void bindValue(PreparedStatement ps, int idx, String raw) throws SQLException {
        if (raw == null) {
            ps.setString(idx, "");
            return;
        }

        String s = stripBom(raw);
        String t = s.trim(); // IMPORTANT: trim before writing

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

        ps.setString(idx, t);
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

    private static LinkedHashSet<String> getTableColumns(Connection conn, String table) throws SQLException {
        LinkedHashSet<String> cols = new LinkedHashSet<>();
        try (Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery("PRAGMA table_info(\"" + table + "\");")) {
            while (rs.next()) {
                String name = rs.getString("name");
                if (name != null) cols.add(name.trim());
            }
        }
        return cols;
    }

    private static boolean isAutoIdColumn(String col) {
        if (col == null) return false;
        String x = col.trim().toLowerCase();
        if (AUTO_ID_COLS.contains(x)) return true;
        // common pattern
        return x.endsWith("_auto_id") || x.endsWith("_pk") || x.equals("autoid");
    }

    // -------------------- CSV parsing (robust pipe CSV) --------------------

    /**
     * Robust CSV reader:
     * - delimiter '|'
     * - keeps empty fields (||)
     * - supports quoted fields with escaped quotes ("")
     * - supports multiline quoted fields
     * - IMPORTANT: quote starts ONLY if it's the first character of the field.
     *   So values like 6" won't break parsing.
     */
    private static List<List<String>> readPipeDelimitedCsv(Path file) throws IOException {
        List<List<String>> rows = new ArrayList<>();

        try (Reader reader = Files.newBufferedReader(file, StandardCharsets.UTF_8);
             BufferedReader br = new BufferedReader(reader)) {

            List<String> row = new ArrayList<>();
            StringBuilder field = new StringBuilder();

            boolean inQuotes = false;
            boolean atFieldStart = true;

            int ch;
            while ((ch = br.read()) != -1) {
                char c = (char) ch;

                // normalize Windows CRLF
                if (c == '\r') continue;

                if (inQuotes) {
                    if (c == '"') {
                        br.mark(1);
                        int next = br.read();
                        if (next == '"') {
                            field.append('"'); // escaped quote
                        } else {
                            inQuotes = false; // end quoted field
                            if (next != -1) br.reset();
                        }
                    } else {
                        field.append(c);
                    }
                    atFieldStart = false;
                    continue;
                }

                // not in quotes
                if (atFieldStart && c == '"') {
                    // quote is only special at field start
                    inQuotes = true;
                    atFieldStart = false;
                    continue;
                }

                if (c == DELIMITER) {
                    row.add(field.toString());
                    field.setLength(0);
                    atFieldStart = true;
                    continue;
                }

                if (c == '\n') {
                    row.add(field.toString());
                    field.setLength(0);
                    atFieldStart = true;

                    // avoid adding a single empty row produced by trailing newline
                    if (!(row.size() == 1 && row.get(0).isEmpty())) {
                        rows.add(row);
                    }
                    row = new ArrayList<>();
                    continue;
                }

                field.append(c);
                atFieldStart = false;
            }

            // flush last record if no newline at EOF
            if (field.length() > 0 || !row.isEmpty()) {
                row.add(field.toString());
                if (!(row.size() == 1 && row.get(0).isEmpty())) {
                    rows.add(row);
                }
            }
        }

        return rows;
    }

    // -------------------- header normalization --------------------

    private static List<String> normalizeLeaderHeaders(List<String> headers) {
        List<String> out = new ArrayList<>(headers.size());
        for (String h : headers) {
            String x = (h == null) ? "" : h.trim();

            // export/spec variants -> your DB columns
            if ("datasheet_id".equalsIgnoreCase(x)) x = "leader_id";
            if ("attached_datasheet_id".equalsIgnoreCase(x)) x = "attached_id";

            out.add(x);
        }
        return out;
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