package eecs2311.group2.wh40k_easycombat.model.snapshot;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

@JsonTypeInfo(
    use = JsonTypeInfo.Id.NAME,
    include = JsonTypeInfo.As.PROPERTY,
    property = "@type"
)
@JsonSubTypes({
    @JsonSubTypes.Type(value = StateSnapshot.class, name = "state"),
    @JsonSubTypes.Type(value = LogSnapshot.class, name = "log")
})
public interface ISnapshot {
    long getTimestamp();
    SnapshotType getType();
}

