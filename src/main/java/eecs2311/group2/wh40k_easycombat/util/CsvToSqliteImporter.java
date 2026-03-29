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
 * - Static tables: generic seed-like import.
 * - Army preset tables: special import with id remapping, because child tables depend on old CSV auto_id values.
 */
public final class CsvToSqliteImporter {
    private static final char DELIMITER = '|';

    // If your auto id column is not named this, add more names here.
    private static final Set<String> AUTO_ID_COLS = Set.of("auto_id", "id_auto", "pk", "rowid");

    // These tables cannot use the generic importer because their child rows reference old CSV auto_id values.
    private static final Set<String> PRESET_ARMY_TABLES = Set.of(
            "Army",
            "Army_detachment",
            "Army_units",
            "Army_wargear"
    );

    private CsvToSqliteImporter() {}

    public static void importDefaultCsvSeedLike(boolean forceReimport) throws IOException, SQLException {
        BundledResourceManager.ensureBundledResourcesAvailable();
        importCsvFolderSeedLike(AppPaths.getSeedCsvDirectory().toString(), forceReimport);
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

                if (isPresetArmyTable(table)) {
                    System.out.println("[DEFER] " + table + ": imported by army preset remap flow");
                    continue;
                }

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

            importArmyPresetFamily(conn, folder, forceReimport);
            conn.commit();
        }
    }

    // -------------------- Special import for Army preset family --------------------

    /**
     * Army preset tables need special handling:
     * - CSV keeps old auto_id / foreign ids for relation purposes
     * - DB inserts must IGNORE auto_id
     * - army_id / units_id must be remapped to the newly generated SQLite ids
     *
     * Current policy:
     * - forceReimport = false: append only NEW preset armies; existing presets are skipped as a whole
     * - forceReimport = true: clear Army preset tables and rebuild them from CSV
     */
    private static void importArmyPresetFamily(Connection conn, Path folder, boolean forceReimport)
            throws IOException, SQLException {

        Path armyCsv = folder.resolve("Army.csv");
        if (!Files.exists(armyCsv)) {
            System.out.println("[SKIP] Army preset import: Army.csv not found");
            return;
        }

        Path armyDetachmentCsv = folder.resolve("Army_detachment.csv");
        Path armyUnitsCsv = folder.resolve("Army_units.csv");
        Path armyWargearCsv = folder.resolve("Army_wargear.csv");

        if (forceReimport) {
            clearArmyPresetTables(conn);
        }

        List<Map<String, String>> armyRows = readCsvAsMaps(armyCsv);
        if (armyRows.isEmpty()) {
            System.out.println("[SKIP] Army preset import: Army.csv has no data rows");
            return;
        }

        Map<Integer, Integer> armyIdMap = new LinkedHashMap<>();
        int importedArmyCount = 0;

        for (Map<String, String> row : armyRows) {
            int csvArmyId = parseIntSafe(row.get("auto_id"));
            if (csvArmyId <= 0) {
                System.out.println("[SKIP] Army preset row missing valid auto_id: " + row);
                continue;
            }

            if (!forceReimport && armyPresetAlreadyExists(conn, row)) {
                System.out.println("[SKIP] Army preset already exists: " + safe(row.get("name")));
                continue;
            }

            int newArmyId = insertArmy(conn, row);
            armyIdMap.put(csvArmyId, newArmyId);
            importedArmyCount++;
        }

        if (armyIdMap.isEmpty()) {
            System.out.println("[OK] Army preset import: no new Army rows to import");
            return;
        }

        int importedDetachmentCount = 0;
        if (Files.exists(armyDetachmentCsv)) {
            importedDetachmentCount = importArmyDetachments(conn, armyDetachmentCsv, armyIdMap);
        }

        Map<Integer, Integer> unitIdMap = new LinkedHashMap<>();
        int importedUnitCount = 0;
        if (Files.exists(armyUnitsCsv)) {
            ImportUnitsResult unitResult = importArmyUnits(conn, armyUnitsCsv, armyIdMap);
            importedUnitCount = unitResult.importedCount();
            unitIdMap.putAll(unitResult.unitIdMap());
        }

        int importedWargearCount = 0;
        if (Files.exists(armyWargearCsv) && !unitIdMap.isEmpty()) {
            importedWargearCount = importArmyWargear(conn, armyWargearCsv, unitIdMap);
        }

        System.out.println(
                "[OK] Army preset import: "
                        + importedArmyCount + " armies, "
                        + importedDetachmentCount + " detachments, "
                        + importedUnitCount + " units, "
                        + importedWargearCount + " wargear rows"
        );
    }

    private static void clearArmyPresetTables(Connection conn) throws SQLException {
        try (Statement st = conn.createStatement()) {
            st.execute("DELETE FROM \"Army_wargear\";");
            st.execute("DELETE FROM \"Army_units\";");
            st.execute("DELETE FROM \"Army_detachment\";");
            st.execute("DELETE FROM \"Army\";");
        }
    }

    private static boolean armyPresetAlreadyExists(Connection conn, Map<String, String> row) throws SQLException {
        String sql = """
                SELECT auto_id
                FROM "Army"
                WHERE name = ?
                  AND faction_id = ?
                  AND warlord_id = ?
                  AND total_points = ?
                  AND isMarked = ?
                LIMIT 1
                """;

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, safe(row.get("name")));
            ps.setString(2, safe(row.get("faction_id")));
            ps.setString(3, safe(row.get("warlord_id")));
            ps.setInt(4, parseIntSafe(row.get("total_points")));
            ps.setInt(5, parseBooleanish(row.get("isMarked")) ? 1 : 0);

            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }

    private static int insertArmy(Connection conn, Map<String, String> row) throws SQLException {
        String sql = """
                INSERT INTO "Army" (name, faction_id, warlord_id, total_points, isMarked)
                VALUES (?, ?, ?, ?, ?)
                """;

        try (PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, safe(row.get("name")));
            ps.setString(2, safe(row.get("faction_id")));
            ps.setString(3, safe(row.get("warlord_id")));
            ps.setInt(4, parseIntSafe(row.get("total_points")));
            ps.setInt(5, parseBooleanish(row.get("isMarked")) ? 1 : 0);
            ps.executeUpdate();
            return getGeneratedId(conn, ps);
        }
    }

    private static int importArmyDetachments(
            Connection conn,
            Path csv,
            Map<Integer, Integer> armyIdMap
    ) throws IOException, SQLException {
        List<Map<String, String>> rows = readCsvAsMaps(csv);
        int inserted = 0;

        String sql = """
                INSERT INTO "Army_detachment" (army_id, datasheet_id, detachment_id)
                VALUES (?, ?, ?)
                """;

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            for (Map<String, String> row : rows) {
                int oldArmyId = parseIntSafe(row.get("army_id"));
                Integer newArmyId = armyIdMap.get(oldArmyId);
                if (newArmyId == null) {
                    continue;
                }

                ps.setInt(1, newArmyId);
                ps.setString(2, safe(row.get("datasheet_id")));
                ps.setString(3, safe(row.get("detachment_id")));
                ps.addBatch();
                inserted++;
            }

            ps.executeBatch();
        }

        return inserted;
    }

    private static ImportUnitsResult importArmyUnits(
            Connection conn,
            Path csv,
            Map<Integer, Integer> armyIdMap
    ) throws IOException, SQLException {
        List<Map<String, String>> rows = readCsvAsMaps(csv);
        Map<Integer, Integer> unitIdMap = new LinkedHashMap<>();
        int inserted = 0;

        String sql = """
                INSERT INTO "Army_units" (army_id, datasheet_id, enhancements_id, model_count, unit_cost)
                VALUES (?, ?, ?, ?, ?)
                """;

        for (Map<String, String> row : rows) {
            int oldArmyId = parseIntSafe(row.get("army_id"));
            Integer newArmyId = armyIdMap.get(oldArmyId);
            if (newArmyId == null) {
                continue;
            }

            int oldUnitId = parseIntSafe(row.get("auto_id"));
            try (PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                ps.setInt(1, newArmyId);
                ps.setString(2, safe(row.get("datasheet_id")));
                ps.setString(3, safe(row.get("enhancements_id")));
                ps.setInt(4, parseIntSafe(row.get("model_count")));
                ps.setInt(5, parseIntSafe(row.get("unit_cost")));
                ps.executeUpdate();

                int newUnitId = getGeneratedId(conn, ps);
                if (oldUnitId > 0) {
                    unitIdMap.put(oldUnitId, newUnitId);
                }
                inserted++;
            }
        }

        return new ImportUnitsResult(inserted, unitIdMap);
    }

    private static int importArmyWargear(
            Connection conn,
            Path csv,
            Map<Integer, Integer> unitIdMap
    ) throws IOException, SQLException {
        List<Map<String, String>> rows = readCsvAsMaps(csv);
        int inserted = 0;

        String sql = """
                INSERT INTO "Army_wargear" (wargear_id, units_id, wargear_count)
                VALUES (?, ?, ?)
                """;

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            for (Map<String, String> row : rows) {
                int oldUnitsId = parseIntSafe(row.get("units_id"));
                Integer newUnitsId = unitIdMap.get(oldUnitsId);
                if (newUnitsId == null) {
                    continue;
                }

                ps.setInt(1, parseIntSafe(row.get("wargear_id")));
                ps.setInt(2, newUnitsId);
                ps.setInt(3, parseIntSafe(row.get("wargear_count")));
                ps.addBatch();
                inserted++;
            }

            ps.executeBatch();
        }

        return inserted;
    }

    private static int getGeneratedId(Connection conn, PreparedStatement ps) throws SQLException {
        try (ResultSet rs = ps.getGeneratedKeys()) {
            if (rs != null && rs.next()) {
                return rs.getInt(1);
            }
        }

        try (Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery("SELECT last_insert_rowid()")) {
            if (rs.next()) {
                return rs.getInt(1);
            }
        }

        throw new SQLException("Failed to read generated id.");
    }

    private static List<Map<String, String>> readCsvAsMaps(Path file) throws IOException {
        List<List<String>> rows = readPipeDelimitedCsv(file);
        if (rows.isEmpty()) {
            return List.of();
        }

        List<String> headers = normalizeHeaders(rows.get(0));
        headers = trimTrailingEmptyHeaders(headers);

        List<Map<String, String>> result = new ArrayList<>();
        for (int r = 1; r < rows.size(); r++) {
            List<String> row = rows.get(r);
            if (isAllEmpty(row)) {
                continue;
            }

            Map<String, String> mapped = new LinkedHashMap<>();
            for (int i = 0; i < headers.size(); i++) {
                String header = headers.get(i);
                if (header == null || header.isBlank()) {
                    continue;
                }

                String value = i < row.size() ? row.get(i) : "";
                mapped.put(header, stripBom(value == null ? "" : value.trim()));
            }

            result.add(mapped);
        }

        return result;
    }

    private record ImportUnitsResult(int importedCount, Map<Integer, Integer> unitIdMap) {
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
        return x.endsWith("_auto_id") || x.endsWith("_pk") || x.equals("autoid");
    }

    private static boolean isPresetArmyTable(String table) {
        for (String presetTable : PRESET_ARMY_TABLES) {
            if (presetTable.equalsIgnoreCase(table)) {
                return true;
            }
        }
        return false;
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

                if (c == '\r') continue;

                if (inQuotes) {
                    if (c == '"') {
                        br.mark(1);
                        int next = br.read();
                        if (next == '"') {
                            field.append('"');
                        } else {
                            inQuotes = false;
                            if (next != -1) br.reset();
                        }
                    } else {
                        field.append(c);
                    }
                    atFieldStart = false;
                    continue;
                }

                if (atFieldStart && c == '"') {
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

                    if (!(row.size() == 1 && row.get(0).isEmpty())) {
                        rows.add(row);
                    }
                    row = new ArrayList<>();
                    continue;
                }

                field.append(c);
                atFieldStart = false;
            }

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

    private static int parseIntSafe(String value) {
        if (value == null || value.isBlank()) {
            return 0;
        }

        try {
            return Integer.parseInt(value.trim());
        } catch (Exception e) {
            return 0;
        }
    }

    private static boolean parseBooleanish(String value) {
        if (value == null || value.isBlank()) {
            return false;
        }

        String normalized = value.trim();
        return "1".equals(normalized)
                || "true".equalsIgnoreCase(normalized)
                || "yes".equalsIgnoreCase(normalized);
    }

    private static String safe(String value) {
        return value == null ? "" : value.trim();
    }
}
