package com.example.al_mirath;

import com.example.al_mirath.controller.GameController;
import com.example.al_mirath.model.Choice;
import com.example.al_mirath.model.GameEvent;
import com.example.al_mirath.service.GameEngine;
import com.example.al_mirath.service.Lives;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Bounds;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Labeled;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

/**
 * Keeps the scroll's writing on the paper, and its effects unhurried.
 *
 * <p>The parchment is drawn across the middle 47% of the scroll artwork, but
 * the popup's content was sized to the whole image: on a 1280x720 window the
 * outcome line started 45px above the paper's top edge and the Continue
 * button sat 90px below its bottom one, both of them on the rolled-up ends
 * where nothing is legible.
 */
class PopupPaperTest {

    /** Where the paper starts and ends, as a share of the artwork's height. */
    private static final double PAPER_TOP = 0.253;
    private static final double PAPER_BOTTOM = 0.724;

    /** A couple of pixels: layout and the artwork round differently. */
    private static final double SLACK = 3;

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

    @Test
    @DisplayName("a scene and its consequence are written on the paper, not the rolled ends")
    void popupsStayOnThePaper() throws Exception {
        GameEngine engine = aLifeMidScene();
        List<String> problems = new ArrayList<>();

        // The window sizes a player might actually use. The scroll is drawn
        // smaller in a short window, so the paper moves with it.
        for (int[] size : new int[][] {{1400, 900}, {1280, 720}, {1000, 650}, {900, 600}}) {
            onFxThread(() -> {
                Parent root = screenFor(engine, size[0], size[1]);
                GameController controller = controllerOf(root);

                call(controller, "presentScene");
                layout(root);
                problems.addAll(offThePaper(root, size, "scene"));

                call(controller, "showPopup", "", "Your family notices your care.",
                        consequenceCategory());
                layout(root);
                problems.addAll(offThePaper(root, size, "consequence"));

                call(controller, "showPopup", "Birth of a Life",
                        "A new life begins...\n\nYou are born during the Umayyad Era."
                                + "\n\nOrigin: Frontier Soldier's Child"
                                + "\nFamily Condition: Favored by Local Scholars",
                        category("BIRTH"));
                layout(root);
                problems.addAll(offThePaper(root, size, "birth"));

                return null;
            });
        }

        if (!problems.isEmpty()) {
            fail("popup content hangs off the parchment:\n" + String.join("\n", problems));
        }
    }

    @Test
    @DisplayName("the effects of a choice arrive one at a time, and Space brings them all")
    void effectsArriveOneAtATime() throws Exception {
        GameEngine engine = aLifeMidScene();

        String outcome = "Your words do not solve everything.";
        String effects = "+5 Morality\n+10 Family Loyalty\n+3 Stress\n+10 Family Council";

        onFxThread(() -> {
            Parent root = screenFor(engine, 1280, 720);
            GameController controller = controllerOf(root);

            call(controller, "showPopup", "", outcome, consequenceCategory());
            call(controller, "revealEffects", outcome, effects);

            Labeled text = (Labeled) root.lookup("#resultTextLabel");

            assertEquals(0, rowsIn(text.getText()),
                    "every effect was on the scroll before the player could see one land");

            assertTrue(text.getText().contains("Effects of this choice:"),
                    "the header should be up while the lines are still coming");

            // What Space does for a reader who would rather not wait.
            call(controller, "completeEffectReveal");

            assertEquals(4, rowsIn(text.getText()),
                    "skipping the wait should put every remaining effect up at once");

            return null;
        });
    }

    /** Counts the +/- lines of an effect report. */
    private int rowsIn(String text) {
        return (int) text.lines()
                .filter(line -> line.startsWith("+") || line.startsWith("-"))
                .count();
    }

    /** Whatever of the popup's content has left the paper behind it. */
    private List<String> offThePaper(Parent root, int[] size, String what) {
        List<String> problems = new ArrayList<>();

        Node art = root.lookup("#popupScrollBackground");
        Node content = root.lookup("#popupContentBox");

        if (art == null || content == null) {
            return problems;
        }

        Bounds paper = art.localToScene(art.getLayoutBounds());
        double top = paper.getMinY() + paper.getHeight() * PAPER_TOP;
        double bottom = paper.getMinY() + paper.getHeight() * PAPER_BOTTOM;

        // A title squeezed to one line says "Rebellion Ignites in the ...",
        // which is the half a player cannot act on.
        Node title = root.lookup("#popupTitleLabel");

        if (title instanceof Labeled titled
                && titled.isVisible()
                && titled.getText() != null
                && !titled.getText().isBlank()) {

            double needed = titled.prefHeight(titled.getWidth());

            if (titled.getHeight() < needed - SLACK) {
                problems.add(String.format(
                        "  at %dx%d the %s title \"%s\" needs %.0fpx of height, got %.0f",
                        size[0], size[1], what, titled.getText(), needed, titled.getHeight()));
            }
        }

        Bounds box = content.localToScene(content.getLayoutBounds());

        if (box.getMinY() < top - SLACK) {
            problems.add(String.format(
                    "  at %dx%d the %s popup starts %.0fpx above the paper",
                    size[0], size[1], what, top - box.getMinY()));
        }

        if (box.getMaxY() > bottom + SLACK) {
            problems.add(String.format(
                    "  at %dx%d the %s popup runs %.0fpx past the paper",
                    size[0], size[1], what, box.getMaxY() - bottom));
        }

        return problems;
    }

    /** A life stopped on a scene long enough to fill the scroll. */
    private GameEngine aLifeMidScene() {
        Random random = new Random(5);

        for (int attempt = 0; attempt < 200; attempt++) {
            GameEngine engine = new GameEngine("Zaynab bint Yusuf");

            for (int year = 0; year < Lives.LIFE_CEILING; year++) {
                engine.ageOneYear();

                if (!engine.getPlayer().isAlive()) {
                    break;
                }

                GameEvent scene = engine.getCurrentEvent();

                if (scene == null) {
                    continue;
                }

                if (engine.isSceneDue() && scene.getDescription().length() > 300) {
                    return engine;
                }

                Choice choice = Lives.anyOpen(engine, scene, random);

                if (choice != null) {
                    engine.applyChoice(choice);
                }
            }
        }

        return fail("in two hundred lives, none stopped on a scene worth showing");
    }

    private Parent screenFor(GameEngine engine, int width, int height) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(
                    "/com/example/al_mirath/fxml/game-screen.fxml"));

            GameController controller = new GameController();
            controller.setRestoredEngine(engine);
            loader.setController(controller);

            Parent root = loader.load();
            root.getProperties().put("controller", controller);

            new Scene(root, width, height);
            layout(root);

            return root;

        } catch (Exception problem) {
            throw new IllegalStateException("the game screen would not load", problem);
        }
    }

    private GameController controllerOf(Parent root) {
        return (GameController) root.getProperties().get("controller");
    }

    private void layout(Parent root) {
        root.applyCss();
        root.layout();
        root.applyCss();
        root.layout();
    }

    /** The popup categories are private to the controller, as is showing one. */
    private Object consequenceCategory() throws Exception {
        return category("CONSEQUENCE");
    }

    private Object category(String name) throws Exception {
        Class<?> categories = Class.forName(
                "com.example.al_mirath.controller.GameController$PopupCategory");

        for (Object category : categories.getEnumConstants()) {
            if (category.toString().equals(name)) {
                return category;
            }
        }

        return fail("the controller has no " + name + " popup any more");
    }

    private void call(GameController controller, String name, Object... arguments)
            throws Exception {

        for (Method method : controller.getClass().getDeclaredMethods()) {
            if (method.getName().equals(name)
                    && method.getParameterCount() == arguments.length) {

                method.setAccessible(true);
                method.invoke(controller, arguments);
                return;
            }
        }

        fail("the controller no longer has " + name + "()");
    }

    /** Runs the work on the FX thread and brings any failure back here. */
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
            throw new AssertionError("the screen work failed", failure.get());
        }
    }

    private interface FxWork {
        Object run() throws Exception;
    }
}
