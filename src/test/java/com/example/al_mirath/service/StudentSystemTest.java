package com.example.al_mirath.service;

import com.example.al_mirath.model.Choice;
import com.example.al_mirath.model.GameEvent;
import com.example.al_mirath.model.PlayerCharacter;
import com.example.al_mirath.model.RecurringCharacter;
import com.example.al_mirath.model.RelationshipType;
import com.example.al_mirath.model.WorldState;
import org.json.JSONObject;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Taking somebody on as a student.
 *
 * <p>A marriage happens to you and a child arrives whether you planned one or
 * not. A student is the attachment of that weight the player actually
 * chooses, so the failures worth guarding against are the ones that would
 * quietly take the choice back: an offer nobody ever sees, an offer that
 * names a different stranger every time the screen redraws, and a house that
 * gets handed to somebody the player never chose at all.
 */
class StudentSystemTest {

    private PlayerCharacter player(int education, int reputation, int power) {
        return new PlayerCharacter(
                "Yusuf", "Abbasid Era", "Scholar's Child", "Stable Household",
                "Patient", 40, 60, 40, education, reputation, power, 55, 60, 40
        );
    }

    /** Someone with something worth following. */
    private PlayerCharacter accomplished() {
        return player(70, 40, 20);
    }

    private List<GameEvent> offersTo(
            RecurringCharacterRegistry cast,
            PlayerCharacter who,
            WorldState world
    ) {
        return StudentEvents.create(cast, who, world);
    }

    // ---- being asked -----------------------------------------------------

    @Test
    @DisplayName("nobody apprentices themselves to a person with nothing to hand on")
    void theOfferIsEarnedRatherThanScheduled() {
        PlayerCharacter unremarkable = player(30, 30, 30);

        assertTrue(
                offersTo(
                        RecurringCharacterRegistry.createFor(unremarkable),
                        unremarkable,
                        new WorldState()
                ).isEmpty(),
                "somebody asked to be taught by a person who had learned, done "
                        + "and become nothing in particular"
        );
    }

    @Test
    @DisplayName("learning, standing or office — any one of them is worth following")
    void thereIsMoreThanOneWayToBeWorthLearningFrom() {
        for (PlayerCharacter who : List.of(
                player(70, 20, 20),
                player(20, 70, 20),
                player(20, 20, 70)
        )) {
            assertFalse(
                    offersTo(
                            RecurringCharacterRegistry.createFor(who),
                            who,
                            new WorldState()
                    ).isEmpty(),
                    "nobody wanted to learn from an accomplished person"
            );
        }
    }

    /**
     * The offer is rebuilt from scratch every time the engine looks for an
     * event, so a rolled name would put a different stranger at the door on
     * every redraw — the same churn that once had one life's city changing
     * between decisions.
     */
    @Test
    @DisplayName("the same person is at the door every time the offer is built")
    void theCandidateDoesNotChangeUnderneathThePlayer() {
        PlayerCharacter who = accomplished();
        RecurringCharacterRegistry cast = RecurringCharacterRegistry.createFor(who);
        WorldState world = new WorldState();

        String first = offersTo(cast, who, world).get(0).getTitle();

        for (int redraw = 0; redraw < 30; redraw++) {
            assertEquals(
                    first,
                    offersTo(cast, who, world).get(0).getTitle(),
                    "a different person was at the door on redraw " + redraw
            );
        }
    }

    @Test
    @DisplayName("the person asking is not somebody the player already knows")
    void theCandidateIsAStranger() {
        PlayerCharacter who = accomplished();
        RecurringCharacterRegistry cast = RecurringCharacterRegistry.createFor(who);

        String candidate = cast.prospectiveStudentName(12345L);

        for (RecurringCharacter known : cast.all()) {
            assertFalse(
                    known.getName().equals(candidate),
                    "the stranger at the door was " + candidate
                            + ", who is already in the cast"
            );
        }
    }

    @Test
    @DisplayName("the second person to ask is not the first one back again")
    void theLateOfferIsSomebodyElse() {
        PlayerCharacter who = accomplished();
        RecurringCharacterRegistry cast = RecurringCharacterRegistry.createFor(who);

        List<GameEvent> both = offersTo(cast, who, new WorldState());

        assertEquals(2, both.size(), "a life should hold at most two chances to teach");

        assertFalse(
                both.get(0).getTitle().equals(both.get(1).getTitle()),
                "the same person asked twice"
        );

        assertEquals("Adulthood", both.get(0).getLifeStage());
        assertEquals("Legacy", both.get(1).getLifeStage());
    }

    // ---- deciding --------------------------------------------------------

    @Test
    @DisplayName("turning somebody away is remembered, and the last refusal is the last")
    void refusingClosesTheDoor() {
        PlayerCharacter who = accomplished();
        RecurringCharacterRegistry cast = RecurringCharacterRegistry.createFor(who);

        WorldState refusedOnce = new WorldState();
        refusedOnce.addFlag(StudentEvents.REFUSED_ONCE);

        List<GameEvent> afterOne = offersTo(cast, who, refusedOnce);

        assertEquals(
                1, afterOne.size(),
                "turning the first one away did not stop them coming back"
        );

        assertEquals(
                "Legacy", afterOne.get(0).getLifeStage(),
                "the one remaining chance was not the late one"
        );

        WorldState refusedTwice = new WorldState();
        refusedTwice.addFlag(StudentEvents.REFUSED_ONCE);
        refusedTwice.addFlag(StudentEvents.REFUSED_TWICE);

        assertTrue(
                offersTo(cast, who, refusedTwice).isEmpty(),
                "somebody kept asking after the player had said no twice"
        );
    }

    @Test
    @DisplayName("saying yes puts a person in the cast, not a number on a sheet")
    void theStudentJoinsTheCast() {
        PlayerCharacter who = accomplished();
        RecurringCharacterRegistry cast = RecurringCharacterRegistry.createFor(who);

        int before = cast.all().size();

        cast.applyStoryFlag(
                RecurringCharacterRegistry.TAKE_STUDENT_PREFIX + "Idris",
                40
        );

        assertEquals(before + 1, cast.all().size(), "nobody joined the cast");

        RecurringCharacter student = null;

        for (RecurringCharacter character : cast.all()) {
            if (character.getRelationshipType() == RelationshipType.STUDENT) {
                student = character;
            }
        }

        assertNotNull(student, "the person taken on was not recorded as a student");
        assertEquals("Idris", student.getName());
        assertTrue(student.isAlive());

        assertTrue(
                student.getRelationship() > 0,
                "somebody the player chose to teach began as a stranger"
        );

        assertFalse(
                student.getCurrentRole().isBlank(),
                "the student has no place in the world at all"
        );
    }

    @Test
    @DisplayName("a player already teaching somebody is not asked again")
    void oneStudentAtATime() {
        PlayerCharacter who = accomplished();
        RecurringCharacterRegistry cast = RecurringCharacterRegistry.createFor(who);

        cast.applyStoryFlag(
                RecurringCharacterRegistry.TAKE_STUDENT_PREFIX + "Idris",
                40
        );

        assertTrue(
                offersTo(cast, who, new WorldState()).isEmpty(),
                "a second stranger asked to be taught by somebody already teaching"
        );

        // The same flag arriving twice — a replayed save, a repeated event —
        // must not put two of the same person in the cast.
        cast.applyStoryFlag(
                RecurringCharacterRegistry.TAKE_STUDENT_PREFIX + "Idris",
                42
        );

        int students = 0;

        for (RecurringCharacter character : cast.all()) {
            if (character.getRelationshipType() == RelationshipType.STUDENT) {
                students++;
            }
        }

        assertEquals(1, students, "the player ended up teaching two copies of one person");
    }

    @Test
    @DisplayName("a student is still a student after the game is saved and loaded")
    void theStudentSurvivesASave() {
        PlayerCharacter who = accomplished();
        RecurringCharacterRegistry cast = RecurringCharacterRegistry.createFor(who);

        cast.applyStoryFlag(
                RecurringCharacterRegistry.TAKE_STUDENT_PREFIX + "Idris",
                40
        );

        RecurringCharacterRegistry loaded =
                RecurringCharacterRegistry.fromJson(
                        new JSONObject(cast.toJson().toString())
                );

        assertTrue(
                loaded.hasLivingStudent(),
                "the student the player chose came back from the save as somebody else"
        );

        assertFalse(
                SuccessionService.candidates(null, loaded).isEmpty(),
                "a loaded student could no longer be handed the house"
        );
    }

    // ---- reaching the player ---------------------------------------------

    /**
     * The wiring, not the content.
     *
     * <p>An event class can be complete and correct and still never be built
     * by the engine, which has happened here before and is invisible to every
     * test that stops at the generator. This plays real lives and watches for
     * the offer to come up on its own.
     */
    @Test
    @DisplayName("the offer actually turns up in lives people play")
    void theOfferReachesTheGame() {
        int lives = 120;
        int sawTheOffer = 0;
        int endedTeaching = 0;

        Random random = new Random(11);

        for (int life = 0; life < lives; life++) {
            GameEngine engine = new GameEngine();
            boolean offered = false;

            while (engine.getCurrentEvent() != null && engine.getPlayer().isAlive()) {
                GameEvent event = engine.getCurrentEvent();

                Choice takeThemOn = null;

                for (Choice choice : event.getChoices()) {
                    for (String flag : choice.getSuccessFlags()) {
                        if (flag.startsWith(RecurringCharacterRegistry.TAKE_STUDENT_PREFIX)) {
                            takeThemOn = choice;
                        }
                    }
                }

                if (takeThemOn != null) {
                    offered = true;
                }

                List<Choice> available = new ArrayList<>();

                for (Choice choice : event.getChoices()) {
                    if (engine.canChoose(choice)) {
                        available.add(choice);
                    }
                }

                if (available.isEmpty()) {
                    break;
                }

                Choice picked =
                        takeThemOn != null && engine.canChoose(takeThemOn)
                                ? takeThemOn
                                : available.get(random.nextInt(available.size()));

                engine.applyChoice(picked);
            }

            if (offered) {
                sawTheOffer++;
            }

            if (engine.getRecurringCharacters().hasLivingStudent()) {
                endedTeaching++;
            }
        }

        // Measured at roughly two lives in five over four hundred runs. The
        // bar is set far below that so the test fails on the offer being
        // unreachable, not on a run of unlucky rolls.
        assertTrue(
                sawTheOffer >= 20,
                "in " + lives + " played lives the chance to teach somebody came "
                        + "up only " + sawTheOffer + " times; the event is not "
                        + "reaching the player"
        );

        assertTrue(
                endedTeaching >= 10,
                "in " + lives + " played lives, accepting every offer left only "
                        + endedTeaching + " players actually teaching anybody"
        );
    }
}
