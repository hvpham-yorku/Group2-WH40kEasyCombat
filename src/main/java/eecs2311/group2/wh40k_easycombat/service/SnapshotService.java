package eecs2311.group2.wh40k_easycombat.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import eecs2311.group2.wh40k_easycombat.model.instance.BattleState;
import eecs2311.group2.wh40k_easycombat.model.snapshot.ISnapshot;
import eecs2311.group2.wh40k_easycombat.model.snapshot.LogSnapshot;
import eecs2311.group2.wh40k_easycombat.model.snapshot.StateSnapshot;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class SnapshotService {
    private final ArrayList<ISnapshot> history;
    private final ObjectMapper mapper = new ObjectMapper()
        .enable(SerializationFeature.INDENT_OUTPUT);

    private static final int MAX_HISTORY_SIZE = 10240;

    public SnapshotService() {
        this.history = new ArrayList<>();
    }

    public void pushState(BattleState state) {
        checkCapacity();
        history.add(new StateSnapshot(state.deepCopy(), System.currentTimeMillis()));
    }

    public void pushLog(String message) {
        checkCapacity();
        history.add(new LogSnapshot(message, System.currentTimeMillis()));
    }

    public Optional<BattleState> undo() {
        if (history.isEmpty()) return Optional.empty();

        boolean removedCurrentState = false;
        while (!history.isEmpty() && !removedCurrentState) {
            ISnapshot top = history.removeLast();
            if (top instanceof StateSnapshot) {
                removedCurrentState = true;
            }
        }

        while (!history.isEmpty()) {
            ISnapshot top = history.getLast();

            if (top instanceof StateSnapshot ss) {
                return Optional.of(ss.state());
            }
        }

        return Optional.empty();
    }

    public List<String> getBattleLogs() {
        return history.stream()
            .filter(s -> s instanceof LogSnapshot)
            .map(s -> ((LogSnapshot) s).message())
            .collect(Collectors.toList());
    }

    public List<LogSnapshot> getLogSnapshots() {
        return history.stream()
                .filter(s -> s instanceof LogSnapshot)
                .map(s -> (LogSnapshot) s)
                .collect(Collectors.toList());
    }

    public Optional<BattleState> peekLatestState() {
        for (int i = history.size() - 1; i >= 0; i--) {
            if (history.get(i) instanceof StateSnapshot ss) {
                return Optional.of(ss.state());
            }
        }
        return Optional.empty();
    }

    public boolean isEmpty() {
        return history.isEmpty();
    }

    public int size() {
        return history.size();
    }

    public void clear() {
        history.clear();
    }

    private void checkCapacity() {
        if (history.size() >= MAX_HISTORY_SIZE) {
            history.removeFirst();
        }
    }

    // TODO: still in testing
    public void exportToFile(String filePath) throws IOException {
        mapper.writeValue(new File(filePath), this.history);
    }

    // TODO: still in testing
    public void loadFromFile(String filePath) throws IOException {
        File file = new File(filePath);
        if (!file.exists()) throw new IOException("File not found: " + filePath);

        ISnapshot[] loadedData = mapper.readValue(file, ISnapshot[].class);
        this.history.clear();
        this.history.addAll(Arrays.asList(loadedData));
    }
}
