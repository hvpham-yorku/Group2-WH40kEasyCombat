//Comments:
// Enumeration that defines exactly what aspect of the game an effect modifies.
//
// Tags are the key driver of behavior in Buff and Debuff classes, as they determine how the effect is applied.
//
// Examples include:
// - HEALTH → affects unit health
// - MORALE → affects battle shock state
// - HIT_ROLL / WOUND_ROLL → affects combat calculations
// - WEAPON_DAMAGE / WEAPON_AP → modifies weapon stats
//
// This separation allows the system to remain flexible and scalable, as new gameplay mechanics can be added simply by introducing new tags.

package eecs2311.group2.wh40k_easycombat.util.effects;

public enum Tag {
    HEALTH,
    MORALE,
    DAMAGE,
    UTILITY,
    VICTORY_POINTS,
    HIT_ROLL,
    WOUND_ROLL,
    WEAPON_ATTACKS,
    WEAPON_DAMAGE,
    WEAPON_AP,
    WEAPON_KEYWORD,
    HIT_REROLL,
    WOUND_REROLL
}
