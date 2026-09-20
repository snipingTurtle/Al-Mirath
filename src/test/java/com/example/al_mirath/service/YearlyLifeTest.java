package com.example.al_mirath.service;

import com.example.al_mirath.model.Activity;
import com.example.al_mirath.model.LifeLogEntry;
import com.example.al_mirath.model.PlayerCharacter;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * The year is the clock.
 *
 * <p>Everything about how this game reads depends on one rule: time moves when
 * the player ages up and at no other moment. A decision is something that
 * happens inside a year, not something that costs six of them.
 */
class YearlyLifeTest {

    @Test
    @DisplayName("ageing up moves the year on by exactly one")
    void oneYearIsOneYear() {
        GameEngine engine = new GameEngine();

        for (int i = 0; i < 12; i++) {
            int before = engine.getPlayer().getAge();
            engine.ageOneYear();

            if (!engine.getPlayer().isAlive()) {
                return;
            }

            assertEquals(before + 1, engine.getPlayer().getAge(),
                    "a year took the character somewhere other than one year on");
        }
    }

    @Test
    @DisplayName("answering a scene costs no time at all")
    void aDecisionIsNotADecade() {
        Random random = new Random(4);

        for (int attempt = 0; attempt < 60; attempt++) {
            GameEngine engine = new GameEngine();

            for (int year = 0; year < 30 && engine.getPlayer().isAlive(); year++) {
                engine.ageOneYear();

                if (!engine.getPlayer().isAlive()) {
                    break;
                }

                var scene = engine.getCurrentEvent();

                if (scene == null) {
                    continue;
                }

                var choice = Lives.anyOpen(engine, scene, random);

                if (choice == null) {
                    continue;
                }

                int before = engine.getPlayer().getAge();
                engine.applyChoice(choice);

                assertEquals(before, engine.getPlayer().getAge(),
                        "the scene '" + scene.getTitle() + "' aged the character; "
                                + "time is supposed to be the age-up and nothing else");

                return;
            }
        }
    }

    @Test
    @DisplayName("a year that happened leaves a line saying so")
    void theYearIsWrittenDown() {
        GameEngine engine = new GameEngine();

        assertFalse(engine.getLifeLog().isEmpty(),
                "a life begins with nothing written down, not even a birth");

        int quiet = 0;

        for (int year = 0; year < 30 && engine.getPlayer().isAlive(); year++) {
            List<LifeLogEntry> added = engine.ageOneYear();

            if (added.isEmpty()) {
                quiet++;
            }

            for (LifeLogEntry entry : added) {
                assertFalse(entry.text().isBlank(), "a blank line was written to the log");
                assertNotNull(entry.tone());
                assertEquals(engine.getPlayer().getAge(), entry.age(),
                        "a line was stamped with the wrong year");
            }
        }

        assertTrue(quiet < 24,
                quiet + " of thirty years passed without a single thing being "
                        + "written down; ageing up would read as nothing happening");
    }

    @Test
    @DisplayName("a life ends when the character dies, not when the scenes run out")
    void theContentRunningOutIsNotDeath() {
        Random random = new Random(21);
        boolean everOutlivedTheContent = false;

        for (int attempt = 0; attempt < 120 && !everOutlivedTheContent; attempt++) {
            GameEngine engine = new GameEngine();

            for (int year = 0; year < 60 && engine.getPlayer().isAlive(); year++) {
                engine.ageOneYear();

                if (!engine.getPlayer().isAlive()) {
                    break;
                }

                var scene = engine.getCurrentEvent();

                if (scene == null) {
                    continue;
                }

                var choice = Lives.anyOpen(engine, scene, random);

                if (choice != null) {
                    engine.applyChoice(choice);
                }
            }

            if (engine.getPlayer().isAlive() && engine.getCurrentEvent() == null) {
                everOutlivedTheContent = true;

                // The life keeps going, which is the whole point.
                int before = engine.getPlayer().getAge();
                engine.ageOneYear();

                assertTrue(engine.getPlayer().getAge() > before,
                        "a character with no scenes left stopped ageing");
            }
        }

        assertTrue(everOutlivedTheContent,
                "in a hundred and twenty tries nobody outlived the authored scenes, "
                        + "so this proved nothing");
    }

    @Test
    @DisplayName("a chapter of life opens because of the character's age")
    void chaptersFollowTheYears() {
        GameEngine engine = new GameEngine();

        while (engine.getPlayer().isAlive() && engine.getPlayer().getAge() < 30) {
            engine.ageOneYear();

            int expected = GameEngine.stageIndexForAge(engine.getPlayer().getAge());

            assertEquals(expected, engine.getCurrentStageIndex(),
                    "at age " + engine.getPlayer().getAge() + " the life was in "
                            + engine.getCurrentLifeStage());
        }
    }

    @Test
    @DisplayName("the chronicle and the purse survive a save")
    void theLifeRoundTrips() {
        GameEngine engine = new GameEngine();

        for (int year = 0; year < 25 && engine.getPlayer().isAlive(); year++) {
            engine.ageOneYear();

            Activity pray = ActivityLibrary.byId("pray");

            if (pray != null && engine.lockedReasonFor(pray).isEmpty()) {
                engine.performActivity(pray, null);
            }
        }

        GameEngine loaded = GameEngine.fromSaveJson(engine.toSaveJson());

        assertEquals(engine.getLifeLog().size(), loaded.getLifeLog().size(),
                "the chronicle was shorter after loading");

        for (int i = 0; i < engine.getLifeLog().size(); i++) {
            assertEquals(engine.getLifeLog().get(i), loaded.getLifeLog().get(i),
                    "line " + i + " of the chronicle changed across a save");
        }

        assertEquals((long) engine.getPlayer().getNetWorth(),
                (long) loaded.getPlayer().getNetWorth(),
                "the purse was emptied by the save");

        assertEquals(engine.getPlayer().getAge(), loaded.getPlayer().getAge());
    }

    @Test
    @DisplayName("a scene raised and not answered is still waiting after a save")
    void anUnansweredDecisionSurvives() {
        Random random = new Random(8);

        for (int attempt = 0; attempt < 80; attempt++) {
            GameEngine engine = new GameEngine();

            for (int year = 0; year < 40 && engine.getPlayer().isAlive(); year++) {
                engine.ageOneYear();

                if (engine.isSceneDue()) {
                    String waiting = engine.getCurrentEvent().getTitle();

                    GameEngine loaded = GameEngine.fromSaveJson(engine.toSaveJson());

                    assertTrue(loaded.isSceneDue(),
                            "a decision the player had been handed was lost in the save");

                    assertEquals(waiting, loaded.getCurrentEvent().getTitle(),
                            "a different scene came back from the save");

                    return;
                }

                var scene = engine.getCurrentEvent();

                if (scene != null) {
                    var choice = Lives.anyOpen(engine, scene, random);

                    if (choice != null) {
                        engine.applyChoice(choice);
                    }
                }
            }
        }
    }

    @Test
    @DisplayName("nobody is charged rent before they keep their own roof up")
    void childrenAreNotBankrupted() {
        for (int attempt = 0; attempt < 40; attempt++) {
            GameEngine engine = new GameEngine();
            PlayerCharacter player = engine.getPlayer();

            double opening = player.getNetWorth();

            while (player.isAlive() && player.getAge() < 17) {
                engine.ageOneYear();
            }

            if (!player.isAlive()) {
                continue;
            }

            assertTrue(player.getNetWorth() >= opening,
                    "a child's purse fell from " + (long) opening + " to "
                            + (long) player.getNetWorth()
                            + " before they were old enough to earn");
        }
    }
}
