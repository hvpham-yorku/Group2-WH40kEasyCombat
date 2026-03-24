package eecs2311.group2.wh40k_easycombat.service.customizable_calculation_system.expr;

import eecs2311.group2.wh40k_easycombat.service.customizable_calculation_system.ExecutionContext;
import eecs2311.group2.wh40k_easycombat.service.customizable_calculation_system.OpCode;

import java.util.Optional;

public class LogicalExpr extends BaseBinaryExpr {
    private final OpCode op;

    public LogicalExpr(Expression left, Expression right, OpCode op) {
        super(left, right);
        this.op = op;
    }

    @Override
    public Optional<Object> eval(ExecutionContext ctx) {
        // 1. 获取左值并转换为布尔
        Object leftVal = left.eval(ctx).orElse(0.0);
        boolean l = asBool(leftVal);


        // 2. 短路逻辑 (Short-circuit)
        // AND: 只要左边是假，整体必为假
        if (op == OpCode.AND && !l) return Optional.of(0.0);
        // OR: 只要左边是真，整体必为真
        if (op == OpCode.OR && l) return Optional.of(1.0);

        // 3. 计算右值
        Object rightVal = right.eval(ctx).orElse(0.0);
        boolean r = asBool(rightVal);

        // 4. 返回统一的 Double 结果 (1.0 代表真, 0.0 代表假)
        boolean finalResult = (op == OpCode.AND) ? (l && r) : (l || r);
        return Optional.of(finalResult ? 1.0 : 0.0);
    }
}