package com.example.al_mirath.service;

import com.example.al_mirath.model.FamilyMember;
import com.example.al_mirath.model.Kinship;
import com.example.al_mirath.model.LifePath;
import com.example.al_mirath.model.NpcMemory;
import com.example.al_mirath.model.PlayerCharacter;
import com.example.al_mirath.model.RecurringCharacter;
import com.example.al_mirath.model.RelationshipType;
import com.example.al_mirath.model.Succession;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * What the Bonds panel says once somebody else is holding the house.
 *
 * <p>The cast used to be copied across whole, which was wrong in three ways
 * at once and all of them were on screen: the forebear's dead were listed as
 * the heir's people, a lifetime of warmth was handed to somebody who had
 * never met them, and the memories still read in the second person — a
 * twenty-four-year-old was told they had rejected a mentor at nine.
 *
 * <p>What should cross is a disposition: the living who mattered, at half the
 * feeling, under a memory that says whose deed it was — and the heir's own
 * people on top, because the friend you make at seven is not inherited.
 */
class BondsAcrossGenerationsTest {

    private PlayerCharacter forebear() {
        return new PlayerCharacter(
                "Yusuf", "Abbasid Era", "Scholar's Child", "Stable Household",
                "Patient", 62, 40, 50, 50, 50, 50, 55, 60, 40
        );
    }

    private PlayerCharacter heir() {
        return new PlayerCharacter(
                "Ayyub", "Abbasid Era", "Scholar's Child", "Stable Household",
                "Patient", 24, 60, 30, 40, 30, 20, 50, 50, 25
        );
    }

    /**
     * The cast a long life leaves behind: warm, cold, weak and dead.
     *
     * <p>Keyed on the ids rather than on what people are, because nobody is
     * anything at the start of a life any more — a companion becomes a friend
     * by being treated like one, which is what this does.
     */
    private RecurringCharacterRegistry aLifetimeOfPeople(PlayerCharacter player) {
        RecurringCharacterRegistry cast = RecurringCharacterRegistry.createFor(player);

        cast.changeRelationship(
                "childhood_companion", 60, "a_life_together",
                "You stood by them when nobody else would.", 40);

        cast.changeRelationship(
                "early_rival", -40, "the_long_quarrel",
                "You broke them in public, and they remember it.", 35);

        // The elder is left as they started: known, and nothing more.
        return cast;
    }

    private RecurringCharacter find(RecurringCharacterRegistry cast, String id) {
        for (RecurringCharacter character : cast.all()) {
            if (character.getId().equals(id)) {
                return character;
            }
        }

        return null;
    }

    // ---- who crosses ------------------------------------------------------

    @Test
    @DisplayName("the forebear's dead are not the heir's people")
    void theDeadDoNotFollowTheHeir() {
        PlayerCharacter player = forebear();
        RecurringCharacterRegistry cast = aLifetimeOfPeople(player);

        // Everyone the forebear knew dies with them.
        for (int year = 0; year < 90; year++) {
            cast.ageEveryone(1);
        }

        RecurringCharacterRegistry inherited = cast.inheritedByTheHouse(heir());

        for (RecurringCharacter character : inherited.all()) {
            assertTrue(
                    character.isAlive(),
                    character.getName() + " is dead and was still handed to the heir "
                            + "as one of their people"
            );
        }
    }

    @Test
    @DisplayName("a bond too weak to matter dies with the person who made it")
    void onlyWhatMatteredCrosses() {
        PlayerCharacter player = forebear();

        // A cast nobody invested in: friendships start at 28, mentors at 35.
        RecurringCharacterRegistry untouched =
                RecurringCharacterRegistry.createFor(player);

        RecurringCharacterRegistry inherited =
                untouched.inheritedByTheHouse(heir());

        for (RecurringCharacter character : inherited.all()) {
            assertFalse(
                    character.getId().startsWith("legacy_"),
                    character.getName() + " was barely known to the forebear and "
                            + "was still handed on as a bond of the house"
            );
        }
    }

    @Test
    @DisplayName("what crosses is a disposition, at half the feeling")
    void warmthIsHalvedTowardNeutral() {
        PlayerCharacter player = forebear();
        RecurringCharacterRegistry cast = aLifetimeOfPeople(player);

        RecurringCharacter friend = find(cast, "childhood_companion");
        RecurringCharacter rival = find(cast, "early_rival");

        assertNotNull(friend);
        assertNotNull(rival);

        RecurringCharacterRegistry inherited = cast.inheritedByTheHouse(heir());

        RecurringCharacter carriedFriend = find(inherited, "legacy_childhood_companion");
        RecurringCharacter carriedRival = find(inherited, "legacy_early_rival");

        assertNotNull(carriedFriend, "a lifelong friend of the house was lost entirely");
        assertNotNull(carriedRival, "a lifelong enemy of the house was lost entirely");

        assertEquals(
                friend.getRelationship() / 2, carriedFriend.getRelationship(),
                "the heir was handed a friendship they never earned at full strength"
        );

        assertEquals(
                rival.getRelationship() / 2, carriedRival.getRelationship(),
                "the heir was handed a quarrel they were never part of at full force"
        );

        // Halved, not erased: a rival's house is still a rival's house.
        assertTrue(carriedRival.getRelationship() < 0);
        assertTrue(carriedFriend.getRelationship() > 0);
    }

    // ---- what they are told -----------------------------------------------

    @Test
    @DisplayName("nothing the forebear did is told to the heir as their own doing")
    void noDeedIsMisattributed() {
        PlayerCharacter player = forebear();
        RecurringCharacterRegistry cast = aLifetimeOfPeople(player);

        RecurringCharacterRegistry inherited = cast.inheritedByTheHouse(heir());

        for (RecurringCharacter character : inherited.all()) {
            if (!character.getId().startsWith("legacy_")) {
                continue;
            }

            for (NpcMemory memory : character.getMemories()) {
                assertTrue(
                        memory.predatesThePlayer(),
                        character.getName() + " remembers \"" + memory.description()
                                + "\" as something the heir did at age "
                                + memory.playerAge() + ", which was before they existed"
                );
            }
        }
    }

    @Test
    @DisplayName("the panel never tells an heir they were nine when it happened")
    void theSummaryReadsCorrectlyForAnInheritedBond() {
        PlayerCharacter player = forebear();
        RecurringCharacterRegistry cast = aLifetimeOfPeople(player);

        String summary = cast.inheritedByTheHouse(heir()).relationshipSummary();

        assertFalse(
                summary.contains("(you were -1)"),
                "an inherited bond was reported with a nonsense age: " + summary
        );

        assertTrue(
                summary.contains("from before your time"),
                "an inherited bond was not marked as predating the heir: " + summary
        );

        assertFalse(
                summary.contains("Deceased"),
                "the heir's Bonds panel is listing the forebear's dead: " + summary
        );
    }

    // ---- the heir's own people --------------------------------------------

    /**
     * Every npc story flag in the game names the three starting ids, so an
     * heir whose own friend is keyed anything else would play a whole life
     * of events that reach a person they have never met.
     */
    @Test
    @DisplayName("the heir gets their own people, under the ids the events name")
    void theHeirHasAFriendOfTheirOwn() {
        PlayerCharacter player = forebear();
        RecurringCharacterRegistry cast = aLifetimeOfPeople(player);

        RecurringCharacterRegistry inherited = cast.inheritedByTheHouse(heir());

        for (String id : List.of("childhood_companion", "early_rival", "elder_mentor")) {
            RecurringCharacter own = find(inherited, id);

            assertNotNull(
                    own, "the heir was given no " + id + " of their own"
            );

            assertTrue(own.isAlive());

            assertTrue(
                    own.getMemories().isEmpty(),
                    own.getName() + " is somebody the heir has just met and already "
                            + "remembers something about them"
            );
        }

        // And the story flags reach that person, not the inherited one.
        RecurringCharacter carried = find(inherited, "legacy_childhood_companion");

        assertNotNull(carried, "the house's oldest friendship was not carried at all");

        assertEquals(1, carried.getMemories().size());

        inherited.applyStoryFlag("npc_friend_secret_protected", 20);

        RecurringCharacter own = find(inherited, "childhood_companion");

        assertFalse(
                own.getMemories().isEmpty(),
                "a friend event did not reach the heir's own friend"
        );

        assertEquals(
                1, carried.getMemories().size(),
                "a friend event landed on somebody the heir inherited instead"
        );
    }

    // ---- the forebear's student -------------------------------------------

    @Test
    @DisplayName("the forebear's student is not the heir's student")
    void aStudentBelongsToWhoeverTaughtThem() {
        PlayerCharacter player = forebear();
        RecurringCharacterRegistry cast = aLifetimeOfPeople(player);

        assertTrue(cast.takeStudent("Idris", 50), "the forebear could not take a student");

        RecurringCharacterRegistry inherited = cast.inheritedByTheHouse(heir());

        RecurringCharacter taught = null;

        for (RecurringCharacter character : inherited.all()) {
            if (character.getName().equals("Idris")) {
                taught = character;
            }
        }

        assertNotNull(taught, "the person the forebear taught vanished entirely");

        assertFalse(
                taught.getRelationshipType() == RelationshipType.STUDENT,
                "the heir was told they are teaching somebody they have never met"
        );

        assertFalse(
                inherited.hasLivingStudent(),
                "an inherited student blocks the heir from ever taking one of their own"
        );

        // And the house cannot be handed to somebody no player ever chose.
        List<Succession> heirs = SuccessionService.candidates(null, inherited);

        for (Succession claimant : heirs) {
            assertFalse(
                    claimant.name().equals("Idris"),
                    "a student the heir never took was offered the house"
            );
        }
    }

    // ---- through the engine, which is what the panel reads ----------------

    /**
     * The registry can do the right thing and the panel still show the wrong
     * thing, because the engine decides what the heir is handed. This plays a
     * life, hands the house on, and reads the panel.
     */
    @Test
    @DisplayName("the panel a real heir opens is their own, not the last life's")
    void theEngineHandsOnADispositionRatherThanACopy() {
        GameEngine finished = aPlayedLifeWithAnHeir();

        String before = finished.getRecurringCharacterSummary();

        GameEngine heir = finished.succeedTo(finished.getSuccessors().get(0));

        assertNotNull(heir, "the house could not be handed on at all");

        String after = heir.getRecurringCharacterSummary();

        assertFalse(
                after.equals(before),
                "the heir opened the Bonds panel and found the last life's, verbatim"
        );

        assertFalse(
                after.contains("Deceased"),
                "the heir's Bonds panel lists people who died before they held "
                        + "the house:\n" + after
        );

        // The heir has made no memories yet, so every "you were" in the panel
        // is an age from a life that was not theirs.
        assertFalse(
                after.contains("(you were "),
                "the heir is being told the forebear's deeds as their own:\n" + after
        );

        assertFalse(
                heir.getRecurringCharacters().hasLivingStudent(),
                "the heir inherited a student they never chose to teach"
        );
    }

    /** A life played out to an end that leaves somebody to carry the house. */
    private GameEngine aPlayedLifeWithAnHeir() {
        java.util.Random random = new java.util.Random(31);

        for (int attempt = 0; attempt < 300; attempt++) {
            GameEngine engine = new GameEngine("Yusuf");

            while (engine.getCurrentEvent() != null && engine.getPlayer().isAlive()) {
                List<com.example.al_mirath.model.Choice> playable =
                        new java.util.ArrayList<>();

                for (var choice : engine.getCurrentEvent().getChoices()) {
                    if (engine.canChoose(choice)) {
                        playable.add(choice);
                    }
                }

                if (playable.isEmpty()) {
                    break;
                }

                engine.applyChoice(playable.get(random.nextInt(playable.size())));
            }

            if (engine.hasSuccessor()) {
                return engine;
            }
        }

        throw new IllegalStateException("in three hundred lives, none left an heir");
    }

    // ---- the household ----------------------------------------------------

    @Test
    @DisplayName("the heir's feeling for their brothers and sisters is their own")
    void householdAffectionIsHalvedToo() {
        JSONArray members = new JSONArray();

        members.put(member("c1", "Ayyub", Kinship.CHILD, 24, 80));
        members.put(member("c2", "Barira", Kinship.CHILD, 19, 80));

        JSONObject root = new JSONObject();
        root.put("members", members);
        root.put("nextId", 3);
        root.put("lastBirthAge", -1);

        FamilyRegistry household = FamilyRegistry.fromJson(root);

        assertTrue(household.succeedTo("c1"), "the heir was not found in the household");

        FamilyMember sibling = household.get("c2");

        assertNotNull(sibling, "the heir's sister left the household entirely");

        assertEquals(
                Kinship.SIBLING, sibling.getKinship(),
                "the forebear's other child is the heir's sibling now"
        );

        assertEquals(
                40, sibling.getAffection(),
                "the heir was handed their parent's feeling for a child at full "
                        + "strength, as though it were their own"
        );
    }

    private JSONObject member(
            String id, String name, Kinship kinship, int age, int affection
    ) {
        JSONObject object = new JSONObject();

        object.put("id", id);
        object.put("name", name);
        object.put("kinship", kinship.name());
        object.put("trait", "watchful");
        object.put("parentId", "");
        object.put("age", age);
        object.put("affection", affection);
        object.put("alive", true);
        object.put("lifePath", LifePath.UNDECIDED.name());
        object.put("deathReason", "");

        return object;
    }
}
