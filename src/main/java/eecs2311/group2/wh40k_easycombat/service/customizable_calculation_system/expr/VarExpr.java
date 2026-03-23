package eecs2311.group2.wh40k_easycombat.service.customizable_calculation_system.expr;

import eecs2311.group2.wh40k_easycombat.service.customizable_calculation_system.ExecutionContext;

import java.util.Optional;

public class VarExpr implements Expression {

    private final String name;

    public VarExpr(String name) {
        this.name = name;
    }

    public Optional<Object> eval(ExecutionContext ctx) {
        Object val = ctx.getRuleContext().get(name);
        System.out.println("VarExpr reading [" + name + "]: " + val);
        return Optional.ofNullable(val);
    }
}