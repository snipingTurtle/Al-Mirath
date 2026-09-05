package com.example.al_mirath.service;

import com.example.al_mirath.model.Choice;
import com.example.al_mirath.model.DeathCause;
import com.example.al_mirath.model.EndingResult;
import com.example.al_mirath.model.GameEvent;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * What the player is told when they die.
 *
 * <p>Two things used to go wrong here: only the catch-all ending mentioned the
 * cause of death at all, and the cause itself was a fixed sentence written for
 * an adult, so a seven-year-old was informed that "years of pressure" had worn
 * them down.
 */
class DeathPanelTest {

    /** Phrases that assume a long life already lived. */
    private static final List<String> ADULT_REGISTER = List.of(
            "final years",
            "final days",
            "years of pressure",
            "years of hardship",
            "old age",
            "final chapter"
    );

    private Choice firstAvailableChoice(GameEngine engine, GameEvent event) {
        for (Choice choice : event.getChoices()) {
            if (engine.canChoose(choice)) {
                return choice;
            }
        }

        throw new IllegalStateException("no selectable choice");
    }

    /** Plays until the player dies, or returns null if they survived. */
    private GameEngine playUntilDeath(GameEngine engine) {
        for (int i = 0; i < 40 && engine.getCurrentEvent() != null; i++) {
            engine.applyChoice(
                    firstAvailableChoice(engine, engine.getCurrentEvent())
            );

            if (!engine.getPlayer().isAlive()) {
                // Forces the ending to be calculated.
                engine.getCurrentEvent();
                return engine;
            }
        }

        return null;
    }

    private List<GameEngine> deaths(int wanted, int maxRuns) {
        List<GameEngine> found = new ArrayList<>();

        for (int run = 0; run < maxRuns && found.size() < wanted; run++) {
            GameEngine dead = playUntilDeath(new GameEngine());

            if (dead != null && dead.getEndingResult() != null) {
                found.add(dead);
            }
        }

        return found;
    }

    // ---- the cause reaches the panel -------------------------------------

    @Test
    @DisplayName("every death panel states the cause of death")
    void everyDeathPanelSaysWhy() {
        List<GameEngine> dead = deaths(40, 2000);

        assertFalse(dead.isEmpty(), "no deaths sampled");

        for (GameEngine engine : dead) {
            String reason = engine.getPlayer().getDeathReason();
            EndingResult ending = engine.getEndingResult();

            assertFalse(
                    reason.isBlank(),
                    "died with no reason recorded: " + ending.getTitle()
            );

            assertTrue(
                    ending.getDescription().contains(reason),
                    "the \"" + ending.getTitle() + "\" panel never says why: "
                            + reason
            );
        }
    }

    @Test
    @DisplayName("a child is not told about their final years")
    void theYoungGetTheirOwnFraming() {
        // Children are about one death in thirty, so sampling deaths in
        // general and hoping for a few of them is how this test ends up
        // proving nothing on a bad run. Collect the case under test directly.
        List<GameEngine> childDeaths = new ArrayList<>();

        for (int run = 0; run < 4000 && childDeaths.size() < 10; run++) {
            GameEngine engine = playUntilDeath(new GameEngine());

            if (engine != null
                    && engine.getEndingResult() != null
                    && engine.getPlayer().getAge() < 13) {

                childDeaths.add(engine);
            }
        }

        assertFalse(
                childDeaths.isEmpty(),
                "no child deaths sampled, so this proved nothing"
        );

        for (GameEngine engine : childDeaths) {

            assertEquals(
                    "A Life Barely Begun",
                    engine.getEndingResult().getTitle(),
                    "a child died at " + engine.getPlayer().getAge()
                            + " and got an ending written for a long life"
            );

            String panel =
                    engine.getEndingResult().getDescription()
                            .toLowerCase(Locale.ROOT);

            for (String phrase : ADULT_REGISTER) {
                assertFalse(
                        panel.contains(phrase),
                        "a child's death panel says \"" + phrase + "\": "
                                + engine.getEndingResult().getTitle()
                );
            }
        }

    }

    @Test
    @DisplayName("a long life is not reported as one cut short")
    void theOldAreNotCutShort() {
        List<GameEngine> elderDeaths = new ArrayList<>();

        for (int run = 0; run < 4000 && elderDeaths.size() < 10; run++) {
            GameEngine engine = playUntilDeath(new GameEngine());

            if (engine != null
                    && engine.getEndingResult() != null
                    && engine.getPlayer().getAge() >= 60) {

                elderDeaths.add(engine);
            }
        }

        assertFalse(
                elderDeaths.isEmpty(),
                "no elder deaths sampled, so this proved nothing"
        );

        for (GameEngine engine : elderDeaths) {
            assertNotEquals(
                    "Life Cut Short",
                    engine.getEndingResult().getTitle(),
                    "died at " + engine.getPlayer().getAge()
                            + ", which is not a life cut short"
            );
        }
    }

    // ---- the cause knows how old the dead were ---------------------------

    @Test
    @DisplayName("the same cause reads differently for a child and an adult")
    void causesAreToldForTheAge() {
        for (DeathCause cause : List.of(
                DeathCause.EXHAUSTION,
                DeathCause.FRAILTY,
                DeathCause.ENMITY)) {

            // Child, youth, adult and elder are four separate tellings; it is
            // not enough for a child to merely avoid the adult one.
            Set<String> tellings = new HashSet<>();

            for (int age : new int[]{8, 20, 40, 70}) {
                assertTrue(
                        tellings.add(cause.describe(age)),
                        cause + " reuses one telling across age bands, "
                                + "including at " + age
                );
            }

            String childText = cause.describe(8).toLowerCase(Locale.ROOT);

            for (String phrase : ADULT_REGISTER) {
                assertFalse(
                        childText.contains(phrase),
                        cause + " tells a child about \"" + phrase + "\""
                );
            }
        }
    }

    @Test
    @DisplayName("every cause has something to say at every age")
    void noCauseIsSpeechless() {
        for (DeathCause cause : DeathCause.values()) {
            for (int age : new int[]{6, 12, 13, 24, 25, 59, 60, 95}) {
                String text = cause.describe(age);

                assertFalse(
                        text == null || text.isBlank(),
                        cause + " says nothing at age " + age
                );

                assertTrue(
                        text.endsWith("."),
                        cause + " at age " + age + " is not a sentence: " + text
                );
            }
        }
    }

    // ---- cruelty is a cause of death -------------------------------------

    @Test
    @DisplayName("a reputation for cruelty can kill, and the panel names it")
    void crueltyIsFatalAndStated() {
        int runs = 400;
        int enmityDeaths = 0;

        for (int run = 0; run < runs; run++) {
            GameEngine engine = new GameEngine();

            // Someone the world has reason to be rid of.
            engine.getPlayer().setStatValue("morality", 5);

            GameEngine dead = playUntilDeath(engine);

            if (dead == null || dead.getEndingResult() == null) {
                continue;
            }

            String reason = dead.getPlayer().getDeathReason();
            int age = dead.getPlayer().getAge();

            if (reason.equals(DeathCause.ENMITY.describe(age))) {
                enmityDeaths++;

                assertTrue(
                        dead.getEndingResult().getDescription().contains(reason),
                        "the panel does not say cruelty killed them"
                );
            }
        }

        assertTrue(
                enmityDeaths > 0,
                "low morality never killed anyone in " + runs + " runs"
        );
    }
}
