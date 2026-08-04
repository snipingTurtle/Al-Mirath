package com.example.al_mirath.service;

import com.example.al_mirath.model.Choice;
import com.example.al_mirath.model.FactionRelations;
import com.example.al_mirath.model.GameEvent;
import com.example.al_mirath.model.PlayerCharacter;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.LinkedHashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Covers the Threads of Fate rewind, which is the one system where a bug would
 * silently corrupt a player's run rather than throwing.
 */
class GameEngineRewindTest {

    private static final String[] STAT_KEYS = {
            "health", "wealth", "education", "reputation",
            "politicalPower", "morality", "familyLoyalty", "stress"
    };

    private static final String[] FACTION_KEYS = {
            "court", "nobles", "military", "scholars",
            "merchants", "commonPeople", "familyCouncil", "shadowNetwork"
    };

    private Map<String, Integer> statsOf(PlayerCharacter player) {
        Map<String, Integer> stats = new LinkedHashMap<>();

        for (String key : STAT_KEYS) {
            stats.put(key, player.getStatValue(key));
        }

        return stats;
    }

    private Map<String, Integer> factionsOf(FactionRelations factions) {
        Map<String, Integer> values = new LinkedHashMap<>();

        for (String key : FACTION_KEYS) {
            values.put(key, factions.getValue(key));
        }

        return values;
    }

    /** Drives the engine to a live event so a choice can be applied. */
    private GameEvent firstEvent(GameEngine engine) {
        GameEvent event = engine.getCurrentEvent();
        assertNotNull(event, "engine should offer an opening event");
        return event;
    }

    private Choice firstAvailableChoice(GameEngine engine, GameEvent event) {
        for (Choice choice : event.getChoices()) {
            if (engine.canChoose(choice)) {
                return choice;
            }
        }

        throw new IllegalStateException("no selectable choice on " + event.getTitle());
    }

    @Test
    @DisplayName("a rewind restores stats, factions, flags and age exactly")
    void rewindRestoresFullState() {
        GameEngine engine = new GameEngine();

        GameEvent event = firstEvent(engine);
        Choice choice = firstAvailableChoice(engine, event);

        Map<String, Integer> statsBefore = statsOf(engine.getPlayer());
        Map<String, Integer> factionsBefore = factionsOf(engine.getFactions());
        int ageBefore = engine.getPlayer().getAge();
        String statusBefore = engine.getPlayer().getCurrentStatus();

        engine.applyChoice(choice);

        assertTrue(engine.canRewind(), "a fresh life starts with a Thread and a snapshot");

        assertTrue(engine.spendFateToken());
        assertTrue(engine.rewindToLastSnapshot());

        assertEquals(statsBefore, statsOf(engine.getPlayer()), "stats must return to their pre-choice values");
        assertEquals(factionsBefore, factionsOf(engine.getFactions()), "faction standing must be restored");
        assertEquals(ageBefore, engine.getPlayer().getAge(), "the years added by the choice must be given back");
        assertEquals(statusBefore, engine.getPlayer().getCurrentStatus());
    }

    @Test
    @DisplayName("a rewind puts the player back in front of the same event")
    void rewindReturnsToSameEvent() {
        GameEngine engine = new GameEngine();

        GameEvent event = firstEvent(engine);
        String titleBefore = event.getTitle();

        engine.applyChoice(firstAvailableChoice(engine, event));

        engine.spendFateToken();
        engine.rewindToLastSnapshot();

        GameEvent afterRewind = engine.getCurrentEvent();

        assertNotNull(afterRewind);
        assertEquals(titleBefore, afterRewind.getTitle(),
                "the undone decision should be presented again, not replaced by a new roll");
    }

    @Test
    @DisplayName("the same choice can be re-applied after a rewind")
    void choiceIsReplayableAfterRewind() {
        GameEngine engine = new GameEngine();

        GameEvent event = firstEvent(engine);
        engine.applyChoice(firstAvailableChoice(engine, event));

        engine.spendFateToken();
        engine.rewindToLastSnapshot();

        GameEvent replayed = engine.getCurrentEvent();
        Choice choice = firstAvailableChoice(engine, replayed);

        String result = engine.applyChoice(choice);

        assertNotNull(result);
        assertFalse(result.startsWith("This choice is unavailable"),
                "the event must be fully playable again after a rewind");
    }

    @Test
    @DisplayName("spending the last Thread disables further rewinds")
    void tokensAreConsumed() {
        GameEngine engine = new GameEngine();

        int startingTokens = engine.getFateTokens();
        assertTrue(startingTokens > 0, "a life should begin with at least one Thread");

        engine.applyChoice(firstAvailableChoice(engine, firstEvent(engine)));

        for (int i = 0; i < startingTokens; i++) {
            assertTrue(engine.spendFateToken(), "each held Thread should be spendable");
        }

        assertEquals(0, engine.getFateTokens());
        assertFalse(engine.canRewind(), "with no Threads left there is nothing to spend");
        assertFalse(engine.spendFateToken(), "spending past zero must fail rather than go negative");
    }

    @Test
    @DisplayName("a rewind is impossible before any choice is made")
    void noRewindWithoutASnapshot() {
        GameEngine engine = new GameEngine();

        assertFalse(engine.canRewind(), "there is nothing to undo at the start of a life");
        assertFalse(engine.rewindToLastSnapshot());
    }

    @Test
    @DisplayName("accepting a consequence clears the undo point")
    void clearingSnapshotPreventsRewind() {
        GameEngine engine = new GameEngine();

        engine.applyChoice(firstAvailableChoice(engine, firstEvent(engine)));
        assertTrue(engine.canRewind());

        engine.clearSnapshot();

        assertFalse(engine.canRewind(), "a consequence the player accepted must not stay undoable");
    }

    @Test
    @DisplayName("a snapshot does not alias the live game state")
    void snapshotIsAnIndependentCopy() {
        GameEngine engine = new GameEngine();

        engine.applyChoice(firstAvailableChoice(engine, firstEvent(engine)));

        var snapshot = engine.getLastSnapshot();
        assertNotNull(snapshot);

        Map<String, Integer> captured = snapshot.getStats();
        captured.put("health", -999);

        // Mutating the returned map must not reach into the snapshot itself.
        assertNotEquals(-999, snapshot.getStats().get("health"),
                "getStats must hand back a defensive copy");
    }

    @Test
    @DisplayName("a rewound life is scored without the abandoned outcome")
    void scoreReflectsRewind() {
        GameEngine engine = new GameEngine();

        engine.applyChoice(firstAvailableChoice(engine, firstEvent(engine)));

        engine.spendFateToken();
        engine.rewindToLastSnapshot();

        assertEquals(1, engine.getRewindsUsed(), "the rewind should be counted for scoring");
        assertTrue(engine.calculateScore() >= 0, "score must never go negative");
    }
}
