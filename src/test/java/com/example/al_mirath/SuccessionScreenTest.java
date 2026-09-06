package com.example.al_mirath;

import com.example.al_mirath.controller.GameController;
import com.example.al_mirath.model.Choice;
import com.example.al_mirath.service.GameEngine;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.StackPane;
import javafx.scene.Node;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

/**
 * The screen a finished life hands you to.
 *
 * <p>The engine could hand a house on for a whole release before anyone
 * noticed the screen never offered it: closing the final chronicle returned
 * to the main menu unconditionally, so a player with three living heirs was
 * simply told the run was over. Every test for the dynasty passed, because
 * they all stopped at the engine. This one drives the click.
 */
class SuccessionScreenTest {

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

    /** A life played to its end that still has somebody to carry the name. */
    private GameEngine aFinishedLifeWithAnHeir() {
        Random random = new Random(31);

        for (int attempt = 0; attempt < 80; attempt++) {
            GameEngine engine = new GameEngine();

            while (engine.getCurrentEvent() != null && engine.getPlayer().isAlive()) {
                List<Choice> available = new ArrayList<>();

                for (Choice choice : engine.getCurrentEvent().getChoices()) {
                    if (engine.canChoose(choice)) {
                        available.add(choice);
                    }
                }

                if (available.isEmpty()) {
                    break;
                }

                engine.applyChoice(available.get(random.nextInt(available.size())));
            }

            if (engine.hasSuccessor()) {
                return engine;
            }
        }

        return fail("in eighty lives, none ended with an heir to hand the house to");
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

        assertTrue(done.await(60, TimeUnit.SECONDS), "work on the FX thread should finish");

        if (failure.get() != null) {
            throw new AssertionError("the game screen failed", failure.get());
        }

        return result.get();
    }

    private final java.util.concurrent.atomic.AtomicBoolean returnedToMenu =
            new java.util.concurrent.atomic.AtomicBoolean(false);

    @Test
    @DisplayName("a finished life with an heir hands you the succession, not the main menu")
    void theChronicleClosesOntoTheNextGeneration() throws Exception {
        GameEngine finished = aFinishedLifeWithAnHeir();
        String heir = finished.getSuccessors().get(0).name();

        // Records the one thing that must not happen: being sent back to the
        // main menu while somebody is still alive to carry the house.
        Main wentBackToTheMenu = new Main() {
            @Override
            public void showWelcomeScreen() {
                returnedToMenu.set(true);
            }
        };

        AtomicReference<Parent> screen = new AtomicReference<>();

        // Loading the screen on a life that is already over shows the chronicle.
        Boolean chronicleShowing = onFxThread(() -> {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource(
                        "/com/example/al_mirath/fxml/game-screen.fxml"));

                GameController controller = new GameController();
                controller.setRestoredEngine(finished);
                controller.setMainApp(wentBackToTheMenu);
                loader.setController(controller);

                Parent root = loader.load();
                new Scene(root, 1440, 900);
                root.applyCss();
                root.layout();

                screen.set(root);

                return true;
            } catch (Exception problem) {
                throw new IllegalStateException(problem);
            }
        });

        assertTrue(chronicleShowing, "the game screen did not load");

        // The chronicle fades in rather than appearing outright.
        Thread.sleep(1200);

        assertTrue(
                onFxThread(() -> screen.get().lookup("#resultPopup").isVisible()),
                "the final chronicle never appeared"
        );

        // Close it, the way the player does.
        onFxThread(() -> {
            ((Button) screen.get().lookup("#popupContinueButton")).fire();
            return null;
        });

        // The close is animated, so give it time to land.
        Thread.sleep(1200);

        String offered = onFxThread(() -> {
            Button first = (Button) screen.get().lookup("#choiceButton1");

            return first.isVisible() ? first.getText() : "";
        });

        assertNotNull(offered);

        assertFalse(
                offered.isBlank(),
                "closing the chronicle left no choices on screen: the run ended "
                        + "even though " + heir + " was alive to carry the house"
        );

        assertTrue(
                offered.contains(heir),
                "the succession offered '" + offered + "' rather than " + heir
        );

        Boolean popupGone = onFxThread(() ->
                !((StackPane) screen.get().lookup("#resultPopup")).isVisible());

        assertTrue(popupGone, "the chronicle never closed");

        assertFalse(
                returnedToMenu.get(),
                "the run was sent back to the main menu even though " + heir
                        + " was alive to carry the house"
        );

        // The choices are inside the HUD, which the chronicle faded out. If it
        // is not faded back in, the succession is on screen but invisible.
        Double hudOpacity = onFxThread(() -> {
            Node hud = screen.get().lookup("#hudLayer");

            return hud == null ? 0.0 : hud.getOpacity();
        });

        assertTrue(
                hudOpacity > 0.5,
                "the succession was laid out behind a HUD still faded to "
                        + hudOpacity + "; the player cannot see or click it"
        );
    }
}
