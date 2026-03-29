package eecs2311.group2.wh40k_easycombat.service.calculations;

public class ShootingCalculations {

    private static DiceService diceService = new DiceService();

    public static int rollD6(){
        diceService.rollDice(1);
        return diceService.getResults().get(0);
    }

    public static boolean hitsTarget(int ballisticSkill, int roll) {
        return roll >= ballisticSkill;
    }

    public static int requiredWoundRoll(int strength, int toughness) {
        if (strength >= 2 * toughness) {
            return 2;
        } else if (strength > toughness) {
            return 3;
        } else if (strength == toughness) {
            return 4;
        } else if (strength * 2 <= toughness) {
            return 6;
        } else {
            return 5;
        }
    }

    public static boolean woundsTarget(int strength, int toughness, int roll) {
        int needed = requiredWoundRoll(strength, toughness);
        return roll >= needed;
    }

    public static int effectiveSave(int saveCharacteristic, int armorPenetration) {
        int result = saveCharacteristic - armorPenetration;

        if (result < 2) {
            result = 2;
        }
        return result;
    }

    public static boolean savesAttack(int saveCharacteristic, int armorPenetration, int roll) {
        int needed = effectiveSave(saveCharacteristic, armorPenetration);
        return roll >= needed;
    }

    public static int resolveRangedAttack(int ballisticSkill, int strength, int toughness, int saveCharacteristic, int armorPenetration, int damage) {
        int hitRoll = rollD6();

        if (!hitsTarget(ballisticSkill, hitRoll)) {
            return 0;
        }

        int woundRoll = rollD6();

        if (!woundsTarget(strength, toughness, woundRoll)) {
            return 0;
        }

        int saveRoll = rollD6();

        if (savesAttack(saveCharacteristic, armorPenetration, saveRoll)) {
            return 0;
        }
        return damage;
    }

    public static int resolveShootingSequence(int numberOfAttacks, int ballisticSkill, int strength, int toughness, int saveCharacteristic, int armorPenetration, int damage) {
        int totalDamage = 0;

        for (int i = 0; i < numberOfAttacks; i++) {
            totalDamage += resolveRangedAttack(ballisticSkill, strength, toughness, saveCharacteristic, armorPenetration, damage);
        }
        return totalDamage;
    }
}
