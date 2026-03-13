package eecs2311.group2.wh40k_easycombat.manager;

public final class RoundManager {

    private RoundManager() {
    }

    public static RoundState initialize(String roundText, String blueCpText, String redCpText) {
        return fromTexts(roundText, blueCpText, redCpText);
    }

    public static RoundState fromTexts(String roundText, String blueCpText, String redCpText) {
        return new RoundState(
                parseRound(roundText),
                CommandPointManager.parseCp(blueCpText),
                CommandPointManager.parseCp(redCpText)
        );
    }

    public static RoundState nextRound(RoundState state) {
        RoundState current = state == null ? initialize(null, null, null) : state;
        return new RoundState(
                current.round() + 1,
                current.blueCp() + 1,
                current.redCp() + 1
        );
    }

    public static RoundState addBlueCp(RoundState state, int delta) {
        RoundState current = state == null ? initialize(null, null, null) : state;
        return new RoundState(
                current.round(),
                current.blueCp() + delta,
                current.redCp()
        );
    }

    public static RoundState addRedCp(RoundState state, int delta) {
        RoundState current = state == null ? initialize(null, null, null) : state;
        return new RoundState(
                current.round(),
                current.blueCp(),
                current.redCp() + delta
        );
    }

    private static int parseRound(String text) {
        if (text == null || text.isBlank()) {
            return 1;
        }

        String cleaned = text.replaceAll("[^0-9-]", "").trim();
        if (cleaned.isBlank()) {
            return 1;
        }

        try {
            return Math.max(1, Integer.parseInt(cleaned));
        } catch (Exception e) {
            return 1;
        }
    }

    public record RoundState(int round, int blueCp, int redCp) {
        public RoundState {
            round = Math.max(1, round);
            blueCp = Math.max(0, blueCp);
            redCp = Math.max(0, redCp);
        }
    }
}
