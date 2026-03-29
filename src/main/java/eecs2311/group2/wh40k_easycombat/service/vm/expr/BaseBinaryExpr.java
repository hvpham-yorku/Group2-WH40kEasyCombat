package eecs2311.group2.wh40k_easycombat.service.vm.expr;

import eecs2311.group2.wh40k_easycombat.service.vm.DicePool;

public abstract class BaseBinaryExpr implements Expression {
    protected final Expression left;
    protected final Expression right;

    public BaseBinaryExpr(Expression left, Expression right) {
        this.left = left;
        this.right = right;
    }

    protected int asInt(Object obj) {
        if (obj instanceof Integer i) return i;
        else if (obj instanceof Number i) return i.intValue();
        else if (obj instanceof DicePool pool) {
            return pool.size();
        }
        else if (obj instanceof Boolean b) return b ? 1 : 0;
        return 0;
    }

    protected boolean asBool(Object obj) {
        if (obj instanceof Boolean b) return b;
        if (obj instanceof Number) return ((Number) obj).doubleValue() != 0.0;
        throw new RuntimeException("can't treat " + obj + " as boolean");
    }
}