package eecs2311.group2.wh40k_easycombat.service.vm;

import eecs2311.group2.wh40k_easycombat.service.calculations.DiceService;

import java.util.*;

public class RuleVM {

    public void execute(CompiledRule rule, ExecutionContext ctx) {
        List<Instruction> code = rule.getInstructions();
        Deque<Object> stack = new ArrayDeque<>();
        int pc = 0;

        try {
            while (pc < code.size()) {
                Instruction inst = code.get(pc);

                switch (inst.op) {
                    case EVAL_EXPR -> {
                        stack.push(inst.expr.eval(ctx).orElseThrow(() -> new RuntimeException("Err for EVAL_EXPR")));
                    }

                    case STORE -> {
                        Object val = stack.pop();
                        ctx.getRuleContext().set(inst.name, val);
                    }

                    case LOAD -> stack.push(ctx.getRuleContext().get(inst.name));

                    case JMP_IF_FALSE -> {
                        Object cond = stack.pop();
                        boolean panned;
                        if (cond instanceof Boolean) {
                            panned = (Boolean) cond;
                        } else if (cond instanceof Number) {
                            // 0 -> false, not 0 -> true
                            panned = ((Number) cond).intValue() != 0;
                        } else {
                            panned = (cond != null);
                        }

                        if (!panned) {
                            pc = inst.target;
                            continue;
                        }
                    }

                    case GOTO, JMP -> {
                        pc = inst.target;
                        continue;
                    }

                    case ROLL_POOL -> {
                        int count = (int) inst.expr.eval(ctx).orElse(0);
                        DicePool pool = new DicePool();
                        pool.addList(DiceService.rollNSideDices(count, 6));
                        stack.push(pool);
                    }

                    case FILTER_POOL -> {
                        Object sourceObj = ctx.getValue(inst.poolName);
                        if (!(sourceObj instanceof DicePool src)) {
                            stack.push(new DicePool());
                            break;
                        }

                        DicePool dst = new DicePool();
                        for (int v : src.getDice()) {
                            ctx.setValue("@", v);

                            Optional<Object> evalResult = inst.expr.eval(ctx);

                            if (evalResult.isPresent() && isTrue(evalResult.get())) {
                                dst.add(v);
                            }
                        }

                        ctx.setValue("@", null);

                        stack.push(dst);
                    }

                    case REROLL_POOL -> {
                        DicePool src = (DicePool) ctx.getValue(inst.poolName);
                        DicePool dst = new DicePool();

                        for (int v : src.getDice()) {
                            ctx.setValue("@", v);

                            if (isTrue(inst.expr.eval(ctx).orElse(false))) {
                                List<Integer> newRoll = DiceService.rollNSideDices(1);
                                int newValue = newRoll.getFirst();

                                dst.add(newValue);
                            } else {
                                dst.add(v);
                            }
                        }

                        ctx.setValue("@", null);
                        stack.push(dst);
                    }

                    case KEEP_HIGH, KEEP_LOW -> {
                        Object source = ctx.getValue(inst.poolName);
                        if (!(source instanceof DicePool sourcePool)) {
                            stack.push(new DicePool());
                            break;
                        }

                        DicePool resultPool = new DicePool();
                        List<Integer> diceList = new ArrayList<>(sourcePool.getDice());

                        if (!diceList.isEmpty()) {
                            if (inst.op == OpCode.KEEP_HIGH) {
                                diceList.sort(Collections.reverseOrder());
                            } else {
                                diceList.sort(Integer::compare);
                            }

                            int keepN = Math.min(inst.value, diceList.size());
                            for (int i = 0; i < keepN; i++) {
                                resultPool.add(diceList.get(i));
                            }
                        }

                        stack.push(resultPool);
                    }

                    case MERGE_POOL -> {
                        DicePool resultPool = new DicePool();

                        // inst.name contains "p1 p2 p3"
                        // Split by any whitespace to get individual variable names
                        String[] poolNames = inst.name.split("\\s+");

                        for (String name : poolNames) {
                            if (name.isEmpty()) continue;

                            Object source = ctx.getValue(name);
                            if (source instanceof DicePool dp) {
                                // Extract all dice from the source and add to our new resultPool
                                for (int die : dp.getDice()) {
                                    resultPool.add(die);
                                }
                            } else {
                                // Optional: Log a warning or handle cases where a variable isn't a pool
                                System.err.println("Warning: Variable '" + name + "' is not a valid DicePool.");
                            }
                        }

                        // Push the newly merged pool onto the stack for the subsequent STORE instruction
                        stack.push(resultPool);
                    }

                    case COUNT_POOL -> {
                        DicePool p = (DicePool) ctx.getRuleContext().get(inst.poolName);
                        stack.push(p.size());
                    }

                    case FIXED_POOL -> {
                        DicePool pool = new DicePool();
                        String[] parts = inst.name.split("\\s+");

                        for (String s : parts) {
                            if (s.isEmpty()) continue;
                            try {
                                int val = Integer.parseInt(s);
                                pool.add(val);
                            } catch (NumberFormatException e) {
                            }
                        }

                        stack.push(pool);
                    }

                    case SUM_POOL -> {
                        Object source = ctx.getValue(inst.poolName);
                        double total = 0.0;

                        if (source instanceof DicePool pool) {
                            for (int val : pool.getDice()) {
                                total += val;
                            }
                        } else if (source instanceof Number num) {
                            total = num.doubleValue();
                        }

                        stack.push(total);
                    }

                    case PRINT -> {
                        Object val = ctx.getValue(inst.name);
                        System.out.print("[DEBUG LOG] " + inst.name + " = ");

                        switch (val) {
                            case DicePool pool -> System.out.println("DicePool " + pool.getDice().toString());
                            case Number d -> System.out.println(d);
                            case null -> System.out.println("null (Undefined)");
                            default -> System.out.println(val.toString());
                        }
                    }
                }
                pc++;
            }
        } catch (Exception e) {
            Instruction errorInst = code.get(pc);
            throw new DSLException("Runtime", e.getMessage(), errorInst.lineNum, errorInst.lineLiteral);
        }
    }

    private boolean isTrue(Object value) {
        if (value instanceof Boolean) return (Boolean) value;
        if (value instanceof Number) return ((Number) value).intValue() != 0;
        return value != null;
    }
}