package eecs2311.group2.wh40k_easycombat.service.mission;

import eecs2311.group2.wh40k_easycombat.model.instance.GameSetupConfig;
import eecs2311.group2.wh40k_easycombat.model.instance.Player;
import eecs2311.group2.wh40k_easycombat.model.mission.MissionCard;
import eecs2311.group2.wh40k_easycombat.model.mission.SecondaryMissionMode;
import eecs2311.group2.wh40k_easycombat.viewmodel.MissionEntryVM;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Random;

public class MissionSessionService {
    private static final MissionSessionService INSTANCE = new MissionSessionService();

    private final MissionService missionService = MissionService.getInstance();
    private final Random random = new Random();

    private MissionCard primaryMission;

    private SecondaryMissionMode blueMode = SecondaryMissionMode.TACTICAL;
    private SecondaryMissionMode redMode = SecondaryMissionMode.TACTICAL;

    private final List<MissionCard> blueDeck = new ArrayList<>();
    private final List<MissionCard> redDeck = new ArrayList<>();
    private final List<MissionCard> blueDiscard = new ArrayList<>();
    private final List<MissionCard> redDiscard = new ArrayList<>();
    private final List<MissionEntryVM> blueActive = new ArrayList<>();
    private final List<MissionEntryVM> redActive = new ArrayList<>();

    private int blueDrawsThisTurn = 0;
    private int redDrawsThisTurn = 0;
    private boolean blueAbandonCpGrantedThisTurn = false;
    private boolean redAbandonCpGrantedThisTurn = false;
    private static final int MAX_TACTICAL_DRAWS_PER_TURN = 2;

    public static MissionSessionService getInstance() {
        return INSTANCE;
    }

    public synchronized void initialize(GameSetupConfig config) {
        reset();

        if (config == null) {
            List<MissionCard> primaries = missionService.getPrimaryMissions();
            if (!primaries.isEmpty()) {
                primaryMission = primaries.get(0);
            }
            blueDeck.addAll(missionService.getSecondaryMissions());
            redDeck.addAll(missionService.getSecondaryMissions());
            return;
        }

        primaryMission = config.primaryMission();
        blueMode = config.blueSecondaryMode();
        redMode = config.redSecondaryMode();

        if (blueMode == SecondaryMissionMode.FIXED) {
            for (MissionCard card : config.blueFixedSecondaryMissions()) {
                blueActive.add(fixedEntry(card));
            }
        } else {
            blueDeck.addAll(missionService.getSecondaryMissions());
        }

        if (redMode == SecondaryMissionMode.FIXED) {
            for (MissionCard card : config.redFixedSecondaryMissions()) {
                redActive.add(fixedEntry(card));
            }
        } else {
            redDeck.addAll(missionService.getSecondaryMissions());
        }
    }

    public synchronized void reset() {
        primaryMission = null;
        blueMode = SecondaryMissionMode.TACTICAL;
        redMode = SecondaryMissionMode.TACTICAL;
        blueDeck.clear();
        redDeck.clear();
        blueDiscard.clear();
        redDiscard.clear();
        blueActive.clear();
        redActive.clear();
        blueDrawsThisTurn = 0;
        redDrawsThisTurn = 0;
        blueAbandonCpGrantedThisTurn = false;
        redAbandonCpGrantedThisTurn = false;
    }

    public synchronized MissionCard primaryMission() {
        return primaryMission;
    }

    public synchronized SecondaryMissionMode modeFor(Player player) {
        return player == Player.ATTACKER ? blueMode : redMode;
    }

    public synchronized List<MissionEntryVM> activeEntriesFor(Player player) {
        return snapshot(player == Player.ATTACKER ? blueActive : redActive);
    }

    public synchronized boolean canDraw(Player player) {
        return modeFor(player) == SecondaryMissionMode.TACTICAL
                && drawCountFor(player) > 0;
    }

    

    public synchronized int drawCountFor(Player player) {
        if (modeFor(player) != SecondaryMissionMode.TACTICAL) {
            return 0;
        }
        return Math.max(0, MAX_TACTICAL_DRAWS_PER_TURN - drawsThisTurn(player));
    }

    public synchronized List<MissionEntryVM> drawFor(Player player) {
        if (!canDraw(player)) {
            return activeEntriesFor(player);
        }

        List<MissionCard> deck = deck(player);
        List<MissionCard> discard = discard(player);
        List<MissionEntryVM> active = activeList(player);
        int remainingDraws = drawCountFor(player);

        while (remainingDraws > 0) {
            if (deck.isEmpty()) {
                if (discard.isEmpty()) {
                    break;
                }
                deck.addAll(discard);
                discard.clear();
            }

            int index = random.nextInt(deck.size());
            MissionCard drawn = deck.remove(index);
            active.add(activeEntry(drawn));
            incrementDrawsThisTurn(player);
            remainingDraws--;
        }

        return snapshot(active);
    }

    public synchronized boolean abandon(Player player, String missionTitle) {
        if (modeFor(player) != SecondaryMissionMode.TACTICAL) {
            return false;
        }

        MissionEntryVM removed = removeActiveEntry(player, missionTitle);
        if (removed == null || removed.getMissionCard() == null) {
            return false;
        }

        discard(player).add(removed.getMissionCard());
        return true;
    }

    public synchronized void startTurn(Player player) {
        if (player == Player.ATTACKER) {
            blueDrawsThisTurn = 0;
            blueAbandonCpGrantedThisTurn = false;
            return;
        }

        redDrawsThisTurn = 0;
        redAbandonCpGrantedThisTurn = false;
    }

    public synchronized boolean grantAbandonCpIfAvailable(Player player) {
        if (player == Player.ATTACKER) {
            if (blueAbandonCpGrantedThisTurn) {
                return false;
            }
            blueAbandonCpGrantedThisTurn = true;
            return true;
        }

        if (redAbandonCpGrantedThisTurn) {
            return false;
        }
        redAbandonCpGrantedThisTurn = true;
        return true;
    }

    public synchronized boolean complete(Player player, String missionTitle) {
        MissionEntryVM entry = findActiveEntry(player, missionTitle);
        if (entry == null || entry.getMissionCard() == null) {
            return false;
        }

        if ("Completed".equalsIgnoreCase(entry.getState())) {
            return false;
        }

        if (modeFor(player) == SecondaryMissionMode.TACTICAL) {
            entry.setState("Completed");
            return true;
        }

        return true;
    }

    private MissionEntryVM fixedEntry(MissionCard card) {
        MissionEntryVM vm = new MissionEntryVM(card);
        vm.setState("Active");
        vm.setMode("Fixed");
        return vm;
    }

    private MissionEntryVM activeEntry(MissionCard card) {
        MissionEntryVM vm = new MissionEntryVM(card);
        vm.setState("Active");
        vm.setMode("Tactical");
        return vm;
    }

    private List<MissionEntryVM> snapshot(List<MissionEntryVM> entries) {
        List<MissionEntryVM> result = new ArrayList<>();
        for (MissionEntryVM entry : entries) {
            MissionEntryVM copy = new MissionEntryVM(entry.getMissionCard());
            copy.setState(entry.getState());
            copy.setMode(entry.getMode());
            result.add(copy);
        }
        return result;
    }

    private List<MissionEntryVM> activeList(Player player) {
        return player == Player.ATTACKER ? blueActive : redActive;
    }

    private List<MissionCard> deck(Player player) {
        return player == Player.ATTACKER ? blueDeck : redDeck;
    }

    private List<MissionCard> discard(Player player) {
        return player == Player.ATTACKER ? blueDiscard : redDiscard;
    }

    private int drawsThisTurn(Player player) {
        return player == Player.ATTACKER ? blueDrawsThisTurn : redDrawsThisTurn;
    }

    private void incrementDrawsThisTurn(Player player) {
        if (player == Player.ATTACKER) {
            blueDrawsThisTurn++;
            return;
        }

        redDrawsThisTurn++;
    }

    private MissionEntryVM removeActiveEntry(Player player, String missionTitle) {
        List<MissionEntryVM> active = activeList(player);
        for (int i = 0; i < active.size(); i++) {
            MissionEntryVM entry = active.get(i);
            if (matchesTitle(entry, missionTitle)) {
                return active.remove(i);
            }
        }
        return null;
    }

    private MissionEntryVM findActiveEntry(Player player, String missionTitle) {
        for (MissionEntryVM entry : activeList(player)) {
            if (matchesTitle(entry, missionTitle)) {
                return entry;
            }
        }
        return null;
    }

    private boolean matchesTitle(MissionEntryVM entry, String missionTitle) {
        if (entry == null || entry.getMissionCard() == null) {
            return false;
        }
        return normalize(entry.getName()).equals(normalize(missionTitle));
    }

    private String normalize(String text) {
        return text == null ? "" : text.trim().toLowerCase(Locale.ROOT);
    }
}
