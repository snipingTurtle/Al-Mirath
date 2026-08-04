package com.example.al_mirath.model;

public enum RelationshipType {

    FRIEND,
    RIVAL,
    MENTOR,
    ALLY,
    FAMILY_FRIEND,
    PATRON,
    STRANGER;

    public String displayName() {
        return switch (this) {
            case FRIEND -> "Friend";
            case RIVAL -> "Rival";
            case MENTOR -> "Mentor";
            case ALLY -> "Ally";
            case FAMILY_FRIEND -> "Family Friend";
            case PATRON -> "Patron";
            case STRANGER -> "Acquaintance";
        };
    }
}