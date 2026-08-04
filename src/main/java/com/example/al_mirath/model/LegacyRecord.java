package com.example.al_mirath.model;

/**
 * One completed life, as stored in the Legacy Records archive.
 */
public class LegacyRecord {

    private final String characterName;
    private final String era;
    private final String origin;
    private final String familyCondition;
    private final String endingTitle;
    private final String legacyTitles;
    private final int ageAtEnd;
    private final String finalStatus;
    private final int score;

    public LegacyRecord(
            String characterName,
            String era,
            String origin,
            String familyCondition,
            String endingTitle,
            String legacyTitles,
            int ageAtEnd,
            String finalStatus,
            int score
    ) {
        this.characterName = characterName;
        this.era = era;
        this.origin = origin;
        this.familyCondition = familyCondition;
        this.endingTitle = endingTitle;
        this.legacyTitles = legacyTitles;
        this.ageAtEnd = ageAtEnd;
        this.finalStatus = finalStatus;
        this.score = score;
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

    public int getAgeAtEnd() {
        return ageAtEnd;
    }

    public String getFinalStatus() {
        return finalStatus;
    }

    public int getScore() {
        return score;
    }

    public String getDisplayText() {
        return characterName
                + "\nEra: " + era
                + "\nOrigin: " + origin
                + "\nFamily: " + familyCondition
                + "\nDied at: " + ageAtEnd
                + "\nFinal Status: " + finalStatus
                + "\nEnding: " + endingTitle
                + "\nTitles: " + legacyTitles
                + "\nScore: " + score;
    }
}
