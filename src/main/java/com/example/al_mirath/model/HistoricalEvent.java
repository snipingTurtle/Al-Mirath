package com.example.al_mirath.model;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Something that actually happened, on the year it actually happened.
 *
 * <p>Unlike a {@link WorldEvent}, which is invented colour drawn at random,
 * every one of these is a dated event from the historical record, carried
 * with the source it was taken from. The player's life is pinned to a real
 * calendar year, so a character living in Baghdad in 1258 meets the Mongols
 * because the Mongols were there that year, not because a die came up.
 *
 * <p>Where it lands depends on where the player is: the sack of Baghdad is
 * the end of the world in Baghdad and news from the east in Cairo. An empty
 * {@code cities} set means the whole empire heard about it.
 */
public record HistoricalEvent(
        String id,
        int year,
        String era,
        String title,

        /** What happened, kept to what the source says. */
        String account,

        /** Where it was felt. Empty means everywhere in that era. */
        Set<String> cities,

        Map<String, Integer> statEffects,
        Map<String, Integer> factionEffects,

        /** Set on the world when this happens, for later events to read. */
        String flag,

        /**
         * Flags that get a person through it. A player who took the road out
         * of Baghdad in the scene this event raised is not in Baghdad when
         * the killing starts.
         */
        Set<String> sparedBy,

        /** What it does to somebody standing in the middle of it. */
        Peril peril,

        /**
         * How the player answers it, if they get to. Empty for events that
         * are only news; a scene otherwise, presented like any other.
         */
        List<Choice> choices,

        /** Where the dates and facts came from. */
        String source
) {

    /** What being in the wrong city in the wrong year costs. */
    public enum Peril {

        /** News. Nobody dies of it. */
        NONE(0),

        /** A hard year: the city suffers and so does anyone in it. */
        HARSH(0),

        /** People died. Some of them were standing where you are. */
        DEADLY(18),

        /** A massacre or a plague at its worst. */
        CATASTROPHIC(34);

        private final int deathChance;

        Peril(int deathChance) {
            this.deathChance = deathChance;
        }

        /** The percentage chance of dying, before anything the player did. */
        public int deathChance() {
            return deathChance;
        }
    }

    public HistoricalEvent {
        cities = cities == null ? Set.of() : Set.copyOf(cities);
        sparedBy = sparedBy == null ? Set.of() : Set.copyOf(sparedBy);
        statEffects = statEffects == null ? Map.of() : Map.copyOf(statEffects);
        factionEffects = factionEffects == null ? Map.of() : Map.copyOf(factionEffects);
        choices = choices == null ? List.of() : List.copyOf(choices);
        peril = peril == null ? Peril.NONE : peril;
    }

    /** News everywhere, with no choice to make and nothing to survive. */
    public static HistoricalEvent news(String id, int year, String era, String title,
                                       String account, String flag, String source) {
        return new HistoricalEvent(id, year, era, title, account, Set.of(),
                Map.of(), Map.of(), flag, Set.of(), Peril.NONE, List.of(), source);
    }

    /** True when somebody standing in this city that year is in it. */
    public boolean reaches(String city) {
        return cities.isEmpty() || cities.contains(city);
    }

    /** True when the player is close enough to have to answer it. */
    public boolean isLocalTo(String city) {
        return !cities.isEmpty() && cities.contains(city);
    }

    public boolean hasChoices() {
        return !choices.isEmpty();
    }

    public boolean canKill() {
        return peril.deathChance() > 0;
    }

    /** True when something the player did takes them out of the worst of it. */
    public boolean isSparedBy(Iterable<String> flags) {
        for (String flag : flags) {
            if (sparedBy.contains(flag)) {
                return true;
            }
        }

        return false;
    }

    /** The year as a player reads it. */
    public String yearText() {
        return year + " CE";
    }
}
