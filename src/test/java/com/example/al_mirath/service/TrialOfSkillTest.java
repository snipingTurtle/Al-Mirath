package com.example.al_mirath.service;

import com.example.al_mirath.minigame.MiniGame;
import com.example.al_mirath.minigame.MiniGameFactory;
import com.example.al_mirath.model.Choice;
import com.example.al_mirath.model.GameEvent;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Covers the Trial of Skill path, where a mini-game result replaces the
 * engine's internal stat roll.
 */
class TrialOfSkillTest {

    /** Finds a live stat-check choice by playing forward until one appears. */
    private Choice findStatCheckChoice(GameEngine engine) {
        for (int i = 0; i < 40; i++) {
            GameEvent event = engine.getCurrentEvent();
            if (event == null) {
                break;
            }

            for (Choice choice : event.getChoices()) {
                if (choice.requiresStatCheck() && engine.canChoose(choice)) {
                    return choice;
                }
            }

            for (Choice choice : event.getChoices()) {
                if (engine.canChoose(choice)) {
                    engine.applyChoice(choice);
                    break;
                }
            }
        }

        return null;
    }

    @Test
    @DisplayName("a forced success applies the success branch, not a random one")
    void forcedSuccessIsHonoured() {
        for (int attempt = 0; attempt < 25; attempt++) {
            GameEngine engine = new GameEngine();
            Choice choice = findStatCheckChoice(engine);

            if (choice == null) {
                continue;
            }

            String result = engine.applyChoice(choice, true);

            assertTrue(result.startsWith("Outcome: Success"),
                    "forcing success must select the success text, got: " + result);
            return;
        }
    }

    @Test
    @DisplayName("a forced failure applies the failure branch")
    void forcedFailureIsHonoured() {
        for (int attempt = 0; attempt < 25; attempt++) {
            GameEngine engine = new GameEngine();
            Choice choice = findStatCheckChoice(engine);

            if (choice == null) {
                continue;
            }

            String result = engine.applyChoice(choice, false);

            assertTrue(result.startsWith("Outcome: Failure"),
                    "forcing failure must select the failure text, got: " + result);
            return;
        }
    }

    @Test
    @DisplayName("the trial factory returns a game for every stat a check can name")
    void factoryCoversEveryCheckStat() {
        List<String> stats = List.of(
                "health", "wealth", "education", "reputation",
                "politicalPower", "morality", "familyLoyalty", "stress"
        );

        for (String stat : stats) {
            for (int difficulty : new int[]{25, 45, 65}) {
                MiniGame game = MiniGameFactory.createForStatCheck(stat, difficulty);

                assertNotNull(game, "no trial produced for stat " + stat);
                assertNotNull(game.getTitle());
                assertTrue(!game.getTitle().isBlank());
                assertTrue(!game.getInstructions().isBlank(),
                        "trial for " + stat + " has no instructions");
            }
        }
    }

    @Test
    @DisplayName("passing no forced outcome still resolves normally")
    void nullForcedOutcomeFallsBackToTheRoll() {
        GameEngine engine = new GameEngine();
        GameEvent event = engine.getCurrentEvent();
        assertNotNull(event);

        Choice choice = event.getChoices().stream()
                .filter(engine::canChoose)
                .findFirst()
                .orElseThrow();

        String viaOverload = engine.applyChoice(choice, null);

        assertNotNull(viaOverload);
        assertTrue(!viaOverload.isBlank());
    }

    @Test
    @DisplayName("a trial result does not disturb the rewind snapshot")
    void trialStillLeavesAnUndoPoint() {
        for (int attempt = 0; attempt < 25; attempt++) {
            GameEngine engine = new GameEngine();
            Choice choice = findStatCheckChoice(engine);

            if (choice == null) {
                continue;
            }

            int ageBefore = engine.getPlayer().getAge();
            engine.applyChoice(choice, false);

            assertTrue(engine.canRewind(), "a trial outcome must still be undoable");

            engine.spendFateToken();
            assertTrue(engine.rewindToLastSnapshot());
            assertEquals(ageBefore, engine.getPlayer().getAge());
            return;
        }
    }
}
