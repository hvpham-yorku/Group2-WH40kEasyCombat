package eecs2311.group2.wh40k_easycombat.service.customizable_calculation_system.expr;

import eecs2311.group2.wh40k_easycombat.service.customizable_calculation_system.ExecutionContext;

import java.util.Optional;

public interface Expression {
    Optional<Object> eval(ExecutionContext ctx);
}