package eecs2311.group2.wh40k_easycombat.service.customizable_calculation_system;

import java.util.ArrayList;
import java.util.List;

public class DicePool {

    private final List<Integer> dice = new ArrayList<>();

    public void add(int value) {
        dice.add(value);
    }

    public void addList(List<Integer> values){
        dice.addAll(values);
    }

    public List<Integer> getDice() {
        return dice;
    }

    public int size() {
        return dice.size();
    }

    public int sum() {
        return dice.stream().mapToInt(Integer::intValue).sum();
    }
}