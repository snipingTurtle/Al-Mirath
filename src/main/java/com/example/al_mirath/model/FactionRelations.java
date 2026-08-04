package com.example.al_mirath.model;

public class FactionRelations {

    private int court;
    private int nobles;
    private int military;
    private int scholars;
    private int merchants;
    private int commonPeople;
    private int familyCouncil;
    private int shadowNetwork;

    public FactionRelations() {
        this.court = 40;
        this.nobles = 40;
        this.military = 40;
        this.scholars = 40;
        this.merchants = 40;
        this.commonPeople = 40;
        this.familyCouncil = 40;
        this.shadowNetwork = 25;
    }

    public FactionRelations(
            int court,
            int nobles,
            int military,
            int scholars,
            int merchants,
            int commonPeople,
            int familyCouncil,
            int shadowNetwork
    ) {
        this.court = clamp(court);
        this.nobles = clamp(nobles);
        this.military = clamp(military);
        this.scholars = clamp(scholars);
        this.merchants = clamp(merchants);
        this.commonPeople = clamp(commonPeople);
        this.familyCouncil = clamp(familyCouncil);
        this.shadowNetwork = clamp(shadowNetwork);
    }

    public void applyChange(String faction, int amount) {
        switch (faction) {
            case "court" -> court = clamp(court + amount);
            case "nobles" -> nobles = clamp(nobles + amount);
            case "military" -> military = clamp(military + amount);
            case "scholars" -> scholars = clamp(scholars + amount);
            case "merchants" -> merchants = clamp(merchants + amount);
            case "commonPeople" -> commonPeople = clamp(commonPeople + amount);
            case "familyCouncil" -> familyCouncil = clamp(familyCouncil + amount);
            case "shadowNetwork" -> shadowNetwork = clamp(shadowNetwork + amount);
            default -> System.out.println("Unknown faction: " + faction);
        }
    }

    public int getValue(String faction) {
        return switch (faction) {
            case "court" -> court;
            case "nobles" -> nobles;
            case "military" -> military;
            case "scholars" -> scholars;
            case "merchants" -> merchants;
            case "commonPeople" -> commonPeople;
            case "familyCouncil" -> familyCouncil;
            case "shadowNetwork" -> shadowNetwork;
            default -> 0;
        };
    }

    /**
     * Overwrites a standing outright instead of nudging it by a delta.
     * Used when rewinding a life back to a recorded snapshot.
     */
    public void setValue(String faction, int value) {
        int clamped = clamp(value);

        switch (faction) {
            case "court" -> court = clamped;
            case "nobles" -> nobles = clamped;
            case "military" -> military = clamped;
            case "scholars" -> scholars = clamped;
            case "merchants" -> merchants = clamped;
            case "commonPeople" -> commonPeople = clamped;
            case "familyCouncil" -> familyCouncil = clamped;
            case "shadowNetwork" -> shadowNetwork = clamped;
            default -> System.out.println("Unknown faction: " + faction);
        }
    }

    private int clamp(int value) {
        return Math.max(0, Math.min(100, value));
    }

    public int getCourt() {
        return court;
    }

    public int getNobles() {
        return nobles;
    }

    public int getMilitary() {
        return military;
    }

    public int getScholars() {
        return scholars;
    }

    public int getMerchants() {
        return merchants;
    }

    public int getCommonPeople() {
        return commonPeople;
    }

    public int getFamilyCouncil() {
        return familyCouncil;
    }

    public int getShadowNetwork() {
        return shadowNetwork;
    }
}