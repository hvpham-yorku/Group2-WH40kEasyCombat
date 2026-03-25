package eecs2311.group2.wh40k_easycombat.service.vm.expr;

import eecs2311.group2.wh40k_easycombat.service.vm.ExecutionContext;
import eecs2311.group2.wh40k_easycombat.service.vm.OpCode;

import java.util.Optional;

public class LogicalExpr extends BaseBinaryExpr {
    private final OpCode op;

    public LogicalExpr(Expression left, Expression right, OpCode op) {
        super(left, right);
        this.op = op;
    }

    @Override
    public Optional<Object> eval(ExecutionContext ctx) {
        Object leftVal = left.eval(ctx).orElse(0.0);
        boolean l = asBool(leftVal);

        if (op == OpCode.AND && !l) return Optional.of(0.0);
        if (op == OpCode.OR && l) return Optional.of(1.0);

        Object rightVal = right.eval(ctx).orElse(0.0);
        boolean r = asBool(rightVal);

        boolean finalResult = (op == OpCode.AND) ? (l && r) : (l || r);
        return Optional.of(finalResult ? 1.0 : 0.0);
    }
}