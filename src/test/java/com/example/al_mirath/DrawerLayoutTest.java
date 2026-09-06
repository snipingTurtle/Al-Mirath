package com.example.al_mirath;

import com.example.al_mirath.controller.GameController;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * The drawers along the edges of the game screen.
 *
 * <p>Both failures here shipped, and neither was catchable by reading the
 * FXML: the tabs are hidden and shown from the controller, and a wrapped
 * label that loses its last line reports no error anywhere. The only way to
 * find them is to lay the screen out and measure it.
 */
class DrawerLayoutTest {

    /** A pixel of slack: text measurement and layout round differently. */
    private static final double SLACK = 1.0;

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

    /** Lays the game screen out at a size and hands it to a reader. */
    private <T> T laidOutAt(int width, int height, Function<Parent, T> read)
            throws Exception {

        AtomicReference<T> result = new AtomicReference<>();
        AtomicReference<Throwable> failure = new AtomicReference<>();
        CountDownLatch done = new CountDownLatch(1);

        Platform.runLater(() -> {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource(
                        "/com/example/al_mirath/fxml/game-screen.fxml"));

                loader.setController(new GameController());

                Parent root = loader.load();
                new Scene(root, width, height);

                root.applyCss();
                root.layout();
                root.applyCss();
                root.layout();

                result.set(read.apply(root));
            } catch (Throwable problem) {
                failure.set(problem);
            } finally {
                done.countDown();
            }
        });

        assertTrue(done.await(60, TimeUnit.SECONDS), "layout should finish");

        if (failure.get() != null) {
            throw new AssertionError("laying out the game screen failed", failure.get());
        }

        return result.get();
    }

    /**
     * The tabs are hidden together while the birth intro is on screen and
     * revealed together afterwards. Adding a fourth drawer to a controller
     * that listed the first three by hand left its tab floating over the
     * intro on every new game.
     */
    @Test
    @DisplayName("every drawer tab is hidden and shown with the others")
    void noTabIsLeftBehindWhenTheRestAreHidden() throws Exception {
        Map<String, Boolean> shown = laidOutAt(1440, 900, root -> {
            Map<String, Boolean> state = new LinkedHashMap<>();

            for (Node node : ((StackPane) root).getChildren()) {
                if (node.getId() == null
                        || !node.getId().endsWith("DrawerButton")
                        || !(node instanceof Button)) {

                    continue;
                }

                state.put(node.getId(), node.isVisible() && node.isManaged());
            }

            return state;
        });

        assertTrue(shown.size() >= 4, "expected every drawer tab, found " + shown.keySet());

        List<String> visible = new ArrayList<>();
        List<String> hidden = new ArrayList<>();

        shown.forEach((id, isShown) -> (isShown ? visible : hidden).add(id));

        assertTrue(
                visible.isEmpty() || hidden.isEmpty(),
                "the drawer tabs disagree about whether they should be on screen: "
                        + visible + " are showing while " + hidden + " are hidden"
        );
    }

    /**
     * A {@code wrapText} label squeezed below its preferred height drops its
     * last line silently, which is why TextFitTest exempts wrapping controls
     * and why this went unnoticed at smaller window sizes.
     */
    @Test
    @DisplayName("no drawer note loses its last line at any window size")
    void theNoteAtTheBottomIsFullyReadable() throws Exception {
        for (int[] size : new int[][]{{1440, 900}, {1280, 720}, {1024, 640}}) {
            List<String> clipped = laidOutAt(size[0], size[1], root -> {
                List<String> tooSmall = new ArrayList<>();

                for (Node node : root.lookupAll(".drawer-note")) {
                    if (!(node instanceof Label label) || label.getWidth() <= 0) {
                        continue;
                    }

                    double needed = label.prefHeight(label.getWidth());

                    if (needed > label.getHeight() + SLACK) {
                        tooSmall.add(String.format(
                                "\"%s\" needs %.0fpx, got %.0fpx",
                                label.getText(), needed, label.getHeight()));
                    }
                }

                return tooSmall;
            });

            assertTrue(
                    clipped.isEmpty(),
                    "at " + size[0] + "x" + size[1]
                            + " these drawer notes lose their last line: " + clipped
            );
        }
    }
}
