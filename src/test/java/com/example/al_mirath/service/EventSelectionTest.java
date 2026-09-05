package com.example.al_mirath.service;

import com.example.al_mirath.model.Choice;
import com.example.al_mirath.model.GameEvent;
import com.example.al_mirath.model.PlayerCharacter;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * How the engine chooses what happens next.
 *
 * <p>The recurring cast used to win outright over the general pool, which
 * made an arc reliable at the cost of making every run open with the same
 * scene. These are the two properties that were in tension, held apart: the
 * opening has to vary, and the cast still has to show up.
 */
class EventSelectionTest {

    /** Plays a life to its end, choosing at random from what is offered. */
    private GameEngine playALife(Random random) {
        GameEngine engine = new GameEngine();

        for (int i = 0; i < 60; i++) {
            GameEvent event = engine.getCurrentEvent();

            if (event == null) {
                break;
            }

            List<Choice> available = new ArrayList<>();

            for (Choice choice : event.getChoices()) {
                if (engine.canChoose(choice)) {
                    available.add(choice);
                }
            }

            if (available.isEmpty()) {
                break;
            }

            engine.applyChoice(
                    available.get(random.nextInt(available.size()))
            );
        }

        return engine;
    }

    /**
     * Cast events are titled after generated names, so the same scene has a
     * different title in every run. Collapsed to the scene it actually is.
     */
    private String scene(String title) {
        if (title.contains("Shares a Secret")) {
            return "friend introduction";
        }

        if (title.contains("Answers First")) {
            return "rival introduction";
        }

        if (title.contains("Stops You in the Doorway")) {
            return "mentor introduction";
        }

        return title;
    }

    @Test
    @DisplayName("a run does not always open with the same scene")
    void theOpeningIsNotAFixedCutscene() {
        int runs = 200;
        Map<String, Integer> openings = new HashMap<>();

        for (int i = 0; i < runs; i++) {
            openings.merge(
                    scene(new GameEngine().getCurrentEvent().getTitle()),
                    1,
                    Integer::sum
            );
        }

        assertTrue(
                openings.size() >= 4,
                "only " + openings.size() + " distinct openings in "
                        + runs + " runs: " + openings.keySet()
        );

        int commonest =
                openings.values().stream()
                        .mapToInt(Integer::intValue)
                        .max()
                        .orElse(0);

        assertTrue(
                commonest < runs * 0.6,
                "one scene opened " + commonest + "/" + runs
                        + " runs: " + openings
        );
    }

    @Test
    @DisplayName("the general pool is reachable in the opening slot")
    void theCastDoesNotOwnTheFirstSlot() {
        int runs = 200;
        int general = 0;

        for (int i = 0; i < runs; i++) {
            String opening =
                    scene(new GameEngine().getCurrentEvent().getTitle());

            if (!opening.endsWith("introduction")) {
                general++;
            }
        }

        assertTrue(
                general > 0,
                "the cast still pre-empts every opening"
        );
    }

    @Test
    @DisplayName("weighting the draw does not cost the cast their arcs")
    void theCastStillReliablyTurnsUp() {
        Random random = new Random(11);
        int lives = 150;
        int metEveryone = 0;

        for (int run = 0; run < lives; run++) {
            Set<String> flags = playALife(random).getWorldFlags();

            if (flags.contains("npc_friend_met")
                    && flags.contains("npc_rival_met")
                    && flags.contains("npc_mentor_met")) {

                metEveryone++;
            }
        }

        // Measured around 85% at the current weight; 57% at half of it.
        assertTrue(
                metEveryone > lives * 0.7,
                "only " + metEveryone + "/" + lives
                        + " lives met the whole cast"
        );
    }

    @Test
    @DisplayName("every cast member is introduced in Childhood")
    void nobodyArrivesUnannounced() {
        RecurringCharacterRegistry registry =
                new RecurringCharacterRegistry(new Random(3));

        registry.generateInitialCast(
                new PlayerCharacter(
                        "Test Character",
                        "Abbasid Era",
                        "Madrasa Student",
                        "Stable Household",
                        "Observant",
                        8, 60, 40, 55, 45, 30, 55, 60, 20
                )
        );

        List<GameEvent> childhood =
                RecurringCharacterEvents.create(registry).stream()
                        .filter(
                                event -> event.getLifeStage()
                                        .equals("Childhood")
                        )
                        .toList();

        Set<String> scenes = new HashSet<>();

        for (GameEvent event : childhood) {
            scenes.add(scene(event.getTitle()));
        }

        assertEquals(
                Set.of(
                        "friend introduction",
                        "rival introduction",
                        "mentor introduction"
                ),
                scenes,
                "each of the three should open their own arc in Childhood"
        );
    }

    @Test
    @DisplayName("cast events are well formed and uniquely titled")
    void castEventsAreWellFormed() {
        RecurringCharacterRegistry registry =
                new RecurringCharacterRegistry(new Random(5));

        registry.generateInitialCast(
                new PlayerCharacter(
                        "Test Character",
                        "Abbasid Era",
                        "Madrasa Student",
                        "Stable Household",
                        "Observant",
                        8, 60, 40, 55, 45, 30, 55, 60, 20
                )
        );

        Set<String> titles = new HashSet<>();

        for (GameEvent event : RecurringCharacterEvents.create(registry)) {
            assertFalse(
                    event.getTitle().isBlank(),
                    "cast event with no title"
            );

            assertFalse(
                    event.getDescription().isBlank(),
                    event.getTitle() + " has no description"
            );

            assertFalse(
                    event.getChoices().isEmpty(),
                    event.getTitle() + " offers no choices"
            );

            for (Choice choice : event.getChoices()) {
                assertFalse(
                        choice.getText().isBlank(),
                        event.getTitle() + " has an unlabelled choice"
                );
            }

            // The engine keys played events by title, so a duplicate would
            // silently suppress one of the two.
            assertTrue(
                    titles.add(event.getTitle()),
                    "duplicate cast event title: " + event.getTitle()
            );
        }
    }
}
