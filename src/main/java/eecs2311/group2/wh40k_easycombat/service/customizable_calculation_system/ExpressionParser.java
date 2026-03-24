package eecs2311.group2.wh40k_easycombat.service.customizable_calculation_system;

import eecs2311.group2.wh40k_easycombat.service.customizable_calculation_system.expr.*;

import java.util.Arrays;
import java.util.List;

public class ExpressionParser {
    private final List<String> tokens;
    private int pos = 0;

    public ExpressionParser(String input) {
        this.tokens = tokenize(input);
    }

    // 优先级 6: 逻辑 OR
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

    // 优先级 5: 逻辑 AND
    private Expression parseLogicalAnd() {
        Expression left = parseComparison();
        while (match("&&")) {
            Expression right = parseComparison();
            left = new LogicalExpr(left, right, OpCode.AND);
        }
        return left;
    }

    // 优先级 4: 比较运算
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

    // 优先级 3: 加减
    private Expression parseAdditive() {
        Expression left = parseMultiplicative();
        while (true) {
            if (match("+")) left = new ArithmeticExpr(left, parseMultiplicative(), OpCode.ADD);
            else if (match("-")) left = new ArithmeticExpr(left, parseMultiplicative(), OpCode.SUB);
            else break;
        }
        return left;
    }

    // 优先级 2: 乘除
    private Expression parseMultiplicative() {
        Expression left = parseUnary();
        while (true) {
            if (match("*")) left = new ArithmeticExpr(left, parseUnary(), OpCode.MUL);
            else if (match("/")) left = new ArithmeticExpr(left, parseUnary(), OpCode.DIV);
            else break;
        }
        return left;
    }

    // 优先级 1: 一元运算 (!, -)
    private Expression parseUnary() {
        if (match("!")) return new UnaryExpr(parseUnary(), OpCode.NOT);
        if (match("-")) return new UnaryExpr(parseUnary(), OpCode.NEG);
        return parsePrimary();
    }

    // 优先级 0: 原子项 (数字, 变量, 属性, 括号)
    private Expression parsePrimary() {
        String token = tokens.get(pos++);

        // 1. 处理布尔常量 (必须在处理变量之前)
        if (token.equalsIgnoreCase("true")) return new ConstExpr(true);
        if (token.equalsIgnoreCase("false")) return new ConstExpr(false);

        // 2. 处理数字
        if (token.matches("\\d+")) return new ConstExpr(Integer.parseInt(token));

        // 3. 处理括号
        if (token.equals("(")) {
            Expression expr = parse(); // 重新开始逻辑链
            match(")");
            return expr;
        }

        // 4. 处理属性 (attacker.BS)
        if (token.contains(".")) {
            String[] parts = token.split("\\.");
            return new PropertyExpr(parts[0], parts[1]);
        }

        // 5. 剩下的才当做变量名/池名
        return new VarExpr(token);
    }

    // --- 工具方法 ---
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
        // 使用正则在符号前后加空格，然后按空格切分
        String spaced = input.replaceAll("([()+\\-*/!]|>=|<=|==|>|<|AND|OR)", " $1 ");
        return Arrays.asList(spaced.trim().split("\\s+"));
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
}