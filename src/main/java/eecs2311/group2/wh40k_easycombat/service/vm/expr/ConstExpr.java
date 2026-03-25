package eecs2311.group2.wh40k_easycombat.service.vm.expr;

import eecs2311.group2.wh40k_easycombat.service.vm.ExecutionContext;

import java.util.Optional;

public class ConstExpr implements Expression {

    private final Object value;

    public ConstExpr(Object value) {
        this.value = value;
    }

    @Override
    public Optional<Object> eval(ExecutionContext ctx) {
        return Optional.of(value);
    }
}