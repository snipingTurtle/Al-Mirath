package com.example.al_mirath.service;

import com.example.al_mirath.model.Choice;
import com.example.al_mirath.model.GameEvent;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Structural checks on the story content.
 *
 * <p>An arc is a chain: an opening event sets a {@code *_arc_started} flag and
 * later events require it. If an opening event is ever gated behind its own
 * flag, or a continuation requires a flag nothing sets, the arc becomes
 * unreachable — and that failure is silent in play, since the engine simply
 * never selects the event.
 */
class ArcIntegrityTest {

    private static final List<String> ARC_FLAGS = List.of(
            "scholars_rise_started", "heresy_arc_started", "caravan_arc_started",
            "guild_war_started", "barracks_arc_started", "frontier_arc_started",
            "janissary_arc_started", "spy_arc_started", "shadow_debt_started",
            "dynasty_arc_started", "bread_arc_started", "tax_arc_started",
            "famine_arc_started", "orphan_patron_started", "marriage_arc_started",
            "feud_arc_started", "heir_arc_started"
    );

    @Test
    @DisplayName("every arc has an opening event that is not gated behind its own flag")
    void everyArcHasAReachableOpening() {
        List<GameEvent> pool = EventLibrary.createEventPool();

        for (String arcFlag : ARC_FLAGS) {
            boolean hasOpening = pool.stream().anyMatch(event ->
                    !event.getRequiredFlags().contains(arcFlag)
                            && event.getChoices().stream().anyMatch(choice ->
                            choice.getSuccessFlags().contains(arcFlag)
                                    || choice.getFailureFlags().contains(arcFlag))
            );

            assertTrue(hasOpening, "arc has no reachable opening event: " + arcFlag);
        }
    }

    @Test
    @DisplayName("every arc has at least one continuation that requires its flag")
    void everyArcHasAContinuation() {
        List<GameEvent> pool = EventLibrary.createEventPool();

        for (String arcFlag : ARC_FLAGS) {
            boolean hasContinuation = pool.stream()
                    .anyMatch(event -> event.getRequiredFlags().contains(arcFlag));

            assertTrue(hasContinuation, "arc opens but never continues: " + arcFlag);
        }
    }

    @Test
    @DisplayName("no event requires a flag that nothing in the game can ever set")
    void noEventRequiresAnUnsettableFlag() {
        List<GameEvent> pool = EventLibrary.createEventPool();

        Set<String> settable = new HashSet<>();

        for (GameEvent event : pool) {
            for (Choice choice : event.getChoices()) {
                settable.addAll(choice.getSuccessFlags());
                settable.addAll(choice.getFailureFlags());
            }
        }

        // Delayed echoes can also set flags that later content may read.
        for (String flag : new ArrayList<>(settable)) {
            ConsequenceLibrary.consequencesFor(flag, 20)
                    .forEach(consequence -> settable.addAll(consequence.flagsToAdd()));
        }

        List<String> orphaned = new ArrayList<>();

        for (GameEvent event : pool) {
            for (String required : event.getRequiredFlags()) {
                if (!settable.contains(required)) {
                    orphaned.add(event.getTitle() + " requires unsettable flag: " + required);
                }
            }
        }

        assertTrue(orphaned.isEmpty(), String.join("\n", orphaned));
    }

    @Test
    @DisplayName("every event offers at least one choice and non-empty text")
    void everyEventIsWellFormed() {
        List<GameEvent> pool = EventLibrary.createEventPool();

        assertFalse(pool.isEmpty());

        for (GameEvent event : pool) {
            assertFalse(event.getTitle().isBlank(), "event with blank title");
            assertFalse(event.getDescription().isBlank(),
                    "blank description: " + event.getTitle());
            assertFalse(event.getChoices().isEmpty(),
                    "event with no choices: " + event.getTitle());

            for (Choice choice : event.getChoices()) {
                assertFalse(choice.getText().isBlank(),
                        "blank choice text in: " + event.getTitle());
            }
        }
    }

    @Test
    @DisplayName("event titles are unique, since the engine keys played events by title")
    void eventTitlesAreUnique() {
        List<GameEvent> pool = EventLibrary.createEventPool();

        Set<String> seen = new HashSet<>();
        List<String> duplicates = new ArrayList<>();

        for (GameEvent event : pool) {
            if (!seen.add(event.getTitle())) {
                duplicates.add(event.getTitle());
            }
        }

        assertTrue(duplicates.isEmpty(), "duplicate event titles: " + duplicates);
    }

    @Test
    @DisplayName("a played life reaches at least one arc the large majority of the time")
    void arcsAreReachableInPlay() {
        Random random = new Random(7);
        int livesTouchingAnArc = 0;
        int lives = 120;

        for (int run = 0; run < lives; run++) {
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

                engine.applyChoice(available.get(random.nextInt(available.size())));
            }

            if (ARC_FLAGS.stream().anyMatch(engine.getWorldFlags()::contains)) {
                livesTouchingAnArc++;
            }
        }

        // Not every life must hit an arc, but the content should not be
        // sitting unreachable behind gating nobody meets.
        assertTrue(
                livesTouchingAnArc > lives * 0.7,
                "only " + livesTouchingAnArc + "/" + lives + " lives reached any arc"
        );
    }
}
