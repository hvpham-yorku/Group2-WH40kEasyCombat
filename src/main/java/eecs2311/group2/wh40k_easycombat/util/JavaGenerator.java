package eecs2311.group2.wh40k_easycombat.util;

import eecs2311.group2.wh40k_easycombat.annotation.*;

import java.io.File;
import java.lang.reflect.Field;
import java.net.URL;
import java.util.*;

public class JavaGenerator {

    public static String generateAddFunc(Class<?> clazz) {
        //Below are the several different styles used in making a repo file
        String tableName = clazz.getAnnotation(Table.class).value(); //units
        String className = clazz.getSimpleName(); //Units
        String classNameSingular = pluralToSingular(className); //Unit
        String classInstance = classNameSingular.toLowerCase(); //unit
        
        //Function header
        String javaFunc = "\t\tpublic static int addNew" + classNameSingular + "(" + className + " " + classInstance + ") throws SQLException {\n\t\t\t\treturn Dao.insert(\n";
        
        StringBuilder sql = new StringBuilder("\t\t\t\t\t\t\"INSERT INTO " + tableName + " (");
        StringBuilder placeholders = new StringBuilder(" VALUES (");
        StringBuilder parameters = new StringBuilder();
        
        for (Field field : clazz.getDeclaredFields()) {
            if (field.getName().equals("id")) { continue; }
            //Id does not need an input for sql

            sql.append(field.getName()).append(", ");
            placeholders.append("?, ");

            //Assumption is that if there is an list we are using it for integers
            if (field.getType().equals(List.class)){
                parameters.append(",\n\t\t\t\t\t\t").append("IntListCodec.encode(").append(classInstance).append("." + field.getName() + "())");
            }
            else{
                parameters.append(",\n\t\t\t\t\t\t").append(classInstance).append("." + field.getName() + "()");
            }
        }
        
        //For every field above it adds a comma and space afterwards, this should be case for every field except the last one so we truncate below after everythign
        sql.setLength(sql.length() - 2);
        placeholders.setLength(placeholders.length() - 2);

        //Closes the sql statement
        placeholders.append(")\"");

        String result = javaFunc + sql.toString() + placeholders.toString() + parameters.toString() + "\n\t\t\t\t).get(0);\n\t\t}\n";
        
        return result;
    }

    public static String generateGetFunc(Class<?> clazz) {
        //Below are the several different styles used in making a repo file
        String tableName = clazz.getAnnotation(Table.class).value(); //units
        String className = clazz.getSimpleName(); //Units
        String classNameSingular = pluralToSingular(className); //Unit
        
        //Function header
        String javaFunc = "\t\tpublic static " + className + " get" + classNameSingular + "ById(int id) throws SQLException {\n\t\t\t\treturn Dao.query(\n";
        
        String sql = "\t\t\t\t\t\t\"SELECT * FROM " + tableName + " WHERE id = ?\",\n";
        
        StringBuilder lambda = new StringBuilder("\t\t\t\t\t\trs -> new "+ className +"(\n");
        
        for (Field field : clazz.getDeclaredFields()) {

            if (field.getType().equals(List.class)){
                lambda.append("\t\t\t\t\t\t\t\t").append("IntListCodec.decode(").append("rs.getString(\"").append(field.getName() + "\")),\n");
            }
            else if (field.getType().equals(String.class)){
                lambda.append("\t\t\t\t\t\t\t\t").append("rs.getString(\"").append(field.getName() + "\"),\n");
            }
            else if (field.getType().equals(int.class) || (field.getType().equals(Integer.class))){
                lambda.append("\t\t\t\t\t\t\t\t").append("rs.getInt(\"").append(field.getName() + "\"),\n");
            }
            else if (field.getType().equals(boolean.class) || (field.getType().equals(Boolean.class))){
                lambda.append("\t\t\t\t\t\t\t\t").append("rs.getBoolean(\"").append(field.getName() + "\"),\n");
            }
        }
        
        //For every field above it adds a comma and newling afterwards, this should be case for every field except the last one so we remove the comma below after everythign
        lambda.deleteCharAt(lambda.length()-2);

        //Closes the lambda statement
        lambda.append("\t\t\t\t\t\t),\n");

        String result = javaFunc + sql + lambda.toString() + "\t\t\t\t\t\tid\n" + "\t\t\t\t).stream().findFirst().orElse(null);\n\t\t}\n";
        
        return result;
    }

    public static String generateGetAllFunc(Class<?> clazz) {
        //Below are the several different styles used in making a repo file
        String tableName = clazz.getAnnotation(Table.class).value(); //units
        String className = clazz.getSimpleName(); //Units
        
        //Function header
        String javaFunc = "\t\tpublic static List<" + className + "> getAll" + className + "() throws SQLException {\n\t\t\t\treturn Dao.query(\n";
        
        String sql = "\t\t\t\t\t\t\"SELECT * FROM " + tableName + "\",\n";
        
        StringBuilder lambda = new StringBuilder("\t\t\t\t\t\trs -> new "+ className +"(\n");
        
        for (Field field : clazz.getDeclaredFields()) {

            if (field.getType().equals(List.class)){
                lambda.append("\t\t\t\t\t\t\t\t").append("IntListCodec.decode(").append("rs.getString(\"").append(field.getName() + "\")),\n");
            }
            else if (field.getType().equals(String.class)){
                lambda.append("\t\t\t\t\t\t\t\t").append("rs.getString(\"").append(field.getName() + "\"),\n");
            }
            else if (field.getType().equals(int.class) || (field.getType().equals(Integer.class))){
                lambda.append("\t\t\t\t\t\t\t\t").append("rs.getInt(\"").append(field.getName() + "\"),\n");
            }
            else if (field.getType().equals(boolean.class) || (field.getType().equals(Boolean.class))){
                lambda.append("\t\t\t\t\t\t\t\t").append("rs.getBoolean(\"").append(field.getName() + "\"),\n");
            }
        }
        
        //For every field above it adds a comma and newling afterwards, this should be case for every field except the last one so we remove the comma below after everythign
        lambda.deleteCharAt(lambda.length()-2);

        //Closes the lambda statement
        lambda.append("\t\t\t\t\t\t)");

        String result = javaFunc + sql + lambda.toString() + "\t\t\t\t\t\t\n" + "\t\t\t\t);\n\t\t}\n";
        
        return result;
    }

    public static String generateUpdateFunc(Class<?> clazz) {
        //Below are the several different styles used in making a repo file
        String tableName = clazz.getAnnotation(Table.class).value(); //units
        String className = clazz.getSimpleName(); //Units
        String classNameSingular = pluralToSingular(className); //Unit
        String classInstance = classNameSingular.toLowerCase(); //unit
        
        //Function header
        String javaFunc = "\t\tpublic static void update" + classNameSingular + "(" + className + " " + classInstance + ") throws SQLException {\n\t\t\t\tDao.update(\n";
        
        StringBuilder sql = new StringBuilder("\t\t\t\t\t\t\"UPDATE " + tableName + " SET ");
        StringBuilder placeholders = new StringBuilder();
        StringBuilder parameters = new StringBuilder();
        
        for (Field field : clazz.getDeclaredFields()) {
            if (field.getName().equals("id")) { continue; }
            //Id does not need an input for sql

            placeholders.append(field.getName() + " = ?, ");

            //Assumption is that if there is an list we are using it for integers
            if (field.getType().equals(List.class)){
                parameters.append(",\n\t\t\t\t\t\t").append("IntListCodec.encode(").append(classInstance).append("." + field.getName() + "())");
            }
            else{
                parameters.append(",\n\t\t\t\t\t\t").append(classInstance).append("." + field.getName() + "()");
            }
        }

        //For every field above it adds a comma and space afterwards, this should be case for every field except the last one so we truncate below after everythign
        placeholders.setLength(placeholders.length() - 2);

        //Id is a common part of all models so we always put it at the end
        placeholders.append(" WHERE id = ?\"");
        parameters.append(",\n\t\t\t\t\t\t").append(classInstance).append(".id()");

        String result = javaFunc + sql.toString() + placeholders.toString() + parameters.toString() + "\n\t\t\t\t);\n\t\t}\n";
        
        return result;
    }

    public static String generateDeleteFunc(Class<?> clazz) {
        //Below are the several different styles used in making a repo file
        String tableName = clazz.getAnnotation(Table.class).value(); //units
        String className = clazz.getSimpleName(); //Units
        String classNameSingular = pluralToSingular(className); //Unit
        String classInstance = classNameSingular.toLowerCase(); //unit
        
        //Function header
        String javaFunc = "\t\tpublic static void delete" + classNameSingular + "(" + className + " " + classInstance + ") throws SQLException {\n\t\t\t\tDao.update(\n";
        
        StringBuilder sql = new StringBuilder("\t\t\t\t\t\t\"DELETE FROM " + tableName);
        StringBuilder placeholders = new StringBuilder();
        StringBuilder parameters = new StringBuilder();

        //Id is a common part of all models so we always put it at the end
        placeholders.append(" WHERE id = ?\"");
        parameters.append(",\n\t\t\t\t\t\t").append(classInstance).append(".id()");

        String result = javaFunc + sql.toString() + placeholders.toString() + parameters.toString() + "\n\t\t\t\t);\n\t\t}\n";
        
        return result;
    }

    public static String generateCrudCode(Class<?> clazz) throws Exception {

        StringBuilder fullJava = new StringBuilder();
        fullJava.append("//-- Auto Generated Java File --").append("\n");
        
        fullJava.append("package eecs2311.group2.wh40k_easycombat.repository;").append("\n\n");

        fullJava.append("import eecs2311.group2.wh40k_easycombat.db.Dao;").append("\n");
        //fullJava.append("import eecs2311.group2.wh40k_easycombat.db.Tx;").append("\n");
        fullJava.append("import eecs2311.group2.wh40k_easycombat.model.").append(clazz.getSimpleName()).append(";").append("\n");
        //fullJava.append("import eecs2311.group2.wh40k_easycombat.util.StringListCodec;").append("\n");
        fullJava.append("import eecs2311.group2.wh40k_easycombat.util.IntListCodec;").append("\n\n");

        //fullJava.append("import java.sql.Connection;").append("\n");
        fullJava.append("import java.util.List;").append("\n");
        fullJava.append("import java.sql.SQLException;").append("\n\n");

        fullJava.append("@SuppressWarnings(\"unused\")").append("\n");
        fullJava.append("public class ").append(pluralToSingular(clazz.getSimpleName())).append("Repository {").append("\n");

        fullJava.append(generateAddFunc(clazz));
        fullJava.append(generateGetFunc(clazz));
        fullJava.append(generateGetAllFunc(clazz));
        fullJava.append(generateUpdateFunc(clazz));
        fullJava.append(generateDeleteFunc(clazz));

        fullJava.append("}");
        

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
