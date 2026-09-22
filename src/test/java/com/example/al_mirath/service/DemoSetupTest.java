package com.example.al_mirath.service;

import com.example.al_mirath.model.CityProfile;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * The demonstration flags, which exist so that a particular year can be shown
 * to somebody on purpose.
 *
 * <p>A life is pinned to a rolled year inside a rolled era, which is right for
 * playing and hopeless for showing: about one Abbasid life in fifty is in
 * Baghdad in time for 1258. These properties settle the roll. They are not in
 * the interface, a player who never sets them cannot tell they are there, and
 * a value that makes no sense is ignored rather than obeyed.
 */
class DemoSetupTest {

    @AfterEach
    void putTheWorldBack() {
        System.clearProperty("almirath.era");
        System.clearProperty("almirath.birthYear");
        System.clearProperty("almirath.city");
    }

    @Test
    @DisplayName("a life can be told where and when to begin")
    void aLifeCanBeStartedOnPurpose() {
        System.setProperty("almirath.era", CityProfile.ABBASID);
        System.setProperty("almirath.birthYear", "1240");
        System.setProperty("almirath.city", "Baghdad");

        GameEngine engine = new GameEngine("Yusuf ibn Ishaq");

        assertEquals(CityProfile.ABBASID, engine.getPlayer().getEra());
        assertEquals(1240, engine.getBirthYear());
        assertEquals("Baghdad", engine.getCurrentCityName());
        assertEquals("1240 CE", engine.getYearText());
    }

    @Test
    @DisplayName("the flags put the Mongols where a demo can reach them")
    void theDemoActuallyReaches1258() {
        System.setProperty("almirath.era", CityProfile.ABBASID);
        System.setProperty("almirath.birthYear", "1240");
        System.setProperty("almirath.city", "Baghdad");

        GameEngine engine = new GameEngine("Yusuf ibn Ishaq");

        // Eighteen presses of Age Up, which is what the demo will be doing.
        for (int year = 0; year < 20 && engine.getPlayer().isAlive(); year++) {
            engine.ageOneYear();

            if (engine.getHistoryMet().contains("baghdad_falls")) {
                break;
            }
        }

        assertTrue(engine.getHistoryMet().contains("mongols_at_the_door"),
                "the 1257 warning never arrived, so the demo has nothing to answer");

        assertTrue(engine.getHistoryMet().contains("baghdad_falls")
                        || !engine.getPlayer().isAlive(),
                "eighteen years in Baghdad from 1240 did not reach 1258");
    }

    @Test
    @DisplayName("a year outside the era is ignored, not obeyed")
    void nonsenseIsIgnored() {
        System.setProperty("almirath.era", CityProfile.ABBASID);
        System.setProperty("almirath.birthYear", "1500");

        GameEngine engine = new GameEngine("Yusuf ibn Ishaq");

        assertNotEquals(1500, engine.getBirthYear(),
                "an Abbasid life was started in 1500, which the Abbasids did not see");

        assertTrue(engine.getBirthYear() >= Eras.startOf(CityProfile.ABBASID)
                        && engine.getBirthYear() <= Eras.endOf(CityProfile.ABBASID),
                "the rolled year fell outside the era after a bad one was refused");
    }

    @Test
    @DisplayName("a city that is not there yet is ignored")
    void aCityThatIsNotThereIsIgnored() {
        System.setProperty("almirath.era", CityProfile.UMAYYAD);
        System.setProperty("almirath.city", "Istanbul");

        GameEngine engine = new GameEngine("Yusuf ibn Ishaq");

        assertNotEquals("Istanbul", engine.getCurrentCityName(),
                "a Umayyad life began in Istanbul, which is seven centuries early");
    }

    @Test
    @DisplayName("with nothing set, nothing is rigged")
    void withoutTheFlagsNothingChanges() {
        GameEngine engine = new GameEngine("Yusuf ibn Ishaq");

        assertTrue(Eras.isKnown(engine.getPlayer().getEra()));
        assertFalse(engine.getCurrentCityName().isBlank());
    }
}
