package eecs2311.group2.wh40k_easycombat.service.vm.expr;

import eecs2311.group2.wh40k_easycombat.service.vm.ExecutionContext;
import eecs2311.group2.wh40k_easycombat.service.vm.OpCode;

import java.util.Optional;

public class UnaryExpr implements Expression {
    private final Expression expr;
    private final OpCode op;

    public UnaryExpr(Expression expr, OpCode op) {
        this.expr = expr;
        this.op = op;
    }

    @Override
    public Optional<Object> eval(ExecutionContext ctx) {
        Object val = expr.eval(ctx).orElseThrow(() -> new RuntimeException("Unary cannot be null"));

        return Optional.of(switch (op) {
            case OpCode.NOT -> !asBool(val);
            case OpCode.NEG -> -asInt(val);
            case OpCode.POS -> asInt(val);
            default -> throw new UnsupportedOperationException("Illegal Unary OpCode" + op);
        });
    }

    private int asInt(Object obj) {
        if (obj instanceof Number n) return n.intValue();
        throw new RuntimeException(obj + " is not a number, can't be +/-");
    }

    private boolean asBool(Object obj) {
        if (obj instanceof Boolean b) return b;
        throw new RuntimeException(obj + " is not a boolean");
    }
}