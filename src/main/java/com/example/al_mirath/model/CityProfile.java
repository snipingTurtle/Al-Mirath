package com.example.al_mirath.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * What a city is like when nothing in particular is happening to it.
 *
 * <p>Every city keeps pulling back toward these numbers. That is what makes
 * Basra recognisably Basra thirty years later: a plague can empty it and a
 * good decade can fill it, but left alone it drifts back to being a crowded,
 * slightly lawless port. Without a baseline to return to, coupled measures
 * like prosperity and trade simply spiral into each other and every city on
 * the map ends up identical and ruined.
 */
public record CityProfile(
        String name,
        Set<String> eras,
        int prosperity,
        int crime,
        int war,
        int disease,
        int scholarship,
        int population,
        int trade
) {

    public static final String UMAYYAD = "Umayyad Era";
    public static final String ABBASID = "Abbasid Era";
    public static final String MAMLUK = "Mamluk Era";
    public static final String OTTOMAN = "Ottoman Era";

    private static final Set<String> EVERY_ERA =
            Set.of(UMAYYAD, ABBASID, MAMLUK, OTTOMAN);

    private static final List<CityProfile> ROSTER = List.of(
            new CityProfile("Damascus", EVERY_ERA, 62, 35, 20, 20, 60, 65, 65),
            new CityProfile("Aleppo", EVERY_ERA, 58, 38, 30, 22, 50, 55, 68),
            new CityProfile("Jerusalem", EVERY_ERA, 48, 30, 35, 25, 62, 45, 45),
            new CityProfile("Basra", EVERY_ERA, 55, 50, 18, 35, 55, 60, 78),

            // Baghdad is founded under the Abbasids, and Cairo's rise as a
            // capital belongs to the same stretch onward.
            new CityProfile("Baghdad", Set.of(ABBASID, MAMLUK, OTTOMAN),
                    70, 40, 25, 25, 85, 80, 72),
            new CityProfile("Cairo", Set.of(ABBASID, MAMLUK, OTTOMAN),
                    68, 45, 20, 30, 70, 85, 70),

            // Andalusia is a going concern early and gone later.
            new CityProfile("Cordoba", Set.of(UMAYYAD, ABBASID),
                    72, 28, 25, 18, 88, 70, 60),

            // And Istanbul is only Istanbul after 1453.
            new CityProfile("Istanbul", Set.of(OTTOMAN), 75, 42, 22, 28, 72, 90, 80)
    );

    /** The cities that exist in a given era, in roster order. */
    public static List<CityProfile> forEra(String era) {
        List<CityProfile> present = new ArrayList<>();

        for (CityProfile profile : ROSTER) {
            if (profile.eras().contains(era)) {
                present.add(profile);
            }
        }

        return present;
    }

    /** Looks a city's character back up when restoring a save. */
    public static CityProfile byName(String name) {
        for (CityProfile profile : ROSTER) {
            if (profile.name().equals(name)) {
                return profile;
            }
        }

        return null;
    }

    public static List<CityProfile> roster() {
        return ROSTER;
    }

    public int baselineOf(String measure) {
        return switch (measure) {
            case "prosperity" -> prosperity;
            case "crime" -> crime;
            case "war" -> war;
            case "disease" -> disease;
            case "scholarship" -> scholarship;
            case "population" -> population;
            case "trade" -> trade;
            default -> 50;
        };
    }
}
