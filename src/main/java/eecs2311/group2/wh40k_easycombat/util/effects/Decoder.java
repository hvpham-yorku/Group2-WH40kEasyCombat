package eecs2311.group2.wh40k_easycombat.util.effects;

public class Decoder {

    public static Effect decodeEffect(String effectClass, String name, String effectTypeText, String tagText, int value) {
        EffectType effectType = decodeEffectType(effectTypeText);
        Tag tag = decodeTag(tagText);

        if (effectClass == null || effectClass.isBlank()) {
            throw new IllegalArgumentException("Effect class cannot be null or blank.");
        }

        String normalized = effectClass.trim().toUpperCase();

        switch (normalized) {
            case "BUFF":
                return new Buff(name, effectType, tag, value);
            case "DEBUFF":
                return new Debuff(name, effectType, tag, value);

            default:
                throw new IllegalArgumentException("Unknown effect class: " + effectClass);
        }
    }

    public static EffectType decodeEffectType(String text) {
        if (text == null || text.isBlank()) {
            throw new IllegalArgumentException("Effect type cannot be null or blank.");
        }

        try {
            return EffectType.valueOf(text.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid effect type: " + text);
        }
    }

    public static Tag decodeTag(String text) {
        if (text == null || text.isBlank()) {
            throw new IllegalArgumentException("Tag cannot be null or blank.");
        }

        try {
            return Tag.valueOf(text.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid tag: " + text);
        }
    }
}