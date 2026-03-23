package eecs2311.group2.wh40k_easycombat.service.customizable_calculation_system;

import eecs2311.group2.wh40k_easycombat.service.customizable_calculation_system.expr.Expression;

public class Instruction {

    public OpCode op;

    public String name;
    public String poolName;
    public String targetPool;

    public int value;
    public int target;

    public Expression expr;

    public Instruction() {}
    public Instruction(OpCode op) { this.op = op; }
}