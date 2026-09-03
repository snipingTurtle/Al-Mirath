package com.example.al_mirath;

import com.example.al_mirath.controller.GameController;
import com.example.al_mirath.controller.ScreenLifecycle;
import javafx.animation.Animation;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

/**
 * Guards the screen-animation leak.
 *
 * <p>JavaFX timelines run on a global timer, so an indefinite animation
 * started by a screen keeps firing — and keeps its scene graph and decoded
 * background alive — long after the stage has moved to another screen. Every
 * visit used to start another set. These tests assert each screen shuts its
 * own animations down.
 */
class ScreenLifecycleTest {

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

    /**
     * Collects every {@link Timeline} the controller holds, by type rather
     * than by name so the guard survives a field being renamed.
     */
    private List<Timeline> timelinesOf(Object controller) throws Exception {
        List<Timeline> found = new ArrayList<>();

        for (Field field : controller.getClass().getDeclaredFields()) {
            if (!Timeline.class.isAssignableFrom(field.getType())) {
                continue;
            }

            field.setAccessible(true);

            Timeline timeline = (Timeline) field.get(controller);

            if (timeline != null) {
                found.add(timeline);
            }
        }

        return found;
    }

    private void assertDisposeStopsAnimations(String fxml, Object suppliedController)
            throws Exception {

        AtomicReference<Throwable> failure = new AtomicReference<>();
        CountDownLatch done = new CountDownLatch(1);

        Platform.runLater(() -> {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource(fxml));

                if (suppliedController != null) {
                    loader.setController(suppliedController);
                }

                loader.load();

                Object controller = loader.getController();

                assertTrue(controller instanceof ScreenLifecycle,
                        controller.getClass().getSimpleName()
                                + " starts animations, so it must implement ScreenLifecycle");

                ScreenLifecycle screen = (ScreenLifecycle) controller;

                screen.dispose();

                for (Timeline timeline : timelinesOf(controller)) {
                    assertTrue(timeline.getStatus() == Animation.Status.STOPPED,
                            "dispose() left an animation running");
                }

                // Screen swaps can dispose a screen that never started, and
                // exitGame() disposes one that is already gone.
                screen.dispose();

            } catch (Throwable t) {
                failure.set(t);
            } finally {
                done.countDown();
            }
        });

        assertTrue(done.await(30, TimeUnit.SECONDS), "timed out on " + fxml);

        if (failure.get() != null) {
            fail("dispose contract broken for " + fxml, failure.get());
        }
    }

    @Test
    @DisplayName("the welcome screen stops its rotation and drift on dispose")
    void welcomeScreenDisposes() throws Exception {
        assertDisposeStopsAnimations(
                "/com/example/al_mirath/fxml/welcome-screen.fxml", null);
    }

    @Test
    @DisplayName("the game screen stops its background drift and reveal on dispose")
    void gameScreenDisposes() throws Exception {
        assertDisposeStopsAnimations(
                "/com/example/al_mirath/fxml/game-screen.fxml", new GameController());
    }
}
