package eecs2311.group2.wh40k_easycombat.service.customizable_calculation_system;

import eecs2311.group2.wh40k_easycombat.service.game.DiceService;
import java.util.*;

public class RuleVM {

    public void execute(CompiledRule rule, ExecutionContext ctx) {
        List<Instruction> code = rule.getInstructions();
        Deque<Object> stack = new ArrayDeque<>();
        int pc = 0;

        while (pc < code.size()) {
            Instruction inst = code.get(pc);
            System.out.println(pc + " Run: " + inst.op);

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
                        // 战锤习惯：0 为假，非 0 为真
                        panned = ((Number) cond).intValue() != 0;
                    } else {
                        panned = (cond != null);
                    }

                    if (!panned) {
                        pc = inst.target;
                        continue; // 跳过 pc++
                    }
                }

                case GOTO -> {
                    pc = inst.target;
                    continue; // 必须 continue，防止 pc++ 导致跳过目标指令
                }

                case ROLL_POOL -> {
                    // 从表达式获取投骰数量
                    int count = (int) inst.expr.eval(ctx).orElse(0);
                    DicePool pool = new DicePool();
                    pool.addList(DiceService.rollNSideDices(count, 6));
                    stack.push(pool);
                }

                case FILTER_POOL -> {
                    DicePool src = (DicePool) ctx.getRuleContext().get(inst.poolName);
                    DicePool dst = new DicePool();
                    for (int v : src.getDice()) {
                        ctx.getRuleContext().set("it", v); // 注入隐式变量
                        if ((boolean) inst.expr.eval(ctx).orElse(false)) {
                            dst.add(v);
                        }
                    }
                    stack.push(dst);
                }

                case COUNT_POOL -> {
                    DicePool p = (DicePool) ctx.getRuleContext().get(inst.poolName);
                    stack.push(p.size());
                }

                case JMP -> {
                    pc = inst.target;
                    continue;
                }
            }
            pc++;
            System.out.println(" [Executed]");
            System.out.println("PC: " + pc + " | Op: " + inst.op + " | Stack: " + stack);
        }
    }
}