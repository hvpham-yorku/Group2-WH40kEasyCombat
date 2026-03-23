package eecs2311.group2.wh40k_easycombat.service.customizable_calculation_system;

import eecs2311.group2.wh40k_easycombat.service.customizable_calculation_system.expr.*;
import java.util.*;
import java.util.regex.*;

public class RuleCompiler {

    // 1. 基础指令
    private static final Pattern ROLL_RE = Pattern.compile("roll\\s+(.+?)\\s*->\\s*(\\w+)");
    private static final Pattern FILTER_RE = Pattern.compile("filter\\s+(\\w+)\\s+\\((.+?)\\)\\s*->\\s*(\\w+)");
    private static final Pattern COUNT_RE = Pattern.compile("count\\s+(\\w+)\\s*->\\s*(\\w+)");

    // 2. 逻辑控制
    private static final Pattern IF_RE = Pattern.compile("if\\s+(?:\\((.*)\\)|(.*))");

    // 3. 自增自减
    private static final Pattern AUTO_INC_DEC_RE = Pattern.compile("(\\w+)\\s*(\\+\\+|--)");

    // 4. 复合赋值 (+=, -=, *=, /=)
    private static final Pattern COMPOUND_ASSIGN_RE = Pattern.compile("(\\w+)\\s*(\\+=|-=|\\*=|/=|%=)\\s*(.+)");

    // 5. 流式赋值 (expr -> var)，排除掉前面的 roll/filter/count
    private static final Pattern FLOW_ASSIGN_RE = Pattern.compile("(.+?)\\s*->\\s*(\\w+)");

    private static final Pattern WHILE_RE = Pattern.compile("while\\s+(?:\\((.*)\\)|(.*))");

    public CompiledRule compile(String ruleName, String source) {
        List<Instruction> code = new ArrayList<>();
        Deque<Integer> ifStack = new ArrayDeque<>();
        Deque<Integer> whileStack = new ArrayDeque<>();
        String[] lines = source.split("\\r?\\n");

        for (String line : lines) {
            line = line.trim();
            if (line.isEmpty() || line.startsWith("#")) continue;

            // --- 优先级分发开始 ---

            // 1. 基础指令 (带关键字)
            if (line.startsWith("roll")) {
                handleRoll(line, code);
            } else if (line.startsWith("filter")) {
                handleFilter(line, code);
            } else if (line.startsWith("count")) {
                handleCount(line, code);
            }
            // 2. 逻辑控制
            else if (line.startsWith("if")) {
                handleIf(line, code, ifStack);
            } else if (line.equals("endif")) {
                handleEndIf(code, ifStack);
            }
            else if (line.startsWith("while")) {
                handleWhile(line, code, whileStack);
            } else if (line.equals("endwhile")) {
                handleEndWhile(code, whileStack);
            }
            // 3. 自增自减 (hits++)
            else if (AUTO_INC_DEC_RE.matcher(line).matches()) {
                handleAutoIncDec(line, code);
            }
            // 4. 复合赋值 (hits += 1)
            else if (COMPOUND_ASSIGN_RE.matcher(line).matches()) {
                handleCompoundAssignment(line, code);
            }
            // 5. 通用流式赋值 (10 -> hits)
            else if (FLOW_ASSIGN_RE.matcher(line).matches()) {
                handleFlowAssignment(line, code);
            }
            // --- 优先级分发结束 ---
        }
        return new CompiledRule(ruleName, code);
    }

    private void handleRoll(String line, List<Instruction> code) {
        Matcher m = ROLL_RE.matcher(line);
        if (m.find()) {
            Instruction ins = new Instruction(OpCode.ROLL_POOL);
            ins.expr = new ExpressionParser(m.group(1)).parse();
            code.add(ins);
            code.add(new Instruction(OpCode.STORE) {{ name = m.group(2); }});
        }
    }

    private void handleFilter(String line, List<Instruction> code) {
        Matcher m = FILTER_RE.matcher(line);
        if (m.find()) {
            Instruction ins = new Instruction(OpCode.FILTER_POOL);
            ins.poolName = m.group(1);
            ins.expr = new ExpressionParser(m.group(2)).parse();
            code.add(ins);
            code.add(new Instruction(OpCode.STORE) {{ name = m.group(3); }});
        }
    }

    private void handleCount(String line, List<Instruction> code) {
        Matcher m = COUNT_RE.matcher(line);
        if (m.find()) {
            code.add(new Instruction(OpCode.COUNT_POOL) {{ poolName = m.group(1); }});
            code.add(new Instruction(OpCode.STORE) {{ name = m.group(2); }});
        }
    }

    private void handleIf(String line, List<Instruction> code, Deque<Integer> stack) {
        Matcher m = IF_RE.matcher(line);
        if (m.find()) {
            String exprStr = (m.group(1) != null) ? m.group(1) : m.group(2);
            Instruction eval = new Instruction(OpCode.EVAL_EXPR);
            eval.expr = new ExpressionParser(exprStr.trim()).parse();
            code.add(eval);

            Instruction jmp = new Instruction(OpCode.JMP_IF_FALSE);
            code.add(jmp);
            stack.push(code.size() - 1);
        }
    }

    private void handleEndIf(List<Instruction> code, Deque<Integer> stack) {
        if (stack.isEmpty()) {
            throw new RuntimeException("Compilation error: 'endif' without 'if'.");
        }
        int jmpIdx = stack.pop();
        code.get(jmpIdx).target = code.size();
    }

    private void handleWhile(String line, List<Instruction> code, Deque<Integer> stack) {
        Matcher m = WHILE_RE.matcher(line);
        if (m.find()) {
            String exprStr = (m.group(1) != null) ? m.group(1) : m.group(2);

            // 1. 记录循环判定的起点 PC
            int loopStart = code.size();

            // 2. 编译判定表达式
            Instruction eval = new Instruction(OpCode.EVAL_EXPR);
            eval.expr = new ExpressionParser(exprStr.trim()).parse();
            code.add(eval);

            // 3. 判定失败则跳出（目标暂空）
            Instruction jmpFalse = new Instruction(OpCode.JMP_IF_FALSE);
            code.add(jmpFalse);

            // 压入栈：我们需要记住 loopStart (回跳位置) 和 jmpFalse 的索引 (回填位置)
            // 这里可以用一个简单的技巧：存两个值，或者存一个自定义对象
            stack.push(loopStart);
            stack.push(code.size() - 1);
        }
    }

    private void handleEndWhile(List<Instruction> code, Deque<Integer> stack) {
        if (stack.size() < 2) throw new RuntimeException("Missing while for endwhile");

        int jmpFalseIdx = stack.pop();
        int loopStart = stack.pop();

        // 1. 添加无条件跳转，回到 loopStart 重新判定
        Instruction gotoStart = new Instruction(OpCode.GOTO);
        gotoStart.target = loopStart;
        code.add(gotoStart);

        // 2. 回填：如果判定失败，跳到 endwhile 之后的位置 (当前 code.size())
        code.get(jmpFalseIdx).target = code.size();
    }

    private void handleCompoundAssignment(String line, List<Instruction> code) {
        Matcher m = COMPOUND_ASSIGN_RE.matcher(line);
        if (m.find()) {
            String varName = m.group(1);
            String op = m.group(2); // "+=", "-=", etc.
            String exprPart = m.group(3);

            String mathOp = op.substring(0, 1);
            // 展开为: varName = varName mathOp (exprPart)
            String synthesizedExpr = varName + " " + mathOp + " (" + exprPart + ")";

            Instruction eval = new Instruction(OpCode.EVAL_EXPR);
            eval.expr = new ExpressionParser(synthesizedExpr).parse();
            code.add(eval);

            Instruction store = new Instruction(OpCode.STORE);
            store.name = varName;
            code.add(store);
        }
    }

    private void handleFlowAssignment(String line, List<Instruction> code) {
        Matcher m = FLOW_ASSIGN_RE.matcher(line);
        if (m.find()) {
            String exprPart = m.group(1);
            String varName = m.group(2);

            Instruction eval = new Instruction(OpCode.EVAL_EXPR);
            eval.expr = new ExpressionParser(exprPart.trim()).parse();
            code.add(eval);

            Instruction store = new Instruction(OpCode.STORE);
            store.name = varName;
            code.add(store);
        }
    }

    private void handleAutoIncDec(String line, List<Instruction> code) {
        Matcher m = AUTO_INC_DEC_RE.matcher(line);
        if (m.find()) {
            String varName = m.group(1);
            String operator = m.group(2);
            String mathOp = operator.equals("++") ? "+" : "-";
            String synthesizedExpr = varName + " " + mathOp + " 1";

            Instruction eval = new Instruction(OpCode.EVAL_EXPR);
            eval.expr = new ExpressionParser(synthesizedExpr).parse();
            code.add(eval);

            Instruction store = new Instruction(OpCode.STORE);
            store.name = varName;
            code.add(store);
        }
    }
}