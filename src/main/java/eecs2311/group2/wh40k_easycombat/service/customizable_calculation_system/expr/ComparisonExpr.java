package eecs2311.group2.wh40k_easycombat.service.customizable_calculation_system.expr;

import eecs2311.group2.wh40k_easycombat.service.customizable_calculation_system.ExecutionContext;
import eecs2311.group2.wh40k_easycombat.service.customizable_calculation_system.OpCode;

import java.util.Objects;
import java.util.Optional;

public class ComparisonExpr extends BaseBinaryExpr {
    private final OpCode op;

    public ComparisonExpr(Expression left, Expression right, OpCode op) {
        super(left, right);
        this.op = op;
    }

    @Override
    public Optional<Object> eval(ExecutionContext ctx) {
        Object l = left.eval(ctx).orElseThrow();
        Object r = right.eval(ctx).orElseThrow();

        return Optional.of(switch (op) {
            case CMP_GE -> asInt(l) >= asInt(r);
            case CMP_GT -> asInt(l) > asInt(r);
            case CMP_LE -> asInt(l) <= asInt(r);
            case CMP_LT -> asInt(l) < asInt(r);
            case CMP_EQ -> l.equals(r);
            default -> throw new UnsupportedOperationException("Illegal Operation");
        });
    }
}
