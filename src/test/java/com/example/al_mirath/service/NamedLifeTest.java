package com.example.al_mirath.service;

import com.example.al_mirath.model.PlayerCharacter;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * The one thing about a life the player decides before living it.
 *
 * <p>The name is not decoration. It is printed on the succession buttons, it
 * is what the house is called for every generation after the first, and it is
 * what the legacy records are filed under — so it has to reach the character
 * before the character is built, and it has to survive whatever gets typed
 * into a text field.
 */
class NamedLifeTest {

    private final CharacterGenerator generator = new CharacterGenerator();

    @Test
    @DisplayName("a life the player named is called what they called it")
    void theNameIsUsed() {
        PlayerCharacter named = generator.generateCharacter("Layla al-Warraq");

        assertEquals("Layla al-Warraq", named.getName());
    }

    @Test
    @DisplayName("naming yourself decides the name and nothing else about you")
    void everythingElseIsStillRolled() {
        boolean sawTwoEras = false;
        String firstEra = null;

        // The generator rolls era, origin, household and temperament. Over
        // twenty lives under one name at least two eras should turn up; if
        // naming yourself quietly fixed the rest of the roll, they would not.
        for (int life = 0; life < 20; life++) {
            PlayerCharacter named = generator.generateCharacter("Yusuf");

            assertEquals("Yusuf", named.getName());

            if (firstEra == null) {
                firstEra = named.getEra();
            } else if (!firstEra.equals(named.getEra())) {
                sawTwoEras = true;
            }
        }

        assertTrue(sawTwoEras, "naming a life appears to have fixed the rest of the roll");
    }

    @Test
    @DisplayName("a player who names nothing is still named")
    void blankNamesFallBackToTheRoll() {
        for (String nothing : new String[]{null, "", "   ", "\n\t "}) {
            PlayerCharacter rolled = generator.generateCharacter(nothing);

            assertFalse(
                    rolled.getName() == null || rolled.getName().isBlank(),
                    "a life submitted with no name came out nameless"
            );
        }
    }

    /**
     * The name goes onto buttons and into a scroll of fixed width, so it
     * cannot arrive carrying line breaks or run on for a paragraph. A pasted
     * essay is trimmed rather than refused: the player gets a life either way.
     */
    @Test
    @DisplayName("a name that would wreck the layout is cut down rather than refused")
    void absurdNamesAreTrimmed() {
        String essay = "Yusuf ".repeat(40);

        String trimmed = generator.nameOrRandom(essay);

        assertTrue(
                trimmed.length() <= 32,
                "a 240-character name reached the buttons at " + trimmed.length()
        );

        String multiline = "  Zaynab\nal-Katib\tthe\r\nScribe  ";

        String flattened = generator.nameOrRandom(multiline);

        assertFalse(
                flattened.contains("\n") || flattened.contains("\r") || flattened.contains("\t"),
                "a name with line breaks in it reached the buttons: '" + flattened + "'"
        );

        assertEquals("Zaynab al-Katib the Scribe", flattened);
    }

    /**
     * The reason the name is asked for before the life is generated rather
     * than applied afterwards: the house is named after whoever founded it,
     * and a house named for a stranger is the bug this ordering prevents.
     */
    @Test
    @DisplayName("the house is named after the player, not after the roll")
    void theNameReachesTheDynasty() {
        GameEngine engine = new GameEngine("Zaynab");

        assertEquals("Zaynab", engine.getPlayer().getName());

        assertTrue(
                engine.getHouseStyling().contains("Zaynab"),
                "the house was styled '" + engine.getHouseStyling()
                        + "', which is not named for the player who founded it"
        );
    }

    @Test
    @DisplayName("a life nobody named still founds a house")
    void anUnnamedLifeStillWorks() {
        GameEngine engine = new GameEngine((String) null);

        assertFalse(engine.getPlayer().getName().isBlank());

        assertTrue(
                engine.getHouseStyling().contains(engine.getPlayer().getName()),
                "the house was not named for the player who founded it"
        );
    }
}
