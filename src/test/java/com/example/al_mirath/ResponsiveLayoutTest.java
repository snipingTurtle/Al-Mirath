package com.example.al_mirath;

import com.example.al_mirath.controller.GameController;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.Region;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

/**
 * Guards the game screen against being crushed in a narrow window.
 *
 * <p>The layout is authored for roughly 1090px. Below that the panels used to
 * share out whatever space there was, squeezing the stats column until its
 * labels broke into one-syllable lines ("Fa / mil / y / L..."). These tests
 * pin the floors that stop it.
 */
class ResponsiveLayoutTest {

    /** Widest label the stats column has to show on one line. */
    private static final String LONGEST_STAT = "Family Loyalty: 100";

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

    private record Layout(boolean statsVisible, double statsWidth,
                          double eventWidth, String fateText) { }

    private Layout layoutAt(int width, int height) throws Exception {
        AtomicReference<Layout> result = new AtomicReference<>();
        AtomicReference<Throwable> failure = new AtomicReference<>();
        CountDownLatch done = new CountDownLatch(1);

        Platform.runLater(() -> {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource(
                        "/com/example/al_mirath/fxml/game-screen.fxml"));
                loader.setController(new GameController());

                Parent root = loader.load();
                new Scene(root, width, height);

                // Twice: the first pass gives the root its width, which is what
                // the breakpoint listener reacts to.
                root.applyCss();
                root.layout();
                root.applyCss();
                root.layout();

                Object controller = loader.getController();

                Region stats = field(controller, "statsPanel");
                Region event = field(controller, "eventPanel");
                Label fate = field(controller, "fateTokenLabel");

                result.set(new Layout(
                        stats.isVisible(), stats.getWidth(),
                        event.getWidth(), fate.getText()));

            } catch (Throwable t) {
                failure.set(t);
            } finally {
                done.countDown();
            }
        });

        assertTrue(done.await(30, TimeUnit.SECONDS), "timed out at " + width + "px");

        if (failure.get() != null) {
            fail("layout failed at " + width + "px", failure.get());
        }

        return result.get();
    }

    @SuppressWarnings("unchecked")
    private <T> T field(Object controller, String name) throws Exception {
        Field f = controller.getClass().getDeclaredField(name);
        f.setAccessible(true);
        return (T) f.get(controller);
    }

    /** Width the longest stat label needs, plus the panel's own padding. */
    private double requiredStatsWidth() {
        javafx.scene.text.Text probe = new javafx.scene.text.Text(LONGEST_STAT);
        probe.setFont(javafx.scene.text.Font.font("Constantia", 15));

        return probe.getLayoutBounds().getWidth();
    }

    @Test
    @DisplayName("at the design width both panels keep their authored size")
    void designWidth() throws Exception {
        Layout layout = layoutAt(1400, 900);

        assertTrue(layout.statsVisible(), "stats column should show on a wide window");
        assertTrue(layout.statsWidth() >= 250,
                "stats column shrank at design width: " + layout.statsWidth());
        assertTrue(layout.eventWidth() >= 760,
                "event panel shrank at design width: " + layout.eventWidth());
        assertTrue(layout.fateText().startsWith("Threads of Fate"),
                "wide windows should show the full counter name");
    }

    @Test
    @DisplayName("in a narrow window the stats column stays readable")
    void narrowWindowKeepsStatsReadable() throws Exception {
        Layout layout = layoutAt(700, 922);

        assertTrue(layout.statsVisible(), "stats column should still show at 700px");

        // This is the actual regression: the column used to be squeezed well
        // under the width of its own longest label.
        assertTrue(layout.statsWidth() > requiredStatsWidth(),
                "stats column too narrow for \"" + LONGEST_STAT + "\": "
                        + layout.statsWidth() + "px");

        assertTrue(layout.eventWidth() >= 300,
                "event panel left unusable: " + layout.eventWidth());
    }

    @Test
    @DisplayName("below the breakpoint the stats column steps aside")
    void veryNarrowWindowDropsStats() throws Exception {
        Layout layout = layoutAt(560, 800);

        assertFalse(layout.statsVisible(),
                "stats column should give way when there is no room for it");

        assertTrue(layout.eventWidth() >= 300,
                "event panel should take the freed space: " + layout.eventWidth());

        assertTrue(layout.fateText().startsWith("Fate:"),
                "counter should shorten rather than wrap: " + layout.fateText());
    }
}
