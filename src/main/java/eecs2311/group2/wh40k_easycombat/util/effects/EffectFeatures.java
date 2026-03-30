//Comments:
// Encapsulates all configurable properties of an effect in a single object.
//
// Instead of spreading effect-related data across multiple classes,
// this class groups together the key attributes that define how an effect behaves:
// - Tag: what aspect of the game is affected (e.g., HEALTH, HIT_ROLL)
// - OperationType: how the value changes (INCREASE, DECREASE, SET)
// - Value: magnitude of the effect
// - Weapon name (optional): restricts the effect to a specific weapon
// - Keyword text (optional): used for adding special rules or abilities
//
// This abstraction separates "what an effect is" from "how it is applied",
// making the system more modular and easier to extend or modify.

package eecs2311.group2.wh40k_easycombat.util.effects;

public class EffectFeatures {
    private Tag tag;
    private OperationType operationType;
    private int value;
    private String weaponName;
    private String keywordText;

    public EffectFeatures(Tag tag, OperationType operationType, int value) {
        this(tag, operationType, value, null, null);
    }

    public EffectFeatures(Tag tag, OperationType operationType, int value, String weaponName) {
        this(tag, operationType, value, weaponName, null);
    }

    public EffectFeatures(Tag tag, OperationType operationType, int value, String weaponName, String keywordText) {
        this.tag = tag;
        this.operationType = operationType;
        this.value = value;
        this.weaponName = weaponName == null ? "" : weaponName.trim();
        this.keywordText = keywordText == null ? "" : keywordText.trim();
    }

    public Tag getTag() {
        return tag;
    }

    public OperationType getOperationType() {
        return operationType;
    }

    public int getValue() {
        return value;
    }

    public String getWeaponName() {
        return weaponName;
    }

    public String getKeywordText() {
        return keywordText;
    }

    public void setTag(Tag tag) {
        this.tag = tag;
    }

    public void setOperationType(OperationType operationType) {
        this.operationType = operationType;
    }

    public void setValue(int value) {
        this.value = value;
    }

    public void setWeaponName(String weaponName) {
        this.weaponName = weaponName == null ? "" : weaponName.trim();
    }

    public void setKeywordText(String keywordText) {
        this.keywordText = keywordText == null ? "" : keywordText.trim();
    }

    @Override
    public String toString() {
        return "EffectFeatures{" + "tag=" + tag + ", operationType=" + operationType + ", value=" + value + ", weaponName='" + weaponName + '\'' + ", keywordText='" + keywordText + '\'' + '}';
    }

    public enum OperationType {
        INCREASE,
        DECREASE,
        SET
    }
}
