package eecs2311.group2.wh40k_easycombat.service.game;

import static org.junit.jupiter.api.Assertions.*;

import java.util.ArrayList;

import org.junit.jupiter.api.Test;

class DiceTest {

	@Test
	void testDiceRoll() {
		DiceService dice = new DiceService();
		dice.rollDice(10);
		ArrayList<Integer> result = dice.getResults();
		assertTrue(result.size() == 10);
	}

}
