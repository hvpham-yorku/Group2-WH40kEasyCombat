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

  public int convertStringIntoRoll(String stat) throws IllegalArgumentException{
    rollDice(1);
    int value = results.getFirst();
    switch (stat) {
        case "D3" -> {
            return (value + 1) / 2 ;
                }
        case "D6" -> {
            return value;
                }
        default -> throw new IllegalArgumentException();
    }
  }
}
