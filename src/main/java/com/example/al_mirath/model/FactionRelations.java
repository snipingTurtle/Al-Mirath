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
