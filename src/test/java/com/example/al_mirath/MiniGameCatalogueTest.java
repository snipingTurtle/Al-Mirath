package com.example.al_mirath;

import com.example.al_mirath.minigame.MiniGame;
import com.example.al_mirath.minigame.MiniGameFactory;
import com.example.al_mirath.minigame.MiniGameResult;
import com.example.al_mirath.ui.MiniGameOverlay;
import javafx.application.Platform;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.layout.StackPane;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Every trial in the game, opened and shut.
 *
 * <p>A mini-game only fails when somebody plays it: a view that throws while
 * being built, a timer that starts before the node is in the scene, a board
 * that lays out to nothing. None of that shows up in a rules test, and all of
 * it is exactly what a new challenge gets wrong.
 */
class MiniGameCatalogueTest {

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

    @Test
    @DisplayName("every named trial exists and says what it is")
    void everyTrialIntroducesItself() {
        assertFalse(MiniGameFactory.TYPES.isEmpty());

        for (String type : MiniGameFactory.TYPES) {
            for (int difficulty = 1; difficulty <= 5; difficulty++) {
                MiniGame game = MiniGameFactory.createByType(type, difficulty);

                assertNotNull(game, type + " at difficulty " + difficulty + " built nothing");

                assertFalse(game.getTitle().isBlank(), type + " has no title");
                assertFalse(game.getFlavourText().isBlank(),
                        type + " has no framing, so the briefing screen is half empty");
                assertFalse(game.getInstructions().isBlank(),
                        type + " never says how it is played");
            }
        }
    }

    @Test
    @DisplayName("every trial builds a board that can be played on")
    void everyTrialBuildsAView() throws Exception {
        for (String type : MiniGameFactory.TYPES) {
            for (int difficulty : new int[]{1, 3, 5}) {
                MiniGame game = MiniGameFactory.createByType(type, difficulty);

                Node view = onFxThread(() -> {
                    Node built = game.buildView();

                    // In a scene and laid out, which is when a board that
                    // computes its own geometry actually computes it.
                    StackPane host = new StackPane(built);
                    new Scene(host, 1000, 700);
                    host.applyCss();
                    host.layout();

                    return built;
                });

                assertNotNull(view, type + " built no view at difficulty " + difficulty);

                assertTrue(view instanceof Parent,
                        type + " built a leaf node with nothing to play on");

                List<Node> pieces = new ArrayList<>(((Parent) view).lookupAll("*"));

                assertFalse(pieces.isEmpty(),
                        type + " built an empty board at difficulty " + difficulty);
            }
        }
    }

    @Test
    @DisplayName("a trial started and abandoned delivers nothing and stops cleanly")
    void abandoningATrialIsSafe() throws Exception {
        for (String type : MiniGameFactory.TYPES) {
            MiniGame game = MiniGameFactory.createByType(type, 3);

            AtomicInteger results = new AtomicInteger();
            AtomicReference<MiniGameResult> delivered = new AtomicReference<>();

            game.setOnFinished(result -> {
                results.incrementAndGet();
                delivered.set(result);
            });

            onFxThread(() -> {
                StackPane host = new StackPane(game.buildView());
                new Scene(host, 1000, 700);
                host.applyCss();
                host.layout();

                game.start();

                // Twice, because the overlay stops a game it is dismissing and
                // the game stops itself when it finishes.
                game.stop();
                game.stop();

                return null;
            });

            assertTrue(results.get() <= 1,
                    type + " delivered " + results.get() + " results for one attempt");
        }
    }

    @Test
    @DisplayName("the overlay opens every trial without falling over")
    void theOverlayHostsThemAll() throws Exception {
        for (String type : MiniGameFactory.TYPES) {
            MiniGame game = MiniGameFactory.createByType(type, 2);

            Boolean opened = onFxThread(() -> {
                MiniGameOverlay overlay = new MiniGameOverlay(game, result -> { });

                StackPane host = new StackPane(overlay);
                new Scene(host, 1280, 800);
                host.applyCss();
                host.layout();

                // The briefing is beat one; pressing through it is beat two,
                // which is where the board is built and the timers start.
                for (Node node : overlay.lookupAll(".button")) {
                    if (node instanceof javafx.scene.control.Button button
                            && "Begin the Trial".equals(button.getText())) {

                        button.fire();
                    }
                }

                host.applyCss();
                host.layout();

                game.stop();

                return true;
            });

            assertTrue(opened, type + " could not be opened in the overlay");
        }
    }

    @Test
    @DisplayName("a trial asked for by name is the trial that arrives")
    void namesAreHonoured() {
        assertTrue(MiniGameFactory.createByType("archery", 2).getTitle().contains("Butts"));
        assertTrue(MiniGameFactory.createByType("caravan", 2).getTitle().contains("Caravan"));
        assertTrue(MiniGameFactory.createByType("geometer", 2).getTitle().contains("Geometer"));
        assertTrue(MiniGameFactory.createByType("haggle", 2).getTitle().contains("Bargain"));
        assertTrue(MiniGameFactory.createByType("lighthand", 2).getTitle().contains("Light Hand"));
        assertTrue(MiniGameFactory.createByType("prosody", 2).getTitle().contains("Meter"));

        // A name nobody recognises still has to hand back a playable game
        // rather than a null, because it reaches here from content.
        assertNotNull(MiniGameFactory.createByType("nonsense", 3));
        assertNotNull(MiniGameFactory.createByType(null, 3));
    }
}
