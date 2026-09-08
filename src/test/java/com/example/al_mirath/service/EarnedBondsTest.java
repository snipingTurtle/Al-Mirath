package com.example.al_mirath.service;

import com.example.al_mirath.model.Choice;
import com.example.al_mirath.model.PlayerCharacter;
import com.example.al_mirath.model.RecurringCharacter;
import com.example.al_mirath.model.RelationshipType;
import org.json.JSONObject;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * What people become, rather than what they were labelled at birth.
 *
 * <p>A run used to open with a friend, an enemy and a teacher already named
 * in the Bonds panel, before a word had passed between any of them and the
 * player — which gave away every arc in the run on the first screen and made
 * the labels furniture. Worse, the labels only ever went up: a friend who was
 * sold out for money was still filed under Friend at eighty points of hatred,
 * so the most consequential choice available about a person changed nothing
 * about what they were.
 *
 * <p>Now nobody is anything until something happens. What they become follows
 * the warmth between them, in both directions, and the two bonds that are
 * agreed to rather than drifted into — teacher and student — have to be
 * offered and can be broken.
 */
class EarnedBondsTest {

    private PlayerCharacter player() {
        return new PlayerCharacter(
                "Yusuf", "Abbasid Era", "Scholar's Child", "Stable Household",
                "Patient", 8, 60, 30, 30, 25, 15, 50, 50, 40
        );
    }

    private RecurringCharacterRegistry aFreshCast() {
        return RecurringCharacterRegistry.createFor(player());
    }

    private RecurringCharacter in(RecurringCharacterRegistry cast, String id) {
        RecurringCharacter character = cast.get(id);

        assertNotNull(character, "the cast has no " + id);

        return character;
    }

    // ---- nobody starts as anything ---------------------------------------

    @Test
    @DisplayName("a life does not open already holding a friend, an enemy and a teacher")
    void nobodyIsAnythingYet() {
        RecurringCharacterRegistry cast = aFreshCast();

        for (RecurringCharacter character : cast.all()) {
            assertEquals(
                    RelationshipType.STRANGER, character.getRelationshipType(),
                    character.getName() + " was somebody's "
                            + character.getRelationshipType().displayName()
                            + " before they had met"
            );

            assertFalse(
                    character.isMet(),
                    character.getName() + " counts as met before anything happened"
            );
        }
    }

    @Test
    @DisplayName("the panel names nobody the player has not met")
    void namesAreNotGivenAway() {
        RecurringCharacterRegistry cast = aFreshCast();

        assertTrue(
                cast.relationshipSummary().isBlank(),
                "the Bonds panel named people on the first screen: "
                        + cast.relationshipSummary()
        );

        // The first thing that actually passes between them is the meeting.
        cast.changeRelationship(
                "childhood_companion", 5, "the_first_time",
                "You shared what you had.", 8);

        String summary = cast.relationshipSummary();

        assertTrue(
                summary.contains(in(cast, "childhood_companion").getName()),
                "somebody the player has just dealt with is still not in the panel"
        );

        assertFalse(
                summary.contains(in(cast, "early_rival").getName()),
                "the panel is still naming somebody the player has never met"
        );
    }

    /**
     * Not every dealing leaves a memory behind — the registry takes a blank
     * one — but anything that moves the warmth between two people is still
     * the two of them having dealt with each other.
     */
    @Test
    @DisplayName("an exchange nobody remembers is still an exchange")
    void meetingDoesNotDependOnLeavingAMemory() {
        RecurringCharacterRegistry cast = aFreshCast();

        cast.changeRelationship("elder_mentor", 6, "", "", 11);

        assertTrue(
                in(cast, "elder_mentor").isMet(),
                "somebody who has affected the player is still not counted as met, "
                        + "so they stay nameless in the panel for the whole run"
        );

        assertTrue(
                cast.relationshipSummary().contains(in(cast, "elder_mentor").getName()),
                "the panel is hiding somebody the player has already dealt with"
        );
    }

    // ---- becoming --------------------------------------------------------

    @Test
    @DisplayName("a companion becomes a friend by being treated like one")
    void friendshipIsEarned() {
        RecurringCharacterRegistry cast = aFreshCast();

        cast.changeRelationship(
                "childhood_companion", 5, "a_small_kindness", "A small kindness.", 9);

        assertEquals(
                RelationshipType.STRANGER,
                in(cast, "childhood_companion").getRelationshipType(),
                "one small kindness made somebody a friend for life"
        );

        cast.applyStoryFlag("npc_friend_secret_protected", 12);
        cast.applyStoryFlag("npc_friend_family_helped", 15);

        assertEquals(
                RelationshipType.FRIEND,
                in(cast, "childhood_companion").getRelationshipType(),
                "a lifetime of standing by somebody left them an acquaintance"
        );
    }

    @Test
    @DisplayName("a rival becomes a rival by crossing you, not by existing")
    void rivalryIsEarnedToo() {
        RecurringCharacterRegistry cast = aFreshCast();

        assertEquals(
                RelationshipType.STRANGER, in(cast, "early_rival").getRelationshipType()
        );

        cast.applyStoryFlag("npc_rival_outdebated", 14);

        assertEquals(
                RelationshipType.RIVAL, in(cast, "early_rival").getRelationshipType(),
                "somebody who set themselves against the player stayed a stranger"
        );
    }

    // ---- and unbecoming --------------------------------------------------

    @Test
    @DisplayName("a friend who is sold is not a friend any more")
    void betrayalEndsIt() {
        RecurringCharacterRegistry cast = aFreshCast();

        // Devoted, deliberately: a bond deep enough to absorb a large loss
        // and still clear the bar is exactly the case that used to survive.
        cast.changeRelationship(
                "childhood_companion", 80, "a_life_together",
                "You stood by them for thirty years.", 40);

        assertEquals(
                RelationshipType.FRIEND,
                in(cast, "childhood_companion").getRelationshipType()
        );

        cast.applyStoryFlag("npc_friend_betrayed", 41);

        RecurringCharacter sold = in(cast, "childhood_companion");

        assertNotEquals(
                RelationshipType.FRIEND, sold.getRelationshipType(),
                "somebody the player sold for money is still recorded as a friend"
        );

        assertEquals(
                RelationshipType.RIVAL, sold.getRelationshipType(),
                "the person the player sold has no quarrel with them"
        );

        assertTrue(
                sold.getRelationship() < 0,
                "the person the player sold still feels warmly toward them"
        );
    }

    /**
     * The whole point of the change, in one assertion: what a deed does must
     * not depend on there having been little to lose.
     */
    @Test
    @DisplayName("a betrayal ends it whether there was much to lose or little")
    void betrayalDoesNotScaleWithWhatWasThere() {
        for (int warmth : new int[]{0, 30, 60, 90, 100}) {
            RecurringCharacterRegistry cast = aFreshCast();

            cast.changeRelationship(
                    "childhood_companion", warmth, "history", "Some history.", 30);

            cast.applyStoryFlag("npc_friend_betrayed", 31);

            RecurringCharacter sold = in(cast, "childhood_companion");

            assertEquals(
                    RelationshipType.RIVAL, sold.getRelationshipType(),
                    "betrayed after " + warmth + " points of warmth, they came out "
                            + sold.getRelationshipType().displayName()
            );
        }
    }

    @Test
    @DisplayName("a rival who is made peace with stops being one, and can become a friend")
    void aRivalCanComeRound() {
        RecurringCharacterRegistry cast = aFreshCast();

        cast.applyStoryFlag("npc_rival_outdebated", 14);

        assertEquals(RelationshipType.RIVAL, in(cast, "early_rival").getRelationshipType());

        // One gesture is not peace. A quarrel that ran for years takes more
        // than a single good afternoon to be over.
        cast.applyStoryFlag("npc_rival_respected", 30);

        assertEquals(
                RelationshipType.STRANGER,
                in(cast, "early_rival").getRelationshipType(),
                "one civil exchange turned a lifelong enemy into an ally"
        );

        cast.applyStoryFlag("npc_rival_respected", 34);

        RecurringCharacter former = in(cast, "early_rival");

        assertEquals(
                RelationshipType.ALLY, former.getRelationshipType(),
                "a quarrel that was settled left them an enemy, or a stranger who "
                        + "had never quarrelled at all"
        );

        assertTrue(former.wasEverRival(), "the house forgot there had been a quarrel");

        cast.applyStoryFlag("npc_rival_reconciled", 40);

        assertEquals(
                RelationshipType.FRIEND, in(cast, "early_rival").getRelationshipType(),
                "somebody who fought the player and then stood with them for years "
                        + "can never be more than an ally"
        );
    }

    // ---- bonds that are agreed to ----------------------------------------

    @Test
    @DisplayName("warmth alone never makes a teacher")
    void theElderMightNeverBecomeTheMentor() {
        RecurringCharacterRegistry cast = aFreshCast();

        // Thirty years of being perfectly pleasant to them.
        cast.changeRelationship(
                "elder_mentor", 65, "always_civil", "Always civil, always polite.", 40);

        assertEquals(
                RelationshipType.FRIEND, in(cast, "elder_mentor").getRelationshipType(),
                "being liked by an elder is not the same as being taught by them"
        );

        // Taking the lesson is what makes them a teacher.
        cast.applyStoryFlag("npc_mentor_guidance_accepted", 41);

        assertEquals(
                RelationshipType.MENTOR, in(cast, "elder_mentor").getRelationshipType(),
                "the player took the lesson and the elder is still not their teacher"
        );
    }

    @Test
    @DisplayName("a bond that was agreed to can be broken")
    void aMentorshipCanEnd() {
        RecurringCharacterRegistry cast = aFreshCast();

        cast.applyStoryFlag("npc_mentor_guidance_accepted", 20);

        assertEquals(RelationshipType.MENTOR, in(cast, "elder_mentor").getRelationshipType());

        cast.applyStoryFlag("npc_mentor_rejected", 25);

        assertNotEquals(
                RelationshipType.MENTOR, in(cast, "elder_mentor").getRelationshipType(),
                "the player threw the bond back in their teacher's face and is "
                        + "still recorded as their student"
        );
    }

    // ---- what a save has to carry ----------------------------------------

    @Test
    @DisplayName("a save remembers who has been met and who was ever an enemy")
    void theHistoryOutlivesASave() {
        RecurringCharacterRegistry cast = aFreshCast();

        cast.applyStoryFlag("npc_rival_outdebated", 14);
        cast.applyStoryFlag("npc_rival_respected", 30);
        cast.applyStoryFlag("npc_rival_respected", 34);

        RecurringCharacterRegistry loaded =
                RecurringCharacterRegistry.fromJson(
                        new JSONObject(cast.toJson().toString()));

        assertFalse(
                in(loaded, "childhood_companion").isMet(),
                "somebody the player never met came back from the save already known"
        );

        assertTrue(in(loaded, "early_rival").isMet());

        assertTrue(
                in(loaded, "early_rival").wasEverRival(),
                "a save forgot that the alliance began as a quarrel"
        );

        assertEquals(
                RelationshipType.ALLY, in(loaded, "early_rival").getRelationshipType(),
                "a settled quarrel came back from the save as something else"
        );
    }

    // ---- and in lives people play ----------------------------------------

    /**
     * The rules can be right and never come up. This plays real lives and
     * counts what people actually turn into.
     */
    @Test
    @DisplayName("all of this happens in lives people play")
    void theArcsReachTheGame() {
        int lives = 120;
        int namedBeforeMeeting = 0;
        int elderNeverTaught = 0;
        int companionEndedHostile = 0;
        int rivalCameRound = 0;

        Random random = new Random(17);

        for (int life = 0; life < lives; life++) {
            GameEngine engine = new GameEngine();

            for (RecurringCharacter character : engine.getRecurringCharacters().all()) {
                if (character.isMet()) {
                    namedBeforeMeeting++;
                }
            }

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

            RecurringCharacterRegistry cast = engine.getRecurringCharacters();

            RecurringCharacter elder = cast.get("elder_mentor");
            RecurringCharacter companion = cast.get("childhood_companion");
            RecurringCharacter rival = cast.get("early_rival");

            if (elder != null && elder.getRelationshipType() != RelationshipType.MENTOR) {
                elderNeverTaught++;
            }

            if (companion != null
                    && companion.getRelationshipType() == RelationshipType.RIVAL) {

                companionEndedHostile++;
            }

            if (rival != null
                    && rival.wasEverRival()
                    && (rival.getRelationshipType() == RelationshipType.ALLY
                            || rival.getRelationshipType() == RelationshipType.FRIEND)) {

                rivalCameRound++;
            }
        }

        assertEquals(
                0, namedBeforeMeeting,
                "lives opened with " + namedBeforeMeeting + " people already met, "
                        + "so the panel gives their names away on the first screen"
        );

        // Measured over five hundred lives: the elder stays untaught in about
        // three lives in five, the companion ends hostile in about three in
        // ten, and a rival comes round in about one in fifty. The bars are far
        // below those, so they fail on an arc being unreachable rather than on
        // an unlucky sample.
        assertTrue(
                elderNeverTaught >= 20,
                "in " + lives + " lives the elder became a teacher all but "
                        + elderNeverTaught + " times; it is not a thing that has "
                        + "to be earned"
        );

        assertTrue(
                elderNeverTaught <= lives - 10,
                "in " + lives + " lives the elder never once became a teacher; "
                        + "the bond cannot be formed at all"
        );

        assertTrue(
                companionEndedHostile >= 8,
                "in " + lives + " lives a childhood friend ended up an enemy only "
                        + companionEndedHostile + " times; betrayal is not costing "
                        + "anything"
        );

        assertTrue(
                rivalCameRound >= 1,
                "in " + lives + " lives no rival was ever won round; the arc is "
                        + "unreachable in play"
        );
    }
}
