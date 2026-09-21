package com.example.al_mirath;

import com.example.al_mirath.controller.GameController;
import com.example.al_mirath.service.GameEngine;
import com.example.al_mirath.service.Lives;
import com.example.al_mirath.ui.ListMenuOverlay;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.Pane;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Random;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * The loop the player actually touches.
 *
 * <p>Age Up is the only control that moves time, the menus are where a year is
 * spent, and the chronicle is what the player reads. None of that is testable
 * from the engine alone: the engine can be perfect while the button is wired
 * to nothing.
 */
class LifeScreenTest {

    @BeforeAll
    static void startToolkit() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);

        try {
            Platform.startup(latch::countDown);
        } catch (IllegalStateException alreadyRunning) {
            latch.countDown();
        }

        assertTrue(latch.await(30, TimeUnit.SECONDS), "JavaFX toolkit should start");
    }

    /** Forces a layout pass, since these scenes are on no Stage to pulse them. */
    private void settle(Parent root) throws Exception {
        onFxThread(() -> {
            root.applyCss();
            root.layout();
            return null;
        });
    }

    private <T> T onFxThread(java.util.function.Supplier<T> work) throws Exception {
        AtomicReference<T> result = new AtomicReference<>();
        AtomicReference<Throwable> failure = new AtomicReference<>();
        CountDownLatch done = new CountDownLatch(1);

        Platform.runLater(() -> {
            try {
                result.set(work.get());
            } catch (Throwable problem) {
                failure.set(problem);
            } finally {
                done.countDown();
            }
        });

        assertTrue(done.await(30, TimeUnit.SECONDS), "the FX thread never came back");

        if (failure.get() != null) {
            throw new AssertionError("on the FX thread: " + failure.get(), failure.get());
        }

        return result.get();
    }

    /**
     * A life already a few years in and still being lived, so the screen has
     * something on it and a clock that still runs.
     *
     * <p>Retried, because a character can die at nineteen and a dead one turns
     * the whole action bar off — which is correct behaviour and makes a test
     * about the action bar fail for the wrong reason.
     */
    private GameEngine aLifeInProgress(int toAge) {
        Random random = new Random(6);

        for (int attempt = 0; attempt < 200; attempt++) {
            GameEngine engine = new GameEngine("Zaynab bint Yusuf");

            while (engine.getPlayer().isAlive() && engine.getPlayer().getAge() < toAge) {
                engine.ageOneYear();

                var scene = engine.getCurrentEvent();

                if (scene != null) {
                    var choice = Lives.anyOpen(engine, scene, random);

                    if (choice != null) {
                        engine.applyChoice(choice);
                    }
                }
            }

            if (engine.getPlayer().isAlive()) {
                return engine;
            }
        }

        throw new IllegalStateException(
                "in two hundred tries nobody reached " + toAge + " alive");
    }

    private Parent screenFor(GameEngine engine) throws Exception {
        Parent root = onFxThread(() -> {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource(
                        "/com/example/al_mirath/fxml/game-screen.fxml"));

                GameController controller = new GameController();
                controller.setRestoredEngine(engine);
                loader.setController(controller);

                Parent parent = loader.load();
                new Scene(parent, 1440, 900);
                parent.applyCss();
                parent.layout();

                return parent;
            } catch (Exception problem) {
                throw new IllegalStateException(problem);
            }
        });

        // The screen finishes assembling itself on the next pulse.
        Thread.sleep(700);

        return root;
    }

    @Test
    @DisplayName("the screen opens on the heartbeat and the things to do with a year")
    void theControlsAreThere() throws Exception {
        Parent screen = screenFor(aLifeInProgress(20));

        Button ageUp = (Button) screen.lookup("#ageUpButton");

        assertNotNull(ageUp, "there is no Age Up button; the game has no clock");
        assertTrue(ageUp.isVisible());
        assertFalse(ageUp.isDisabled(), "a living character cannot age");

        Node actions = screen.lookup("#actionBar");

        assertNotNull(actions, "there is nothing to do with a year");
        assertTrue(((Parent) actions).getChildrenUnmodifiable().size() >= 4,
                "the action bar has almost nothing on it");
    }

    @Test
    @DisplayName("pressing Age Up lives a year and writes it down")
    void theHeartbeatBeats() throws Exception {
        GameEngine engine = aLifeInProgress(20);
        Parent screen = screenFor(engine);

        int ageBefore = engine.getPlayer().getAge();
        int linesBefore = onFxThread(() ->
                ((Parent) screen.lookup("#ageLogBox")).getChildrenUnmodifiable().size());

        onFxThread(() -> {
            ((Button) screen.lookup("#ageUpButton")).fire();
            return null;
        });

        Thread.sleep(500);

        assertEquals(ageBefore + 1, engine.getPlayer().getAge(),
                "the button did not move the year on");

        int linesAfter = onFxThread(() ->
                ((Parent) screen.lookup("#ageLogBox")).getChildrenUnmodifiable().size());

        assertTrue(linesAfter >= linesBefore,
                "the chronicle lost lines when a year passed");

        assertEquals(engine.getLifeLog().size(), linesAfter,
                "the chronicle on screen and the chronicle in the engine disagree");
    }

    @Test
    @DisplayName("the chronicle is looking at the year you just lived")
    void theNewestLineIsInView() throws Exception {
        GameEngine engine = aLifeInProgress(34);
        Parent screen = screenFor(engine);

        onFxThread(() -> {
            ((Button) screen.lookup("#ageUpButton")).fire();
            return null;
        });

        Thread.sleep(600);
        settle(screen);

        Double position = onFxThread(() -> {
            ScrollPane pane = (ScrollPane) screen.lookup("#eventDescriptionScroll");
            javafx.scene.layout.Region content = (javafx.scene.layout.Region) pane.getContent();

            if (content.getHeight() <= pane.getViewportBounds().getHeight()) {
                return 1.0;
            }

            return pane.getVvalue();
        });

        assertTrue(position > 0.95,
                "after ageing up the chronicle was sitting at " + position
                        + ", so the player is reading a year they lived decades ago");
    }

    @Test
    @DisplayName("the activities menu opens with something in it, and closes again")
    void theYearCanBeSpent() throws Exception {
        GameEngine engine = aLifeInProgress(26);
        Parent screen = screenFor(engine);

        onFxThread(() -> {
            for (Node node : screen.lookupAll(".action-button")) {
                if (node instanceof Button button && "Activities".equals(button.getText())) {
                    button.fire();
                }
            }
            return null;
        });

        Thread.sleep(500);
        settle(screen);

        ListMenuOverlay menu = onFxThread(() -> {
            for (Node node : ((Pane) screen).getChildren()) {
                if (node instanceof ListMenuOverlay overlay) {
                    return overlay;
                }
            }
            return null;
        });

        assertNotNull(menu, "the Activities button opened nothing");

        int rows = onFxThread(() -> menu.lookupAll(".menu-row").size());

        assertTrue(rows >= 6,
                "the activities menu offered only " + rows + " things to do with a year");

        onFxThread(() -> {
            menu.dismiss();
            return null;
        });

        Thread.sleep(400);

        Boolean gone = onFxThread(() -> {
            for (Node node : ((Pane) screen).getChildren()) {
                if (node instanceof ListMenuOverlay) {
                    return false;
                }
            }
            return true;
        });

        assertTrue(gone, "the menu would not close");
    }

    @Test
    @DisplayName("every menu on the action bar opens")
    void nothingOnTheBarIsDead() throws Exception {
        GameEngine engine = aLifeInProgress(30);
        Parent screen = screenFor(engine);

        for (String label : new String[]{"Activities", "Trade", "Holdings", "People", "Travel"}) {
            onFxThread(() -> {
                for (Node node : screen.lookupAll(".action-button")) {
                    if (node instanceof Button button && label.equals(button.getText())) {
                        button.fire();
                    }
                }
                return null;
            });

            Thread.sleep(400);
            settle(screen);

            ListMenuOverlay menu = onFxThread(() -> {
                for (Node node : ((Pane) screen).getChildren()) {
                    if (node instanceof ListMenuOverlay overlay) {
                        return overlay;
                    }
                }
                return null;
            });

            assertNotNull(menu, "the " + label + " button opened nothing");

            onFxThread(() -> {
                menu.dismiss();
                return null;
            });

            Thread.sleep(350);
        }
    }

    @Test
    @DisplayName("a character who has died cannot be aged any further")
    void deathStopsTheClock() throws Exception {
        GameEngine engine = new GameEngine("Zaynab bint Yusuf");
        Lives.live(engine, Lives.takingFirstOpenChoice());

        if (engine.getPlayer().isAlive()) {
            return;
        }

        Parent screen = screenFor(engine);

        Thread.sleep(900);

        Boolean stopped = onFxThread(() ->
                ((Button) screen.lookup("#ageUpButton")).isDisabled());

        assertTrue(stopped, "a dead character can still press Age Up");
    }
}
