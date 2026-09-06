package com.example.al_mirath.service;

import com.example.al_mirath.model.City;
import com.example.al_mirath.model.CityCondition;
import com.example.al_mirath.model.CityProfile;
import com.example.al_mirath.model.PlayerCharacter;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Random;


/**
 * The map the run happens on.
 *
 * <p>"The world" used to be a single abstraction. This generates the actual
 * cities of the era — you cannot be in Istanbul under the Umayyads, and
 * Cordoba is gone by the time the Mamluks rule — gives each one its own seven
 * numbers, and then lets them run. The player stands in exactly one of them at
 * a time, and the rest keep changing while they are not looking.
 */
public final class CityRegistry {

    /** How far a generated city may differ from its historical baseline. */
    private static final int JITTER = 12;

    /** Flags of the form {@code travel_to_baghdad} move the player. */
    private static final String TRAVEL_PREFIX = "travel_to_";

    private final Map<String, City> cities = new LinkedHashMap<>();
    private final Random random = new Random();

    private String currentCityName = "";

    /** Builds the map a particular life is lived on. */
    public static CityRegistry createFor(PlayerCharacter player) {
        CityRegistry registry = new CityRegistry();

        String era = player == null ? "" : player.getEra();

        for (CityProfile profile : CityProfile.forEra(era)) {
            registry.cities.put(
                    profile.name(),
                    new City(profile, registry.random, JITTER)
            );
        }

        // No era should ever leave the player standing nowhere.
        if (registry.cities.isEmpty()) {
            CityProfile fallback = CityProfile.roster().get(0);

            registry.cities.put(
                    fallback.name(),
                    new City(fallback, registry.random, JITTER)
            );
        }

        registry.currentCityName = registry.startingCityFor(player);

        return registry;
    }

    /**
     * Where a life starts: the seat its era is run from, unless the character's
     * own background names a city, in which case that is home.
     */
    private String startingCityFor(PlayerCharacter player) {
        String seat =
                player == null
                        ? ""
                        : TitleForge.seatOf(player.getEra(), player.getOrigin());

        if (cities.containsKey(seat)) {
            return seat;
        }

        return cities.keySet().iterator().next();
    }

    // ---- the world turning ----------------------------------------------

    /**
     * Runs every city forward, including the ones the player is nowhere near.
     *
     * @return news worth telling the player: what happened where they are, and
     *         the loudest thing that happened somewhere they are not
     */
    public List<String> advanceYears(int years) {
        List<String> news = new ArrayList<>();

        if (years <= 0) {
            return news;
        }

        for (int year = 0; year < years; year++) {
            for (City city : cities.values()) {
                String happened = city.advanceOneYear(random);

                if (happened == null) {
                    continue;
                }

                // Distance decides whether you hear about it as news or as
                // something happening to you.
                if (city.getName().equals(currentCityName)) {
                    news.add(happened);
                } else if (news.size() < 3) {
                    news.add("Word reaches you: " + happened.toLowerCase(Locale.ROOT));
                }
            }
        }

        return news;
    }

    /**
     * Lets a choice change the city it was made in, or move the player out of
     * it entirely.
     *
     * <p>This is the half that makes the map worth having. A player who forces
     * a granary open leaves a measurably less desperate city behind them, and
     * one who takes the protection money leaves a worse one — years later,
     * for whoever is still standing there.
     *
     * @return true when the flag meant something to the map
     */
    public boolean applyStoryFlag(String flag) {
        if (flag == null) {
            return false;
        }

        if (flag.startsWith(TRAVEL_PREFIX)) {
            String wanted = flag.substring(TRAVEL_PREFIX.length());

            for (String name : cities.keySet()) {
                if (name.toLowerCase(Locale.ROOT).equals(wanted)) {
                    return travelTo(name);
                }
            }

            return false;
        }

        City here = currentCity();

        if (here == null) {
            return false;
        }

        switch (flag) {
            case "city_walls_held" -> here.change("war", -25);
            case "city_gates_opened" -> {
                here.change("war", -40);
                here.change("prosperity", -15);
                here.change("crime", 10);
            }
            case "city_plague_contained" -> here.change("disease", -25);
            case "city_plague_spread" -> {
                here.change("disease", 15);
                here.change("population", -8);
            }
            case "city_crime_broken" -> here.change("crime", -20);
            case "city_crime_joined" -> here.change("crime", 10);
            case "city_trade_opened" -> {
                here.change("trade", 12);
                here.change("prosperity", 8);
            }
            case "city_scholars_endowed" -> here.change("scholarship", 12);
            case "city_granary_opened" -> {
                here.change("prosperity", 10);
                here.change("crime", -8);
            }
            default -> {
                return false;
            }
        }

        return true;
    }

    /** @return true when the player actually moved */
    public boolean travelTo(String cityName) {
        if (!cities.containsKey(cityName) || cityName.equals(currentCityName)) {
            return false;
        }

        currentCityName = cityName;

        return true;
    }

    // ---- reading the map -------------------------------------------------

    public City currentCity() {
        return cities.get(currentCityName);
    }

    public String currentCityName() {
        return currentCityName;
    }

    public CityCondition currentCondition() {
        City here = currentCity();

        return here == null ? CityCondition.QUIET : here.condition();
    }

    /** Every city of the era, the player's own first. */
    public List<City> everyCity() {
        List<City> all = new ArrayList<>();
        City here = currentCity();

        if (here != null) {
            all.add(here);
        }

        for (City city : cities.values()) {
            if (city != here) {
                all.add(city);
            }
        }

        return all;
    }

    /**
     * Somewhere else the player could plausibly go: the city of the era doing
     * best that is not this one, so leaving is a real alternative rather than
     * a lottery.
     */
    public City somewhereElse() {
        City best = null;

        for (City city : cities.values()) {
            if (city.getName().equals(currentCityName)) {
                continue;
            }

            if (best == null || appeal(city) > appeal(best)) {
                best = city;
            }
        }

        return best;
    }

    private int appeal(City city) {
        return city.getProsperity() + city.getTrade()
                - city.getWar() - city.getDisease() - city.getCrime() / 2;
    }

    /** The line that puts an otherwise placeless scene somewhere. */
    public String situate() {
        City here = currentCity();

        return here == null ? "" : here.condition().situate(here.getName());
    }

    /**
     * The rest of the era's map, every city of it, always in the same order.
     *
     * <p>This panel used to show a single "somewhere else" — whichever city
     * was currently doing best. Since that is recomputed as the world drifts,
     * the name under the player's own city changed almost every year: 93% of
     * lives displayed two or more cities there, some of them seven. With no
     * label saying what that second entry was, it read as though the player's
     * own city kept changing. Listing all of them in roster order says plainly
     * that these are other places.
     */
    public String elsewhereSummary() {
        StringBuilder summary = new StringBuilder();

        for (City city : cities.values()) {
            if (city.getName().equals(currentCityName)) {
                continue;
            }

            if (!summary.isEmpty()) {
                summary.append("\n\n");
            }

            summary.append(city.getName())
                    .append("\n")
                    .append(city.condition().displayName());
        }

        return summary.toString();
    }

    /** The city panel's text, for callers that want it as one block. */
    public String whereYouAreSummary() {
        City here = currentCity();

        if (here == null) {
            return "";
        }

        return here.getName()
                + "\n" + here.condition().displayName()
                + "\n" + measureLine(here)
                + "\n\nElsewhere\n" + elsewhereSummary();
    }

    private String measureLine(City city) {
        return "Prosperity " + city.getProsperity()
                + "  ·  Trade " + city.getTrade()
                + "  ·  Crime " + city.getCrime()
                + "\nScholarship " + city.getScholarship()
                + "  ·  People " + city.getPopulation();
    }

    // ---- persistence -----------------------------------------------------

    public JSONObject toJson() {
        JSONObject root = new JSONObject();
        JSONArray list = new JSONArray();

        for (City city : cities.values()) {
            JSONObject entry = new JSONObject();

            entry.put("name", city.getName());
            entry.put("prosperity", city.getProsperity());
            entry.put("crime", city.getCrime());
            entry.put("war", city.getWar());
            entry.put("disease", city.getDisease());
            entry.put("scholarship", city.getScholarship());
            entry.put("population", city.getPopulation());
            entry.put("trade", city.getTrade());

            list.put(entry);
        }

        root.put("cities", list);
        root.put("current", currentCityName);

        return root;
    }

    public static CityRegistry fromJson(JSONObject root) {
        CityRegistry registry = new CityRegistry();

        if (root == null) {
            return registry;
        }

        JSONArray list = root.optJSONArray("cities");

        if (list != null) {
            for (int i = 0; i < list.length(); i++) {
                JSONObject entry = list.getJSONObject(i);
                CityProfile profile = CityProfile.byName(entry.getString("name"));

                if (profile == null) {
                    // A save naming a city this build does not know.
                    continue;
                }

                registry.cities.put(
                        entry.getString("name"),
                        new City(
                                profile,
                                entry.getInt("prosperity"),
                                entry.getInt("crime"),
                                entry.getInt("war"),
                                entry.getInt("disease"),
                                entry.getInt("scholarship"),
                                entry.getInt("population"),
                                entry.getInt("trade")
                        )
                );
            }
        }

        registry.currentCityName = root.optString("current", "");

        if (!registry.cities.containsKey(registry.currentCityName)
                && !registry.cities.isEmpty()) {

            registry.currentCityName = registry.cities.keySet().iterator().next();
        }

        return registry;
    }

    public void replaceWith(CityRegistry restored) {
        cities.clear();

        if (restored == null) {
            currentCityName = "";
            return;
        }

        cities.putAll(restored.cities);
        currentCityName = restored.currentCityName;
    }
}
