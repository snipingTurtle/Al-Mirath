package com.example.al_mirath.model;

public class LegacyRecord {

    private final String characterName;
    private final String era;
    private final String origin;
    private final String familyCondition;
    private final String endingTitle;
    private final String legacyTitles;

    public LegacyRecord(
            String characterName,
            String era,
            String origin,
            String familyCondition,
            String endingTitle,
            String legacyTitles
    ) {
        this.characterName = characterName;
        this.era = era;
        this.origin = origin;
        this.familyCondition = familyCondition;
        this.endingTitle = endingTitle;
        this.legacyTitles = legacyTitles;
    }

    public String getCharacterName() {
        return characterName;
    }

    public String getEra() {
        return era;
    }

    public String getOrigin() {
        return origin;
    }

    public String getFamilyCondition() {
        return familyCondition;
    }

    public String getEndingTitle() {
        return endingTitle;
    }

    public String getLegacyTitles() {
        return legacyTitles;
    }

    public String getDisplayText() {
        return characterName
                + "\nEra: " + era
                + "\nOrigin: " + origin
                + "\nFamily: " + familyCondition
                + "\nEnding: " + endingTitle
                + "\nTitles: " + legacyTitles;
    }
}