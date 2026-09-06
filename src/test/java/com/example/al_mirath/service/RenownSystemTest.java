package com.example.al_mirath.service;

import com.example.al_mirath.model.Choice;
import com.example.al_mirath.model.GameEvent;
import com.example.al_mirath.model.PlayerCharacter;
import com.example.al_mirath.model.Renown;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Reputation that spreads.
 *
 * <p>Reputation was a number that gated choices. The point of this system is
 * that a deed starts known only to the people who were standing there, and
 * only travels if it was notable enough and the player well-known enough to
 * carry it. Two runs with identical reputation scores should be greeted
 * differently.
 */
class RenownSystemTest {

    private PlayerCharacter player(int reputation) {
        return new PlayerCharacter(
                "Test Character",
                "Abbasid Era",
                "Madrasa Student",
                "Stable Household",
                "Observant",
                30,
                60, 40, 55, reputation, 30, 55, 60, 20
        );
    }

    /** Every flag any choice or delayed consequence in the game can set. */
    private Set<String> settableFlags() {
        Set<String> settable = new HashSet<>();

        for (GameEvent event : EventLibrary.createEventPool()) {
            for (Choice choice : event.getChoices()) {
                settable.addAll(choice.getSuccessFlags());
                settable.addAll(choice.getFailureFlags());
            }
        }

        for (String flag : new ArrayList<>(settable)) {
            ConsequenceLibrary.consequencesFor(flag, 20)
                    .forEach(c -> settable.addAll(c.flagsToAdd()));
        }

        return settable;
    }

    // ---- the names are attached to deeds that exist ----------------------

    @Test
    @DisplayName("every deed that earns a name is a deed the game can actually produce")
    void noRenownHangsOnAnUnreachableDeed() {
        Set<String> settable = settableFlags();
        List<String> unreachable = new ArrayList<>();

        for (Renown renown : Renown.values()) {
            for (String deed : renown.deeds()) {
                if (!settable.contains(deed)) {
                    unreachable.add(renown.name() + " listens for " + deed
                            + ", which nothing sets");
                }
            }
        }

        assertTrue(
                unreachable.isEmpty(),
                String.join("\n", unreachable)
        );
    }

    @Test
    @DisplayName("every name is reachable, and no deed earns two of them")
    void everyRenownCanBeEarnedExactlyOneWay() {
        Map<String, Renown> byDeed = Renown.byDeed();
        Map<String, Integer> claims = new HashMap<>();

        for (Renown renown : Renown.values()) {
            assertFalse(
                    renown.deeds().isEmpty(),
                    renown + " can never be earned"
            );

            assertFalse(renown.epithet().isBlank());
            assertFalse(renown.witness().isBlank());
            assertTrue(renown.weight() > 0);

            for (String deed : renown.deeds()) {
                claims.merge(deed, 1, Integer::sum);
            }
        }

        for (Map.Entry<String, Integer> claim : claims.entrySet()) {
            assertEquals(
                    1,
                    claim.getValue(),
                    claim.getKey() + " earns more than one name, so byDeed() "
                            + "silently drops one"
            );
        }

        assertEquals(claims.size(), byDeed.size());
    }

    // ---- a deed starts where it happened ---------------------------------

    @Test
    @DisplayName("nothing is known before it happens")
    void silenceIsTheDefault() {
        RenownRegistry renown = new RenownRegistry();

        assertEquals(0, renown.reachOf(Renown.HAND_THAT_FEEDS));
        assertFalse(renown.isKnownTo(Renown.HAND_THAT_FEEDS, "commonPeople"));
        assertNull(renown.greetingFrom("commonPeople"));
        assertNull(renown.publicName());
        assertTrue(renown.renownSummary().isBlank());

        assertFalse(
                renown.record("a_flag_that_earns_nothing"),
                "an ordinary flag should not make anyone famous"
        );
    }

    @Test
    @DisplayName("a deed begins known only to the people who saw it")
    void aDeedStartsInTheRoomItHappenedIn() {
        RenownRegistry renown = new RenownRegistry();

        assertTrue(renown.record("fed_people_during_riot"));

        assertTrue(
                renown.isKnownTo(Renown.HAND_THAT_FEEDS, "commonPeople"),
                "the people who were fed do not remember being fed"
        );

        assertFalse(
                renown.isKnownTo(Renown.HAND_THAT_FEEDS, "court"),
                "the court heard about it the moment it happened"
        );

        assertNull(
                renown.publicName(),
                "a deed nobody has repeated yet is already a public name"
        );
    }

    @Test
    @DisplayName("stories travel, and then everyone has heard them")
    void storiesSpread() {
        RenownRegistry renown = new RenownRegistry();
        renown.record("fed_people_during_riot");

        int atTheTime = renown.reachOf(Renown.HAND_THAT_FEEDS);

        renown.spread(20, player(50));

        int later = renown.reachOf(Renown.HAND_THAT_FEEDS);

        assertTrue(later > atTheTime, "twenty years changed nothing");

        assertTrue(
                renown.isKnownTo(Renown.HAND_THAT_FEEDS, "court"),
                "after twenty years the court still had not heard"
        );

        assertEquals(
                Renown.HAND_THAT_FEEDS,
                renown.publicName(),
                "the story travelled but the player carries no name for it"
        );
    }

    @Test
    @DisplayName("a well-known player's deeds travel further in the same time")
    void fameCarriesAStoryFurther() {
        RenownRegistry unknown = new RenownRegistry();
        unknown.record("fed_people_during_riot");
        unknown.spread(5, player(0));

        RenownRegistry famous = new RenownRegistry();
        famous.record("fed_people_during_riot");
        famous.spread(5, player(100));

        assertTrue(
                famous.reachOf(Renown.HAND_THAT_FEEDS)
                        > unknown.reachOf(Renown.HAND_THAT_FEEDS),
                "being well known made no difference to how far it travelled"
        );
    }

    /** How many years pass before this circle has heard the story. */
    private int yearsUntilHeardBy(String audience) {
        RenownRegistry renown = new RenownRegistry();
        renown.record("fed_people_during_riot");

        for (int year = 1; year <= 100; year++) {
            renown.spread(1, player(20));

            if (renown.isKnownTo(Renown.HAND_THAT_FEEDS, audience)) {
                return year;
            }
        }

        return Integer.MAX_VALUE;
    }

    @Test
    @DisplayName("the shadow network hears a story before the court does")
    void theShadowsHearFirst() {
        int shadows = yearsUntilHeardBy("shadowNetwork");
        int court = yearsUntilHeardBy("court");

        assertTrue(
                shadows < court,
                "informers heard it after " + shadows + " years and the court "
                        + "after " + court + "; they trade in things that have "
                        + "not spread yet"
        );

        assertTrue(
                court < Integer.MAX_VALUE,
                "the court never hears anything at all"
        );
    }

    @Test
    @DisplayName("repeating the same kind of deed pushes the story on")
    void deedsCompound() {
        RenownRegistry once = new RenownRegistry();
        once.record("fed_people_during_riot");

        RenownRegistry twice = new RenownRegistry();
        twice.record("fed_people_during_riot");
        twice.record("built_a_common_granary");

        assertTrue(
                twice.reachOf(Renown.HAND_THAT_FEEDS)
                        > once.reachOf(Renown.HAND_THAT_FEEDS),
                "doing it twice was no more memorable than doing it once"
        );
    }

    @Test
    @DisplayName("the loudest story is the one the player is known by")
    void theStrongestNameSticks() {
        RenownRegistry renown = new RenownRegistry();

        renown.record("quietly_helped_the_village");
        renown.record("framed_an_innocent_man");
        renown.spread(20, player(60));

        assertEquals(
                Renown.KNIFE_IN_THE_DARK,
                renown.publicName(),
                "a small kindness outweighed a framed man"
        );
    }

    // ---- the same name, different rooms ----------------------------------

    @Test
    @DisplayName("a merchant and a soldier do not greet you the same way")
    void theGreetingFitsTheAudience() {
        Set<String> lines = new HashSet<>();

        for (String audience : List.of(
                "commonPeople", "court", "scholars", "merchants",
                "military", "shadowNetwork", "familyCouncil")) {

            String line = Renown.HAND_THAT_FEEDS.greeting(audience);

            assertFalse(line.isBlank());

            assertTrue(
                    line.contains(Renown.HAND_THAT_FEEDS.epithet()),
                    audience + " greeted the player without using the name"
            );

            assertTrue(
                    lines.add(line),
                    audience + " reuses another circle's greeting"
            );
        }
    }

    @Test
    @DisplayName("being admired and being notorious do not read the same")
    void praiseAndNotorietyDiffer() {
        for (String audience : List.of("commonPeople", "court", "merchants")) {
            assertNotEquals(
                    Renown.HAND_THAT_FEEDS.greeting(audience),
                    Renown.GRAIN_HOARDER.greeting(audience),
                    "the man who fed them and the man who sold the grain get "
                            + "the same reception from " + audience
            );
        }

        assertTrue(Renown.HAND_THAT_FEEDS.isAdmired());
        assertFalse(Renown.GRAIN_HOARDER.isAdmired());
    }

    @Test
    @DisplayName("the cast open with what their own circle has heard")
    void npcsGreetByWhatTheyHaveHeard() {
        RecurringCharacterRegistry cast =
                new RecurringCharacterRegistry(new Random(3));

        cast.generateInitialCast(player(50));

        RenownRegistry renown = new RenownRegistry();
        renown.record("fed_people_during_riot");

        String companion = cast.get("childhood_companion").getName();

        // The companion moves among the common people, who witnessed it.
        boolean greeted =
                RecurringCharacterEvents.create(cast, renown).stream()
                        .filter(event -> event.getDescription().contains(companion))
                        .anyMatch(event -> event.getDescription()
                                .contains(Renown.HAND_THAT_FEEDS.epithet()));

        assertTrue(greeted, "the companion never mentions what they have heard");

        boolean greetedWithoutRenown =
                RecurringCharacterEvents.create(cast, null).stream()
                        .anyMatch(event -> event.getDescription()
                                .contains(Renown.HAND_THAT_FEEDS.epithet()));

        assertFalse(
                greetedWithoutRenown,
                "a scene invented a reputation the player has not earned"
        );
    }

    @Test
    @DisplayName("a circle that has not heard the story says nothing about it")
    void silenceWhereTheStoryHasNotReached() {
        RenownRegistry renown = new RenownRegistry();
        renown.record("fed_people_during_riot");

        assertNotNull(renown.greetingFrom("commonPeople"));

        assertNull(
                renown.greetingFrom("court"),
                "the court greeted the player with a story it has not heard"
        );
    }

    // ---- it survives the engine ------------------------------------------

    /**
     * The registry working in isolation proves nothing about the game. This
     * plays real lives and looks for a name turning up in a scene the player
     * is actually shown.
     */
    @Test
    @DisplayName("a name the player earned reaches the events they are shown")
    void reputationReachesThePlayersScreen() {
        Random random = new Random(9);

        for (int run = 0; run < 120; run++) {
            GameEngine engine = new GameEngine();

            for (int i = 0; i < 40 && engine.getCurrentEvent() != null; i++) {
                String scene = engine.getCurrentEvent().getDescription();

                for (Renown story : Renown.values()) {
                    if (engine.getRenown().reachOf(story) > 0
                            && scene.contains(story.epithet())) {

                        return;
                    }
                }

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
        }

        fail("in 120 played lives, nobody ever greeted the player by what "
                + "they had done; the engine is not passing renown into the "
                + "scenes it builds");
    }

    @Test
    @DisplayName("renown round-trips through a save")
    void renownSurvivesASave() {
        RenownRegistry renown = new RenownRegistry();
        renown.record("framed_an_innocent_man");
        renown.record("fed_people_during_riot");
        renown.spread(10, player(40));

        RenownRegistry restored = RenownRegistry.fromJson(renown.toJson());

        for (Renown story : Renown.values()) {
            assertEquals(
                    renown.reachOf(story),
                    restored.reachOf(story),
                    story + " did not survive the save"
            );
        }
    }

    @Test
    @DisplayName("a played life earns names, and the engine keeps them")
    void theEngineRecordsAndSpreads() {
        Random random = new Random(5);
        boolean everEarned = false;

        for (int run = 0; run < 40 && !everEarned; run++) {
            GameEngine engine = new GameEngine();

            for (int i = 0; i < 40 && engine.getCurrentEvent() != null; i++) {
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

            if (engine.getRenownSummary().isBlank()) {
                continue;
            }

            everEarned = true;

            GameEngine loaded = GameEngine.fromSaveJson(engine.toSaveJson());

            assertEquals(
                    engine.getRenownSummary(),
                    loaded.getRenownSummary(),
                    "what the player was known for was lost in the save"
            );

            assertEquals(
                    engine.getPublicName(),
                    loaded.getPublicName()
            );
        }

        assertTrue(
                everEarned,
                "forty played lives produced nothing anybody would repeat"
        );
    }
}
