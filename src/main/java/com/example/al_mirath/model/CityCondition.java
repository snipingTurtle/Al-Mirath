package com.example.al_mirath.model;

/**
 * What a city is like to live in this year.
 *
 * <p>Seven numbers are what the simulation runs on; this is what the player
 * reads. The order the checks run in is the order a resident would rank their
 * problems: a city under siege is under siege first, whatever its granary
 * looks like.
 */
public enum CityCondition {

    BESIEGED("Under siege"),
    PLAGUE_STRICKEN("Plague-stricken"),
    LAWLESS("Lawless"),
    LEARNED("A seat of learning"),
    FLOURISHING("Flourishing"),
    STRUGGLING("Struggling"),
    QUIET("Quiet");

    private final String displayName;

    CityCondition(String displayName) {
        this.displayName = displayName;
    }

    /**
     * Reads a city's year.
     *
     * <p>Sieges, plagues and learning are judged on absolutes — a plague is a
     * plague anywhere, and a city is a seat of learning or it is not. Doing
     * well and doing badly are judged against the city's own
     * {@link CityProfile} instead, because a good year in Jerusalem does not
     * look like a good year in Baghdad, and an absolute bar would mean the
     * poorer half of the map could never have one.
     */
    public static CityCondition of(City city) {
        CityProfile normal = city.getProfile();

        if (city.getWar() >= 55) {
            return BESIEGED;
        }
        if (city.getDisease() >= 45) {
            return PLAGUE_STRICKEN;
        }
        if (city.getCrime() >= normal.crime() + 18) {
            return LAWLESS;
        }
        if (city.getScholarship() >= 65 && city.getWar() < 40) {
            return LEARNED;
        }
        if (city.getProsperity() >= normal.prosperity() - 3
                && city.getWar() < 30
                && city.getDisease() < 25) {
            return FLOURISHING;
        }
        if (city.getProsperity() <= normal.prosperity() - 18) {
            return STRUGGLING;
        }

        return QUIET;
    }

    public String displayName() {
        return displayName;
    }

    /**
     * The half-line that puts a scene somewhere.
     *
     * <p>Prepended to events that would otherwise happen in an unnamed
     * nowhere, so that the same argument in a doorway reads differently in a
     * besieged Aleppo and a flourishing one.
     */
    public String situate(String city) {
        return switch (this) {
            case BESIEGED ->
                    city + ", with the war close enough to hear.";
            case PLAGUE_STRICKEN ->
                    city + ", in a year the sickness has not finished with.";
            case LAWLESS ->
                    city + ", where nobody walks the long way home any more.";
            case LEARNED ->
                    city + ", where the copyists work by lamplight until dawn.";
            case FLOURISHING ->
                    city + ", fat with a good year.";
            case STRUGGLING ->
                    city + ", thin and getting thinner.";
            case QUIET ->
                    city + ", going about its business.";
        };
    }
}
