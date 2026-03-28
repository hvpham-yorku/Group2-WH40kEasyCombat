package eecs2311.group2.wh40k_easycombat.service;

import eecs2311.group2.wh40k_easycombat.model.instance.Phase;
import eecs2311.group2.wh40k_easycombat.model.instance.Player;
import eecs2311.group2.wh40k_easycombat.model.snapshot.LogSnapshot;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

public class BattleLogService {
    private static final BattleLogService INSTANCE = new BattleLogService();
    private static final DateTimeFormatter TIME_FORMATTER =
            DateTimeFormatter.ofPattern("HH:mm:ss", Locale.ROOT);

    private final SnapshotService snapshotService = new SnapshotService();

    public static BattleLogService getInstance() {
        return INSTANCE;
    }

    public synchronized void clear() {
        snapshotService.clear();
    }

    public synchronized void log(String message) {
        if (message == null || message.isBlank()) {
            return;
        }

        snapshotService.pushLog(message.trim());
    }

    public synchronized void logTurnEvent(int round, Phase phase, Player actor, String message) {
        log(buildPrefix(round, phase, actor) + message);
    }

    public synchronized List<String> displayEntries() {
        List<LogSnapshot> snapshots = snapshotService.getLogSnapshots();
        if (snapshots.isEmpty()) {
            return List.of("Battle log is empty.");
        }

        return snapshots.stream()
                .map(this::formatSnapshot)
                .collect(Collectors.toList());
    }

    public synchronized String displayText() {
        return String.join("\n\n", displayEntries()) + "\n";
    }

    public synchronized int entryCount() {
        return snapshotService.getLogSnapshots().size();
    }

    private String formatSnapshot(LogSnapshot snapshot) {
        String timeText = TIME_FORMATTER.format(
                Instant.ofEpochMilli(snapshot.timestamp())
                        .atZone(ZoneId.systemDefault())
                        .toLocalTime()
        );
        return "[" + timeText + "] " + snapshot.message();
    }

    private String buildPrefix(int round, Phase phase, Player actor) {
        StringBuilder prefix = new StringBuilder();

        if (round > 0) {
            prefix.append("Round ").append(round);
        }

        if (phase != null) {
            if (prefix.length() > 0) {
                prefix.append(" | ");
            }
            prefix.append(phaseLabel(phase));
        }

        if (actor != null) {
            if (prefix.length() > 0) {
                prefix.append(" | ");
            }
            prefix.append(playerLabel(actor));
        }

        if (prefix.length() > 0) {
            prefix.insert(0, "[");
            prefix.append("] ");
        }

        return prefix.toString();
    }

    private String phaseLabel(Phase phase) {
        return switch (phase) {
            case COMMAND -> "Command";
            case MOVEMENT -> "Movement";
            case SHOOTING -> "Shooting";
            case CHARGE -> "Charge";
            case FIGHT -> "Fight";
        };
    }

    private String playerLabel(Player player) {
        return player == Player.DEFENDER ? "Defender" : "Attacker";
    }
}
