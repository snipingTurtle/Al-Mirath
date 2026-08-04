package com.example.al_mirath.service;

import com.example.al_mirath.model.Choice;
import com.example.al_mirath.model.GameEvent;
import org.json.JSONObject;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Round-trips the save format. A save that silently drops a field would let a
 * player continue a run with the wrong Threads or a lost story flag.
 */
class GameEnginePersistenceTest {

    private Choice firstAvailableChoice(GameEngine engine, GameEvent event) {
        for (Choice choice : event.getChoices()) {
            if (engine.canChoose(choice)) {
                return choice;
            }
        }

        throw new IllegalStateException("no selectable choice on " + event.getTitle());
    }

    @Test
    @DisplayName("a saved run reloads with identical character and standing")
    void savedRunReloadsIdentically() {
        GameEngine original = new GameEngine();

        GameEvent event = original.getCurrentEvent();
        assertNotNull(event);
        original.applyChoice(firstAvailableChoice(original, event));

        GameEngine restored = GameEngine.fromSaveJson(original.toSaveJson());

        assertEquals(original.getPlayer().getName(), restored.getPlayer().getName());
        assertEquals(original.getPlayer().getEra(), restored.getPlayer().getEra());
        assertEquals(original.getPlayer().getAge(), restored.getPlayer().getAge());
        assertEquals(original.getPlayer().getCurrentStatus(), restored.getPlayer().getCurrentStatus());

        assertEquals(original.getPlayer().getHealth(), restored.getPlayer().getHealth());
        assertEquals(original.getPlayer().getWealth(), restored.getPlayer().getWealth());
        assertEquals(original.getPlayer().getMorality(), restored.getPlayer().getMorality());

        assertEquals(original.getFactions().getCourt(), restored.getFactions().getCourt());
        assertEquals(original.getFactions().getShadowNetwork(), restored.getFactions().getShadowNetwork());

        assertEquals(original.getCurrentStageIndex(), restored.getCurrentStageIndex());
    }

    @Test
    @DisplayName("Threads of Fate and run counters survive a save")
    void fateEconomySurvivesSave() {
        GameEngine original = new GameEngine();

        original.applyChoice(firstAvailableChoice(original, original.getCurrentEvent()));
        original.spendFateToken();
        original.recordMinigameOutcome(true);
        original.rewindToLastSnapshot();

        GameEngine restored = GameEngine.fromSaveJson(original.toSaveJson());

        assertEquals(original.getFateTokens(), restored.getFateTokens(), "Threads must persist");
        assertEquals(original.getChoicesMade(), restored.getChoicesMade());
        assertEquals(original.getRewindsUsed(), restored.getRewindsUsed());
        assertEquals(original.getMinigamesWon(), restored.getMinigamesWon());
    }

    @Test
    @DisplayName("a save written before Threads existed still loads")
    void legacySaveWithoutNewFieldsLoads() {
        GameEngine original = new GameEngine();
        original.applyChoice(firstAvailableChoice(original, original.getCurrentEvent()));

        // Strip the fields added by this feature, mimicking an older save file.
        JSONObject json = new JSONObject(original.toSaveJson());
        json.remove("fateTokens");
        json.remove("stagesRewarded");
        json.remove("choicesMade");
        json.remove("rewindsUsed");
        json.remove("minigamesWon");
        json.remove("minigamesLost");

        GameEngine restored = GameEngine.fromSaveJson(json.toString());

        assertNotNull(restored.getPlayer());
        assertEquals(1, restored.getFateTokens(), "an old save should fall back to the starting Thread");
        assertEquals(0, restored.getRewindsUsed());
    }

    @Test
    @DisplayName("a rewound run does not carry a stale undo point into its save")
    void rewoundRunSavesCleanly() {
        GameEngine engine = new GameEngine();

        engine.applyChoice(firstAvailableChoice(engine, engine.getCurrentEvent()));
        engine.spendFateToken();
        engine.rewindToLastSnapshot();

        assertFalse(engine.canRewind(), "the consumed snapshot should be gone after a rewind");

        String json = engine.toSaveJson();
        assertTrue(json.contains("fateTokens"));

        GameEngine restored = GameEngine.fromSaveJson(json);
        assertNotNull(restored.getCurrentEvent(), "a reloaded run must still have somewhere to go");
    }
}
