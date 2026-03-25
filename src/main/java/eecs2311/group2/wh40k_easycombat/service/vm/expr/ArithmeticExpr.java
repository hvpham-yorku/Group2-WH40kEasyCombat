package eecs2311.group2.wh40k_easycombat.service.vm.expr;

import eecs2311.group2.wh40k_easycombat.service.vm.ExecutionContext;
import eecs2311.group2.wh40k_easycombat.service.vm.OpCode;

import java.util.Optional;

public class ArithmeticExpr extends BaseBinaryExpr {
    private final OpCode op;

    public ArithmeticExpr(Expression left, Expression right, OpCode op) {
        super(left, right);
        this.op = op;
    }

    @Override
    public Optional<Object> eval(ExecutionContext ctx) {
        int l = asInt(left.eval(ctx).orElse(0));
        int r = asInt(right.eval(ctx).orElse(0));

        return Optional.of(switch (op) {
            case ADD -> l + r;
            case SUB -> l - r;
            case MUL -> l * r;
            case DIV -> l / r;
            default -> throw new UnsupportedOperationException("illegal operation");
        });
    }
}