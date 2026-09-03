package com.example.al_mirath;

import com.example.al_mirath.controller.GameController;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Labeled;
import javafx.scene.text.Text;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

/**
 * Catches labels and buttons whose text does not fit the space they are given.
 *
 * <p>JavaFX quietly ellipsizes a {@code Labeled} that is squeezed below its
 * text width — "Challenge Fate" becomes "Chall...", with no error anywhere.
 * The only way to notice is to look, which is how several of these reached
 * the player. This measures every control on every screen instead.
 */
class TextFitTest {

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

    /**
     * A control is clipped when its own text needs more room than it was
     * given. Wrapping controls are exempt: they answer a shortage with another
     * line rather than an ellipsis.
     */
    private List<String> clippedIn(Parent root) {
        List<String> clipped = new ArrayList<>();

        for (Node node : root.lookupAll("*")) {
            if (!(node instanceof Labeled labeled)) {
                continue;
            }

            if (labeled.isWrapText()
                    || !labeled.isVisible()
                    || labeled.getText() == null
                    || labeled.getText().isBlank()) {

                continue;
            }

            // A control that was never laid out has nothing to say.
            if (labeled.getWidth() <= 0) {
                continue;
            }

            Text probe = new Text(labeled.getText());
            probe.setFont(labeled.getFont());

            // getInsets() already covers padding plus border, so the padding
            // must not be added again on top of it.
            double needed = probe.getLayoutBounds().getWidth()
                    + labeled.getInsets().getLeft()
                    + labeled.getInsets().getRight();

            if (needed > labeled.getWidth() + SLACK) {
                clipped.add(String.format(
                        "%s \"%s\" needs %.0fpx, got %.0fpx",
                        labeled.getClass().getSimpleName(),
                        labeled.getText().replace("\n", " "),
                        needed,
                        labeled.getWidth()));
            }
        }

        return clipped;
    }

    /**
     * Shows nodes that ship hidden. The scroll popup and its rewind button are
     * both {@code visible="false"} in the FXML, so an unrevealed scan skips
     * exactly the buttons that were clipped in the first place.
     */
    private void reveal(Parent root, String... selectors) {
        for (String selector : selectors) {
            Node node = root.lookup(selector);

            if (node != null) {
                node.setVisible(true);
                node.setManaged(true);
            }
        }
    }

    private List<String> scan(String fxml, boolean needsController, int w, int h)
            throws Exception {

        AtomicReference<List<String>> found = new AtomicReference<>();
        AtomicReference<Throwable> failure = new AtomicReference<>();
        CountDownLatch done = new CountDownLatch(1);

        Platform.runLater(() -> {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource(fxml));

                if (needsController) {
                    loader.setController(new GameController());
                }

                Parent root = loader.load();
                new Scene(root, w, h);

                reveal(root, "#resultPopup", "#popupRewindButton");

                root.applyCss();
                root.layout();
                root.applyCss();
                root.layout();

                found.set(clippedIn(root));

            } catch (Throwable t) {
                failure.set(t);
            } finally {
                done.countDown();
            }
        });

        assertTrue(done.await(30, TimeUnit.SECONDS), "timed out on " + fxml);

        if (failure.get() != null) {
            fail("could not scan " + fxml, failure.get());
        }

        return found.get();
    }

    private void assertFits(String screen, String fxml, boolean controller) throws Exception {
        List<String> problems = new ArrayList<>();

        // The sizes a player might actually use, narrow end included.
        for (int[] size : new int[][] {{1400, 900}, {1196, 701}, {973, 919}, {700, 922}}) {
            for (String clipped : scan(fxml, controller, size[0], size[1])) {
                problems.add("  at " + size[0] + "x" + size[1] + ": " + clipped);
            }
        }

        if (!problems.isEmpty()) {
            fail("text is clipped on the " + screen + " screen:\n"
                    + String.join("\n", problems));
        }
    }

    /**
     * The overlays build their controls in Java rather than FXML, which is how
     * the trial prompt's three buttons kept being clipped after every FXML
     * screen had been fixed.
     */
    private List<String> scanOverlay(Parent overlay, int w, int h) throws Exception {
        AtomicReference<List<String>> found = new AtomicReference<>();
        AtomicReference<Throwable> failure = new AtomicReference<>();
        CountDownLatch done = new CountDownLatch(1);

        Platform.runLater(() -> {
            try {
                javafx.scene.layout.StackPane root =
                        new javafx.scene.layout.StackPane(overlay);

                Scene scene = new Scene(root, w, h);
                scene.getStylesheets().add(getClass().getResource(
                        "/com/example/al_mirath/css/almirath-theme.css")
                        .toExternalForm());

                root.applyCss();
                root.layout();
                root.applyCss();
                root.layout();

                found.set(clippedIn(root));

            } catch (Throwable t) {
                failure.set(t);
            } finally {
                done.countDown();
            }
        });

        assertTrue(done.await(30, TimeUnit.SECONDS), "timed out on overlay");

        if (failure.get() != null) {
            fail("could not scan overlay", failure.get());
        }

        return found.get();
    }

    @Test
    @DisplayName("no text is clipped on the trial prompt")
    void trialPromptFits() throws Exception {
        List<String> problems = new ArrayList<>();

        for (int[] size : new int[][] {{1400, 900}, {1196, 701}, {973, 919}}) {
            com.example.al_mirath.model.Choice choice =
                    new com.example.al_mirath.model.Choice(
                            "Speak, and keep it to demands rather than violence",
                            "reputation", 50, "ok", "no",
                            java.util.Map.of(), java.util.Map.of(),
                            java.util.Map.of(), java.util.Map.of(),
                            List.of(), List.of(), List.of());

            var overlay = new com.example.al_mirath.ui.TrialPromptOverlay(
                    choice, 49, decision -> { });

            for (String clipped : scanOverlay(overlay, size[0], size[1])) {
                problems.add("  at " + size[0] + "x" + size[1] + ": " + clipped);
            }
        }

        if (!problems.isEmpty()) {
            fail("text is clipped on the trial prompt:\n" + String.join("\n", problems));
        }
    }

    @Test
    @DisplayName("no text is clipped on the welcome screen")
    void welcomeFits() throws Exception {
        assertFits("welcome", "/com/example/al_mirath/fxml/welcome-screen.fxml", false);
    }

    @Test
    @DisplayName("no text is clipped on the game screen")
    void gameFits() throws Exception {
        assertFits("game", "/com/example/al_mirath/fxml/game-screen.fxml", true);
    }

    @Test
    @DisplayName("no text is clipped on the settings screen")
    void settingsFits() throws Exception {
        assertFits("settings", "/com/example/al_mirath/fxml/settings-screen.fxml", false);
    }

    @Test
    @DisplayName("no text is clipped on the legacy records screen")
    void recordsFits() throws Exception {
        assertFits("legacy records", "/com/example/al_mirath/fxml/legacy-records-screen.fxml", false);
    }

    @Test
    @DisplayName("no text is clipped on the achievements screen")
    void achievementsFits() throws Exception {
        assertFits("achievements", "/com/example/al_mirath/fxml/achievements-screen.fxml", false);
    }
}
