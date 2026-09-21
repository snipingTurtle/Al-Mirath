package com.example.al_mirath;

import com.example.al_mirath.controller.GameController;
import com.example.al_mirath.core.GameSettings;
import com.example.al_mirath.service.GameEngine;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.ScrollPane;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

/**
 * A passage too long for the parchment reads itself.
 *
 * <p>Holding a popup's content to the paper keeps its title off the rolled-up
 * ends, but it also puts the end of a long scene below the fold, behind a
 * scrollbar a couple of pixels wide. A world event announcing a rebellion
 * stopped mid-sentence at "has declared defiance, and the court", and a birth
 * hid half of what the life was being given.
 */
class AutoScrollTest {

    /** Longer than any parchment: this is the case that needs the walk. */
    private static final String A_LONG_PASSAGE = """
            Word arrives of an uprising in the outer provinces. Tax collectors \
            have been driven out, a local commander has declared defiance, and \
            the court cannot agree whether this is a rebellion or a complaint. \
            Whoever is sent will be blamed for whichever it turns out to be, \
            and the roads south are already watched by people who have decided \
            the answer for themselves. The city waits to hear what will be done, \
            and every house with a name worth keeping is counting which way it \
            ought to be seen to lean before the season turns.""";

    @BeforeAll
    static void startToolkit() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);

        try {
            Platform.startup(latch::countDown);
        } catch (IllegalStateException alreadyRunning) {
            latch.countDown();
        }

        assertTrue(latch.await(30, TimeUnit.SECONDS), "the toolkit should start");
    }

    @AfterEach
    void putTheSettingBack() {
        GameSettings.setAutoScrollEnabled(true);
    }

    @Test
    @DisplayName("a passage longer than the paper walks itself down")
    void longPassagesScrollThemselves() throws Exception {
        GameSettings.setAutoScrollEnabled(true);

        Screen screen = openPopupWith(A_LONG_PASSAGE);

        assertNotNull(walker(screen.controller),
                "a passage that does not fit should have started scrolling");

        // Reading pace, so this is deliberately unhurried: a lead-in of 1.8s
        // and then 24px a second.
        double moved = waitForScroll(screen.scroll, 8000);

        assertTrue(moved > 0.05,
                "the passage should have moved down on its own, but sat at " + moved);
    }

    @Test
    @DisplayName("a passage that fits is left alone")
    void shortPassagesStayPut() throws Exception {
        GameSettings.setAutoScrollEnabled(true);

        Screen screen = openPopupWith("The year passes quietly.");

        assertNull(walker(screen.controller),
                "nothing is hidden, so nothing should be scrolling");
    }

    @Test
    @DisplayName("the setting turns it off")
    void theSettingTurnsItOff() throws Exception {
        GameSettings.setAutoScrollEnabled(false);

        Screen screen = openPopupWith(A_LONG_PASSAGE);

        assertNull(walker(screen.controller),
                "with the setting off nothing should scroll itself");
    }

    /** The field is private, as the reveal it sits beside is. */
    private Object walker(GameController controller) throws Exception {
        Field field = controller.getClass().getDeclaredField("autoScrollTimeline");
        field.setAccessible(true);

        AtomicReference<Object> value = new AtomicReference<>();
        onFxThread(() -> value.set(field.get(controller)));

        return value.get();
    }

    /** How far down the passage has walked by the deadline. */
    private double waitForScroll(ScrollPane scroll, long millis) throws Exception {
        long deadline = System.currentTimeMillis() + millis;
        double moved = 0;

        while (System.currentTimeMillis() < deadline && moved <= 0.05) {
            Thread.sleep(200);

            AtomicReference<Double> value = new AtomicReference<>(0.0);
            onFxThread(() -> value.set(scroll.getVvalue()));

            moved = value.get();
        }

        return moved;
    }

    private record Screen(GameController controller, ScrollPane scroll) {
    }

    private Screen openPopupWith(String message) throws Exception {
        AtomicReference<Screen> screen = new AtomicReference<>();

        onFxThread(() -> {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(
                    "/com/example/al_mirath/fxml/game-screen.fxml"));

            GameController controller = new GameController();
            controller.setRestoredEngine(new GameEngine("Zaynab bint Yusuf"));
            loader.setController(controller);

            Parent root = loader.load();
            new Scene(root, 1280, 720);
            root.applyCss();
            root.layout();

            Class<?> categories = Class.forName(
                    "com.example.al_mirath.controller.GameController$PopupCategory");
            Object worldEvent = null;

            for (Object category : categories.getEnumConstants()) {
                if (category.toString().equals("WORLD_EVENT")) {
                    worldEvent = category;
                }
            }

            Method show = controller.getClass().getDeclaredMethod(
                    "showPopup", String.class, String.class, categories);
            show.setAccessible(true);
            show.invoke(controller, "Rebellion Ignites in the Provinces", message, worldEvent);

            root.applyCss();
            root.layout();

            screen.set(new Screen(controller,
                    (ScrollPane) root.lookup("#popupMessageScroll")));
        });

        // The walk is set going one pulse later, once the pane has measured
        // the text it was just handed.
        onFxThread(() -> { });
        Thread.sleep(300);
        onFxThread(() -> { });

        return screen.get();
    }

    private void onFxThread(FxWork work) throws Exception {
        AtomicReference<Throwable> failure = new AtomicReference<>();
        CountDownLatch done = new CountDownLatch(1);

        Platform.runLater(() -> {
            try {
                work.run();
            } catch (Throwable problem) {
                failure.set(problem);
            } finally {
                done.countDown();
            }
        });

        assertTrue(done.await(60, TimeUnit.SECONDS), "the screen work should finish");

        if (failure.get() instanceof AssertionError assertion) {
            throw assertion;
        }

        if (failure.get() != null) {
            fail("the screen work failed", failure.get());
        }
    }

    private interface FxWork {
        void run() throws Exception;
    }
}
