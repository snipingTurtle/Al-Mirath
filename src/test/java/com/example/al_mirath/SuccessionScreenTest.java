package com.example.al_mirath;

import com.example.al_mirath.controller.GameController;
import com.example.al_mirath.model.Choice;
import com.example.al_mirath.model.LifePath;
import com.example.al_mirath.model.Succession;
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
import static org.junit.jupiter.api.Assertions.assertEquals;
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

    // ---- the way out -----------------------------------------------------

    /**
     * The label on the button that stops the chronicle rather than continuing
     * it. Matched on rather than an index, because which of the three buttons
     * it lands on depends on how many heirs there are.
     */
    private static final String THE_WAY_OUT = "Let the line end here";

    /**
     * Continuing has to be a decision.
     *
     * <p>Before this, a life that ended with an heir alive had exactly one
     * door out of the chronicle: take up their life. A player who was done
     * with the run had to close the window. The succession offers the end of
     * the line alongside the heirs, and it has to be a button they can
     * actually see and press.
     */
    @Test
    @DisplayName("a succession with heirs still offers to end the line")
    void theLineCanBeEndedDeliberately() throws Exception {
        GameEngine finished = aFinishedLifeWithAnHeir();
        String heir = finished.getSuccessors().get(0).name();

        AtomicReference<Boolean> menu = new AtomicReference<>(false);

        Main watchingForTheMenu = new Main() {
            @Override
            public void showWelcomeScreen() {
                menu.set(true);
            }
        };

        Parent screen = succession(finished, watchingForTheMenu);

        List<String> onScreen = onFxThread(() -> visibleChoices(screen));

        assertTrue(
                onScreen.stream().anyMatch(text -> text.contains(heir)),
                "the succession offered " + onScreen + " rather than " + heir
        );

        Button wayOut = onFxThread(() -> choiceSaying(screen, THE_WAY_OUT));

        assertNotNull(
                wayOut,
                "the succession offered " + onScreen + " and no way to stop; a "
                        + "player done with the run has to close the window"
        );

        // Pressing it closes the house rather than handing it on.
        onFxThread(() -> {
            wayOut.fire();
            return null;
        });

        Thread.sleep(1200);

        assertFalse(
                menu.get(),
                "ending the line skipped straight past its own closing scene"
        );

        assertTrue(
                onFxThread(() -> screen.lookup("#resultPopup").isVisible()),
                "ending the line said nothing at all about what happened"
        );

        onFxThread(() -> {
            ((Button) screen.lookup("#popupContinueButton")).fire();
            return null;
        });

        Thread.sleep(1200);

        assertTrue(
                menu.get(),
                "the player chose to end the line and was left sitting on the "
                        + "succession screen"
        );
    }

    /**
     * There are three choice buttons and the last of them belongs to the way
     * out, so a household full of children cannot crowd it off the screen.
     */
    @Test
    @DisplayName("a crowd of heirs never crowds out the way to stop")
    void theWayOutAlwaysFits() {
        List<Succession> five = new ArrayList<>();

        for (int i = 0; i < 5; i++) {
            five.add(new Succession(
                    "c" + i, "Child " + i, "your child", 30 - i,
                    "watchful", LifePath.SCHOLAR, true
            ));
        }

        List<Succession> offered = GameController.offerableHeirs(five);

        assertEquals(
                2, offered.size(),
                "five claimants filled all three buttons and left no way to stop"
        );

        // The strongest claims are the ones kept.
        assertEquals("Child 0", offered.get(0).name());
        assertEquals("Child 1", offered.get(1).name());

        assertTrue(GameController.offerableHeirs(List.of()).isEmpty());
        assertTrue(GameController.offerableHeirs(null).isEmpty());
    }

    // ---- shared machinery ------------------------------------------------

    /** Loads a finished life and closes its chronicle, as a player would. */
    private Parent succession(GameEngine finished, Main app) throws Exception {
        AtomicReference<Parent> screen = new AtomicReference<>();

        onFxThread(() -> {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource(
                        "/com/example/al_mirath/fxml/game-screen.fxml"));

                GameController controller = new GameController();
                controller.setRestoredEngine(finished);
                controller.setMainApp(app);
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

        Thread.sleep(1200);

        onFxThread(() -> {
            ((Button) screen.get().lookup("#popupContinueButton")).fire();
            return null;
        });

        Thread.sleep(1200);

        return screen.get();
    }

    private List<String> visibleChoices(Parent screen) {
        List<String> texts = new ArrayList<>();

        for (String id : new String[]{"#choiceButton1", "#choiceButton2", "#choiceButton3"}) {
            Button button = (Button) screen.lookup(id);

            if (button != null && button.isVisible()) {
                texts.add(button.getText());
            }
        }

        return texts;
    }

    private Button choiceSaying(Parent screen, String wanted) {
        for (String id : new String[]{"#choiceButton1", "#choiceButton2", "#choiceButton3"}) {
            Button button = (Button) screen.lookup(id);

            if (button != null
                    && button.isVisible()
                    && button.getText() != null
                    && button.getText().contains(wanted)) {

                return button;
            }
        }

        return null;
    }
}
