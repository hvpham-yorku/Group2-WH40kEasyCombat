//Comments:
// Utility class responsible for converting raw text input into Effect objects.
//
// This class acts as a bridge between external data (e.g., user input, files, UI)
// and the internal effect system. It parses strings and maps them to:
// - EffectType enum values
// - Tag enum values
// - specific Effect implementations (Buff or Debuff)
//
// It uses normalization (uppercase conversion, removing spaces/hyphens) to ensure flexible and error-tolerant input handling.
//
// The Decoder supports multiple types of effects:
// - general effects
// - weapon-specific effects
// - keyword-based effects
//
// By centralizing object creation here, the system avoids hardcoding logic elsewhere and makes it easier to extend with new effect types in the future.

package eecs2311.group2.wh40k_easycombat.util.effects;

public class Decoder {

    public static Effect decodeEffect(String effectClass, String name, String effectTypeText, String tagText, int value) {
        EffectType effectType = decodeEffectType(effectTypeText);
        Tag tag = decodeTag(tagText);

        String normalizedClass = normalize(effectClass);

        return switch (normalizedClass) {
            case "BUFF" -> new Buff(name, effectType, tag, value);
            case "DEBUFF" -> new Debuff(name, effectType, tag, value);
            default -> throw new IllegalArgumentException("Unknown effect class: " + effectClass);
        };
    }

    public static Effect decodeWeaponEffect(String effectClass, String name, String effectTypeText, String tagText, int value, String weaponName) {
        EffectType effectType = decodeEffectType(effectTypeText);
        Tag tag = decodeTag(tagText);

        String normalizedClass = normalize(effectClass);

        return switch (normalizedClass) {
            case "BUFF" -> new Buff(name, effectType, tag, value, weaponName);
            case "DEBUFF" -> new Debuff(name, effectType, tag, value, weaponName);
            default -> throw new IllegalArgumentException("Unknown effect class: " + effectClass);
        };
    }

    public static Effect decodeKeywordEffect(String effectClass, String name, String effectTypeText, String tagText, String weaponName, String keywordText) {
        EffectType effectType = decodeEffectType(effectTypeText);
        Tag tag = decodeTag(tagText);

        String normalizedClass = normalize(effectClass);

        return switch (normalizedClass) {
            case "BUFF" -> new Buff(name, effectType, tag, 0, weaponName, keywordText);
            case "DEBUFF" -> new Debuff(name, effectType, tag, 0, weaponName, keywordText);
            default -> throw new IllegalArgumentException("Unknown effect class: " + effectClass);
        };
    }

    public static EffectType decodeEffectType(String text) {
        String normalized = normalize(text);

        try {
            return EffectType.valueOf(normalized);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid effect type: " + text);
        }
    }

    public static Tag decodeTag(String text) {
        String normalized = normalize(text);

        try {
            return Tag.valueOf(normalized);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid tag: " + text);
        }
    }

    private static String normalize(String text) {
        if (text == null || text.isBlank()) {
            throw new IllegalArgumentException("Text cannot be null or blank.");
        }

        return text.trim().toUpperCase().replace(' ', '_').replace('-', '_');
    }
}
