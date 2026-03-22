package eecs2311.group2.wh40k_easycombat.service.calculations;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.*;

class DiceServiceTest {

    @Test
    @DisplayName("rollDice returns the requested number of D6 results")
    void rollDiceReturnsExpectedCount() {
        DiceService dice = new DiceService();

        dice.rollDice(10);
        ArrayList<Integer> result = dice.getResults();

        assertEquals(10, result.size());
        assertTrue(result.stream().allMatch(value -> value >= 1 && value <= 6));
    }

    @Test
    @DisplayName("convertStringIntoRoll supports D3 and D6")
    void convertStringIntoRollSupportsD3AndD6() {
        DiceService dice = new DiceService();

        int d3 = dice.convertStringIntoRoll("D3");
        int d6 = dice.convertStringIntoRoll("D6");

        assertTrue(d3 >= 1 && d3 <= 3);
        assertTrue(d6 >= 1 && d6 <= 6);
    }

    @Test
    @DisplayName("convertStringIntoRoll rejects unsupported dice expressions")
    void convertStringIntoRollRejectsUnsupportedExpressions() {
        DiceService dice = new DiceService();
        assertThrows(IllegalArgumentException.class, () -> dice.convertStringIntoRoll("2D6"));
    }
}
