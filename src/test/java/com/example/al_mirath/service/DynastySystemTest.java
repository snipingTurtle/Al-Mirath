package com.example.al_mirath.service;

import com.example.al_mirath.model.Choice;
import com.example.al_mirath.model.City;
import com.example.al_mirath.model.Dynasty;
import com.example.al_mirath.model.FactionRelations;
import com.example.al_mirath.model.FamilyMember;
import com.example.al_mirath.model.Kinship;
import com.example.al_mirath.model.LifePath;
import com.example.al_mirath.model.PlayerCharacter;
import com.example.al_mirath.model.Renown;
import com.example.al_mirath.model.Succession;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

/**
 * A house that outlives the person holding it.
 *
 * <p>The whole feature turns on one rule: what crosses the boundary between
 * generations. Nothing personal does — the heir's body and learning are their
 * own. Everything the world holds rather than the body does: the money, the
 * standing, the cities with their sieges and their good decades, and whatever
 * the last generation was famous enough for that people still repeat it.
 *
 * <p>The failures worth guarding against are a heir who is secretly a clone of
 * their parent, and a "continuation" that quietly rebuilds the world from
 * scratch and so means nothing.
 */
class DynastySystemTest {

    private PlayerCharacter forebear(int wealth, int education, int reputation, int power) {
        return new PlayerCharacter(
                "Yusuf", "Abbasid Era", "Scholar's Child", "Stable Household",
                "Patient", 62, 40, wealth, education, reputation, power, 55, 60, 40
        );
    }

    /** A household built to order, so a test can say exactly who is alive. */
    private FamilyRegistry household(JSONObject... members) {
        JSONArray array = new JSONArray();

        for (JSONObject member : members) {
            array.put(member);
        }

        JSONObject root = new JSONObject();
        root.put("members", array);
        root.put("nextId", members.length + 1);
        root.put("lastBirthAge", -1);

        return FamilyRegistry.fromJson(root);
    }

    private JSONObject member(
            String id, String name, Kinship kinship, int age,
            boolean alive, LifePath path, String parentId
    ) {
        JSONObject object = new JSONObject();

        object.put("id", id);
        object.put("name", name);
        object.put("kinship", kinship.name());
        object.put("trait", "watchful");
        object.put("parentId", parentId);
        object.put("age", age);
        object.put("affection", 40);
        object.put("alive", alive);
        object.put("lifePath", path.name());
        object.put("deathReason", "");

        return object;
    }

    // ---- who inherits ----------------------------------------------------

    @Test
    @DisplayName("a line with nobody left to carry it ends, and that is a real outcome")
    void aLineCanEnd() {
        FamilyRegistry noOneLeft = household(
                member("f1", "Amina", Kinship.MOTHER, 80, true, LifePath.UNDECIDED, ""),
                member("c1", "Hakim", Kinship.CHILD, 30, false, LifePath.SOLDIER, ""),
                member("c2", "Layla", Kinship.CHILD, 6, true, LifePath.UNDECIDED, "")
        );

        assertTrue(
                SuccessionService.candidates(noOneLeft, null).isEmpty(),
                "a dead child and a six-year-old were treated as heirs"
        );
    }

    @Test
    @DisplayName("blood comes before loyalty, and the eldest claim comes first")
    void theOrderOfClaims() {
        FamilyRegistry children = household(
                member("c1", "Zahra", Kinship.CHILD, 19, true, LifePath.SCHOLAR, ""),
                member("c2", "Anas", Kinship.CHILD, 31, true, LifePath.COURTIER, ""),
                member("g1", "Nur", Kinship.GRANDCHILD, 14, true, LifePath.UNDECIDED, "c2")
        );

        List<Succession> heirs = SuccessionService.candidates(children, null);

        assertEquals(3, heirs.size());
        assertEquals("Anas", heirs.get(0).name(), "the eldest claim was not offered first");
        assertEquals("Zahra", heirs.get(1).name());
        assertEquals("Nur", heirs.get(2).name());

        for (Succession heir : heirs) {
            assertTrue(heir.blood(), heir.name() + " was not counted as blood");
        }
    }

    /**
     * Being liked is not the same as having been taught.
     *
     * <p>Any warm cast member used to clear the bar, which made the most
     * consequential relationship in a run a side effect of being popular.
     * Taking a student is a decision the player makes in an event now, and
     * that decision is what this asks for.
     */
    @Test
    @DisplayName("a house with no blood left goes to a student, not to whoever liked you")
    void loyaltyInheritsWhenBloodDoesNot() {
        PlayerCharacter player = forebear(50, 50, 50, 50);
        RecurringCharacterRegistry cast = RecurringCharacterRegistry.createFor(player);

        assertTrue(
                SuccessionService.candidates(household(), cast).isEmpty(),
                "a house was handed to somebody the player barely knew"
        );

        // Keyed on the id: nobody starts a life already being anything, so
        // "find the friend" would find nobody until one is made.
        String companion = "childhood_companion";

        assertNotNull(
                cast.get(companion), "the cast contained no companion at all"
        );

        // Warmed as far as warmth goes — and still not an heir.
        cast.changeRelationship(companion, 70, "a_life_together", "A life together.", 50);

        assertTrue(
                SuccessionService.candidates(household(), cast).isEmpty(),
                "the house went to a devoted friend the player never chose to teach"
        );

        assertTrue(
                cast.takeStudent("Idris", 50),
                "the player could not take a student at all"
        );

        List<Succession> heirs = SuccessionService.candidates(household(), cast);

        assertFalse(heirs.isEmpty(), "a student the player chose was left no way to inherit");

        for (Succession heir : heirs) {
            assertFalse(heir.blood(), "a student was recorded as blood");
            assertEquals("your student", heir.relation());
            assertEquals("Idris", heir.name(), "somebody other than the student inherited");
        }
    }

    @Test
    @DisplayName("a rival is never handed your house")
    void yourEnemiesDoNotInheritFromYou() {
        PlayerCharacter player = forebear(50, 50, 50, 50);
        RecurringCharacterRegistry cast = RecurringCharacterRegistry.createFor(player);

        String rivalId = "early_rival";

        assertNotNull(cast.get(rivalId), "the cast contained no rival at all");

        // They are nobody until they cross you, so make them cross you.
        cast.changeRelationship(rivalId, -40, "the_quarrel", "They crossed you.", 20);

        // Someone who spent your life fighting you is not close enough to be
        // handed the house, which the warmth bar settles on its own.
        for (Succession heir : SuccessionService.candidates(household(), cast)) {
            assertFalse(heir.id().equals(rivalId), "an open rival was offered your house");
        }

        // Making peace is supposed to change what they are: past 35 the
        // registry stops calling them a rival and calls them an ally, and an
        // ally may well inherit. That is the design, so pin it.
        cast.changeRelationship(rivalId, 95, "made_peace", "You made peace.", 60);

        for (var character : cast.all()) {
            if (character.getId().equals(rivalId)) {
                assertEquals(
                        com.example.al_mirath.model.RelationshipType.ALLY,
                        character.getRelationshipType(),
                        "a rival reconciled with is still recorded as a rival"
                );
            }
        }
    }

    /**
     * A save can hold a combination the live game never builds, because it
     * stores warmth and relationship type as separate fields. A rival who is
     * somehow warm on disk must still not be handed the house.
     */
    @Test
    @DisplayName("a rival loaded warm from a save is still not an heir")
    void theGuardHoldsForStatesOnlyASaveCanProduce() {
        PlayerCharacter player = forebear(50, 50, 50, 50);
        RecurringCharacterRegistry cast = RecurringCharacterRegistry.createFor(player);

        JSONObject saved = new JSONObject(cast.toJson().toString());
        JSONArray characters = saved.getJSONArray("characters");

        String rivalName = null;

        // Written into the save rather than found in it. Nobody is born a
        // rival now, and the point of this test is a combination the live
        // game cannot build: warmth and stance disagreeing on disk.
        for (int i = 0; i < characters.length(); i++) {
            JSONObject character = characters.getJSONObject(i);

            if ("early_rival".equals(character.optString("id"))) {
                character.put("relationshipType", "RIVAL");
                character.put("relationship", 80);
                rivalName = character.optString("name");
            }
        }

        assertNotNull(rivalName, "no rival was found in the saved cast");

        RecurringCharacterRegistry loaded = RecurringCharacterRegistry.fromJson(saved);

        for (Succession heir : SuccessionService.candidates(household(), loaded)) {
            assertFalse(
                    heir.name().equals(rivalName),
                    rivalName + " is recorded as a rival and was still offered the house"
            );
        }
    }

    // ---- what they inherit ----------------------------------------------

    private Succession child(String id, String name, int age) {
        return new Succession(id, name, "your child", age, "watchful", LifePath.SCHOLAR, true);
    }

    @Test
    @DisplayName("the heir is their own person, not a copy of the one before")
    void nothingPersonalCrossesOver() {
        PlayerCharacter brilliant = forebear(0, 100, 0, 0);

        PlayerCharacter heir = SuccessionService.heirOf(
                child("c1", "Zahra", 20), brilliant, null, new Random(1));

        assertTrue(
                heir.getEducation() < 70,
                "a great scholar's child was born already learned ("
                        + heir.getEducation() + "); learning is not inherited"
        );

        assertTrue(heir.getStress() <= 35, "the heir inherited the last life's exhaustion");
        assertTrue(heir.getHealth() >= 40, "the heir was born already dying");
    }

    @Test
    @DisplayName("what the house owns and is owed does cross over")
    void theEstateAndTheNameCarry() {
        PlayerCharacter rich = forebear(90, 40, 80, 70);

        PlayerCharacter blood = SuccessionService.heirOf(
                child("c1", "Anas", 22), rich, null, new Random(2));

        assertTrue(blood.getWealth() > 30, "an heir to a fortune started with nothing");
        assertTrue(blood.getReputation() > 20, "the house's name counted for nothing");
        assertTrue(blood.getPoliticalPower() > 10, "no part of the position carried");

        // And a student inherits a different half of it.
        Succession student = new Succession(
                "s1", "Idris", "your student", 25, "devoted", LifePath.UNDECIDED, false);

        PlayerCharacter taught = SuccessionService.heirOf(student, rich, null, new Random(2));

        assertTrue(
                taught.getWealth() < blood.getWealth(),
                "a student inherited as much of the money as a child did"
        );

        assertTrue(
                taught.getEducation() > blood.getEducation(),
                "a student inherited none of the teaching that made them the heir"
        );
    }

    @Test
    @DisplayName("the factions remember the house, but only half as strongly")
    void standingFadesRatherThanResettingOrPersisting() {
        FactionRelations adored = new FactionRelations(90, 90, 10, 90, 40, 90, 90, 10);
        FactionRelations inherited = SuccessionService.regardInheritedFrom(adored);

        assertTrue(
                inherited.getCourt() < adored.getCourt(),
                "the heir inherited the full force of a feeling they never earned"
        );

        assertTrue(
                inherited.getCourt() > 40,
                "a beloved house's heir starts as a total stranger"
        );

        assertTrue(
                inherited.getMilitary() > adored.getMilitary(),
                "a hated house's heir inherits the full hatred"
        );

        assertEquals(40, inherited.getMerchants(), "a neutral regard should stay neutral");
    }

    @Test
    @DisplayName("a name that travelled outlives the one who earned it")
    void renownIsInheritedAtHalfForce() {
        RenownRegistry earned = new RenownRegistry();

        earned.record("fed_people_during_riot");
        earned.spread(40, forebear(50, 50, 90, 50));

        Renown widelyKnown = null;

        for (Renown story : Renown.values()) {
            if (earned.reachOf(story) >= 50) {
                widelyKnown = story;
            }
        }

        assertNotNull(widelyKnown, "the probe never got a story to travel");

        RenownRegistry house = earned.inheritedByTheHouse();

        assertTrue(house.reachOf(widelyKnown) > 0, "a famous name died with its owner");

        assertTrue(
                house.reachOf(widelyKnown) < earned.reachOf(widelyKnown),
                "the heir is as famous for it as the one who did it"
        );

        // A deed nobody outside the room heard about does not become family lore.
        RenownRegistry private_ = new RenownRegistry();
        private_.record("framed_an_innocent_man");

        assertTrue(
                private_.inheritedByTheHouse().renownSummary().isBlank(),
                "a secret nobody repeated was inherited as though it were famous"
        );
    }

    // ---- the household reshapes -----------------------------------------

    @Test
    @DisplayName("the household reforms around whoever took the name")
    void everyonesPlaceIsRecomputed() {
        FamilyRegistry family = household(
                member("p1", "Amina", Kinship.MOTHER, 84, true, LifePath.UNDECIDED, ""),
                member("sp", "Rania", Kinship.SPOUSE, 60, true, LifePath.UNDECIDED, ""),
                member("c1", "Anas", Kinship.CHILD, 31, true, LifePath.COURTIER, ""),
                member("c2", "Zahra", Kinship.CHILD, 27, true, LifePath.SCHOLAR, ""),
                member("c3", "Hakim", Kinship.CHILD, 20, false, LifePath.SOLDIER, ""),
                member("g1", "Nur", Kinship.GRANDCHILD, 8, true, LifePath.UNDECIDED, "c1"),
                member("g2", "Sami", Kinship.GRANDCHILD, 6, true, LifePath.UNDECIDED, "c2")
        );

        assertTrue(family.succeedTo("c1"), "the heir was not found in their own household");

        List<String> names = new ArrayList<>();

        for (FamilyMember member : family.all()) {
            names.add(member.getName() + ":" + member.getKinship());
        }

        assertFalse(
                names.toString().contains("Anas"),
                "the heir is still listed as a member of their own household"
        );

        assertTrue(names.contains("Zahra:SIBLING"), "a co-child did not become a sibling: " + names);
        assertTrue(names.contains("Nur:CHILD"), "the heir's own child was not carried over: " + names);

        assertFalse(names.contains("Sami:CHILD"), "somebody else's child became the heir's: " + names);
        assertFalse(names.toString().contains("Amina"), "the forebear's parent stayed in the house");
        assertFalse(names.toString().contains("Rania"), "the forebear's spouse stayed in the house");
        assertFalse(names.toString().contains("Hakim"), "the dead stayed in the house");
    }

    @Test
    @DisplayName("someone who is not a descendant cannot be succeeded to")
    void theHouseholdRefusesAnImpossibleHeir() {
        FamilyRegistry family = household(
                member("sp", "Rania", Kinship.SPOUSE, 60, true, LifePath.UNDECIDED, "")
        );

        assertFalse(family.succeedTo("sp"));
        assertFalse(family.succeedTo("nobody"));
    }

    // ---- and it all hangs together --------------------------------------

    /** Plays a life out, whatever happens to it. */
    private void live(GameEngine engine, Random random) {
        while (engine.getCurrentEvent() != null && engine.getPlayer().isAlive()) {
            List<Choice> available = new ArrayList<>();

            for (Choice choice : engine.getCurrentEvent().getChoices()) {
                if (engine.canChoose(choice)) {
                    available.add(choice);
                }
            }

            if (available.isEmpty()) {
                return;
            }

            engine.applyChoice(available.get(random.nextInt(available.size())));
        }
    }

    /** A completed life that has somebody to hand the house to. */
    /**
     * A finished life with somebody to carry the house and somebody still
     * alive who knew them.
     *
     * <p>Both halves are needed by what this fixture is used for, and the
     * second is easy to forget: a life can end with three heirs and every
     * person it ever knew already buried, and then a test about which bonds
     * cross a succession has nothing to measure and fails for no reason.
     */
    private GameEngine aHouseWithAnHeir(Random random) {
        for (int attempt = 0; attempt < 200; attempt++) {
            GameEngine engine = new GameEngine();
            live(engine, random);

            if (engine.hasSuccessor() && someoneOutlivedThem(engine)) {
                return engine;
            }
        }

        return fail("in two hundred lives, nobody left an heir and a living face");
    }

    private boolean someoneOutlivedThem(GameEngine engine) {
        for (var character : engine.getRecurringCharacters().all()) {
            if (character.isAlive()) {
                return true;
            }
        }

        return false;
    }

    @Test
    @DisplayName("the world is inherited, not rebuilt")
    void continuingIsNotRestarting() {
        Random random = new Random(11);
        GameEngine founder = aHouseWithAnHeir(random);

        String cityBefore = founder.getCurrentCityName();
        String worldBefore = founder.getCities().whereYouAreSummary();

        // Made deliberately rather than hoped for: whether a random bot's
        // choices happened to leave somebody behind is not what this is about,
        // and relying on it made the test pass or fail on the roll. Applied to
        // everyone still alive, because naming one of them brought the same
        // problem back in a quieter form — the person named can be dead.
        for (var character : founder.getRecurringCharacters().all()) {
            if (character.isAlive()) {
                founder.getRecurringCharacters().changeRelationship(
                        character.getId(), 70, "a_life_together",
                        "You stood by them when nobody else would.", 40);
            }
        }

        List<String> knewThem = new ArrayList<>();

        for (var character : founder.getRecurringCharacters().all()) {
            if (character.isAlive() && Math.abs(character.getRelationship()) >= 40) {
                knewThem.add(character.getName());
            }
        }

        GameEngine heir = founder.succeedTo(founder.getSuccessors().get(0));

        assertNotNull(heir, "the house could not actually be handed on");

        assertEquals(
                cityBefore, heir.getCurrentCityName(),
                "the heir woke up in a different city than the one their house is in"
        );

        assertEquals(
                worldBefore, heir.getCities().whereYouAreSummary(),
                "the world was regenerated rather than inherited"
        );

        // The people the house has history with are still out there. This used
        // to assert the cast came across verbatim, which is how the heir ended
        // up being told they had rejected a mentor at nine — what crosses is a
        // standing between houses, not a copy of somebody else's address book.
        List<String> stillThere = new ArrayList<>();

        for (var character : heir.getRecurringCharacters().all()) {
            stillThere.add(character.getName());
        }

        for (String name : knewThem) {
            assertTrue(
                    stillThere.contains(name),
                    name + " mattered enough to the forebear to outlive them and "
                            + "the heir has never heard of them"
            );
        }

        assertFalse(
                knewThem.isEmpty(),
                "this life left nobody behind, so the test proves nothing"
        );

        // But the record of the last life is not the heir's record.
        assertEquals(
                "None", heir.getPlayer().getLegacyTitlesText(),
                "the heir was born already holding their forebear's titles"
        );

        assertTrue(
                heir.getWorldFlags().isEmpty(),
                "the heir inherited the last life's story flags: " + heir.getWorldFlags()
        );
    }

    @Test
    @DisplayName("the chronicle remembers every generation, and the house keeps its name")
    void theHouseIsWrittenDown() {
        Random random = new Random(13);
        GameEngine engine = aHouseWithAnHeir(random);

        String house = engine.getDynasty().houseName();
        String founderName = engine.getPlayer().getName();
        int founderAge = engine.getPlayer().getAge();

        assertEquals(1, engine.getDynasty().generation());

        GameEngine heir = engine.succeedTo(engine.getSuccessors().get(0));

        assertEquals(2, heir.getDynasty().generation(), "the generation did not advance");
        assertEquals(house, heir.getDynasty().houseName(), "the house changed its name");

        String chronicle = heir.getDynastyChronicle();

        assertTrue(chronicle.contains(founderName), "the founder is not in the chronicle");
        assertTrue(
                chronicle.contains(String.valueOf(founderAge)),
                "the chronicle does not say how old the founder was when they died"
        );

        assertEquals(1, heir.getDynasty().line().size());
    }

    @Test
    @DisplayName("an heir begins at the stage their age belongs to")
    void nobodyGrownIsSentBackToChildhood() {
        assertEquals(0, SuccessionService.stageIndexForAge(8));
        assertEquals(1, SuccessionService.stageIndexForAge(19));
        assertEquals(2, SuccessionService.stageIndexForAge(30));
        assertEquals(3, SuccessionService.stageIndexForAge(52));

        Random random = new Random(17);
        GameEngine engine = aHouseWithAnHeir(random);

        Succession eldest = engine.getSuccessors().get(0);
        GameEngine heir = engine.succeedTo(eldest);

        if (eldest.age() >= 25) {
            assertFalse(
                    "Childhood".equals(heir.getCurrentLifeStage()),
                    "a grown heir was put back into childhood events"
            );
        }
    }

    @Test
    @DisplayName("an heir who was never offered cannot be installed")
    void youCannotInventAnHeir() {
        Random random = new Random(19);
        GameEngine engine = aHouseWithAnHeir(random);

        assertNull(engine.succeedTo(null));

        assertNull(
                engine.succeedTo(new Succession(
                        "nobody", "Ghost", "your child", 30,
                        "watchful", LifePath.SCHOLAR, true)),
                "a made-up heir was allowed to take the house"
        );
    }

    @Test
    @DisplayName("the heir's life can actually be played, and saved")
    void theNextGenerationIsAPlayableLife() {
        Random random = new Random(23);
        GameEngine engine = aHouseWithAnHeir(random);

        GameEngine heir = engine.succeedTo(engine.getSuccessors().get(0));

        assertNotNull(heir.getCurrentEvent(), "the heir was handed a life with nothing in it");
        assertTrue(heir.getPlayer().isAlive());

        List<Choice> available = new ArrayList<>();

        for (Choice choice : heir.getCurrentEvent().getChoices()) {
            if (heir.canChoose(choice)) {
                available.add(choice);
            }
        }

        assertFalse(available.isEmpty(), "the heir's first scene offered them nothing to do");
        heir.applyChoice(available.get(0));

        GameEngine loaded = GameEngine.fromSaveJson(heir.toSaveJson());

        assertEquals(
                heir.getDynasty().generation(),
                loaded.getDynasty().generation(),
                "the generation was lost in the save"
        );

        assertEquals(
                heir.getDynasty().houseName(),
                loaded.getDynasty().houseName()
        );

        assertEquals(
                heir.getDynastyChronicle(),
                loaded.getDynastyChronicle(),
                "the chronicle of the house was lost in the save"
        );
    }

    @Test
    @DisplayName("a dynasty round-trips through its own json")
    void theChronicleSurvivesOnItsOwn() {
        Dynasty dynasty = new Dynasty("the House of Yusuf");

        dynasty.succeed(forebear(50, 50, 50, 50), "Lion of Baghdad", "A Long Reign");
        dynasty.succeed(forebear(20, 20, 20, 20), "", "Gone Before Your Name Was Made");

        Dynasty restored = Dynasty.fromJson(new JSONObject(dynasty.toJson().toString()));

        assertEquals(dynasty.houseName(), restored.houseName());
        assertEquals(dynasty.generation(), restored.generation());
        assertEquals(dynasty.chronicle(), restored.chronicle());
        assertEquals(3, restored.generation());
    }
}
