package com.example.al_mirath.service;

import com.example.al_mirath.model.Choice;
import com.example.al_mirath.model.CityProfile;
import com.example.al_mirath.model.GameEvent;
import com.example.al_mirath.model.HistoricalEvent;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

/**
 * The history has to be history.
 *
 * <p>The point of dating the timeline from published sources is lost the
 * moment an entry drifts: a year outside its own era, a city that did not
 * exist yet, an entry nobody can ever meet because it falls before the first
 * year a life can begin. None of that is visible by reading the file, and all
 * of it is visible from here.
 */
class HistoryTest {

    private static final Set<String> STATS = Set.of(
            "health", "wealth", "education", "reputation",
            "politicalPower", "morality", "familyLoyalty", "stress");

    private static final Set<String> FACTIONS = Set.of(
            "court", "nobles", "military", "scholars",
            "merchants", "commonPeople", "familyCouncil", "shadowNetwork");

    @Test
    @DisplayName("every dated event falls inside the era it is filed under")
    void everyEventSitsInsideItsEra() {
        List<String> wrong = new ArrayList<>();

        for (HistoricalEvent event : HistoricalTimeline.all()) {
            if (!Eras.isKnown(event.era())) {
                wrong.add(event.id() + " belongs to no known era: " + event.era());
                continue;
            }

            int first = Eras.startOf(event.era());
            int last = Eras.endOf(event.era());

            if (event.year() < first || event.year() > last) {
                wrong.add(event.id() + " is dated " + event.year()
                        + ", outside " + event.era() + " (" + first + "-" + last + ")");
            }
        }

        if (!wrong.isEmpty()) {
            fail("the timeline has entries outside their own era:\n  "
                    + String.join("\n  ", wrong));
        }
    }

    @Test
    @DisplayName("no event happens in a city that is not there yet")
    void everyCityExistsWhenItIsNamed() {
        List<String> wrong = new ArrayList<>();

        for (HistoricalEvent event : HistoricalTimeline.all()) {
            for (String city : event.cities()) {
                CityProfile profile = CityProfile.byName(city);

                if (profile == null) {
                    wrong.add(event.id() + " names a city the game has never heard of: " + city);
                    continue;
                }

                if (!profile.eras().contains(event.era())) {
                    wrong.add(event.id() + " puts " + city + " in the " + event.era()
                            + ", where it does not appear");
                }

                if (!Eras.cityExistsIn(city, event.year())) {
                    wrong.add(event.id() + " happens in " + city + " in " + event.year()
                            + ", before there was a " + city + " to happen in");
                }
            }
        }

        if (!wrong.isEmpty()) {
            fail("the timeline puts events in cities that were not there:\n  "
                    + String.join("\n  ", wrong));
        }
    }

    @Test
    @DisplayName("every entry carries a source, an id of its own, and its year in order")
    void everyEntryIsAccountedFor() {
        Set<String> ids = new HashSet<>();
        List<String> wrong = new ArrayList<>();
        int previousYear = Integer.MIN_VALUE;

        for (HistoricalEvent event : HistoricalTimeline.all()) {
            if (!ids.add(event.id())) {
                wrong.add("two entries share the id " + event.id());
            }

            if (event.source() == null || event.source().isBlank()) {
                wrong.add(event.id() + " cites no source");
            }

            if (event.account() == null || event.account().length() < 40) {
                wrong.add(event.id() + " says almost nothing about what happened");
            }

            if (event.year() < previousYear) {
                wrong.add(event.id() + " is out of order at " + event.year());
            }

            previousYear = event.year();
        }

        if (!wrong.isEmpty()) {
            fail("the record is not in order:\n  " + String.join("\n  ", wrong));
        }
    }

    @Test
    @DisplayName("every effect names a stat or a faction that exists")
    void everyEffectLandsSomewhere() {
        List<String> wrong = new ArrayList<>();

        for (HistoricalEvent event : HistoricalTimeline.all()) {
            for (String stat : event.statEffects().keySet()) {
                if (!STATS.contains(stat)) {
                    wrong.add(event.id() + " moves a stat that does not exist: " + stat);
                }
            }

            for (String faction : event.factionEffects().keySet()) {
                if (!FACTIONS.contains(faction)) {
                    wrong.add(event.id() + " moves a faction that does not exist: " + faction);
                }
            }
        }

        if (!wrong.isEmpty()) {
            fail("the timeline moves things that are not there:\n  "
                    + String.join("\n  ", wrong));
        }
    }

    @Test
    @DisplayName("a year that can kill you can also be answered")
    void everyKillingYearCanBeAnswered() {
        List<String> wrong = new ArrayList<>();

        // Every flag any choice in the timeline can set. A way out that
        // nothing can ever grant is not a way out.
        Set<String> reachableFlags = new HashSet<>();

        for (HistoricalEvent event : HistoricalTimeline.all()) {
            for (Choice choice : event.choices()) {
                reachableFlags.addAll(choice.getSuccessFlags());
                reachableFlags.addAll(choice.getFailureFlags());
            }
        }

        for (HistoricalEvent event : HistoricalTimeline.all()) {
            if (!event.canKill()) {
                continue;
            }

            if (event.choices().size() < 2) {
                wrong.add(event.id() + " can kill the player and offers them nothing to do");
            }

            for (String spare : event.sparedBy()) {
                if (!reachableFlags.contains(spare)) {
                    wrong.add(event.id() + " can be survived by \"" + spare
                            + "\", which no choice anywhere can set");
                }
            }
        }

        if (!wrong.isEmpty()) {
            fail("the dangerous years are not playable:\n  "
                    + String.join("\n  ", wrong));
        }
    }

    @Test
    @DisplayName("every era has history a life can actually reach")
    void everyEraHasReachableHistory() {
        for (String era : Eras.all()) {
            List<HistoricalEvent> events = HistoricalTimeline.forEra(era);

            assertFalse(events.isEmpty(), era + " has no history in it at all");

            long reachable = events.stream()
                    .filter(event -> event.year() >= Eras.startOf(era))
                    .count();

            assertTrue(reachable >= 5,
                    era + " has only " + reachable + " events a life could live to see");
        }
    }

    @Test
    @DisplayName("a life is dated, and ages alongside the calendar")
    void aLifeKeepsTheCalendar() {
        GameEngine engine = new GameEngine("Zaynab bint Yusuf");

        int birthYear = engine.getBirthYear();
        String era = engine.getPlayer().getEra();

        assertTrue(birthYear >= Eras.startOf(era) && birthYear <= Eras.endOf(era),
                "a " + era + " life was born in " + birthYear);

        assertEquals(birthYear + engine.getPlayer().getAge(), engine.getCurrentYear());

        int ageBefore = engine.getPlayer().getAge();
        engine.ageOneYear();

        assertEquals(birthYear + engine.getPlayer().getAge(), engine.getCurrentYear(),
                "the year and the age came apart");
        assertTrue(engine.getPlayer().getAge() > ageBefore || !engine.getPlayer().isAlive());
    }

    @Test
    @DisplayName("Baghdad in 1258 happens to whoever is standing in it")
    void theSackOfBaghdadFindsThePlayer() throws Exception {
        List<HistoricalEvent> due =
                HistoricalTimeline.eventsIn(1258, CityProfile.ABBASID, "Baghdad");

        assertEquals(1, due.size(), "1258 in Baghdad should be one thing and it should be that thing");
        assertEquals("baghdad_falls", due.get(0).id());

        // And nobody in Cairo that year is in it.
        assertTrue(HistoricalTimeline.eventsIn(1258, CityProfile.ABBASID, "Cairo").isEmpty(),
                "the sack of Baghdad reached Cairo, which is a long way to be sacked from");

        GameEngine engine = anAbbasidLifeIn("Baghdad", 1255);

        // Age up to the year itself. The scene is raised the moment it lands.
        for (int year = 0; year < 6 && engine.getPlayer().isAlive(); year++) {
            engine.ageOneYear();

            if (engine.getHistoryMet().contains("baghdad_falls")) {
                break;
            }
        }

        assertTrue(engine.getHistoryMet().contains("baghdad_falls"),
                "a life in Baghdad reached 1258 without the Mongols arriving");

        assertTrue(engine.getWorldFlags().contains("history_baghdad_sacked")
                        || !engine.getPlayer().isAlive(),
                "the sack left no mark on the world");
    }

    @Test
    @DisplayName("the year the world ends is offered as a scene, with real choices")
    void aCatastropheIsPlayed() throws Exception {
        GameEngine engine = anAbbasidLifeIn("Baghdad", 1256);

        GameEvent scene = null;

        for (int year = 0; year < 5 && engine.getPlayer().isAlive(); year++) {
            engine.ageOneYear();

            GameEvent current = engine.getCurrentEvent();

            if (current != null && current.getTitle().contains("Baghdad Falls")) {
                scene = current;
                break;
            }
        }

        assertNotNull(scene, "1258 went by in Baghdad without asking the player anything");
        assertTrue(scene.getChoices().size() >= 3,
                "the worst year in the city's history offered "
                        + scene.getChoices().size() + " ways to meet it");
        assertTrue(scene.getTitle().contains("1258"), "the scene does not say when it is");
    }

    /**
     * A life dated to a chosen year, standing where we need it to stand.
     * The engine picks both for itself, so both are set here directly.
     */
    private GameEngine anAbbasidLifeIn(String city, int birthYear) throws Exception {
        for (int attempt = 0; attempt < 200; attempt++) {
            GameEngine engine = new GameEngine("Zaynab bint Yusuf");

            if (!engine.getPlayer().getEra().equals(CityProfile.ABBASID)) {
                continue;
            }

            Field year = GameEngine.class.getDeclaredField("birthYear");
            year.setAccessible(true);
            year.setInt(engine, birthYear);

            engine.travelTo(city);

            if (engine.getCurrentCityName().equals(city)) {
                return engine;
            }
        }

        return fail("in two hundred lives, none could be put in " + city);
    }
}
