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
 * Requirements:
 * - delimiter: '|'
 * - UTF-8 (may contain BOM)
 * - '||' means empty field (must be preserved)
 * - quoted fields may contain delimiter and even newlines (multiline text)
 * - header row provides column names
 *
 */
public final class CsvToSqliteImporter {

    private static final String DEFAULT_CSV_FOLDER = "src/main/resources/csv";
    private static final char DELIMITER = '|';

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

                // ✅ special header normalization for Datasheets_leader
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

                String insertSql = buildInsertSql(table, headers);

                int inserted = 0;
                try (PreparedStatement ps = conn.prepareStatement(insertSql)) {
                    for (int r = 1; r < rows.size(); r++) {
                        List<String> row = rows.get(r);
                        if (isAllEmpty(row)) continue;

                        bindRow(table, ps, headers, row);
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

    private static void bindRow(String table, PreparedStatement ps, List<String> headers, List<String> row) throws SQLException {
        // ✅ Datasheets_models packed profile fix
        if ("Datasheets_models".equalsIgnoreCase(table)) {
            row = fixPackedModelsRow(headers, row);
        }

        // (If in future you discover other "packed" columns in other tables,
        // add similar fix methods here.)

        for (int i = 0; i < headers.size(); i++) {
            String raw = (i < row.size()) ? row.get(i) : "";
            bindValue(ps, i + 1, raw);
        }
    }

    private static void bindValue(PreparedStatement ps, int idx, String raw) throws SQLException {
        if (raw == null) {
            ps.setString(idx, "");
            return;
        }

        // remove BOM and trim BEFORE writing to DB (prevents ids with '\r' / trailing spaces)
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

                // odd number of quotes toggles quoting state
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

    /** Supports quoted fields and escaped quotes (""). Keeps empty fields (||) correctly. */
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

    // -------------------- Special fixes --------------------
    private static List<String> fixPackedModelsRow(List<String> headers, List<String> row) {
        int idxM = headers.indexOf("M");
        if (idxM < 0 || idxM >= row.size()) return row;

        String mCell = row.get(idxM);
        if (mCell == null || !mCell.contains("|")) return row;

        // IMPORTANT: keep empty tokens for "||"
        String[] packed = mCell.split("\\|", -1);

        // If it doesn't have at least the early fields, don't touch it
        if (packed.length < 3) return row;

        List<String> out = new ArrayList<>(row);

        // ✅ include inv_sv_descr (your “SV 描述”列)
        setIfPresent(headers, out, "M",               packed, 0);
        setIfPresent(headers, out, "T",               packed, 1);
        setIfPresent(headers, out, "Sv",              packed, 2);
        setIfPresent(headers, out, "inv_sv",          packed, 3);
        setIfPresent(headers, out, "inv_sv_descr",    packed, 4);
        setIfPresent(headers, out, "W",               packed, 5);
        setIfPresent(headers, out, "Ld",              packed, 6);
        setIfPresent(headers, out, "OC",              packed, 7);
        setIfPresent(headers, out, "base_size",       packed, 8);
        setIfPresent(headers, out, "base_size_descr", packed, 9);

        return out;
    }

    private static void setIfPresent(List<String> headers, List<String> row, String col, String[] packed, int packedIndex) {
        int idx = headers.indexOf(col);
        if (idx < 0) return;
        if (packedIndex < 0 || packedIndex >= packed.length) return;

        while (row.size() <= idx) row.add("");
        row.set(idx, packed[packedIndex]);
    }

    /** Accept both naming conventions and normalize to DB columns: leader_id, attached_id. */
    private static List<String> normalizeLeaderHeaders(List<String> headers) {
        List<String> out = new ArrayList<>(headers.size());
        for (String h : headers) {
            String x = (h == null) ? "" : h.trim();

            // Some specs/export versions use these names:
            // datasheet_id -> leader_id
            // attached_datasheet_id -> attached_id
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