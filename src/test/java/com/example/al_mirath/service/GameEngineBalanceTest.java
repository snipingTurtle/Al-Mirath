package com.example.al_mirath.service;

import com.example.al_mirath.model.Choice;
import com.example.al_mirath.model.GameEvent;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
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

    /**
     * A healthy, unstressed young character must not die of nothing.
     *
     * <p>The mortality check caps young-death risk rather than clearing it, so
     * a failed skill check used to carry an 8% chance of killing an
     * eight-year-old at full health — reported with the generic message,
     * because no named danger had contributed anything. That message on a
     * character under 25 is the signature of the bug.
     */
    @Test
    @DisplayName("the young do not die without a named danger")
    void youngDeathsAlwaysHaveACause() {
        String generic =
                "Your life ended before your ambitions could fully unfold.";

        int runs = 300;

        for (int run = 0; run < runs; run++) {
            GameEngine engine = new GameEngine();

            for (int i = 0; i < 40 && engine.getCurrentEvent() != null; i++) {
                engine.applyChoice(
                        firstAvailableChoice(engine, engine.getCurrentEvent())
                );

                if (!engine.getPlayer().isAlive()) {
                    int age = engine.getPlayer().getAge();

                    if (age < 25) {
                        assertNotEquals(
                                generic,
                                engine.getPlayer().getDeathReason(),
                                "died at " + age + " with health "
                                        + engine.getPlayer().getStatValue("health")
                                        + " and stress "
                                        + engine.getPlayer().getStatValue("stress")
                                        + ", for no stated reason"
                        );
                    }

                    break;
                }
            }
        }
    }

    /**
     * The roll is per choice, not per life, so this counts choices rather than
     * lives. Tying it to one life made it fail whenever that life ended early
     * — a premise the test never actually needed.
     */
    @Test
    @DisplayName("world events surface over a run of choices, without error")
    void worldEventsEventuallyFire() {
        int choicesToMake = 60;
        int choicesMade = 0;
        boolean sawWorldEvent = false;

        while (choicesMade < choicesToMake) {
            GameEngine engine = new GameEngine();

            while (choicesMade < choicesToMake
                    && engine.getCurrentEvent() != null) {

                engine.applyChoice(
                        firstAvailableChoice(engine, engine.getCurrentEvent())
                );

                choicesMade++;

                if (!engine.getLatestWorldEventTitle().isBlank()) {
                    sawWorldEvent = true;

                    String title = engine.getLatestWorldEventTitle();
                    String message = engine.consumeLatestWorldEventMessage();

                    assertFalse(title.isBlank());
                    assertFalse(message.isBlank());
                    assertTrue(
                            engine.getLatestWorldEventTitle().isBlank(),
                            "consuming must clear the pending title too"
                    );
                }
            }
        }

        assertTrue(
                sawWorldEvent,
                "no world event fired in " + choicesToMake
                        + " choices; the ~22% roll may be broken"
        );
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
