package eecs2311.group2.wh40k_easycombat.service.vm.expr;

import eecs2311.group2.wh40k_easycombat.service.vm.ExecutionContext;

import java.util.Optional;

public class VarExpr implements Expression {

    private final String name;

    public VarExpr(String name) {
        this.name = name;
    }

    public Optional<Object> eval(ExecutionContext ctx) {
        Object val = ctx.getRuleContext().get(name);

        if (val == null) {
            // Throwing a RuntimeException that will be caught by RuleVM
            // and wrapped into a DSLException with line number information.
            throw new RuntimeException("Variable '" + name + "' is undefined in the current context.");
        }

        return Optional.of(val);
    }
}