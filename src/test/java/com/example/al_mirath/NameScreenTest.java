package com.example.al_mirath;

import com.example.al_mirath.controller.NameController;
import com.example.al_mirath.controller.WelcomeController;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Naming yourself, driven the way a player does it.
 *
 * <p>The generator can accept a name perfectly and the screen can load
 * perfectly and the player can still never be asked, because the menu button
 * goes somewhere else. That failure is invisible to every test that stops at
 * the service, and it has happened here before, so these tests press the
 * buttons.
 */
class NameScreenTest {

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

        assertTrue(done.await(60, TimeUnit.SECONDS), "work on the FX thread should finish");

        if (failure.get() != null) {
            throw new AssertionError("the screen failed", failure.get());
        }

        return result.get();
    }

    @Test
    @DisplayName("New Game asks who you are before generating anybody")
    void theMenuLeadsToTheNamingScreen() throws Exception {
        AtomicReference<String> wentTo = new AtomicReference<>("nowhere");

        Main watching = new Main() {
            @Override
            public void showNameScreen() {
                wentTo.set("naming");
            }

            @Override
            public void showGameScreen() {
                wentTo.set("straight into a life");
            }

            @Override
            public void showNewLife(String chosenName) {
                wentTo.set("straight into a life");
            }
        };

        Button newGame = onFxThread(() -> {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource(
                        "/com/example/al_mirath/fxml/welcome-screen.fxml"));

                Parent root = loader.load();
                new Scene(root, 1280, 720);
                root.applyCss();
                root.layout();

                WelcomeController controller = loader.getController();
                controller.setMainApp(watching);

                for (javafx.scene.Node node : root.lookupAll(".welcome-button")) {
                    if (node instanceof Button button
                            && "New Game".equals(button.getText())) {

                        return button;
                    }
                }

                return null;

            } catch (Exception problem) {
                throw new IllegalStateException(problem);
            }
        });

        assertNotNull(newGame, "the menu has no New Game button");

        onFxThread(() -> {
            newGame.fire();
            return null;
        });

        assertEquals(
                "naming", wentTo.get(),
                "pressing New Game went " + wentTo.get()
                        + "; the player is never asked what to call themselves"
        );
    }

    @Test
    @DisplayName("the naming screen opens on a name, and Begin carries what was typed")
    void thePlayerCanTypeOrRoll() throws Exception {
        AtomicReference<String> began = new AtomicReference<>(null);

        Main watching = new Main() {
            @Override
            public void showNewLife(String chosenName) {
                began.set(chosenName);
            }
        };

        AtomicReference<Parent> screen = new AtomicReference<>();
        AtomicReference<NameController> controller = new AtomicReference<>();

        onFxThread(() -> {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource(
                        "/com/example/al_mirath/fxml/name-screen.fxml"));

                Parent root = loader.load();
                new Scene(root, 1280, 720);
                root.applyCss();
                root.layout();

                NameController named = loader.getController();
                named.setMainApp(watching);

                screen.set(root);
                controller.set(named);

                return true;

            } catch (Exception problem) {
                throw new IllegalStateException(problem);
            }
        });

        TextField field = onFxThread(() -> (TextField) screen.get().lookup("#nameField"));

        assertNotNull(field, "the naming screen has no field to type into");

        // A player who wants to get on with it can press Begin immediately.
        assertFalse(
                field.getText() == null || field.getText().isBlank(),
                "the naming screen opened empty, so Begin would name nobody"
        );

        String opened = field.getText();

        // Rolling gives a different name to look at rather than doing nothing.
        boolean rolledSomethingElse = false;

        for (int attempt = 0; attempt < 30 && !rolledSomethingElse; attempt++) {
            onFxThread(() -> {
                ((Button) screen.get().lookup("#rollNameButton")).fire();
                return null;
            });

            rolledSomethingElse = !opened.equals(onFxThread(field::getText));
        }

        assertTrue(
                rolledSomethingElse,
                "'Let fate name me' kept handing back the same name, so it does nothing"
        );

        // And a player who types is taken at their word.
        onFxThread(() -> {
            field.setText("  Zaynab bint Yusuf  ");
            ((Button) screen.get().lookup("#beginButton")).fire();
            return null;
        });

        assertEquals(
                "Zaynab bint Yusuf", began.get(),
                "the life began under '" + began.get() + "' rather than what was typed"
        );
    }
}
