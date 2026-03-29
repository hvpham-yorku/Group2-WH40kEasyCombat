package eecs2311.group2.wh40k_easycombat.service.vm;

import eecs2311.group2.wh40k_easycombat.service.vm.expr.*;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ExpressionParser {
    private static final Pattern TOKEN_PATTERN = Pattern.compile(
            "\"(?:\\\\.|[^\"\\\\])*\"|'(?:\\\\.|[^'\\\\])*'|>=|<=|==|\\|\\||&&|[()+\\-*/!<>]|[^\\s()+\\-*/!<>=|]+"
    );

    private final List<String> tokens;
    private int pos = 0;

    public ExpressionParser(String input) {
        this.tokens = tokenize(input);
    }

    public Expression parse() {
        return parseLogicalOr();
    }

    private Expression parseLogicalOr() {
        Expression left = parseLogicalAnd();
        while (match("||")) {
            Expression right = parseLogicalAnd();
            left = new LogicalExpr(left, right, OpCode.OR);
        }
        return left;
    }

    private Expression parseLogicalAnd() {
        Expression left = parseComparison();
        while (match("&&")) {
            Expression right = parseComparison();
            left = new LogicalExpr(left, right, OpCode.AND);
        }
        return left;
    }

    private Expression parseComparison() {
        Expression left = parseAdditive();
        String[] ops = {">=", ">", "<=", "<", "=="};
        String opStr;


        if ((opStr = matchAny(ops)) != null) {
            Expression right = parseAdditive();
            left = new ComparisonExpr(left, right, parseOp(opStr));
        }

        return left;
    }

    private Expression parseAdditive() {
        Expression left = parseMultiplicative();
        while (true) {
            if (match("+")) left = new ArithmeticExpr(left, parseMultiplicative(), OpCode.ADD);
            else if (match("-")) left = new ArithmeticExpr(left, parseMultiplicative(), OpCode.SUB);
            else break;
        }
        return left;
    }

    private Expression parseMultiplicative() {
        Expression left = parseUnary();
        while (true) {
            if (match("*")) left = new ArithmeticExpr(left, parseUnary(), OpCode.MUL);
            else if (match("/")) left = new ArithmeticExpr(left, parseUnary(), OpCode.DIV);
            else break;
        }
        return left;
    }

    private Expression parseUnary() {
        if (match("!")) return new UnaryExpr(parseUnary(), OpCode.NOT);
        if (match("-")) return new UnaryExpr(parseUnary(), OpCode.NEG);
        return parsePrimary();
    }

    private Expression parsePrimary() {
        String token = tokens.get(pos++);

        if (token.equalsIgnoreCase("true")) return new ConstExpr(true);
        if (token.equalsIgnoreCase("false")) return new ConstExpr(false);
        if (isQuotedString(token)) return new ConstExpr(unescapeQuotedString(token));

        if (token.matches("\\d+")) return new ConstExpr(Integer.parseInt(token));

        if (token.equals("(")) {
            Expression expr = parse();
            match(")");
            return expr;
        }

        if (token.contains(".")) {
            String[] parts = token.split("\\.");
            return new PropertyExpr(parts[0], parts[1]);
        }

        return new VarExpr(token);
    }

    // tools
    private boolean match(String expected) {
        if (pos < tokens.size() && tokens.get(pos).equalsIgnoreCase(expected)) {
            pos++;
            return true;
        }
        return false;
    }

    private String matchAny(String[] ops) {
        for (String op : ops) {
            if (match(op)) return op;
        }
        return null;
    }

    private List<String> tokenize(String input) {
        List<String> result = new ArrayList<>();
        Matcher matcher = TOKEN_PATTERN.matcher(input == null ? "" : input);
        while (matcher.find()) {
            result.add(matcher.group());
        }
        return result;
    }

    private OpCode parseOp(String op) {
        return switch (op) {
            case ">=" -> OpCode.CMP_GE;
            case ">" -> OpCode.CMP_GT;
            case "<=" -> OpCode.CMP_LE;
            case "<" -> OpCode.CMP_LT;
            case "==" -> OpCode.CMP_EQ;
            default -> throw new RuntimeException("Unknown op: " + op);
        };
    }

    private boolean isQuotedString(String token) {
        return token != null
                && token.length() >= 2
                && ((token.startsWith("\"") && token.endsWith("\""))
                || (token.startsWith("'") && token.endsWith("'")));
    }

    private String unescapeQuotedString(String token) {
        String quoteStripped = token.substring(1, token.length() - 1);
        return quoteStripped
                .replace("\\\"", "\"")
                .replace("\\'", "'")
                .replace("\\\\", "\\");
    }
}
