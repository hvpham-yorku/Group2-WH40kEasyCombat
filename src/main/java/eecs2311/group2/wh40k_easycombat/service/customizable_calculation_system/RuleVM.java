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
                    // 获取源池子
                    Object sourceObj = ctx.getValue(inst.poolName);
                    if (!(sourceObj instanceof DicePool src)) {
                        stack.push(new DicePool()); // 防御性编程：源不存在则推入空池
                        break;
                    }

                    DicePool dst = new DicePool();
                    for (int v : src.getDice()) {
                        // 注入隐式变量 it
                        ctx.setValue("@", v);

                        // 使用你的 eval(ctx)
                        Optional<Object> evalResult = inst.expr.eval(ctx);

                        // 建议增加一个工具方法 isTrue，处理非布尔值的逻辑
                        if (evalResult.isPresent() && isTrue(evalResult.get())) {
                            dst.add(v);
                        }
                    }

                    // 执行完毕，清理 it 变量，防止干扰后续指令
                    ctx.setValue("@", null);

                    stack.push(dst);
                }

                case REROLL_POOL -> {
                    // 1. 获取源池子
                    DicePool src = (DicePool) ctx.getValue(inst.poolName);
                    DicePool dst = new DicePool();

                    for (int v : src.getDice()) {
                        // 注入当前点数供表达式判定
                        ctx.setValue("@", v);

                        // 2. 评估重投条件 (例如 it == 1)
                        // 建议使用 isTrue 工具方法处理 eval 结果
                        if (isTrue(inst.expr.eval(ctx).orElse(false))) {
                            // 3. 执行重投：调用你的静态方法投 1 个 6 面骰
                            List<Integer> newRoll = DiceService.rollNSideDices(1);
                            int newValue = newRoll.getFirst();

                            dst.add(newValue);
                            // 这里可以加上 Trace 记录：从 v 变成了 newValue
                        } else {
                            // 不满足重投条件，保留原值
                            dst.add(v);
                        }
                    }

                    // 清理现场并压栈
                    ctx.setValue("@", null);
                    stack.push(dst);
                }

                case KEEP_HIGH, KEEP_LOW -> {
                    // 1. 从上下文中获取原始池子 (作为源数据)
                    Object source = ctx.getValue(inst.poolName);
                    if (!(source instanceof DicePool sourcePool)) {
                        // 防御性处理：压入一个空池子确保后续 STORE 不报错
                        stack.push(new DicePool());
                        break;
                    }

                    // 2. 创建结果池
                    DicePool resultPool = new DicePool();
                    List<Integer> diceList = new ArrayList<>(sourcePool.getDice());

                    if (!diceList.isEmpty()) {
                        // 3. 排序
                        if (inst.op == OpCode.KEEP_HIGH) {
                            diceList.sort(Collections.reverseOrder());
                        } else {
                            diceList.sort(Integer::compare);
                        }

                        // 4. 截取
                        int keepN = Math.min(inst.value, diceList.size());
                        for (int i = 0; i < keepN; i++) {
                            resultPool.add(diceList.get(i));
                        }
                    }

                    // 5. 关键：将结果压入栈顶，等待下一条 STORE 指令
                    stack.push(resultPool);
                }

                case COUNT_POOL -> {
                    DicePool p = (DicePool) ctx.getRuleContext().get(inst.poolName);
                    stack.push(p.size());
                }

                case FIXED_POOL -> {
                    DicePool pool = new DicePool();

                    // 从指令中获取数字字符串，按空格拆分
                    // inst.name 在这里存的是 "1 2 3 4 5"
                    String[] parts = inst.name.split("\\s+");

                    for (String s : parts) {
                        if (s.isEmpty()) continue;
                        try {
                            // 解析数字并加入池子
                            int val = Integer.parseInt(s);
                            pool.add(val);
                        } catch (NumberFormatException e) {
                            // 健壮性处理：非数字跳过
                        }
                    }

                    // 关键：压入栈顶，交给下一条 STORE 指令处理
                    stack.push(pool);
                }

                case SUM_POOL -> {
                    // 1. 获取源池子
                    Object source = ctx.getValue(inst.poolName);
                    double total = 0.0;

                    if (source instanceof DicePool pool) {
                        // 2. 遍历池子求和
                        for (int val : pool.getDice()) {
                            total += val;
                        }
                    } else if (source instanceof Number num) {
                        // 健壮性处理：如果源本来就是个数字，直接取值
                        total = num.doubleValue();
                    }

                    // 3. 压入栈顶，等待 STORE
                    stack.push(total);
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

    private boolean isTrue(Object value) {
        if (value instanceof Boolean) return (Boolean) value;
        if (value instanceof Number) return ((Number) value).intValue() != 0;
        return value != null;
    }
}