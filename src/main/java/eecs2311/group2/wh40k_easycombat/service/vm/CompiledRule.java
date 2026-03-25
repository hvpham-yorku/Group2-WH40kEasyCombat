package eecs2311.group2.wh40k_easycombat.service.vm;

import java.util.List;

public class CompiledRule {

    private final String name;

    private final List<Instruction> instructions;

    public CompiledRule(String name, List<Instruction> instructions) {
        this.name = name;
        this.instructions = instructions;
    }

    public String getName() {
        return name;
    }

    public List<Instruction> getInstructions() {
        return instructions;
    }
}