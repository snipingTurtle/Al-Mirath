package com.example.al_mirath.service;

import com.example.al_mirath.model.DelayedConsequence;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Covers the delayed-consequence engine: the system that makes a choice made
 * in childhood surface again decades later.
 */
class DelayedConsequenceTest {

    @Test
    @DisplayName("a corrupt choice plants an echo dated years into the future")
    void bribingPlantsAFutureEcho() {
        List<DelayedConsequence> planted = ConsequenceLibrary.consequencesFor("used_bribery", 10);

        assertFalse(planted.isEmpty(), "bribery should plant a long-term echo");

        DelayedConsequence echo = planted.get(0);

        assertTrue(echo.triggerAge() > 10, "the echo must come due later than the choice");
        assertFalse(echo.description().isBlank());
        assertTrue(echo.hasCancelFlag(), "confessing should be able to defuse this one");
    }

    @Test
    @DisplayName("virtuous choices plant echoes too, not only punishments")
    void virtueAlsoEchoes() {
        List<DelayedConsequence> planted = ConsequenceLibrary.consequencesFor("protected_commoners", 12);

        assertFalse(planted.isEmpty());

        DelayedConsequence echo = planted.get(0);

        // Standing up for people early should pay off, not merely cost.
        assertTrue(
                echo.statEffects().getOrDefault("reputation", 0) > 0,
                "protecting commoners should raise reputation when it returns"
        );
    }

    @Test
    @DisplayName("a flag with no scripted echo plants nothing")
    void unknownFlagsPlantNothing() {
        assertTrue(ConsequenceLibrary.consequencesFor("some_unscripted_flag", 20).isEmpty());
    }

    @Test
    @DisplayName("an echo can be rebuilt from its id when a save is loaded")
    void echoesRebuildFromId() {
        DelayedConsequence original = ConsequenceLibrary.consequencesFor("used_bribery", 10).get(0);

        DelayedConsequence restored = ConsequenceLibrary.byId(original.id(), 47);

        assertNotNull(restored, "a known id must rebuild");
        assertEquals(original.id(), restored.id());
        assertEquals(original.title(), restored.title());
        assertEquals(47, restored.triggerAge(), "the saved trigger age must win over the template's");
    }

    @Test
    @DisplayName("an id no longer in the library drops out of an old save cleanly")
    void removedEchoesDropOut() {
        assertNull(ConsequenceLibrary.byId("echo_that_no_longer_exists", 40));
    }

    @Test
    @DisplayName("scheduled echoes survive a save and reload")
    void pendingEchoesPersist() {
        GameEngine engine = new GameEngine();

        // Play far enough that at least one echo-planting flag is likely set.
        for (int i = 0; i < 25 && engine.getCurrentEvent() != null; i++) {
            var event = engine.getCurrentEvent();

            for (var choice : event.getChoices()) {
                if (engine.canChoose(choice)) {
                    engine.applyChoice(choice);
                    break;
                }
            }
        }

        int pendingBefore = engine.getPendingConsequenceCount();

        GameEngine restored = GameEngine.fromSaveJson(engine.toSaveJson());

        assertEquals(
                pendingBefore,
                restored.getPendingConsequenceCount(),
                "pending echoes must survive a save/load round trip"
        );
    }
}
