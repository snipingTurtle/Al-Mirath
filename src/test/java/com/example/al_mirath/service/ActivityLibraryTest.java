package com.example.al_mirath.service;

import com.example.al_mirath.minigame.MiniGameFactory;
import com.example.al_mirath.model.Activity;
import com.example.al_mirath.model.PlayerCharacter;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

/**
 * The activity catalogue, checked as content.
 *
 * <p>Everything here is the kind of mistake that costs nothing to make and is
 * invisible until a player hits it: a stat name with a typo in it silently
 * does nothing, a mini-game name with a typo in it silently becomes a random
 * game, and an activity nobody can ever reach is content that does not exist.
 */
class ActivityLibraryTest {

    private static final Set<String> STATS = Set.of(
            "health", "wealth", "education", "reputation",
            "politicalPower", "morality", "familyLoyalty", "stress");

    private static final Set<String> FACTIONS = Set.of(
            "court", "nobles", "military", "scholars",
            "merchants", "commonPeople", "familyCouncil", "shadowNetwork");

    @Test
    @DisplayName("every activity is named, described, and reachable")
    void theCatalogueIsWhole() {
        List<Activity> all = ActivityLibrary.all();

        assertTrue(all.size() >= 40,
                "only " + all.size() + " activities; a year's menu would be thin");

        Set<String> ids = new HashSet<>();

        for (Activity activity : all) {
            assertTrue(ids.add(activity.id()),
                    "two activities share the id " + activity.id());

            assertFalse(activity.name().isBlank(), activity.id() + " has no name");
            assertFalse(activity.description().isBlank(),
                    activity.id() + " has no description, so the menu row is half empty");

            assertTrue(activity.minAge() <= activity.maxAge(),
                    activity.id() + " can never be done: min age " + activity.minAge()
                            + " is past max age " + activity.maxAge());

            assertTrue(activity.minAge() < 60,
                    activity.id() + " only unlocks at " + activity.minAge()
                            + ", which most lives never reach");
        }
    }

    @Test
    @DisplayName("every stat and faction an activity touches actually exists")
    void nothingIsSpeltWrong() {
        List<String> wrong = new ArrayList<>();

        for (Activity activity : ActivityLibrary.all()) {
            for (Map<String, Integer> effects : List.of(
                    activity.successStatEffects(), activity.failureStatEffects())) {

                for (String stat : effects.keySet()) {
                    if (!STATS.contains(stat)) {
                        wrong.add(activity.id() + " moves a stat called '" + stat + "'");
                    }
                }
            }

            for (Map<String, Integer> effects : List.of(
                    activity.successFactionEffects(), activity.failureFactionEffects())) {

                for (String faction : effects.keySet()) {
                    if (!FACTIONS.contains(faction)) {
                        wrong.add(activity.id() + " moves a faction called '" + faction + "'");
                    }
                }
            }

            if (activity.requiredStat() != null && !STATS.contains(activity.requiredStat())) {
                wrong.add(activity.id() + " is gated on a stat called '"
                        + activity.requiredStat() + "'");
            }
        }

        assertTrue(wrong.isEmpty(),
                "these would silently do nothing at runtime: " + wrong);
    }

    @Test
    @DisplayName("every trial an activity asks for is a trial that exists")
    void everyTrialIsReal() {
        List<String> wrong = new ArrayList<>();

        for (Activity activity : ActivityLibrary.all()) {
            if (!activity.requiresMiniGame()) {
                continue;
            }

            if (!MiniGameFactory.TYPES.contains(activity.miniGameType())) {
                wrong.add(activity.id() + " asks for '" + activity.miniGameType() + "'");
            }

            assertTrue(activity.difficulty() >= 1 && activity.difficulty() <= 5,
                    activity.id() + " has difficulty " + activity.difficulty());
        }

        assertTrue(wrong.isEmpty(),
                "these would quietly fall back to a random challenge: " + wrong);
    }

    @Test
    @DisplayName("an activity that can fail says what failing looks like")
    void failureIsWritten() {
        for (Activity activity : ActivityLibrary.all()) {
            if (!activity.requiresMiniGame()) {
                continue;
            }

            assertFalse(activity.failureText() == null || activity.failureText().isBlank(),
                    activity.id() + " can be failed but has nothing to say about it");

            assertFalse(activity.successText().isBlank(),
                    activity.id() + " has nothing to say about succeeding either");
        }
    }

    @Test
    @DisplayName("a child is shown something to do, and so is an elder")
    void everyAgeHasAMenu() {
        for (int age : new int[]{5, 8, 12, 16, 22, 35, 50, 65, 80}) {
            PlayerCharacter player = characterAged(age);

            Map<String, List<Activity>> menu = ActivityLibrary.menuFor(player);

            int open = 0;

            for (List<Activity> section : menu.values()) {
                for (Activity activity : section) {
                    if (activity.lockedReason(player, false, false).isEmpty()) {
                        open++;
                    }
                }
            }

            assertTrue(open >= 3,
                    "at age " + age + " only " + open + " activities were open; "
                            + "the year has nothing in it");
        }
    }

    @Test
    @DisplayName("the menu never dangles an activity decades out of reach")
    void nothingIsShownThatIsYearsAway() {
        PlayerCharacter child = characterAged(6);

        for (List<Activity> section : ActivityLibrary.menuFor(child).values()) {
            for (Activity activity : section) {
                if (activity.minAge() > child.getAge() + 6) {
                    fail("a six-year-old is shown '" + activity.name()
                            + "', which unlocks at " + activity.minAge());
                }
            }
        }
    }

    @Test
    @DisplayName("every category the menu can show has something in it")
    void noCategoryIsEmpty() {
        Set<String> used = new HashSet<>();

        for (Activity activity : ActivityLibrary.all()) {
            used.add(activity.category());
        }

        for (String category : Activity.CATEGORY_ORDER) {
            assertTrue(used.contains(category),
                    "the menu has a '" + category + "' section with nothing in it");
        }
    }

    @Test
    @DisplayName("an activity that costs money says so before it is attempted")
    void thePriceIsOnTheRow() {
        PlayerCharacter pauper = characterAged(30);
        pauper.setNetWorth(0);

        for (Activity activity : ActivityLibrary.all()) {
            if (activity.wealthCost() <= 0) {
                continue;
            }

            String reason = activity.lockedReason(pauper, false, false);

            assertTrue(reason.contains(String.valueOf(activity.wealthCost())),
                    activity.id() + " costs " + activity.wealthCost()
                            + " and an empty purse is told: '" + reason + "'");
        }
    }

    @Test
    @DisplayName("an activity already spent this year says so rather than going quiet")
    void aSpentYearIsExplained() {
        PlayerCharacter player = characterAged(30);
        Activity pray = ActivityLibrary.byId("pray");

        assertEquals("", pray.lockedReason(player, false, false),
                "prayer should be open to a thirty-year-old");

        player.markSpentThisYear(pray.id());

        assertEquals("Already done this year.",
                pray.lockedReason(player, false, false));

        player.clearYearlyActivities();

        assertEquals("", pray.lockedReason(player, false, false),
                "a new year did not give the afternoon back");
    }

    private PlayerCharacter characterAged(int age) {
        return new PlayerCharacter(
                "Test", "Abbasid Era", "Merchant Guild", "Stable Household",
                "Patient", age, 70, 50, 50, 50, 50, 50, 50, 30);
    }
}
