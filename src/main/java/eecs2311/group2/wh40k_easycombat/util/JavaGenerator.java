package eecs2311.group2.wh40k_easycombat.util;

import eecs2311.group2.wh40k_easycombat.annotation.*;

import java.io.File;
import java.lang.reflect.RecordComponent;
import java.net.URL;
import java.util.*;

public class JavaGenerator {

    // -----------------------------
    // Record Introspection Helpers
    // -----------------------------

    private static RecordComponent[] components(Class<?> clazz) {
        if (!clazz.isRecord()) {
            throw new IllegalArgumentException(clazz.getSimpleName() + " is not a record. JavaGenerator expects record models.");
        }
        return clazz.getRecordComponents();
    }

    private static boolean isListComponent(RecordComponent rc) {
        return List.class.isAssignableFrom(rc.getType());
    }

    private static String sqlGetterExpr(RecordComponent rc, String colName) {
        if (isListComponent(rc)) {
            return "IntListCodec.decode(rs.getString(\"" + colName + "\"))";
        }
        Class<?> t = rc.getType();
        if (t.equals(String.class)) return "rs.getString(\"" + colName + "\")";
        if (t.equals(int.class) || t.equals(Integer.class)) return "rs.getInt(\"" + colName + "\")";
        if (t.equals(boolean.class) || t.equals(Boolean.class)) return "rs.getBoolean(\"" + colName + "\")";
        return "(" + t.getSimpleName() + ") rs.getObject(\"" + colName + "\")";
    }

    private static String paramExpr(String instanceName, RecordComponent rc) {
        if (isListComponent(rc)) {
            return "IntListCodec.encode(" + instanceName + "." + rc.getName() + "())";
        }
        return instanceName + "." + rc.getName() + "()";
    }

    // Primary Key detection:
    // 1) If @CompositePK exists on TYPE, use its columns()
    // 2) else, use record components annotated with @PK
    private static List<String> pkColumns(Class<?> clazz) {
        CompositePK cpk = clazz.getAnnotation(CompositePK.class);
        if (cpk != null && cpk.columns() != null && cpk.columns().length > 0) {
            return Arrays.asList(cpk.columns());
        }
        List<String> cols = new ArrayList<>();
        for (RecordComponent rc : components(clazz)) {
            if (rc.getAnnotation(PK.class) != null) cols.add(rc.getName());
        }
        return cols;
    }

    private static Set<String> autoIncrementColumns(Class<?> clazz) {
        Set<String> cols = new HashSet<>();
        for (RecordComponent rc : components(clazz)) {
            if (rc.getAnnotation(AutoIncrement.class) != null) cols.add(rc.getName());
        }
        return cols;
    }

    private static RecordComponent findComponent(Class<?> clazz, String name) {
        for (RecordComponent rc : components(clazz)) {
            if (rc.getName().equals(name)) return rc;
        }
        return null;
    }

    // -----------------------------
    // CRUD Generators
    // -----------------------------

    public static String generateAddFunc(Class<?> clazz) {
        String tableName = clazz.getAnnotation(Table.class).value();
        String className = clazz.getSimpleName();
        String repoTargetName = className; // keep exact
        String instanceName = className.toLowerCase();

        Set<String> autoIncCols = autoIncrementColumns(clazz);

        StringBuilder javaFunc = new StringBuilder();
        javaFunc.append("\tpublic static int addNew").append(repoTargetName)
                .append("(").append(className).append(" ").append(instanceName).append(") throws java.sql.SQLException {\n")
                .append("\t\treturn Dao.update(\n");

        StringBuilder sql = new StringBuilder("\t\t\t\"INSERT INTO " + tableName + " (");
        StringBuilder placeholders = new StringBuilder(") VALUES (");
        StringBuilder params = new StringBuilder();

        boolean firstCol = true;
        List<RecordComponent> insertComponents = new ArrayList<>();

        for (RecordComponent rc : components(clazz)) {
            // skip auto-increment columns only
            if (autoIncCols.contains(rc.getName())) continue;

            insertComponents.add(rc);

            if (!firstCol) sql.append(", ");
            sql.append(rc.getName());
            firstCol = false;
        }

        for (int i = 0; i < insertComponents.size(); i++) {
            if (i > 0) placeholders.append(", ");
            placeholders.append("?");
        }

        sql.append(placeholders).append(")\"");

        boolean firstParam = true;
        for (RecordComponent rc : insertComponents) {
            if (firstParam) {
                params.append("\n");
                firstParam = false;
            } else {
                params.append(",\n");
            }
            params.append("\t\t\t").append(paramExpr(instanceName, rc));
        }

        javaFunc.append(sql).append(",").append(params).append("\n\t\t);\n\t}\n");
        return javaFunc.toString();
    }

    public static String generateGetByPkFunc(Class<?> clazz) {
        String tableName = clazz.getAnnotation(Table.class).value();
        String className = clazz.getSimpleName();

        List<String> pkCols = pkColumns(clazz);
        if (pkCols.isEmpty()) {
            // no PK → no getByPk
            return "";
        }

        // build signature with correct param types
        StringBuilder sig = new StringBuilder();
        sig.append("\tpublic static ").append(className).append(" get")
                .append(className).append("ByPk(");

        StringBuilder where = new StringBuilder();
        StringBuilder callParams = new StringBuilder();

        for (int i = 0; i < pkCols.size(); i++) {
            String col = pkCols.get(i);
            RecordComponent rc = findComponent(clazz, col);
            if (rc == null) {
                throw new RuntimeException("CompositePK column '" + col + "' not found in record " + className);
            }
            if (i > 0) {
                sig.append(", ");
                where.append(" AND ");
                callParams.append(", ");
            }
            sig.append(rc.getType().getSimpleName()).append(" ").append(col);
            where.append(col).append(" = ?");
            callParams.append(col);
        }
        sig.append(") throws java.sql.SQLException {\n");

        StringBuilder func = new StringBuilder();
        func.append(sig);
        func.append("\t\treturn Dao.query(\n");
        func.append("\t\t\t\"SELECT * FROM ").append(tableName).append(" WHERE ").append(where).append("\",\n");

        // mapper
        func.append("\t\t\trs -> new ").append(className).append("(\n");
        RecordComponent[] rcs = components(clazz);
        for (int i = 0; i < rcs.length; i++) {
            RecordComponent rc = rcs[i];
            func.append("\t\t\t\t").append(sqlGetterExpr(rc, rc.getName()));
            if (i < rcs.length - 1) func.append(",");
            func.append("\n");
        }
        func.append("\t\t\t),\n");
        func.append("\t\t\t").append(callParams).append("\n");
        func.append("\t\t).stream().findFirst().orElse(null);\n");
        func.append("\t}\n");

        return func.toString();
    }

    public static String generateGetAllFunc(Class<?> clazz) {
        String tableName = clazz.getAnnotation(Table.class).value();
        String className = clazz.getSimpleName();

        StringBuilder func = new StringBuilder();
        func.append("\tpublic static java.util.List<").append(className).append("> getAll")
                .append(className).append("() throws java.sql.SQLException {\n");
        func.append("\t\treturn Dao.query(\n");
        func.append("\t\t\t\"SELECT * FROM ").append(tableName).append("\",\n");

        func.append("\t\t\trs -> new ").append(className).append("(\n");
        RecordComponent[] rcs = components(clazz);
        for (int i = 0; i < rcs.length; i++) {
            RecordComponent rc = rcs[i];
            func.append("\t\t\t\t").append(sqlGetterExpr(rc, rc.getName()));
            if (i < rcs.length - 1) func.append(",");
            func.append("\n");
        }
        func.append("\t\t\t)\n");
        func.append("\t\t);\n");
        func.append("\t}\n");

        return func.toString();
    }

    public static String generateUpdateFunc(Class<?> clazz) {
        String tableName = clazz.getAnnotation(Table.class).value();
        String className = clazz.getSimpleName();
        String instanceName = className.toLowerCase();

        List<String> pkCols = pkColumns(clazz);
        if (pkCols.isEmpty()) {
            // no PK → no update
            return "";
        }

        Set<String> pkSet = new HashSet<>(pkCols);

        List<RecordComponent> setComponents = new ArrayList<>();
        for (RecordComponent rc : components(clazz)) {
            if (!pkSet.contains(rc.getName())) {
                setComponents.add(rc);
            }
        }

        // If all columns are PK (rare), update is meaningless
        if (setComponents.isEmpty()) return "";

        StringBuilder func = new StringBuilder();
        func.append("\tpublic static void update").append(className)
                .append("(").append(className).append(" ").append(instanceName)
                .append(") throws java.sql.SQLException {\n");
        func.append("\t\tDao.update(\n");

        StringBuilder sql = new StringBuilder();
        sql.append("\t\t\t\"UPDATE ").append(tableName).append(" SET ");

        for (int i = 0; i < setComponents.size(); i++) {
            RecordComponent rc = setComponents.get(i);
            if (i > 0) sql.append(", ");
            sql.append(rc.getName()).append(" = ?");
        }

        sql.append(" WHERE ");
        for (int i = 0; i < pkCols.size(); i++) {
            if (i > 0) sql.append(" AND ");
            sql.append(pkCols.get(i)).append(" = ?");
        }
        sql.append("\",");

        func.append(sql).append("\n");

        // params: set cols, then pk cols
        boolean first = true;
        for (RecordComponent rc : setComponents) {
            if (!first) func.append(",\n");
            func.append("\t\t\t").append(paramExpr(instanceName, rc));
            first = false;
        }
        for (String pk : pkCols) {
            RecordComponent rc = findComponent(clazz, pk);
            if (rc == null) throw new RuntimeException("PK column '" + pk + "' not found in " + className);
            func.append(",\n\t\t\t").append(paramExpr(instanceName, rc));
        }

        func.append("\n\t\t);\n");
        func.append("\t}\n");
        return func.toString();
    }

    public static String generateDeleteFunc(Class<?> clazz) {
        String tableName = clazz.getAnnotation(Table.class).value();
        String className = clazz.getSimpleName();
        String instanceName = className.toLowerCase();

        List<String> pkCols = pkColumns(clazz);
        if (pkCols.isEmpty()) {
            // no PK → no delete
            return "";
        }

        StringBuilder func = new StringBuilder();
        func.append("\tpublic static void delete").append(className)
                .append("(").append(className).append(" ").append(instanceName)
                .append(") throws java.sql.SQLException {\n");
        func.append("\t\tDao.update(\n");

        StringBuilder sql = new StringBuilder();
        sql.append("\t\t\t\"DELETE FROM ").append(tableName).append(" WHERE ");
        for (int i = 0; i < pkCols.size(); i++) {
            if (i > 0) sql.append(" AND ");
            sql.append(pkCols.get(i)).append(" = ?");
        }
        sql.append("\",");

        func.append(sql).append("\n");

        for (int i = 0; i < pkCols.size(); i++) {
            RecordComponent rc = findComponent(clazz, pkCols.get(i));
            if (rc == null) throw new RuntimeException("PK column '" + pkCols.get(i) + "' not found in " + className);
            if (i > 0) func.append(",\n");
            func.append("\t\t\t").append(paramExpr(instanceName, rc));
        }

        func.append("\n\t\t);\n");
        func.append("\t}\n");
        return func.toString();
    }

    public static String generateCrudCode(Class<?> clazz) throws Exception {
        String className = clazz.getSimpleName();

        StringBuilder fullJava = new StringBuilder();
        fullJava.append("//-- Auto Generated Java File --\n\n");
        fullJava.append("package eecs2311.group2.wh40k_easycombat.repository;\n\n");

        fullJava.append("import eecs2311.group2.wh40k_easycombat.db.Dao;\n");
        fullJava.append("import eecs2311.group2.wh40k_easycombat.model.").append(className).append(";\n");
        fullJava.append("import eecs2311.group2.wh40k_easycombat.util.IntListCodec;\n\n");

        fullJava.append("import java.util.List;\n");
        fullJava.append("import java.sql.SQLException;\n\n");

        fullJava.append("@SuppressWarnings(\"unused\")\n");
        fullJava.append("public class ").append(className).append("Repository {\n\n");

        fullJava.append(generateAddFunc(clazz)).append("\n");
        fullJava.append(generateGetByPkFunc(clazz)).append("\n");
        fullJava.append(generateGetAllFunc(clazz)).append("\n");
        fullJava.append(generateUpdateFunc(clazz)).append("\n");
        fullJava.append(generateDeleteFunc(clazz)).append("\n");

        fullJava.append("}\n");
        return fullJava.toString();
    }

    // -----------------------------
    // Naming helper (keep stable)
    // -----------------------------
    public static String pluralToSingular(String input) {
        // Keep old behavior only for simple names; avoid breaking names with underscores.
        if (input.contains("_")) return input;
        if (input.endsWith("ies") && input.length() > 3) return input.substring(0, input.length() - 3) + "y";
        if (input.endsWith("s") && !input.endsWith("ss") && input.length() > 1) return input.substring(0, input.length() - 1);
        return input;
    }

    // -----------------------------
    // Scanner
    // -----------------------------
    public static List<Class<?>> getClassesWithTableAnnotation(String packageName) throws Exception {
        List<Class<?>> classes = new ArrayList<>();
        String path = packageName.replace('.', '/');
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        URL resource = classLoader.getResource(path);

        if (resource == null) return classes;

        File directory = new File(resource.toURI());
        if (directory.exists()) {
            File[] files = directory.listFiles();
            if (files == null) return classes;

            for (File file : files) {
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