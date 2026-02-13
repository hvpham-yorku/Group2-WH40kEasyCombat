package eecs2311.group2.wh40k_easycombat.util;

import eecs2311.group2.wh40k_easycombat.annotation.*;

import java.io.File;
import java.lang.reflect.Field;
import java.net.URL;
import java.util.*;

public class JavaGenerator {

    private static boolean isListField(Field field) {
        return List.class.isAssignableFrom(field.getType());
    }

    public static String generateAddFunc(Class<?> clazz) {
        String tableName = clazz.getAnnotation(Table.class).value();
        String className = clazz.getSimpleName();
        String classNameSingular = pluralToSingular(className);
        String classInstance = classNameSingular.toLowerCase();

        String javaFunc =
                "\t\tpublic static int addNew" + classNameSingular + "(" + className + " " + classInstance + ") throws SQLException {\n" +
                "\t\t\t\treturn Dao.update(\n";

        StringBuilder sql = new StringBuilder("\t\t\t\t\t\t\"INSERT INTO " + tableName + " (");
        StringBuilder placeholders = new StringBuilder(") VALUES (");
        StringBuilder parameters = new StringBuilder();

        boolean first = true;

        for (Field field : clazz.getDeclaredFields()) {
            if (field.getName().equals("id")) continue;

            sql.append(field.getName()).append(", ");
            placeholders.append("?, ");

            if (first) first = false;
        }

        // remove last ", "
        if (sql.toString().endsWith(", ")) sql.setLength(sql.length() - 2);
        if (placeholders.toString().endsWith(", ")) placeholders.setLength(placeholders.length() - 2);

        // close SQL string
        placeholders.append(")\"");

        // build parameters (NO leading comma)
        boolean firstParam = true;
        for (Field field : clazz.getDeclaredFields()) {
            if (field.getName().equals("id")) continue;

            if (firstParam) {
                parameters.append("\n");
                firstParam = false;
            } else {
                parameters.append(",\n");
            }

            parameters.append("\t\t\t\t\t\t");
            if (isListField(field)) {
                parameters.append("IntListCodec.encode(").append(classInstance).append(".").append(field.getName()).append("())");
            } else {
                parameters.append(classInstance).append(".").append(field.getName()).append("()");
            }
        }

        return javaFunc
                + sql + placeholders + ","
                + parameters
                + "\n\t\t\t\t);\n\t\t}\n";
    }

    public static String generateGetFunc(Class<?> clazz) {
        String tableName = clazz.getAnnotation(Table.class).value();
        String className = clazz.getSimpleName();
        String classNameSingular = pluralToSingular(className);

        String javaFunc =
                "\t\tpublic static " + className + " get" + classNameSingular + "ById(int id) throws SQLException {\n" +
                "\t\t\t\treturn Dao.query(\n";

        String sql = "\t\t\t\t\t\t\"SELECT * FROM " + tableName + " WHERE id = ?\",\n";

        StringBuilder lambda = new StringBuilder("\t\t\t\t\t\trs -> new " + className + "(\n");

        for (Field field : clazz.getDeclaredFields()) {
            if (isListField(field)) {
                lambda.append("\t\t\t\t\t\t\t\t")
                        .append("IntListCodec.decode(rs.getString(\"").append(field.getName()).append("\")),\n");
            } else if (field.getType().equals(String.class)) {
                lambda.append("\t\t\t\t\t\t\t\t")
                        .append("rs.getString(\"").append(field.getName()).append("\"),\n");
            } else if (field.getType().equals(int.class) || field.getType().equals(Integer.class)) {
                lambda.append("\t\t\t\t\t\t\t\t")
                        .append("rs.getInt(\"").append(field.getName()).append("\"),\n");
            } else if (field.getType().equals(boolean.class) || field.getType().equals(Boolean.class)) {
                lambda.append("\t\t\t\t\t\t\t\t")
                        .append("rs.getBoolean(\"").append(field.getName()).append("\"),\n");
            } else {
                // fallback: getObject
                lambda.append("\t\t\t\t\t\t\t\t")
                        .append("(").append(field.getType().getSimpleName()).append(") rs.getObject(\"").append(field.getName()).append("\"),\n");
            }
        }

        // remove last ",\n"
        if (lambda.toString().endsWith(",\n")) lambda.setLength(lambda.length() - 2);

        lambda.append("\t\t\t\t\t\t),\n");

        return javaFunc
                + sql
                + lambda
                + "\t\t\t\t\t\tid\n"
                + "\t\t\t\t).stream().findFirst().orElse(null);\n\t\t}\n";
    }

    public static String generateGetAllFunc(Class<?> clazz) {
        String tableName = clazz.getAnnotation(Table.class).value();
        String className = clazz.getSimpleName();

        String javaFunc =
                "\t\tpublic static List<" + className + "> getAll" + className + "() throws SQLException {\n" +
                "\t\t\t\treturn Dao.query(\n";

        String sql = "\t\t\t\t\t\t\"SELECT * FROM " + tableName + "\",\n";

        StringBuilder lambda = new StringBuilder("\t\t\t\t\t\trs -> new " + className + "(\n");

        for (Field field : clazz.getDeclaredFields()) {
            if (isListField(field)) {
                lambda.append("\t\t\t\t\t\t\t\t")
                        .append("IntListCodec.decode(rs.getString(\"").append(field.getName()).append("\")),\n");
            } else if (field.getType().equals(String.class)) {
                lambda.append("\t\t\t\t\t\t\t\t")
                        .append("rs.getString(\"").append(field.getName()).append("\"),\n");
            } else if (field.getType().equals(int.class) || field.getType().equals(Integer.class)) {
                lambda.append("\t\t\t\t\t\t\t\t")
                        .append("rs.getInt(\"").append(field.getName()).append("\"),\n");
            } else if (field.getType().equals(boolean.class) || field.getType().equals(Boolean.class)) {
                lambda.append("\t\t\t\t\t\t\t\t")
                        .append("rs.getBoolean(\"").append(field.getName()).append("\"),\n");
            } else {
                lambda.append("\t\t\t\t\t\t\t\t")
                        .append("(").append(field.getType().getSimpleName()).append(") rs.getObject(\"").append(field.getName()).append("\"),\n");
            }
        }

        if (lambda.toString().endsWith(",\n")) lambda.setLength(lambda.length() - 2);

        lambda.append("\t\t\t\t\t\t)\n");

        return javaFunc
                + sql
                + lambda
                + "\t\t\t\t);\n\t\t}\n";
    }

    public static String generateUpdateFunc(Class<?> clazz) {
        String tableName = clazz.getAnnotation(Table.class).value();
        String className = clazz.getSimpleName();
        String classNameSingular = pluralToSingular(className);
        String classInstance = classNameSingular.toLowerCase();

        String javaFunc =
                "\t\tpublic static void update" + classNameSingular + "(" + className + " " + classInstance + ") throws SQLException {\n" +
                "\t\t\t\tDao.update(\n";

        StringBuilder sql = new StringBuilder("\t\t\t\t\t\t\"UPDATE " + tableName + " SET ");

        // build SET ...
        for (Field field : clazz.getDeclaredFields()) {
            if (field.getName().equals("id")) continue;
            sql.append(field.getName()).append(" = ?, ");
        }
        if (sql.toString().endsWith(", ")) sql.setLength(sql.length() - 2);

        sql.append(" WHERE id = ?\"");

        // build params
        StringBuilder parameters = new StringBuilder();

        boolean firstParam = true;
        for (Field field : clazz.getDeclaredFields()) {
            if (field.getName().equals("id")) continue;

            if (firstParam) {
                parameters.append("\n");
                firstParam = false;
            } else {
                parameters.append(",\n");
            }

            parameters.append("\t\t\t\t\t\t");
            if (isListField(field)) {
                parameters.append("IntListCodec.encode(").append(classInstance).append(".").append(field.getName()).append("())");
            } else {
                parameters.append(classInstance).append(".").append(field.getName()).append("()");
            }
        }

        // add id last
        parameters.append(",\n\t\t\t\t\t\t").append(classInstance).append(".id()");

        return javaFunc
                + sql + ","
                + parameters
                + "\n\t\t\t\t);\n\t\t}\n";
    }

    public static String generateDeleteFunc(Class<?> clazz) {
        String tableName = clazz.getAnnotation(Table.class).value();
        String className = clazz.getSimpleName();
        String classNameSingular = pluralToSingular(className);
        String classInstance = classNameSingular.toLowerCase();

        String javaFunc =
                "\t\tpublic static void delete" + classNameSingular + "(" + className + " " + classInstance + ") throws SQLException {\n" +
                "\t\t\t\tDao.update(\n";

        String sql = "\t\t\t\t\t\t\"DELETE FROM " + tableName + " WHERE id = ?\",\n";
        String params = "\t\t\t\t\t\t" + classInstance + ".id()\n";

        return javaFunc + sql + params + "\t\t\t\t);\n\t\t}\n";
    }

    public static String generateCrudCode(Class<?> clazz) throws Exception {

        StringBuilder fullJava = new StringBuilder();
        fullJava.append("//-- Auto Generated Java File --\n\n");

        fullJava.append("package eecs2311.group2.wh40k_easycombat.repository;\n\n");

        fullJava.append("import eecs2311.group2.wh40k_easycombat.db.Dao;\n");
        fullJava.append("import eecs2311.group2.wh40k_easycombat.model.").append(clazz.getSimpleName()).append(";\n");
        fullJava.append("import eecs2311.group2.wh40k_easycombat.util.IntListCodec;\n\n");

        fullJava.append("import java.util.List;\n");
        fullJava.append("import java.sql.SQLException;\n\n");

        fullJava.append("@SuppressWarnings(\"unused\")\n");
        fullJava.append("public class ").append(pluralToSingular(clazz.getSimpleName())).append("Repository {\n");

        fullJava.append(generateAddFunc(clazz));
        fullJava.append(generateGetFunc(clazz));
        fullJava.append(generateGetAllFunc(clazz));
        fullJava.append(generateUpdateFunc(clazz));
        fullJava.append(generateDeleteFunc(clazz));

        fullJava.append("}\n");

        return fullJava.toString();
    }

    // -----------------------------
    // Helpers
    // -----------------------------

    public static String pluralToSingular(String input){
        return input.substring(input.length() - 3).equals("ies") ?
                input.substring(0, input.length() - 3) + "y" :
                input.substring(0, input.length() - 1);
    }

    public static List<Class<?>> getClassesWithTableAnnotation(String packageName) throws Exception {
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
