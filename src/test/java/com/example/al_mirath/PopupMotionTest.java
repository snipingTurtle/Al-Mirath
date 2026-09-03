package com.example.al_mirath;

import com.example.al_mirath.controller.GameController;
import javafx.animation.Animation;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

/**
 * The background drift must stop while a scroll is open.
 *
 * <p>It repaints the whole window every frame, and the scrim, the HUD and its
 * shadows, and the scroll's alpha channel are all blended again with it —
 * none of it visible behind a full-screen scroll. Leaving it running is what
 * made the popup pages feel heavy.
 */
class PopupMotionTest {

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

    @Test
    @DisplayName("the drift pauses while a scroll is open and resumes after it closes")
    void driftFollowsPopupVisibility() throws Exception {
        AtomicReference<Throwable> failure = new AtomicReference<>();
        CountDownLatch done = new CountDownLatch(1);

        Platform.runLater(() -> {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource(
                        "/com/example/al_mirath/fxml/game-screen.fxml"));
                loader.setController(new GameController());

                Parent root = loader.load();
                new Scene(root, 1196, 701);

                Object controller = loader.getController();

                start(controller);

                Timeline drift = field(controller, "backgroundMotion");

                assertEquals(Animation.Status.RUNNING, drift.getStatus(),
                        "the drift should be running before a scroll opens");

                Node popup = field(controller, "resultPopup");

                popup.setVisible(true);
                assertEquals(Animation.Status.PAUSED, drift.getStatus(),
                        "the drift should stop while a scroll is on screen");

                // Every path that hides the popup must release it, not just
                // the ordinary close: a trial starting, a reset and a restart
                // all set this directly.
                popup.setVisible(false);
                assertEquals(Animation.Status.RUNNING, drift.getStatus(),
                        "the drift should resume once the scroll is gone");

            } catch (Throwable t) {
                failure.set(t);
            } finally {
                done.countDown();
            }
        });

        assertTrue(done.await(30, TimeUnit.SECONDS), "timed out");

        if (failure.get() != null) {
            fail("drift did not follow the scroll", failure.get());
        }
    }

    /** The drift only starts once the screen decides to show a background. */
    private void start(Object controller) throws Exception {
        Method m = controller.getClass().getDeclaredMethod("animateBackgroundMotion");
        m.setAccessible(true);
        m.invoke(controller);
    }

    @SuppressWarnings("unchecked")
    private <T> T field(Object controller, String name) throws Exception {
        Field f = controller.getClass().getDeclaredField(name);
        f.setAccessible(true);
        return (T) f.get(controller);
    }
}
