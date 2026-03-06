package eecs2311.group2.wh40k_easycombat.util;

import eecs2311.group2.wh40k_easycombat.annotation.*;

import java.io.File;
import java.lang.reflect.Field;
import java.net.URL;
import java.util.*;
import java.util.stream.Collectors;

public class SqlGenerator {

    private static final String MODEL_PACKAGE = "eecs2311.group2.wh40k_easycombat.model";

    // SQLite reserved keywords (lowercase)
    private static final Set<String> SQLITE_KEYWORDS = Set.of(
        "group", "order", "table", "index", "join", "where", "limit", "offset"
    );

    public static String generateCreateTable(Class<?> clazz) {

        String tableName = safeName(
            clazz.isAnnotationPresent(Table.class)
                ? clazz.getAnnotation(Table.class).value()
                : clazz.getSimpleName()
        );

        StringBuilder sql = new StringBuilder("CREATE TABLE IF NOT EXISTS " + tableName + " (\n");
        List<String> defs = new ArrayList<>();

        // Collect column definitions
        for (Field field : clazz.getDeclaredFields()) {
            if (field.isAnnotationPresent(Ignore.class)) continue;

            String colName = safeName(field.getName());
            Class<?> type = field.getType();
            String sqlType = getSqlType(type);

            StringBuilder col = new StringBuilder("    " + colName + " " + sqlType);

            // PRIMARY KEY / AUTOINCREMENT
            if (field.isAnnotationPresent(AutoIncrement.class)) {
                if (!field.isAnnotationPresent(PK.class) ||
                    !(type.equals(int.class) || type.equals(Integer.class))) {
                    throw new IllegalStateException(
                        "AUTOINCREMENT requires INTEGER PRIMARY KEY on field: " + field.getName()
                    );
                }
                col.append(" PRIMARY KEY AUTOINCREMENT");
            } else if (field.isAnnotationPresent(PK.class) && !clazz.isAnnotationPresent(CompositePK.class)) {
                col.append(" PRIMARY KEY");
            }

            // NOT NULL
            if (field.isAnnotationPresent(NotNull.class)) {
                col.append(" NOT NULL");
            }

            // UNIQUE
            if (field.isAnnotationPresent(Unique.class)) {
                col.append(" UNIQUE");
            }

            // DEFAULT
            if (field.isAnnotationPresent(Default.class)) {
                String raw = field.getAnnotation(Default.class).value();
                col.append(" DEFAULT ").append(formatDefault(type, raw));
            }

            // boolean CHECK
            if ((type.equals(boolean.class) || type.equals(Boolean.class))
                && !field.isAnnotationPresent(Check.class)) {
                col.append(" CHECK(").append(colName).append(" IN (0,1))");
            }

            // user-defined CHECK
            if (field.isAnnotationPresent(Check.class)) {
                col.append(" CHECK(").append(field.getAnnotation(Check.class).value()).append(")");
            }

            defs.add(col.toString());
        }

        // Foreign keys
        for (Field field : clazz.getDeclaredFields()) {
            if (field.isAnnotationPresent(FK.class)) {
                FK fk = field.getAnnotation(FK.class);

                defs.add(String.format(
                    "    FOREIGN KEY(%s) REFERENCES %s(%s) ON DELETE CASCADE ON UPDATE CASCADE",
                    safeName(field.getName()),
                    safeName(fk.table()),
                    safeName(fk.column())
                ));
            }
        }

        // Composite primary key
        if (clazz.isAnnotationPresent(CompositePK.class)) {
            String cols = Arrays.stream(clazz.getAnnotation(CompositePK.class).columns())
                .map(SqlGenerator::safeName)
                .collect(Collectors.joining(", "));
            defs.add("    PRIMARY KEY (" + cols + ")");
        }

        sql.append(String.join(",\n", defs));
        sql.append("\n);");

        return sql.toString();
    }

    public static String generateFullSchema() throws Exception {
        List<Class<?>> tableClasses = getClassesWithTableAnnotation(MODEL_PACKAGE);

        StringBuilder fullSql = new StringBuilder();
        fullSql.append("-- Auto Generated Script --").append("\n");
        fullSql.append("PRAGMA foreign_keys = ON;\n\n");
        fullSql.append("PRAGMA journal_mode = WAL;\n\n");
        fullSql.append("PRAGMA synchronous = NORMAL;\n\n");

        for (Class<?> clazz : tableClasses) {
            fullSql.append(SqlGenerator.generateCreateTable(clazz)).append("\n\n");
        }

        return fullSql.toString();
    }

    // -----------------------------
    // Helpers
    // -----------------------------

    private static String safeName(String name) {
        String lower = name.toLowerCase();
        if (SQLITE_KEYWORDS.contains(lower)) {
            return "\"" + name + "\""; // quote only if keyword
        }
        return name; // preserve original case
    }

    private static String getSqlType(Class<?> type) {
        if (type == int.class || type == Integer.class || type == long.class)
            return "INTEGER";
        if (type == float.class || type == double.class) return "REAL";
        if (type == boolean.class || type == Boolean.class) return "INTEGER";
        return "TEXT";
    }

    private static String formatDefault(Class<?> type, String value) {
        if (type == boolean.class || type == Boolean.class) {
            if (value.equalsIgnoreCase("true") || value.equals("1")) return "1";
            return "0";
        }

        // If looks like a function call, return as-is
        if (value.contains("(") && value.contains(")")) {
            return "(" + value + ")";
        }

        // String default → wrap in quotes, escape single quotes
        if (type == String.class) {
            String escaped = value.replace("'", "''");
            return "'" + escaped + "'";
        }

        // numeric default
        return value;
    }

    private static List<Class<?>> getClassesWithTableAnnotation(String packageName) throws Exception {
        List<Class<?>> classes = new ArrayList<>();
        String path = packageName.replace('.', '/');
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        URL resource = classLoader.getResource(path);

        if (resource == null) return classes;

        File directory = new File(resource.toURI());
        if (directory.exists()) {
            for (File file : directory.listFiles()) {
                if (file.getName().endsWith(".class")) {
                    String className = packageName + '.' + file.getName().substring(0, file.getName().length() - 6);
                    Class<?> clazz = Class.forName(className);
                    if (clazz.isAnnotationPresent(Table.class)) {
                        classes.add(clazz);
                    }
                }
            }
        }
        return classes;
    }
}
