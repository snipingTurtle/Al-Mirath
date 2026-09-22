package com.example.al_mirath.model;

/**
 * What the player decided about a life before it was rolled.
 *
 * <p>A run is meant to hand you a person rather than let you build one, so
 * the household, the trait and the station stay rolled. When and where are
 * different: the game now pins a life to a real year, and wanting to be born
 * in Baghdad in 1240 — knowing exactly what is coming in 1258 — is a reason
 * to play, not a way of cheating.
 *
 * <p>Anything left out is rolled as before: a null era, a year of zero, or a
 * null city each mean "surprise me".
 */
public record LifeStart(String era, int birthYear, String city) {

    private static final LifeStart ROLLED = new LifeStart(null, 0, null);

    public LifeStart {
        era = blankToNull(era);
        city = blankToNull(city);
        birthYear = Math.max(0, birthYear);
    }

    /** Everything left to the roll, which is how the game has always begun. */
    public static LifeStart rolled() {
        return ROLLED;
    }

    public boolean hasEra() {
        return era != null;
    }

    public boolean hasBirthYear() {
        return birthYear > 0;
    }

    public boolean hasCity() {
        return city != null;
    }

    /** True when the player asked for nothing in particular. */
    public boolean isRolled() {
        return !hasEra() && !hasBirthYear() && !hasCity();
    }

    private static String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }
}
