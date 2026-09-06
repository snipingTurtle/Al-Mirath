package com.example.al_mirath.model;

import java.util.Random;

/**
 * A city that goes on living whether the player is looking at it or not.
 *
 * <p>The game had one world, described in the abstract. This is a place: it
 * has a granary and a garrison and a plague year, and those move on their own
 * schedule. A war arrives, trade dries up, the poor turn to crime, and the
 * madrasas empty — not because the player did anything, but because that is
 * what happens to a city under siege. Come back to Basra twenty years later
 * and it is not the Basra you left.
 *
 * <p>The seven measures the design asked for are coupled, so none of them is a
 * dial that moves alone. They are also pulled back toward the city's
 * {@link CityProfile} every year, which is the part that keeps the coupling
 * from eating itself: prosperity and trade each feeding on the other's decline
 * drives every city on the map to zero within a lifetime.
 */
public class City {

    /** How fast a shaken city returns to being itself: roughly a six-year pull. */
    private static final int RECOVERY = 6;

    /** A war only counts as one if it can actually reach the walls. */
    private static final int WAR_ARRIVES = 55;
    private static final int PLAGUE_ARRIVES = 50;

    private final CityProfile profile;

    private int prosperity;
    private int crime;
    private int war;
    private int disease;
    private int scholarship;
    private int population;
    private int trade;

    /** A city as it stands, restoring a save or continuing a run. */
    public City(
            CityProfile profile,
            int prosperity,
            int crime,
            int war,
            int disease,
            int scholarship,
            int population,
            int trade
    ) {
        this.profile = profile;
        this.prosperity = clamp(prosperity);
        this.crime = clamp(crime);
        this.war = clamp(war);
        this.disease = clamp(disease);
        this.scholarship = clamp(scholarship);
        this.population = clamp(population);
        this.trade = clamp(trade);
    }

    /** A freshly generated city: its own character, but not to the point. */
    public City(CityProfile profile, Random random, int jitter) {
        this(
                profile,
                vary(profile.prosperity(), random, jitter),
                vary(profile.crime(), random, jitter),
                vary(profile.war(), random, jitter),
                vary(profile.disease(), random, jitter),
                vary(profile.scholarship(), random, jitter),
                vary(profile.population(), random, jitter),
                vary(profile.trade(), random, jitter)
        );
    }

    private static int vary(int baseline, Random random, int jitter) {
        return baseline - jitter + random.nextInt(jitter * 2 + 1);
    }

    /**
     * One year of the city living its own life.
     *
     * <p>Wars and plagues arrive as rare shocks and then burn themselves out,
     * which is what makes them memorable. Everything else is dragged along
     * behind them and then hauled slowly back: trade dies under siege,
     * prosperity follows trade, crime fills the space prosperity leaves,
     * scholarship needs money and quiet, and population is the slowest of all
     * to come back.
     *
     * @return what a person living there would say happened this year, or null
     *         in a year nothing worth reporting did
     */
    public String advanceOneYear(Random random) {
        String news = null;

        if (war < 20 && random.nextInt(100) < 3) {
            war = clamp(WAR_ARRIVES + random.nextInt(35));
            news = "War has reached " + profile.name() + ".";
        }

        if (disease < 15 && random.nextInt(100) < 4) {
            disease = clamp(PLAGUE_ARRIVES + random.nextInt(40));
            news = "Plague has broken out in " + profile.name() + ".";
        }

        // The shocks lift on their own; nothing else recovers until they do.
        war = clamp(war - (war > 0 ? 5 + random.nextInt(5) : 0));
        disease = clamp(disease - (disease > 0 ? 8 + random.nextInt(7) : 0));

        prosperity = clamp(
                prosperity + toward(prosperity, profile.prosperity())
                        - war / 6 - disease / 8 - excess(crime) / 10 + jitter(random)
        );

        trade = clamp(
                trade + toward(trade, profile.trade())
                        - war / 6 - disease / 8
                        + (prosperity - profile.prosperity()) / 10 + jitter(random)
        );

        crime = clamp(
                crime + toward(crime, profile.crime())
                        + (profile.prosperity() - prosperity) / 8 + war / 25 + jitter(random)
        );

        scholarship = clamp(
                scholarship + toward(scholarship, profile.scholarship())
                        + (prosperity - profile.prosperity()) / 12 - war / 12 + jitter(random)
        );

        population = clamp(
                population + toward(population, profile.population())
                        - disease / 10 - war / 15 + jitter(random)
        );

        return news;
    }

    /** The pull back toward being this city rather than any other. */
    private static int toward(int current, int baseline) {
        return (baseline - current) / RECOVERY;
    }

    /** Crime only bites prosperity once it is worse than ordinary. */
    private static int excess(int crime) {
        return Math.max(0, crime - 50);
    }

    private static int jitter(Random random) {
        return random.nextInt(3) - 1;
    }

    private static int clamp(int value) {
        return Math.max(0, Math.min(100, value));
    }

    public String getName() {
        return profile.name();
    }

    public CityProfile getProfile() {
        return profile;
    }

    public int getProsperity() {
        return prosperity;
    }

    public int getCrime() {
        return crime;
    }

    public int getWar() {
        return war;
    }

    public int getDisease() {
        return disease;
    }

    public int getScholarship() {
        return scholarship;
    }

    public int getPopulation() {
        return population;
    }

    public int getTrade() {
        return trade;
    }

    public void change(String measure, int amount) {
        switch (measure) {
            case "prosperity" -> prosperity = clamp(prosperity + amount);
            case "crime" -> crime = clamp(crime + amount);
            case "war" -> war = clamp(war + amount);
            case "disease" -> disease = clamp(disease + amount);
            case "scholarship" -> scholarship = clamp(scholarship + amount);
            case "population" -> population = clamp(population + amount);
            case "trade" -> trade = clamp(trade + amount);
            default -> System.out.println("Unknown city measure: " + measure);
        }
    }

    public int get(String measure) {
        return switch (measure) {
            case "prosperity" -> prosperity;
            case "crime" -> crime;
            case "war" -> war;
            case "disease" -> disease;
            case "scholarship" -> scholarship;
            case "population" -> population;
            case "trade" -> trade;
            default -> 0;
        };
    }

    /** What the place is like right now, in one word. */
    public CityCondition condition() {
        return CityCondition.of(this);
    }
}
