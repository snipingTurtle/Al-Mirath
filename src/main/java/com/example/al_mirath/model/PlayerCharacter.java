package com.example.al_mirath.model;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public class PlayerCharacter {

    private final String name;
    private final String era;
    private final String origin;
    private final String familyCondition;
    private final String trait;
    private int age;
    private boolean alive = true;
    private String currentStatus;
    private String deathReason = "";
    private final List<String> legacyTitles = new ArrayList<>();

    private int health;
    private int wealth;
    private int education;
    private int reputation;
    private int politicalPower;
    private int morality;
    private int familyLoyalty;
    private int stress;

    /**
     * Liquid money, in dirhams. Distinct from the {@code wealth} stat, which
     * is standing rather than coin: a landed noble with an empty purse is
     * wealthy and broke at the same time, and both facts should be playable.
     */
    private double netWorth;

    private Career career = Career.UNEMPLOYED;
    private int careerRank = 0;
    private int yearsInCareer = 0;
    private int yearsInRank = 0;
    private boolean retired = false;

    private final List<Property> properties = new ArrayList<>();

    /**
     * Activity ids already attempted this year. A life is lived a year at a
     * time, so the same afternoon cannot be spent twice; clearing this is
     * what makes ageing up feel like time actually passing.
     */
    private final Set<String> spentThisYear = new LinkedHashSet<>();

    public PlayerCharacter(
            String name,
            String era,
            String origin,
            String familyCondition,
            String trait,
            int age,
            int health,
            int wealth,
            int education,
            int reputation,
            int politicalPower,
            int morality,
            int familyLoyalty,
            int stress
    ) {
        this.name = name;
        this.era = era;
        this.origin = origin;
        this.familyCondition = familyCondition;
        this.trait = trait;
        this.age = age;

        this.health = clamp(health);
        this.wealth = clamp(wealth);
        this.education = clamp(education);
        this.reputation = clamp(reputation);
        this.politicalPower = clamp(politicalPower);
        this.morality = clamp(morality);
        this.familyLoyalty = clamp(familyLoyalty);
        this.stress = clamp(stress);
        this.currentStatus = origin;

        this.netWorth = startingPurseFor(origin);
    }

    /**
     * What the household puts in a newborn's name.
     *
     * <p>Scaled against the career incomes in {@link Career} and the prices in
     * {@link Property}: a prince begins able to buy a caravansary outright, a
     * merchant's child able to buy a shop after a few good years, and a
     * village child able to buy nothing at all.
     */
    private static double startingPurseFor(String origin) {
        if (origin == null) {
            return 40;
        }

        return switch (origin) {
            case "Royal House" -> 20000;
            case "Nobility" -> 6000;
            case "Merchant Guild" -> 2400;
            case "Scholar Household", "Military Household" -> 600;
            case "Village Orphan", "Poor Village Child", "Poor Urban Child" -> 10;
            default -> 120;
        };
    }

    public double getNetWorth() {
        return netWorth;
    }

    public void setNetWorth(double netWorth) {
        this.netWorth = netWorth;
    }

    public void addNetWorth(double amount) {
        this.netWorth += amount;
    }

    public int getStatValue(String stat) {
        return switch (stat) {
            case "health" -> health;
            case "wealth" -> wealth;
            case "education" -> education;
            case "reputation" -> reputation;
            case "politicalPower" -> politicalPower;
            case "morality" -> morality;
            case "familyLoyalty" -> familyLoyalty;
            case "stress" -> stress;
            default -> 0;
        };
    }

    /**
     * Overwrites a stat outright instead of nudging it by a delta.
     * Used when rewinding a life back to a recorded snapshot.
     */
    public void setStatValue(String stat, int value) {
        int clamped = clamp(value);

        switch (stat) {
            case "health" -> health = clamped;
            case "wealth" -> wealth = clamped;
            case "education" -> education = clamped;
            case "reputation" -> reputation = clamped;
            case "politicalPower" -> politicalPower = clamped;
            case "morality" -> morality = clamped;
            case "familyLoyalty" -> familyLoyalty = clamped;
            case "stress" -> stress = clamped;
            default -> System.out.println("Unknown stat: " + stat);
        }
    }

    public void setAge(int age) {
        this.age = Math.max(0, age);
    }

    /** Brings the character back to life when a rewind undoes a fatal choice. */
    public void restoreLife() {
        this.alive = true;
        this.deathReason = "";
    }

    public void replaceLegacyTitles(List<String> titles) {
        legacyTitles.clear();
        legacyTitles.addAll(titles);
    }

    public void applyChange(String stat, int amount) {
        switch (stat) {
            case "health" -> health = clamp(health + amount);
            case "wealth" -> wealth = clamp(wealth + amount);
            case "education" -> education = clamp(education + amount);
            case "reputation" -> reputation = clamp(reputation + amount);
            case "politicalPower" -> politicalPower = clamp(politicalPower + amount);
            case "morality" -> morality = clamp(morality + amount);
            case "familyLoyalty" -> familyLoyalty = clamp(familyLoyalty + amount);
            case "stress" -> stress = clamp(stress + dampenStressIncrease(amount));
            default -> System.out.println("Unknown stat: " + stat);
        }
    }

    /**
     * Softens further stress increases once stress is already high.
     *
     * <p>Without this, stress reaches 100 within a handful of choices and stays
     * there for the rest of the life, flattening every later decision. A person
     * already under heavy pressure grows some resistance to additional strain,
     * while relief (a negative amount) is never dampened.
     */
    private int dampenStressIncrease(int amount) {
        if (amount <= 0) {
            return amount;
        }

        double factor;

        if (stress >= 85) {
            factor = 0.35;
        } else if (stress >= 70) {
            factor = 0.55;
        } else if (stress >= 55) {
            factor = 0.78;
        } else {
            factor = 1.0;
        }

        return Math.max(1, (int) Math.round(amount * factor));
    }

    private int clamp(int value) {
        return Math.max(0, Math.min(100, value));
    }

    public String getName() {
        return name;
    }

    public String getEra() {
        return era;
    }

    public int getAge() {
        return age;
    }

    public void increaseAge(int years) {
        age += years;
    }

    public boolean isAlive() {
        return alive;
    }

    public String getDeathReason() {
        return deathReason;
    }

    public void markDead(String deathReason) {
        this.alive = false;
        this.deathReason = deathReason;
    }

    public String getCurrentStatus() {
        return currentStatus;
    }

    public void setCurrentStatus(String currentStatus) {
        this.currentStatus = currentStatus;
    }

    public String getOrigin() {
        return origin;
    }

    public String getFamilyCondition() {
        return familyCondition;
    }

    public String getTrait() {
        return trait;
    }

    public int getHealth() {
        return health;
    }

    public int getWealth() {
        return wealth;
    }

    public int getEducation() {
        return education;
    }

    public int getReputation() {
        return reputation;
    }

    public int getPoliticalPower() {
        return politicalPower;
    }

    public int getMorality() {
        return morality;
    }

    public int getFamilyLoyalty() {
        return familyLoyalty;
    }

    public int getStress() {
        return stress;
    }

    public boolean addLegacyTitle(String title) {
        if (!legacyTitles.contains(title)) {
            legacyTitles.add(title);
            return true;
        }

        return false;
    }

    public boolean hasLegacyTitle(String title) {
        return legacyTitles.contains(title);
    }

    public List<String> getLegacyTitles() {
        return legacyTitles;
    }

    public String getLegacyTitlesText() {
        if (legacyTitles.isEmpty()) {
            return "None";
        }

        return String.join(", ", legacyTitles);
    }

    // ── Coin ────────────────────────────────────────────────────────────

    /**
     * Spends from the purse if there is enough in it.
     *
     * @return false when the purse is short, having taken nothing
     */
    public boolean spend(double amount) {
        if (amount <= 0) {
            return true;
        }

        if (netWorth < amount) {
            return false;
        }

        netWorth -= amount;
        return true;
    }

    /** "1,240 dirhams", or "in debt" when the purse has gone under. */
    public String purseText() {
        if (netWorth < 0) {
            return String.format("%,d dirhams owed", (long) Math.abs(netWorth));
        }

        return String.format("%,d dirhams", (long) netWorth);
    }

    // ── Trade and calling ───────────────────────────────────────────────

    public Career getCareer() {
        return career;
    }

    public int getCareerRank() {
        return careerRank;
    }

    public int getYearsInCareer() {
        return yearsInCareer;
    }

    public int getYearsInRank() {
        return yearsInRank;
    }

    public boolean isRetired() {
        return retired;
    }

    public boolean isEmployed() {
        return career != Career.UNEMPLOYED && !retired;
    }

    /** Takes up a trade at the given rung, resetting time served. */
    public void takeCareer(Career career, int rank) {
        this.career = career == null ? Career.UNEMPLOYED : career;
        this.careerRank = Math.max(0, Math.min(rank, this.career.maxRank()));
        this.yearsInCareer = 0;
        this.yearsInRank = 0;
        this.retired = false;
    }

    /** Moves up one rung. Returns false when already at the top. */
    public boolean promote() {
        if (careerRank >= career.maxRank()) {
            return false;
        }

        careerRank++;
        yearsInRank = 0;
        return true;
    }

    /** Moves down one rung, as a public failure does. */
    public void demote() {
        if (careerRank > 0) {
            careerRank--;
            yearsInRank = 0;
        }
    }

    public void leaveCareer() {
        this.career = Career.UNEMPLOYED;
        this.careerRank = 0;
        this.yearsInCareer = 0;
        this.yearsInRank = 0;
    }

    public void retire() {
        this.retired = true;
    }

    /** Restores a saved career without pretending it was just taken up. */
    public void restoreCareer(Career career, int rank, int yearsInCareer,
                              int yearsInRank, boolean retired) {
        this.career = career == null ? Career.UNEMPLOYED : career;
        this.careerRank = Math.max(0, Math.min(rank, this.career.maxRank()));
        this.yearsInCareer = Math.max(0, yearsInCareer);
        this.yearsInRank = Math.max(0, yearsInRank);
        this.retired = retired;
    }

    /** Annual pay at the current rung, or nothing when out of work. */
    public int annualIncome() {
        return isEmployed() ? career.incomeAt(careerRank) : 0;
    }

    /** "Copyist of the madrasa", or "No trade" when out of work. */
    public String careerTitle() {
        if (retired) {
            return "Retired " + career.rank(careerRank).title();
        }

        if (career == Career.UNEMPLOYED) {
            return "No trade";
        }

        return career.rank(careerRank).title() + ", " + career.displayName();
    }

    /** A year served at the current rung. */
    public void recordYearWorked() {
        yearsInCareer++;
        yearsInRank++;
    }

    // ── Holdings ────────────────────────────────────────────────────────

    public List<Property> getProperties() {
        return properties;
    }

    public void addProperty(Property property) {
        if (property != null) {
            properties.add(property);
        }
    }

    public boolean removeProperty(Property property) {
        return properties.remove(property);
    }

    public void replaceProperties(List<Property> replacement) {
        properties.clear();

        if (replacement != null) {
            properties.addAll(replacement);
        }
    }

    /** What the holdings pay this year, after their condition is accounted for. */
    public int propertyIncome() {
        int total = 0;

        for (Property property : properties) {
            total += property.effectiveIncome();
        }

        return total;
    }

    /** Everything owned, at what it would fetch today. */
    public double estateValue() {
        double total = netWorth;

        for (Property property : properties) {
            total += property.resaleValue();
        }

        return total;
    }

    // ── The year's remaining hours ──────────────────────────────────────

    /** True once this activity has been spent this year. */
    public boolean hasSpentThisYear(String activityId) {
        return spentThisYear.contains(activityId);
    }

    public void markSpentThisYear(String activityId) {
        if (activityId != null) {
            spentThisYear.add(activityId);
        }
    }

    /** A new year: everything is available again. */
    public void clearYearlyActivities() {
        spentThisYear.clear();
    }

    public Set<String> getSpentThisYear() {
        return spentThisYear;
    }
}
