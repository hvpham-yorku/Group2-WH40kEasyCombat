package eecs2311.group2.wh40k_easycombat.service.game;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.stream.IntStream;

public class DiceService {
	private static final Random rng = new Random();
	private int diceNumber;
	private ArrayList<Integer> results;
	
	public void rollDice(int n) {
		diceNumber = n;
		results = new ArrayList<>(diceNumber);
		for (int i = 0; i < n; i++) {
			results.add(rng.nextInt(6) + 1);
		}
	}
	
	public int getDiceNumber() {
		return diceNumber;
	}
	
	public ArrayList<Integer> getResults() {
		return results;
	}

	public static List<Integer> rollNSideDices(int diceNumber) {
		return rollNSideDices(diceNumber, 6);
	}

	public static List<Integer> rollNSideDices(int diceNumber, int sides){
		return IntStream.range(0, diceNumber)
				.map(i -> rng.nextInt(sides) + 1)
				.boxed()
				.toList();
	}
}
