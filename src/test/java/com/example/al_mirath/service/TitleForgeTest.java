package com.example.al_mirath.service;

import com.example.al_mirath.model.Choice;
import com.example.al_mirath.model.EarnedTitle;
import com.example.al_mirath.model.FactionRelations;
import com.example.al_mirath.model.PlayerCharacter;
import com.example.al_mirath.model.WorldState;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.regex.Pattern;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

/**
 * Titles that are forged rather than shelved.
 *
 * <p>The game used to keep two dozen pre-written honorifics and hand you
 * whichever your stats unlocked, word for word, every run. The point of this
 * system is that the name comes out of the shape of the life: the arena it was
 * spent in picks the family of names, and then the era, a trait, or a second
 * stat picks which one. Two military heroes should not end up called the same
 * thing, and a life lived under the Umayyads should never be named for Cairo.
 */
class TitleForgeTest {

    private final TitleForge forge = new TitleForge();

    // ---- describing a life in the terms a title depends on ---------------

    /** A synthetic life, described only by the parts a title reads. */
    private static final class Life {

        private String era = "Abbasid Era";
        private String origin = "Scholar's Child";
        private String condition = "Stable Household";
        private String trait = "Patient";

        private int health = 50;
        private int wealth = 40;
        private int education = 40;
        private int reputation = 40;
        private int power = 30;
        private int morality = 50;
        private int loyalty = 50;
        private int stress = 30;

        private int court = 40;
        private int nobles = 40;
        private int military = 40;
        private int scholars = 40;
        private int merchants = 40;
        private int commonPeople = 40;
        private int familyCouncil = 40;
        private int shadows = 25;

        private final Set<String> flags = new HashSet<>();

        Life era(String value) { era = value; return this; }
        Life origin(String value) { origin = value; return this; }
        Life condition(String value) { condition = value; return this; }
        Life trait(String value) { trait = value; return this; }

        Life health(int value) { health = value; return this; }
        Life wealth(int value) { wealth = value; return this; }
        Life education(int value) { education = value; return this; }
        Life reputation(int value) { reputation = value; return this; }
        Life power(int value) { power = value; return this; }
        Life morality(int value) { morality = value; return this; }
        Life loyalty(int value) { loyalty = value; return this; }
        Life stress(int value) { stress = value; return this; }

        Life military(int value) { military = value; return this; }
        Life scholars(int value) { scholars = value; return this; }
        Life merchants(int value) { merchants = value; return this; }
        Life commonPeople(int value) { commonPeople = value; return this; }
        Life shadows(int value) { shadows = value; return this; }
        Life court(int value) { court = value; return this; }

        Life flag(String value) { flags.add(value); return this; }

        PlayerCharacter player() {
            return new PlayerCharacter(
                    "Test Character", era, origin, condition, trait, 40,
                    health, wealth, education, reputation,
                    power, morality, loyalty, stress
            );
        }

        FactionRelations factions() {
            return new FactionRelations(
                    court, nobles, military, scholars,
                    merchants, commonPeople, familyCouncil, shadows
            );
        }

        WorldState world() {
            WorldState world = new WorldState();
            world.addFlags(flags);
            return world;
        }
    }

    private List<EarnedTitle> titlesOf(Life life) {
        return forge.forge(life.player(), life.factions(), life.world());
    }

    private List<String> namesOf(Life life) {
        List<String> names = new ArrayList<>();

        for (EarnedTitle title : titlesOf(life)) {
            names.add(title.text());
        }

        return names;
    }

    /** The one name a life is led with. */
    private String crownOf(Life life) {
        EarnedTitle crown = forge.crowningTitle(titlesOf(life));

        return crown == null ? "" : crown.text();
    }

    /** A hero of the same war, over and over. */
    private Life warHero() {
        return new Life().military(72).reputation(65).health(60);
    }

    // ---- the name comes out of the life ----------------------------------

    @Test
    @DisplayName("the same heroism is named differently in different eras")
    void oneShelfOfNamesDoesNotCoverEveryEra() {
        Set<String> names = new LinkedHashSet<>();

        for (String era : List.of(
                "Umayyad Era", "Abbasid Era", "Mamluk Era", "Ottoman Era")) {

            names.add(crownOf(warHero().era(era)));
        }

        assertEquals(
                4, names.size(),
                "four eras produced " + names + "; the era is not reaching the "
                        + "name, so every run is called the same thing"
        );

        assertTrue(
                names.contains("Lion of Cairo"),
                "a Mamluk war hero should be named for Cairo, got " + names
        );

        assertFalse(
                crownOf(warHero().era("Umayyad Era")).contains("Cairo"),
                "a life lived under the Umayyads was named for Cairo"
        );
    }

    @Test
    @DisplayName("inside one arena, a second signal picks which name you get")
    void theArenaPicksTheFamilyAndADetailPicksTheName() {
        String valiant = crownOf(warHero());
        String cruel = crownOf(new Life().military(80).morality(15));
        String cunning =
                crownOf(new Life().military(72).reputation(40).education(60));

        Set<String> distinct = new LinkedHashSet<>(List.of(valiant, cruel, cunning));

        assertEquals(
                3, distinct.size(),
                "three soldiers who fought the same war but lived it "
                        + "differently were all called the same thing: " + distinct
        );
    }

    @Test
    @DisplayName("a life the world had no reason to name gets no name")
    void nothingEarnedIsNothingCalled() {
        assertTrue(
                titlesOf(new Life()).isEmpty(),
                "a life that cleared no threshold in any arena was still "
                        + "handed a title"
        );

        assertNull(forge.crowningTitle(List.of()));
    }

    @Test
    @DisplayName("the same life-state forges the same words twice")
    void theForgeDoesNotImproviseFreshEachTime() {
        Life life = new Life()
                .military(78).morality(20).wealth(80).merchants(85)
                .education(80).scholars(70).reputation(60);

        assertEquals(
                namesOf(life), namesOf(life),
                "the same life produced different names on a second look; a "
                        + "stat drifting a point would spray the record with "
                        + "near-duplicates"
        );
    }

    @Test
    @DisplayName("history leads with the highest-standing name")
    void theCrownOutranksEverythingElseEarned() {
        Life throne = new Life()
                .flag("took_throne").power(90).reputation(70)
                .military(75).education(80).scholars(70).wealth(80).merchants(85);

        List<EarnedTitle> earned = titlesOf(throne);

        assertTrue(earned.size() > 1, "expected a life with several names");

        EarnedTitle crown = forge.crowningTitle(earned);

        for (EarnedTitle title : earned) {
            assertTrue(
                    crown.prestige() >= title.prestige(),
                    "history led with " + crown + " over the higher-standing "
                            + title
            );
        }

        assertEquals(
                "Sovereign of Baghdad", crown.text(),
                "someone who took the throne of Abbasid Baghdad and kept the "
                        + "people's regard should be led with as its sovereign"
        );
    }

    @Test
    @DisplayName("no arena hands out two names at once")
    void oneNamePerArena() {
        Random random = new Random(11);

        for (int run = 0; run < 4000; run++) {
            List<EarnedTitle> earned = titlesOf(randomLife(random));

            Set<String> arenas = new HashSet<>();
            Set<String> names = new HashSet<>();

            for (EarnedTitle title : earned) {
                assertTrue(
                        arenas.add(title.domain()),
                        "one life was named twice for " + title.domain()
                                + ": " + earned
                );

                assertTrue(
                        names.add(title.text()),
                        "one life earned the name " + title + " twice"
                );
            }
        }
    }

    // ---- the words themselves --------------------------------------------

    /**
     * Every distinct name the forge can produce, found by sweeping lives
     * rather than by reading the source, so a name that can only be reached
     * through an impossible combination never gets counted.
     */
    private Set<String> everyNameTheForgeProduces() {
        Random random = new Random(7);
        Set<String> vocabulary = new LinkedHashSet<>();

        for (int run = 0; run < 30000; run++) {
            vocabulary.addAll(namesOf(randomLife(random)));
        }

        return vocabulary;
    }

    private Life randomLife(Random random) {
        Life life = new Life()
                .era(pick(random, "Umayyad Era", "Abbasid Era", "Mamluk Era",
                        "Ottoman Era"))
                .origin(pick(random, "Scholar's Child", "Cairo Merchant's Child",
                        "Baghdad Merchant's Child", "Poor Village Child"))
                .condition(pick(random, "Stable Household", "Disgraced Bloodline",
                        "Exiled Branch", "Recently Orphaned"))
                .trait(pick(random, "Cunning", "Brave", "Pious", "Ruthless",
                        "Patient"))
                .health(random.nextInt(101))
                .wealth(random.nextInt(101))
                .education(random.nextInt(101))
                .reputation(random.nextInt(101))
                .power(random.nextInt(101))
                .morality(random.nextInt(101))
                .loyalty(random.nextInt(101))
                .stress(random.nextInt(101))
                .military(random.nextInt(101))
                .scholars(random.nextInt(101))
                .merchants(random.nextInt(101))
                .commonPeople(random.nextInt(101))
                .shadows(random.nextInt(101))
                .court(random.nextInt(101));

        if (random.nextInt(100) < 20) {
            life.flag(random.nextBoolean() ? "took_throne" : "became_regent");
        }

        return life;
    }

    private String pick(Random random, String... options) {
        return options[random.nextInt(options.length)];
    }

    @Test
    @DisplayName("the forge has a wider vocabulary than the shelf it replaced")
    void thereAreMoreNamesThanTheOldFixedList() {
        Set<String> vocabulary = everyNameTheForgeProduces();

        assertTrue(
                vocabulary.size() >= 30,
                "the forge only ever produced " + vocabulary.size()
                        + " distinct names (" + vocabulary + "); it is a fixed "
                        + "shelf wearing a generator's clothes"
        );
    }

    @Test
    @DisplayName("every name it can produce reads like a title")
    void nothingComesOutMalformed() {
        for (String name : everyNameTheForgeProduces()) {
            assertFalse(name.isBlank(), "the forge produced a blank name");

            assertEquals(
                    name.trim(), name,
                    "'" + name + "' came out with loose whitespace"
            );

            assertFalse(
                    name.contains("  ") || name.contains("null"),
                    "'" + name + "' has a hole in it where a word should be"
            );

            assertFalse(
                    name.contains("The the") || name.contains("of of"),
                    "'" + name + "' doubled an article joining its parts"
            );

            assertTrue(
                    name.startsWith("The ") || name.contains(" of "),
                    "'" + name + "' does not read as an epithet"
            );
        }
    }

    @Test
    @DisplayName("no forged name assumes the player's gender")
    void theForgeDoesNotDecideWhoThePlayerIs() {
        Pattern gendered = Pattern.compile(
                "\\b(king|queen|emir|emira|sultan|sultana|lord|lady|prince"
                        + "|princess|patriarch|matriarch|father|mother|son"
                        + "|daughter|brother|sister|man|woman|his|her)\\b",
                Pattern.CASE_INSENSITIVE
        );

        for (String name : everyNameTheForgeProduces()) {
            assertFalse(
                    gendered.matcher(name).find(),
                    "'" + name + "' assumes a gender the character does not have"
            );
        }
    }

    // ---- and it reaches the player ---------------------------------------

    @Test
    @DisplayName("a forged name reaches the record the player actually reads")
    void theForgeReachesThePlayersScreen() {
        Random random = new Random(3);

        for (int run = 0; run < 60; run++) {
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

            String crown = engine.getEarnedTitle();

            if (crown.isBlank()) {
                continue;
            }

            assertTrue(
                    engine.getPlayer().getLegacyTitles().contains(crown),
                    "the engine styles the player '" + crown + "' but never "
                            + "wrote it onto their record"
            );

            assertTrue(
                    engine.getLifeSummary().contains("Known to history as: " + crown),
                    "the life summary never mentions what the life was called"
            );

            return;
        }

        fail("in sixty played lives the engine never forged a single title; "
                + "it is not calling the forge");
    }

    @Test
    @DisplayName("what a life was called survives a save")
    void theCrownIsStillThereAfterLoading() {
        Random random = new Random(9);

        for (int run = 0; run < 60; run++) {
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

            if (engine.getEarnedTitle().isBlank()) {
                continue;
            }

            GameEngine loaded = GameEngine.fromSaveJson(engine.toSaveJson());

            assertEquals(
                    engine.getEarnedTitle(),
                    loaded.getEarnedTitle(),
                    "the life was called something before the save and "
                            + "something else after it"
            );

            assertEquals(
                    engine.getPlayer().getLegacyTitlesText(),
                    loaded.getPlayer().getLegacyTitlesText()
            );

            return;
        }

        fail("sixty played lives earned no title to save");
    }

    @Test
    @DisplayName("a name once earned is not taken back when the stats drift")
    void titlesAccumulateRatherThanFlicker() {
        Life rich = new Life().wealth(80).merchants(85);

        String earned = crownOf(rich);
        assertNotEquals("", earned);

        // The same life after a bad year: the forge no longer offers the name,
        // but the record is append-only, which is what keeps it stable.
        assertTrue(titlesOf(new Life().wealth(20).merchants(85)).isEmpty());

        PlayerCharacter player = rich.player();
        assertTrue(player.addLegacyTitle(earned));
        assertFalse(
                player.addLegacyTitle(earned),
                "the same name was written onto the record twice"
        );
    }
}
