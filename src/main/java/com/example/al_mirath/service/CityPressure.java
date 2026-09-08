package com.example.al_mirath.service;

import com.example.al_mirath.model.City;
import com.example.al_mirath.model.DeathCause;

/**
 * What the city does to the life being lived in it.
 *
 * <p>The map used to be scenery. A city could be plague-stricken, half-starved
 * and under siege, and the only difference it made was which scenes came up
 * and a half-line of place-setting on top of them: the seven bars in the drawer
 * moved every year and changed nothing about the run. A player could watch
 * Baghdad burn from a life that was completely unaffected by it.
 *
 * <p>So the same numbers now bend three things — the odds, the outcome, and
 * whether you survive the year. Deliberately in that order of force: a good
 * city makes a scholar's life easier and a bad one makes every kind of life
 * harder, but the city colours a roll rather than deciding it. Only plague and
 * siege kill on their own, and only while they last.
 *
 * <p>All of it is a pure function of the city, so the line shown to the player
 * in the drawer is computed from the same rules that move their stats. A city
 * cannot say one thing and do another.
 */
public final class CityPressure {

    /** The middle of every city measure: at 50 a city neither helps nor hinders. */
    private static final int ORDINARY = 50;

    /** The most a city can move a stat check, either way. */
    private static final int MOST_HELP = 15;
    private static final int MOST_HARM = -20;

    /**
     * The most a city can scale what a choice does to you.
     *
     * <p>The gentlest must stay at or above a half. Below it, a one-point
     * consequence rounds to nothing and the choice that earned it silently
     * did not happen.
     */
    private static final double GENTLEST = 0.55;
    private static final double HARSHEST = 1.75;

    /**
     * The war a city carries without anything being wrong. Every city on the
     * map sits somewhere between eighteen and thirty-five: a frontier is a
     * frontier. Read absolutely, that made the court harder to work in every
     * city in the game, permanently, which is not a pressure — it is a tax.
     */
    private static final int FRONTIER_QUIET = 25;

    /** Below these a city is merely unpleasant; past them it kills people. */
    private static final int PLAGUE_KILLS = 45;
    private static final int SIEGE_KILLS = 55;

    private CityPressure() {
    }

    // ---- the odds --------------------------------------------------------

    /**
     * How much easier or harder this city makes a test of a given stat.
     *
     * <p>Added to the success chance. Learning is easier where there are
     * people to learn from, a deal is easier where there is trade and harder
     * where there is theft, and nothing political moves at all in a city
     * whose walls are being hit.
     */
    public static int shiftFor(City city, String stat) {
        if (city == null || stat == null) {
            return 0;
        }

        int shift = switch (stat) {
            case "education" -> (city.getScholarship() - ORDINARY) / 4;

            case "wealth" -> (city.getTrade() - ORDINARY) / 4
                    - (city.getCrime() - ORDINARY) / 6;

            case "politicalPower" -> (city.getProsperity() - ORDINARY) / 6
                    - above(city.getWar(), FRONTIER_QUIET) / 4;

            case "health" -> -city.getDisease() / 5
                    - city.getWar() / 10;

            case "reputation" -> (city.getProsperity() - ORDINARY) / 6
                    - (city.getCrime() - ORDINARY) / 8;

            // Nerve, conscience and family are carried in, not found here.
            default -> 0;
        };

        return Math.max(MOST_HARM, Math.min(MOST_HELP, shift));
    }

    // ---- the outcome -----------------------------------------------------

    /**
     * What a change to a stat actually comes to, here.
     *
     * <p>Money made in a rich city goes further and money lost in a lawless
     * one goes further still; a wound taken during a plague is worse than the
     * same wound taken in a quiet year. The sign never changes and a change
     * never disappears — a city bends what happens to you, it does not decide
     * that nothing did.
     */
    public static int bend(City city, String stat, int delta) {
        if (city == null || delta == 0) {
            return delta;
        }

        double scale = scaleFor(city, stat, delta);

        if (scale == 1.0) {
            return delta;
        }

        // Rounding must never swallow a change entirely, which is what keeps
        // GENTLEST at or above a half: a choice whose consequence the city
        // quietly rounded to nothing is worse than either extreme, and the
        // property is asserted rather than patched up here.
        return (int) Math.round(delta * scale);
    }

    private static double scaleFor(City city, String stat, int delta) {
        boolean gain = delta > 0;

        double scale = switch (stat) {
            case "wealth" -> gain
                    ? 1.0 + percent((city.getProsperity() - ORDINARY) / 2
                            + (city.getTrade() - ORDINARY) / 4)
                    : 1.0 + percent(above(city.getCrime()) / 2);

            case "health" -> gain
                    ? 1.0 - percent((city.getDisease() + city.getWar()) / 2)
                    : 1.0 + percent((city.getDisease() + city.getWar()) / 3);

            case "education" -> gain
                    ? 1.0 + percent((city.getScholarship() - ORDINARY) / 2)
                    : 1.0;

            case "stress" -> gain
                    ? 1.0 + percent((city.getWar() + city.getDisease()
                            + above(city.getCrime())) / 6)
                    : 1.0;

            case "reputation" -> gain
                    ? 1.0 + percent((city.getPopulation() - ORDINARY) / 3)
                    : 1.0;

            default -> 1.0;
        };

        return Math.max(GENTLEST, Math.min(HARSHEST, scale));
    }

    private static double percent(int points) {
        return points / 100.0;
    }

    /**
     * How far above ordinary a measure is, or zero.
     *
     * <p>Crime, prosperity, trade and scholarship are read against the middle,
     * because every city has some of each and an average amount of it should
     * cost nothing. War and disease are read against zero, because a city is
     * not normally at war and is not normally sick — any of either is already
     * the exception.
     */
    private static int above(int measure) {
        return above(measure, ORDINARY);
    }

    private static int above(int measure, int normal) {
        return Math.max(0, measure - normal);
    }

    // ---- surviving the year ----------------------------------------------

    /**
     * How much more likely this city makes it that the year is your last.
     *
     * <p>Kept deliberately light, because it is charged on every choice made
     * while the plague or the siege lasts and compounds fast. Measured over six
     * hundred played lives: the odds and outcomes above cost two points of
     * mortality, and a first attempt at this method cost another eight on top,
     * which made the city the main thing killing people rather than one of the
     * things. At these numbers a single choice in a bad year adds three or four
     * points at worst, and about one life in ten ends in the city — enough that
     * a plague year is a reason to leave, not enough that where you live
     * decides the run.
     */
    public static int deathRisk(City city) {
        if (city == null) {
            return 0;
        }

        int risk = 0;

        if (city.getDisease() >= PLAGUE_KILLS) {
            risk += city.getDisease() / 25;
        }

        if (city.getWar() >= SIEGE_KILLS) {
            risk += city.getWar() / 30;
        }

        return risk;
    }

    /** What the death panel calls it when the city is what killed you. */
    public static DeathCause deathCause(City city) {
        if (city == null) {
            return null;
        }

        boolean plague = city.getDisease() >= PLAGUE_KILLS;
        boolean siege = city.getWar() >= SIEGE_KILLS;

        if (plague && siege) {
            // A besieged city with plague inside it dies of the plague; the
            // walls are only why nobody could leave.
            return DeathCause.PLAGUE;
        }

        if (plague) {
            return DeathCause.PLAGUE;
        }

        return siege ? DeathCause.SIEGE : null;
    }

    // ---- saying so -------------------------------------------------------

    /**
     * What living here is doing to you, in one line.
     *
     * <p>Read off the same rules that move the stats, so the drawer cannot
     * promise something the engine does not do. Mechanics the player cannot
     * see are not much better than scenery.
     */
    public static String felt(City city) {
        if (city == null) {
            return "";
        }

        StringBuilder line = new StringBuilder();

        if (city.getDisease() >= PLAGUE_KILLS) {
            add(line, "the sickness here can take you on its own, and injuries "
                    + "heal badly");

        } else if (city.getDisease() >= 25) {
            add(line, "wounds and illness go worse here than they would elsewhere");
        }

        if (city.getWar() >= SIEGE_KILLS) {
            add(line, "the walls are being hit, which can kill you and has "
                    + "stopped anything political moving");

        } else if (city.getWar() >= 30) {
            add(line, "the fighting nearby makes anything to do with the court "
                    + "harder");
        }

        if (city.getCrime() >= 60) {
            add(line, "what you lose here, you lose more of, and a deal is "
                    + "harder to hold");
        }

        if (city.getScholarship() >= 65) {
            add(line, "there is more to learn here than most places, and people "
                    + "to learn it from");

        } else if (city.getScholarship() <= 30) {
            add(line, "there is nobody here to learn from");
        }

        if (city.getProsperity() >= 65 && city.getTrade() >= 55) {
            add(line, "money made here goes further");

        } else if (city.getProsperity() <= 30) {
            add(line, "there is little money here to be made");
        }

        if (line.isEmpty()) {
            return "An ordinary place to live: nothing here is helping you or "
                    + "getting in your way.";
        }

        String felt = line.toString();

        return Character.toUpperCase(felt.charAt(0)) + felt.substring(1) + ".";
    }

    private static void add(StringBuilder line, String clause) {
        if (!line.isEmpty()) {
            line.append("; ");
        }

        line.append(clause);
    }
}
