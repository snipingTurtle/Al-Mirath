package com.example.al_mirath.service;

import com.example.al_mirath.model.FamilyMember;
import com.example.al_mirath.model.GameEvent;
import com.example.al_mirath.model.Kinship;
import com.example.al_mirath.model.LifePath;
import com.example.al_mirath.model.PlayerCharacter;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.*;

/**
 * The household.
 *
 * <p>The game already had a "family condition" — a phrase that nudged a few
 * stats at birth and then meant nothing for the rest of the run. These cover
 * the two things that make it a system instead: the household exists as
 * people who age and die, and what the player made of their life decides what
 * their children make of theirs.
 */
class FamilySystemTest {

    private PlayerCharacter player(String familyCondition) {
        return player(familyCondition, 8, 55, 45, 30, 55, 60, 20);
    }

    private PlayerCharacter player(
            String familyCondition,
            int age,
            int education,
            int reputation,
            int politicalPower,
            int morality,
            int familyLoyalty,
            int wealth
    ) {
        return new PlayerCharacter(
                "Test Character",
                "Abbasid Era",
                "Madrasa Student",
                familyCondition,
                "Observant",
                age,
                60,
                wealth,
                education,
                reputation,
                politicalPower,
                morality,
                familyLoyalty,
                20
        );
    }

    private FamilyRegistry householdOf(String condition, long seed) {
        FamilyRegistry family = new FamilyRegistry(new Random(seed));
        family.generateInitialFamily(player(condition));

        return family;
    }

    private List<FamilyMember> of(FamilyRegistry family, Kinship kinship) {
        List<FamilyMember> found = new ArrayList<>();

        for (FamilyMember member : family.all()) {
            if (member.getKinship() == kinship) {
                found.add(member);
            }
        }

        return found;
    }

    // ---- born into a household ------------------------------------------

    @Test
    @DisplayName("a run opens with parents rather than a phrase about them")
    void everyoneIsBornIntoAHousehold() {
        FamilyRegistry family = householdOf("Stable Household", 1);

        assertEquals(1, of(family, Kinship.FATHER).size());
        assertEquals(1, of(family, Kinship.MOTHER).size());

        for (FamilyMember parent : family.all()) {
            if (parent.getKinship().isElder()) {
                assertTrue(
                        parent.getAge() > 20,
                        "a parent should be a generation older than the player"
                );

                assertFalse(parent.getName().isBlank());
                assertFalse(parent.getTrait().isBlank());
            }
        }
    }

    @Test
    @DisplayName("an orphan's parents are on the sheet, and dead")
    void anOrphanStartsWithoutLivingParents() {
        FamilyRegistry family = householdOf("Recently Orphaned", 2);

        for (FamilyMember member : family.all()) {
            if (member.getKinship().isElder()) {
                assertFalse(
                        member.isAlive(),
                        "a recently orphaned player has living parents"
                );

                assertFalse(
                        member.getDeathReason().isBlank(),
                        "the household should say what happened to them"
                );
            }
        }
    }

    @Test
    @DisplayName("the family condition decides how close the household is")
    void theConditionIsNoLongerJustALabel() {
        int stable = warmthOf(householdOf("Stable Household", 3));
        int divided = warmthOf(householdOf("Family Divided by Rivalry", 3));

        assertTrue(
                stable > divided + 40,
                "a stable household (" + stable + ") should not feel like a "
                        + "divided one (" + divided + ")"
        );
    }

    private int warmthOf(FamilyRegistry family) {
        int total = 0;
        int counted = 0;

        for (FamilyMember member : family.all()) {
            total += member.getAffection();
            counted++;
        }

        return counted == 0 ? 0 : total / counted;
    }

    // ---- the household changing over a life ------------------------------

    /** Plays the household forward without needing a whole GameEngine. */
    private FamilyRegistry livedOut(PlayerCharacter person, long seed, int toAge) {
        FamilyRegistry family = new FamilyRegistry(new Random(seed));
        family.generateInitialFamily(person);

        while (person.getAge() < toAge) {
            person.setAge(person.getAge() + 2);
            family.advanceYears(2, person);
        }

        return family;
    }

    @Test
    @DisplayName("a household gathers a spouse, children and grandchildren")
    void theHouseholdGrowsAcrossALife() {
        int withSpouse = 0;
        int withChildren = 0;
        int withGrandchildren = 0;
        int runs = 60;

        for (int seed = 0; seed < runs; seed++) {
            FamilyRegistry family =
                    livedOut(player("Stable Household"), seed, 70);

            if (!of(family, Kinship.SPOUSE).isEmpty()) {
                withSpouse++;
            }

            if (!of(family, Kinship.CHILD).isEmpty()) {
                withChildren++;
            }

            if (!of(family, Kinship.GRANDCHILD).isEmpty()) {
                withGrandchildren++;
            }
        }

        assertTrue(withSpouse > runs / 2, "only " + withSpouse + " married");
        assertTrue(withChildren > runs / 2, "only " + withChildren + " had children");

        assertTrue(
                withGrandchildren > 0,
                "the name never reached a third generation"
        );
    }

    @Test
    @DisplayName("children arrive spaced out, not all in one burst")
    void birthsAreSpacedOut() {
        for (int seed = 0; seed < 40; seed++) {
            FamilyRegistry family =
                    livedOut(player("Stable Household"), seed, 70);

            List<Integer> ages = new ArrayList<>();

            for (FamilyMember child : of(family, Kinship.CHILD)) {
                ages.add(child.getAge());
            }

            ages.sort(null);

            for (int i = 1; i < ages.size(); i++) {
                assertTrue(
                        ages.get(i) - ages.get(i - 1) >= 2,
                        "two children born within a year of each other: " + ages
                );
            }
        }
    }

    @Test
    @DisplayName("the dead stay dead and stop ageing into the household")
    void deathIsRecordedWithAReason() {
        FamilyRegistry family =
                livedOut(player("Stable Household"), 7, 80);

        boolean sawADeath = false;

        for (FamilyMember member : family.all()) {
            if (!member.isAlive()) {
                sawADeath = true;

                assertFalse(
                        member.getDeathReason().isBlank(),
                        member.getName() + " died with no reason recorded"
                );
            }
        }

        assertTrue(sawADeath, "nobody died in eighty years");
    }

    // ---- what the children become ----------------------------------------

    private Map<LifePath, Integer> pathsFor(PlayerCharacter parent, int samples) {
        Map<LifePath, Integer> counts = new EnumMap<>(LifePath.class);
        Random random = new Random(11);

        for (int i = 0; i < samples; i++) {
            counts.merge(LifePath.decideFor(parent, random), 1, Integer::sum);
        }

        return counts;
    }

    @Test
    @DisplayName("a scholar's house produces more scholars than a soldier's")
    void theParentsLifeShapesTheChilds() {
        PlayerCharacter scholar =
                player("Stable Household", 40, 95, 40, 20, 60, 60, 20);

        PlayerCharacter politician =
                player("Stable Household", 40, 20, 60, 95, 60, 60, 20);

        int scholarsFromScholar =
                pathsFor(scholar, 400).getOrDefault(LifePath.SCHOLAR, 0);

        int scholarsFromPolitician =
                pathsFor(politician, 400).getOrDefault(LifePath.SCHOLAR, 0);

        assertTrue(
                scholarsFromScholar > scholarsFromPolitician * 2,
                "a life of study produced " + scholarsFromScholar
                        + " scholars against a life of politics producing "
                        + scholarsFromPolitician
        );
    }

    @Test
    @DisplayName("only a house already near power produces a ruler")
    void theThroneIsNotDriftedInto() {
        PlayerCharacter ordinary =
                player("Stable Household", 40, 50, 50, 50, 60, 60, 20);

        assertEquals(
                0,
                pathsFor(ordinary, 500).getOrDefault(LifePath.RULER, 0),
                "an ordinary household produced a ruler"
        );

        PlayerCharacter powerful =
                player("Stable Household", 40, 50, 85, 90, 60, 60, 20);

        assertTrue(
                pathsFor(powerful, 500).getOrDefault(LifePath.RULER, 0) > 0,
                "a household at the top of the court never produced one"
        );
    }

    @Test
    @DisplayName("a corrupt house is the only one that produces outlaws")
    void crueltyIsInherited() {
        PlayerCharacter upright =
                player("Stable Household", 40, 50, 50, 50, 90, 60, 20);

        assertEquals(
                0,
                pathsFor(upright, 500).getOrDefault(LifePath.CRIMINAL, 0),
                "an upright household produced an outlaw"
        );

        PlayerCharacter corrupt =
                player("Stable Household", 40, 50, 50, 50, 10, 60, 20);

        assertTrue(
                pathsFor(corrupt, 500).getOrDefault(LifePath.CRIMINAL, 0) > 50,
                "a house that taught cruelty produced no outlaws"
        );
    }

    @Test
    @DisplayName("siblings take a life of their own; parents do not")
    void whoGetsALifePath() {
        assertTrue(Kinship.SIBLING.takesALifePath());
        assertTrue(Kinship.CHILD.takesALifePath());
        assertTrue(Kinship.GRANDCHILD.takesALifePath());

        assertFalse(Kinship.FATHER.takesALifePath());
        assertFalse(Kinship.MOTHER.takesALifePath());
        assertFalse(Kinship.SPOUSE.takesALifePath());
    }

    // ---- the player's hand on it -----------------------------------------

    @Test
    @DisplayName("steering a child settles their path before the roll ever runs")
    void steeringOverridesTheStatRoll() {
        FamilyRegistry family = householdOf("Stable Household", 5);
        PlayerCharacter parent = player("Stable Household");

        // Grow the household until there is a child old enough to steer.
        while (family.livingChildren().isEmpty() && parent.getAge() < 60) {
            parent.setAge(parent.getAge() + 2);
            family.advanceYears(2, parent);
        }

        assertFalse(
                family.livingChildren().isEmpty(),
                "no child was ever born to steer"
        );

        FamilyMember child = family.livingChildren().get(0);
        child.setLifePath(LifePath.UNDECIDED);

        family.applyStoryFlag("family_child_to_scholar");

        assertEquals(
                LifePath.SCHOLAR,
                child.getLifePath(),
                "the child the event was about kept drifting to a stat roll"
        );
    }

    @Test
    @DisplayName("family flags move the household's feeling")
    void flagsReachTheHousehold() {
        FamilyRegistry family = householdOf("Stable Household", 6);

        int before = warmthOf(family);

        family.applyStoryFlag("family_abandoned");

        assertTrue(
                warmthOf(family) < before,
                "abandoning the household changed nothing"
        );

        FamilyRegistry other = householdOf("Stable Household", 6);
        int siblingBefore = of(other, Kinship.SIBLING).isEmpty()
                ? 0
                : of(other, Kinship.SIBLING).get(0).getAffection();

        other.applyStoryFlag("family_sibling_helped");

        if (!of(other, Kinship.SIBLING).isEmpty()) {
            assertTrue(
                    of(other, Kinship.SIBLING).get(0).getAffection() > siblingBefore,
                    "helping a sibling did not register with them"
            );
        }
    }

    // ---- events -----------------------------------------------------------

    @Test
    @DisplayName("the household only raises questions about people who exist")
    void eventsNeedTheirPeople() {
        FamilyRegistry newborn = householdOf("Stable Household", 8);

        for (GameEvent event : FamilyEvents.create(newborn)) {
            assertFalse(
                    event.getTitle().contains("Will Become"),
                    "a childless household asked what its child would become"
            );
        }

        assertTrue(
                FamilyEvents.create(new FamilyRegistry(new Random(1))).isEmpty(),
                "an empty household still generated events"
        );
    }

    @Test
    @DisplayName("every family event is well formed and uniquely titled")
    void familyEventsAreWellFormed() {
        FamilyRegistry family =
                livedOut(player("Stable Household"), 12, 70);

        java.util.Set<String> titles = new java.util.HashSet<>();

        for (GameEvent event : FamilyEvents.create(family)) {
            assertFalse(event.getTitle().isBlank());
            assertFalse(event.getDescription().isBlank());
            assertFalse(event.getChoices().isEmpty());

            assertTrue(
                    titles.add(event.getTitle()),
                    "duplicate family event title: " + event.getTitle()
            );
        }
    }

    // ---- persistence ------------------------------------------------------

    @Test
    @DisplayName("the household survives a save and load")
    void householdRoundTripsThroughJson() {
        FamilyRegistry family =
                livedOut(player("Stable Household"), 13, 70);

        FamilyRegistry restored = FamilyRegistry.fromJson(family.toJson());

        assertEquals(family.all().size(), restored.all().size());

        for (FamilyMember member : family.all()) {
            FamilyMember copy = restored.get(member.getId());

            assertNotNull(copy, member.getName() + " was lost in the save");
            assertEquals(member.getName(), copy.getName());
            assertEquals(member.getKinship(), copy.getKinship());
            assertEquals(member.getAge(), copy.getAge());
            assertEquals(member.getAffection(), copy.getAffection());
            assertEquals(member.isAlive(), copy.isAlive());
            assertEquals(member.getLifePath(), copy.getLifePath());
            assertEquals(member.getDeathReason(), copy.getDeathReason());
        }
    }

    @Test
    @DisplayName("the household reaches the player through the engine")
    void theEngineExposesTheHousehold() {
        GameEngine engine = new GameEngine();

        assertFalse(
                engine.getFamily().all().isEmpty(),
                "a new life started with no family at all"
        );

        assertFalse(
                engine.getHouseholdSummary().isBlank(),
                "the Household panel would be empty"
        );

        // Names, not counts: a save that drops the household regenerates one
        // of the same shape, so counting alone proves nothing.
        List<String> before = new ArrayList<>();

        for (FamilyMember member : engine.getFamily().all()) {
            before.add(member.getId() + ":" + member.getName());
        }

        String saved = engine.toSaveJson();
        GameEngine loaded = GameEngine.fromSaveJson(saved);

        List<String> after = new ArrayList<>();

        for (FamilyMember member : loaded.getFamily().all()) {
            after.add(member.getId() + ":" + member.getName());
        }

        assertEquals(
                before,
                after,
                "the household did not survive a save; it was regenerated"
        );
    }
}
