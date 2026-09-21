package com.example.al_mirath.service;

import com.example.al_mirath.model.LifeLogEntry;
import com.example.al_mirath.model.PlayerCharacter;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;

/**
 * The small weather of a life.
 *
 * <p>Between the scenes the player is asked to decide, a year still has to
 * contain something. These are the things that happen <em>to</em> a character:
 * a fever, a good harvest, a cousin's wedding, a horse that stands on your
 * foot. None of them is worth a decision, and a life without any of them
 * reads as a spreadsheet with a name on it.
 *
 * <p>Effects are deliberately tiny. The point is texture, not balance — if one
 * of these ever decides a run, it is doing something it was not built for.
 */
public final class YearlyLifeEvents {

    /** What a character's circumstances must be for a moment to be possible. */
    public enum Need {
        ANY,
        CHILD,
        GROWN,
        ELDER,
        EMPLOYED,
        IDLE,
        MONEYED,
        POOR,
        HAS_CHILD,
        HAS_PROPERTY,
        STRAINED,
        AILING
    }

    /**
     * One thing that can happen in a year.
     *
     * @param text    the log line, written as something that happened to you
     * @param minAge  earliest age it makes sense
     * @param maxAge  latest age it makes sense
     * @param need    a circumstance it requires
     * @param effects stat nudges, all small
     * @param tone    how the line reads in the log
     */
    public record Moment(String text, int minAge, int maxAge, Need need,
                         Map<String, Integer> effects, LifeLogEntry.Tone tone) {
    }

    private static final Random RANDOM = new Random();

    private YearlyLifeEvents() {
    }

    private static Moment good(String text, int minAge, int maxAge, Need need,
                               Map<String, Integer> effects) {
        return new Moment(text, minAge, maxAge, need, effects, LifeLogEntry.Tone.GOOD);
    }

    private static Moment bad(String text, int minAge, int maxAge, Need need,
                              Map<String, Integer> effects) {
        return new Moment(text, minAge, maxAge, need, effects, LifeLogEntry.Tone.BAD);
    }

    private static Moment plain(String text, int minAge, int maxAge, Need need,
                                Map<String, Integer> effects) {
        return new Moment(text, minAge, maxAge, need, effects, LifeLogEntry.Tone.NEUTRAL);
    }

    private static final List<Moment> MOMENTS = List.of(

            // ── Childhood ───────────────────────────────────────────────
            bad("You came down with a fever that went through the whole quarter.",
                    1, 14, Need.ANY, Map.of("health", -4)),
            good("You grew a hand's breadth over the summer and ate like a siege.",
                    3, 16, Need.ANY, Map.of("health", 3)),
            plain("You spent the year following an older cousin around, being tolerated.",
                    4, 12, Need.ANY, Map.of("familyLoyalty", 2)),
            good("A neighbour taught you to swim in the canal, badly and then well.",
                    5, 14, Need.ANY, Map.of("health", 3, "stress", -3)),
            bad("You broke your arm falling off a wall you had been told about.",
                    4, 15, Need.CHILD, Map.of("health", -5, "stress", 3)),
            good("You were given a puppy. It is not a good dog, but it is yours.",
                    4, 14, Need.ANY, Map.of("stress", -5)),
            plain("You learned to count in the bazaar, faster than you learned it at school.",
                    6, 13, Need.ANY, Map.of("education", 2, "wealth", 1)),
            bad("A teacher decided you were insolent, and spent the year proving it.",
                    6, 15, Need.ANY, Map.of("stress", 5, "education", -1)),

            // ── Youth ───────────────────────────────────────────────────
            good("You made a friend this year who will still be one in thirty years.",
                    12, 28, Need.ANY, Map.of("stress", -5, "reputation", 2)),
            bad("You were robbed in the street and gave up the purse without a fight.",
                    12, 60, Need.ANY, Map.of("stress", 5, "wealth", -2)),
            good("You were noticed. You are not sure by whom, but doors opened a little.",
                    15, 45, Need.ANY, Map.of("reputation", 3)),
            bad("An argument in a courtyard turned into something the quarter discussed.",
                    14, 45, Need.ANY, Map.of("reputation", -3, "stress", 4)),
            plain("You spent the year in love with somebody who did not know.",
                    14, 32, Need.ANY, Map.of("stress", 3, "morality", 1)),
            good("You walked to the coast and back with two friends and no plan.",
                    14, 35, Need.ANY, Map.of("health", 3, "stress", -7)),

            // ── Work and money ──────────────────────────────────────────
            good("A good year in your trade. You were given the difficult work, which is praise.",
                    16, 70, Need.EMPLOYED, Map.of("reputation", 3, "stress", 2)),
            bad("A rival in your trade got the credit for something that was yours.",
                    18, 70, Need.EMPLOYED, Map.of("stress", 6, "reputation", -2)),
            good("You were asked to settle a dispute between two men older than you.",
                    22, 75, Need.EMPLOYED, Map.of("reputation", 4, "politicalPower", 2)),
            bad("A long stretch of nothing. You ate into what you had saved.",
                    16, 70, Need.IDLE, Map.of("stress", 6, "wealth", -3)),
            bad("You were taxed twice for the same year and could prove nothing.",
                    18, 75, Need.MONEYED, Map.of("stress", 5, "wealth", -2)),
            good("Somebody repaid an old loan you had written off entirely.",
                    20, 80, Need.ANY, Map.of("wealth", 3, "stress", -3)),
            bad("You went to bed hungry more nights this year than you will admit.",
                    10, 80, Need.POOR, Map.of("health", -4, "stress", 6)),
            good("A patron sent a gift at the feast, unprompted and expensive.",
                    20, 80, Need.MONEYED, Map.of("wealth", 3, "reputation", 2)),

            // ── Household ───────────────────────────────────────────────
            good("Your household had a year with nothing in it worth recording, which is the best kind.",
                    18, 90, Need.ANY, Map.of("stress", -6, "familyLoyalty", 3)),
            bad("A quarrel at home that nobody started and nobody would end.",
                    16, 90, Need.ANY, Map.of("familyLoyalty", -4, "stress", 5)),
            good("One of your children said something so exactly like you that the room stopped.",
                    22, 90, Need.HAS_CHILD, Map.of("familyLoyalty", 5, "stress", -4)),
            bad("One of your children was ill through the winter, and you did not sleep.",
                    22, 80, Need.HAS_CHILD, Map.of("stress", 8, "health", -2)),
            good("You mended something in the house yourself and were absurdly proud of it.",
                    18, 90, Need.HAS_PROPERTY, Map.of("stress", -4)),
            bad("The roof went in the winter rains and the repair cost more than the roof.",
                    18, 90, Need.HAS_PROPERTY, Map.of("wealth", -3, "stress", 4)),

            // ── The city and the world ──────────────────────────────────
            plain("The price of bread rose and everyone talked about nothing else.",
                    8, 90, Need.ANY, Map.of("stress", 3)),
            good("A good harvest. Even the people who complain had less to complain about.",
                    8, 90, Need.ANY, Map.of("stress", -4, "health", 2)),
            bad("A fire took three houses on your street. Yours was not one of them.",
                    8, 90, Need.ANY, Map.of("stress", 5)),
            good("A famous teacher passed through and you heard him speak for nothing.",
                    12, 80, Need.ANY, Map.of("education", 3)),
            bad("A funeral you did not expect to attend, for someone younger than you.",
                    16, 90, Need.ANY, Map.of("stress", 6, "morality", 2)),
            plain("You sat on your roof in the evenings all summer, and did not think about very much.",
                    14, 90, Need.ANY, Map.of("stress", -5)),

            // ── Strain and decline ──────────────────────────────────────
            bad("You could not sleep for most of the year, and it showed in everything.",
                    16, 90, Need.STRAINED, Map.of("health", -4, "stress", 3)),
            good("You made yourself stop for a season, and it worked better than you expected.",
                    18, 90, Need.STRAINED, Map.of("stress", -10)),
            bad("The cough came back with the cold, and stayed past the spring.",
                    30, 95, Need.AILING, Map.of("health", -5)),
            good("A physician gave you advice you actually took.",
                    25, 95, Need.AILING, Map.of("health", 5, "stress", -3)),

            // ── Age ─────────────────────────────────────────────────────
            plain("Somebody asked your advice this year purely because you were old, which was new.",
                    52, 100, Need.ELDER, Map.of("reputation", 2)),
            bad("Your knees told you the weather before the sky did.",
                    50, 100, Need.ELDER, Map.of("health", -3)),
            good("You were asked to speak at a wedding, and did it well.",
                    45, 100, Need.GROWN, Map.of("reputation", 3, "stress", -4)),
            plain("You began to notice that the people who remembered the same things you did were fewer.",
                    58, 100, Need.ELDER, Map.of("stress", 3, "morality", 1)),
            good("You slept well, ate what you liked, and troubled nobody all year.",
                    55, 110, Need.ELDER, Map.of("stress", -7, "health", 1))
    );

    /**
     * Rolls one moment for the year, or none.
     *
     * @param hasChild whether the household has a living child
     * @return a moment, or null when the year holds nothing in particular
     */
    public static Moment rollFor(PlayerCharacter player, boolean hasChild) {
        if (player == null || RANDOM.nextInt(100) >= 62) {
            return null;
        }

        List<Moment> candidates = new ArrayList<>();

        for (Moment moment : MOMENTS) {
            if (player.getAge() < moment.minAge() || player.getAge() > moment.maxAge()) {
                continue;
            }

            if (!satisfies(moment.need(), player, hasChild)) {
                continue;
            }

            candidates.add(moment);
        }

        if (candidates.isEmpty()) {
            return null;
        }

        return candidates.get(RANDOM.nextInt(candidates.size()));
    }

    private static boolean satisfies(Need need, PlayerCharacter player, boolean hasChild) {
        return switch (need) {
            case ANY -> true;
            case CHILD -> player.getAge() < 16;
            case GROWN -> player.getAge() >= 18;
            case ELDER -> player.getAge() >= 50;
            case EMPLOYED -> player.isEmployed();
            case IDLE -> !player.isEmployed() && player.getAge() >= 16 && !player.isRetired();
            case MONEYED -> player.getNetWorth() >= 800;
            case POOR -> player.getNetWorth() < 60;
            case HAS_CHILD -> hasChild;
            case HAS_PROPERTY -> !player.getProperties().isEmpty();
            case STRAINED -> player.getStress() >= 60;
            case AILING -> player.getHealth() <= 45;
        };
    }

    /** Every moment in the table, for content checks. */
    public static List<Moment> all() {
        return MOMENTS;
    }
}
