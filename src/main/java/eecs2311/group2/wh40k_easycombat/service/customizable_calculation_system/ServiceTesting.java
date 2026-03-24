package eecs2311.group2.wh40k_easycombat.service.customizable_calculation_system;

import eecs2311.group2.wh40k_easycombat.service.game.DiceService;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ServiceTesting {
    public static void main(String[] args) {
//        Map<String, Object> attacker = new HashMap<>();
//        attacker.put("BS", 3); // 3+ 命中
//
//        Map<String, Object> weapon = new HashMap<>();
//        weapon.put("A", 5);    // 5 次攻击
//
//        // 2. 初始化上下文
//        RuleContext rule_ctx = new RuleContext();
//        rule_ctx.set("attacker", attacker);
//        rule_ctx.set("weapon", weapon);
//        rule_ctx.set("mortal_wounds", 0); // 预设初始值
//
//        RuleService service = new DefaultRuleService(new RuleVM(), new RuleCompiler());
//        ExecutionContext ctx = new ExecutionContext(rule_ctx);
//        RuleCompiler compiler = new RuleCompiler();
//        CompiledRule rule = compiler.compile("rule1", """
//                10 -> attacks
//                0 -> extra_bonus
//
//                while (attacks > 0)
//                    roll 1 -> r
//                    if (r == 6)
//                        extra_bonus += 1
//                    endif
//                    attacks -= 1
//                endwhile
//
//                roll extra_bonus -> bonus_hits
//                """);
//        service.loadCompiledRule(rule);
//
//        CompiledRule rule2 = compiler.compile("rule2", """
//                # --- 准备阶段 ---
//                # 从注入的对象获取攻击次数并开始投骰
//                roll weapon.A -> hit_pool
//
//                # --- 命中判定 ---
//                # 过滤出大于等于攻击者 BS (Ballistic Skill) 的骰子
//                filter hit_pool (it >= attacker.BS) -> succ_hits
//                count succ_hits -> num_to_wound
//
//                # --- 伤口判定循环 (While 嵌套 If) ---
//                # 初始化成功伤口数为 0
//                0 -> wounds
//
//                while (num_to_wound > 0)
//                    # 模拟投一个伤口骰
//                    roll 1 -> r
//
//                    # 模拟战锤逻辑：如果骰子是 6，触发致命伤 (Mortal Wounds)
//                    if (r == 6)
//                        mortal_wounds += 1
//                    endif
//
//                    # 基础伤口判定：假设 4+ 成功
//                    if (r >= 4)
//                        wounds += 1
//                    endif
//
//                    # 计数器减一，驱动循环
//                    num_to_wound -= 1
//                endwhile
//
//                # --- 最终结算 ---
//                # 将结果存入 final_hits 供 Java 外部读取
//                wounds -> final_damage
//                """);
//        service.loadCompiledRule(rule2);
//
////        var result = service.run("rule1", ctx, true);
////        if (result.isSuccess()) {
////            result.getLogs().forEach(System.out::println);
////        } else {
////            System.out.println("Err: " + result.getError());
////        }
//
//        var result2 = service.run("rule2", ctx, true);
//        if (result2.isSuccess()) {
////            result2.getLogs().forEach(System.out::println);
//            System.out.println("成功伤口数: " + rule_ctx.get("final_damage"));
//            System.out.println("触发的致命伤: " + rule_ctx.get("mortal_wounds"));
//        } else {
//            System.out.println("Err: " + result2.getError());
//        }

//        testRerollAndFilter();
        testKeepHighLowInstructions();
    }

    public static void testRerollAndFilter() {
        // 1. 初始化上下文
        RuleContext ruleContext = new RuleContext();
        ExecutionContext execContext = new ExecutionContext(ruleContext);
        // 注意：确保你的 RuleVM 内部实现使用了 DiceService.rollNSideDices(1)

        // 2. 编译并执行
        RuleCompiler compiler = new RuleCompiler();
        // 脚本逻辑：投10个 -> 重投1 -> 过滤4+ -> 计数
        CompiledRule rule = compiler.compile("RerollFilterTest", """
            # 1. 投 10 个 D6 骰子
            roll 10 -> hits
            
            # 2. 战锤经典：重投所有的 1 (Reroll 1s)
            reroll hits (@ > 3 && @ < 5) -> hits_after_reroll
            
            # 3. 过滤出点数大于等于 4 的骰子
            filter hits_after_reroll (@ > 3 && @ < 5) -> succ
            
            # 4. 统计成功数量
            count succ -> succ_count
            """);

        RuleVM vm = new RuleVM();
        vm.execute(rule, execContext);

        // 3. 打印结果验证
        System.out.println("=== 战锤重投测试 ===");

        // 打印原始骰子
        DicePool rawPool = (DicePool) execContext.getValue("hits");
        System.out.print("原始骰子 (hits): ");
        rawPool.getDice().forEach(d -> System.out.print(d + " "));
        System.out.println();

        // 打印重投后的骰子
        DicePool rerolledPool = (DicePool) execContext.getValue("hits_after_reroll");
        System.out.print("重投后 (hits_after_reroll): ");
        rerolledPool.getDice().forEach(d -> System.out.print(d + " "));
        System.out.println(" (理论上这里的 1 会比原始池少)");

        // 打印过滤后的结果
        DicePool filterPool = (DicePool) execContext.getValue("succ");
        System.out.print("最终成功 (succ, 4+): ");
        filterPool.getDice().forEach(d -> System.out.print(d + " "));
        System.out.println();

        System.out.println("成功总数 (succ_count): " + execContext.getValue("succ_count"));
    }

    public static void testKeepHighLowInstructions() {
        RuleContext ruleContext = new RuleContext();
        ExecutionContext execContext = new ExecutionContext(ruleContext);
        RuleCompiler compiler = new RuleCompiler();

        // 编写测试脚本
        String script = """
            roll 4 -> raw_pool
            keep_high raw_pool 3 -> high_pool
            keep_low raw_pool 1 -> low_pool
            """;

        // 1. 编译
        CompiledRule rule = compiler.compile("KeepTest", script);

        // 2. 验证指令生成 (断点查看或打印)
        System.out.println("生成的指令序列：");
        for (Instruction ins : rule.getInstructions()) {
            System.out.println("Op: " + ins.op + ", Pool: " + ins.poolName + ", TargetPool: " + ins.targetPool + ", Value: " + ins.value);
        }

        // 3. 执行
        RuleVM vm = new RuleVM();
        vm.execute(rule, execContext);

        // 4. 验证结果
        DicePool raw = (DicePool) execContext.getValue("raw_pool");
        DicePool high = (DicePool) execContext.getValue("high_pool");
        DicePool low = (DicePool) execContext.getValue("low_pool");

        System.out.println("\n--- 执行结果 ---");
        System.out.println("原始池子 (4d6): " + raw.getDice());
        System.out.println("最高 3 个 (keep_high 3): " + high.getDice());
        System.out.println("最低 1 个 (keep_low 1): " + low.getDice());

        // 简单的断言逻辑
        assert high.size() == 3;
        assert low.size() == 1;
        // 验证最高 3 个的和是否大于等于最低的
    }
}
