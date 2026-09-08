package com.example.al_mirath;

import com.example.al_mirath.controller.GameController;
import com.example.al_mirath.model.Choice;
import com.example.al_mirath.model.Succession;
import com.example.al_mirath.service.GameEngine;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Labeled;
import javafx.geometry.Orientation;
import javafx.scene.control.ScrollBar;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.Region;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

/**
 * Catches text lost downwards rather than sideways.
 *
 * <p>{@link TextFitTest} measures whether a control is too narrow for its
 * text, and exempts wrapping controls on the grounds that they answer a
 * shortage with another line. They do — until the panel holding them is short
 * of height, at which point a VBox takes the shortfall off every child that
 * can give and a wrapping control loses its last line to an ellipsis just as
 * silently as a narrow one loses its last word.
 *
 * <p>That is what happened to the succession screen below 1280x720: the scene
 * lost two lines and each heir lost the line saying who they had become, so
 * the player chose between "Aged 42, sharp with money. Rabia reads, argues,
 * and…" and the same again. This measures the height every wrapping control
 * needs against the height it was given.
 */
class ChoiceFitTest {

    /** A pixel of slack: text measurement and layout round differently. */
    private static final double SLACK = 1.5;

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

    // ---- lives to look at -------------------------------------------------

    /**
     * The longest a heir's button text gets: a long name, a long trait and a
     * long sentence about what they became.
     *
     * <p>Lives are rolled, so asking for any life with two heirs tests
     * whatever length that run happened to produce — which is how this test
     * passed on a build whose buttons were still being cut. It asks for a
     * hard one instead.
     */
    private static final int A_LONG_HEIR = 105;

    /** A life played to its end whose heirs need every line they can get. */
    private GameEngine finishedWithHeirs() {
        Random random = new Random(31);

        for (int attempt = 0; attempt < 400; attempt++) {
            GameEngine engine = new GameEngine("Zaynab bint Yusuf");

            while (engine.getCurrentEvent() != null && engine.getPlayer().isAlive()) {
                List<Choice> playable = playable(engine);

                if (playable.isEmpty()) {
                    break;
                }

                engine.applyChoice(playable.get(random.nextInt(playable.size())));
            }

            List<Succession> heirs = engine.getSuccessors();

            if (heirs.size() >= 2 && longest(heirs) >= A_LONG_HEIR) {
                return engine;
            }
        }

        return fail("in four hundred lives, none ended with two long-described heirs");
    }

    /** The longest button caption this succession would put on screen. */
    private int longest(List<Succession> heirs) {
        int longest = 0;

        for (Succession heir : heirs) {
            longest = Math.max(longest, (heir.offer() + heir.describe()).length());
        }

        return longest;
    }

    /** A living run stopped on a long scene, which is where height runs out. */
    private GameEngine onALongScene() {
        Random random = new Random(5);

        for (int attempt = 0; attempt < 200; attempt++) {
            GameEngine engine = new GameEngine("Zaynab bint Yusuf");

            for (int step = 0; step < 14; step++) {
                if (engine.getCurrentEvent() == null || !engine.getPlayer().isAlive()) {
                    break;
                }

                List<Choice> playable = playable(engine);

                if (playable.isEmpty()) {
                    break;
                }

                engine.applyChoice(playable.get(random.nextInt(playable.size())));
            }

            if (engine.getPlayer().isAlive()
                    && engine.getCurrentEvent() != null
                    && engine.getCurrentEvent().getDescription().length() > 400) {

                return engine;
            }
        }

        return fail("in two hundred lives, none stopped on a long scene");
    }

    private List<Choice> playable(GameEngine engine) {
        List<Choice> playable = new ArrayList<>();

        for (Choice choice : engine.getCurrentEvent().getChoices()) {
            if (engine.canChoose(choice)) {
                playable.add(choice);
            }
        }

        return playable;
    }

    // ---- the screen -------------------------------------------------------

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

    /**
     * Puts the screen in front of us at a given size, past the popup and past
     * the typewriter, and lays it out.
     */
    private Parent screenFor(GameEngine engine, int width, int height) throws Exception {
        AtomicReference<Parent> screen = new AtomicReference<>();

        onFxThread(() -> {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource(
                        "/com/example/al_mirath/fxml/game-screen.fxml"));

                GameController controller = new GameController();
                controller.setRestoredEngine(engine);
                loader.setController(controller);

                Parent root = loader.load();
                new Scene(root, width, height);
                root.applyCss();
                root.layout();

                screen.set(root);

                return true;

            } catch (Exception problem) {
                throw new IllegalStateException(problem);
            }
        });

        Thread.sleep(1400);

        onFxThread(() -> {
            Button go = (Button) screen.get().lookup("#popupContinueButton");

            if (go != null && go.isVisible()) {
                go.fire();
            }

            return null;
        });

        // The scene is typed out a character at a time, and only reaches its
        // full height when the last of it has been typed.
        Thread.sleep(12000);

        onFxThread(() -> {
            screen.get().applyCss();
            screen.get().layout();

            return null;
        });

        return screen.get();
    }

    // ---- the measurement --------------------------------------------------

    /**
     * Every wrapping control in the event panel that was given less height
     * than its own text needs. A control inside a scroll pane is exempt: it
     * keeps its full height and what does not fit is scrolled to rather than
     * lost.
     *
     * <p>Scoped to the panel the scene and the choices live in. The character
     * drawer has a squeeze of its own below about 1024x640 — its name and
     * origin lines lose their second line the same way — but that is a
     * separate panel with a separate cause, and a test that fails for it
     * would stop being about the choices.
     */
    private List<String> squeezedIn(Parent root) {
        List<String> squeezed = new ArrayList<>();

        Node panel = root.lookup("#eventPanel");

        if (panel == null) {
            return List.of("the event panel is not on the screen at all");
        }

        for (Node node : ((Parent) panel).lookupAll("*")) {
            if (!(node instanceof Labeled labeled)
                    || !labeled.isWrapText()
                    || !labeled.isVisible()
                    || labeled.getText() == null
                    || labeled.getText().isBlank()
                    || labeled.getWidth() <= 0) {

                continue;
            }

            double needed = labeled.prefHeight(labeled.getWidth());

            if (needed > labeled.getHeight() + SLACK) {
                squeezed.add(String.format(
                        "%s \"%s\" needs %.0fpx of height, got %.0fpx",
                        labeled.getClass().getSimpleName(),
                        labeled.getText().replace("\n", " / "),
                        needed,
                        labeled.getHeight()));
            }
        }

        return squeezed;
    }

    private List<Button> visibleChoices(Parent root) {
        List<Button> choices = new ArrayList<>();

        for (String id : new String[]{"#choiceButton1", "#choiceButton2", "#choiceButton3"}) {
            Button button = (Button) root.lookup(id);

            if (button != null && button.isVisible() && button.getWidth() > 0) {
                choices.add(button);
            }
        }

        return choices;
    }

    // ---- what must hold ---------------------------------------------------

    @Test
    @DisplayName("an heir's description survives a smaller window")
    void theSuccessionIsReadableWhenTheWindowIsSmall() throws Exception {
        GameEngine finished = finishedWithHeirs();

        for (int[] size : new int[][]{{1280, 720}, {1024, 640}, {1000, 600}, {900, 520}, {860, 480}}) {
            Parent screen = screenFor(finished, size[0], size[1]);

            List<Button> choices = onFxThread(() -> visibleChoices(screen));

            assertTrue(
                    choices.size() >= 2,
                    "the succession showed fewer than two choices at "
                            + size[0] + "x" + size[1]
            );

            List<String> squeezed = onFxThread(() -> squeezedIn(screen));

            if (!squeezed.isEmpty()) {
                fail("at " + size[0] + "x" + size[1]
                        + " the succession lost text downwards: " + squeezed);
            }
        }
    }

    @Test
    @DisplayName("a long scene's choices are readable when the window is small")
    void ordinaryChoicesSurviveToo() throws Exception {
        GameEngine living = onALongScene();

        for (int[] size : new int[][]{{1280, 720}, {1024, 640}}) {
            Parent screen = screenFor(living, size[0], size[1]);

            List<String> squeezed = onFxThread(() -> squeezedIn(screen));

            if (!squeezed.isEmpty()) {
                fail("at " + size[0] + "x" + size[1]
                        + " a long scene lost text downwards: " + squeezed);
            }
        }
    }

    /**
     * The scene gives its height up, but only when there is not enough to go
     * round. At the size the game opens at it must be shown whole, with no
     * scroll bar on a scene that fits.
     */
    @Test
    @DisplayName("the scene is shown whole in a window that has room for it")
    void nothingScrollsThatDoesNotHaveTo() throws Exception {
        Parent screen = screenFor(finishedWithHeirs(), 1280, 720);

        Double slack = onFxThread(() -> {
            ScrollPane pane = (ScrollPane) screen.lookup("#eventDescriptionScroll");

            assertNotNull(pane, "the scene has no scroll pane to give height up with");

            Region content = (Region) pane.getContent();

            return pane.getViewportBounds().getHeight() - content.getHeight();
        });

        assertTrue(
                slack >= 0,
                "at the size the game opens at, the scene was "
                        + Math.abs(slack) + "px taller than the room it was given, "
                        + "so the player is asked to scroll a scene that fits"
        );
    }

    /**
     * When the scene does give height up, the player has to be able to tell.
     *
     * <p>Scrolling that leaves no mark on the screen is indistinguishable from
     * text that was simply cut: the reader has no reason to try. The bar is
     * the difference between a scene that continues and a scene that stopped.
     */
    @Test
    @DisplayName("a scene that had to give up height says so")
    void theScrollBarShowsWhenThereIsMoreToRead() throws Exception {
        Parent screen = screenFor(finishedWithHeirs(), 1000, 600);

        Double overflow = onFxThread(() -> {
            ScrollPane pane = (ScrollPane) screen.lookup("#eventDescriptionScroll");

            assertNotNull(pane, "the scene has no scroll pane");

            Region content = (Region) pane.getContent();

            return content.getHeight() - pane.getViewportBounds().getHeight();
        });

        assertTrue(
                overflow > 0,
                "this size was chosen because the scene does not fit in it, and "
                        + "it fitted; the test is no longer measuring anything"
        );

        Boolean barShowing = onFxThread(() -> {
            ScrollPane pane = (ScrollPane) screen.lookup("#eventDescriptionScroll");

            for (Node bar : pane.lookupAll(".scroll-bar")) {
                if (bar instanceof ScrollBar vertical
                        && vertical.getOrientation() == Orientation.VERTICAL
                        && vertical.isVisible()) {

                    return true;
                }
            }

            return false;
        });

        assertTrue(
                barShowing,
                "the scene was " + overflow + "px taller than the room it had and "
                        + "nothing on screen said so, so the rest of it reads as "
                        + "text that was cut rather than text that continues"
        );
    }

    /**
     * An empty row still costs a gap. The cue box holds the stat changes after
     * a choice and is empty on most screens, including the succession; leaving
     * it in the layout spent eighteen pixels of a panel that had none spare.
     */
    @Test
    @DisplayName("the change-cue row leaves the layout when it has nothing to say")
    void anEmptyRowCostsNothing() throws Exception {
        Parent screen = screenFor(finishedWithHeirs(), 1024, 640);

        Boolean managed = onFxThread(() -> {
            Node box = screen.lookup("#changeCueBox");

            assertNotNull(box, "the change-cue row is not on the screen at all");

            return box.isManaged();
        });

        assertTrue(
                !managed,
                "the change-cue row is empty on the succession screen and is "
                        + "still taking up a row of a panel that is out of room"
        );
    }
}
