package com.example.al_mirath.service;

import com.example.al_mirath.model.GameEvent;
import com.example.al_mirath.model.NpcMemory;
import com.example.al_mirath.model.PlayerCharacter;
import com.example.al_mirath.model.RecurringCharacter;
import com.example.al_mirath.model.RelationshipType;
import com.example.al_mirath.model.RivalFeud;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.*;

/**
 * The rivalry has to do three things the plain relationship score cannot:
 * deepen in recognisable stages, let the rival act on its own initiative once
 * they have the standing to, and outlive the person who started it.
 */
class RivalSystemTest {

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

    /** A cast with no randomness left in it: nobody dies, nobody varies. */
    private RecurringCharacterRegistry castOf(int rivalRelationship) {
        RecurringCharacterRegistry registry =
                new RecurringCharacterRegistry(new Random(7));

        registry.generateInitialCast(createPlayer());

        RecurringCharacter rival = registry.get("early_rival");

        rival.changeRelationship(
                rivalRelationship - rival.getRelationship()
        );

        return registry;
    }

    /** Climbs a character to the rung their age would eventually earn them. */
    private void promoteTo(RecurringCharacter character, int age) {
        character.ageBy(age - character.getAge());

        while (character.advanceRoleForAge() != null) {
            // Each call moves at most one rung.
        }
    }

    private RecurringCharacter rivalAt(int relationship) {
        return new RecurringCharacter(
                "early_rival",
                "Yusuf",
                "A boy who was always half a step ahead of you.",
                "proud and unforgiving",
                RelationshipType.RIVAL,
                "Fellow Student",
                20,
                relationship,
                true
        );
    }

    private void wound(RecurringCharacter character, int count) {
        for (int i = 0; i < count; i++) {
            character.addMemory(
                    new NpcMemory(
                            "wound_" + i,
                            "You humiliated them again.",
                            15 + i,
                            -20
                    )
            );
        }
    }

    private boolean hasEventTitled(
            List<GameEvent> events,
            String fragment
    ) {
        return events.stream()
                .anyMatch(
                        event -> event.getTitle().contains(fragment)
                );
    }

    // ---- the feud ladder -------------------------------------------------

    @Test
    void ordinaryCompetitionIsNotAFeud() {
        assertEquals(
                RivalFeud.NONE,
                RivalFeud.of(rivalAt(-10)),
                "losing an argument is not a grudge"
        );

        assertEquals(
                RivalFeud.NONE,
                RivalFeud.of(rivalAt(30))
        );
    }

    @Test
    void theFeudDeepensWithTheScore() {
        assertEquals(RivalFeud.SLIGHTED, RivalFeud.of(rivalAt(-20)));
        assertEquals(RivalFeud.RESENTFUL, RivalFeud.of(rivalAt(-45)));
        assertEquals(RivalFeud.VENGEFUL, RivalFeud.of(rivalAt(-70)));
        assertEquals(RivalFeud.BLOOD_FEUD, RivalFeud.of(rivalAt(-90)));
    }

    @Test
    void repeatedHumiliationCompoundsTheFeud() {
        RecurringCharacter patient = rivalAt(-20);
        wound(patient, 2);

        assertEquals(
                RivalFeud.SLIGHTED,
                RivalFeud.of(patient),
                "two bad afternoons are still two bad afternoons"
        );

        RecurringCharacter humiliated = rivalAt(-20);
        wound(humiliated, 3);

        assertEquals(
                RivalFeud.RESENTFUL,
                RivalFeud.of(humiliated),
                "a pattern reads worse than the sum of its parts"
        );
    }

    @Test
    void kindnessIsNotAWound() {
        RecurringCharacter rival = rivalAt(-20);

        for (int i = 0; i < 5; i++) {
            rival.addMemory(
                    new NpcMemory(
                            "gift_" + i,
                            "You did them a good turn.",
                            20,
                            15
                    )
            );
        }

        assertEquals(0, rival.woundCount());
        assertEquals(RivalFeud.SLIGHTED, RivalFeud.of(rival));
    }

    @Test
    void theFeudCannotExceedItsWorstStage() {
        RecurringCharacter worst = rivalAt(-95);
        wound(worst, 6);

        assertEquals(RivalFeud.BLOOD_FEUD, RivalFeud.of(worst));
    }

    @Test
    void theDeadHoldNoGrudges() {
        RecurringCharacter rival = rivalAt(-90);
        rival.markDead();

        assertEquals(RivalFeud.NONE, RivalFeud.of(rival));
        assertEquals(RivalFeud.NONE, RivalFeud.of(null));
    }

    // ---- the rival acting on their own ----------------------------------

    @Test
    void aRivalWithoutStandingCannotReachYou() {
        RecurringCharacterRegistry registry = castOf(-70);

        List<GameEvent> events =
                RecurringCharacterEvents.create(registry);

        assertFalse(
                hasEventTitled(events, "The Hand Behind the Door"),
                "hatred without a position to abuse it changes nothing"
        );

        assertFalse(
                hasEventTitled(events, "Moves Against Your House")
        );
    }

    @Test
    void standingWithoutAGrudgeIsJustACareer() {
        RecurringCharacterRegistry registry = castOf(10);

        promoteTo(registry.get("early_rival"), 50);

        List<GameEvent> events =
                RecurringCharacterEvents.create(registry);

        assertFalse(
                hasEventTitled(events, "The Hand Behind the Door")
        );

        assertFalse(
                hasEventTitled(events, "Moves Against Your House")
        );
    }

    @Test
    void aResentfulRivalWithRankObstructsYou() {
        RecurringCharacterRegistry registry = castOf(-50);

        RecurringCharacter rival = registry.get("early_rival");
        promoteTo(rival, 32);

        assertEquals(RivalFeud.RESENTFUL, rival.feud());
        assertTrue(rival.getRoleRank() >= 2);

        List<GameEvent> events =
                RecurringCharacterEvents.create(registry);

        assertTrue(
                hasEventTitled(events, "The Hand Behind the Door")
        );

        assertFalse(
                hasEventTitled(events, "Moves Against Your House"),
                "resentment stops short of moving on your household"
        );
    }

    @Test
    void aVengefulRivalWithRankMovesAgainstYou() {
        RecurringCharacterRegistry registry = castOf(-70);

        RecurringCharacter rival = registry.get("early_rival");
        promoteTo(rival, 50);

        assertEquals(RivalFeud.VENGEFUL, rival.feud());
        assertTrue(rival.getRoleRank() >= 3);

        List<GameEvent> events =
                RecurringCharacterEvents.create(registry);

        assertTrue(
                hasEventTitled(events, "Moves Against Your House")
        );
    }

    @Test
    void retaliationIsSignedWithTheRankTheyHoldNow() {
        RecurringCharacterRegistry registry = castOf(-70);

        RecurringCharacter rival = registry.get("early_rival");
        promoteTo(rival, 50);

        GameEvent strike =
                RecurringCharacterEvents.create(registry)
                        .stream()
                        .filter(
                                event -> event.getTitle()
                                        .contains("Moves Against Your House")
                        )
                        .findFirst()
                        .orElseThrow();

        assertTrue(
                strike.getTitle().contains(rival.getName())
        );

        assertTrue(
                strike.getDescription()
                        .contains(rival.getCurrentRole().toLowerCase()),
                "the threat should name the office they are abusing"
        );
    }

    // ---- flags that used to fall through --------------------------------

    @Test
    void theFlagsTheRivalEventsEmitAllMoveTheRelationship() {
        record Case(String flag, int direction) { }

        List<Case> cases = List.of(
                new Case("npc_rival_counterattacked", -1),
                new Case("npc_rival_emboldened", -1),
                new Case("npc_rival_rejected_peace", -1),
                new Case("npc_rival_bypassed", -1),
                new Case("npc_rival_owes_protection", 1),
                new Case("npc_rival_struck_home", 1),
                new Case("npc_rival_house_withdrew", 1)
        );

        for (Case each : cases) {
            RecurringCharacterRegistry registry = castOf(-40);

            int before =
                    registry.get("early_rival").getRelationship();

            registry.applyStoryFlag(each.flag(), 30);

            int after =
                    registry.get("early_rival").getRelationship();

            assertEquals(
                    each.direction(),
                    Integer.signum(after - before),
                    each.flag() + " should move the rivalry"
            );

            assertTrue(
                    registry.get("early_rival").hasMemory(each.flag()),
                    each.flag() + " should leave a memory behind"
            );
        }
    }

    // ---- the feud outliving its owner -----------------------------------

    /** A cast aged past a death the registry cannot decline to roll. */
    private RecurringCharacterRegistry castWhereEveryoneDies(
            int rivalRelationship,
            int companionRelationship
    ) {
        RecurringCharacterRegistry registry =
                new RecurringCharacterRegistry(
                        new Random() {
                            @Override
                            public int nextInt(int bound) {
                                return 0;
                            }
                        }
                );

        registry.generateInitialCast(createPlayer());

        RecurringCharacter rival = registry.get("early_rival");
        rival.changeRelationship(
                rivalRelationship - rival.getRelationship()
        );

        RecurringCharacter companion =
                registry.get("childhood_companion");

        companion.changeRelationship(
                companionRelationship - companion.getRelationship()
        );

        // Old enough that the death chance is non-zero, which a Random
        // pinned to its floor then always takes.
        registry.ageEveryone(70);

        return registry;
    }

    @Test
    void aRivalsHeirIsBornIntoTheQuarrel() {
        RecurringCharacterRegistry registry =
                castWhereEveryoneDies(-70, 70);

        RecurringCharacter heir =
                registry.get("early_rival_descendant");

        assertNotNull(heir, "a strong feud should leave an heir");

        assertEquals(
                RelationshipType.RIVAL,
                heir.getRelationshipType(),
                "they were handed a side before they met you"
        );

        assertTrue(
                heir.getCurrentRole().contains("Quarrel"),
                "their role should say what they inherited"
        );

        assertTrue(heir.getRelationship() < 0);
    }

    @Test
    void aFriendsHeirIsNotBornOwingYouAnything() {
        RecurringCharacterRegistry registry =
                castWhereEveryoneDies(-70, 70);

        RecurringCharacter heir =
                registry.get("childhood_companion_descendant");

        assertNotNull(heir);

        assertEquals(
                RelationshipType.STRANGER,
                heir.getRelationshipType()
        );

        assertFalse(heir.getCurrentRole().contains("Quarrel"));
    }

    @Test
    void aRivalsHeirComesCollectingRatherThanAsking() {
        RecurringCharacterRegistry registry =
                castWhereEveryoneDies(-70, 70);

        List<GameEvent> events =
                RecurringCharacterEvents.create(registry);

        String rivalHeir =
                registry.get("early_rival_descendant").getName();

        String friendHeir =
                registry.get("childhood_companion_descendant").getName();

        assertTrue(
                hasEventTitled(events, rivalHeir + " Finishes It"),
                "the quarrel should still be running after its owner died"
        );

        assertFalse(
                hasEventTitled(events, rivalHeir + " Comes Asking"),
                "a rival's child does not arrive as a petitioner"
        );

        assertTrue(
                hasEventTitled(events, friendHeir + " Comes Asking")
        );
    }

    @Test
    void anHeirsFlagDoesNotLandOnTheOtherHeir() {
        RecurringCharacterRegistry registry =
                castWhereEveryoneDies(-70, 70);

        RecurringCharacter friendHeir =
                registry.get("childhood_companion_descendant");

        int untouched = friendHeir.getRelationship();

        registry.applyStoryFlag("npc_rival_heir_crushed", 60);

        assertTrue(
                registry.get("early_rival_descendant")
                        .hasMemory("npc_rival_heir_crushed"),
                "the flag should reach the heir it was written for"
        );

        assertEquals(
                untouched,
                friendHeir.getRelationship(),
                "settling a feud must not punish an unrelated child"
        );
    }

    @Test
    void aSettledRivalryLeavesNoQuarrelToInherit() {
        RecurringCharacterRegistry registry =
                castWhereEveryoneDies(60, 70);

        RecurringCharacter heir =
                registry.get("early_rival_descendant");

        assertNotNull(heir);

        assertNotEquals(
                RelationshipType.RIVAL,
                heir.getRelationshipType(),
                "a rival you made peace with dies with the feud settled"
        );
    }

    @Test
    void theFeudIsVisibleToThePlayer() {
        RecurringCharacterRegistry registry = castOf(-70);

        assertTrue(
                registry.relationshipSummary().contains("Vengeful"),
                "the People In Your Life screen should name the feud"
        );
    }
}
