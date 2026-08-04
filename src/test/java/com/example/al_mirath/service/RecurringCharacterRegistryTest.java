package com.example.al_mirath.service;

import com.example.al_mirath.model.PlayerCharacter;
import com.example.al_mirath.model.RecurringCharacter;
import org.junit.jupiter.api.Test;

import java.util.Random;

import static org.junit.jupiter.api.Assertions.*;

class RecurringCharacterRegistryTest {

    private PlayerCharacter createPlayer() {
        return new PlayerCharacter(
                "Test Character",
                "Abbasid Era",
                "Madrasa Student",
                "Stable Household",
                "Observant",
                8,
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
}