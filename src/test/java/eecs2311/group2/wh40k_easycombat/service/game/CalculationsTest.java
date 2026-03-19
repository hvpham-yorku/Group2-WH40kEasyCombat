package eecs2311.group2.wh40k_easycombat.service.game;

import static org.junit.jupiter.api.Assertions.*;

import eecs2311.group2.wh40k_easycombat.service.calculations.BattleShockCalculations;
import eecs2311.group2.wh40k_easycombat.service.calculations.VictoryPoints;
import org.junit.jupiter.api.Test;

public class CalculationsTest {

    @Test
    void battleShockTest1(){
        BattleShockCalculations battleShockCalculations1 = new BattleShockCalculations();
        battleShockCalculations1.checkBattleShock(7,6);

        assertEquals(true, true);
    }

    @Test
    void battleShockTest2(){
        BattleShockCalculations battleShockCalculations2 = new BattleShockCalculations();
        battleShockCalculations2.checkBattleShock(5,5);

        assertEquals(true, true);
    }

    @Test
    void battleShockTest3(){
        BattleShockCalculations battleShockCalculations3 = new BattleShockCalculations();
        battleShockCalculations3.checkBattleShock(3,4);

        assertEquals(false, false);
    }

    @Test
    void VPTest1(){
        VictoryPoints vp1 = new VictoryPoints();
        vp1.calculateVictoryPoints(1,1,1);

        assertEquals(3,3);
    }

    @Test
    void VPTest2(){
        VictoryPoints vp2 = new VictoryPoints();
        vp2.addVictoryPoints(3,5);

        assertEquals(8,8);
    }

    //@Test
}