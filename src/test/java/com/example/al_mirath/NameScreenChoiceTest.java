package com.example.al_mirath;

import com.example.al_mirath.controller.NameController;
import com.example.al_mirath.model.CityProfile;
import com.example.al_mirath.model.LifeStart;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Spinner;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

/**
 * The three boxes on the naming screen, which have to agree with each other.
 *
 * <p>They are dependent in one direction — an era decides which years exist,
 * and a year decides which cities were standing — and getting that wrong is
 * how a player ends up offered a birth in an Istanbul that is seven hundred
 * years away, or an Abbasid year the dynasty never saw.
 */
class NameScreenChoiceTest {

    private static final String LET_FATE_DECIDE = "Let fate decide";

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
    @DisplayName("left alone, the screen asks for nothing in particular")
    void theDefaultIsStillTheRoll() throws Exception {
        onScreen(screen -> {
            assertEquals(LET_FATE_DECIDE, screen.era().getValue());
            assertTrue(screen.year().isDisabled(), "the year box is live before an era is chosen");
            assertTrue(screen.city().isDisabled(), "the city box is live before an era is chosen");

            assertTrue(screen.controller().chosenStart().isRolled(),
                    "a screen nobody touched asked for something anyway");
        });
    }

    @Test
    @DisplayName("choosing an era opens its own years, and no others")
    void theYearsBelongToTheEra() throws Exception {
        onScreen(screen -> {
            screen.era().getSelectionModel().select(CityProfile.ABBASID);

            assertFalse(screen.year().isDisabled());

            Spinner<Integer> year = screen.year();

            assertEquals(762, year.getValueFactory().getValue(),
                    "the Abbasid years should open at the founding of Baghdad");

            // The spinner refuses to leave the era in either direction.
            year.getValueFactory().decrement(5);
            assertEquals(762, year.getValueFactory().getValue());

            year.getValueFactory().increment(10_000);
            assertEquals(1258, year.getValueFactory().getValue(),
                    "the years offered should run to the end of the era and stop");
        });
    }

    @Test
    @DisplayName("the cities offered are the ones that were standing that year")
    void theCitiesBelongToTheYear() throws Exception {
        onScreen(screen -> {
            screen.era().getSelectionModel().select(CityProfile.UMAYYAD);
            screen.year().getValueFactory().setValue(700);

            assertFalse(screen.city().getItems().contains("Cordoba"),
                    "Cordoba was offered as a birthplace in 700, before the conquest of 711");

            screen.year().getValueFactory().setValue(720);

            assertTrue(screen.city().getItems().contains("Cordoba"),
                    "Cordoba was not offered in 720, when it was there");
        });
    }

    @Test
    @DisplayName("what the player picked is what the life is given")
    void theChoiceIsHandedOn() throws Exception {
        onScreen(screen -> {
            screen.era().getSelectionModel().select(CityProfile.ABBASID);
            screen.year().getValueFactory().setValue(1240);
            screen.city().getSelectionModel().select("Baghdad");

            LifeStart start = screen.controller().chosenStart();

            assertEquals(CityProfile.ABBASID, start.era());
            assertEquals(1240, start.birthYear());
            assertEquals("Baghdad", start.city());
        });
    }

    /** The screen, loaded and laid out, on the FX thread. */
    private void onScreen(ScreenWork work) throws Exception {
        AtomicReference<Throwable> failure = new AtomicReference<>();
        CountDownLatch done = new CountDownLatch(1);

        Platform.runLater(() -> {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource(
                        "/com/example/al_mirath/fxml/name-screen.fxml"));

                Parent root = loader.load();
                new Scene(root, 1280, 720);
                root.applyCss();
                root.layout();

                NameController controller = loader.getController();

                @SuppressWarnings("unchecked")
                ComboBox<String> era = (ComboBox<String>) root.lookup("#eraChoice");

                @SuppressWarnings("unchecked")
                Spinner<Integer> year = (Spinner<Integer>) root.lookup("#yearChoice");

                @SuppressWarnings("unchecked")
                ComboBox<String> city = (ComboBox<String>) root.lookup("#cityChoice");

                work.run(new Screen(controller, era, year, city));

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
            fail("the naming screen failed", failure.get());
        }
    }

    private record Screen(NameController controller,
                          ComboBox<String> era,
                          Spinner<Integer> year,
                          ComboBox<String> city) {
    }

    private interface ScreenWork {
        void run(Screen screen) throws Exception;
    }
}
