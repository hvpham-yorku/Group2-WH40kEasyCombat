package eecs2311.group2.wh40k_easycombat.util.populator;

import java.sql.Connection;
import java.util.List;

@FunctionalInterface
public interface CsvHandler {
    void handle(Connection conn, List<String[]> rows) throws Exception;
}