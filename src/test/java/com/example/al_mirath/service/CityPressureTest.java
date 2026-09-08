package com.example.al_mirath.service;

import com.example.al_mirath.model.Choice;
import com.example.al_mirath.model.City;
import com.example.al_mirath.model.CityProfile;
import com.example.al_mirath.model.DeathCause;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

/**
 * The city doing something rather than describing something.
 *
 * <p>Seven bars moved every year in the drawer and changed nothing: a player
 * could watch Baghdad burn from a life that was completely unaffected by it.
 * The same numbers now bend the odds on a stat check, bend what a choice costs
 * or earns, and can kill — in that order of force, because a city is supposed
 * to colour a run rather than decide it.
 */
class CityPressureTest {

    /**
     * A city built to order, so a test can say exactly what it is like.
     *
     * <p>Ordinary is the middle of every measure a city always has some of,
     * and none at all of the two it does not: a city is not normally at war
     * and is not normally sick, so fifty of either would be a catastrophe
     * rather than a baseline.
     */
    private City cityWhere(String measure, int value) {
        City city = new City(CityProfile.roster().get(0), new Random(1), 0);

        for (String middling : List.of(
                "prosperity", "crime", "scholarship", "population", "trade")) {

            city.change(middling, 50 - city.get(middling));
        }

        for (String none : List.of("war", "disease")) {
            city.change(none, -city.get(none));
        }

        city.change(measure, value - city.get(measure));

        return city;
    }

    private City anOrdinaryCity() {
        return cityWhere("prosperity", 50);
    }

    // ---- the odds --------------------------------------------------------

    @Test
    @DisplayName("an ordinary city neither helps nor hinders")
    void theMiddleOfTheRoadCostsNothing() {
        City ordinary = anOrdinaryCity();

        for (String stat : List.of(
                "education", "wealth", "politicalPower", "health", "reputation")) {

            assertEquals(
                    0, CityPressure.shiftFor(ordinary, stat),
                    "an unremarkable city moved the odds on a " + stat + " check"
            );

            assertEquals(
                    10, CityPressure.bend(ordinary, stat, 10),
                    "an unremarkable city changed what a " + stat + " choice did"
            );
        }
    }

    @Test
    @DisplayName("learning is easier where there is somebody to learn from")
    void scholarshipMovesTheOddsOnLearning() {
        int learned = CityPressure.shiftFor(cityWhere("scholarship", 90), "education");
        int ignorant = CityPressure.shiftFor(cityWhere("scholarship", 10), "education");

        assertTrue(learned > 0, "a city full of scholars did nothing for study");
        assertTrue(ignorant < 0, "a city with nobody to learn from did nothing to study");
        assertTrue(learned > ignorant);
    }

    @Test
    @DisplayName("nothing political moves while the walls are being hit")
    void aSiegeClosesTheCourt() {
        assertTrue(
                CityPressure.shiftFor(cityWhere("war", 90), "politicalPower") < 0,
                "a city under siege was as good a place to play politics as any"
        );

        assertTrue(
                CityPressure.shiftFor(cityWhere("crime", 90), "wealth") < 0,
                "a city full of thieves was as good a place to make a deal as any"
        );

        assertTrue(
                CityPressure.shiftFor(cityWhere("trade", 90), "wealth") > 0,
                "a trading city did nothing for a deal"
        );

        assertTrue(
                CityPressure.shiftFor(cityWhere("disease", 90), "health") < 0,
                "a plague city was as safe to gamble your body in as any other"
        );
    }

    /**
     * The city colours the roll; it does not decide it. Without a bound, a bad
     * enough city would make a strong character's certainty into a coin flip.
     */
    @Test
    @DisplayName("no city is worth more than the person standing in it")
    void theShiftIsBounded() {
        for (String measure : List.of(
                "prosperity", "crime", "war", "disease", "scholarship", "trade")) {

            for (int value : new int[]{0, 100}) {
                City extreme = cityWhere(measure, value);

                for (String stat : List.of(
                        "education", "wealth", "politicalPower",
                        "health", "reputation", "morality", "stress")) {

                    int shift = CityPressure.shiftFor(extreme, stat);

                    assertTrue(
                            shift >= -20 && shift <= 15,
                            measure + " at " + value + " moved a " + stat
                                    + " check by " + shift
                    );
                }
            }
        }
    }

    // ---- the outcome -----------------------------------------------------

    @Test
    @DisplayName("money goes further in a rich city and disappears faster in a lawless one")
    void wealthIsWorthWhatThePlaceIsWorth() {
        City rich = cityWhere("prosperity", 95);
        City lawless = cityWhere("crime", 95);

        assertTrue(
                CityPressure.bend(rich, "wealth", 10) > 10,
                "a fortune made in a rich city came to no more than anywhere else"
        );

        assertTrue(
                CityPressure.bend(lawless, "wealth", -10) < -10,
                "a loss in a city of thieves cost no more than anywhere else"
        );
    }

    @Test
    @DisplayName("a wound taken during a plague is worse than the same wound elsewhere")
    void sicknessMakesEverythingWorse() {
        City sick = cityWhere("disease", 90);

        assertTrue(
                CityPressure.bend(sick, "health", -10) < -10,
                "an injury in a plague city was no worse than an injury anywhere"
        );

        assertTrue(
                CityPressure.bend(sick, "health", 10) < 10,
                "recovering was as easy in a plague city as anywhere"
        );
    }

    /**
     * A city bends what happens to you; it never decides that nothing did. A
     * scaled-down effect that rounds to zero is a choice with no consequence,
     * which is worse than either extreme.
     */
    @Test
    @DisplayName("no city can round a consequence away to nothing")
    void aChangeNeverDisappears() {
        Map<String, City> extremes = new LinkedHashMap<>();

        for (String measure : List.of(
                "prosperity", "crime", "war", "disease", "scholarship", "trade")) {

            for (int value : new int[]{0, 100}) {
                extremes.put(measure + " at " + value, cityWhere(measure, value));
            }
        }

        // The worst place the map can produce: besieged, sick and lawless at
        // once. One bad measure at a time never reaches the floor the scaling
        // is clamped to, so testing them one at a time left that floor — and
        // the thing it protects — unexercised.
        City theWorstOfIt = cityWhere("war", 100);
        theWorstOfIt.change("disease", 100 - theWorstOfIt.get("disease"));
        theWorstOfIt.change("crime", 100 - theWorstOfIt.get("crime"));

        extremes.put("besieged, sick and lawless at once", theWorstOfIt);

        for (Map.Entry<String, City> where : extremes.entrySet()) {
            String describe = where.getKey();
            City extreme = where.getValue();

            for (String stat : List.of("wealth", "health", "education", "stress")) {
                for (int delta : new int[]{1, -1, 2, -2, 25, -25}) {
                    int bent = CityPressure.bend(extreme, stat, delta);

                    assertFalse(
                            bent == 0,
                            describe + " turned a " + delta + " change in "
                                    + stat + " into nothing at all"
                    );

                    assertTrue(
                            Math.abs(bent) >= Math.abs(delta) / 2,
                            describe + " cut a " + delta + " change in " + stat
                                    + " down to " + bent + ", more than halving it"
                    );

                    assertTrue(
                            (bent > 0) == (delta > 0),
                            describe + " turned a " + delta + " change in "
                                    + stat + " into " + bent
                    );
                }
            }
        }
    }

    // ---- surviving it ----------------------------------------------------

    @Test
    @DisplayName("an ordinary city is not trying to kill you")
    void onlyPlagueAndSiegeKill() {
        assertEquals(0, CityPressure.deathRisk(anOrdinaryCity()));
        assertNull(CityPressure.deathCause(anOrdinaryCity()));

        assertTrue(
                CityPressure.deathRisk(cityWhere("disease", 90)) > 0,
                "a plague city added no danger to being alive in it"
        );

        assertEquals(
                DeathCause.PLAGUE, CityPressure.deathCause(cityWhere("disease", 90))
        );

        assertTrue(
                CityPressure.deathRisk(cityWhere("war", 90)) > 0,
                "a city being taken added no danger to being in it"
        );

        assertEquals(
                DeathCause.SIEGE, CityPressure.deathCause(cityWhere("war", 90))
        );
    }

    // ---- saying so --------------------------------------------------------

    @Test
    @DisplayName("the drawer says what the bars are doing, and only when they do it")
    void theCitySaysWhatItIsDoing() {
        assertTrue(
                CityPressure.felt(anOrdinaryCity()).contains("ordinary"),
                "an unremarkable city claimed to be doing something"
        );

        String plague = CityPressure.felt(cityWhere("disease", 90));

        assertTrue(
                plague.contains("sickness"),
                "a plague city said nothing about the plague: " + plague
            );

        String learned = CityPressure.felt(cityWhere("scholarship", 90));

        assertTrue(
                learned.contains("learn"),
                "a city of scholars said nothing about learning: " + learned
        );

        for (String measure : List.of("crime", "war", "disease", "scholarship")) {
            for (int value : new int[]{0, 50, 100}) {
                String felt = CityPressure.felt(cityWhere(measure, value));

                assertFalse(felt.isBlank(), measure + " at " + value + " said nothing");
                assertTrue(felt.endsWith("."), "not a sentence: " + felt);
            }
        }
    }

    // ---- reaching the run -------------------------------------------------

    /**
     * The odds the engine actually offers, in one city and then another.
     *
     * <p>The rules can be right and the engine never consult them, which is
     * invisible to anything that only tests the rules. This asks the engine
     * itself what a check is worth, standing in one place and then another.
     */
    @Test
    @DisplayName("where you are standing changes the odds the engine offers")
    void theEngineAsksTheCityBeforeItRolls() {
        GameEngine engine = new GameEngine("Yusuf");

        Choice study = new Choice(
                "Sit with the copyists",
                "education",
                50,
                "You learn something.",
                "You do not.",
                java.util.Map.of("education", 5),
                java.util.Map.of("stress", 3),
                java.util.Map.of(),
                java.util.Map.of()
        );

        City here = engine.getCurrentCity();

        assertNotNull(here, "the run is not standing anywhere");

        int scholarshipBefore = here.getScholarship();

        here.change("scholarship", 95 - scholarshipBefore);
        int amongScholars = engine.successChanceFor(study);

        here.change("scholarship", 5 - here.getScholarship());
        int amongNobody = engine.successChanceFor(study);

        assertTrue(
                amongScholars > amongNobody,
                "the engine offered the same odds on a study check in a city full "
                        + "of scholars (" + amongScholars + ") as in one with none ("
                        + amongNobody + "); it is not asking the city anything"
        );

        // And a choice with nothing to check is not affected by anywhere.
        Choice plain = new Choice(
                "Go home", "You go home.", java.util.Map.of(), java.util.Map.of());

        assertEquals(100, engine.successChanceFor(plain));
    }

    /**
     * Working out the odds and then ignoring them is the one way this could
     * be correct everywhere and still do nothing, so the roll is watched too.
     */
    @Test
    @DisplayName("the roll is against the odds, not against a coin")
    void theRollUsesTheOddsItWorkedOut() {
        GameEngine engine = new GameEngine("Yusuf");

        Choice hopeless = new Choice(
                "Argue the point", "education", 200,
                "You win it.", "You do not.",
                java.util.Map.of("education", 1), java.util.Map.of("stress", 1),
                java.util.Map.of(), java.util.Map.of()
        );

        Choice certain = new Choice(
                "Say your own name", "education", -200,
                "You manage it.", "You do not.",
                java.util.Map.of("education", 1), java.util.Map.of("stress", 1),
                java.util.Map.of(), java.util.Map.of()
        );

        assertEquals(10, engine.successChanceFor(hopeless), "the floor moved");
        assertEquals(90, engine.successChanceFor(certain), "the ceiling moved");

        int rolls = 2000;
        int wonTheHopeless = 0;
        int wonTheCertain = 0;

        for (int roll = 0; roll < rolls; roll++) {
            if (engine.resolveChoiceSuccess(hopeless)) {
                wonTheHopeless++;
            }

            if (engine.resolveChoiceSuccess(certain)) {
                wonTheCertain++;
            }
        }

        // Ten percent against ninety. Anything close to even means the roll
        // stopped consulting the number it just worked out.
        assertTrue(
                wonTheHopeless < rolls / 4,
                "a hopeless check came off " + wonTheHopeless + " times in " + rolls
                        + "; the roll is not using the odds"
        );

        assertTrue(
                wonTheCertain > rolls * 3 / 4,
                "a certainty failed most of the time (" + wonTheCertain + " of "
                        + rolls + "); the roll is not using the odds"
        );
    }

    /**
     * The rules can be right and the engine never consult them. This plays
     * real lives and checks the city turns up in what actually happens.
     */
    @Test
    @DisplayName("the city reaches lives people play, in the results and the deaths")
    void theCityIsFeltInARun() {
        int lives = 150;
        int resultsMentioningTheCity = 0;
        int deathsInTheCity = 0;

        Random random = new Random(13);

        for (int life = 0; life < lives; life++) {
            GameEngine engine = new GameEngine();

            while (engine.getCurrentEvent() != null && engine.getPlayer().isAlive()) {
                List<Choice> playable = new ArrayList<>();

                for (Choice choice : engine.getCurrentEvent().getChoices()) {
                    if (engine.canChoose(choice)) {
                        playable.add(choice);
                    }
                }

                if (playable.isEmpty()) {
                    break;
                }

                String city = engine.getCurrentCityName();

                String result = engine.applyChoice(
                        playable.get(random.nextInt(playable.size())));

                if (result != null && result.contains(city + ":")) {
                    resultsMentioningTheCity++;
                }
            }

            String reason = engine.getPlayer().getDeathReason();

            if (reason != null
                    && (reason.contains("sickness in the city")
                            || reason.contains("city was being taken")
                            || reason.contains("walls did not hold")
                            || reason.contains("sickness went through")
                            || reason.contains("sickness took the old")
                            || reason.contains("expected it to hold"))) {

                deathsInTheCity++;
            }
        }

        // Measured at about one choice in five and one life in eleven over six
        // hundred runs. The bars are far below that: they fail on the city
        // being ignored entirely, not on an unlucky sample.
        assertTrue(
                resultsMentioningTheCity >= 40,
                "across " + lives + " lives the city changed the outcome of a "
                        + "choice only " + resultsMentioningTheCity + " times; it is "
                        + "not reaching the run"
        );

        assertTrue(
                deathsInTheCity >= 2,
                "across " + lives + " lives, plague and siege killed "
                        + deathsInTheCity + " people; the city cannot kill anybody"
        );
    }

    /**
     * The one thing that must not happen: a city so bad the run is decided by
     * where the character happened to be born.
     */
    @Test
    @DisplayName("the city colours a run without deciding it")
    void theCityDoesNotTakeOverTheRun() {
        int lives = 200;
        int died = 0;

        Random random = new Random(7);

        for (int life = 0; life < lives; life++) {
            GameEngine engine = new GameEngine();

            while (engine.getCurrentEvent() != null && engine.getPlayer().isAlive()) {
                List<Choice> playable = new ArrayList<>();

                for (Choice choice : engine.getCurrentEvent().getChoices()) {
                    if (engine.canChoose(choice)) {
                        playable.add(choice);
                    }
                }

                if (playable.isEmpty()) {
                    break;
                }

                engine.applyChoice(playable.get(random.nextInt(playable.size())));
            }

            if (!engine.getPlayer().isAlive()) {
                died++;
            }
        }

        int mortality = 100 * died / lives;

        // A random bot dies about 75% of the time with the cities inert and
        // about 79% with them live. Anything approaching ninety means the map
        // has stopped colouring the run and started being it.
        if (mortality > 88) {
            fail("a random life now dies " + mortality + "% of the time; the city "
                    + "has gone from a pressure to the thing that decides a run");
        }
    }
}
