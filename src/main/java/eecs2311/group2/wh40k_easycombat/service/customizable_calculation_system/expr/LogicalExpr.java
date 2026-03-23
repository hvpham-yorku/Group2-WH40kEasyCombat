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
        boolean l = asBool(left.eval(ctx).orElse(false));

        if (op == OpCode.AND && !l) return Optional.of(false);
        if (op == OpCode.OR && l) return Optional.of(true);

        boolean r = asBool(right.eval(ctx).orElse(false));
        return Optional.of(op == OpCode.AND ? l && r : l || r);
    }
}