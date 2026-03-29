package eecs2311.group2.wh40k_easycombat.service.mission;

import eecs2311.group2.wh40k_easycombat.model.instance.GameSetupConfig;
import eecs2311.group2.wh40k_easycombat.model.instance.Player;
import eecs2311.group2.wh40k_easycombat.model.mission.MissionCard;
import eecs2311.group2.wh40k_easycombat.model.mission.MissionType;
import eecs2311.group2.wh40k_easycombat.model.mission.SecondaryMissionMode;
import eecs2311.group2.wh40k_easycombat.viewmodel.MissionEntryVM;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class MissionSessionServiceTest {

    private final MissionSessionService service = MissionSessionService.getInstance();
    private final MissionService missionService = MissionService.getInstance();

    @BeforeEach
    void setUp() {
        service.reset();
    }

    @AfterEach
    void tearDown() {
        service.reset();
    }

    @Test
    @DisplayName("tactical missions draw at most two cards per turn and reset next turn")
    void tacticalMissionsDrawAtMostTwoCardsPerTurn() {
        service.initialize(tacticalConfig());

        List<MissionEntryVM> firstDraw = service.drawFor(Player.ATTACKER);

        assertEquals(2, firstDraw.size());
        assertFalse(service.canDraw(Player.ATTACKER));
        assertEquals(0, service.drawCountFor(Player.ATTACKER));

        service.startTurn(Player.ATTACKER);

        assertTrue(service.canDraw(Player.ATTACKER));
        assertEquals(2, service.drawCountFor(Player.ATTACKER));
    }

    @Test
    @DisplayName("unfinished tactical missions remain active across turns and new draws add to them")
    void unfinishedTacticalMissionsRemainActiveAcrossTurns() {
        service.initialize(tacticalConfig());
        List<MissionEntryVM> firstDraw = service.drawFor(Player.ATTACKER);
        Set<String> firstTitles = firstDraw.stream().map(MissionEntryVM::getName).collect(java.util.stream.Collectors.toSet());

        service.startTurn(Player.ATTACKER);
        List<MissionEntryVM> secondTurnEntries = service.drawFor(Player.ATTACKER);

        assertEquals(4, secondTurnEntries.size());
        assertTrue(secondTurnEntries.stream().map(MissionEntryVM::getName).collect(java.util.stream.Collectors.toSet()).containsAll(firstTitles));
    }

    @Test
    @DisplayName("abandoning multiple missions only grants the command point bonus once per turn")
    void abandoningMultipleMissionsOnlyGrantsCpOncePerTurn() {
        service.initialize(tacticalConfig());
        List<MissionEntryVM> entries = service.drawFor(Player.ATTACKER);

        assertTrue(service.abandon(Player.ATTACKER, entries.get(0).getName()));
        assertTrue(service.grantAbandonCpIfAvailable(Player.ATTACKER));

        assertTrue(service.abandon(Player.ATTACKER, entries.get(1).getName()));
        assertFalse(service.grantAbandonCpIfAvailable(Player.ATTACKER));
    }

    @Test
    @DisplayName("completing a tactical mission keeps it in the list and marks it completed")
    void completingTacticalMissionKeepsCompletedEntry() {
        service.initialize(tacticalConfig());
        MissionEntryVM mission = service.drawFor(Player.ATTACKER).getFirst();

        assertTrue(service.complete(Player.ATTACKER, mission.getName()));

        List<MissionEntryVM> activeEntries = service.activeEntriesFor(Player.ATTACKER);
        assertEquals(2, activeEntries.size());
        MissionEntryVM completed = activeEntries.stream()
                .filter(entry -> entry.getName().equals(mission.getName()))
                .findFirst()
                .orElseThrow();
        assertEquals("Completed", completed.getState());
        assertFalse(service.complete(Player.ATTACKER, mission.getName()));
    }

    @Test
    @DisplayName("fixed mission setup starts with two active fixed missions")
    void fixedMissionSetupStartsWithTwoActiveFixedMissions() {
        MissionCard fixedOne = new MissionCard(MissionType.SECONDARY, "Fixed One", "Intro", List.of("Body"));
        MissionCard fixedTwo = new MissionCard(MissionType.SECONDARY, "Fixed Two", "Intro", List.of("Body"));
        GameSetupConfig config = new GameSetupConfig(
                null,
                null,
                1000,
                "Incursion",
                missionService.getPrimaryMissions().getFirst(),
                SecondaryMissionMode.FIXED,
                SecondaryMissionMode.TACTICAL,
                List.of(fixedOne, fixedTwo),
                List.of(),
                5,
                true
        );

        service.initialize(config);

        List<MissionEntryVM> entries = service.activeEntriesFor(Player.ATTACKER);
        assertEquals(2, entries.size());
        assertTrue(entries.stream().allMatch(entry -> "Active".equals(entry.getState())));
        assertTrue(entries.stream().allMatch(entry -> "Fixed".equals(entry.getMode())));
    }

    private GameSetupConfig tacticalConfig() {
        return new GameSetupConfig(
                null,
                null,
                1000,
                "Incursion",
                missionService.getPrimaryMissions().getFirst(),
                SecondaryMissionMode.TACTICAL,
                SecondaryMissionMode.TACTICAL,
                List.of(),
                List.of(),
                5,
                true
        );
    }
}
