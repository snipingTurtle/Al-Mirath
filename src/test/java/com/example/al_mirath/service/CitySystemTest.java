package com.example.al_mirath.service;

import com.example.al_mirath.model.Choice;
import com.example.al_mirath.model.City;
import com.example.al_mirath.model.CityCondition;
import com.example.al_mirath.model.CityProfile;
import com.example.al_mirath.model.GameEvent;
import com.example.al_mirath.model.PlayerCharacter;
import org.json.JSONObject;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

/**
 * Cities that go on living whether the player is looking at them or not.
 *
 * <p>"The world" used to be one abstraction. The point of this system is that
 * there are places: each with its own seven measures, each drifting on its own
 * schedule, each recognisably itself thirty years later. The things most
 * likely to go silently wrong are the simulation eating itself (coupled
 * measures spiralling until every city is identical and ruined) and the map
 * quietly failing to reach the player at all.
 */
class CitySystemTest {

    private PlayerCharacter player(String era, String origin) {
        return new PlayerCharacter(
                "Test Character", era, origin, "Stable Household", "Patient",
                20, 55, 45, 50, 45, 35, 50, 50, 30
        );
    }

    private List<String> cityNamesFor(String era) {
        List<String> names = new ArrayList<>();

        for (City city : CityRegistry.createFor(player(era, "Scholar's Child")).everyCity()) {
            names.add(city.getName());
        }

        return names;
    }

    // ---- the map is the era's map ----------------------------------------

    @Test
    @DisplayName("a run only contains the cities its era actually had")
    void theMapFitsTheEra() {
        assertFalse(
                cityNamesFor("Umayyad Era").contains("Istanbul"),
                "the Umayyads were given Istanbul, which is a thousand years early"
        );

        assertFalse(
                cityNamesFor("Umayyad Era").contains("Baghdad"),
                "Baghdad appears before the Abbasids founded it"
        );

        assertFalse(
                cityNamesFor("Mamluk Era").contains("Cordoba"),
                "Cordoba is still on the map long after Andalusia is gone"
        );

        assertTrue(
                cityNamesFor("Ottoman Era").contains("Istanbul"),
                "the Ottomans were not given Istanbul"
        );

        for (String era : List.of(
                "Umayyad Era", "Abbasid Era", "Mamluk Era", "Ottoman Era")) {

            assertTrue(
                    cityNamesFor(era).size() >= 4,
                    era + " has only " + cityNamesFor(era).size()
                            + " cities; there is nowhere to go"
            );
        }
    }

    @Test
    @DisplayName("a life starts in the seat its era is run from")
    void youBeginWhereTheEraIsGoverned() {
        assertEquals("Damascus",
                CityRegistry.createFor(player("Umayyad Era", "Bedouin Nomad"))
                        .currentCityName());

        assertEquals("Baghdad",
                CityRegistry.createFor(player("Abbasid Era", "Scholar's Child"))
                        .currentCityName());

        assertEquals("Cairo",
                CityRegistry.createFor(player("Mamluk Era", "Madrasa Student"))
                        .currentCityName());

        // Unless your own background names a city, in which case that is home.
        assertEquals("Cairo",
                CityRegistry.createFor(
                        player("Ottoman Era", "Cairo Merchant's Child")
                ).currentCityName(),
                "a character born to a Cairo family started somewhere else"
        );
    }

    @Test
    @DisplayName("two runs of the same era do not get the same Basra")
    void citiesAreGeneratedRatherThanFixed() {
        Set<Integer> prosperities = new HashSet<>();

        for (int run = 0; run < 40; run++) {
            for (City city : CityRegistry
                    .createFor(player("Abbasid Era", "Scholar's Child"))
                    .everyCity()) {

                if (city.getName().equals("Basra")) {
                    prosperities.add(city.getProsperity());
                }
            }
        }

        assertTrue(
                prosperities.size() >= 5,
                "forty runs produced " + prosperities.size()
                        + " different versions of Basra; the cities are a fixed "
                        + "table, not generated"
        );
    }

    // ---- the world turns -------------------------------------------------

    @Test
    @DisplayName("cities change while the player is nowhere near them")
    void theWorldTurnsWithoutYou() {
        CityRegistry map = CityRegistry.createFor(player("Abbasid Era", "Scholar's Child"));

        String here = map.currentCityName();
        Map<String, Integer> before = new java.util.HashMap<>();

        for (City city : map.everyCity()) {
            before.put(city.getName(), snapshotOf(city));
        }

        map.advanceYears(25);

        boolean somewhereElseMoved = false;

        for (City city : map.everyCity()) {
            if (city.getName().equals(here)) {
                continue;
            }

            if (snapshotOf(city) != before.get(city.getName())) {
                somewhereElseMoved = true;
            }
        }

        assertTrue(
                somewhereElseMoved,
                "twenty-five years passed and every city the player was not "
                        + "standing in was exactly as they left it"
        );
    }

    private int snapshotOf(City city) {
        return city.getProsperity() * 7 + city.getCrime() * 11 + city.getWar() * 13
                + city.getDisease() * 17 + city.getScholarship() * 19
                + city.getPopulation() * 23 + city.getTrade() * 29;
    }

    /**
     * The failure this guards against is not subtle in aggregate and invisible
     * in a single run: prosperity and trade each feeding on the other's decline
     * drove every city on the map to zero within a lifetime.
     */
    @Test
    @DisplayName("a city stays recognisably itself over a lifetime")
    void theSimulationDoesNotEatItself() {
        for (String name : List.of("Damascus", "Baghdad", "Jerusalem", "Basra")) {
            CityProfile profile = CityProfile.byName(name);

            long peaceProsperity = 0;
            int peaceYears = 0;
            int ruinedYears = 0;
            int years = 0;

            for (int run = 0; run < 120; run++) {
                City city = new City(profile, new Random(run), 12);

                for (int year = 0; year < 70; year++) {
                    city.advanceOneYear(new Random(run * 97L + year));
                    years++;

                    if (city.getProsperity() == 0) {
                        ruinedYears++;
                    }

                    if (city.getWar() == 0 && city.getDisease() == 0) {
                        peaceProsperity += city.getProsperity();
                        peaceYears++;
                    }
                }
            }

            assertTrue(
                    ruinedYears * 100.0 / years < 5.0,
                    name + " spent " + (ruinedYears * 100 / years)
                            + "% of its years at zero prosperity; the coupled "
                            + "measures are spiralling into each other"
            );

            double settled = peaceProsperity * 1.0 / Math.max(1, peaceYears);

            assertTrue(
                    Math.abs(settled - profile.prosperity()) < 20,
                    name + " settles at " + Math.round(settled)
                            + " prosperity in peacetime against a baseline of "
                            + profile.prosperity() + "; it no longer pulls back "
                            + "toward being itself"
            );
        }
    }

    @Test
    @DisplayName("wars and plagues arrive, and then they lift")
    void shocksAreShocksRatherThanWeather() {
        CityProfile profile = CityProfile.byName("Aleppo");

        int sieges = 0;
        int plagues = 0;
        int longestSiege = 0;
        int running = 0;

        for (int run = 0; run < 200; run++) {
            City city = new City(profile, new Random(run), 12);
            Random random = new Random(run * 131L);
            boolean wasBesieged = false;

            for (int year = 0; year < 70; year++) {
                city.advanceOneYear(random);

                boolean besieged = city.condition() == CityCondition.BESIEGED;

                if (besieged) {
                    running++;
                    longestSiege = Math.max(longestSiege, running);

                    if (!wasBesieged) {
                        sieges++;
                    }
                } else {
                    running = 0;
                }

                wasBesieged = besieged;

                if (city.condition() == CityCondition.PLAGUE_STRICKEN) {
                    plagues++;
                }
            }
        }

        assertTrue(sieges > 20, "in 14,000 city-years, war reached Aleppo " + sieges + " times");
        assertTrue(plagues > 20, "in 14,000 city-years, plague struck " + plagues + " times");

        assertTrue(
                longestSiege < 25,
                "a siege ran for " + longestSiege + " years; shocks are supposed "
                        + "to burn out, not become the permanent state of the city"
        );
    }

    /**
     * A good year in Jerusalem does not look like a good year in Baghdad. When
     * the thresholds were absolute, the poorer half of the map could never
     * have one and every city read the same.
     */
    @Test
    @DisplayName("what counts as notable depends on which city it is")
    void citiesAreJudgedAgainstThemselves() {
        Map<String, Map<CityCondition, Integer>> byCity = new java.util.HashMap<>();

        for (String name : List.of("Baghdad", "Basra", "Jerusalem")) {
            Map<CityCondition, Integer> tally = new EnumMap<>(CityCondition.class);

            for (int run = 0; run < 120; run++) {
                City city = new City(CityProfile.byName(name), new Random(run), 12);

                for (int year = 0; year < 70; year++) {
                    city.advanceOneYear(new Random(run * 53L + year));
                    tally.merge(city.condition(), 1, Integer::sum);
                }
            }

            byCity.put(name, tally);
        }

        int learnedInBaghdad = byCity.get("Baghdad").getOrDefault(CityCondition.LEARNED, 0);
        int learnedInBasra = byCity.get("Basra").getOrDefault(CityCondition.LEARNED, 0);

        assertTrue(
                learnedInBaghdad > learnedInBasra * 5,
                "Baghdad is a seat of learning no more often than Basra is; "
                        + "the cities do not have their own characters"
        );

        assertTrue(
                byCity.get("Jerusalem").getOrDefault(CityCondition.FLOURISHING, 0) > 0,
                "Jerusalem can never have a good year, because a good year is "
                        + "measured against a bar only rich cities can clear"
        );
    }

    // ---- and the map reaches the player ----------------------------------

    private CityRegistry mapWith(String cityName, String measure, int value) {
        CityRegistry map = CityRegistry.createFor(player("Abbasid Era", "Scholar's Child"));

        map.travelTo(cityName);

        City here = map.currentCity();
        here.change(measure, value - here.get(measure));

        return map;
    }

    @Test
    @DisplayName("the city you are standing in decides what happens to you")
    void aSiegeIsNotBackgroundScenery() {
        CityRegistry besieged = mapWith("Aleppo", "war", 80);

        assertEquals(CityCondition.BESIEGED, besieged.currentCondition());

        List<String> titles = new ArrayList<>();

        for (GameEvent event : CityEvents.create(besieged)) {
            titles.add(event.getTitle());
        }

        assertTrue(
                titles.contains("The Walls of Aleppo"),
                "a besieged city offered no siege event, only " + titles
        );

        CityRegistry calm = mapWith("Aleppo", "war", 0);

        for (GameEvent event : CityEvents.create(calm)) {
            assertFalse(
                    event.getTitle().startsWith("The Walls of"),
                    "a city at peace is still offering siege events"
            );
        }
    }

    @Test
    @DisplayName("every event a city produces is well formed and says where it is")
    void cityEventsAreWellFormed() {
        Set<String> seenTitles = new HashSet<>();

        for (Map.Entry<String, Integer> shock : Map.of(
                "war", 80, "disease", 70, "crime", 95,
                "scholarship", 90, "prosperity", 95).entrySet()) {

            CityRegistry map = mapWith("Basra", shock.getKey(), shock.getValue());

            for (GameEvent event : CityEvents.create(map)) {
                assertFalse(event.getTitle().isBlank());
                assertFalse(event.getDescription().isBlank());

                assertTrue(
                        event.getChoices().size() >= 2,
                        event.getTitle() + " offers fewer than two choices"
                );

                assertTrue(
                        event.getDescription().contains("Basra")
                                || event.getTitle().contains("Basra")
                                || event.getTitle().startsWith("The Road Out"),
                        event.getTitle() + " never says where it is happening"
                );

                for (Choice choice : event.getChoices()) {
                    assertFalse(
                            choice.getText().isBlank(),
                            event.getTitle() + " has a blank choice"
                    );
                }

                seenTitles.add(event.getTitle());
            }
        }

        assertTrue(
                seenTitles.size() >= 8,
                "only " + seenTitles.size() + " distinct city events are reachable"
        );
    }

    @Test
    @DisplayName("the road out leads somewhere real, and taking it moves you")
    void leavingActuallyLeaves() {
        CityRegistry map = CityRegistry.createFor(player("Ottoman Era", "Palace Clerk's Child"));

        String home = map.currentCityName();
        GameEvent road = null;

        for (GameEvent event : CityEvents.create(map)) {
            if (event.getTitle().startsWith("The Road Out of")) {
                road = event;
            }
        }

        assertNotNull(road, "there was never any way to leave " + home);

        List<String> travelFlags = new ArrayList<>();

        for (Choice choice : road.getChoices()) {
            for (String flag : choice.getSuccessFlags()) {
                if (flag.startsWith("travel_to_")) {
                    travelFlags.add(flag);
                }
            }
        }

        assertEquals(1, travelFlags.size(), "the road out went nowhere in particular");

        assertTrue(
                map.applyStoryFlag(travelFlags.get(0)),
                "taking the road out did not move the player"
        );

        assertFalse(
                map.currentCityName().equals(home),
                "the player took the road out of " + home + " and arrived in " + home
        );
    }

    @Test
    @DisplayName("a choice made in a city changes that city")
    void thePlayerLeavesAMarkOnThePlace() {
        CityRegistry map = mapWith("Basra", "prosperity", 30);

        int before = map.currentCity().getProsperity();
        assertTrue(map.applyStoryFlag("city_granary_opened"));

        assertTrue(
                map.currentCity().getProsperity() > before,
                "forcing the granaries open left the city exactly as hungry"
        );

        CityRegistry lawful = mapWith("Basra", "crime", 40);

        int crimeBefore = lawful.currentCity().getCrime();
        assertTrue(lawful.applyStoryFlag("city_crime_joined"));

        assertTrue(
                lawful.currentCity().getCrime() > crimeBefore,
                "taking the protection money left the quarter no worse"
        );

        // A flag that has nothing to do with the map must not move it.
        CityRegistry untouched = mapWith("Basra", "prosperity", 50);
        int quiet = snapshotOf(untouched.currentCity());

        assertFalse(untouched.applyStoryFlag("stood_by_father"));
        assertEquals(quiet, snapshotOf(untouched.currentCity()));
    }

    // ---- persistence -----------------------------------------------------

    @Test
    @DisplayName("the map survives a save")
    void everyCitySurvivesTheRoundTrip() {
        CityRegistry map = CityRegistry.createFor(player("Ottoman Era", "Royal Prince"));
        map.advanceYears(30);
        map.travelTo("Aleppo");

        CityRegistry restored = CityRegistry.fromJson(new JSONObject(map.toJson().toString()));

        assertEquals(map.currentCityName(), restored.currentCityName());
        assertEquals(map.everyCity().size(), restored.everyCity().size());

        for (City before : map.everyCity()) {
            City after = null;

            for (City candidate : restored.everyCity()) {
                if (candidate.getName().equals(before.getName())) {
                    after = candidate;
                }
            }

            assertNotNull(after, before.getName() + " was lost in the save");

            assertEquals(
                    snapshotOf(before), snapshotOf(after),
                    before.getName() + " came back from the save as a different city"
            );
        }
    }

    /**
     * The baseline a city pulls back toward lives in its profile, not in the
     * save. A restored city that has forgotten what it normally is would treat
     * a plague year as its permanent character.
     */
    @Test
    @DisplayName("a city restored from a save still knows what it normally is")
    void theBaselineSurvivesTooEvenThoughItIsNotWrittenDown() {
        CityRegistry map = CityRegistry.createFor(player("Abbasid Era", "Scholar's Child"));
        map.travelTo("Baghdad");

        // Wreck it, save it, and let the restored copy recover.
        map.currentCity().change("prosperity", -60);

        CityRegistry restored = CityRegistry.fromJson(new JSONObject(map.toJson().toString()));
        int wrecked = restored.currentCity().getProsperity();

        restored.advanceYears(40);

        assertTrue(
                restored.currentCity().getProsperity() > wrecked + 10,
                "a restored Baghdad never recovered from the state it was saved "
                        + "in; it has forgotten what it normally is"
        );
    }

    // ---- end to end ------------------------------------------------------

    @Test
    @DisplayName("every scene the player is shown happens somewhere")
    void noEventHappensNowhere() {
        Set<String> poolTitles = new HashSet<>();

        for (GameEvent event : EventLibrary.createEventPool()) {
            poolTitles.add(event.getTitle());
        }

        Random random = new Random(4);
        int situated = 0;

        for (int run = 0; run < 25; run++) {
            GameEngine engine = new GameEngine();

            for (int i = 0; i < 40 && engine.getCurrentEvent() != null; i++) {
                GameEvent shown = engine.getCurrentEvent();

                if (poolTitles.contains(shown.getTitle())) {
                    String city = engine.getCurrentCityName();

                    assertTrue(
                            shown.getDescription().startsWith(city + ","),
                            "'" + shown.getTitle() + "' was put in front of the "
                                    + "player without saying they were in " + city
                    );

                    situated++;
                }

                List<Choice> available = new ArrayList<>();

                for (Choice choice : shown.getChoices()) {
                    if (engine.canChoose(choice)) {
                        available.add(choice);
                    }
                }

                if (available.isEmpty()) {
                    break;
                }

                engine.applyChoice(available.get(random.nextInt(available.size())));
            }
        }

        assertTrue(
                situated > 50,
                "only " + situated + " placeless events were ever situated; the "
                        + "map is not reaching the scenes the player reads"
        );
    }

    /**
     * The panel used to end with a single "somewhere else" — whichever city
     * happened to be doing best that year. It was recomputed as the world
     * drifted and carried no label, so the name under the player's own city
     * changed almost every year and read as though they had been moved: 93% of
     * lives showed two or more cities there.
     */
    @Test
    @DisplayName("the panel never implies the player has moved when they have not")
    void theCityPanelDoesNotChurn() {
        Random random = new Random(2);
        int churned = 0;
        int lives = 40;

        for (int run = 0; run < lives; run++) {
            GameEngine engine = new GameEngine();

            String home = engine.getCurrentCityName();
            Set<String> elsewhereShown = new HashSet<>();

            for (int i = 0; i < 30 && engine.getCurrentEvent() != null; i++) {
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

                if (!engine.getCurrentCityName().equals(home)) {
                    break;
                }

                elsewhereShown.add(engine.getCities().elsewhereSummary());

                assertFalse(
                        engine.getCities().elsewhereSummary().contains(home),
                        "the city the player is standing in was also listed as "
                                + "somewhere else"
                    );
            }

            if (elsewhereShown.size() > 1) {
                churned++;
            }
        }

        // The listing may still change as cities change condition, but the set
        // of cities in it must not: a player who has not moved must never see
        // a different place named under their own.
        for (int run = 0; run < 5; run++) {
            CityRegistry map = CityRegistry.createFor(player("Abbasid Era", "Scholar's Child"));

            List<String> first = namesIn(map.elsewhereSummary());

            for (int year = 0; year < 12; year++) {
                map.advanceYears(3);

                assertEquals(
                        first,
                        namesIn(map.elsewhereSummary()),
                        "the cities listed beside the player's own changed "
                                + "while they stayed put"
                );
            }
        }
    }

    private List<String> namesIn(String summary) {
        List<String> names = new ArrayList<>();

        for (String block : summary.split("\n\n")) {
            String[] lines = block.split("\n");

            if (lines.length > 0 && !lines[0].isBlank()) {
                names.add(lines[0]);
            }
        }

        return names;
    }

    @Test
    @DisplayName("the engine runs the world, and keeps it across a save")
    void theEngineOwnsTheMap() {
        Random random = new Random(6);
        GameEngine engine = new GameEngine();

        assertFalse(engine.getCurrentCityName().isBlank(), "the player began nowhere");
        assertFalse(engine.getCitySummary().isBlank(), "the panel had nothing to show");

        for (int i = 0; i < 30 && engine.getCurrentEvent() != null; i++) {
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

        GameEngine loaded = GameEngine.fromSaveJson(engine.toSaveJson());

        assertEquals(
                engine.getCurrentCityName(),
                loaded.getCurrentCityName(),
                "the player was somewhere else after loading"
        );

        assertEquals(
                engine.getCitySummary(),
                loaded.getCitySummary(),
                "the world was rebuilt from scratch rather than restored"
        );
    }

    @Test
    @DisplayName("a rewind puts the city back the way it was")
    void rewindingUndoesWhatYouDidToThePlace() {
        Random random = new Random(8);

        for (int run = 0; run < 40; run++) {
            GameEngine engine = new GameEngine();

            for (int i = 0; i < 25 && engine.getCurrentEvent() != null; i++) {
                String before = engine.getCitySummary();

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

                if (!engine.canRewind() || before.equals(engine.getCitySummary())) {
                    continue;
                }

                engine.rewindToLastSnapshot();

                assertEquals(
                        before,
                        engine.getCitySummary(),
                        "a rewind left the city in the state the undone choice "
                                + "had put it in"
                );

                return;
            }
        }

        fail("in forty lives, no choice ever changed the city enough to rewind");
    }
}
