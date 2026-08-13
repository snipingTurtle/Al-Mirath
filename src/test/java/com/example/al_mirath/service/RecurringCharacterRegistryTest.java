package com.example.al_mirath.service;

import com.example.al_mirath.model.PlayerCharacter;
import com.example.al_mirath.model.RecurringCharacter;
import com.example.al_mirath.model.RelationshipType;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.*;

class RecurringCharacterRegistryTest {

    private static final List<String> SOLDIER_LADDER = List.of(
            "Childhood Companion",
            "Conscript Soldier",
            "Company Captain",
            "Garrison Commander",
            "General"
    );

    private PlayerCharacter createPlayer() {
        return createPlayer(8);
    }

    private PlayerCharacter createPlayer(int age) {
        return new PlayerCharacter(
                "Test Character",
                "Abbasid Era",
                "Madrasa Student",
                "Stable Household",
                "Observant",
                age,
                60,
                40,
                55,
                45,
                30,
                55,
                60,
                20
        );
    }

    /**
     * Ages are the only input to a role climb, so a Random that always returns
     * its floor makes both the cast's ages and their deaths predictable.
     */
    private Random alwaysLowest() {
        return new Random() {
            @Override
            public int nextInt(int bound) {
                return 0;
            }

            @Override
            public boolean nextBoolean() {
                return true;
            }
        };
    }

    private RecurringCharacter climber(int age, int relationship) {
        return new RecurringCharacter(
                "test_climber",
                "Hasan",
                "A child from the same district.",
                "loyal but proud",
                RelationshipType.FRIEND,
                SOLDIER_LADDER.get(0),
                age,
                relationship,
                true,
                SOLDIER_LADDER,
                0,
                ""
        );
    }

    @Test
    void generatesThreeCoreCharacters() {
        RecurringCharacterRegistry registry =
                new RecurringCharacterRegistry(
                        new Random(1)
                );

        registry.generateInitialCast(
                createPlayer()
        );

        assertEquals(
                3,
                registry.all().size()
        );

        assertNotNull(
                registry.get("childhood_companion")
        );

        assertNotNull(
                registry.get("early_rival")
        );

        assertNotNull(
                registry.get("elder_mentor")
        );
    }

    @Test
    void relationshipsAndMemoriesChangeTogether() {
        RecurringCharacterRegistry registry =
                new RecurringCharacterRegistry(
                        new Random(2)
                );

        registry.generateInitialCast(
                createPlayer()
        );

        RecurringCharacter companion =
                registry.get(
                        "childhood_companion"
                );

        int startingRelationship =
                companion.getRelationship();

        registry.changeRelationship(
                "childhood_companion",
                15,
                "protected_secret",
                "The player protected a childhood secret.",
                10
        );

        assertEquals(
                startingRelationship + 15,
                companion.getRelationship()
        );

        assertTrue(
                companion.hasMemory(
                        "protected_secret"
                )
        );
    }

    @Test
    void jsonRoundTripPreservesCharacters() {
        RecurringCharacterRegistry registry =
                new RecurringCharacterRegistry(
                        new Random(3)
                );

        registry.generateInitialCast(
                createPlayer()
        );

        registry.changeRelationship(
                "early_rival",
                -20,
                "public_humiliation",
                "The rivalry became personal.",
                17
        );

        RecurringCharacterRegistry restored =
                RecurringCharacterRegistry.fromJson(
                        registry.toJson()
                );

        assertEquals(
                registry.all().size(),
                restored.all().size()
        );

        assertEquals(
                registry.get("early_rival")
                        .getRelationship(),

                restored.get("early_rival")
                        .getRelationship()
        );

        assertTrue(
                restored.get("early_rival")
                        .hasMemory(
                                "public_humiliation"
                        )
        );
    }

    @Test
    void eventsContainGeneratedNames() {
        RecurringCharacterRegistry registry =
                new RecurringCharacterRegistry(
                        new Random(4)
                );

        registry.generateInitialCast(
                createPlayer()
        );

        String friendName =
                registry.get(
                        "childhood_companion"
                ).getName();

        boolean foundName =
                RecurringCharacterEvents
                        .create(registry)
                        .stream()
                        .anyMatch(
                                event ->
                                        event.getTitle()
                                                .contains(friendName)
                                                ||
                                                event.getDescription()
                                                        .contains(friendName)
                        );

        assertTrue(foundName);
    }

    @Test
    void roleAdvancesOnlyWhenTheYearsEarnIt() {
        RecurringCharacter hasan = climber(8, 60);

        assertNull(
                hasan.advanceRoleForAge(),
                "a child has not earned a title yet"
        );

        hasan.ageBy(8);

        assertEquals(
                "Conscript Soldier",
                hasan.advanceRoleForAge()
        );

        hasan.ageBy(44);

        assertEquals(
                "General",
                hasan.advanceRoleForAge()
        );

        assertNull(
                hasan.advanceRoleForAge(),
                "the ladder ends at its final rung"
        );
    }

    @Test
    void aBondTooWeakToMatterStopsShortOfTheFinalRung() {
        RecurringCharacter stranger = climber(70, 10);

        assertEquals(
                "Garrison Commander",
                stranger.advanceRoleForAge()
        );
    }

    @Test
    void theCastClimbsAsThePlayerAges() {
        RecurringCharacterRegistry registry =
                new RecurringCharacterRegistry(
                        new Random(5)
                );

        registry.generateInitialCast(
                createPlayer()
        );

        RecurringCharacter companion =
                registry.get("childhood_companion");

        List<String> announcements =
                registry.ageEveryone(10);

        assertTrue(
                companion.getRoleRank() > 0,
                "a companion who reached sixteen should hold a new title"
        );

        assertTrue(
                announcements.stream()
                        .anyMatch(line ->
                                line.contains(companion.getName())),
                "a change worth seeing should be announced"
        );
    }

    @Test
    void deathEndsProgressionAndLeavesAnHeirBehindAStrongBond() {
        RecurringCharacterRegistry registry =
                new RecurringCharacterRegistry(
                        alwaysLowest()
                );

        registry.generateInitialCast(
                createPlayer(50)
        );

        RecurringCharacter companion =
                registry.get("childhood_companion");

        registry.changeRelationship(
                "childhood_companion",
                40,
                "saved_me",
                "You pulled them out of the river.",
                20
        );

        registry.ageEveryone(1);

        assertFalse(
                companion.isAlive(),
                "a character past the age floor should die on a certain roll"
        );

        RecurringCharacter heir =
                registry.get("childhood_companion_descendant");

        assertNotNull(heir);
        assertEquals(companion.getName(), heir.getParentName());
        assertTrue(heir.isDescendant());

        assertEquals(
                companion.getRelationship() / 2,
                heir.getRelationship(),
                "an heir inherits half of what the player earned"
        );

        assertTrue(
                heir.hasMemory("saved_me"),
                "an heir carries the memory that mattered most"
        );

        int ageAtDeath = companion.getAge();
        String roleAtDeath = companion.getCurrentRole();

        registry.ageEveryone(20);

        assertEquals(ageAtDeath, companion.getAge());
        assertEquals(roleAtDeath, companion.getCurrentRole());
    }

    @Test
    void jsonRoundTripPreservesRolesAndLineage() {
        RecurringCharacterRegistry registry =
                new RecurringCharacterRegistry(
                        alwaysLowest()
                );

        registry.generateInitialCast(
                createPlayer(50)
        );

        registry.changeRelationship(
                "childhood_companion",
                40,
                "saved_me",
                "You pulled them out of the river.",
                20
        );

        registry.ageEveryone(1);

        RecurringCharacterRegistry restored =
                RecurringCharacterRegistry.fromJson(
                        registry.toJson()
                );

        RecurringCharacter companion =
                registry.get("childhood_companion");

        RecurringCharacter restoredCompanion =
                restored.get("childhood_companion");

        assertFalse(restoredCompanion.isAlive());

        assertEquals(
                companion.getCurrentRole(),
                restoredCompanion.getCurrentRole()
        );

        assertEquals(
                companion.getRoleLadder(),
                restoredCompanion.getRoleLadder()
        );

        assertEquals(
                companion.getRoleRank(),
                restoredCompanion.getRoleRank()
        );

        RecurringCharacter restoredHeir =
                restored.get("childhood_companion_descendant");

        assertNotNull(restoredHeir);

        assertEquals(
                companion.getName(),
                restoredHeir.getParentName()
        );

        assertTrue(
                restoredHeir.hasMemory("saved_me")
        );
    }
}