package com.example.al_mirath.service;

import com.example.al_mirath.model.CityProfile;

import java.util.List;
import java.util.Map;
import java.util.Random;

/**
 * Puts the four eras on the calendar, so a life has a date rather than a mood.
 *
 * <p>The game used to know only that a character lived in "the Abbasid Era".
 * That is enough to pick a costume and no use at all for meeting the year
 * 1258 in Baghdad. Every life now starts on a real year inside its era, ages
 * a year at a time alongside it, and meets what the record says happened.
 *
 * <p>Spans are the ones the sources give for the dynasties themselves:
 * Umayyad 661–750, Abbasid 750–1258, Mamluk 1250–1517 (Metropolitan Museum
 * of Art, Heilbrunn Timeline of Art History). The Ottoman window starts at
 * the conquest of Constantinople in 1453 rather than the dynasty's own
 * beginning, because that is the point from which this game's cities — the
 * city the player can be born in — are Ottoman.
 */
public final class Eras {

    /** The first and last year a life may begin in each era. */
    private static final Map<String, int[]> SPANS = Map.of(
            CityProfile.UMAYYAD, new int[]{661, 750},
            // 762 rather than 750: the dynasty takes power in 750, but its
            // capital is laid out in 762, and an Abbasid life begins in the
            // seat the era is run from. The twelve years between belong to
            // the end of the Umayyad era, which is where the record puts them.
            CityProfile.ABBASID, new int[]{762, 1258},
            CityProfile.MAMLUK, new int[]{1250, 1517},
            CityProfile.OTTOMAN, new int[]{1453, 1699}
    );

    /**
     * The year a city becomes somewhere a player can be born.
     *
     * <p>Baghdad is not a place to be born in 700: the Abbasids founded it in
     * 762. Cordoba is in the story from the conquest of Iberia in 711–714,
     * and Istanbul is Istanbul from 1453.
     */
    private static final Map<String, Integer> CITY_FROM = Map.of(
            "Baghdad", 762,
            "Cordoba", 711,
            "Istanbul", 1453
    );

    /**
     * A life should be lived mostly inside its own era rather than spilling
     * out the far end of it, so births stop well short of the last year.
     */
    private static final int ROOM_TO_LIVE = 25;

    private Eras() {
    }

    public static int startOf(String era) {
        return SPANS.getOrDefault(era, new int[]{661, 750})[0];
    }

    public static int endOf(String era) {
        return SPANS.getOrDefault(era, new int[]{661, 750})[1];
    }

    public static boolean isKnown(String era) {
        return SPANS.containsKey(era);
    }

    public static List<String> all() {
        return List.of(CityProfile.UMAYYAD, CityProfile.ABBASID,
                CityProfile.MAMLUK, CityProfile.OTTOMAN);
    }

    /**
     * A year to be born in: anywhere in the era with a life's worth of it
     * left, so a character is not born into its final winter every time.
     */
    public static int birthYearIn(String era, Random random) {
        int start = startOf(era);
        int last = Math.max(start, endOf(era) - ROOM_TO_LIVE);

        return start + random.nextInt(last - start + 1);
    }

    /** True when a year is one the era actually saw. */
    public static boolean isYearIn(String era, int year) {
        return isKnown(era) && year >= startOf(era) && year <= endOf(era);
    }

    /**
     * The last year a life may be started in, which stops short of the era's
     * own end so that a character has a life's worth of it in front of them.
     */
    public static int lastBirthYearIn(String era) {
        return Math.max(startOf(era), endOf(era) - ROOM_TO_LIVE);
    }

    /** True when this city is somewhere a person could be, that year. */
    public static boolean cityExistsIn(String city, int year) {
        return year >= CITY_FROM.getOrDefault(city, 0);
    }

    /** The cities of an era that are also there on the year in question. */
    public static List<CityProfile> citiesIn(String era, int year) {
        return CityProfile.forEra(era).stream()
                .filter(profile -> cityExistsIn(profile.name(), year))
                .toList();
    }

    /** The era a year belongs to, for a life that outlives its own era. */
    public static String eraOf(int year) {
        for (String era : all()) {
            if (year >= startOf(era) && year <= endOf(era)) {
                return era;
            }
        }

        return "";
    }

    /** How a year is written on screen. */
    public static String yearText(int year) {
        return year + " CE";
    }
}
