package com.example.al_mirath.service;

import com.example.al_mirath.model.CityProfile;
import com.example.al_mirath.model.LifeStart;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Being born where and when you asked.
 *
 * <p>The station, the household and the temperament stay rolled. When and
 * where do not, because a life is pinned to a real year now and choosing to
 * be in Baghdad for 1258 is a reason to play. What the game will not do is
 * agree to a life it cannot honestly give: an Abbasid 1500, or an Umayyad
 * childhood in an Istanbul that is seven centuries away.
 */
class LifeStartTest {

    @Test
    @DisplayName("a life begins where and when the player asked")
    void theChoiceIsKept() {
        GameEngine engine = new GameEngine(
                "Yusuf ibn Ishaq",
                new LifeStart(CityProfile.ABBASID, 1240, "Baghdad"));

        assertEquals(CityProfile.ABBASID, engine.getPlayer().getEra());
        assertEquals(1240, engine.getBirthYear());
        assertEquals("Baghdad", engine.getCurrentCityName());
        assertEquals("1240 CE", engine.getYearText());
    }

    @Test
    @DisplayName("choosing the year is choosing what you will live through")
    void theChosenYearReachesTheHistoryInIt() {
        GameEngine engine = new GameEngine(
                "Yusuf ibn Ishaq",
                new LifeStart(CityProfile.ABBASID, 1240, "Baghdad"));

        for (int year = 0; year < 20 && engine.getPlayer().isAlive(); year++) {
            engine.ageOneYear();

            if (engine.getHistoryMet().contains("baghdad_falls")) {
                break;
            }
        }

        assertTrue(engine.getHistoryMet().contains("mongols_at_the_door"),
                "a life begun in Baghdad in 1240 never heard the Mongols coming");
    }

    @Test
    @DisplayName("a year the era never saw is refused, not obeyed")
    void anImpossibleYearIsRefused() {
        GameEngine engine = new GameEngine(
                "Yusuf ibn Ishaq",
                new LifeStart(CityProfile.ABBASID, 1500, null));

        assertNotEquals(1500, engine.getBirthYear(),
                "an Abbasid life began in 1500, which the Abbasids did not see");

        assertTrue(Eras.isYearIn(CityProfile.ABBASID, engine.getBirthYear()),
                "the year fell back to something outside the era anyway");
    }

    @Test
    @DisplayName("a city that was not there yet is refused")
    void anImpossibleCityIsRefused() {
        GameEngine engine = new GameEngine(
                "Yusuf ibn Ishaq",
                new LifeStart(CityProfile.UMAYYAD, 700, "Cordoba"));

        // Muslim Cordoba belongs to the conquest of 711 onward, so in 700 the
        // player is put in the seat of the era instead.
        assertNotEquals("Cordoba", engine.getCurrentCityName(),
                "a life began in Cordoba in 700");

        assertEquals("Damascus", engine.getCurrentCityName());
    }

    @Test
    @DisplayName("asking for nothing begins a life exactly as it always did")
    void rollingIsStillTheDefault() {
        GameEngine engine = new GameEngine("Yusuf ibn Ishaq", LifeStart.rolled());

        assertTrue(Eras.isKnown(engine.getPlayer().getEra()));
        assertTrue(Eras.isYearIn(engine.getPlayer().getEra(), engine.getBirthYear()));
        assertTrue(Eras.cityExistsIn(engine.getCurrentCityName(), engine.getBirthYear()));
    }

    @Test
    @DisplayName("an era on its own still picks a year inside that era")
    void anEraAloneIsEnough() {
        GameEngine engine = new GameEngine(
                "Yusuf ibn Ishaq",
                new LifeStart(CityProfile.MAMLUK, 0, null));

        assertEquals(CityProfile.MAMLUK, engine.getPlayer().getEra());
        assertTrue(Eras.isYearIn(CityProfile.MAMLUK, engine.getBirthYear()),
                "a Mamluk life was born in " + engine.getBirthYear());
    }
}
