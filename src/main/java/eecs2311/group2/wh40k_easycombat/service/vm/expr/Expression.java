package eecs2311.group2.wh40k_easycombat.service.vm.expr;

import eecs2311.group2.wh40k_easycombat.service.vm.ExecutionContext;

import java.util.Optional;

public interface Expression {
    Optional<Object> eval(ExecutionContext ctx);
}