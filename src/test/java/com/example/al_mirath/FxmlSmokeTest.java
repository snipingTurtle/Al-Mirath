package com.example.al_mirath;

import com.example.al_mirath.controller.GameController;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

/**
 * Loads every FXML screen through a real FXMLLoader.
 *
 * <p>A malformed layout, a missing controller method referenced by onAction, or
 * an fx:id with no matching field only fails when the screen is opened. These
 * tests surface that at build time instead of leaving it for the player to find.
 */
class FxmlSmokeTest {

    private static boolean toolkitStarted = false;

    @BeforeAll
    static void startToolkit() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);

        try {
            Platform.startup(latch::countDown);
        } catch (IllegalStateException alreadyRunning) {
            // Another test in this JVM already started it.
            latch.countDown();
        }

        toolkitStarted = latch.await(30, TimeUnit.SECONDS);
        assertTrue(toolkitStarted, "JavaFX toolkit should start");
    }

    private Object load(String resource) throws Exception {
        return load(resource, null);
    }

    /**
     * Loads an FXML on the FX thread and rethrows whatever it threw, so a
     * failure names the real cause rather than a timeout.
     *
     * @param controller supplied for screens that declare no {@code fx:controller},
     *                   matching how {@link Main} injects one at runtime
     */
    private Object load(String resource, Object controller) throws Exception {
        AtomicReference<Object> loaded = new AtomicReference<>();
        AtomicReference<Throwable> failure = new AtomicReference<>();

        CountDownLatch latch = new CountDownLatch(1);

        Platform.runLater(() -> {
            try {
                var url = FxmlSmokeTest.class.getResource(resource);

                if (url == null) {
                    throw new IllegalStateException("FXML not found on classpath: " + resource);
                }

                FXMLLoader loader = new FXMLLoader(url);

                if (controller != null) {
                    loader.setController(controller);
                }

                loaded.set(loader.load());

            } catch (Throwable t) {
                failure.set(t);
            } finally {
                latch.countDown();
            }
        });

        if (!latch.await(30, TimeUnit.SECONDS)) {
            fail("Timed out loading " + resource);
        }

        if (failure.get() != null) {
            throw new AssertionError("Failed to load " + resource, failure.get());
        }

        return loaded.get();
    }

    @Test
    @DisplayName("the welcome screen loads with all controls bound")
    void welcomeScreenLoads() throws Exception {
        assertNotNull(load("/com/example/al_mirath/fxml/welcome-screen.fxml"));
    }

    /**
     * The game screen declares no fx:controller on purpose: Main constructs the
     * controller first so a restored engine can be handed over before
     * initialize() runs. The test injects one the same way.
     */
    @Test
    @DisplayName("the game screen loads, including the fate token and rewind controls")
    void gameScreenLoads() throws Exception {
        assertNotNull(load(
                "/com/example/al_mirath/fxml/game-screen.fxml",
                new GameController()
        ));
    }

    @Test
    @DisplayName("the legacy records screen loads")
    void legacyRecordsScreenLoads() throws Exception {
        assertNotNull(load("/com/example/al_mirath/fxml/legacy-records-screen.fxml"));
    }

    @Test
    @DisplayName("the achievements screen loads and populates its cards")
    void achievementsScreenLoads() throws Exception {
        assertNotNull(load("/com/example/al_mirath/fxml/achievements-screen.fxml"));
    }

    @Test
    @DisplayName("the settings screen loads with its controls bound")
    void settingsScreenLoads() throws Exception {
        assertNotNull(load("/com/example/al_mirath/fxml/settings-screen.fxml"));
    }

    @Test
    @DisplayName("the stylesheet is present on the classpath")
    void stylesheetResolves() {
        assertNotNull(
                FxmlSmokeTest.class.getResource("/com/example/al_mirath/css/almirath-theme.css"),
                "screens reference this stylesheet by relative path"
        );
    }
}
