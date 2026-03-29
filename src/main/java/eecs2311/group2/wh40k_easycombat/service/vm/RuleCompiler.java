package eecs2311.group2.wh40k_easycombat.service.vm;

import java.util.*;
import java.util.regex.*;

public class RuleCompiler {

    // --- 1. Basic Pool Operations ---

    /**
     * Matches: roll [expression] -> [var]
     * Example: roll 10 -> hit_pool (Generates 10 D6 dice)
     */
    private static final Pattern ROLL_RE = Pattern.compile("^roll\\s+(.+?)\\s*->\\s*(\\w+)$");

    /**
     * Matches: filter [source] ([condition]) -> [target]
     * Example: filter hit_pool (@ >= 4) -> successes (Keeps dice with value 4+)
     */
    private static final Pattern FILTER_RE = Pattern.compile("^filter\\s+(\\w+)\\s+\\((.+?)\\)\\s*->\\s*(\\w+)$");

    /**
     * Matches: count [pool] -> [var]
     * Example: count successes -> num_hits (Stores the number of dice in a variable)
     */
    private static final Pattern COUNT_RE = Pattern.compile("^count\\s+(\\w+)\\s*->\\s*(\\w+)$");

    /**
     * Matches: reroll [source] ([condition]) -> [target]
     * Example: reroll hits (@ == 1) -> new_hits (Rerolls 1s and replaces them)
     */
    private static final Pattern REROLL_RE = Pattern.compile("^reroll\\s+(\\w+)\\s+\\((.+?)\\)\\s*->\\s*(\\w+)$");

    /**
     * Matches: keep_(high|low) [source] [count] -> [target]
     * Example: keep_high damage_pool 2 -> final_dmg (Keeps only the top 2 dice)
     */
    private static final Pattern KEEP_PATTERN = Pattern.compile("^keep_(high|low)\\s+(\\w+)\\s+(\\d+)\\s*->\\s*(\\w+)$");

    /**
     * Matches: fixed [values] -> [var]
     * Example: fixed 1 2 6 -> miracle_dice (Creates a pool with specific values)
     */
    private static final Pattern FIXED_RE = Pattern.compile("^fixed\\s+(.+?)\\s*->\\s*(\\w+)$");

    /**
     * Matches: merge [pool1 pool2 ...] -> [target]
     * Example: merge bolters meltas -> total_attacks (Combines multiple pools)
     */
    private static final Pattern MULTI_MERGE_RE = Pattern.compile("^merge\\s+((?:\\w+\\s+)+)->\\s*(\\w+)$");

    /**
     * Matches: sum [pool] -> [var]
     * Example: sum damage_pool -> total_damage (Adds all dice values together)
     */
    private static final Pattern SUM_RE = Pattern.compile("^sum\\s+(\\w+)\\s*->\\s*(\\w+)$");


// --- 2. Logical Control Flow ---

    /**
     * Matches: if ([condition]) or if [condition]
     * Example: if (total_damage > 10) ... endif
     */
    private static final Pattern IF_RE = Pattern.compile("^if\\s+(?:\\((.*)\\)|(.*))$");

    /**
     * Matches: while ([condition]) or while [condition]
     * Example: while (hp > 0) ... endwhile
     */
    private static final Pattern WHILE_RE = Pattern.compile("^while\\s+(?:\\((.*)\\)|(.*))$");


// --- 3. Arithmetic & Variable Assignments ---

    /**
     * Matches: [var]++ or [var]--
     * Example: wounds_inflicted++ (Increments variable by 1)
     */
    private static final Pattern AUTO_INC_DEC_RE = Pattern.compile("^\\s*(\\w+)\\s*(\\+\\+|--)\\s*$");

    /**
     * Matches: [var] [operator] [expression]
     * Example: hp -= 3 (Subtracts 3 from variable hp)
     */
    private static final Pattern COMPOUND_ASSIGN_RE = Pattern.compile("^(\\w+)\\s*(\\+=|-=|\\*=|/=|%=)\\s*(.+)$");

    /**
     * Matches: [expression] -> [var]
     * Example: 10 + modifier -> final_score (General flow-based assignment)
     */
    private static final Pattern FLOW_ASSIGN_RE = Pattern.compile("^(.+?)\\s*->\\s*(\\w+)$");


// --- 4. Debugging & Utilities ---

    /**
     * Matches: print [var]
     * Example: print total_damage (Logs the variable value to console)
     */
    private static final Pattern PRINT_RE = Pattern.compile("^print\\s+(\\w+)$");


    public CompiledRule compile(String ruleName, String source) {
        List<Instruction> code = new ArrayList<>();
        // Stacks now store line numbers for better error reporting on unclosed blocks
        Deque<Integer> ifStack = new ArrayDeque<>();
        Deque<Integer> whileStack = new ArrayDeque<>();
        String[] lines = source.split("\\r?\\n");

        for (int i = 0; i < lines.length; i++) {
            String originalLine = lines[i];
            String line = originalLine.trim();
            int lineNum = i + 1; // 1-based line numbering

            if (line.isEmpty() || line.startsWith("#")) continue;

            try {
                // --- Dispatch with Line Numbering ---
                if (line.equals("endif")) {
                    handleEndIf(code, ifStack, lineNum, originalLine);
                } else if (line.equals("endwhile")) {
                    handleEndWhile(code, whileStack, lineNum, originalLine);
                } else if (IF_RE.matcher(line).matches()) {
                    handleIf(line, code, ifStack, lineNum);
                } else if (WHILE_RE.matcher(line).matches()) {
                    handleWhile(line, code, whileStack, lineNum);
                } else if (ROLL_RE.matcher(line).matches()) {
                    handleRoll(line, code, lineNum);
                } else if (REROLL_RE.matcher(line).matches()) {
                    handleReroll(line, code, lineNum);
                } else if (FILTER_RE.matcher(line).matches()) {
                    handleFilter(line, code, lineNum);
                } else if (COUNT_RE.matcher(line).matches()) {
                    handleCount(line, code, lineNum);
                } else if (KEEP_PATTERN.matcher(line).matches()) {
                    handleKeep(line, code, lineNum);
                } else if (FIXED_RE.matcher(line).matches()) {
                    handleFixed(line, code, lineNum);
                } else if (MULTI_MERGE_RE.matcher(line).matches()) {
                    handleMultiMerge(line, code, lineNum);
                } else if (SUM_RE.matcher(line).matches()) {
                    handleSum(line, code, lineNum);
                } else if (PRINT_RE.matcher(line).matches()) {
                    handlePrint(line, code, lineNum);
                } else if (AUTO_INC_DEC_RE.matcher(line).matches()) {
                    handleAutoIncDec(line, code, lineNum);
                } else if (COMPOUND_ASSIGN_RE.matcher(line).matches()) {
                    handleCompoundAssignment(line, code, lineNum);
                } else if (FLOW_ASSIGN_RE.matcher(line).matches()) {
                    handleFlowAssignment(line, code, lineNum);
                } else {
                    throw new DSLException("Syntax", "Unrecognized command", lineNum, originalLine);
                }
            } catch (DSLException e) {
                throw e; // Re-throw our custom exceptions
            } catch (Exception e) {
                // Wrap any unexpected parser or logic errors with line info
                throw new DSLException("Compiler", e.getMessage(), lineNum, originalLine);
            }
        }

        // Final validation for unclosed blocks
        if (!ifStack.isEmpty()) {
            throw new DSLException("Syntax", "Unclosed 'if' block", -1, "Check for missing 'endif'");
        }
        if (whileStack.size() >= 2) { // whileStack stores 2 entries per loop
            throw new DSLException("Syntax", "Unclosed 'while' block", -1, "Check for missing 'endwhile'");
        }

        return new CompiledRule(ruleName, code);
    }

    /**
     * Handles: roll [expression] -> [varName]
     * Generates a ROLL_POOL instruction followed by a STORE.
     */
    private void handleRoll(String line, List<Instruction> code, int lineNum) {
        Matcher m = ROLL_RE.matcher(line);
        if (m.find()) {
            Instruction ins = new Instruction(OpCode.ROLL_POOL);
            ins.lineNum = lineNum;
            ins.lineLiteral = line;
            try {
                ins.expr = new ExpressionParser(m.group(1)).parse();
            } catch (Exception e) {
                throw new DSLException("Expression", "Invalid roll expression: " + e.getMessage(), lineNum, line);
            }
            code.add(ins);

            Instruction store = new Instruction(OpCode.STORE);
            store.lineNum = lineNum;
            store.lineLiteral = line;
            store.name = m.group(2);
            code.add(store);
        }
    }

    /**
     * Handles: filter [poolName] ([condition]) -> [targetVar]
     * Filters dice in a pool based on a condition.
     */
    private void handleFilter(String line, List<Instruction> code, int lineNum) {
        Matcher m = FILTER_RE.matcher(line);
        if (m.find()) {
            Instruction ins = new Instruction(OpCode.FILTER_POOL);
            ins.lineNum = lineNum;
            ins.lineLiteral = line;
            ins.poolName = m.group(1);
            ins.expr = new ExpressionParser(m.group(2)).parse();
            code.add(ins);

            Instruction store = new Instruction(OpCode.STORE);
            store.lineNum = lineNum;
            store.lineLiteral = line;
            store.name = m.group(3);
            code.add(store);
        }
    }

    /**
     * Handles: reroll [poolName] ([condition]) -> [targetVar]
     * Rerolls specific dice within a pool.
     */
    private void handleReroll(String line, List<Instruction> code, int lineNum) {
        Matcher m = REROLL_RE.matcher(line);
        if (m.find()) {
            Instruction ins = new Instruction(OpCode.REROLL_POOL);
            ins.lineNum = lineNum;
            ins.lineLiteral = line;
            ins.poolName = m.group(1);
            try {
                ins.expr = new ExpressionParser(m.group(2)).parse();
            } catch (Exception e) {
                throw new DSLException("Expression", "Invalid reroll condition: " + e.getMessage(), lineNum, line);
            }
            code.add(ins);

            Instruction store = new Instruction(OpCode.STORE);
            store.lineNum = lineNum;
            store.lineLiteral = line;
            store.name = m.group(3);
            code.add(store);
        }
    }

    /**
     * Handles: count [poolName] -> [targetVar]
     * Pushes the size of the pool to the stack.
     */
    private void handleCount(String line, List<Instruction> code, int lineNum) {
        Matcher m = COUNT_RE.matcher(line);
        if (m.find()) {
            String poolName = m.group(1);
            String targetVar = m.group(2);

            // Instruction A: Count pool size and push to stack
            Instruction count = new Instruction(OpCode.COUNT_POOL);
            count.lineNum = lineNum;
            count.lineLiteral = line;
            count.poolName = poolName;
            code.add(count);

            // Instruction B: Store the count in the target variable
            Instruction store = new Instruction(OpCode.STORE);
            store.lineNum = lineNum;
            store.lineLiteral = line;
            store.name = targetVar;
            code.add(store);
        } else {
            throw new DSLException("Syntax", "Invalid 'count' command format", lineNum, line);
        }
    }

    /**
     * Handles: fixed [v1 v2...] -> [targetVar]
     * Creates a pool with manually specified dice values.
     */
    private void handleFixed(String line, List<Instruction> code, int lineNum) {
        Matcher m = FIXED_RE.matcher(line);
        if (m.find()) {
            String values = m.group(1).trim();
            if (values.isEmpty()) {
                throw new DSLException("Syntax", "Fixed pool values cannot be empty", lineNum, line);
            }

            Instruction ins = new Instruction(OpCode.FIXED_POOL);
            ins.lineNum = lineNum;
            ins.lineLiteral = line;
            ins.name = values; // VM will parse this space-separated string
            code.add(ins);

            Instruction store = new Instruction(OpCode.STORE);
            store.lineNum = lineNum;
            store.lineLiteral = line;
            store.name = m.group(2);
            code.add(store);
        }
    }

    /**
     * Handles: if ([condition])
     * Pushes current instruction index to stack for later JMP backfilling.
     */
    private void handleIf(String line, List<Instruction> code, Deque<Integer> stack, int lineNum) {
        Matcher m = IF_RE.matcher(line);
        if (m.find()) {
            String exprStr = (m.group(1) != null) ? m.group(1) : m.group(2);
            Instruction eval = new Instruction(OpCode.EVAL_EXPR);
            eval.lineNum = lineNum;
            eval.lineLiteral = line;
            try {
                eval.expr = new ExpressionParser(exprStr.trim()).parse();
            } catch (Exception e) {
                throw new DSLException("Expression", "Invalid IF condition: " + e.getMessage(), lineNum, line);
            }
            code.add(eval);

            Instruction jmp = new Instruction(OpCode.JMP_IF_FALSE);
            jmp.lineNum = lineNum;
            jmp.lineLiteral = line;
            code.add(jmp);

            // Push current instruction index for backfilling target
            // and lineNum for error tracking
            stack.push(code.size() - 1);
        }
    }

    /**
     * Handles: endif
     * Pops the last 'if' JMP index and sets its target to current code size.
     */
    private void handleEndIf(List<Instruction> code, Deque<Integer> stack, int lineNum, String src) {
        if (stack.isEmpty()) {
            throw new DSLException("Syntax", "Unexpected 'endif' found without matching 'if'", lineNum, src);
        }
        int jmpIdx = stack.pop();
        code.get(jmpIdx).target = code.size();
    }

    /**
     * Handles: while ([condition])
     * Stores start PC for looping and JMP index for breaking.
     */
    private void handleWhile(String line, List<Instruction> code, Deque<Integer> stack, int lineNum) {
        Matcher m = WHILE_RE.matcher(line);
        if (m.find()) {
            String exprStr = (m.group(1) != null) ? m.group(1) : m.group(2);
            int loopStart = code.size();

            Instruction eval = new Instruction(OpCode.EVAL_EXPR);
            eval.lineNum = lineNum;
            eval.lineLiteral = line;
            try {
                eval.expr = new ExpressionParser(exprStr.trim()).parse();
            } catch (Exception e) {
                throw new DSLException("Expression", "Invalid WHILE condition: " + e.getMessage(), lineNum, line);
            }
            code.add(eval);

            Instruction jmpFalse = new Instruction(OpCode.JMP_IF_FALSE);
            jmpFalse.lineNum = lineNum;
            jmpFalse.lineLiteral = line;
            code.add(jmpFalse);

            stack.push(loopStart);
            stack.push(code.size() - 1);
        }
    }

    /**
     * Handles: endwhile
     * Adds GOTO back to start and fills the while-break jump target.
     */
    private void handleEndWhile(List<Instruction> code, Deque<Integer> stack, int lineNum, String src) {
        if (stack.size() < 2) {
            throw new DSLException("Syntax", "Unexpected 'endwhile' found without matching 'while'", lineNum, src);
        }
        int jmpFalseIdx = stack.pop();
        int loopStart = stack.pop();

        Instruction gotoStart = new Instruction(OpCode.GOTO);
        gotoStart.lineNum = lineNum;
        gotoStart.lineLiteral = src;
        gotoStart.target = loopStart;
        code.add(gotoStart);

        code.get(jmpFalseIdx).target = code.size();
    }

    /**
     * Handles: var += expr, var -= expr, etc.
     * Synthesizes a full expression: var = var + (expr).
     */
    private void handleCompoundAssignment(String line, List<Instruction> code, int lineNum) {
        Matcher m = COMPOUND_ASSIGN_RE.matcher(line);
        if (m.find()) {
            String varName = m.group(1);
            String op = m.group(2);
            String exprPart = m.group(3);

            String mathOp = op.substring(0, 1);
            String synthesizedExpr = varName + " " + mathOp + " (" + exprPart + ")";

            Instruction eval = new Instruction(OpCode.EVAL_EXPR);
            eval.lineNum = lineNum;
            eval.lineLiteral = line;
            try {
                eval.expr = new ExpressionParser(synthesizedExpr).parse();
            } catch (Exception e) {
                throw new DSLException("Expression", "Malformed assignment expression", lineNum, line);
            }
            code.add(eval);

            Instruction store = new Instruction(OpCode.STORE);
            store.lineNum = lineNum;
            store.lineLiteral = line;
            store.name = varName;
            code.add(store);
        }
    }

    /**
     * Handles: [expression] -> [varName]
     * Evaluates a raw expression and stores the result.
     */
    private void handleFlowAssignment(String line, List<Instruction> code, int lineNum) {
        Matcher m = FLOW_ASSIGN_RE.matcher(line);
        if (m.find()) {
            Instruction eval = new Instruction(OpCode.EVAL_EXPR);
            eval.lineNum = lineNum;
            eval.lineLiteral = line;
            try {
                eval.expr = new ExpressionParser(m.group(1).trim()).parse();
            } catch (Exception e) {
                throw new DSLException("Expression", "Invalid expression logic", lineNum, line);
            }
            code.add(eval);

            Instruction store = new Instruction(OpCode.STORE);
            store.lineNum = lineNum;
            store.lineLiteral = line;
            store.name = m.group(2);
            code.add(store);
        }
    }

    /**
     * Handles: var++ or var--
     * Synthesizes: var = var + 1 or var = var - 1.
     */
    private void handleAutoIncDec(String line, List<Instruction> code, int lineNum) {
        Matcher m = AUTO_INC_DEC_RE.matcher(line);
        if (m.find()) {
            String varName = m.group(1);
            String op = m.group(2);
            String mathOp = op.equals("++") ? "+" : "-";
            // Logic: var = var + 1
            String synthesizedExpr = varName + " " + mathOp + " 1";

            Instruction eval = new Instruction(OpCode.EVAL_EXPR);
            eval.lineNum = lineNum;
            eval.lineLiteral = line;
            try {
                eval.expr = new ExpressionParser(synthesizedExpr).parse();
            } catch (Exception e) {
                throw new DSLException("Internal", "Failed to compile auto-increment", lineNum, line);
            }
            code.add(eval);

            Instruction store = new Instruction(OpCode.STORE);
            store.lineNum = lineNum;
            store.lineLiteral = line;
            store.name = varName;
            code.add(store);
        }
    }

    /**
     * Handles: keep_high [pool] [n] -> [target]
     * Selects top N dice from the pool.
     */
    private void handleKeep(String line, List<Instruction> code, int lineNum) {
        Matcher m = KEEP_PATTERN.matcher(line);
        if (m.find()) {
            try {
                OpCode op = m.group(1).equals("high") ? OpCode.KEEP_HIGH : OpCode.KEEP_LOW;
                Instruction inst = new Instruction(op);
                inst.lineNum = lineNum;
                inst.lineLiteral = line;
                inst.poolName = m.group(2);
                inst.value = Integer.parseInt(m.group(3));
                code.add(inst);

                Instruction store = new Instruction(OpCode.STORE);
                store.lineNum = lineNum;
                store.lineLiteral = line;
                store.name = m.group(4);
                code.add(store);
            } catch (NumberFormatException e) {
                throw new DSLException("Syntax", "Keep count must be a valid integer", lineNum, line);
            }
        }
    }

    /**
     * Handles: merge [p1 p2...] -> [target]
     * Combines multiple pools into one result.
     */
    private void handleMultiMerge(String line, List<Instruction> code, int lineNum) {
        Matcher m = MULTI_MERGE_RE.matcher(line);
        if (m.find()) {
            String pools = m.group(1).trim();
            if (pools.isEmpty()) {
                throw new DSLException("Syntax", "Merge command requires at least one source pool", lineNum, line);
            }

            Instruction ins = new Instruction(OpCode.MERGE_POOL);
            ins.lineNum = lineNum;
            ins.lineLiteral = line;
            ins.name = pools;
            code.add(ins);

            Instruction store = new Instruction(OpCode.STORE);
            store.lineNum = lineNum;
            store.lineLiteral = line;
            store.name = m.group(2);
            code.add(store);
        }
    }

    /**
     * Handles: sum [poolName] -> [targetVar]
     * Calculates the total value of all dice in a pool.
     */
    private void handleSum(String line, List<Instruction> code, int lineNum) {
        Matcher m = SUM_RE.matcher(line);
        if (m.find()) {
            String poolName = m.group(1);
            String targetVar = m.group(2);

            // Instruction A: Calculate the sum and push to stack
            Instruction ins = new Instruction(OpCode.SUM_POOL);
            ins.lineNum = lineNum;
            ins.lineLiteral = line;
            ins.poolName = poolName;
            code.add(ins);

            // Instruction B: Pop result from stack and store in variable
            Instruction store = new Instruction(OpCode.STORE);
            store.lineNum = lineNum;
            store.lineLiteral = line;
            store.name = targetVar;
            code.add(store);
        } else {
            // This should technically be caught by the dispatcher,
            // but we add a safety check for regex mismatches.
            throw new DSLException("Syntax", "Invalid 'sum' command format", lineNum, line);
        }
    }

    /**
     * Handles: print [varName]
     * Triggers a debug log for the specified variable.
     */
    private void handlePrint(String line, List<Instruction> code, int lineNum) {
        Matcher m = PRINT_RE.matcher(line);
        if (m.find()) {
            String varName = m.group(1);

            Instruction ins = new Instruction(OpCode.PRINT);
            ins.lineNum = lineNum;
            ins.lineLiteral = line;
            ins.name = varName; // The variable name to be printed
            code.add(ins);
        } else {
            throw new DSLException("Syntax", "Invalid 'print' command format", lineNum, line);
        }
    }
}