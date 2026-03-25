package eecs2311.group2.wh40k_easycombat.service.vm;

public class DSLException extends RuntimeException {
    public DSLException(String type, String message, int lineNum, String lineContent) {
        super(String.format("[%s Error at Line %d]: %s\n  --> Source: \"%s\"",
                type, lineNum, message, lineContent));
    }
}