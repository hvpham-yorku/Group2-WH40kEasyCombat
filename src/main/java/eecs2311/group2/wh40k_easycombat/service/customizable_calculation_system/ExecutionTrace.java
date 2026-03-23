package eecs2311.group2.wh40k_easycombat.service.customizable_calculation_system;

import java.util.ArrayList;
import java.util.List;

public class ExecutionTrace {

    private final List<TraceStep> steps = new ArrayList<>();

    public void addStep(TraceStep step) {
        steps.add(step);
    }

    public List<TraceStep> getSteps() {
        return steps;
    }
}