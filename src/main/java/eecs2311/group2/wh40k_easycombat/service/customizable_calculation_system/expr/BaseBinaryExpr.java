package eecs2311.group2.wh40k_easycombat.service.customizable_calculation_system.expr;

import eecs2311.group2.wh40k_easycombat.service.customizable_calculation_system.DicePool;

public abstract class BaseBinaryExpr implements Expression {
    protected final Expression left;
    protected final Expression right;

    public BaseBinaryExpr(Expression left, Expression right) {
        this.left = left;
        this.right = right;
    }

    protected int asInt(Object obj) {
        if (obj instanceof Integer i) return i;
        if (obj instanceof DicePool pool) {
            // 这里的逻辑根据你的战锤规则定：是返回个数，还是返回最大值？
            // 建议：默认返回 size()，特殊判定由 filter 处理
            return pool.size();
        }
        if (obj instanceof Boolean b) return b ? 1 : 0;
        return 0;
    }

    protected boolean asBool(Object obj) {
        if (obj instanceof Boolean b) return b;
        throw new RuntimeException("can't treat " + obj + " as boolean");
    }
}