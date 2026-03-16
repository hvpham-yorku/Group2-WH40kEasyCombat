package eecs2311.group2.wh40k_easycombat.service.game;

import java.util.ArrayList;
import java.util.Random;

public class DiceService {
	private final Random rng = new Random();
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
}
