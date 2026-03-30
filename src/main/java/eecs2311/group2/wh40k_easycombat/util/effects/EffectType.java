//Comments:
// Enumeration that categorizes effects based on their general purpose.
//
// EffectType provides a high-level classification (e.g., DAMAGE, HEAL, SPEED), which can be used for organizing, filtering, or identifying effects.
//
// While Tag defines what is modified, EffectType describes the nature or theme of the effect itself.

package eecs2311.group2.wh40k_easycombat.util.effects;

public enum EffectType {
    FIRE,
    HEAL,
    POISON,
    DAMAGE,
    SPEED,
    SLOWNESS,
    MORALE,
    COMMAND,
    VICTORY,
    WEAPON
}
