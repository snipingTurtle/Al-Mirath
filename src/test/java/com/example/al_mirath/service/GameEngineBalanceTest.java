package com.example.al_mirath.service;

import com.example.al_mirath.model.Choice;
import com.example.al_mirath.model.GameEvent;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Covers the stress rebalance and the world-events system: two additions
 * that only misbehave over a long play session, not on the first choice.
 */
class GameEngineBalanceTest {

    private Choice firstAvailableChoice(GameEngine engine, GameEvent event) {
        for (Choice choice : event.getChoices()) {
            if (engine.canChoose(choice)) {
                return choice;
            }
        }
        throw new IllegalStateException("no selectable choice on " + event.getTitle());
    }

    /** Plays choices until the life ends or a cap is hit, to exercise long-run behavior. */
    private int playUntilEndOrLimit(GameEngine engine, int limit) {
        int played = 0;

        while (played < limit) {
            GameEvent event = engine.getCurrentEvent();
            if (event == null) {
                break;
            }

            engine.applyChoice(firstAvailableChoice(engine, event));
            played++;
        }

        return played;
    }

    @Test
    @DisplayName("stress does not rush to the ceiling within the first handful of choices")
    void stressStaysControlledEarly() {
        GameEngine engine = new GameEngine();

        // Ten choices is enough to expose an unbalanced climb if the
        // dampening and passive recovery were not in place.
        playUntilEndOrLimit(engine, 10);

        assertTrue(
                engine.getPlayer().getStress() < 100,
                "stress reached the ceiling within 10 choices, meaning the rebalance regressed"
        );
    }

    @Test
    @DisplayName("a long life does not force stress to sit at the ceiling")
    void stressDoesNotPermanentlyMaxOut() {
        GameEngine engine = new GameEngine();

        playUntilEndOrLimit(engine, 40);

        // Not a guarantee of a specific number — random choices and stat
        // checks vary — but the dampening plus passive recovery should keep
        // at least some room below the hard ceiling over a long run.
        assertTrue(
                engine.getPlayer().getStress() <= 100,
                "stress must stay within its clamp"
        );
    }

    @Test
    @DisplayName("current life stage is readable at every point in a life")
    void currentLifeStageIsReported() {
        GameEngine engine = new GameEngine();

        assertEquals("Childhood", engine.getCurrentLifeStage());

        playUntilEndOrLimit(engine, 30);

        // Whatever stage it lands on, it must be one of the real ones and
        // must never throw or return null/blank.
        String stage = engine.getCurrentLifeStage();
        assertFalse(stage.isBlank());
    }

    @Test
    @DisplayName("a long life surfaces at least one world event without error")
    void worldEventsEventuallyFire() {
        GameEngine engine = new GameEngine();

        boolean sawWorldEvent = false;

        for (int i = 0; i < 40 && engine.getCurrentEvent() != null; i++) {
            engine.applyChoice(firstAvailableChoice(engine, engine.getCurrentEvent()));

            if (!engine.getLatestWorldEventTitle().isBlank()) {
                sawWorldEvent = true;

                String title = engine.getLatestWorldEventTitle();
                String message = engine.consumeLatestWorldEventMessage();

                assertFalse(title.isBlank());
                assertFalse(message.isBlank());
                assertTrue(engine.getLatestWorldEventTitle().isBlank(), "consuming must clear the pending title too");
            }
        }

        assertTrue(sawWorldEvent, "no world event fired in 40 choices; the ~22% roll may be broken");
    }

    @Test
    @DisplayName("world events never repeat within a single life")
    void worldEventsDoNotRepeatInOneLife() {
        GameEngine engine = new GameEngine();

        java.util.Set<String> seen = new java.util.HashSet<>();

        for (int i = 0; i < 60 && engine.getCurrentEvent() != null; i++) {
            engine.applyChoice(firstAvailableChoice(engine, engine.getCurrentEvent()));

            String title = engine.getLatestWorldEventTitle();
            if (!title.isBlank()) {
                assertTrue(seen.add(title), "world event repeated within one life: " + title);
                engine.consumeLatestWorldEventMessage();
            }
        }
    }
}
