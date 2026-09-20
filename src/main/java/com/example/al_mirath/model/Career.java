package com.example.al_mirath.model;

import java.util.List;
import java.util.Map;
import java.util.function.Function;

/**
 * Career tracks available in the Islamic Golden Age, each with a ladder of
 * ranks the player can climb through skill, reputation, and connections.
 *
 * <p>Every rank specifies the stat thresholds a character must meet to be
 * promoted into it. Income rises with rank and funds the player's net worth
 * each year. The career also gates certain activities and events.</p>
 */
public enum Career {

    UNEMPLOYED("Unemployed", "You have no trade or calling.", 0, List.of(
            new Rank("Idle", 0, Map.of())
    )),

    FARMER("Farmer", "You work the land that feeds the city.", 10, List.of(
            new Rank("Laborer", 20, Map.of("health", 20)),
            new Rank("Tenant Farmer", 60, Map.of("health", 30)),
            new Rank("Landowner", 200, Map.of("wealth", 40)),
            new Rank("Estate Holder", 500, Map.of("wealth", 55, "reputation", 35))
    )),

    ARTISAN("Artisan", "You craft goods in the workshops of the souk.", 12, List.of(
            new Rank("Apprentice", 30, Map.of()),
            new Rank("Journeyman", 90, Map.of("education", 25)),
            new Rank("Master Craftsman", 250, Map.of("education", 40, "reputation", 30)),
            new Rank("Guild Master", 500, Map.of("reputation", 50, "wealth", 45))
    )),

    SCHOLAR("Scholar", "You pursue knowledge at the madrasa.", 14, List.of(
            new Rank("Student", 30, Map.of("education", 25)),
            new Rank("Copyist", 80, Map.of("education", 40)),
            new Rank("Lecturer", 200, Map.of("education", 55, "reputation", 35)),
            new Rank("Chair of Studies", 500, Map.of("education", 70, "reputation", 50)),
            new Rank("Head of Madrasa", 1000, Map.of("education", 85, "reputation", 65))
    )),

    MERCHANT("Merchant", "You buy and sell in the great bazaars.", 14, List.of(
            new Rank("Apprentice", 40, Map.of("wealth", 20)),
            new Rank("Shopkeeper", 150, Map.of("wealth", 35)),
            new Rank("Trader", 400, Map.of("wealth", 50, "reputation", 35)),
            new Rank("Caravan Master", 900, Map.of("wealth", 65, "reputation", 45)),
            new Rank("Grand Merchant", 2000, Map.of("wealth", 80, "reputation", 60))
    )),

    SOLDIER("Soldier", "You serve in the garrison or on the frontier.", 16, List.of(
            new Rank("Recruit", 35, Map.of("health", 30)),
            new Rank("Guard", 100, Map.of("health", 45)),
            new Rank("Officer", 250, Map.of("health", 55, "politicalPower", 30)),
            new Rank("Commander", 600, Map.of("health", 65, "politicalPower", 50)),
            new Rank("General", 1500, Map.of("health", 75, "politicalPower", 65))
    )),

    COURTIER("Courtier", "You navigate the intrigues of the palace.", 18, List.of(
            new Rank("Page", 50, Map.of("politicalPower", 20)),
            new Rank("Clerk", 150, Map.of("politicalPower", 35, "education", 30)),
            new Rank("Secretary", 350, Map.of("politicalPower", 50)),
            new Rank("Advisor", 800, Map.of("politicalPower", 65, "reputation", 50)),
            new Rank("Vizier", 2500, Map.of("politicalPower", 80, "reputation", 65))
    )),

    PHYSICIAN("Physician", "You study the humours and heal the sick.", 16, List.of(
            new Rank("Herbalist", 45, Map.of("education", 30)),
            new Rank("Apprentice Physician", 120, Map.of("education", 45)),
            new Rank("Physician", 350, Map.of("education", 60, "morality", 35)),
            new Rank("Court Physician", 800, Map.of("education", 75, "reputation", 50)),
            new Rank("Chief Physician", 1500, Map.of("education", 85, "reputation", 60))
    )),

    IMAM("Religious Leader", "You guide the faithful and interpret the law.", 18, List.of(
            new Rank("Prayer Caller", 25, Map.of("morality", 35)),
            new Rank("Preacher", 80, Map.of("morality", 50, "education", 35)),
            new Rank("Imam", 180, Map.of("morality", 60, "education", 45)),
            new Rank("Judge", 500, Map.of("morality", 70, "education", 60, "reputation", 50)),
            new Rank("Grand Judge", 1200, Map.of("morality", 80, "education", 70, "reputation", 60))
    )),

    CRIMINAL("Criminal", "You operate in the shadows of the city.", 12, List.of(
            new Rank("Pickpocket", 20, Map.of()),
            new Rank("Thief", 80, Map.of()),
            new Rank("Gang Leader", 250, Map.of("politicalPower", 25)),
            new Rank("Shadow Lord", 700, Map.of("politicalPower", 45)),
            new Rank("Master of Daggers", 1500, Map.of("politicalPower", 60))
    ));

    private final String displayName;
    private final String description;
    private final int minimumAge;
    private final List<Rank> ranks;

    Career(String displayName, String description, int minimumAge, List<Rank> ranks) {
        this.displayName = displayName;
        this.description = description;
        this.minimumAge = minimumAge;
        this.ranks = List.copyOf(ranks);
    }

    public String displayName() { return displayName; }
    public String description() { return description; }
    public int minimumAge() { return minimumAge; }
    public List<Rank> ranks() { return ranks; }
    public int maxRank() { return ranks.size() - 1; }

    /** Returns the rank at the given index, clamped to bounds. */
    public Rank rank(int index) {
        return ranks.get(Math.max(0, Math.min(index, ranks.size() - 1)));
    }

    /** Returns the annual income for a given rank index. */
    public int incomeAt(int rankIndex) {
        return rank(rankIndex).annualIncome();
    }

    /**
     * Checks whether the character qualifies for the next rank above their
     * current one. Returns {@code true} if promotion is possible.
     */
    public boolean canPromote(int currentRank, Function<String, Integer> statLookup) {
        int next = currentRank + 1;
        if (next > maxRank()) return false;
        return rank(next).qualifies(statLookup);
    }

    /**
     * Returns the highest rank the character currently qualifies for,
     * starting from rank 0. Used when first entering a career.
     */
    public int entryRank(Function<String, Integer> statLookup) {
        int best = 0;
        for (int i = 0; i <= maxRank(); i++) {
            if (rank(i).qualifies(statLookup)) best = i;
            else break;
        }
        return best;
    }

    /** Look up a career by name or display name, defaulting to UNEMPLOYED. */
    public static Career fromName(String name) {
        if (name == null || name.isBlank()) return UNEMPLOYED;
        for (Career c : values()) {
            if (c.name().equalsIgnoreCase(name)
                    || c.displayName.equalsIgnoreCase(name)) {
                return c;
            }
        }
        return UNEMPLOYED;
    }

    // ── Inner record ────────────────────────────────────────────────────

    /**
     * A single rung on the career ladder.
     *
     * @param title          User-facing rank title
     * @param annualIncome   Gold earned per game-year at this rank
     * @param requirements   Stat thresholds (stat-name → minimum value)
     */
    public record Rank(String title, int annualIncome,
                       Map<String, Integer> requirements) {

        public Rank {
            requirements = Map.copyOf(requirements);
        }

        /** Returns {@code true} if every required stat meets its threshold. */
        public boolean qualifies(Function<String, Integer> statLookup) {
            return requirements.entrySet().stream()
                    .allMatch(e -> statLookup.apply(e.getKey()) >= e.getValue());
        }
    }
}
