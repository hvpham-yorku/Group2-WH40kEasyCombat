package eecs2311.group2.wh40k_easycombat.service;

import eecs2311.group2.wh40k_easycombat.model.instance.BattleState;
import eecs2311.group2.wh40k_easycombat.model.instance.Phase;
import eecs2311.group2.wh40k_easycombat.model.instance.Player;
import eecs2311.group2.wh40k_easycombat.model.snapshot.LogSnapshot;
import eecs2311.group2.wh40k_easycombat.model.snapshot.SnapshotType;
import eecs2311.group2.wh40k_easycombat.model.snapshot.StateSnapshot;

import org.junit.jupiter.api.*;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class SnapshotServiceTest {

    private SnapshotService service;

    @BeforeEach
    void setUp() {
        service = new SnapshotService();
    }

    // ========== LogSnapshot model ==========

    @Nested
    @DisplayName("LogSnapshot")
    class LogSnapshotTests {

        @Test
        @DisplayName("stores message and timestamp")
        void storesFields() {
            LogSnapshot log = new LogSnapshot("attack hit", 1000L);
            assertEquals("attack hit", log.message());
            assertEquals(1000L, log.timestamp());
        }

        @Test
        @DisplayName("type is LOG")
        void typeIsLog() {
            assertEquals(SnapshotType.LOG, new LogSnapshot("msg", 1L).getType());
        }

        @Test
        @DisplayName("getTimestamp returns record timestamp")
        void getTimestamp() {
            assertEquals(42L, new LogSnapshot("msg", 42L).getTimestamp());
        }
    }

    // ========== StateSnapshot model ==========

    @Nested
    @DisplayName("StateSnapshot")
    class StateSnapshotTests {

        @Test
        @DisplayName("stores BattleState and timestamp")
        void storesFields() {
            BattleState state = new BattleState("Mission");
            StateSnapshot snap = new StateSnapshot(state, 2000L);
            assertSame(state, snap.state());
            assertEquals(2000L, snap.timestamp());
        }

        @Test
        @DisplayName("type is STATE")
        void typeIsState() {
            assertEquals(SnapshotType.STATE, new StateSnapshot(new BattleState(), 1L).getType());
        }
    }

    // ========== SnapshotType enum ==========

    @Nested
    @DisplayName("SnapshotType")
    class SnapshotTypeTests {

        @Test
        @DisplayName("has STATE, LOG, ACTION values")
        void allValues() {
            assertEquals(3, SnapshotType.values().length);
            assertNotNull(SnapshotType.valueOf("STATE"));
            assertNotNull(SnapshotType.valueOf("LOG"));
            assertNotNull(SnapshotType.valueOf("ACTION"));
        }
    }

    // ========== Empty service ==========

    @Nested
    @DisplayName("Empty service")
    class EmptyService {

        @Test
        @DisplayName("isEmpty returns true on new service")
        void isEmpty() {
            assertTrue(service.isEmpty());
        }

        @Test
        @DisplayName("size is zero on new service")
        void sizeZero() {
            assertEquals(0, service.size());
        }

        @Test
        @DisplayName("undo returns empty on empty history")
        void undoEmpty() {
            assertTrue(service.undo().isEmpty());
        }

        @Test
        @DisplayName("peekLatestState returns empty on empty history")
        void peekEmpty() {
            assertTrue(service.peekLatestState().isEmpty());
        }

        @Test
        @DisplayName("getBattleLogs returns empty list on empty history")
        void logsEmpty() {
            assertTrue(service.getBattleLogs().isEmpty());
        }
    }

    // ========== pushState ==========

    @Nested
    @DisplayName("pushState")
    class PushState {

        @Test
        @DisplayName("increases size by 1")
        void increasesSize() {
            service.pushState(new BattleState("M"));
            assertEquals(1, service.size());
            assertFalse(service.isEmpty());
        }

        @Test
        @DisplayName("stores a deep copy so original mutations don't affect snapshot")
        void deepCopy() {
            BattleState original = new BattleState("Original");
            service.pushState(original);
            original.setMissionName("Mutated");

            assertEquals("Original", service.peekLatestState().orElseThrow().getMissionName());
        }

        @Test
        @DisplayName("preserves round, phase, and active player in deep copy")
        void preservesDetails() {
            BattleState state = new BattleState("M");
            state.setCurrentRound(3);
            state.setCurrentPhase(Phase.SHOOTING);
            state.setActivePlayer(Player.DEFENDER);
            service.pushState(state);

            BattleState peeked = service.peekLatestState().orElseThrow();
            assertEquals(3, peeked.getCurrentRound());
            assertEquals(Phase.SHOOTING, peeked.getCurrentPhase());
            assertEquals(Player.DEFENDER, peeked.getActivePlayer());
        }

        @Test
        @DisplayName("preserves battle over, max rounds and army points in deep copy")
        void preservesExpandedRuntimeState() {
            BattleState state = new BattleState("Mission", 4);
            state.setBattleOver(true);
            state.setCurrentRound(2);

            var attacker = new eecs2311.group2.wh40k_easycombat.model.instance.ArmyInstance(
                    1,
                    "Attacker",
                    "faction",
                    "Faction",
                    "detachment"
            );
            attacker.setCurrentCp(5);
            attacker.setCurrentVp(12);
            state.setAttackerArmy(attacker);

            service.pushState(state);

            BattleState peeked = service.peekLatestState().orElseThrow();
            assertTrue(peeked.isBattleOver());
            assertEquals(4, peeked.getMaxRounds());
            assertEquals(5, peeked.getCurrentCp(Player.ATTACKER));
            assertEquals(12, peeked.getCurrentVp(Player.ATTACKER));

            attacker.setCurrentCp(0);
            assertEquals(5, peeked.getCurrentCp(Player.ATTACKER));
        }
    }

    // ========== pushLog ==========

    @Nested
    @DisplayName("pushLog")
    class PushLog {

        @Test
        @DisplayName("increases size by 1")
        void increasesSize() {
            service.pushLog("something happened");
            assertEquals(1, service.size());
        }

        @Test
        @DisplayName("log appears in getBattleLogs")
        void appearsInLogs() {
            service.pushLog("Unit attacked");
            assertEquals("Unit attacked", service.getBattleLogs().get(0));
        }

        @Test
        @DisplayName("multiple logs are returned in order")
        void multipleLogsInOrder() {
            service.pushLog("first");
            service.pushLog("second");
            service.pushLog("third");

            List<String> logs = service.getBattleLogs();
            assertEquals(3, logs.size());
            assertEquals("first", logs.get(0));
            assertEquals("second", logs.get(1));
            assertEquals("third", logs.get(2));
        }
    }

    // ========== getBattleLogs ==========

    @Nested
    @DisplayName("getBattleLogs")
    class GetBattleLogs {

        @Test
        @DisplayName("filters out state snapshots, only returns log messages")
        void filtersStates() {
            service.pushState(new BattleState("M"));
            service.pushLog("log1");
            service.pushState(new BattleState("M2"));
            service.pushLog("log2");

            List<String> logs = service.getBattleLogs();
            assertEquals(2, logs.size());
            assertEquals("log1", logs.get(0));
            assertEquals("log2", logs.get(1));
        }
    }

    // ========== peekLatestState ==========

    @Nested
    @DisplayName("peekLatestState")
    class PeekLatestState {

        @Test
        @DisplayName("returns latest state even with logs after it")
        void returnsLatestPastLogs() {
            service.pushState(new BattleState("First"));
            service.pushLog("some log");
            service.pushLog("another log");

            assertEquals("First", service.peekLatestState().orElseThrow().getMissionName());
        }

        @Test
        @DisplayName("returns the most recent state when multiple exist")
        void returnsMostRecent() {
            service.pushState(new BattleState("Old"));
            service.pushState(new BattleState("New"));

            assertEquals("New", service.peekLatestState().orElseThrow().getMissionName());
        }

        @Test
        @DisplayName("returns empty when only logs exist")
        void emptyWhenOnlyLogs() {
            service.pushLog("just a log");
            assertTrue(service.peekLatestState().isEmpty());
        }
    }

    // ========== undo (states only — no logs between states) ==========

    @Nested
    @DisplayName("undo")
    class Undo {

        @Test
        @DisplayName("removes current state and returns previous state")
        void returnsPreviousState() {
            service.pushState(new BattleState("First"));
            service.pushState(new BattleState("Second"));

            Optional<BattleState> undone = service.undo();
            assertTrue(undone.isPresent());
            assertEquals("First", undone.get().getMissionName());
        }

        @Test
        @DisplayName("undo with only one state returns empty")
        void singleStateReturnsEmpty() {
            service.pushState(new BattleState("Only"));
            assertTrue(service.undo().isEmpty());
        }

        @Test
        @DisplayName("double undo goes back two states")
        void doubleUndo() {
            service.pushState(new BattleState("A"));
            service.pushState(new BattleState("B"));
            service.pushState(new BattleState("C"));

            service.undo(); // C -> B
            Optional<BattleState> result = service.undo(); // B -> A
            assertTrue(result.isPresent());
            assertEquals("A", result.get().getMissionName());
        }

        @Test
        @DisplayName("undo past all states returns empty")
        void undoPastAllReturnsEmpty() {
            service.pushState(new BattleState("A"));
            service.pushState(new BattleState("B"));

            service.undo(); // B -> A
            service.undo(); // A -> empty
            assertTrue(service.undo().isEmpty());
        }
    }

    // ========== clear ==========

    @Nested
    @DisplayName("clear")
    class Clear {

        @Test
        @DisplayName("clear empties the history")
        void clearsHistory() {
            service.pushState(new BattleState("M"));
            service.pushLog("log");

            service.clear();
            assertTrue(service.isEmpty());
            assertEquals(0, service.size());
            assertTrue(service.peekLatestState().isEmpty());
            assertTrue(service.getBattleLogs().isEmpty());
        }
    }

    // ========== Mixed state + log interactions ==========

    @Nested
    @DisplayName("Mixed state and log interactions")
    class MixedInteractions {

        @Test
        @DisplayName("interleaved pushState and pushLog tracks size correctly")
        void interleavedSize() {
            service.pushState(new BattleState("M1"));
            service.pushLog("log1");
            service.pushState(new BattleState("M2"));
            service.pushLog("log2");
            service.pushLog("log3");

            assertEquals(5, service.size());
        }

        @Test
        @DisplayName("peekLatestState ignores trailing logs")
        void peekIgnoresTrailingLogs() {
            service.pushState(new BattleState("State1"));
            service.pushLog("trailing1");
            service.pushLog("trailing2");
            service.pushLog("trailing3");

            assertEquals("State1", service.peekLatestState().orElseThrow().getMissionName());
        }
    }
}
