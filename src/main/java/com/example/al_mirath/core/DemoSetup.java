package com.example.al_mirath.core;

import com.example.al_mirath.model.LifeStart;

/**
 * Lets a life be started deliberately rather than rolled, for demonstrating
 * and testing the parts of the game that are otherwise a matter of luck.
 *
 * <p>The game pins every life to a real year inside a real era, and both are
 * rolled: an Abbasid character is born anywhere between 762 and 1233. That is
 * right for play and useless for showing somebody the Mongols arriving at
 * Baghdad in 1258, which roughly one life in fifty is in a position to see.
 *
 * <p>So three properties, read once at the start of a life and never
 * mentioned in the interface:
 *
 * <pre>
 *   java -Dalmirath.era="Abbasid Era" \
 *        -Dalmirath.birthYear=1240 \
 *        -Dalmirath.city=Baghdad \
 *        -jar target/almirath.jar
 * </pre>
 *
 * <p>A player who never sets them cannot tell they exist, and a value that
 * does not make sense — a city that was not there yet, a year outside its
 * era — is ignored rather than obeyed, because a broken demo is worse than
 * an unrigged one.
 */
public final class DemoSetup {

    private static final String ERA = "almirath.era";
    private static final String BIRTH_YEAR = "almirath.birthYear";
    private static final String CITY = "almirath.city";

    private DemoSetup() {
    }

    /** The era a life was told to begin in, or null to roll one. */
    public static String era() {
        return trimmedOrNull(ERA);
    }

    /** The year a life was told to begin in, or -1 to roll one. */
    public static int birthYear() {
        String raw = trimmedOrNull(BIRTH_YEAR);

        if (raw == null) {
            return -1;
        }

        try {
            return Integer.parseInt(raw);
        } catch (NumberFormatException notAYear) {
            System.out.println("Ignoring " + BIRTH_YEAR + "=" + raw + ": not a year.");
            return -1;
        }
    }

    /** The city a life was told to begin in, or null to use the era's seat. */
    public static String city() {
        return trimmedOrNull(CITY);
    }

    /**
     * The properties as a {@link LifeStart}, so the command line and the
     * naming screen hand the engine the same thing and only one path through
     * the engine has to exist.
     */
    public static LifeStart asLifeStart() {
        return new LifeStart(era(), Math.max(0, birthYear()), city());
    }

    private static String trimmedOrNull(String key) {
        String value = System.getProperty(key);

        return value == null || value.isBlank() ? null : value.trim();
    }
}
