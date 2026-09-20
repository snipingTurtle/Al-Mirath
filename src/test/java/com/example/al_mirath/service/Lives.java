package com.example.al_mirath.service;

import com.example.al_mirath.model.Activity;
import com.example.al_mirath.model.Choice;
import com.example.al_mirath.model.GameEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Plays whole lives the way the game does, for tests that need a run rather
 * than a unit.
 *
 * <p>Time is the age-up, not the choice. A test that drives a life by calling
 * {@link GameEngine#applyChoice} in a loop is not playing the game — it is
 * making thirty decisions in a single afternoon, and every system that keys
 * off age (chapters, ageing children, heirs, the city's decades) sits still
 * while it does. Everything here goes through {@link GameEngine#ageOneYear()},
 * so the run a test samples is the run a player would get.
 */
public final class Lives {

    /** Nobody lives past this, so no test can hang on a life that will not end. */
    public static final int LIFE_CEILING = 110;

    private Lives() {
    }

    /**
     * What happens when a scene is put in front of the character.
     *
     * <p>Deliberately given the engine rather than asked for a choice: tests
     * that care about the result text, or about what the scene said before it
     * was answered, need to be the ones calling
     * {@link GameEngine#applyChoice}.
     */
    @FunctionalInterface
    public interface Scene {
        void play(GameEngine engine, GameEvent event);
    }

    /**
     * The ordinary things a person does to stay well, none of which needs any
     * skill or standing to attempt.
     */
    public static final List<String> LOOKING_AFTER_YOURSELF = List.of(
            "pray", "garden", "bathhouse", "family_evening", "alms", "wrestle");

    /**
     * Plays a life in which the character actually looks after themselves.
     *
     * <p>The difference between this and {@link #live(GameEngine, Scene)} is
     * the entire claim the activity menu makes: that choosing to pray, to walk
     * in the garden and to sit with your household is worth something the
     * numbers can see.
     */
    public static GameEngine liveCarefully(GameEngine engine, Scene scene) {
        for (int year = 0; year < LIFE_CEILING; year++) {
            if (!engine.getPlayer().isAlive()) {
                break;
            }

            engine.ageOneYear();

            if (!engine.getPlayer().isAlive()) {
                break;
            }

            for (String id : LOOKING_AFTER_YOURSELF) {
                Activity activity = ActivityLibrary.byId(id);

                if (activity != null && engine.lockedReasonFor(activity).isEmpty()) {
                    engine.performActivity(activity, null);
                }
            }

            GameEvent event = engine.getCurrentEvent();

            if (event != null) {
                scene.play(engine, event);
            }
        }

        engine.getCurrentEvent();

        return engine;
    }

    /** Plays until the character dies or reaches the ceiling. */
    public static GameEngine live(GameEngine engine, Scene scene) {
        return live(engine, LIFE_CEILING, scene);
    }

    /**
     * Lives {@code maxYears} years, or fewer if the character does not last.
     *
     * <p>Ends by asking for the current event, so a caller that goes straight
     * on to read the ending gets one.
     */
    public static GameEngine live(GameEngine engine, int maxYears, Scene scene) {
        for (int year = 0; year < maxYears; year++) {
            if (!engine.getPlayer().isAlive()) {
                break;
            }

            engine.ageOneYear();

            if (!engine.getPlayer().isAlive()) {
                break;
            }

            GameEvent event = engine.getCurrentEvent();

            if (event != null) {
                scene.play(engine, event);
            }
        }

        engine.getCurrentEvent();

        return engine;
    }

    /** Answers every scene with a choice picked at random from the open ones. */
    public static Scene takingAnyOpenChoice(Random random) {
        return (engine, event) -> {
            Choice choice = anyOpen(engine, event, random);

            if (choice != null) {
                engine.applyChoice(choice);
            }
        };
    }

    /** Answers every scene with the first choice that is not locked. */
    public static Scene takingFirstOpenChoice() {
        return (engine, event) -> {
            Choice choice = firstOpen(engine, event);

            if (choice != null) {
                engine.applyChoice(choice);
            }
        };
    }

    /**
     * A living character of about the given age, with the years behind them
     * actually lived rather than assigned.
     *
     * <p>Retried, because a life can end at any point and a test about what a
     * thirty-year-old can do is not helped by being handed a dead child.
     */
    public static GameEngine aliveAt(int age, Scene scene) {
        for (int attempt = 0; attempt < 300; attempt++) {
            GameEngine engine = new GameEngine();

            while (engine.getPlayer().isAlive() && engine.getPlayer().getAge() < age) {
                engine.ageOneYear();

                GameEvent event = engine.getCurrentEvent();

                if (event != null && scene != null) {
                    scene.play(engine, event);
                }
            }

            if (engine.getPlayer().isAlive()) {
                return engine;
            }
        }

        throw new IllegalStateException("nobody reached " + age + " alive in 300 tries");
    }

    /** As {@link #aliveAt(int, Scene)}, leaving every scene unanswered. */
    public static GameEngine aliveAt(int age) {
        return aliveAt(age, null);
    }

    /** Every choice on this scene the character is actually allowed to take. */
    public static List<Choice> openChoices(GameEngine engine, GameEvent event) {
        List<Choice> open = new ArrayList<>();

        for (Choice choice : event.getChoices()) {
            if (engine.canChoose(choice)) {
                open.add(choice);
            }
        }

        return open;
    }

    /** The first choice that is not locked, or null when none is. */
    public static Choice firstOpen(GameEngine engine, GameEvent event) {
        List<Choice> open = openChoices(engine, event);
        return open.isEmpty() ? null : open.get(0);
    }

    /** A choice at random from the ones that are open, or null when none is. */
    public static Choice anyOpen(GameEngine engine, GameEvent event, Random random) {
        List<Choice> open = openChoices(engine, event);
        return open.isEmpty() ? null : open.get(random.nextInt(open.size()));
    }
}
