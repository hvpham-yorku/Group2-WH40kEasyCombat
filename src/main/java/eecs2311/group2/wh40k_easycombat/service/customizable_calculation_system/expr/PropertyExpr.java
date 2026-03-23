package eecs2311.group2.wh40k_easycombat.service.customizable_calculation_system.expr;

import eecs2311.group2.wh40k_easycombat.service.customizable_calculation_system.ExecutionContext;

import java.util.Optional;

public class PropertyExpr implements Expression {
    private final String objectName;
    private final String property;

    public PropertyExpr(String objectName, String property) {
        this.objectName = objectName;
        this.property = property;
    }

    @Override
    public Optional<Object> eval(ExecutionContext ctx) {
        Object target = switch (objectName) {
            case "attacker" -> ctx.getAttacker();
            case "defender" -> ctx.getDefender();
            default -> ctx.getValue(objectName);
        };

        if (target == null) return Optional.empty();
        return Optional.ofNullable(PropertyResolver.get(target, property));
    }
}