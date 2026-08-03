package com.example.al_mirath.model;

import java.util.ArrayList;
import java.util.List;

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

    public void applyChange(String stat, int amount) {
        switch (stat) {
            case "health" -> health = clamp(health + amount);
            case "wealth" -> wealth = clamp(wealth + amount);
            case "education" -> education = clamp(education + amount);
            case "reputation" -> reputation = clamp(reputation + amount);
            case "politicalPower" -> politicalPower = clamp(politicalPower + amount);
            case "morality" -> morality = clamp(morality + amount);
            case "familyLoyalty" -> familyLoyalty = clamp(familyLoyalty + amount);
            case "stress" -> stress = clamp(stress + amount);
            default -> System.out.println("Unknown stat: " + stat);
        }
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
}