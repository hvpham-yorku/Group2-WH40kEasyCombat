package eecs2311.group2.wh40k_easycombat.classes.utility;
import java.util.Random;

public class DiceUtility {
  
  public static int convertStringIntoRoll(String input){
    int rollResult = 0;
    
    switch(input){
      case "D3": 
        rollResult = (rollD6() + 1 ) / 2;
        break;
      case "D6":
        rollResult = rollD6();
        break;
      default:
        throw new IllegalArgumentException("Dice type " + input + " unknown");
    }
    
    return rollResult;
  }

  public static int rollD6(){
    Random random = new Random();
    return random.nextInt(6) + 1;
  }
}
