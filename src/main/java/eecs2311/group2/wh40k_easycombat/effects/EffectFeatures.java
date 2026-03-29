package eecs2311.group2.wh40k_easycombat.effects;

public class EffectFeatures {
    private Tag tag;
    private OperationType operationType;
    private int value;

    public EffectFeatures(Tag tag, OperationType operationType, int value) {
        this.tag = tag;
        this.operationType = operationType;
        this.value = value;
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

    public void setTag(Tag tag) {
        this.tag = tag;
    }

    public void setOperationType(OperationType operationType) {
        this.operationType = operationType;
    }

    public void setValue(int value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return "EffectFeatures{" + "tag=" + tag + ", operationType=" + operationType + ", value=" + value + '}';
    }

    public enum OperationType {
        INCREASE,
        DECREASE,
        SET
    }
}