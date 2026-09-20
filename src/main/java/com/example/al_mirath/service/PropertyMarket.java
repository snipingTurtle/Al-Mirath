package com.example.al_mirath.service;

import com.example.al_mirath.model.City;
import com.example.al_mirath.model.PlayerCharacter;
import com.example.al_mirath.model.Property;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * What is for sale in the quarter this year.
 *
 * <p>Property is the game's second economy. Wages arrive because you turned
 * up; rents arrive because of a decision you made years ago and then kept
 * paying to maintain. It is also the only thing a character can convert a
 * fortune <em>into</em> — a purse is worth nothing to an heir, but a
 * caravansary is.
 *
 * <p>Prices follow the city. Buying a shop in a city whose trade has
 * collapsed is cheap and stays cheap, which is either a bargain or a lesson.
 */
public final class PropertyMarket {

    private static final Random RANDOM = new Random();

    private PropertyMarket() {
    }

    /**
     * Something on the market.
     *
     * @param name      what the deed calls it
     * @param type      what it is
     * @param price     what it costs here, this year
     * @param blockedBy why it cannot be bought, or empty
     */
    public record Listing(String name, Property.Type type, int price, String blockedBy) {

        public boolean isOpen() {
            return blockedBy.isEmpty();
        }

        /** Turns the listing into the thing you own. */
        public Property toProperty() {
            return new Property(name, type, price);
        }

        public String summary() {
            return type.description()
                    + "  ·  " + type.baseIncome() + " dirhams a year"
                    + (type.stressModifier() < 0
                            ? "  ·  eases the mind"
                            : "");
        }
    }

    private static final String[] STREETS = {
            "the Street of Coppersmiths",
            "the Tanners' Row",
            "the Gate of the Booksellers",
            "the Lane of Date Sellers",
            "the Old Cistern",
            "the Weavers' Quarter",
            "the Northern Wall",
            "the Spice Market",
            "the Potters' Yard",
            "the Bridge Road"
    };

    /**
     * This year's listings.
     *
     * <p>Deliberately a short, changing list rather than a catalogue: a market
     * you can exhaust is a menu, and a market that offers everything forever
     * makes waiting free.
     */
    public static List<Listing> listings(PlayerCharacter player, City city, long seed) {
        Random random = new Random(seed);
        List<Listing> listings = new ArrayList<>();

        for (Property.Type type : Property.Type.values()) {
            if (!offeredHere(type, city, random)) {
                continue;
            }

            int price = priceOf(type, city, random);
            String name = nameFor(type, random);

            String blocked = "";

            if (player.getNetWorth() < price) {
                blocked = "You are " + (price - (int) player.getNetWorth()) + " dirhams short.";
            } else if (ownsOneLike(player, type) && type != Property.Type.SHOP) {
                blocked = "You already hold one.";
            }

            listings.add(new Listing(name, type, price, blocked));
        }

        return listings;
    }

    /**
     * Not everything is for sale everywhere. A city under siege is not
     * selling gardens, and a city with no trade is not selling caravansaries.
     */
    private static boolean offeredHere(Property.Type type, City city, Random random) {
        if (city == null) {
            return random.nextInt(100) < 70;
        }

        int chance = switch (type) {
            case HOUSE -> 85;
            case GARDEN -> city.getWar() > 45 ? 15 : 70;
            case SHOP -> 40 + city.getTrade() / 2;
            case FARMLAND -> 70 - city.getWar() / 2;
            case WORKSHOP -> 30 + city.getProsperity() / 2;
            case CARAVANSARY -> city.getTrade() >= 55 ? 45 : 10;
            case BATHHOUSE -> city.getProsperity() >= 55 ? 40 : 12;
        };

        return random.nextInt(100) < chance;
    }

    /**
     * What it costs here. A prosperous city charges more for the same walls,
     * and a city being emptied by plague or war charges much less.
     */
    private static int priceOf(Property.Type type, City city, Random random) {
        double price = type.basePrice();

        if (city != null) {
            price *= 0.70 + (city.getProsperity() / 140.0);
            price *= 1.0 - (city.getWar() / 260.0);
            price *= 1.0 - (city.getDisease() / 300.0);
        }

        // A little noise, so two cities with the same numbers are not the
        // same market.
        price *= 0.88 + random.nextDouble() * 0.28;

        return Math.max(40, (int) Math.round(price / 10) * 10);
    }

    private static String nameFor(Property.Type type, Random random) {
        String street = STREETS[random.nextInt(STREETS.length)];

        return switch (type) {
            case HOUSE -> "A house on " + street;
            case SHOP -> "A shop on " + street;
            case FARMLAND -> "Land beyond the walls";
            case WORKSHOP -> "A workshop on " + street;
            case CARAVANSARY -> "The caravansary by " + street;
            case BATHHOUSE -> "The baths at " + street;
            case GARDEN -> "A walled garden near " + street;
        };
    }

    private static boolean ownsOneLike(PlayerCharacter player, Property.Type type) {
        for (Property owned : player.getProperties()) {
            if (owned.type() == type) {
                return true;
            }
        }

        return false;
    }

    /**
     * What this year does to a holding when nobody is looking after it.
     *
     * <p>Returns a line for the log when the damage is worth reporting, and
     * an empty string when the year was simply a year.
     */
    public static String weatherOneYear(Property property, City city) {
        property.degrade();

        if (city == null) {
            return "";
        }

        int hazard = Math.max(city.getWar(), Math.max(city.getCrime() - 20, city.getDisease() - 30));

        if (hazard <= 0 || RANDOM.nextInt(100) >= hazard / 3) {
            return "";
        }

        int damage = 6 + RANDOM.nextInt(14);
        property.damage(damage);

        if (city.getWar() >= 50) {
            return property.name() + " was damaged in the fighting.";
        }

        if (city.getCrime() >= 55) {
            return property.name() + " was broken into and stripped of what would carry.";
        }

        return property.name() + " has been standing empty, and it shows.";
    }
}
