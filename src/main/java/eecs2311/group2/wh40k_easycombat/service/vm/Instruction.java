package eecs2311.group2.wh40k_easycombat.service.vm;

import eecs2311.group2.wh40k_easycombat.service.vm.expr.Expression;

public class Instruction {

    public OpCode op;

    public String name;
    public String poolName;
    public String targetPool;

    public int value;
    public int target;

    public Expression expr;

    public int lineNum;
    public String lineLiteral;

    public Instruction() {}
    public Instruction(OpCode op) { this.op = op; }
}